package com.rotai.iq.core.domain.parser

import com.rotai.iq.core.domain.model.RideCategory
import com.rotai.iq.core.domain.model.RideOffer
import com.rotai.iq.core.domain.model.RidePlatform

class DeliveryParser : OfferParser {
    override val platform: RidePlatform = RidePlatform.OTHER

    override fun canParse(rawText: String, packageName: String?): Boolean {
        val lower = rawText.lowercase()
        return lower.contains("flash") ||
                lower.contains("entrega") ||
                lower.contains("pacote") ||
                lower.contains("encomenda") ||
                lower.contains("lalamove") ||
                lower.contains("uber flash") ||
                lower.contains("99entrega")
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

        // Entregas têm overhead de coleta e entrega (espera pelo remetente/destinatário: +5 min adicionais)
        val packageOverheadMinutes = 5.0

        val (pickupDuration, tripDuration) = when {
            durations.size >= 2 -> Pair(durations[0], durations[1] + packageOverheadMinutes)
            durations.size == 1 -> Pair(0.0, durations[0] + packageOverheadMinutes)
            else -> Pair(0.0, 15.0)
        }

        val platform = when {
            rawText.contains("uber", ignoreCase = true) -> RidePlatform.UBER
            rawText.contains("99", ignoreCase = true) -> RidePlatform.NINETY_NINE
            else -> RidePlatform.OTHER
        }

        return RideOffer(
            platform = platform,
            grossFare = grossFare,
            distanceKm = tripDistance,
            durationMinutes = tripDuration,
            pickupDistanceKm = pickupDistance,
            pickupDurationMinutes = pickupDuration,
            stopsCount = stops,
            category = RideCategory.DELIVERY,
            rawText = rawText
        )
    }
}
