package com.rotai.iq.core.automation.tts

import com.rotai.iq.core.domain.model.EvaluationClassification
import com.rotai.iq.core.domain.model.RideEvaluation
import kotlin.math.roundToInt

object TtsMessageFormatter {

    fun formatSpeechMessage(evaluation: RideEvaluation): String {
        val profitRounded = evaluation.netProfit.roundToInt()
        val hourlyRounded = evaluation.netRatePerHour.roundToInt()

        return when (evaluation.classification) {
            EvaluationClassification.EXCELLENT -> {
                if (profitRounded > 0) {
                    "Corrida excelente! Sobra cerca de $profitRounded reais. $hourlyRounded reais por hora."
                } else {
                    "Corrida excelente! Ótima taxa por quilômetro."
                }
            }
            EvaluationClassification.GOOD -> {
                "Boa corrida! Sobra cerca de $profitRounded reais. Ritmo de $hourlyRounded reais por hora."
            }
            EvaluationClassification.ACCEPTABLE -> {
                val alertHint = evaluation.alerts.firstOrNull()?.let { ". Atenção: $it." } ?: ""
                "Corrida aceitável. Sobra cerca de $profitRounded reais$alertHint"
            }
            EvaluationClassification.BAD -> {
                "Corrida desfavorável! Lucro baixo de apenas $profitRounded reais."
            }
            EvaluationClassification.AVOID -> {
                if (evaluation.netProfit < 0) {
                    val loss = (-evaluation.netProfit).roundToInt()
                    "Atenção, evite esta corrida! Prejuízo estimado de cerca de $loss reais."
                } else {
                    "Atenção, evite esta corrida! Rentabilidade abaixo do custo operacional do carro."
                }
            }
        }
    }
}
