package com.rotai.iq.core.domain.engine

object FinancialEngine {

    data class FinancialMetrics(
        val grossFare: Double,
        val estimatedCost: Double,
        val netProfit: Double,
        val profitMarginPercent: Double,
        val grossRatePerKm: Double,
        val netRatePerKm: Double,
        val grossRatePerHour: Double,
        val netRatePerHour: Double,
        val grossRatePerMinute: Double
    )

    fun calculateMetrics(
        grossFare: Double,
        totalCost: Double,
        totalDistanceKm: Double,
        totalDurationMinutes: Double
    ): FinancialMetrics {
        val safeDistance = if (totalDistanceKm > 0.0) totalDistanceKm else 0.001
        val safeMinutes = if (totalDurationMinutes > 0.0) totalDurationMinutes else 1.0
        val totalDurationHours = safeMinutes / 60.0

        val netProfit = grossFare - totalCost
        val margin = if (grossFare > 0.0) (netProfit / grossFare) * 100.0 else 0.0

        val grossRatePerKm = grossFare / safeDistance
        val netRatePerKm = netProfit / safeDistance

        val grossRatePerHour = grossFare / totalDurationHours
        val netRatePerHour = netProfit / totalDurationHours
        val grossRatePerMinute = grossFare / safeMinutes

        return FinancialMetrics(
            grossFare = grossFare,
            estimatedCost = totalCost,
            netProfit = netProfit,
            profitMarginPercent = margin,
            grossRatePerKm = grossRatePerKm,
            netRatePerKm = netRatePerKm,
            grossRatePerHour = grossRatePerHour,
            netRatePerHour = netRatePerHour,
            grossRatePerMinute = grossRatePerMinute
        )
    }
}
