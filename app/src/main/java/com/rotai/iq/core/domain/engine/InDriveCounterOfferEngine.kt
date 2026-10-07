package com.rotai.iq.core.domain.engine

import com.rotai.iq.core.domain.model.DriverGoal
import com.rotai.iq.core.domain.model.RideOffer
import com.rotai.iq.core.domain.model.Vehicle
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max

enum class CounterOfferAction(val label: String, val emoji: String) {
    ACCEPT_IMMEDIATELY("Aceitar de Imediato", "🟢"),
    COUNTER_OFFER("Fazer Contraproposta", "🟡"),
    AVOID_RIDE("Recusar / Fora de Cogitação", "🔴")
}

data class CounterOfferStep(
    val deltaFare: Double,           // +R$ 2.00, +R$ 4.00, etc.
    val totalFare: Double,           // Preço final
    val netProfit: Double,           // Lucro líquido real
    val netHourlyRate: Double,       // R$/h líquido
    val isRecommended: Boolean
)

data class InDriveEvaluation(
    val passengerFare: Double,
    val tripCost: Double,
    val passengerNetProfit: Double,
    val passengerHourlyRate: Double,
    val passengerMarginPercent: Double,
    val suggestedCounterOfferFare: Double,
    val counterOfferDelta: Double,
    val counterOfferNetProfit: Double,
    val counterOfferHourlyRate: Double,
    val recommendedAction: CounterOfferAction,
    val steps: List<CounterOfferStep>,
    val explanation: String
)

class InDriveCounterOfferEngine(
    private val defaultVehicle: Vehicle = Vehicle(),
    private val defaultGoal: DriverGoal = DriverGoal()
) {

    fun evaluateCounterOffer(
        offer: RideOffer,
        vehicle: Vehicle = defaultVehicle,
        goal: DriverGoal = defaultGoal
    ): InDriveEvaluation {
        val totalKm = offer.totalDistanceKm.coerceAtLeast(0.1)
        val totalHours = offer.totalDurationHours.coerceAtLeast(0.05)

        val costBreakdown = vehicle.calculateTripCost(totalKm)
        val tripCost = costBreakdown.totalCost

        val passengerFare = offer.grossFare
        val passengerNetProfit = passengerFare - tripCost
        val passengerHourlyRate = passengerNetProfit / totalHours
        val passengerMargin = if (passengerFare > 0.0) (passengerNetProfit / passengerFare) * 100.0 else 0.0

        // Meta mínima por hora
        val targetHourly = goal.targetHourlyRate.coerceAtLeast(30.0)
        // Valor bruto necessário para atingir a meta horária desejada
        val neededGrossForTarget = (targetHourly * totalHours) + tripCost

        // No app inDrive, os botões padrão de contraproposta aumentam de R$ 2 em R$ 2, R$ 3 ou R$ 4
        val stepIncrements = listOf(2.0, 4.0, 6.0, 8.0, 10.0)
        val steps = mutableListOf<CounterOfferStep>()

        var recommendedStepFare = passengerFare
        var recommendedDelta = 0.0

        val action: CounterOfferAction
        if (passengerHourlyRate >= targetHourly && passengerMargin >= 40.0) {
            action = CounterOfferAction.ACCEPT_IMMEDIATELY
        } else if (passengerNetProfit <= 0.0 && neededGrossForTarget > passengerFare + 12.0) {
            action = CounterOfferAction.AVOID_RIDE
        } else {
            action = CounterOfferAction.COUNTER_OFFER
        }

        // Encontra o acréscimo ótimo para cobrir o target
        val idealIncrement = max(0.0, neededGrossForTarget - passengerFare)
        // Arredonda para o degrau par mais próximo (+2, +4, +6, etc.)
        val roundedDelta = (ceil(idealIncrement / 2.0) * 2.0).coerceIn(2.0, 10.0)

        for (inc in stepIncrements) {
            val stepFare = passengerFare + inc
            val stepProfit = stepFare - tripCost
            val stepHourly = stepProfit / totalHours
            val isRec = (action == CounterOfferAction.COUNTER_OFFER && inc == roundedDelta)
            if (isRec) {
                recommendedStepFare = stepFare
                recommendedDelta = inc
            }
            steps.add(
                CounterOfferStep(
                    deltaFare = inc,
                    totalFare = stepFare,
                    netProfit = stepProfit,
                    netHourlyRate = stepHourly,
                    isRecommended = isRec
                )
            )
        }

        if (action == CounterOfferAction.ACCEPT_IMMEDIATELY) {
            recommendedStepFare = passengerFare
            recommendedDelta = 0.0
        }

        val counterNetProfit = recommendedStepFare - tripCost
        val counterHourlyRate = counterNetProfit / totalHours

        val explanation = when (action) {
            CounterOfferAction.ACCEPT_IMMEDIATELY -> {
                String.format(
                    Locale("pt", "BR"),
                    "Oferta do passageiro é ótima! Lucro de R$ %.2f (R$ %.2f/h líquido). Aceite de imediato.",
                    passengerNetProfit,
                    passengerHourlyRate
                )
            }
            CounterOfferAction.COUNTER_OFFER -> {
                String.format(
                    Locale("pt", "BR"),
                    "Oferta baixa (R$ %.2f/h). Contraproposta de +R$ %.0f (R$ %.2f) eleva seu lucro para R$ %.2f/h líquido.",
                    passengerHourlyRate,
                    recommendedDelta,
                    recommendedStepFare,
                    counterHourlyRate
                )
            }
            CounterOfferAction.AVOID_RIDE -> {
                String.format(
                    Locale("pt", "BR"),
                    "Inviável mesmo com contraproposta. Custo operacional é R$ %.2f para retorno insuficiente.",
                    tripCost
                )
            }
        }

        return InDriveEvaluation(
            passengerFare = passengerFare,
            tripCost = tripCost,
            passengerNetProfit = passengerNetProfit,
            passengerHourlyRate = passengerHourlyRate,
            passengerMarginPercent = passengerMargin,
            suggestedCounterOfferFare = recommendedStepFare,
            counterOfferDelta = recommendedDelta,
            counterOfferNetProfit = counterNetProfit,
            counterOfferHourlyRate = counterHourlyRate,
            recommendedAction = action,
            steps = steps,
            explanation = explanation
        )
    }
}
