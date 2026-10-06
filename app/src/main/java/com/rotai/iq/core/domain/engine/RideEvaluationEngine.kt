package com.rotai.iq.core.domain.engine

import com.rotai.iq.core.domain.model.DriverGoal
import com.rotai.iq.core.domain.model.DriverPreference
import com.rotai.iq.core.domain.model.DriverZone
import com.rotai.iq.core.domain.model.EvaluationClassification
import com.rotai.iq.core.domain.model.RideEvaluation
import com.rotai.iq.core.domain.model.RideOffer
import com.rotai.iq.core.domain.model.Vehicle
import java.util.Locale
import kotlin.math.roundToInt

class RideEvaluationEngine(
    private val defaultVehicle: Vehicle = Vehicle(),
    private val defaultPreferences: DriverPreference = DriverPreference(),
    private val defaultGoal: DriverGoal = DriverGoal()
) {

    fun evaluate(
        offer: RideOffer,
        vehicle: Vehicle = defaultVehicle,
        preferences: DriverPreference = defaultPreferences,
        goal: DriverGoal = defaultGoal,
        destinationZone: DriverZone? = null
    ): RideEvaluation {
        val totalDistanceKm = offer.totalDistanceKm.coerceAtLeast(0.1)
        val totalDurationMinutes = offer.totalDurationMinutes.coerceAtLeast(1.0)
        val totalHours = totalDurationMinutes / 60.0

        // 1. Custo e Métricas Financeiras Reais
        val costBreakdown = vehicle.calculateTripCost(totalDistanceKm)
        val estimatedCost = costBreakdown.totalCost
        val netProfit = offer.grossFare - estimatedCost
        val profitMarginPercent = if (offer.grossFare > 0.0) {
            ((netProfit / offer.grossFare) * 100.0)
        } else {
            0.0
        }

        val grossRatePerKm = offer.grossFare / totalDistanceKm
        val netRatePerKm = netProfit / totalDistanceKm
        val grossRatePerHour = offer.grossFare / totalHours
        val netRatePerHour = netProfit / totalHours
        val grossRatePerMinute = offer.grossFare / totalDurationMinutes

        val reasons = mutableListOf<String>()
        val alerts = mutableListOf<String>()

        // 2. Pontuação Multi-fatorial (0 a 100)
        var scoreAcc = 50.0 // Base neutra

        // Fator 1: Rentabilidade por Hora (Peso forte: até +25 / -30)
        val targetHourly = goal.targetHourlyRate.coerceAtLeast(25.0)
        val hourlyRatio = grossRatePerHour / targetHourly
        when {
            hourlyRatio >= 1.5 -> {
                scoreAcc += 25.0
                reasons.add("✓ Excelente retorno por hora (R$ %.2f/h)".format(Locale("pt", "BR"), grossRatePerHour))
            }
            hourlyRatio >= 1.15 -> {
                scoreAcc += 18.0
                reasons.add("✓ Acima da sua meta horária (R$ %.2f/h)".format(Locale("pt", "BR"), grossRatePerHour))
            }
            hourlyRatio >= 0.95 -> {
                scoreAcc += 5.0
                reasons.add("✓ Alinhado com a meta horária (R$ %.2f/h)".format(Locale("pt", "BR"), grossRatePerHour))
            }
            hourlyRatio >= 0.70 -> {
                scoreAcc -= 15.0
                alerts.add("⚠️ Retorno por hora abaixo do ideal (R$ %.2f/h)".format(Locale("pt", "BR"), grossRatePerHour))
            }
            else -> {
                scoreAcc -= 30.0
                alerts.add("⛔ Retorno por hora muito baixo (R$ %.2f/h)".format(Locale("pt", "BR"), grossRatePerHour))
            }
        }

        // Fator 2: Rentabilidade por Quilômetro (Peso forte: até +20 / -25)
        val targetKm = goal.targetKmRate.coerceAtLeast(1.50)
        val kmRatio = grossRatePerKm / targetKm
        when {
            kmRatio >= 1.4 -> {
                scoreAcc += 20.0
                reasons.add("✓ Excelente relação valor/distância (R$ %.2f/km)".format(Locale("pt", "BR"), grossRatePerKm))
            }
            kmRatio >= 1.1 -> {
                scoreAcc += 12.0
                reasons.add("✓ Boa relação valor/distância (R$ %.2f/km)".format(Locale("pt", "BR"), grossRatePerKm))
            }
            kmRatio >= 0.90 -> {
                scoreAcc += 2.0
            }
            kmRatio >= 0.70 -> {
                scoreAcc -= 15.0
                alerts.add("⚠️ Valor por km abaixo do recomendado (R$ %.2f/km)".format(Locale("pt", "BR"), grossRatePerKm))
            }
            else -> {
                scoreAcc -= 25.0
                alerts.add("⛔ Corrida paga muito pouco por km (R$ %.2f/km)".format(Locale("pt", "BR"), grossRatePerKm))
            }
        }

        // Fator 3: Deslocamento até o Passageiro (Deadhead) (Peso: até +10 / -25)
        val pickupRatio = if (offer.distanceKm > 0.0) offer.pickupDistanceKm / offer.distanceKm else 1.0
        when {
            offer.pickupDistanceKm <= 1.0 -> {
                scoreAcc += 10.0
                reasons.add("✓ Passageiro muito próximo (%.1f km)".format(Locale("pt", "BR"), offer.pickupDistanceKm))
            }
            offer.pickupDistanceKm <= preferences.maxPickupDistanceKm && pickupRatio <= 0.35 -> {
                scoreAcc += 4.0
            }
            offer.pickupDistanceKm > preferences.maxPickupDistanceKm -> {
                scoreAcc -= 20.0
                alerts.add("⚠️ Passageiro distante (%.1f km até o embarque)".format(Locale("pt", "BR"), offer.pickupDistanceKm))
            }
            pickupRatio > 0.50 -> {
                scoreAcc -= 15.0
                alerts.add("⚠️ Deslocamento até embarque consome %.0f%% da viagem".format(Locale("pt", "BR"), pickupRatio * 100.0))
            }
        }

        // Fator 4: Custo do Veículo e Margem Líquida Real (Peso: até +10 / -25)
        when {
            profitMarginPercent >= 75.0 -> {
                scoreAcc += 10.0
                reasons.add("✓ Baixo custo estimado (R$ %.2f, margem %.0f%%)".format(Locale("pt", "BR"), estimatedCost, profitMarginPercent))
            }
            profitMarginPercent >= 60.0 -> {
                scoreAcc += 5.0
                reasons.add("✓ Margem líquida saudável (%.0f%%)".format(Locale("pt", "BR"), profitMarginPercent))
            }
            profitMarginPercent < 40.0 && profitMarginPercent > 0.0 -> {
                scoreAcc -= 18.0
                alerts.add("⚠️ Custo do veículo consome a maior parte do valor (R$ %.2f)".format(Locale("pt", "BR"), estimatedCost))
            }
            netProfit <= 0.0 -> {
                scoreAcc -= 35.0
                alerts.add("⛔ Prejuízo financeiro: Custo (R$ %.2f) maior que a corrida!".format(Locale("pt", "BR"), estimatedCost))
            }
        }

        // Fator 5: Paradas Intermediárias (Penalidade)
        if (offer.stopsCount > 0) {
            val stopPenalty = offer.stopsCount * 8.0
            scoreAcc -= stopPenalty
            alerts.add("⚠️ Viagem com %d parada(s) intermediária(s)".format(offer.stopsCount))
        }

        // Fator 6: Contexto Geográfico / Zonas
        if (destinationZone != null) {
            if (destinationZone.isAvoidZone) {
                scoreAcc -= 25.0
                alerts.add("⛔ Destino em região evitada: %s".format(destinationZone.name))
            } else if (destinationZone.isFavoriteZone) {
                scoreAcc += 10.0
                reasons.add("✓ Destino em área de alta demanda: %s".format(destinationZone.name))
            } else if (destinationZone.profitabilityMultiplier > 1.0) {
                scoreAcc += (destinationZone.profitabilityMultiplier - 1.0) * 20.0
                reasons.add("✓ Região favorável para novas corridas")
            }
        } else {
            // Se o tempo for aceitável e a margem boa, adiciona razão padrão
            if (totalDurationMinutes in 8.0..40.0 && profitMarginPercent >= 60.0) {
                reasons.add("✓ Tempo e duração aceitáveis (%d min)".format(totalDurationMinutes.toInt()))
            }
        }

        val finalScore = scoreAcc.roundToInt().coerceIn(0, 100)
        val classification = EvaluationClassification.fromScore(finalScore)

        if (reasons.isEmpty()) {
            reasons.add("✓ Avaliação calculada com base nos custos operacionais")
        }

        return RideEvaluation(
            offer = offer,
            score = finalScore,
            classification = classification,
            grossFare = offer.grossFare,
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
            alerts = alerts
        )
    }
}
