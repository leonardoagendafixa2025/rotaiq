package com.rotai.iq

import com.google.common.truth.Truth.assertThat
import com.rotai.iq.core.domain.engine.MultiPeriodGoalEngine
import org.junit.Test

class MultiPeriodGoalEngineTest {

    @Test
    fun calculateProgress_normalPace_returnsAccurateMetrics() {
        val goals = MultiPeriodGoalEngine.MultiPeriodGoals(
            dailyGrossTarget = 300.0,
            weeklyGrossTarget = 1800.0,
            monthlyGrossTarget = 7500.0
        )

        val progress = MultiPeriodGoalEngine.calculateProgress(
            goals = goals,
            dailyCurrent = 150.0,
            weeklyCurrent = 900.0,
            monthlyCurrent = 3750.0,
            hoursWorkedToday = 3.0,
            shiftPlannedHours = 6.0
        )

        assertThat(progress.remainingDaily).isWithin(0.01).of(150.0)
        assertThat(progress.remainingWeekly).isWithin(0.01).of(900.0)
        assertThat(progress.remainingMonthly).isWithin(0.01).of(3750.0)
        assertThat(progress.dailyPercentage).isWithin(0.01).of(50.0)
        assertThat(progress.weeklyPercentage).isWithin(0.01).of(50.0)
        assertThat(progress.monthlyPercentage).isWithin(0.01).of(50.0)
        assertThat(progress.currentHourlyPace).isWithin(0.01).of(50.0)
        assertThat(progress.requiredHourlyPace).isWithin(0.01).of(50.0)
        assertThat(progress.coachingMessage).contains("Seu ritmo atual")
    }

    @Test
    fun calculateProgress_dailyCompleted_celebratesAndFocusesOnWeekly() {
        val goals = MultiPeriodGoalEngine.MultiPeriodGoals(
            dailyGrossTarget = 300.0,
            weeklyGrossTarget = 1800.0,
            monthlyGrossTarget = 7500.0
        )

        val progress = MultiPeriodGoalEngine.calculateProgress(
            goals = goals,
            dailyCurrent = 350.0,
            weeklyCurrent = 1200.0,
            monthlyCurrent = 5000.0,
            hoursWorkedToday = 7.0,
            shiftPlannedHours = 8.0
        )

        assertThat(progress.remainingDaily).isEqualTo(0.0)
        assertThat(progress.dailyPercentage).isEqualTo(100.0)
        assertThat(progress.coachingMessage).contains("Meta diária concluída")
        assertThat(progress.coachingMessage).contains("600")
    }

    @Test
    fun calculateProgress_behindPace_indicatesRequiredPace() {
        val goals = MultiPeriodGoalEngine.MultiPeriodGoals(
            dailyGrossTarget = 300.0,
            weeklyGrossTarget = 1800.0,
            monthlyGrossTarget = 7500.0
        )

        val progress = MultiPeriodGoalEngine.calculateProgress(
            goals = goals,
            dailyCurrent = 60.0,
            weeklyCurrent = 300.0,
            monthlyCurrent = 1000.0,
            hoursWorkedToday = 4.0, // pace 15/h
            shiftPlannedHours = 8.0 // remaining 4h, need 240 / 4 = 60/h
        )

        assertThat(progress.currentHourlyPace).isWithin(0.01).of(15.0)
        assertThat(progress.requiredHourlyPace).isWithin(0.01).of(60.0)
        assertThat(progress.coachingMessage).contains("Para atingir sua meta")
        assertThat(progress.coachingMessage).contains("60")
    }
}
