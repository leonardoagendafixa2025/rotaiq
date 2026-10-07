package com.rotai.iq.core.domain.engine

import com.rotai.iq.core.domain.model.DriverGoal
import com.rotai.iq.core.domain.model.EvaluationClassification
import com.rotai.iq.core.domain.model.RideEvaluation
import com.rotai.iq.core.domain.model.RideOffer
import com.rotai.iq.core.domain.model.Vehicle
import java.util.Locale
import kotlin.math.max

data class StrategicRejectionAnalysis(
    val offer: RideOffer,
    val isRejectionRecommended: Boolean,
    val badOfferNetProfit: Double,
    val badOfferHourlyRate: Double,
    val targetHourlyRate: Double,
    val breakEvenWaitMinutes: Double,
    val avoidedOperatingCost: Double,
    val vehicleWearAvoidedKm: Double,
    val verdict: String,
    val tacticalAdvice: String
)

class StrategicRejectionEngine(
    private val defaultVehicle: Vehicle = Vehicle(),
    private val defaultGoal: DriverGoal = DriverGoal()
) {

    fun analyzeRejection(
        offer: RideOffer,
        vehicle: Vehicle = defaultVehicle,
        goal: DriverGoal = defaultGoal
    ): StrategicRejectionAnalysis {
        val totalKm = offer.totalDistanceKm.coerceAtLeast(0.1)
        val totalMinutes = offer.totalDurationMinutes.coerceAtLeast(1.0)
        val totalHours = totalMinutes / 60.0

        val tripCost = vehicle.calculateTripCost(totalKm).totalCost
        val netProfit = offer.grossFare - tripCost
        val hourlyRate = netProfit / totalHours
        val targetHourly = goal.targetHourlyRate.coerceAtLeast(35.0)

        val isRecommended = netProfit <= 0.0 || hourlyRate < (targetHourly * 0.6)

        // Cálculo de Ponto de Equilíbrio de Espera (Break-even wait time)
        // Quantos minutos o motorista pode esperar parado antes de aceitar valer menos que uma corrida boa?
        val hourlyEfficiencyRatio = (hourlyRate / targetHourly).coerceIn(0.0, 1.0)
        val breakEvenMinutes = totalMinutes * (1.0 - hourlyEfficiencyRatio)

        val verdict: String
        val advice: String

        if (netProfit <= 0.0) {
            verdict = "RECUSA OBRIGATÓRIA (PREJUÍZO DIRETO)"
            advice = String.format(
                Locale("pt", "BR"),
                "Esta corrida custa R$ %.2f de operação e paga apenas R$ %.2f (prejuízo de R$ %.2f). Ficar parado no ponto sem gastar combustível economiza R$ %.2f.",
                tripCost,
                offer.grossFare,
                -netProfit,
                tripCost
            )
        } else if (isRecommended) {
            verdict = "RECUSA ESTRATÉGICA RECOMENDADA"
            advice = String.format(
                Locale("pt", "BR"),
                "Você pode esperar até %.0f minutos parado por uma oferta da sua meta (R$ %.2f/h) que ainda assim lucrará mais do que se prender por %.0f min nesta corrida de R$ %.2f/h.",
                breakEvenMinutes,
                targetHourly,
                totalMinutes,
                hourlyRate
            )
        } else {
            verdict = "CORRIDA VIÁVEL / ACEITÁVEL"
            advice = String.format(
                Locale("pt", "BR"),
                "A corrida remunera a R$ %.2f/h líquido, próxima ou acima do seu patamar desejado (R$ %.2f/h). Aceitação recomendada.",
                hourlyRate,
                targetHourly
            )
        }

        return StrategicRejectionAnalysis(
            offer = offer,
            isRejectionRecommended = isRecommended,
            badOfferNetProfit = netProfit,
            badOfferHourlyRate = hourlyRate,
            targetHourlyRate = targetHourly,
            breakEvenWaitMinutes = breakEvenMinutes,
            avoidedOperatingCost = tripCost,
            vehicleWearAvoidedKm = totalKm,
            verdict = verdict,
            tacticalAdvice = advice
        )
    }
}
