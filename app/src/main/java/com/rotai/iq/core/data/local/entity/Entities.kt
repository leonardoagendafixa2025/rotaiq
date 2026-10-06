package com.rotai.iq.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey val id: String,
    val name: String,
    val model: String,
    val plate: String,
    val year: Int,
    val fuelType: String,
    val consumptionKmPerLiter: Double,
    val fuelPricePerLiter: Double,
    val maintenanceCostPerKm: Double,
    val monthlyInsuranceCost: Double,
    val annualTaxesCost: Double,
    val monthlyDepreciation: Double,
    val monthlyOtherCosts: Double,
    val estimatedMonthlyKm: Double,
    val isActive: Boolean
)

@Entity(tableName = "driver_goals")
data class DriverGoalEntity(
    @PrimaryKey val id: String,
    val dailyGrossTarget: Double,
    val dailyNetTarget: Double,
    val targetHourlyRate: Double,
    val targetKmRate: Double,
    val shiftTargetHours: Double,
    val currentDailyGross: Double,
    val currentDailyNet: Double,
    val hoursWorkedToday: Double,
    val kmDrivenToday: Double
)

@Entity(tableName = "driver_preferences")
data class DriverPreferenceEntity(
    @PrimaryKey val id: String,
    val minRatePerKm: Double,
    val minRatePerHour: Double,
    val maxPickupDistanceKm: Double,
    val maxStops: Int,
    val preferShortTrips: Boolean,
    val audioAlertsEnabled: Boolean,
    val overlayHudEnabled: Boolean
)

@Entity(tableName = "ride_evaluations")
data class RideEvaluationEntity(
    @PrimaryKey val id: String,
    val platform: String,
    val grossFare: Double,
    val distanceKm: Double,
    val durationMinutes: Double,
    val pickupDistanceKm: Double,
    val pickupDurationMinutes: Double,
    val stopsCount: Int,
    val category: String,
    val score: Int,
    val classification: String,
    val estimatedCost: Double,
    val netProfit: Double,
    val profitMarginPercent: Double,
    val grossRatePerKm: Double,
    val netRatePerKm: Double,
    val grossRatePerHour: Double,
    val netRatePerHour: Double,
    val grossRatePerMinute: Double,
    val totalDistanceKm: Double,
    val totalDurationMinutes: Double,
    val reasons: List<String>,
    val alerts: List<String>,
    val evaluatedAt: Long,
    val rawText: String?
)

@Entity(tableName = "daily_financials")
data class DailyFinancialEntity(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val totalGrossRevenue: Double,
    val totalEstimatedCosts: Double,
    val totalNetProfit: Double,
    val totalRidesEvaluated: Int,
    val totalRidesAccepted: Int,
    val totalRidesRejected: Int,
    val totalKmDriven: Double,
    val totalHoursOnline: Double,
    val avgGrossRatePerKm: Double,
    val avgGrossRatePerHour: Double,
    val avgNetProfitPerHour: Double
)

@Entity(tableName = "fuel_records")
data class FuelRecordEntity(
    @PrimaryKey val id: String,
    val vehicleId: String,
    val date: String,
    val odometerKm: Double,
    val liters: Double,
    val pricePerLiter: Double,
    val totalPaid: Double,
    val fuelType: String,
    val isFullTank: Boolean,
    val calculatedKmPerLiter: Double?,
    val calculatedCostPerKm: Double?,
    val notes: String?,
    val syncedWithServer: Boolean,
    val createdAt: Long
)

@Entity(tableName = "maintenance_records")
data class MaintenanceRecordEntity(
    @PrimaryKey val id: String,
    val vehicleId: String,
    val date: String,
    val odometerKm: Double,
    val type: String,
    val description: String,
    val cost: Double,
    val nextServiceKm: Double?,
    val isCompleted: Boolean,
    val notes: String?,
    val syncedWithServer: Boolean,
    val createdAt: Long
)

@Entity(tableName = "vehicle_expenses")
data class VehicleExpenseEntity(
    @PrimaryKey val id: String,
    val vehicleId: String,
    val date: String,
    val category: String,
    val description: String,
    val amount: Double,
    val notes: String?,
    val syncedWithServer: Boolean,
    val createdAt: Long
)
