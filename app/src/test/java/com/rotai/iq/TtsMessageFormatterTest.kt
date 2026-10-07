package com.rotai.iq

import com.google.common.truth.Truth.assertThat
import com.rotai.iq.core.automation.tts.TtsMessageFormatter
import com.rotai.iq.core.domain.model.EvaluationClassification
import com.rotai.iq.core.domain.model.RideEvaluation
import com.rotai.iq.core.domain.model.RideOffer
import com.rotai.iq.core.domain.model.RidePlatform
import org.junit.Test

class TtsMessageFormatterTest {

    @Test
    fun formatSpeechMessage_excellentRide_returnsEncouragingSpeech() {
        val offer = RideOffer(
            platform = RidePlatform.UBER,
            grossFare = 35.0,
            distanceKm = 8.0,
            durationMinutes = 20.0
        )
        val eval = RideEvaluation(
            offer = offer,
            grossFare = 35.0,
            score = 92,
            classification = EvaluationClassification.EXCELLENT,
            estimatedCost = 8.0,
            netProfit = 27.0,
            profitMarginPercent = 77.0,
            grossRatePerKm = 4.37,
            netRatePerKm = 3.37,
            grossRatePerHour = 105.0,
            netRatePerHour = 81.0,
            grossRatePerMinute = 1.75,
            totalDistanceKm = 9.0,
            totalDurationMinutes = 23.0,
            reasons = listOf("Alta rentabilidade por hora"),
            alerts = emptyList()
        )

        val speech = TtsMessageFormatter.formatSpeechMessage(eval)
        assertThat(speech).contains("Corrida excelente")
        assertThat(speech).contains("27 reais")
        assertThat(speech).contains("81 reais por hora")
    }

    @Test
    fun formatSpeechMessage_avoidRideNegativeProfit_warnsAboutLoss() {
        val offer = RideOffer(
            platform = RidePlatform.NINETY_NINE,
            grossFare = 15.0,
            distanceKm = 20.0,
            durationMinutes = 50.0
        )
        val eval = RideEvaluation(
            offer = offer,
            grossFare = 15.0,
            score = 15,
            classification = EvaluationClassification.AVOID,
            estimatedCost = 21.0,
            netProfit = -6.0,
            profitMarginPercent = -40.0,
            grossRatePerKm = 0.75,
            netRatePerKm = -0.30,
            grossRatePerHour = 18.0,
            netRatePerHour = -7.2,
            grossRatePerMinute = 0.30,
            totalDistanceKm = 24.0,
            totalDurationMinutes = 60.0,
            reasons = listOf("Valor muito baixo para a quilometragem"),
            alerts = listOf("Corrida operando no prejuízo")
        )

        val speech = TtsMessageFormatter.formatSpeechMessage(eval)
        assertThat(speech).contains("evite esta corrida")
        assertThat(speech).contains("Prejuízo estimado de cerca de 6 reais")
    }

    @Test
    fun formatSpeechMessage_acceptableRide_includesAlertWarning() {
        val offer = RideOffer(
            platform = RidePlatform.UBER,
            grossFare = 22.0,
            distanceKm = 10.0,
            durationMinutes = 30.0
        )
        val eval = RideEvaluation(
            offer = offer,
            grossFare = 22.0,
            score = 62,
            classification = EvaluationClassification.ACCEPTABLE,
            estimatedCost = 10.0,
            netProfit = 12.0,
            profitMarginPercent = 54.0,
            grossRatePerKm = 2.20,
            netRatePerKm = 1.20,
            grossRatePerHour = 44.0,
            netRatePerHour = 24.0,
            grossRatePerMinute = 0.73,
            totalDistanceKm = 11.5,
            totalDurationMinutes = 35.0,
            reasons = listOf("Rentabilidade média"),
            alerts = listOf("2 paradas intermediárias")
        )

        val speech = TtsMessageFormatter.formatSpeechMessage(eval)
        assertThat(speech).contains("Corrida aceitável")
        assertThat(speech).contains("12 reais")
        assertThat(speech).contains("2 paradas intermediárias")
    }

    @Test
    fun formatSpeechMessage_goodRide_returnsEncouragingPaceSpeech() {
        val offer = RideOffer(
            platform = RidePlatform.UBER,
            grossFare = 28.0,
            distanceKm = 7.0,
            durationMinutes = 18.0
        )
        val eval = RideEvaluation(
            offer = offer,
            grossFare = 28.0,
            score = 82,
            classification = EvaluationClassification.GOOD,
            estimatedCost = 6.5,
            netProfit = 21.5,
            profitMarginPercent = 76.0,
            grossRatePerKm = 4.0,
            netRatePerKm = 3.07,
            grossRatePerHour = 93.3,
            netRatePerHour = 71.6,
            grossRatePerMinute = 1.55,
            totalDistanceKm = 8.0,
            totalDurationMinutes = 21.0,
            reasons = listOf("Boa rentabilidade"),
            alerts = emptyList()
        )

        val speech = TtsMessageFormatter.formatSpeechMessage(eval)
        assertThat(speech).contains("Boa corrida")
        assertThat(speech).contains("22 reais")
        assertThat(speech).contains("72 reais por hora")
    }

    @Test
    fun formatSpeechMessage_badRide_warnsAboutLowProfit() {
        val offer = RideOffer(
            platform = RidePlatform.NINETY_NINE,
            grossFare = 12.0,
            distanceKm = 10.0,
            durationMinutes = 25.0
        )
        val eval = RideEvaluation(
            offer = offer,
            grossFare = 12.0,
            score = 38,
            classification = EvaluationClassification.BAD,
            estimatedCost = 8.0,
            netProfit = 4.0,
            profitMarginPercent = 33.0,
            grossRatePerKm = 1.20,
            netRatePerKm = 0.40,
            grossRatePerHour = 28.8,
            netRatePerHour = 9.6,
            grossRatePerMinute = 0.48,
            totalDistanceKm = 11.0,
            totalDurationMinutes = 28.0,
            reasons = listOf("Margem baixa"),
            alerts = listOf("Abaixo da taxa mínima de R$/km")
        )

        val speech = TtsMessageFormatter.formatSpeechMessage(eval)
        assertThat(speech).contains("Corrida desfavorável")
        assertThat(speech).contains("4 reais")
    }

    @Test
    fun formatSpeechMessage_avoidRidePositiveProfit_warnsAboutOperationalCost() {
        val offer = RideOffer(
            platform = RidePlatform.UBER,
            grossFare = 16.0,
            distanceKm = 18.0,
            durationMinutes = 40.0
        )
        val eval = RideEvaluation(
            offer = offer,
            grossFare = 16.0,
            score = 22,
            classification = EvaluationClassification.AVOID,
            estimatedCost = 15.5,
            netProfit = 0.5,
            profitMarginPercent = 3.0,
            grossRatePerKm = 0.88,
            netRatePerKm = 0.02,
            grossRatePerHour = 24.0,
            netRatePerHour = 0.75,
            grossRatePerMinute = 0.40,
            totalDistanceKm = 20.0,
            totalDurationMinutes = 45.0,
            reasons = listOf("Lucro irrisório"),
            alerts = listOf("Desgaste veicular incompatível")
        )

        val speech = TtsMessageFormatter.formatSpeechMessage(eval)
        assertThat(speech).contains("evite esta corrida")
        assertThat(speech).contains("Rentabilidade abaixo do custo operacional")
    }
}
