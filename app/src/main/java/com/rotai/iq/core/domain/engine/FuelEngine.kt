package com.rotai.iq.core.domain.engine

import com.rotai.iq.core.domain.model.FuelRecord

object FuelEngine {

    data class FuelConsumptionResult(
        val distanceTraveledKm: Double,
        val litersConsumed: Double,
        val consumptionKmPerLiter: Double,
        val realCostPerKm: Double,
        val isConsistent: Boolean // Verifica se o valor está em faixa realista (4 a 28 km/L)
    )

    /**
     * Calcula o consumo real e custo por km através do método do tanque cheio
     * utilizando dois abastecimentos consecutivos com tanque cheio.
     */
    fun calculateConsumptionBetweenFills(
        previousFullTank: FuelRecord,
        currentFullTank: FuelRecord
    ): FuelConsumptionResult? {
        if (!previousFullTank.isFullTank || !currentFullTank.isFullTank) {
            return null
        }
        val distance = currentFullTank.odometerKm - previousFullTank.odometerKm
        if (distance <= 0.0 || currentFullTank.liters <= 0.0) {
            return null
        }

        val kmPerLiter = distance / currentFullTank.liters
        val costPerKm = currentFullTank.totalPaid / distance
        val isConsistent = kmPerLiter in 4.0..28.0

        return FuelConsumptionResult(
            distanceTraveledKm = distance,
            litersConsumed = currentFullTank.liters,
            consumptionKmPerLiter = kmPerLiter,
            realCostPerKm = costPerKm,
            isConsistent = isConsistent
        )
    }

    /**
     * Calcula a média de consumo a partir de uma lista histórica ordenada cronologicamente.
     */
    fun calculateAverageConsumption(records: List<FuelRecord>): Double? {
        val fullTankRecords = records.filter { it.isFullTank }.sortedBy { it.odometerKm }
        if (fullTankRecords.size < 2) return null

        var totalDistance = 0.0
        var totalLiters = 0.0

        for (i in 1 until fullTankRecords.size) {
            val prev = fullTankRecords[i - 1]
            val curr = fullTankRecords[i]
            val dist = curr.odometerKm - prev.odometerKm
            if (dist > 0.0 && curr.liters > 0.0) {
                totalDistance += dist
                totalLiters += curr.liters
            }
        }

        return if (totalLiters > 0.0) totalDistance / totalLiters else null
    }
}
