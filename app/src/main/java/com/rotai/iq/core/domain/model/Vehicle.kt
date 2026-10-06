package com.rotai.iq.core.domain.model

data class Vehicle(
    val id: String = "default_vehicle",
    val name: String = "Meu Carro",
    val model: String = "Sedan / Hatch",
    val plate: String = "ABC-1D23",
    val year: Int = 2022,
    val fuelType: FuelType = FuelType.GASOLINE,
    val consumptionKmPerLiter: Double = 11.0,      // km/L
    val fuelPricePerLiter: Double = 5.89,           // R$/L
    val maintenanceCostPerKm: Double = 0.18,        // R$/km (pneus, óleo, freios, suspensão)
    val monthlyInsuranceCost: Double = 220.0,       // R$/mês
    val annualTaxesCost: Double = 1800.0,           // R$/ano (IPVA + Licenciamento)
    val monthlyDepreciation: Double = 350.0,        // R$/mês
    val monthlyOtherCosts: Double = 150.0,          // R$/mês (lavagem, seguro extra, etc)
    val estimatedMonthlyKm: Double = 3000.0,        // km rodados/mês
    val isActive: Boolean = true
) {
    val fuelCostPerKm: Double
        get() = if (consumptionKmPerLiter > 0.0) fuelPricePerLiter / consumptionKmPerLiter else 0.0

    val monthlyTaxesCost: Double
        get() = annualTaxesCost / 12.0

    val totalMonthlyFixedCosts: Double
        get() = monthlyInsuranceCost + monthlyTaxesCost + monthlyDepreciation + monthlyOtherCosts

    val fixedCostPerKm: Double
        get() = if (estimatedMonthlyKm > 0.0) totalMonthlyFixedCosts / estimatedMonthlyKm else 0.0

    val totalCostPerKm: Double
        get() = fuelCostPerKm + maintenanceCostPerKm + fixedCostPerKm

    fun calculateTripCost(totalKm: Double): VehicleCostBreakdown {
        val fuel = fuelCostPerKm * totalKm
        val maintenance = maintenanceCostPerKm * totalKm
        val fixed = fixedCostPerKm * totalKm
        val total = fuel + maintenance + fixed
        return VehicleCostBreakdown(
            fuelCost = fuel,
            maintenanceCost = maintenance,
            fixedCost = fixed,
            totalCost = total,
            costPerKm = totalCostPerKm,
            totalKm = totalKm
        )
    }
}

data class VehicleCostBreakdown(
    val fuelCost: Double,
    val maintenanceCost: Double,
    val fixedCost: Double,
    val totalCost: Double,
    val costPerKm: Double,
    val totalKm: Double
)
