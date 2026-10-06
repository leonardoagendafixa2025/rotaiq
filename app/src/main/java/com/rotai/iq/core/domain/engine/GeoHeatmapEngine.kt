package com.rotai.iq.core.domain.engine

import com.rotai.iq.core.domain.model.DemandLevel
import com.rotai.iq.core.domain.model.GeoZone
import com.rotai.iq.core.domain.model.TimeSlot
import java.util.Calendar

object GeoHeatmapEngine {

    fun getCurrentTimeSlot(hourOfDay: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)): TimeSlot {
        return when (hourOfDay) {
            in 6..9 -> TimeSlot.MORNING_PEAK
            in 10..16 -> TimeSlot.DAYTIME
            in 17..20 -> TimeSlot.EVENING_PEAK
            in 21..23 -> TimeSlot.NIGHT
            else -> TimeSlot.DAWN
        }
    }

    fun getDefaultZones(): List<GeoZone> {
        return listOf(
            GeoZone(
                id = "zone-paulista",
                name = "Centro Financeiro (Paulista / Faria Lima / Itaim)",
                city = "São Paulo",
                baseDemandLevel = DemandLevel.VERY_HIGH,
                returnTripProbability = 0.95,
                averageWaitTimeMinutes = 3.0,
                deadheadKmToCenter = 0.0,
                isAvoidZone = false,
                isSurgeFrequent = true,
                notes = "Altíssima densidade de passageiros e tarifa dinâmica frequente."
            ),
            GeoZone(
                id = "zone-jardins",
                name = "Zona Sul Nobre (Jardins / Moema / Vila Olímpia)",
                city = "São Paulo",
                baseDemandLevel = DemandLevel.HIGH,
                returnTripProbability = 0.90,
                averageWaitTimeMinutes = 4.5,
                deadheadKmToCenter = 3.0,
                isAvoidZone = false,
                isSurgeFrequent = true,
                notes = "Ticket médio alto e excelente taxa de retorno."
            ),
            GeoZone(
                id = "zone-aeroporto-gru",
                name = "Aeroporto Internacional de Guarulhos (GRU)",
                city = "Guarulhos",
                baseDemandLevel = DemandLevel.HIGH,
                returnTripProbability = 0.85,
                averageWaitTimeMinutes = 12.0,
                deadheadKmToCenter = 26.0,
                isAvoidZone = false,
                isSurgeFrequent = true,
                notes = "Corridas longas de alto valor, mas exige retorno coordenado."
            ),
            GeoZone(
                id = "zone-tatuape",
                name = "Zona Leste Comercial (Tatuapé / Anália Franco)",
                city = "São Paulo",
                baseDemandLevel = DemandLevel.BALANCED,
                returnTripProbability = 0.75,
                averageWaitTimeMinutes = 7.0,
                deadheadKmToCenter = 8.0,
                isAvoidZone = false,
                isSurgeFrequent = false,
                notes = "Fluxo regular ao longo de todo o dia."
            ),
            GeoZone(
                id = "zone-granja-viana",
                name = "Condomínios Fechados / Granja Viana",
                city = "Cotia",
                baseDemandLevel = DemandLevel.LOW,
                returnTripProbability = 0.35,
                averageWaitTimeMinutes = 25.0,
                deadheadKmToCenter = 19.0,
                isAvoidZone = false,
                isSurgeFrequent = false,
                notes = "Risco de volta vazia pela Rodovia Raposo Tavares."
            ),
            GeoZone(
                id = "zone-periferia-extrema",
                name = "Periferia Remota / Área sem Retorno",
                city = "Grande SP",
                baseDemandLevel = DemandLevel.DEAD_ZONE,
                returnTripProbability = 0.15,
                averageWaitTimeMinutes = 40.0,
                deadheadKmToCenter = 24.0,
                isAvoidZone = false,
                isSurgeFrequent = false,
                notes = "Armadilha clássica: praticamente 100% de volta vazia."
            ),
            GeoZone(
                id = "zone-risco-noturno",
                name = "Zona Crítica / Alto Risco Noturno",
                city = "Grande SP",
                baseDemandLevel = DemandLevel.HIGH_RISK,
                returnTripProbability = 0.10,
                averageWaitTimeMinutes = 30.0,
                deadheadKmToCenter = 15.0,
                isAvoidZone = true,
                isSurgeFrequent = false,
                notes = "Área desaconselhada para operação por riscos de segurança."
            )
        )
    }

    fun evaluateZoneDemandForTimeSlot(zone: GeoZone, timeSlot: TimeSlot): DemandLevel {
        if (zone.isAvoidZone) return DemandLevel.HIGH_RISK

        return when (timeSlot) {
            TimeSlot.MORNING_PEAK -> {
                if (zone.id.contains("paulista") || zone.id.contains("aeroporto")) {
                    DemandLevel.VERY_HIGH
                } else zone.baseDemandLevel
            }
            TimeSlot.EVENING_PEAK -> {
                if (zone.id.contains("paulista") || zone.id.contains("jardins")) {
                    DemandLevel.VERY_HIGH
                } else zone.baseDemandLevel
            }
            TimeSlot.NIGHT -> {
                if (zone.id.contains("jardins") || zone.id.contains("paulista")) {
                    DemandLevel.HIGH
                } else if (zone.id.contains("granja") || zone.id.contains("periferia")) {
                    DemandLevel.DEAD_ZONE
                } else zone.baseDemandLevel
            }
            TimeSlot.DAWN -> {
                if (zone.id.contains("aeroporto")) {
                    DemandLevel.HIGH
                } else if (zone.id.contains("granja") || zone.id.contains("periferia")) {
                    DemandLevel.DEAD_ZONE
                } else DemandLevel.LOW
            }
            TimeSlot.DAYTIME -> zone.baseDemandLevel
        }
    }
}
