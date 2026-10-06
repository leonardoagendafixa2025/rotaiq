package com.rotai.iq.core.domain.parser

import com.rotai.iq.core.domain.model.RideCategory
import com.rotai.iq.core.domain.model.RideOffer
import com.rotai.iq.core.domain.model.RidePlatform

class UberParser : OfferParser {
    override val platform: RidePlatform = RidePlatform.UBER

    override fun canParse(rawText: String, packageName: String?): Boolean {
        if (packageName == RidePlatform.UBER.packageName) return true
        val lower = rawText.lowercase()
        return lower.contains("uber") || lower.contains("uberx") || lower.contains("comfort")
    }

    override fun parse(rawText: String): RideOffer? {
        val grossFare = OfferNormalizer.parseCurrency(rawText) ?: return null
        val distances = OfferNormalizer.parseAllDistancesKm(rawText)
        val durations = OfferNormalizer.parseAllDurationsMinutes(rawText)
        val stops = OfferNormalizer.parseStopsCount(rawText)

        // Uber frequentemente exibe: [distância/tempo até passageiro] e [distância/tempo da viagem]
        val (pickupDistance, tripDistance) = when {
            distances.size >= 2 -> Pair(distances[0], distances[1])
            distances.size == 1 -> Pair(0.0, distances[0])
            else -> return null
        }

        val (pickupDuration, tripDuration) = when {
            durations.size >= 2 -> Pair(durations[0], durations[1])
            durations.size == 1 -> Pair(0.0, durations[0])
            else -> Pair(0.0, 10.0) // fallback de tempo
        }

        val category = when {
            rawText.contains("Comfort", ignoreCase = true) -> RideCategory.UBER_COMFORT
            rawText.contains("Black", ignoreCase = true) -> RideCategory.UBER_BLACK
            else -> RideCategory.UBER_X
        }

        return RideOffer(
            platform = RidePlatform.UBER,
            grossFare = grossFare,
            distanceKm = tripDistance,
            durationMinutes = tripDuration,
            pickupDistanceKm = pickupDistance,
            pickupDurationMinutes = pickupDuration,
            stopsCount = stops,
            category = category,
            rawText = rawText
        )
    }
}

class NinetyNineParser : OfferParser {
    override val platform: RidePlatform = RidePlatform.NINETY_NINE

    override fun canParse(rawText: String, packageName: String?): Boolean {
        if (packageName == RidePlatform.NINETY_NINE.packageName) return true
        val lower = rawText.lowercase()
        return lower.contains("99") || lower.contains("99pop") || lower.contains("99plus")
    }

    override fun parse(rawText: String): RideOffer? {
        val grossFare = OfferNormalizer.parseCurrency(rawText) ?: return null
        val distances = OfferNormalizer.parseAllDistancesKm(rawText)
        val durations = OfferNormalizer.parseAllDurationsMinutes(rawText)
        val stops = OfferNormalizer.parseStopsCount(rawText)

        val (pickupDistance, tripDistance) = when {
            distances.size >= 2 -> Pair(distances[0], distances[1])
            distances.size == 1 -> Pair(0.0, distances[0])
            else -> return null
        }

        val (pickupDuration, tripDuration) = when {
            durations.size >= 2 -> Pair(durations[0], durations[1])
            durations.size == 1 -> Pair(0.0, durations[0])
            else -> Pair(0.0, 10.0)
        }

        val category = when {
            rawText.contains("99Plus", ignoreCase = true) -> RideCategory.PLUS_99
            else -> RideCategory.POP_99
        }

        return RideOffer(
            platform = RidePlatform.NINETY_NINE,
            grossFare = grossFare,
            distanceKm = tripDistance,
            durationMinutes = tripDuration,
            pickupDistanceKm = pickupDistance,
            pickupDurationMinutes = pickupDuration,
            stopsCount = stops,
            category = category,
            rawText = rawText
        )
    }
}

class PlatformDetector(
    private val parsers: List<OfferParser> = listOf(UberParser(), NinetyNineParser())
) {
    fun detectAndParse(rawText: String, packageName: String? = null): RideOffer? {
        for (parser in parsers) {
            if (parser.canParse(rawText, packageName)) {
                val offer = parser.parse(rawText)
                if (offer != null) return offer
            }
        }
        // Fallback genérico se nenhuma marca identificada
        val fare = OfferNormalizer.parseCurrency(rawText) ?: return null
        val distances = OfferNormalizer.parseAllDistancesKm(rawText)
        val durations = OfferNormalizer.parseAllDurationsMinutes(rawText)
        if (distances.isEmpty()) return null

        val tripDist = distances.last()
        val pickupDist = if (distances.size > 1) distances.first() else 0.0
        val tripDur = if (durations.isNotEmpty()) durations.last() else 10.0
        val pickupDur = if (durations.size > 1) durations.first() else 0.0

        return RideOffer(
            platform = RidePlatform.OTHER,
            grossFare = fare,
            distanceKm = tripDist,
            durationMinutes = tripDur,
            pickupDistanceKm = pickupDist,
            pickupDurationMinutes = pickupDur,
            stopsCount = OfferNormalizer.parseStopsCount(rawText),
            category = RideCategory.STANDARD,
            rawText = rawText
        )
    }
}
