package com.rotai.iq.core.domain.engine

import com.rotai.iq.core.domain.model.Vehicle
import com.rotai.iq.core.domain.model.VehicleCostBreakdown

object VehicleCostEngine {

    /**
     * Calcula o custo detalhado para uma determinada quilometragem percorrida.
     */
    fun calculateTripCost(vehicle: Vehicle, distanceKm: Double): VehicleCostBreakdown {
        return vehicle.calculateTripCost(distanceKm)
    }

    /**
     * Calcula o custo por quilômetro consolidado do veículo.
     */
    fun calculateCostPerKm(vehicle: Vehicle): Double {
        return vehicle.totalCostPerKm
    }

    /**
     * Projeções de custo temporal para gestão preventiva do motorista.
     */
    data class CostProjections(
        val costPerKm: Double,
        val costPerHourEstimated: Double,  // assumindo velocidade média urbana de 25 km/h
        val dailyFixedCost: Double,
        val monthlyFixedCost: Double,
        val annualEstimatedCost: Double
    )

    fun calculateProjections(vehicle: Vehicle, avgKmPerHour: Double = 25.0): CostProjections {
        val costPerKm = vehicle.totalCostPerKm
        val costPerHour = costPerKm * avgKmPerHour
        val monthlyFixed = vehicle.totalMonthlyFixedCosts
        val dailyFixed = monthlyFixed / 30.0
        val annualFixed = monthlyFixed * 12.0
        val annualVariable = costPerKm * (vehicle.estimatedMonthlyKm * 12.0)
        val annualTotal = annualFixed + annualVariable

        return CostProjections(
            costPerKm = costPerKm,
            costPerHourEstimated = costPerHour,
            dailyFixedCost = dailyFixed,
            monthlyFixedCost = monthlyFixed,
            annualEstimatedCost = annualTotal
        )
    }
}
