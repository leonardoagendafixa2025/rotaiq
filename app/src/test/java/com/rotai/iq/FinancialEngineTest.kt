package com.rotai.iq

import com.google.common.truth.Truth.assertThat
import com.rotai.iq.core.domain.engine.FinancialEngine
import org.junit.Test

class FinancialEngineTest {

    @Test
    fun calculateMetrics_normalRide_computesAccurateFinancials() {
        val metrics = FinancialEngine.calculateMetrics(
            grossFare = 32.80,
            totalCost = 6.20,
            totalDistanceKm = 9.4,
            totalDurationMinutes = 26.0
        )

        // Net profit = 32.80 - 6.20 = 26.60
        assertThat(metrics.netProfit).isWithin(0.01).of(26.60)
        // Margin = (26.60 / 32.80) * 100 = ~81.09%
        assertThat(metrics.profitMarginPercent).isWithin(0.1).of(81.1)
        // Rate per km = 32.80 / 9.4 = ~3.489 R$/km
        assertThat(metrics.grossRatePerKm).isWithin(0.02).of(3.49)
        // Rate per hour = 32.80 / (26/60) = 32.80 / 0.4333 = ~75.69 R$/h
        assertThat(metrics.grossRatePerHour).isWithin(0.1).of(75.69)
        // Net rate per hour = 26.60 / (26/60) = ~61.38 R$/h
        assertThat(metrics.netRatePerHour).isWithin(0.1).of(61.38)
    }

    @Test
    fun calculateMetrics_negativeProfitRide_reportsNegativeMargin() {
        val metrics = FinancialEngine.calculateMetrics(
            grossFare = 10.00,
            totalCost = 14.50,
            totalDistanceKm = 15.0,
            totalDurationMinutes = 40.0
        )

        assertThat(metrics.netProfit).isLessThan(0.0)
        assertThat(metrics.profitMarginPercent).isLessThan(0.0)
    }
}
