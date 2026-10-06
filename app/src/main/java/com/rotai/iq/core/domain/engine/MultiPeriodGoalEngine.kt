package com.rotai.iq.core.domain.engine

import java.util.Locale

object MultiPeriodGoalEngine {

    data class MultiPeriodGoals(
        val dailyGrossTarget: Double = 300.0,
        val weeklyGrossTarget: Double = 1800.0,
        val monthlyGrossTarget: Double = 7500.0,
        val targetHourlyRate: Double = 45.0,
        val targetKmRate: Double = 2.40
    )

    data class MultiPeriodProgress(
        val dailyCurrent: Double,
        val weeklyCurrent: Double,
        val monthlyCurrent: Double,
        val hoursWorkedToday: Double,
        val remainingDaily: Double,
        val remainingWeekly: Double,
        val remainingMonthly: Double,
        val dailyPercentage: Double,
        val weeklyPercentage: Double,
        val monthlyPercentage: Double,
        val currentHourlyPace: Double,
        val requiredHourlyPace: Double,
        val coachingMessage: String
    )

    fun calculateProgress(
        goals: MultiPeriodGoals,
        dailyCurrent: Double,
        weeklyCurrent: Double,
        monthlyCurrent: Double,
        hoursWorkedToday: Double,
        shiftPlannedHours: Double
    ): MultiPeriodProgress {
        val remDaily = (goals.dailyGrossTarget - dailyCurrent).coerceAtLeast(0.0)
        val remWeekly = (goals.weeklyGrossTarget - weeklyCurrent).coerceAtLeast(0.0)
        val remMonthly = (goals.monthlyGrossTarget - monthlyCurrent).coerceAtLeast(0.0)

        val pctDaily = if (goals.dailyGrossTarget > 0) ((dailyCurrent / goals.dailyGrossTarget) * 100.0).coerceIn(0.0, 100.0) else 0.0
        val pctWeekly = if (goals.weeklyGrossTarget > 0) ((weeklyCurrent / goals.weeklyGrossTarget) * 100.0).coerceIn(0.0, 100.0) else 0.0
        val pctMonthly = if (goals.monthlyGrossTarget > 0) ((monthlyCurrent / goals.monthlyGrossTarget) * 100.0).coerceIn(0.0, 100.0) else 0.0

        val currentPace = if (hoursWorkedToday > 0.0) dailyCurrent / hoursWorkedToday else 0.0
        val remainingHours = (shiftPlannedHours - hoursWorkedToday).coerceAtLeast(0.0)
        val reqHourlyPace = if (remainingHours > 0.0) remDaily / remainingHours else 0.0

        val coaching = when {
            remDaily <= 0.0 -> "Meta diária concluída! Foco agora na meta semanal (faltam R$ %.2f).".format(Locale("pt", "BR"), remWeekly)
            hoursWorkedToday > 0.0 && (dailyCurrent + (currentPace * remainingHours)) >= goals.dailyGrossTarget -> {
                "Seu ritmo atual (R$ %.2f/h) indica que você baterá a meta hoje!".format(Locale("pt", "BR"), currentPace)
            }
            remainingHours > 0.0 -> {
                "Para atingir sua meta, você precisa fazer aproximadamente R$ %.2f/h nas próximas %.1fh.".format(Locale("pt", "BR"), reqHourlyPace, remainingHours)
            }
            else -> "Você precisa faturar mais R$ %.2f hoje.".format(Locale("pt", "BR"), remDaily)
        }

        return MultiPeriodProgress(
            dailyCurrent = dailyCurrent,
            weeklyCurrent = weeklyCurrent,
            monthlyCurrent = monthlyCurrent,
            hoursWorkedToday = hoursWorkedToday,
            remainingDaily = remDaily,
            remainingWeekly = remWeekly,
            remainingMonthly = remMonthly,
            dailyPercentage = pctDaily,
            weeklyPercentage = pctWeekly,
            monthlyPercentage = pctMonthly,
            currentHourlyPace = currentPace,
            requiredHourlyPace = reqHourlyPace,
            coachingMessage = coaching
        )
    }
}
