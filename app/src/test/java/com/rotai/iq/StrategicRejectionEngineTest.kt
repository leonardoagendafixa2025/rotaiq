package com.rotai.iq

import com.rotai.iq.core.domain.engine.StrategicRejectionEngine
import com.rotai.iq.core.domain.model.DriverGoal
import com.rotai.iq.core.domain.model.RideOffer
import com.rotai.iq.core.domain.model.RidePlatform
import com.rotai.iq.core.domain.model.Vehicle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StrategicRejectionEngineTest {

    private lateinit var engine: StrategicRejectionEngine
    private lateinit var vehicle: Vehicle
    private lateinit var goal: DriverGoal

    @Before
    fun setUp() {
        vehicle = Vehicle(
            consumptionKmPerLiter = 10.0,
            fuelPricePerLiter = 5.0,
            maintenanceCostPerKm = 0.20,
            estimatedMonthlyKm = 2000.0,
            monthlyInsuranceCost = 200.0,
            annualTaxesCost = 1200.0,
            monthlyDepreciation = 200.0,
            monthlyOtherCosts = 100.0
            // Custo total: R$ 1.00/km
        )
        goal = DriverGoal(targetHourlyRate = 45.0)
        engine = StrategicRejectionEngine(vehicle, goal)
    }

    @Test
    fun analyzeRejection_identifiesDirectLossRide() {
        val deficitRide = RideOffer(
            platform = RidePlatform.UBER,
            grossFare = 10.0,
            distanceKm = 15.0, // Custo = R$ 15.00 -> Prejuízo de R$ -5.00
            durationMinutes = 35.0
        )

        val analysis = engine.analyzeRejection(deficitRide, vehicle, goal)
        assertTrue(analysis.isRejectionRecommended)
        assertTrue(analysis.badOfferNetProfit < 0.0)
        assertTrue(analysis.verdict.contains("PREJUÍZO"))
        assertEquals(15.0, analysis.avoidedOperatingCost, 0.01)
    }

    @Test
    fun analyzeRejection_calculatesBreakEvenWaitMinutesAccurately() {
        // Corrida de 40 min, paga R$ 5/h líquido (muito baixa, meta é R$ 50/h)
        val badRide = RideOffer(
            platform = RidePlatform.UBER,
            grossFare = 15.0,
            distanceKm = 10.0, // Custo R$ 10.00 -> Lucro R$ 5.00 em 40 min (0.666h) = R$ 7.50/h
            durationMinutes = 40.0
        )
        val customGoal = DriverGoal(targetHourlyRate = 50.0)

        val analysis = engine.analyzeRejection(badRide, vehicle, customGoal)
        assertTrue(analysis.isRejectionRecommended)
        // Ratio de eficiência: 7.50 / 50.0 = 0.15
        // Break-even wait time = 40 * (1 - 0.15) = 40 * 0.85 = 34 minutos!
        assertTrue(analysis.breakEvenWaitMinutes in 30.0..36.0)
    }

    @Test
    fun analyzeRejection_approvesProfitableRide() {
        val goodRide = RideOffer(
            platform = RidePlatform.UBER,
            grossFare = 40.0,
            distanceKm = 10.0, // Custo R$ 10.00 -> Lucro R$ 30.00 em 30 min = R$ 60.00/h
            durationMinutes = 30.0
        )

        val analysis = engine.analyzeRejection(goodRide, vehicle, goal)
        assertFalse(analysis.isRejectionRecommended)
        assertTrue(analysis.verdict.contains("VIÁVEL"))
    }
}
