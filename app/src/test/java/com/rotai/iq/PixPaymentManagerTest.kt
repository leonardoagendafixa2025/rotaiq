package com.rotai.iq

import com.rotai.iq.core.domain.engine.PixPaymentManager
import com.rotai.iq.core.domain.model.SubscriptionTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PixPaymentManagerTest {

    private lateinit var pixPaymentManager: PixPaymentManager

    @Before
    fun setUp() {
        pixPaymentManager = PixPaymentManager(
            merchantPixKey = "pix@rotai.com.br",
            merchantName = "ROTA IQ TECNOLOGIA",
            merchantCity = "SAO PAULO"
        )
    }

    @Test
    fun testGenerateEmvPayloadAndCrc16Integrity() {
        val payload = pixPaymentManager.generateEmvPayload(
            pixKey = "pix@rotai.com.br",
            amount = 29.90,
            txId = "ROTAIQTEST1",
            name = "ROTA IQ TECNOLOGIA",
            city = "SAO PAULO"
        )

        // Verifica cabeçalhos e tags essenciais do padrão BACEN
        assertTrue(payload.startsWith("000201")) // Payload Format Indicator
        assertTrue(payload.contains("br.gov.bcb.pix"))
        assertTrue(payload.contains("pix@rotai.com.br"))
        assertTrue(payload.contains("5303986")) // Currency BRL
        assertTrue(payload.contains("540529.90")) // Amount
        assertTrue(payload.contains("5802BR")) // Country Code
        assertTrue(payload.contains("5918ROTA IQ TECNOLOGIA"))
        assertTrue(payload.contains("6009SAO PAULO"))
        assertTrue(payload.contains("6304")) // CRC Tag

        // Valida cálculo do CRC16
        assertTrue(pixPaymentManager.validatePayloadCrc(payload))
    }

    @Test
    fun testCreatePixOrderForAnnualPlan() {
        val order = pixPaymentManager.createPixOrder(SubscriptionTier.PRO_ANNUAL)

        assertEquals(SubscriptionTier.PRO_ANNUAL, order.planTier)
        assertEquals(23990, order.amountCents)
        assertEquals(239.90, order.amountReais, 0.001)
        assertFalse(order.isExpired)
        assertTrue(order.pixCopiaECola.isNotBlank())
        assertTrue(pixPaymentManager.validatePayloadCrc(order.pixCopiaECola))
    }

    @Test
    fun testValidatePayloadCrcDetectsCorruptedPayload() {
        val validPayload = pixPaymentManager.generateEmvPayload(
            pixKey = "pix@rotai.com.br",
            amount = 10.00,
            txId = "CORRUPTTEST",
            name = "ROTA IQ",
            city = "SP"
        )

        // Modifica um caractere no meio do payload simulando corrupção
        val corruptedPayload = validPayload.replaceFirst("ROTA IQ", "FAKE IQ")
        assertFalse(pixPaymentManager.validatePayloadCrc(corruptedPayload))
    }
}
