package com.rotai.iq.feature.geographic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rotai.iq.core.data.repository.RotaIqRepository
import com.rotai.iq.core.domain.engine.DeadheadPredictorEngine
import com.rotai.iq.core.domain.engine.GeoHeatmapEngine
import com.rotai.iq.core.domain.engine.PlatformComparisonEngine
import com.rotai.iq.core.domain.model.DeadheadAnalysis
import com.rotai.iq.core.domain.model.DemandLevel
import com.rotai.iq.core.domain.model.GeoZone
import com.rotai.iq.core.domain.model.PlatformComparisonReport
import com.rotai.iq.core.domain.model.RideEvaluation
import com.rotai.iq.core.domain.model.RideOffer
import com.rotai.iq.core.domain.model.RidePlatform
import com.rotai.iq.core.domain.model.TimeSlot
import com.rotai.iq.core.domain.model.Vehicle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ZoneWithCurrentDemand(
    val zone: GeoZone,
    val currentDemand: DemandLevel
)

data class GeoInsightsUiState(
    val timeSlot: TimeSlot = GeoHeatmapEngine.getCurrentTimeSlot(),
    val zones: List<ZoneWithCurrentDemand> = emptyList(),
    val platformReport: PlatformComparisonReport = PlatformComparisonReport(emptyList(), null, null, ""),
    val selectedZone: GeoZone = GeoHeatmapEngine.getDefaultZones().first(),
    val simFare: String = "48.00",
    val simDistanceKm: String = "22.0",
    val simPickupKm: String = "2.0",
    val deadheadAnalysis: DeadheadAnalysis? = null,
    val vehicle: Vehicle = Vehicle(),
    val currentTab: Int = 0
)

class GeoInsightsViewModel(
    private val repository: RotaIqRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GeoInsightsUiState())
    val uiState: StateFlow<GeoInsightsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val defaultZones = GeoHeatmapEngine.getDefaultZones()
            val timeSlot = GeoHeatmapEngine.getCurrentTimeSlot()
            val zonesWithDemand = defaultZones.map { zone ->
                ZoneWithCurrentDemand(
                    zone = zone,
                    currentDemand = GeoHeatmapEngine.evaluateZoneDemandForTimeSlot(zone, timeSlot)
                )
            }

            repository.getActiveVehicle().collect { vehicle ->
                val evaluations = repository.getAllEvaluations().first()
                val report = PlatformComparisonEngine.comparePlatforms(evaluations)

                _uiState.value = _uiState.value.copy(
                    timeSlot = timeSlot,
                    zones = zonesWithDemand,
                    platformReport = report,
                    selectedZone = defaultZones[4], // Condomínios / Granja Viana por padrão para ilustrar deadhead
                    vehicle = vehicle
                )
                recalculateDeadhead()
            }
        }

        viewModelScope.launch {
            repository.getAllEvaluations().collect { evals ->
                val report = PlatformComparisonEngine.comparePlatforms(evals)
                _uiState.value = _uiState.value.copy(platformReport = report)
            }
        }
    }

    fun setTab(index: Int) {
        _uiState.value = _uiState.value.copy(currentTab = index)
    }

    fun selectZone(zone: GeoZone) {
        _uiState.value = _uiState.value.copy(selectedZone = zone)
        recalculateDeadhead()
    }

    fun updateInputs(fare: String, distance: String, pickup: String) {
        _uiState.value = _uiState.value.copy(
            simFare = fare,
            simDistanceKm = distance,
            simPickupKm = pickup
        )
        recalculateDeadhead()
    }

    private fun recalculateDeadhead() {
        val state = _uiState.value
        val fare = state.simFare.toDoubleOrNull() ?: 48.0
        val dist = state.simDistanceKm.toDoubleOrNull() ?: 22.0
        val pickup = state.simPickupKm.toDoubleOrNull() ?: 2.0

        val offer = RideOffer(
            platform = RidePlatform.UBER,
            grossFare = fare,
            distanceKm = dist,
            durationMinutes = dist * 2.2, // ~2.2 min/km
            pickupDistanceKm = pickup,
            pickupDurationMinutes = pickup * 3.0
        )

        val analysis = DeadheadPredictorEngine.analyzeDeadheadRisk(
            offer = offer,
            destinationZone = state.selectedZone,
            vehicle = state.vehicle
        )

        _uiState.value = _uiState.value.copy(deadheadAnalysis = analysis)
    }
}
