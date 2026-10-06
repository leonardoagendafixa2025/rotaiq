package com.rotai.iq.core.domain.engine

import com.rotai.iq.core.domain.model.PlatformComparisonReport
import com.rotai.iq.core.domain.model.PlatformPerformanceSummary
import com.rotai.iq.core.domain.model.RideEvaluation
import com.rotai.iq.core.domain.model.RidePlatform
import java.util.Locale

object PlatformComparisonEngine {

    fun comparePlatforms(evaluations: List<RideEvaluation>): PlatformComparisonReport {
        if (evaluations.isEmpty()) {
            return PlatformComparisonReport(
                summaries = emptyList(),
                bestPlatformByHourlyRate = null,
                bestPlatformByNetMargin = null,
                recommendation = "Nenhuma corrida avaliada ainda para gerar comparativo entre plataformas."
            )
        }

        val grouped = evaluations.groupBy { it.offer.platform }
        val summaries = grouped.map { (platform, list) ->
            val count = list.size
            val totalGross = list.sumOf { it.grossFare }
            val totalNet = list.sumOf { it.netProfit }
            val totalKm = list.sumOf { it.totalDistanceKm }.coerceAtLeast(0.1)
            val totalHours = list.sumOf { it.totalDurationMinutes / 60.0 }.coerceAtLeast(0.01)

            val avgGrossFare = totalGross / count
            val avgNetProfit = totalNet / count
            val avgGrossKm = totalGross / totalKm
            val avgGrossHourly = totalGross / totalHours
            val avgNetHourly = totalNet / totalHours
            val margin = if (totalGross > 0.0) ((totalNet / totalGross) * 100.0) else 0.0

            PlatformPerformanceSummary(
                platform = platform,
                totalOffersEvaluated = count,
                totalOffersAccepted = count,
                acceptanceRatePercent = 100.0,
                totalGrossRevenue = totalGross,
                totalNetProfit = totalNet,
                averageGrossFare = avgGrossFare,
                averageNetProfit = avgNetProfit,
                averageGrossRatePerKm = avgGrossKm,
                averageGrossRatePerHour = avgGrossHourly,
                averageNetRatePerHour = avgNetHourly,
                profitMarginPercent = margin
            )
        }.sortedByDescending { it.averageNetRatePerHour }

        val bestByHourly = summaries.maxByOrNull { it.averageNetRatePerHour }?.platform
        val bestByMargin = summaries.maxByOrNull { it.profitMarginPercent }?.platform

        val recommendation = when {
            summaries.size >= 2 -> {
                val first = summaries[0]
                val second = summaries[1]
                val diffHourly = first.averageNetRatePerHour - second.averageNetRatePerHour
                if (diffHourly > 5.0) {
                    "A plataforma %s gerou R$ %.2f/h de lucro limpo, superando a %s em R$ %.2f/h. Priorize chamadas da %s nos momentos de maior demanda.".format(
                        Locale("pt", "BR"),
                        first.platform.displayName,
                        first.averageNetRatePerHour,
                        second.platform.displayName,
                        diffHourly,
                        first.platform.displayName
                    )
                } else {
                    "As plataformas %s e %s apresentam rentabilidade similar (diferença de R$ %.2f/h). Mantenha ambos os aplicativos ativos.".format(
                        Locale("pt", "BR"),
                        first.platform.displayName,
                        second.platform.displayName,
                        diffHourly
                    )
                }
            }
            summaries.size == 1 -> {
                "Você operou apenas com a plataforma %s. Registre ou ative ofertas de outros apps para comparar a rentabilidade.".format(
                    Locale("pt", "BR"),
                    summaries.first().platform.displayName
                )
            }
            else -> "Aguardando mais dados para cruzamento de métricas."
        }

        return PlatformComparisonReport(
            summaries = summaries,
            bestPlatformByHourlyRate = bestByHourly,
            bestPlatformByNetMargin = bestByMargin,
            recommendation = recommendation
        )
    }
}
