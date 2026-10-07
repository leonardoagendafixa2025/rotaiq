package com.rotai.iq.core.domain.parser

import com.rotai.iq.core.domain.model.RideCategory
import com.rotai.iq.core.domain.model.RideOffer
import com.rotai.iq.core.domain.model.RidePlatform

class InDriveParser : OfferParser {
    override val platform: RidePlatform = RidePlatform.INDRAVE

    override fun canParse(rawText: String, packageName: String?): Boolean {
        if (packageName == RidePlatform.INDRAVE.packageName) return true
        val lower = rawText.lowercase()
        return lower.contains("indrive") ||
                lower.contains("proposta do passageiro") ||
                lower.contains("passageiro oferece") ||
                (lower.contains("contraproposta") && lower.contains("r$"))
    }

    override fun parse(rawText: String): RideOffer? {
        val grossFare = OfferNormalizer.parseCurrency(rawText) ?: return null
        val distances = OfferNormalizer.parseAllDistancesKm(rawText)
        val durations = OfferNormalizer.parseAllDurationsMinutes(rawText)
        val stops = OfferNormalizer.parseStopsCount(rawText)

        // inDrive frequentemente mostra: [distância até passageiro] e [distância da viagem]
        val (pickupDistance, tripDistance) = when {
            distances.size >= 2 -> Pair(distances[0], distances[1])
            distances.size == 1 -> Pair(0.0, distances[0])
            else -> return null
        }

        val (pickupDuration, tripDuration) = when {
            durations.size >= 2 -> Pair(durations[0], durations[1])
            durations.size == 1 -> Pair(0.0, durations[0])
            else -> {
                // Estimativa realista se não houver duração explícita: média 2.2 min por km
                val estimated = (tripDistance * 2.2).coerceAtLeast(8.0)
                Pair(3.0, estimated)
            }
        }

        return RideOffer(
            platform = RidePlatform.INDRAVE,
            grossFare = grossFare,
            distanceKm = tripDistance,
            durationMinutes = tripDuration,
            pickupDistanceKm = pickupDistance,
            pickupDurationMinutes = pickupDuration,
            stopsCount = stops,
            category = RideCategory.STANDARD,
            rawText = rawText
        )
    }
}
