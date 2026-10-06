package com.rotai.iq.core.domain.parser

import com.rotai.iq.core.domain.model.RideCategory
import com.rotai.iq.core.domain.model.RideOffer
import com.rotai.iq.core.domain.model.RidePlatform

interface OfferParser {
    val platform: RidePlatform
    fun canParse(rawText: String, packageName: String?): Boolean
    fun parse(rawText: String): RideOffer?
}

object OfferNormalizer {

    private val currencyRegex = Regex("""(?:R\$|\$)\s*([0-9]{1,3}(?:\.[0-9]{3})*,[0-9]{2}|[0-9]{1,3}(?:,[0-9]{3})*\.[0-9]{2}|[0-9]+[.,][0-9]{2})""")
    private val distanceRegex = Regex("""([0-9]+(?:[.,][0-9]+)?)\s*km""", RegexOption.IGNORE_CASE)
    private val timeRegex = Regex("""([0-9]+)\s*(?:min|m\b)""", RegexOption.IGNORE_CASE)
    private val stopsRegex = Regex("""([0-9]+)\s*parad[as]?""", RegexOption.IGNORE_CASE)

    fun parseCurrency(text: String): Double? {
        val match = currencyRegex.find(text) ?: return null
        val raw = match.groupValues[1]
        val clean = if (raw.contains(",") && raw.contains(".")) {
            if (raw.lastIndexOf(",") > raw.lastIndexOf(".")) {
                // Formato brasileiro: 1.250,50
                raw.replace(".", "").replace(",", ".")
            } else {
                // Formato US: 1,250.50
                raw.replace(",", "")
            }
        } else if (raw.contains(",")) {
            raw.replace(",", ".")
        } else {
            raw
        }
        return clean.toDoubleOrNull()
    }

    fun parseDistanceKm(text: String): Double? {
        val match = distanceRegex.find(text) ?: return null
        val clean = match.groupValues[1].replace(",", ".")
        return clean.toDoubleOrNull()
    }

    fun parseAllDistancesKm(text: String): List<Double> {
        return distanceRegex.findAll(text).mapNotNull {
            it.groupValues[1].replace(",", ".").toDoubleOrNull()
        }.toList()
    }

    fun parseDurationMinutes(text: String): Double? {
        val match = timeRegex.find(text) ?: return null
        return match.groupValues[1].toDoubleOrNull()
    }

    fun parseAllDurationsMinutes(text: String): List<Double> {
        return timeRegex.findAll(text).mapNotNull {
            it.groupValues[1].toDoubleOrNull()
        }.toList()
    }

    fun parseStopsCount(text: String): Int {
        val match = stopsRegex.find(text) ?: return 0
        return match.groupValues[1].toIntOrNull() ?: 0
    }
}
