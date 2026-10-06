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
    maxPickupDistanceKm = maxPickupDistanceKm,
    maxStops = maxStops,
    preferShortTrips = preferShortTrips,
    audioAlertsEnabled = audioAlertsEnabled,
    overlayHudEnabled = overlayHudEnabled
)

fun DriverPreference.toEntity(): DriverPreferenceEntity = DriverPreferenceEntity(
    id = id,
    minRatePerKm = minRatePerKm,
    minRatePerHour = minRatePerHour,
    maxPickupDistanceKm = maxPickupDistanceKm,
    maxStops = maxStops,
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
