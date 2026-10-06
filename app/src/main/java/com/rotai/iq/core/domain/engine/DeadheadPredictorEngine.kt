package com.rotai.iq.core.domain.engine

import com.rotai.iq.core.domain.model.DeadheadAnalysis
import com.rotai.iq.core.domain.model.GeoZone
import com.rotai.iq.core.domain.model.RideOffer
import com.rotai.iq.core.domain.model.Vehicle
import java.util.Locale

object DeadheadPredictorEngine {

    fun analyzeDeadheadRisk(
        offer: RideOffer,
        destinationZone: GeoZone,
        vehicle: Vehicle
    ): DeadheadAnalysis {
        val totalDistanceKm = offer.totalDistanceKm.coerceAtLeast(0.1)
        val originalCost = vehicle.calculateTripCost(totalDistanceKm).totalCost
        val originalProfit = offer.grossFare - originalCost

        val riskOfNoReturn = (1.0 - destinationZone.returnTripProbability).coerceIn(0.0, 1.0)
        val expectedEmptyKm = destinationZone.deadheadKmToCenter * riskOfNoReturn
        val emptyReturnCost = expectedEmptyKm * vehicle.totalCostPerKm

        val adjustedNetProfit = originalProfit - emptyReturnCost
        val totalEffectiveKm = totalDistanceKm + expectedEmptyKm
        val adjustedRatePerKm = if (totalEffectiveKm > 0) offer.grossFare / totalEffectiveKm else 0.0

        val isDeadheadTrap = destinationZone.isAvoidZone ||
                destinationZone.returnTripProbability < 0.45 ||
                adjustedNetProfit <= 0.0 ||
                (expectedEmptyKm >= offer.distanceKm * 0.55 && destinationZone.deadheadKmToCenter > 10.0)

        val alertMessage = when {
            destinationZone.isAvoidZone -> {
                "Área de alto risco ou restrição operacional cadastrada."
            }
            isDeadheadTrap && adjustedNetProfit <= 0.0 -> {
                "Prejuízo com Volta Vazia: Você precisará rodar cerca de %.1f km sem passageiro para voltar.".format(
                    Locale("pt", "BR"),
                    expectedEmptyKm
                )
            }
            isDeadheadTrap -> {
                "Armadilha de Deadhead (Volta Vazia): Risco de rodar %.1f km sem passageiro (Custo de retorno estimado: R$ %.2f).".format(
                    Locale("pt", "BR"),
                    expectedEmptyKm,
                    emptyReturnCost
                )
            }
            destinationZone.returnTripProbability < 0.65 -> {
                "Região com demanda moderada. Pode haver espera de até %.0f min por retorno.".format(
                    Locale("pt", "BR"),
                    destinationZone.averageWaitTimeMinutes
                )
            }
            else -> null
        }

        val recommendation = when {
            destinationZone.returnTripProbability >= 0.85 -> {
                "Excelente destino! Alta probabilidade de viagem de retorno (%.0f%%) na região de desembarque.".format(
                    Locale("pt", "BR"),
                    destinationZone.returnTripProbability * 100.0
                )
            }
            isDeadheadTrap -> {
                "Atenção: A corrida paga R$ %.2f, mas descontando os custos do retorno vazio (R$ %.2f), o lucro real cai de R$ %.2f para apenas R$ %.2f.".format(
                    Locale("pt", "BR"),
                    offer.grossFare,
                    emptyReturnCost,
                    originalProfit,
                    adjustedNetProfit
                )
            }
            else -> {
                "Destino viável. Sobra real estimada de R$ %.2f considerando a chance de retorno local.".format(
                    Locale("pt", "BR"),
                    adjustedNetProfit
                )
            }
        }

        return DeadheadAnalysis(
            originalOffer = offer,
            destinationZone = destinationZone,
            returnProbability = destinationZone.returnTripProbability,
            expectedEmptyReturnKm = expectedEmptyKm,
            emptyReturnCost = emptyReturnCost,
            originalNetProfit = originalProfit,
            adjustedNetProfit = adjustedNetProfit,
            adjustedRatePerKm = adjustedRatePerKm,
            isDeadheadTrap = isDeadheadTrap,
            alertMessage = alertMessage,
            coachingRecommendation = recommendation
        )
    }
}
