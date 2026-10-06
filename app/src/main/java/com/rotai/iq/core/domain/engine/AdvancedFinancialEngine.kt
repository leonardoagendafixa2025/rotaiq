package com.rotai.iq.core.domain.engine

import com.rotai.iq.core.domain.model.ComprehensiveFinancialReport
import com.rotai.iq.core.domain.model.FinancialPeriod
import com.rotai.iq.core.domain.model.FuelRecord
import com.rotai.iq.core.domain.model.MaintenanceRecord
import com.rotai.iq.core.domain.model.RideEvaluation
import com.rotai.iq.core.domain.model.Vehicle
import com.rotai.iq.core.domain.model.VehicleExpense

object AdvancedFinancialEngine {

    fun generateReport(
        period: FinancialPeriod,
        periodLabel: String,
        rides: List<RideEvaluation>,
        fuelRecords: List<FuelRecord>,
        maintenanceRecords: List<MaintenanceRecord>,
        otherExpenses: List<VehicleExpense>,
        vehicle: Vehicle,
        hoursOnline: Double,
        hoursDriving: Double
    ): ComprehensiveFinancialReport {
        val grossRevenue = rides.sumOf { it.grossFare }
        val totalKmDriven = rides.sumOf { it.totalDistanceKm }
        val ridesCompleted = rides.size

        // Combustível real (ou estimado se não houver registros manuais)
        val fuelSum = fuelRecords.sumOf { it.totalPaid }
        val fuelCosts = if (fuelSum > 0.0) fuelSum else (vehicle.fuelCostPerKm * totalKmDriven)

        // Manutenção real (ou estimada pelo km rodado)
        val maintSum = maintenanceRecords.sumOf { it.cost }
        val maintenanceCosts = if (maintSum > 0.0) maintSum else (vehicle.maintenanceCostPerKm * totalKmDriven)

        // Custos fixos proporcionais ao período
        val fixedCosts = when (period) {
            FinancialPeriod.DAILY -> vehicle.totalMonthlyFixedCosts / 30.0
            FinancialPeriod.WEEKLY -> (vehicle.totalMonthlyFixedCosts / 30.0) * 7.0
            FinancialPeriod.MONTHLY -> vehicle.totalMonthlyFixedCosts
            FinancialPeriod.ANNUAL -> vehicle.totalMonthlyFixedCosts * 12.0
        }

        val otherExpensesCost = otherExpenses.sumOf { it.amount }

        return ComprehensiveFinancialReport(
            period = period,
            periodLabel = periodLabel,
            grossRevenue = grossRevenue,
            fuelCosts = fuelCosts,
            maintenanceCosts = maintenanceCosts,
            fixedCosts = fixedCosts,
            otherExpenses = otherExpensesCost,
            totalKmDriven = totalKmDriven,
            totalHoursOnline = hoursOnline,
            totalHoursDriving = hoursDriving,
            totalRidesCompleted = ridesCompleted
        )
    }
}
