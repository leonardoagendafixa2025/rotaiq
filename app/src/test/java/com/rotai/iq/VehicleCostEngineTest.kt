package com.rotai.iq

import com.google.common.truth.Truth.assertThat
import com.rotai.iq.core.domain.engine.VehicleCostEngine
import com.rotai.iq.core.domain.model.FuelType
import com.rotai.iq.core.domain.model.Vehicle
import org.junit.Test

class VehicleCostEngineTest {

    @Test
    fun calculateTripCost_standardVehicle_computesCorrectBreakdown() {
        val vehicle = Vehicle(
            fuelPricePerLiter = 6.00,
            consumptionKmPerLiter = 12.0,      // Fuel = 0.50 R$/km
            maintenanceCostPerKm = 0.20,       // Maintenance = 0.20 R$/km
            monthlyInsuranceCost = 200.0,
            annualTaxesCost = 1200.0,          // Taxes = 100 R$/mês
            monthlyDepreciation = 300.0,
            monthlyOtherCosts = 0.0,           // Total fixed monthly = 600 R$
            estimatedMonthlyKm = 3000.0        // Fixed = 600 / 3000 = 0.20 R$/km
        )

        // Total cost per km = 0.50 + 0.20 + 0.20 = 0.90 R$/km
        assertThat(vehicle.fuelCostPerKm).isWithin(0.001).of(0.50)
        assertThat(vehicle.fixedCostPerKm).isWithin(0.001).of(0.20)
        assertThat(vehicle.totalCostPerKm).isWithin(0.001).of(0.90)

        val breakdown = VehicleCostEngine.calculateTripCost(vehicle, 10.0)
        assertThat(breakdown.fuelCost).isWithin(0.001).of(5.00)
        assertThat(breakdown.maintenanceCost).isWithin(0.001).of(2.00)
        assertThat(breakdown.fixedCost).isWithin(0.001).of(2.00)
        assertThat(breakdown.totalCost).isWithin(0.001).of(9.00)
        assertThat(breakdown.costPerKm).isWithin(0.001).of(0.90)
    }

    @Test
    fun calculateTripCost_lowCostGnvVehicle_hasMuchLowerCostPerKm() {
        val gnvVehicle = Vehicle(
            fuelType = FuelType.CNG,
            fuelPricePerLiter = 4.20,
            consumptionKmPerLiter = 14.0,       // Fuel = 0.30 R$/km
            maintenanceCostPerKm = 0.12,
            monthlyInsuranceCost = 150.0,
            annualTaxesCost = 600.0,           // 50 R$/mês
            monthlyDepreciation = 200.0,
            monthlyOtherCosts = 0.0,           // Total fixed = 400 R$
            estimatedMonthlyKm = 4000.0        // Fixed = 0.10 R$/km
        )

        // Total cost per km = 0.30 + 0.12 + 0.10 = 0.52 R$/km
        assertThat(gnvVehicle.totalCostPerKm).isWithin(0.01).of(0.52)
        val breakdown = VehicleCostEngine.calculateTripCost(gnvVehicle, 20.0)
        assertThat(breakdown.totalCost).isWithin(0.01).of(10.40)
    }

    @Test
    fun calculateTripCost_highCostGasGuzzler_hasHighCostPerKm() {
        val heavyVehicle = Vehicle(
            fuelPricePerLiter = 6.20,
            consumptionKmPerLiter = 7.0,        // Fuel = ~0.885 R$/km
            maintenanceCostPerKm = 0.35,
            monthlyInsuranceCost = 350.0,
            annualTaxesCost = 2400.0,          // 200 R$/mês
            monthlyDepreciation = 500.0,
            monthlyOtherCosts = 150.0,         // Fixed = 1200 R$
            estimatedMonthlyKm = 2000.0        // Fixed = 0.60 R$/km
        )

        // Total cost per km = 0.885 + 0.35 + 0.60 = 1.835 R$/km
        assertThat(heavyVehicle.totalCostPerKm).isGreaterThan(1.80)
    }

    @Test
    fun calculateProjections_computesAccurateHourlyAndAnnualProjections() {
        val vehicle = Vehicle(
            fuelPricePerLiter = 5.00,
            consumptionKmPerLiter = 10.0,
            maintenanceCostPerKm = 0.20,
            monthlyInsuranceCost = 300.0,
            annualTaxesCost = 1200.0,
            monthlyDepreciation = 300.0,
            monthlyOtherCosts = 100.0,
            estimatedMonthlyKm = 2000.0
        )
        val projections = VehicleCostEngine.calculateProjections(vehicle, avgKmPerHour = 25.0)

        // costPerKm = 0.50 (fuel) + 0.20 (maint) + (800 / 2000 = 0.40 fixed) = 1.10 R$/km
        assertThat(projections.costPerKm).isWithin(0.01).of(1.10)
        // 1.10 * 25 = 27.50 R$/h
        assertThat(projections.costPerHourEstimated).isWithin(0.01).of(27.50)
        // monthly fixed = 800 R$
        assertThat(projections.monthlyFixedCost).isWithin(0.01).of(800.0)
        // daily fixed = 800 / 30 = 26.66 R$
        assertThat(projections.dailyFixedCost).isWithin(0.1).of(26.66)
    }
}
