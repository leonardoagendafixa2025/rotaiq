package com.rotai.iq

import com.google.common.truth.Truth.assertThat
import com.rotai.iq.core.domain.engine.PlatformComparisonEngine
import com.rotai.iq.core.domain.model.EvaluationClassification
import com.rotai.iq.core.domain.model.RideEvaluation
import com.rotai.iq.core.domain.model.RideOffer
import com.rotai.iq.core.domain.model.RidePlatform
import org.junit.Test

class PlatformComparisonEngineTest {

    @Test
    fun comparePlatforms_emptyEvaluations_returnsSafeEmptyReport() {
        val report = PlatformComparisonEngine.comparePlatforms(emptyList())
        assertThat(report.summaries).isEmpty()
        assertThat(report.bestPlatformByHourlyRate).isNull()
        assertThat(report.recommendation).contains("Nenhuma corrida")
    }

    @Test
    fun comparePlatforms_uberAndNinetyNine_computesAccurateAveragesAndIdentifiesBest() {
        val uberOffer = RideOffer(
            platform = RidePlatform.UBER,
            grossFare = 50.0,
            distanceKm = 10.0,
            durationMinutes = 30.0
        )
        val uberEval = RideEvaluation(
            offer = uberOffer,
            grossFare = 50.0,
            score = 90,
            classification = EvaluationClassification.EXCELLENT,
            estimatedCost = 10.0,
            netProfit = 40.0,
            profitMarginPercent = 80.0,
            grossRatePerKm = 5.0,
            netRatePerKm = 4.0,
            grossRatePerHour = 100.0,
            netRatePerHour = 80.0,
            grossRatePerMinute = 1.66,
            totalDistanceKm = 10.0,
            totalDurationMinutes = 30.0,
            reasons = emptyList(),
            alerts = emptyList()
        )

        val ninetyNineOffer = RideOffer(
            platform = RidePlatform.NINETY_NINE,
            grossFare = 20.0,
            distanceKm = 10.0,
            durationMinutes = 30.0
        )
        val ninetyNineEval = RideEvaluation(
            offer = ninetyNineOffer,
            grossFare = 20.0,
            score = 55,
            classification = EvaluationClassification.ACCEPTABLE,
            estimatedCost = 10.0,
            netProfit = 10.0,
            profitMarginPercent = 50.0,
            grossRatePerKm = 2.0,
            netRatePerKm = 1.0,
            grossRatePerHour = 40.0,
            netRatePerHour = 20.0,
            grossRatePerMinute = 0.66,
            totalDistanceKm = 10.0,
            totalDurationMinutes = 30.0,
            reasons = emptyList(),
            alerts = emptyList()
        )

        val report = PlatformComparisonEngine.comparePlatforms(listOf(uberEval, ninetyNineEval))

        assertThat(report.summaries).hasSize(2)
        assertThat(report.bestPlatformByHourlyRate).isEqualTo(RidePlatform.UBER)
        assertThat(report.bestPlatformByNetMargin).isEqualTo(RidePlatform.UBER)

        val uberSummary = report.summaries.first { it.platform == RidePlatform.UBER }
        assertThat(uberSummary.averageNetRatePerHour).isWithin(0.01).of(80.0)
        assertThat(uberSummary.profitMarginPercent).isWithin(0.01).of(80.0)

        val ninetyNineSummary = report.summaries.first { it.platform == RidePlatform.NINETY_NINE }
        assertThat(ninetyNineSummary.averageNetRatePerHour).isWithin(0.01).of(20.0)
        assertThat(ninetyNineSummary.profitMarginPercent).isWithin(0.01).of(50.0)

        assertThat(report.recommendation).contains("Uber")
    }
}
