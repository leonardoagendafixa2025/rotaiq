package com.rotai.iq.feature.rides

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rotai.iq.core.data.repository.RotaIqRepository
import com.rotai.iq.core.domain.engine.RideEvaluationEngine
import com.rotai.iq.core.domain.model.DriverGoal
import com.rotai.iq.core.domain.model.DriverPreference
import com.rotai.iq.core.domain.model.RideEvaluation
import com.rotai.iq.core.domain.model.RideOffer
import com.rotai.iq.core.domain.model.RidePlatform
import com.rotai.iq.core.domain.model.Vehicle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class RideSimulatorUiState(
    val selectedPlatform: RidePlatform = RidePlatform.UBER,
    val grossFareInput: String = "32.80",
    val distanceKmInput: String = "9.4",
    val durationMinutesInput: String = "26",
    val pickupDistanceKmInput: String = "1.2",
    val pickupDurationMinutesInput: String = "4",
    val stopsCountInput: String = "0",
    val currentEvaluation: RideEvaluation? = null,
    val isSaved: Boolean = false,
    val vehicle: Vehicle = Vehicle(),
    val goal: DriverGoal = DriverGoal(),
    val preferences: DriverPreference = DriverPreference()
)

class RideSimulatorViewModel(
    private val repository: RotaIqRepository
) : ViewModel() {

    private val evaluationEngine = RideEvaluationEngine()
    private val _uiState = MutableStateFlow(RideSimulatorUiState())
    val uiState: StateFlow<RideSimulatorUiState> = _uiState.asStateFlow()

    init {
        loadDependencies()
    }

    private fun loadDependencies() {
        viewModelScope.launch {
            val vehicle = repository.getActiveVehicle().first()
            val goal = repository.getDriverGoal().first()
            val preferences = repository.getDriverPreference().first()

            _uiState.value = _uiState.value.copy(
                vehicle = vehicle,
                goal = goal,
                preferences = preferences
            )
            // Avaliação inicial padrão
            evaluateCurrentInput()
        }
    }

    fun onPlatformChanged(platform: RidePlatform) {
        _uiState.value = _uiState.value.copy(selectedPlatform = platform)
    }

    fun onGrossFareChanged(value: String) {
        _uiState.value = _uiState.value.copy(grossFareInput = value)
    }

    fun onDistanceKmChanged(value: String) {
        _uiState.value = _uiState.value.copy(distanceKmInput = value)
    }

    fun onDurationMinutesChanged(value: String) {
        _uiState.value = _uiState.value.copy(durationMinutesInput = value)
    }

    fun onPickupDistanceChanged(value: String) {
        _uiState.value = _uiState.value.copy(pickupDistanceKmInput = value)
    }

    fun onPickupDurationChanged(value: String) {
        _uiState.value = _uiState.value.copy(pickupDurationMinutesInput = value)
    }

    fun onStopsCountChanged(value: String) {
        _uiState.value = _uiState.value.copy(stopsCountInput = value)
    }

    fun loadPreset(
        platform: RidePlatform,
        grossFare: Double,
        distanceKm: Double,
        durationMinutes: Double,
        pickupDistKm: Double,
        pickupDurMin: Double,
        stops: Int
    ) {
        _uiState.value = _uiState.value.copy(
            selectedPlatform = platform,
            grossFareInput = grossFare.toString(),
            distanceKmInput = distanceKm.toString(),
            durationMinutesInput = durationMinutes.toString(),
            pickupDistanceKmInput = pickupDistKm.toString(),
            pickupDurationMinutesInput = pickupDurMin.toString(),
            stopsCountInput = stops.toString()
        )
        evaluateCurrentInput()
    }

    fun evaluateCurrentInput() {
        val state = _uiState.value
        val fare = state.grossFareInput.replace(",", ".").toDoubleOrNull() ?: 0.0
        val dist = state.distanceKmInput.replace(",", ".").toDoubleOrNull() ?: 0.0
        val dur = state.durationMinutesInput.replace(",", ".").toDoubleOrNull() ?: 0.0
        val pickupDist = state.pickupDistanceKmInput.replace(",", ".").toDoubleOrNull() ?: 0.0
        val pickupDur = state.pickupDurationMinutesInput.replace(",", ".").toDoubleOrNull() ?: 0.0
        val stops = state.stopsCountInput.toIntOrNull() ?: 0

        val offer = RideOffer(
            platform = state.selectedPlatform,
            grossFare = fare,
            distanceKm = dist,
            durationMinutes = dur,
            pickupDistanceKm = pickupDist,
            pickupDurationMinutes = pickupDur,
            stopsCount = stops
        )

        val evaluation = evaluationEngine.evaluate(
            offer = offer,
            vehicle = state.vehicle,
            preferences = state.preferences,
            goal = state.goal
        )

        _uiState.value = _uiState.value.copy(
            currentEvaluation = evaluation,
            isSaved = false
        )
    }

    fun saveEvaluationToHistory() {
        val evaluation = _uiState.value.currentEvaluation ?: return
        viewModelScope.launch {
            repository.saveEvaluation(evaluation)
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }
}
