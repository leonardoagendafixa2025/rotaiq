package com.rotai.iq

import com.rotai.iq.core.domain.model.RideCategory
import com.rotai.iq.core.domain.model.RidePlatform
import com.rotai.iq.core.domain.parser.DeliveryParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeliveryParserTest {

    private lateinit var parser: DeliveryParser

    @Before
    fun setUp() {
        parser = DeliveryParser()
    }

    @Test
    fun canParse_detectsDeliveryKeywords() {
        assertTrue(parser.canParse("Uber Flash entrega de encomenda", null))
        assertTrue(parser.canParse("99Entrega pacote pequeno R$ 15,00", null))
        assertTrue(parser.canParse("Lalamove entrega expressa", null))
    }

    @Test
    fun parse_setsCategoryToDeliveryAndIncludesPickupOverhead() {
        val sampleText = """
            Uber Flash
            R$ 21,90
            1,2 km até coleta (3 min)
            6,5 km até entrega (15 min)
        """.trimIndent()

        val offer = parser.parse(sampleText)
        assertNotNull(offer)
        assertEquals(RidePlatform.UBER, offer!!.platform)
        assertEquals(RideCategory.DELIVERY, offer.category)
        assertEquals(21.90, offer.grossFare, 0.01)
        // 15 min + 5 min overhead = 20 min de viagem
        assertEquals(20.0, offer.durationMinutes, 0.01)
    }
}
