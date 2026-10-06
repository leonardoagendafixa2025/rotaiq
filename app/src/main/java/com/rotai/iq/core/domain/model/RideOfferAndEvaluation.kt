package com.rotai.iq.core.domain.model

import java.util.UUID

data class RideOffer(
    val id: String = UUID.randomUUID().toString(),
    val platform: RidePlatform = RidePlatform.UBER,
    val grossFare: Double,                   // R$ ofertado
    val distanceKm: Double,                  // Distância da viagem (km)
    val durationMinutes: Double,              // Duração da viagem (min)
    val pickupDistanceKm: Double = 0.0,      // Distância até o passageiro (km)
    val pickupDurationMinutes: Double = 0.0,  // Tempo até o passageiro (min)
    val stopsCount: Int = 0,                 // Número de paradas
    val category: RideCategory = RideCategory.UBER_X,
    val pickupAddress: String? = null,
    val destinationAddress: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val rawText: String? = null
) {
    val totalDistanceKm: Double
        get() = distanceKm + pickupDistanceKm

    val totalDurationMinutes: Double
        get() = durationMinutes + pickupDurationMinutes

    val totalDurationHours: Double
        get() = totalDurationMinutes / 60.0
}

data class RideEvaluation(
    val id: String = UUID.randomUUID().toString(),
    val offer: RideOffer,
    val score: Int,                                    // 0 a 100
    val classification: EvaluationClassification,
    val grossFare: Double,                            // R$
    val estimatedCost: Double,                        // R$
    val netProfit: Double,                            // R$
    val profitMarginPercent: Double,                  // %
    val grossRatePerKm: Double,                       // R$/km
    val netRatePerKm: Double,                         // Lucro R$/km
    val grossRatePerHour: Double,                     // R$/h
    val netRatePerHour: Double,                       // Lucro R$/h
    val grossRatePerMinute: Double,                   // R$/min
    val totalDistanceKm: Double,                      // km total
    val totalDurationMinutes: Double,                 // min total
    val reasons: List<String>,                        // Motivos positivos
    val alerts: List<String>,                         // Alertas negativos ou riscos
    val evaluatedAt: Long = System.currentTimeMillis()
)

data class DailyFinancialSummary(
    val date: String,                                 // YYYY-MM-DD
    val totalGrossRevenue: Double = 0.0,
    val totalEstimatedCosts: Double = 0.0,
    val totalNetProfit: Double = 0.0,
    val totalRidesEvaluated: Int = 0,
    val totalRidesAccepted: Int = 0,
    val totalRidesRejected: Int = 0,
    val totalKmDriven: Double = 0.0,
    val totalHoursOnline: Double = 0.0,
    val avgGrossRatePerKm: Double = 0.0,
    val avgGrossRatePerHour: Double = 0.0,
    val avgNetProfitPerHour: Double = 0.0
) {
    val profitMarginPercent: Double
        get() = if (totalGrossRevenue > 0.0) ((totalNetProfit / totalGrossRevenue) * 100.0) else 0.0
}
