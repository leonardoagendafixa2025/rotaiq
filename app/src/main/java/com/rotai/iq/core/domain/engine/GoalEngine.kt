package com.rotai.iq.core.domain.engine

import com.rotai.iq.core.domain.model.DriverGoal
import java.util.Locale

object GoalEngine {

    enum class GoalPaceStatus(val label: String, val emoji: String) {
        GOAL_REACHED("Meta Atingida!", "🏆"),
        ON_TRACK("No ritmo planejado", "⚡"),
        NEEDS_ATTENTION("Ritmo ligeiramente abaixo", "⚠️"),
        BEHIND_PACE("Abaixo da meta", "🚨")
    }

    data class GoalCoachingAdvice(
        val remainingAmount: Double,
        val remainingMessage: String,
        val paceProjectionMessage: String,
        val requiredRateMessage: String,
        val status: GoalPaceStatus,
        val progressPercentage: Double
    )

    fun evaluateGoalProgress(goal: DriverGoal): GoalCoachingAdvice {
        val remaining = goal.remainingGross
        val progressPct = goal.progressPercent

        if (remaining <= 0.0) {
            return GoalCoachingAdvice(
                remainingAmount = 0.0,
                remainingMessage = "Parabéns! Você já atingiu sua meta diária de R$ %.2f.".format(Locale("pt", "BR"), goal.dailyGrossTarget),
                paceProjectionMessage = "Faturamento atual: R$ %.2f".format(Locale("pt", "BR"), goal.currentDailyGross),
                requiredRateMessage = "Meta concluída.",
                status = GoalPaceStatus.GOAL_REACHED,
                progressPercentage = 100.0
            )
        }

        val remainingHours = goal.remainingHoursInShift
        val requiredPace = goal.requiredHourlyPace
        val currentPace = goal.currentHourlyPace
        val projected = goal.projectedDailyGross

        val status = when {
            goal.hoursWorkedToday == 0.0 -> GoalPaceStatus.ON_TRACK
            projected >= goal.dailyGrossTarget -> GoalPaceStatus.ON_TRACK
            projected >= goal.dailyGrossTarget * 0.85 -> GoalPaceStatus.NEEDS_ATTENTION
            else -> GoalPaceStatus.BEHIND_PACE
        }

        val remainingMsg = "Você precisa faturar mais R$ %.2f hoje.".format(Locale("pt", "BR"), remaining)
        val projectionMsg = if (goal.hoursWorkedToday > 0.0) {
            "Seu ritmo atual indica R$ %.2f até o final do turno.".format(Locale("pt", "BR"), projected)
        } else {
            "Turno em início. Foco em atingir R$ %.2f.".format(Locale("pt", "BR"), goal.dailyGrossTarget)
        }
        val rateMsg = if (remainingHours > 0.0) {
            "Para atingir sua meta, você precisa fazer aproximadamente R$ %.2f/h.".format(Locale("pt", "BR"), requiredPace)
        } else {
            "Horário de turno estourado. Faltam R$ %.2f.".format(Locale("pt", "BR"), remaining)
        }

        return GoalCoachingAdvice(
            remainingAmount = remaining,
            remainingMessage = remainingMsg,
            paceProjectionMessage = projectionMsg,
            requiredRateMessage = rateMsg,
            status = status,
            progressPercentage = progressPct
        )
    }
}
