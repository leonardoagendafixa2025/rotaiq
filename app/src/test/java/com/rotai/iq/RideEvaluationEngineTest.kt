package com.rotai.iq

import com.google.common.truth.Truth.assertThat
import com.rotai.iq.core.domain.engine.RideEvaluationEngine
import com.rotai.iq.core.domain.model.DriverGoal
import com.rotai.iq.core.domain.model.DriverPreference
import com.rotai.iq.core.domain.model.DriverZone
import com.rotai.iq.core.domain.model.EvaluationClassification
import com.rotai.iq.core.domain.model.RideCategory
import com.rotai.iq.core.domain.model.RideOffer
import com.rotai.iq.core.domain.model.RidePlatform
import com.rotai.iq.core.domain.model.Vehicle
import org.junit.Before
import org.junit.Test

class RideEvaluationEngineTest {

    private lateinit var engine: RideEvaluationEngine
    private lateinit var defaultVehicle: Vehicle
    private lateinit var defaultGoal: DriverGoal
    private lateinit var defaultPreferences: DriverPreference

    @Before
    fun setUp() {
        engine = RideEvaluationEngine()
        // Custo total per km = 5.80/11 (~0.527) + 0.18 + (870/3000 = 0.29) = ~0.997 R$/km
        defaultVehicle = Vehicle(
            consumptionKmPerLiter = 11.0,
            fuelPricePerLiter = 5.80,
            maintenanceCostPerKm = 0.18,
            monthlyInsuranceCost = 220.0,
            annualTaxesCost = 1800.0,
            monthlyDepreciation = 350.0,
            monthlyOtherCosts = 150.0,
            estimatedMonthlyKm = 3000.0
        )
        defaultGoal = DriverGoal(
            dailyGrossTarget = 300.0,
            targetHourlyRate = 45.0,
            targetKmRate = 2.40
        )
        defaultPreferences = DriverPreference(
            minRatePerKm = 2.20,
            minRatePerHour = 40.0,
            maxPickupDistanceKm = 3.5,
            maxStops = 1
        )
    }

    @Test
    fun evaluate_idealRide_returnsExcellentScore() {
        // Exemplo da especificação: R$ 32,80, 9,4 km, 26 min, pickup 1 km / 3 min
        val offer = RideOffer(
            platform = RidePlatform.UBER,
            grossFare = 32.80,
            distanceKm = 9.4,
            durationMinutes = 26.0,
            pickupDistanceKm = 1.0,
            pickupDurationMinutes = 3.0,
            stopsCount = 0
        )

        val eval = engine.evaluate(
            offer = offer,
            vehicle = defaultVehicle,
            preferences = defaultPreferences,
            goal = defaultGoal
        )

        assertThat(eval.score).isAtLeast(80)
        assertThat(eval.classification).isEqualTo(EvaluationClassification.EXCELLENT)
        assertThat(eval.netProfit).isGreaterThan(20.0)
        assertThat(eval.reasons).isNotEmpty()
        assertThat(eval.alerts).isEmpty()
    }

    @Test
    fun evaluate_lowRateLongRide_returnsAvoidOrBad() {
        // R$ 20,00 por 25 km em 50 minutos -> R$ 0,80/km (menor que o custo do carro!)
        val offer = RideOffer(
            platform = RidePlatform.UBER,
            grossFare = 20.00,
            distanceKm = 25.0,
            durationMinutes = 50.0,
            pickupDistanceKm = 2.0,
            pickupDurationMinutes = 5.0
        )

        val eval = engine.evaluate(
            offer = offer,
            vehicle = defaultVehicle,
            preferences = defaultPreferences,
            goal = defaultGoal
        )

        assertThat(eval.score).isLessThan(45)
        assertThat(eval.classification).isAnyOf(EvaluationClassification.BAD, EvaluationClassification.AVOID)
        assertThat(eval.alerts).isNotEmpty()
    }

    @Test
    fun evaluate_shortProfitableRide_returnsGoodOrExcellent() {
        // R$ 16,00 por 2,5 km em 8 minutos, embarque a 500m -> R$ 5,33/km, R$ 96/h!
        val offer = RideOffer(
            platform = RidePlatform.NINETY_NINE,
            grossFare = 16.00,
            distanceKm = 2.5,
            durationMinutes = 8.0,
            pickupDistanceKm = 0.5,
            pickupDurationMinutes = 2.0
        )

        val eval = engine.evaluate(
            offer = offer,
            vehicle = defaultVehicle,
            preferences = defaultPreferences,
            goal = defaultGoal
        )

        assertThat(eval.score).isAtLeast(75)
        assertThat(eval.grossRatePerHour).isGreaterThan(90.0)
    }

    @Test
    fun evaluate_farPickupDeadhead_penalizesScoreWithAlert() {
        // Corrida de R$ 14,00, viagem de 3 km, mas passageiro a 6 km (longe!)
        val offer = RideOffer(
            platform = RidePlatform.UBER,
            grossFare = 14.00,
            distanceKm = 3.0,
            durationMinutes = 10.0,
            pickupDistanceKm = 6.0, // bem acima dos 3.5km preferidos
            pickupDurationMinutes = 15.0
        )

        val eval = engine.evaluate(
            offer = offer,
            vehicle = defaultVehicle,
            preferences = defaultPreferences,
            goal = defaultGoal
        )

        assertThat(eval.alerts.any { it.contains("Passageiro distante") }).isTrue()
        assertThat(eval.score).isLessThan(60)
    }

    @Test
    fun evaluate_multipleStops_appliesStopPenalties() {
        val noStopsOffer = RideOffer(
            grossFare = 30.00,
            distanceKm = 10.0,
            durationMinutes = 25.0,
            stopsCount = 0
        )
        val threeStopsOffer = RideOffer(
            grossFare = 30.00,
            distanceKm = 10.0,
            durationMinutes = 25.0,
            stopsCount = 3
        )

        val evalNoStops = engine.evaluate(noStopsOffer, defaultVehicle, defaultPreferences, defaultGoal)
        val evalStops = engine.evaluate(threeStopsOffer, defaultVehicle, defaultPreferences, defaultGoal)

        assertThat(evalStops.score).isLessThan(evalNoStops.score)
        assertThat(evalStops.alerts.any { it.contains("parada") }).isTrue()
    }

    @Test
    fun evaluate_highGrossValueLowHourly_detectsTrafficTrap() {
        // R$ 70,00 parece muito dinheiro, mas leva 2h30 em engarrafamento (150 min, 30 km) -> R$ 28/h
        val offer = RideOffer(
            grossFare = 70.00,
            distanceKm = 30.0,
            durationMinutes = 150.0,
            pickupDistanceKm = 2.0,
            pickupDurationMinutes = 5.0
        )

        val eval = engine.evaluate(offer, defaultVehicle, defaultPreferences, defaultGoal)
        assertThat(eval.grossRatePerHour).isLessThan(30.0)
        assertThat(eval.alerts.any { it.contains("Retorno por hora") }).isTrue()
        assertThat(eval.score).isLessThan(55)
    }

    @Test
    fun evaluate_avoidZoneDestination_penalizesAndAlerts() {
        val offer = RideOffer(
            grossFare = 35.00,
            distanceKm = 10.0,
            durationMinutes = 25.0
        )
        val dangerousZone = DriverZone(
            id = "zone_1",
            name = "Área de Risco / Sem Retorno",
            isAvoidZone = true
        )

        val normalEval = engine.evaluate(offer, defaultVehicle, defaultPreferences, defaultGoal)
        val avoidEval = engine.evaluate(offer, defaultVehicle, defaultPreferences, defaultGoal, destinationZone = dangerousZone)

        assertThat(avoidEval.score).isLessThan(normalEval.score)
        assertThat(avoidEval.alerts.any { it.contains("região evitada") }).isTrue()
    }

    @Test
    fun evaluate_highCostVehicleVsLowCostVehicle_producesDifferentProfitAndScore() {
        val highCostVehicle = Vehicle(
            fuelPricePerLiter = 6.50,
            consumptionKmPerLiter = 6.5,  // gasta 1,00 R$/km só de gasolina
            maintenanceCostPerKm = 0.35,
            monthlyInsuranceCost = 400.0,
            annualTaxesCost = 2500.0,
            monthlyDepreciation = 500.0,
            monthlyOtherCosts = 200.0,
            estimatedMonthlyKm = 2000.0
        )
        val lowCostVehicle = Vehicle(
            fuelPricePerLiter = 4.00,
            consumptionKmPerLiter = 15.0, // GNV / Econômico
            maintenanceCostPerKm = 0.12,
            monthlyInsuranceCost = 150.0,
            annualTaxesCost = 800.0,
            monthlyDepreciation = 200.0,
            monthlyOtherCosts = 50.0,
            estimatedMonthlyKm = 3500.0
        )

        val offer = RideOffer(
            grossFare = 28.00,
            distanceKm = 12.0,
            durationMinutes = 25.0,
            pickupDistanceKm = 1.0,
            pickupDurationMinutes = 3.0
        )

        val evalHigh = engine.evaluate(offer, highCostVehicle, defaultPreferences, defaultGoal)
        val evalLow = engine.evaluate(offer, lowCostVehicle, defaultPreferences, defaultGoal)

        assertThat(evalLow.netProfit).isGreaterThan(evalHigh.netProfit)
        assertThat(evalLow.score).isGreaterThan(evalHigh.score)
    }
}
