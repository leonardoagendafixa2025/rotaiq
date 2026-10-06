package com.rotai.iq

import com.rotai.iq.core.security.PiiSanitizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PiiSanitizerTest {

    @Test
    fun testMaskCpf() {
        val masked = PiiSanitizer.maskCpf("123.456.789-00")
        assertEquals("***.456.789-**", masked)

        val cleanMasked = PiiSanitizer.maskCpf("12345678900")
        assertEquals("***.456.789-**", cleanMasked)
    }

    @Test
    fun testMaskEmail() {
        val masked = PiiSanitizer.maskEmail("motorista@gmail.com")
        assertEquals("m***a@gmail.com", masked)

        val shortMasked = PiiSanitizer.maskEmail("ab@uber.com")
        assertEquals("***@uber.com", shortMasked)
    }

    @Test
    fun testMaskPlate() {
        val masked = PiiSanitizer.maskPlate("ABC-1234")
        assertEquals("ABC-****", masked)

        val mercosul = PiiSanitizer.maskPlate("ABC1D23")
        assertEquals("ABC-****", mercosul)
    }

    @Test
    fun testSanitizeTextRemovesPiiInSentences() {
        val sentence = "Motorista com CPF 123.456.789-00 e email joao.silva@teste.com placa BRA2E19"
        val sanitized = PiiSanitizer.sanitizeText(sentence)

        assertFalse(sanitized.contains("123.456.789-00"))
        assertFalse(sanitized.contains("joao.silva@teste.com"))
        assertFalse(sanitized.contains("BRA2E19"))
        assertTrue(sanitized.contains("***.456.789-**"))
        assertTrue(sanitized.contains("BRA-****"))
    }

    @Test
    fun testObfuscateCoordinatesReducesPrecision() {
        val (lat, lon) = PiiSanitizer.obfuscateCoordinates(-23.550520, -46.633308)
        assertEquals(-23.55, lat, 0.001)
        assertEquals(-46.63, lon, 0.001)
    }
}
