package com.rotai.iq

import com.google.common.truth.Truth.assertThat
import com.rotai.iq.core.domain.engine.GoalEngine
import com.rotai.iq.core.domain.model.DriverGoal
import org.junit.Test

class GoalEngineTest {

    @Test
    fun evaluateGoalProgress_onTrack_producesEncouragingAdvice() {
        val goal = DriverGoal(
            dailyGrossTarget = 300.0,
            shiftTargetHours = 8.0,
            currentDailyGross = 150.0,
            hoursWorkedToday = 3.0 // 50 R$/h pace!
        )
        val advice = GoalEngine.evaluateGoalProgress(goal)

        assertThat(advice.remainingAmount).isWithin(0.01).of(150.0)
        assertThat(advice.status).isEqualTo(GoalEngine.GoalPaceStatus.ON_TRACK)
        assertThat(advice.remainingMessage).contains("150")
        assertThat(advice.paceProjectionMessage).contains("400") // 150 + 50*5 = 400
    }

    @Test
    fun evaluateGoalProgress_behindPace_warnsDriver() {
        val goal = DriverGoal(
            dailyGrossTarget = 300.0,
            shiftTargetHours = 8.0,
            currentDailyGross = 60.0,
            hoursWorkedToday = 4.0 // 15 R$/h pace, projected = 60 + 15*4 = 120 (way below 300)
        )
        val advice = GoalEngine.evaluateGoalProgress(goal)

        assertThat(advice.status).isEqualTo(GoalEngine.GoalPaceStatus.BEHIND_PACE)
        assertThat(advice.remainingAmount).isWithin(0.01).of(240.0)
        assertThat(advice.requiredRateMessage).contains("60,00/h") // 240 / 4h = 60 R$/h
    }

    @Test
    fun evaluateGoalProgress_goalReached_celebratesSuccess() {
        val goal = DriverGoal(
            dailyGrossTarget = 300.0,
            shiftTargetHours = 8.0,
            currentDailyGross = 320.0,
            hoursWorkedToday = 6.0
        )
        val advice = GoalEngine.evaluateGoalProgress(goal)

        assertThat(advice.status).isEqualTo(GoalEngine.GoalPaceStatus.GOAL_REACHED)
        assertThat(advice.remainingAmount).isEqualTo(0.0)
        assertThat(advice.progressPercentage).isEqualTo(100.0)
    }
}
