package com.rotai.iq

import com.google.common.truth.Truth.assertThat
import com.rotai.iq.core.domain.engine.AdvancedFinancialEngine
import com.rotai.iq.core.domain.model.FinancialPeriod
import com.rotai.iq.core.domain.model.FuelRecord
import com.rotai.iq.core.domain.model.MaintenanceRecord
import com.rotai.iq.core.domain.model.MaintenanceType
import com.rotai.iq.core.domain.model.RideEvaluation
import com.rotai.iq.core.domain.model.RideOffer
import com.rotai.iq.core.domain.model.Vehicle
import com.rotai.iq.core.domain.model.VehicleExpense
import org.junit.Test

class AdvancedFinancialEngineTest {

    private val vehicle = Vehicle(
        consumptionKmPerLiter = 10.0,
        fuelPricePerLiter = 5.00,        // fuelCostPerKm = 0.50
        maintenanceCostPerKm = 0.20,
        monthlyInsuranceCost = 300.0,
        annualTaxesCost = 1200.0,
        monthlyDepreciation = 300.0,
        monthlyOtherCosts = 100.0,
        estimatedMonthlyKm = 2000.0      // fixed monthly = 800 R$ (daily = 26.67 R$)
    )

    @Test
    fun generateReport_dailyPeriod_calculatesAccurateNetProfitAndMargins() {
        // 2 corridas no dia: total R$ 100,00, 40 km rodados
        val rides = listOf(
            RideEvaluation(
                offer = RideOffer(grossFare = 60.0, distanceKm = 20.0, durationMinutes = 30.0, pickupDistanceKm = 2.0, pickupDurationMinutes = 5.0),
                score = 85,
                classification = com.rotai.iq.core.domain.model.EvaluationClassification.EXCELLENT,
                grossFare = 60.0,
                estimatedCost = 15.0,
                netProfit = 45.0,
                profitMarginPercent = 75.0,
                grossRatePerKm = 2.72,
                netRatePerKm = 2.04,
                grossRatePerHour = 102.8,
                netRatePerHour = 77.1,
                grossRatePerMinute = 1.71,
                totalDistanceKm = 22.0,
                totalDurationMinutes = 35.0,
                reasons = emptyList(),
                alerts = emptyList()
            ),
            RideEvaluation(
                offer = RideOffer(grossFare = 40.0, distanceKm = 15.0, durationMinutes = 25.0, pickupDistanceKm = 3.0, pickupDurationMinutes = 5.0),
                score = 75,
                classification = com.rotai.iq.core.domain.model.EvaluationClassification.GOOD,
                grossFare = 40.0,
                estimatedCost = 12.0,
                netProfit = 28.0,
                profitMarginPercent = 70.0,
                grossRatePerKm = 2.22,
                netRatePerKm = 1.55,
                grossRatePerHour = 80.0,
                netRatePerHour = 56.0,
                grossRatePerMinute = 1.33,
                totalDistanceKm = 18.0,
                totalDurationMinutes = 30.0,
                reasons = emptyList(),
                alerts = emptyList()
            )
        )

        val report = AdvancedFinancialEngine.generateReport(
            period = FinancialPeriod.DAILY,
            periodLabel = "Hoje",
            rides = rides,
            fuelRecords = emptyList(), // usa estimativa do veículo: 40 km * 0.50 = 20.00
            maintenanceRecords = emptyList(), // usa estimativa do veículo: 40 km * 0.20 = 8.00
            otherExpenses = emptyList(),
            vehicle = vehicle,
            hoursOnline = 4.0,
            hoursDriving = 3.0
        )

        assertThat(report.grossRevenue).isEqualTo(100.0)
        assertThat(report.totalKmDriven).isEqualTo(40.0)
        assertThat(report.fuelCosts).isWithin(0.01).of(20.0)
        assertThat(report.maintenanceCosts).isWithin(0.01).of(8.0)
        assertThat(report.fixedCosts).isWithin(0.1).of(26.67)
        // total costs = 20 + 8 + 26.67 = 54.67
        assertThat(report.totalCosts).isWithin(0.1).of(54.67)
        // net profit = 100 - 54.67 = 45.33
        assertThat(report.netProfit).isWithin(0.1).of(45.33)
        assertThat(report.profitMarginPercent).isWithin(0.5).of(45.3)
        // gross rate per km = 100 / 40 = 2.50
        assertThat(report.grossRatePerKm).isWithin(0.01).of(2.50)
        // gross rate per hour = 100 / 4 = 25.00
        assertThat(report.grossRatePerHour).isWithin(0.01).of(25.00)
    }

    @Test
    fun generateReport_withManualFuelAndExpenses_usesRealExpensesInsteadOfEstimates() {
        val fuelRecord = FuelRecord(
            date = "2026-10-06",
            odometerKm = 50200.0,
            liters = 30.0,
            pricePerLiter = 5.00,
            totalPaid = 150.0 // R$ 150 gastos na bomba
        )
        val expense = VehicleExpense(
            date = "2026-10-06",
            category = com.rotai.iq.core.domain.model.ExpenseCategory.CAR_WASH,
            description = "Lavagem completa",
            amount = 40.0
        )

        val report = AdvancedFinancialEngine.generateReport(
            period = FinancialPeriod.DAILY,
            periodLabel = "Hoje",
            rides = emptyList(),
            fuelRecords = listOf(fuelRecord),
            maintenanceRecords = emptyList(),
            otherExpenses = listOf(expense),
            vehicle = vehicle,
            hoursOnline = 5.0,
            hoursDriving = 4.0
        )

        assertThat(report.fuelCosts).isEqualTo(150.0)
        assertThat(report.otherExpenses).isEqualTo(40.0)
        assertThat(report.netProfit).isLessThan(0.0) // Dia com prejuízo por abastecimento
    }
}
