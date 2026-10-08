package com.rotai.iq.core.domain.model

data class DriverGoal(
    val id: String = "default_goal",
    val dailyGrossTarget: Double = 300.0,       // R$ meta bruta diária
    val dailyNetTarget: Double = 220.0,         // R$ meta líquida diária
    val targetHourlyRate: Double = 45.0,        // R$/h mínimo desejado
    val targetKmRate: Double = 2.40,            // R$/km mínimo desejado
    val shiftTargetHours: Double = 8.0,         // Horas de turno planejadas
    val currentDailyGross: Double = 0.0,        // Faturamento acumulado no dia
    val currentDailyNet: Double = 0.0,          // Lucro acumulado no dia
    val hoursWorkedToday: Double = 0.0,         // Horas trabalhadas hoje
    val kmDrivenToday: Double = 0.0             // Km rodados hoje
) {
    val remainingGross: Double
        get() = (dailyGrossTarget - currentDailyGross).coerceAtLeast(0.0)

    val remainingNet: Double
        get() = (dailyNetTarget - currentDailyNet).coerceAtLeast(0.0)

    val progressPercent: Double
        get() = if (dailyGrossTarget > 0) ((currentDailyGross / dailyGrossTarget) * 100.0).coerceIn(0.0, 100.0) else 0.0

    val remainingHoursInShift: Double
        get() = (shiftTargetHours - hoursWorkedToday).coerceAtLeast(0.0)

    val requiredHourlyPace: Double
        get() = if (remainingHoursInShift > 0.0) remainingGross / remainingHoursInShift else 0.0

    val currentHourlyPace: Double
        get() = if (hoursWorkedToday > 0.0) currentDailyGross / hoursWorkedToday else 0.0

    val projectedDailyGross: Double
        get() = currentDailyGross + (currentHourlyPace * remainingHoursInShift)
}

data class DriverPreference(
    val id: String = "default_preferences",
    val minRatePerKm: Double = 2.20,              // Piso R$/km (mínimo desejado por km)
    val minRatePerHour: Double = 40.0,            // Piso R$/h (faturamento mínimo por hora)
    val minGrossFare: Double = 10.0,              // Valor mínimo total da corrida (R$)
    val maxPickupDistanceKm: Double = 3.5,        // Distância máxima até o passageiro (km)
    val maxPickupMinutes: Double = 10.0,          // Tempo máximo até o passageiro (min)
    val maxStops: Int = 0,                        // Quantidade máxima de paradas (0 = sem paradas)
    val allowIntermediateStops: Boolean = false,  // Rejeitar viagens com paradas adicionais
    val minProfitMarginPercent: Double = 50.0,    // Margem líquida mínima (%)
    val preferShortTrips: Boolean = false,
    val audioAlertsEnabled: Boolean = true,
    val overlayHudEnabled: Boolean = true
)

data class DriverZone(
    val id: String,
    val name: String,
    val profitabilityMultiplier: Double = 1.0,  // > 1.0 = zona boa, < 1.0 = zona ruim
    val isAvoidZone: Boolean = false,
    val isFavoriteZone: Boolean = false
)
