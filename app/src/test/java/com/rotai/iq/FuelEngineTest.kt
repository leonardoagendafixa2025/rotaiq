package com.rotai.iq

import com.google.common.truth.Truth.assertThat
import com.rotai.iq.core.domain.engine.FuelEngine
import com.rotai.iq.core.domain.model.FuelRecord
import org.junit.Test

class FuelEngineTest {

    @Test
    fun calculateConsumptionBetweenFills_validConsecutiveTanks_calculatesExactKmPerLiter() {
        val tank1 = FuelRecord(
            date = "2026-10-01",
            odometerKm = 50000.0,
            liters = 40.0,
            pricePerLiter = 5.50,
            isFullTank = true
        )
        val tank2 = FuelRecord(
            date = "2026-10-05",
            odometerKm = 50450.0, // rodou 450 km
            liters = 36.0,        // abasteceu 36 litros até encher
            pricePerLiter = 5.50, // total = 198.00
            isFullTank = true
        )

        val result = FuelEngine.calculateConsumptionBetweenFills(tank1, tank2)
        assertThat(result).isNotNull()
        // Consumo = 450 km / 36 L = 12.5 km/L
        assertThat(result?.consumptionKmPerLiter).isWithin(0.01).of(12.5)
        // Custo por km = 198.00 / 450 = 0.44 R$/km
        assertThat(result?.realCostPerKm).isWithin(0.01).of(0.44)
        assertThat(result?.isConsistent).isTrue()
    }

    @Test
    fun calculateConsumptionBetweenFills_partialTank_returnsNull() {
        val tank1 = FuelRecord(odometerKm = 50000.0, liters = 20.0, pricePerLiter = 5.0, date = "2026-10-01", isFullTank = false)
        val tank2 = FuelRecord(odometerKm = 50200.0, liters = 20.0, pricePerLiter = 5.0, date = "2026-10-03", isFullTank = true)

        val result = FuelEngine.calculateConsumptionBetweenFills(tank1, tank2)
        assertThat(result).isNull()
    }

    @Test
    fun calculateAverageConsumption_multipleFills_returnsWeightedAverage() {
        val list = listOf(
            FuelRecord(odometerKm = 50000.0, liters = 40.0, pricePerLiter = 5.0, date = "2026-10-01", isFullTank = true),
            FuelRecord(odometerKm = 50400.0, liters = 40.0, pricePerLiter = 5.0, date = "2026-10-04", isFullTank = true), // 400km / 40L = 10 km/L
            FuelRecord(odometerKm = 50900.0, liters = 50.0, pricePerLiter = 5.0, date = "2026-10-08", isFullTank = true)  // 500km / 50L = 10 km/L
        )

        val avg = FuelEngine.calculateAverageConsumption(list)
        assertThat(avg).isNotNull()
        assertThat(avg).isWithin(0.01).of(10.0)
    }
}
