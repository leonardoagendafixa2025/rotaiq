package com.rotai.iq.core.security

import java.util.Locale

object PiiSanitizer {

    private val CPF_REGEX = Regex("""\b\d{3}\.?\d{3}\.?\d{3}-?\d{2}\b""")
    private val EMAIL_REGEX = Regex("""\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}\b""")
    private val PHONE_REGEX = Regex("""\b(?:\+?55\s?)?(?:\(?\d{2}\)?\s?)?(?:9?\d{4})[-.\s]?\d{4}\b""")
    private val PLATE_REGEX = Regex("""\b[A-Z]{3}-?\d[A-Z0-9]\d{2}\b""", RegexOption.IGNORE_CASE)

    /**
     * Sanitiza qualquer texto removendo ou mascarando dados pessoais identificáveis (PII).
     */
    fun sanitizeText(input: String?): String {
        if (input.isNullOrBlank()) return ""

        var sanitized = input
        sanitized = sanitizeCpfInText(sanitized)
        sanitized = sanitizeEmailInText(sanitized)
        sanitized = sanitizePhoneInText(sanitized)
        sanitized = sanitizePlateInText(sanitized)

        return sanitized
    }

    fun maskCpf(cpf: String): String {
        val clean = cpf.filter { it.isDigit() }
        if (clean.length != 11) return "***.***.***-**"
        return "***.${clean.substring(3, 6)}.${clean.substring(6, 9)}-**"
    }

    fun maskEmail(email: String): String {
        val parts = email.split("@")
        if (parts.size != 2 || parts[0].isEmpty()) return "***@***.***"
        val name = parts[0]
        val domain = parts[1]
        val maskedName = if (name.length <= 2) "***" else "${name.first()}***${name.last()}"
        return "$maskedName@$domain"
    }

    fun maskPhone(phone: String): String {
        val clean = phone.filter { it.isDigit() }
        if (clean.length < 8) return "(**) *****-****"
        val ddd = if (clean.length >= 10) clean.substring(0, 2) else "XX"
        val suffix = clean.takeLast(2)
        return "($ddd) 9****-**$suffix"
    }

    fun maskPlate(plate: String): String {
        val clean = plate.replace("-", "").uppercase(Locale.ROOT)
        if (clean.length != 7) return "***-****"
        return "${clean.take(3)}-****"
    }

    fun obfuscateCoordinates(lat: Double, lon: Double): Pair<Double, Double> {
        // Reduz a precisão geográfica para ~1.1km para proteger residências de motoristas/passageiros
        val obfuscatedLat = String.format(Locale.US, "%.2f", lat).toDouble()
        val obfuscatedLon = String.format(Locale.US, "%.2f", lon).toDouble()
        return Pair(obfuscatedLat, obfuscatedLon)
    }

    private fun sanitizeCpfInText(text: String): String {
        return CPF_REGEX.replace(text) { matchResult ->
            maskCpf(matchResult.value)
        }
    }

    private fun sanitizeEmailInText(text: String): String {
        return EMAIL_REGEX.replace(text) { matchResult ->
            maskEmail(matchResult.value)
        }
    }

    private fun sanitizePhoneInText(text: String): String {
        return PHONE_REGEX.replace(text) { matchResult ->
            maskPhone(matchResult.value)
        }
    }

    private fun sanitizePlateInText(text: String): String {
        return PLATE_REGEX.replace(text) { matchResult ->
            maskPlate(matchResult.value)
        }
    }
}
