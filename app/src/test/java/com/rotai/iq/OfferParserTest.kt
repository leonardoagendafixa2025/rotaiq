package com.rotai.iq

import com.google.common.truth.Truth.assertThat
import com.rotai.iq.core.domain.model.RideCategory
import com.rotai.iq.core.domain.model.RidePlatform
import com.rotai.iq.core.domain.parser.NinetyNineParser
import com.rotai.iq.core.domain.parser.OfferNormalizer
import com.rotai.iq.core.domain.parser.PlatformDetector
import com.rotai.iq.core.domain.parser.UberParser
import org.junit.Test

class OfferParserTest {

    @Test
    fun offerNormalizer_parsesBrlCurrency() {
        assertThat(OfferNormalizer.parseCurrency("R$ 32,80")).isEqualTo(32.80)
        assertThat(OfferNormalizer.parseCurrency("R$ 1.250,50")).isEqualTo(1250.50)
        assertThat(OfferNormalizer.parseCurrency("Valor: $ 15,00")).isEqualTo(15.00)
    }

    @Test
    fun offerNormalizer_parsesKmDistances() {
        val distances = OfferNormalizer.parseAllDistancesKm("1,2 km até o local • Viagem de 9,4 km")
        assertThat(distances).containsExactly(1.2, 9.4).inOrder()
    }

    @Test
    fun offerNormalizer_parsesDurations() {
        val durations = OfferNormalizer.parseAllDurationsMinutes("4 min até embarque • 26 min de viagem")
        assertThat(durations).containsExactly(4.0, 26.0).inOrder()
    }

    @Test
    fun offerNormalizer_parsesStopsCount() {
        assertThat(OfferNormalizer.parseStopsCount("Corrida com 2 paradas")).isEqualTo(2)
        assertThat(OfferNormalizer.parseStopsCount("1 parada intermediária")).isEqualTo(1)
        assertThat(OfferNormalizer.parseStopsCount("Sem paradas")).isEqualTo(0)
    }

    @Test
    fun uberParser_parsesTypicalCard() {
        val rawUber = """
            UberX
            R$ 32,80
            1,2 km • 4 min
            Viagem: 9,4 km • 26 min
        """.trimIndent()

        val parser = UberParser()
        assertThat(parser.canParse(rawUber, "com.ubercab.driver")).isTrue()

        val offer = parser.parse(rawUber)
        assertThat(offer).isNotNull()
        assertThat(offer?.platform).isEqualTo(RidePlatform.UBER)
        assertThat(offer?.grossFare).isEqualTo(32.80)
        assertThat(offer?.pickupDistanceKm).isEqualTo(1.2)
        assertThat(offer?.distanceKm).isEqualTo(9.4)
        assertThat(offer?.category).isEqualTo(RideCategory.UBER_X)
    }

    @Test
    fun ninetyNineParser_parsesTypicalCard() {
        val raw99 = """
            99Pop
            R$ 22,50
            0,8 km • 3 min
            Viagem: 6,5 km • 18 min
        """.trimIndent()

        val parser = NinetyNineParser()
        assertThat(parser.canParse(raw99, "com.taxis99")).isTrue()

        val offer = parser.parse(raw99)
        assertThat(offer).isNotNull()
        assertThat(offer?.platform).isEqualTo(RidePlatform.NINETY_NINE)
        assertThat(offer?.grossFare).isEqualTo(22.50)
        assertThat(offer?.pickupDistanceKm).isEqualTo(0.8)
        assertThat(offer?.distanceKm).isEqualTo(6.5)
        assertThat(offer?.category).isEqualTo(RideCategory.POP_99)
    }

    @Test
    fun platformDetector_routesToCorrectParser() {
        val detector = PlatformDetector()
        val offer = detector.detectAndParse("Uber Comfort R$ 45,00 • 2,0 km • 15,0 km")
        assertThat(offer?.platform).isEqualTo(RidePlatform.UBER)
        assertThat(offer?.category).isEqualTo(RideCategory.UBER_COMFORT)
    }
}
