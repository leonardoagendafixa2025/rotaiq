package com.rotai.iq

import com.google.common.truth.Truth.assertThat
import com.rotai.iq.core.domain.engine.DeadheadPredictorEngine
import com.rotai.iq.core.domain.model.DemandLevel
import com.rotai.iq.core.domain.model.GeoZone
import com.rotai.iq.core.domain.model.RideOffer
import com.rotai.iq.core.domain.model.RidePlatform
import com.rotai.iq.core.domain.model.Vehicle
import org.junit.Test

class DeadheadPredictorEngineTest {

    private val vehicle = Vehicle(
        consumptionKmPerLiter = 10.0,
        fuelPricePerLiter = 5.00,
        maintenanceCostPerKm = 0.20,
        estimatedMonthlyKm = 3000.0,
        monthlyInsuranceCost = 300.0,
        annualTaxesCost = 1200.0,
        monthlyDepreciation = 300.0,
        monthlyOtherCosts = 100.0
    ) // Custo por km ~ R$ 1.00/km

    @Test
    fun analyzeDeadheadRisk_highReturnZone_doesNotTriggerDeadheadTrap() {
        val highReturnZone = GeoZone(
            name = "Centro Paulista",
            baseDemandLevel = DemandLevel.VERY_HIGH,
            returnTripProbability = 0.95,
            deadheadKmToCenter = 0.0
        )

        val offer = RideOffer(
            platform = RidePlatform.UBER,
            grossFare = 40.0,
            distanceKm = 10.0,
            durationMinutes = 25.0,
            pickupDistanceKm = 1.0
        )

        val analysis = DeadheadPredictorEngine.analyzeDeadheadRisk(offer, highReturnZone, vehicle)

        assertThat(analysis.isDeadheadTrap).isFalse()
        assertThat(analysis.expectedEmptyReturnKm).isWithin(0.01).of(0.0)
        assertThat(analysis.emptyReturnCost).isWithin(0.01).of(0.0)
        assertThat(analysis.adjustedNetProfit).isGreaterThan(20.0)
        assertThat(analysis.alertMessage).isNull()
        assertThat(analysis.coachingRecommendation).contains("Alta probabilidade de viagem de retorno")
    }

    @Test
    fun analyzeDeadheadRisk_remoteSuburbanZone_triggersDeadheadTrapWithAlert() {
        val remoteZone = GeoZone(
            name = "Condomínio Afastado",
            baseDemandLevel = DemandLevel.DEAD_ZONE,
            returnTripProbability = 0.20, // 80% de chance de voltar vazio!
            deadheadKmToCenter = 25.0
        )

        val offer = RideOffer(
            platform = RidePlatform.NINETY_NINE,
            grossFare = 45.0,
            distanceKm = 20.0,
            durationMinutes = 40.0,
            pickupDistanceKm = 2.0
        )

        val analysis = DeadheadPredictorEngine.analyzeDeadheadRisk(offer, remoteZone, vehicle)

        assertThat(analysis.isDeadheadTrap).isTrue()
        // 25km * (1 - 0.20) = 20km vazios esperados
        assertThat(analysis.expectedEmptyReturnKm).isWithin(0.01).of(20.0)
        assertThat(analysis.emptyReturnCost).isGreaterThan(15.0)
        assertThat(analysis.alertMessage).isNotEmpty()
        assertThat(analysis.alertMessage).contains("Volta Vazia")
        assertThat(analysis.coachingRecommendation).contains("retorno vazio")
    }

    @Test
    fun analyzeDeadheadRisk_avoidZone_alwaysFlagsTrap() {
        val avoidZone = GeoZone(
            name = "Área de Risco",
            baseDemandLevel = DemandLevel.HIGH_RISK,
            returnTripProbability = 0.50,
            deadheadKmToCenter = 10.0,
            isAvoidZone = true
        )

        val offer = RideOffer(
            platform = RidePlatform.UBER,
            grossFare = 30.0,
            distanceKm = 8.0,
            durationMinutes = 20.0
        )

        val analysis = DeadheadPredictorEngine.analyzeDeadheadRisk(offer, avoidZone, vehicle)

        assertThat(analysis.isDeadheadTrap).isTrue()
        assertThat(analysis.alertMessage).contains("risco")
    }
}
