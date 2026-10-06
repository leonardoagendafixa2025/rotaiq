package com.rotai.iq.core.domain.engine

import com.rotai.iq.core.domain.model.PixPaymentOrder
import com.rotai.iq.core.domain.model.SubscriptionTier
import java.util.Locale
import java.util.UUID

class PixPaymentManager(
    private val merchantPixKey: String = "pix@rotai.com.br",
    private val merchantName: String = "ROTA IQ TECNOLOGIA",
    private val merchantCity: String = "SAO PAULO"
) {

    /**
     * Cria um novo pedido PIX para o plano selecionado.
     */
    fun createPixOrder(
        planTier: SubscriptionTier,
        validityMinutes: Long = 15
    ): PixPaymentOrder {
        val orderId = "ROTAIQ" + UUID.randomUUID().toString().replace("-", "").take(8).uppercase(Locale.ROOT)
        val amountReais = planTier.fullPrice
        val amountCents = (amountReais * 100).toInt()
        val expiresAt = System.currentTimeMillis() + (validityMinutes * 60 * 1000)

        val emvPayload = generateEmvPayload(
            pixKey = merchantPixKey,
            amount = amountReais,
            txId = orderId,
            name = merchantName,
            city = merchantCity
        )

        return PixPaymentOrder(
            orderId = orderId,
            planTier = planTier,
            amountCents = amountCents,
            amountReais = amountReais,
            pixKey = merchantPixKey,
            pixCopiaECola = emvPayload,
            qrCodeEmv = emvPayload,
            expiresAtEpochMs = expiresAt,
            status = "PENDING"
        )
    }

    /**
     * Gera string EMV BR Code (Pix Copia e Cola) oficial conforme padrão BACEN.
     */
    fun generateEmvPayload(
        pixKey: String,
        amount: Double,
        txId: String,
        name: String,
        city: String
    ): String {
        val formattedAmount = String.format(Locale.US, "%.2f", amount)
        val cleanTxId = txId.take(25)

        // Subcampos de Merchant Account Information (Tag 26)
        val gui = formatEmvField("00", "br.gov.bcb.pix")
        val key = formatEmvField("01", pixKey)
        val merchantAccountInfo = formatEmvField("26", gui + key)

        // Subcampo de Additional Data (Tag 62)
        val txIdField = formatEmvField("05", cleanTxId)
        val additionalData = formatEmvField("62", txIdField)

        val basePayload = buildString {
            append(formatEmvField("00", "01")) // Payload Format Indicator
            append(merchantAccountInfo)
            append(formatEmvField("52", "0000")) // Merchant Category Code
            append(formatEmvField("53", "986"))  // Currency BRL (986)
            append(formatEmvField("54", formattedAmount)) // Amount
            append(formatEmvField("58", "BR"))   // Country Code
            append(formatEmvField("59", name.take(25))) // Merchant Name
            append(formatEmvField("60", city.take(15))) // Merchant City
            append(additionalData)
            append("6304") // CRC16 Header
        }

        val crc = calculateCrc16(basePayload)
        return basePayload + crc
    }

    /**
     * Formata campo TLV (Tag, Length, Value) padrão EMV.
     */
    private fun formatEmvField(id: String, value: String): String {
        val length = String.format(Locale.ROOT, "%02d", value.length)
        return "$id$length$value"
    }

    /**
     * Calcula CRC-16/CCITT-FALSE (Polinômio 0x1021, valor inicial 0xFFFF).
     */
    fun calculateCrc16(data: String): String {
        var crc = 0xFFFF
        val polynomial = 0x1021

        val bytes = data.toByteArray(Charsets.ISO_8859_1)
        for (b in bytes) {
            for (i in 0 until 8) {
                val bit = (b.toInt() shr (7 - i) and 1) == 1
                val c15 = (crc shr 15 and 1) == 1
                crc = crc shl 1
                if (c15 xor bit) {
                    crc = crc xor polynomial
                }
            }
        }
        crc = crc and 0xFFFF
        return String.format(Locale.ROOT, "%04X", crc)
    }

    /**
     * Valida integridade do CRC16 de um payload PIX Copia e Cola.
     */
    fun validatePayloadCrc(payload: String): Boolean {
        if (payload.length < 8) return false
        val dataWithoutCrc = payload.substring(0, payload.length - 4)
        val receivedCrc = payload.takeLast(4).uppercase(Locale.ROOT)
        val calculatedCrc = calculateCrc16(dataWithoutCrc)
        return receivedCrc == calculatedCrc
    }
}
