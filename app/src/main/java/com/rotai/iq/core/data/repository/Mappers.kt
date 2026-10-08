package com.rotai.iq.core.data.repository

import com.rotai.iq.core.data.local.entity.DailyFinancialEntity
import com.rotai.iq.core.data.local.entity.DriverGoalEntity
import com.rotai.iq.core.data.local.entity.DriverPreferenceEntity
import com.rotai.iq.core.data.local.entity.RideEvaluationEntity
import com.rotai.iq.core.data.local.entity.VehicleEntity
import com.rotai.iq.core.domain.model.DailyFinancialSummary
import com.rotai.iq.core.domain.model.DriverGoal
import com.rotai.iq.core.domain.model.DriverPreference
import com.rotai.iq.core.domain.model.EvaluationClassification
import com.rotai.iq.core.domain.model.FuelType
import com.rotai.iq.core.domain.model.RideCategory
import com.rotai.iq.core.domain.model.RideEvaluation
import com.rotai.iq.core.domain.model.RideOffer
import com.rotai.iq.core.domain.model.RidePlatform
import com.rotai.iq.core.domain.model.Vehicle

fun VehicleEntity.toDomain(): Vehicle = Vehicle(
    id = id,
    name = name,
    model = model,
    plate = plate,
    year = year,
    fuelType = runCatching { FuelType.valueOf(fuelType) }.getOrDefault(FuelType.GASOLINE),
    consumptionKmPerLiter = consumptionKmPerLiter,
    fuelPricePerLiter = fuelPricePerLiter,
    maintenanceCostPerKm = maintenanceCostPerKm,
    monthlyInsuranceCost = monthlyInsuranceCost,
    annualTaxesCost = annualTaxesCost,
    monthlyDepreciation = monthlyDepreciation,
    monthlyOtherCosts = monthlyOtherCosts,
    estimatedMonthlyKm = estimatedMonthlyKm,
    isActive = isActive
)

fun Vehicle.toEntity(): VehicleEntity = VehicleEntity(
    id = id,
    name = name,
    model = model,
    plate = plate,
    year = year,
    fuelType = fuelType.name,
    consumptionKmPerLiter = consumptionKmPerLiter,
    fuelPricePerLiter = fuelPricePerLiter,
    maintenanceCostPerKm = maintenanceCostPerKm,
    monthlyInsuranceCost = monthlyInsuranceCost,
    annualTaxesCost = annualTaxesCost,
    monthlyDepreciation = monthlyDepreciation,
    monthlyOtherCosts = monthlyOtherCosts,
    estimatedMonthlyKm = estimatedMonthlyKm,
    isActive = isActive
)

fun DriverGoalEntity.toDomain(): DriverGoal = DriverGoal(
    id = id,
    dailyGrossTarget = dailyGrossTarget,
    dailyNetTarget = dailyNetTarget,
    targetHourlyRate = targetHourlyRate,
    targetKmRate = targetKmRate,
    shiftTargetHours = shiftTargetHours,
    currentDailyGross = currentDailyGross,
    currentDailyNet = currentDailyNet,
    hoursWorkedToday = hoursWorkedToday,
    kmDrivenToday = kmDrivenToday
)

fun DriverGoal.toEntity(): DriverGoalEntity = DriverGoalEntity(
    id = id,
    dailyGrossTarget = dailyGrossTarget,
    dailyNetTarget = dailyNetTarget,
    targetHourlyRate = targetHourlyRate,
    targetKmRate = targetKmRate,
    shiftTargetHours = shiftTargetHours,
    currentDailyGross = currentDailyGross,
    currentDailyNet = currentDailyNet,
    hoursWorkedToday = hoursWorkedToday,
    kmDrivenToday = kmDrivenToday
)

fun DriverPreferenceEntity.toDomain(): DriverPreference = DriverPreference(
    id = id,
    minRatePerKm = minRatePerKm,
    minRatePerHour = minRatePerHour,
    minGrossFare = minGrossFare,
    maxPickupDistanceKm = maxPickupDistanceKm,
    maxPickupMinutes = maxPickupMinutes,
    maxStops = maxStops,
    allowIntermediateStops = allowIntermediateStops,
    minProfitMarginPercent = minProfitMarginPercent,
    preferShortTrips = preferShortTrips,
    audioAlertsEnabled = audioAlertsEnabled,
    overlayHudEnabled = overlayHudEnabled
)

fun DriverPreference.toEntity(): DriverPreferenceEntity = DriverPreferenceEntity(
    id = id,
    minRatePerKm = minRatePerKm,
    minRatePerHour = minRatePerHour,
    minGrossFare = minGrossFare,
    maxPickupDistanceKm = maxPickupDistanceKm,
    maxPickupMinutes = maxPickupMinutes,
    maxStops = maxStops,
    allowIntermediateStops = allowIntermediateStops,
    minProfitMarginPercent = minProfitMarginPercent,
    preferShortTrips = preferShortTrips,
    audioAlertsEnabled = audioAlertsEnabled,
    overlayHudEnabled = overlayHudEnabled
)

fun RideEvaluationEntity.toDomain(): RideEvaluation {
    val offer = RideOffer(
        id = id,
        platform = runCatching { RidePlatform.valueOf(platform) }.getOrDefault(RidePlatform.UBER),
        grossFare = grossFare,
        distanceKm = distanceKm,
        durationMinutes = durationMinutes,
        pickupDistanceKm = pickupDistanceKm,
        pickupDurationMinutes = pickupDurationMinutes,
        stopsCount = stopsCount,
        category = runCatching { RideCategory.valueOf(category) }.getOrDefault(RideCategory.UBER_X),
        timestamp = evaluatedAt,
        rawText = rawText
    )
    return RideEvaluation(
        id = id,
        offer = offer,
        score = score,
        classification = runCatching { EvaluationClassification.valueOf(classification) }.getOrDefault(EvaluationClassification.fromScore(score)),
        grossFare = grossFare,
        estimatedCost = estimatedCost,
        netProfit = netProfit,
        profitMarginPercent = profitMarginPercent,
        grossRatePerKm = grossRatePerKm,
        netRatePerKm = netRatePerKm,
        grossRatePerHour = grossRatePerHour,
        netRatePerHour = netRatePerHour,
        grossRatePerMinute = grossRatePerMinute,
        totalDistanceKm = totalDistanceKm,
        totalDurationMinutes = totalDurationMinutes,
        matchesFilter = matchesFilter,
        filterViolations = filterViolations,
        reasons = reasons,
        alerts = alerts,
        evaluatedAt = evaluatedAt
    )
}

fun RideEvaluation.toEntity(): RideEvaluationEntity = RideEvaluationEntity(
    id = id,
    platform = offer.platform.name,
    grossFare = grossFare,
    distanceKm = offer.distanceKm,
    durationMinutes = offer.durationMinutes,
    pickupDistanceKm = offer.pickupDistanceKm,
    pickupDurationMinutes = offer.pickupDurationMinutes,
    stopsCount = offer.stopsCount,
    category = offer.category.name,
    score = score,
    classification = classification.name,
    estimatedCost = estimatedCost,
    netProfit = netProfit,
    profitMarginPercent = profitMarginPercent,
    grossRatePerKm = grossRatePerKm,
    netRatePerKm = netRatePerKm,
    grossRatePerHour = grossRatePerHour,
    netRatePerHour = netRatePerHour,
    grossRatePerMinute = grossRatePerMinute,
    totalDistanceKm = totalDistanceKm,
    totalDurationMinutes = totalDurationMinutes,
    matchesFilter = matchesFilter,
    filterViolations = filterViolations,
    reasons = reasons,
    alerts = alerts,
    evaluatedAt = evaluatedAt,
    rawText = offer.rawText
)

fun DailyFinancialEntity.toDomain(): DailyFinancialSummary = DailyFinancialSummary(
    date = date,
    totalGrossRevenue = totalGrossRevenue,
    totalEstimatedCosts = totalEstimatedCosts,
    totalNetProfit = totalNetProfit,
    totalRidesEvaluated = totalRidesEvaluated,
    totalRidesAccepted = totalRidesAccepted,
    totalRidesRejected = totalRidesRejected,
    totalKmDriven = totalKmDriven,
    totalHoursOnline = totalHoursOnline,
    avgGrossRatePerKm = avgGrossRatePerKm,
    avgGrossRatePerHour = avgGrossRatePerHour,
    avgNetProfitPerHour = avgNetProfitPerHour
)

fun DailyFinancialSummary.toEntity(): DailyFinancialEntity = DailyFinancialEntity(
    date = date,
    totalGrossRevenue = totalGrossRevenue,
    totalEstimatedCosts = totalEstimatedCosts,
    totalNetProfit = totalNetProfit,
    totalRidesEvaluated = totalRidesEvaluated,
    totalRidesAccepted = totalRidesAccepted,
    totalRidesRejected = totalRidesRejected,
    totalKmDriven = totalKmDriven,
    totalHoursOnline = totalHoursOnline,
    avgGrossRatePerKm = avgGrossRatePerKm,
    avgGrossRatePerHour = avgGrossRatePerHour,
    avgNetProfitPerHour = avgNetProfitPerHour
)

fun com.rotai.iq.core.data.local.entity.FuelRecordEntity.toDomain(): com.rotai.iq.core.domain.model.FuelRecord = com.rotai.iq.core.domain.model.FuelRecord(
    id = id,
    vehicleId = vehicleId,
    date = date,
    odometerKm = odometerKm,
    liters = liters,
    pricePerLiter = pricePerLiter,
    totalPaid = totalPaid,
    fuelType = runCatching { com.rotai.iq.core.domain.model.FuelType.valueOf(fuelType) }.getOrDefault(com.rotai.iq.core.domain.model.FuelType.GASOLINE),
    isFullTank = isFullTank,
    calculatedKmPerLiter = calculatedKmPerLiter,
    calculatedCostPerKm = calculatedCostPerKm,
    notes = notes,
    syncedWithServer = syncedWithServer,
    createdAt = createdAt
)

fun com.rotai.iq.core.domain.model.FuelRecord.toEntity(): com.rotai.iq.core.data.local.entity.FuelRecordEntity = com.rotai.iq.core.data.local.entity.FuelRecordEntity(
    id = id,
    vehicleId = vehicleId,
    date = date,
    odometerKm = odometerKm,
    liters = liters,
    pricePerLiter = pricePerLiter,
    totalPaid = totalPaid,
    fuelType = fuelType.name,
    isFullTank = isFullTank,
    calculatedKmPerLiter = calculatedKmPerLiter,
    calculatedCostPerKm = calculatedCostPerKm,
    notes = notes,
    syncedWithServer = syncedWithServer,
    createdAt = createdAt
)

fun com.rotai.iq.core.data.local.entity.MaintenanceRecordEntity.toDomain(): com.rotai.iq.core.domain.model.MaintenanceRecord = com.rotai.iq.core.domain.model.MaintenanceRecord(
    id = id,
    vehicleId = vehicleId,
    date = date,
    odometerKm = odometerKm,
    type = runCatching { com.rotai.iq.core.domain.model.MaintenanceType.valueOf(type) }.getOrDefault(com.rotai.iq.core.domain.model.MaintenanceType.OIL_CHANGE),
    description = description,
    cost = cost,
    nextServiceKm = nextServiceKm,
    isCompleted = isCompleted,
    notes = notes,
    syncedWithServer = syncedWithServer,
    createdAt = createdAt
)

fun com.rotai.iq.core.domain.model.MaintenanceRecord.toEntity(): com.rotai.iq.core.data.local.entity.MaintenanceRecordEntity = com.rotai.iq.core.data.local.entity.MaintenanceRecordEntity(
    id = id,
    vehicleId = vehicleId,
    date = date,
    odometerKm = odometerKm,
    type = type.name,
    description = description,
    cost = cost,
    nextServiceKm = nextServiceKm,
    isCompleted = isCompleted,
    notes = notes,
    syncedWithServer = syncedWithServer,
    createdAt = createdAt
)

fun com.rotai.iq.core.data.local.entity.VehicleExpenseEntity.toDomain(): com.rotai.iq.core.domain.model.VehicleExpense = com.rotai.iq.core.domain.model.VehicleExpense(
    id = id,
    vehicleId = vehicleId,
    date = date,
    category = runCatching { com.rotai.iq.core.domain.model.ExpenseCategory.valueOf(category) }.getOrDefault(com.rotai.iq.core.domain.model.ExpenseCategory.OTHER),
    description = description,
    amount = amount,
    notes = notes,
    syncedWithServer = syncedWithServer,
    createdAt = createdAt
)

fun com.rotai.iq.core.domain.model.VehicleExpense.toEntity(): com.rotai.iq.core.data.local.entity.VehicleExpenseEntity = com.rotai.iq.core.data.local.entity.VehicleExpenseEntity(
    id = id,
    vehicleId = vehicleId,
    date = date,
    category = category.name,
    description = description,
    amount = amount,
    notes = notes,
    syncedWithServer = syncedWithServer,
    createdAt = createdAt
)

