package com.rotai.iq

import com.google.common.truth.Truth.assertThat
import com.rotai.iq.core.automation.accessibility.AccessibilityNodeExtractor
import com.rotai.iq.core.automation.tts.TtsMessageFormatter
import com.rotai.iq.core.domain.engine.RideEvaluationEngine
import com.rotai.iq.core.domain.model.DriverGoal
import com.rotai.iq.core.domain.model.DriverPreference
import com.rotai.iq.core.domain.model.EvaluationClassification
import com.rotai.iq.core.domain.model.RidePlatform
import com.rotai.iq.core.domain.model.Vehicle
import com.rotai.iq.core.domain.parser.PlatformDetector
import org.junit.Test

class OfferRealTimeParsingPipelineTest {

    private val engine = RideEvaluationEngine()
    private val detector = PlatformDetector()

    @Test
    fun livePipeline_uberCard_extractsEvaluatesAndGeneratesSpokenAlert() {
        val extractedTokens = listOf(
            "UberX",
            "R$ 38,90",
            "Embarque em 1,5 km (5 min)",
            "Viagem de 10,0 km (25 min)",
            "Avenida Paulista -> Pinheiros"
        )

        // 1. Concatenação da acessibilidade
        val combinedText = AccessibilityNodeExtractor.buildCombinedText(extractedTokens)

        // 2. Detecção e parsing de oferta
        val offer = detector.detectAndParse(combinedText)
        assertThat(offer).isNotNull()
        assertThat(offer?.platform).isEqualTo(RidePlatform.UBER)
        assertThat(offer?.grossFare).isWithin(0.01).of(38.90)
        assertThat(offer?.distanceKm).isWithin(0.01).of(10.0)

        // 3. Avaliação pelo motor
        val vehicle = Vehicle(
            consumptionKmPerLiter = 11.0,
            fuelPricePerLiter = 5.50
        )
        val evaluation = engine.evaluate(
            offer = offer!!,
            vehicle = vehicle,
            preferences = DriverPreference(),
            goal = DriverGoal()
        )

        assertThat(evaluation.score).isAtLeast(70)
        assertThat(evaluation.netProfit).isGreaterThan(20.0)

        // 4. Geração de áudio para o motorista
        val audioAlert = TtsMessageFormatter.formatSpeechMessage(evaluation)
        assertThat(audioAlert).isNotEmpty()
        assertThat(audioAlert).contains("reais")
    }

    @Test
    fun livePipeline_ninetyNineTrapCard_identifiesDeficitAndGeneratesAvoidSpeech() {
        val extractedTokens = listOf(
            "99Pop",
            "R$ 13,50",
            "3,5 km de busca (10 min)",
            "20,0 km de viagem (45 min)",
            "2 paradas"
        )

        val combinedText = AccessibilityNodeExtractor.buildCombinedText(extractedTokens)
        val offer = detector.detectAndParse(combinedText)
        assertThat(offer).isNotNull()
        assertThat(offer?.platform).isEqualTo(RidePlatform.NINETY_NINE)
        assertThat(offer?.grossFare).isWithin(0.01).of(13.50)
        assertThat(offer?.stopsCount).isEqualTo(2)

        val evaluation = engine.evaluate(
            offer = offer!!,
            vehicle = Vehicle(),
            preferences = DriverPreference(),
            goal = DriverGoal()
        )

        assertThat(evaluation.classification).isAnyOf(EvaluationClassification.BAD, EvaluationClassification.AVOID)
        assertThat(evaluation.alerts).isNotEmpty()

        val audioAlert = TtsMessageFormatter.formatSpeechMessage(evaluation)
        assertThat(audioAlert).contains("evite")
    }
}
