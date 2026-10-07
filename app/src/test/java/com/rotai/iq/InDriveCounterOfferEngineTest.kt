package com.rotai.iq

import com.rotai.iq.core.domain.engine.CounterOfferAction
import com.rotai.iq.core.domain.engine.InDriveCounterOfferEngine
import com.rotai.iq.core.domain.model.DriverGoal
import com.rotai.iq.core.domain.model.RideOffer
import com.rotai.iq.core.domain.model.RidePlatform
import com.rotai.iq.core.domain.model.Vehicle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InDriveCounterOfferEngineTest {

    private lateinit var engine: InDriveCounterOfferEngine
    private lateinit var vehicle: Vehicle
    private lateinit var goal: DriverGoal

    @Before
    fun setUp() {
        vehicle = Vehicle(
            consumptionKmPerLiter = 10.0,
            fuelPricePerLiter = 5.0, // R$ 0.50/km
            maintenanceCostPerKm = 0.20,
            estimatedMonthlyKm = 2000.0,
            monthlyInsuranceCost = 200.0,
            annualTaxesCost = 1200.0,
            monthlyDepreciation = 200.0,
            monthlyOtherCosts = 100.0 // Fixo: 600 / 2000 = R$ 0.30/km -> Total R$ 1.00/km
        )
        goal = DriverGoal(
            dailyGrossTarget = 300.0,
            targetHourlyRate = 45.0,
            targetKmRate = 2.20
        )
        engine = InDriveCounterOfferEngine(vehicle, goal)
    }

    @Test
    fun evaluateCounterOffer_recommendsImmediateAcceptanceWhenHighlyProfitable() {
        val highOffer = RideOffer(
            platform = RidePlatform.INDRAVE,
            grossFare = 45.0,
            distanceKm = 8.0,
            durationMinutes = 20.0, // 0.33 h -> R$ 135/h bruto, custo R$ 8.00
            pickupDistanceKm = 0.0,
            pickupDurationMinutes = 0.0
        )

        val eval = engine.evaluateCounterOffer(highOffer, vehicle, goal)
        assertEquals(CounterOfferAction.ACCEPT_IMMEDIATELY, eval.recommendedAction)
        assertEquals(45.0, eval.suggestedCounterOfferFare, 0.01)
        assertEquals(0.0, eval.counterOfferDelta, 0.01)
        assertTrue(eval.passengerHourlyRate >= 45.0)
    }

    @Test
    fun evaluateCounterOffer_recommendsCounterOfferWhenPassengerFareIsLow() {
        val lowOffer = RideOffer(
            platform = RidePlatform.INDRAVE,
            grossFare = 16.0,
            distanceKm = 10.0, // Custo R$ 10.00
            durationMinutes = 30.0, // 0.5 h -> Lucro R$ 6.00 em 0.5h = R$ 12/h (abaixo dos R$ 45/h)
            pickupDistanceKm = 0.0,
            pickupDurationMinutes = 0.0
        )

        val eval = engine.evaluateCounterOffer(lowOffer, vehicle, goal)
        assertEquals(CounterOfferAction.COUNTER_OFFER, eval.recommendedAction)
        assertTrue(eval.suggestedCounterOfferFare > lowOffer.grossFare)
        assertTrue(eval.counterOfferDelta > 0.0)
        // Contraproposta eleva o retorno por hora significativamente
        assertTrue(eval.counterOfferHourlyRate > eval.passengerHourlyRate)
    }

    @Test
    fun evaluateCounterOffer_generatesValidSteps() {
        val offer = RideOffer(
            platform = RidePlatform.INDRAVE,
            grossFare = 20.0,
            distanceKm = 8.0,
            durationMinutes = 25.0
        )

        val eval = engine.evaluateCounterOffer(offer, vehicle, goal)
        assertEquals(5, eval.steps.size)
        // Degraus ordenados: +2, +4, +6, +8, +10
        assertEquals(2.0, eval.steps[0].deltaFare, 0.01)
        assertEquals(22.0, eval.steps[0].totalFare, 0.01)
        assertEquals(4.0, eval.steps[1].deltaFare, 0.01)
        assertEquals(24.0, eval.steps[1].totalFare, 0.01)
    }
}
