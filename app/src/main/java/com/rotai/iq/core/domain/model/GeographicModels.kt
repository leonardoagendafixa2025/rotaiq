package com.rotai.iq.core.domain.model

import java.util.UUID

enum class DemandLevel(val displayName: String, val scoreWeight: Int, val hexColor: Long) {
    VERY_HIGH("Demanda Muito Alta (Dinâmica Frequente)", 20, 0xFF00E676),
    HIGH("Demanda Alta (Fácil Retorno)", 10, 0xFF76FF03),
    BALANCED("Demanda Equilibrada", 0, 0xFF00D2FF),
    LOW("Demanda Baixa (Risco de Espera)", -15, 0xFFFFB300),
    DEAD_ZONE("Zona Sem Retorno (Volta Vazia)", -35, 0xFFFF5252),
    HIGH_RISK("Área com Restrição / Alto Risco", -40, 0xFFFF1744);
}

enum class TimeSlot(val displayName: String, val timeRange: String) {
    MORNING_PEAK("Pico Matutino", "06:00 - 09:30"),
    DAYTIME("Entrepico Diurno", "09:30 - 16:30"),
    EVENING_PEAK("Pico Vespertino", "16:30 - 20:30"),
    NIGHT("Noturno", "20:30 - 00:00"),
    DAWN("Madrugada", "00:00 - 06:00");
}

data class GeoZone(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val city: String = "São Paulo",
    val baseDemandLevel: DemandLevel = DemandLevel.BALANCED,
    val returnTripProbability: Double = 0.80,    // 0.0 a 1.0 (probabilidade de conseguir viagem saindo da zona)
    val averageWaitTimeMinutes: Double = 6.0,     // Tempo médio de espera por nova corrida
    val deadheadKmToCenter: Double = 5.0,        // Quilômetros vazios para retornar à área central caso não consiga passageiro
    val isAvoidZone: Boolean = false,
    val isSurgeFrequent: Boolean = false,
    val notes: String? = null
)

data class DeadheadAnalysis(
    val originalOffer: RideOffer,
    val destinationZone: GeoZone,
    val returnProbability: Double,
    val expectedEmptyReturnKm: Double,
    val emptyReturnCost: Double,
    val originalNetProfit: Double,
    val adjustedNetProfit: Double,
    val adjustedRatePerKm: Double,
    val isDeadheadTrap: Boolean,
    val alertMessage: String?,
    val coachingRecommendation: String
)

data class PlatformPerformanceSummary(
    val platform: RidePlatform,
    val totalOffersEvaluated: Int,
    val totalOffersAccepted: Int,
    val acceptanceRatePercent: Double,
    val totalGrossRevenue: Double,
    val totalNetProfit: Double,
    val averageGrossFare: Double,
    val averageNetProfit: Double,
    val averageGrossRatePerKm: Double,
    val averageGrossRatePerHour: Double,
    val averageNetRatePerHour: Double,
    val profitMarginPercent: Double
)

data class PlatformComparisonReport(
    val summaries: List<PlatformPerformanceSummary>,
    val bestPlatformByHourlyRate: RidePlatform?,
    val bestPlatformByNetMargin: RidePlatform?,
    val recommendation: String
)
