package com.rotai.iq

import com.google.common.truth.Truth.assertThat
import com.rotai.iq.core.domain.engine.GeoHeatmapEngine
import com.rotai.iq.core.domain.model.DemandLevel
import com.rotai.iq.core.domain.model.TimeSlot
import org.junit.Test

class GeoHeatmapEngineTest {

    @Test
    fun getCurrentTimeSlot_mapsHourCorrectly() {
        assertThat(GeoHeatmapEngine.getCurrentTimeSlot(7)).isEqualTo(TimeSlot.MORNING_PEAK)
        assertThat(GeoHeatmapEngine.getCurrentTimeSlot(12)).isEqualTo(TimeSlot.DAYTIME)
        assertThat(GeoHeatmapEngine.getCurrentTimeSlot(18)).isEqualTo(TimeSlot.EVENING_PEAK)
        assertThat(GeoHeatmapEngine.getCurrentTimeSlot(22)).isEqualTo(TimeSlot.NIGHT)
        assertThat(GeoHeatmapEngine.getCurrentTimeSlot(3)).isEqualTo(TimeSlot.DAWN)
    }

    @Test
    fun evaluateZoneDemandForTimeSlot_detectsSurgeZonesInPeak() {
        val defaultZones = GeoHeatmapEngine.getDefaultZones()
        val paulista = defaultZones.first { it.id == "zone-paulista" }
        val periferia = defaultZones.first { it.id == "zone-periferia-extrema" }

        // Na hora de pico matutino, Paulista deve ser VERY_HIGH
        val paulistaDemand = GeoHeatmapEngine.evaluateZoneDemandForTimeSlot(paulista, TimeSlot.MORNING_PEAK)
        assertThat(paulistaDemand).isEqualTo(DemandLevel.VERY_HIGH)

        // De madrugada, periferia remota deve ser DEAD_ZONE
        val periferiaDawn = GeoHeatmapEngine.evaluateZoneDemandForTimeSlot(periferia, TimeSlot.DAWN)
        assertThat(periferiaDawn).isEqualTo(DemandLevel.DEAD_ZONE)
    }
}
