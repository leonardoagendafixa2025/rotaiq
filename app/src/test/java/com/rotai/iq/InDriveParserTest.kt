package com.rotai.iq

import com.rotai.iq.core.domain.model.RidePlatform
import com.rotai.iq.core.domain.parser.InDriveParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InDriveParserTest {

    private lateinit var parser: InDriveParser

    @Before
    fun setUp() {
        parser = InDriveParser()
    }

    @Test
    fun canParse_identifiesInDrivePackageAndKeywords() {
        assertTrue(parser.canParse("Oferta comum", "sinet.bm.driver"))
        assertTrue(parser.canParse("inDrive Proposta do passageiro R$ 25,00", null))
        assertTrue(parser.canParse("Passageiro oferece R$ 18,00 a 1,2 km", null))
    }

    @Test
    fun parse_extractsFaresAndDistancesCorrectly() {
        val sampleText = """
            inDrive
            Proposta do passageiro: R$ 24,50
            A 1,5 km de você (4 min)
            Distância da viagem: 8,0 km (18 min)
        """.trimIndent()

        val offer = parser.parse(sampleText)
        assertNotNull(offer)
        assertEquals(RidePlatform.INDRAVE, offer!!.platform)
        assertEquals(24.50, offer.grossFare, 0.01)
        assertEquals(1.5, offer.pickupDistanceKm, 0.01)
        assertEquals(8.0, offer.distanceKm, 0.01)
        assertEquals(9.5, offer.totalDistanceKm, 0.01)
    }

    @Test
    fun parse_fallbackDurationCalculatesReasonably() {
        val sampleText = "inDrive: R$ 30,00 - 12 km"
        val offer = parser.parse(sampleText)
        assertNotNull(offer)
        assertEquals(30.0, offer!!.grossFare, 0.01)
        assertEquals(12.0, offer.distanceKm, 0.01)
        assertTrue(offer.durationMinutes > 0)
    }
}
