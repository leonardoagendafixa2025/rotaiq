package com.rotai.iq.feature.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rotai.iq.core.data.repository.RotaIqRepository
import com.rotai.iq.core.domain.model.DriverPreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class RideFilterUiState(
    val preference: DriverPreference = DriverPreference(),
    val minRatePerKmInput: String = "2.50",
    val minRatePerHourInput: String = "45.0",
    val minGrossFareInput: String = "10.0",
    val maxPickupDistanceKmInput: String = "3.0",
    val maxPickupMinutesInput: String = "8.0",
    val allowIntermediateStops: Boolean = false,
    val minProfitMarginPercentInput: String = "50.0",
    val audioAlertsEnabled: Boolean = true,
    val overlayHudEnabled: Boolean = true,
    val isSavedSuccess: Boolean = false
)

class RideFilterViewModel(
    private val repository: RotaIqRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RideFilterUiState())
    val uiState: StateFlow<RideFilterUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            repository.getDriverPreference().collect { pref ->
                _uiState.value = _uiState.value.copy(
                    preference = pref,
                    minRatePerKmInput = "%.2f".format(java.util.Locale.US, pref.minRatePerKm),
                    minRatePerHourInput = "%.1f".format(java.util.Locale.US, pref.minRatePerHour),
                    minGrossFareInput = "%.1f".format(java.util.Locale.US, pref.minGrossFare),
                    maxPickupDistanceKmInput = "%.1f".format(java.util.Locale.US, pref.maxPickupDistanceKm),
                    maxPickupMinutesInput = "%.1f".format(java.util.Locale.US, pref.maxPickupMinutes),
                    allowIntermediateStops = pref.allowIntermediateStops,
                    minProfitMarginPercentInput = "%.0f".format(java.util.Locale.US, pref.minProfitMarginPercent),
                    audioAlertsEnabled = pref.audioAlertsEnabled,
                    overlayHudEnabled = pref.overlayHudEnabled
                )
            }
        }
    }

    fun onMinRatePerKmChanged(value: String) {
        _uiState.value = _uiState.value.copy(minRatePerKmInput = value, isSavedSuccess = false)
    }

    fun onMinRatePerHourChanged(value: String) {
        _uiState.value = _uiState.value.copy(minRatePerHourInput = value, isSavedSuccess = false)
    }

    fun onMinGrossFareChanged(value: String) {
        _uiState.value = _uiState.value.copy(minGrossFareInput = value, isSavedSuccess = false)
    }

    fun onMaxPickupDistanceKmChanged(value: String) {
        _uiState.value = _uiState.value.copy(maxPickupDistanceKmInput = value, isSavedSuccess = false)
    }

    fun onMaxPickupMinutesChanged(value: String) {
        _uiState.value = _uiState.value.copy(maxPickupMinutesInput = value, isSavedSuccess = false)
    }

    fun onAllowIntermediateStopsChanged(value: Boolean) {
        _uiState.value = _uiState.value.copy(allowIntermediateStops = value, isSavedSuccess = false)
    }

    fun onMinProfitMarginPercentChanged(value: String) {
        _uiState.value = _uiState.value.copy(minProfitMarginPercentInput = value, isSavedSuccess = false)
    }

    fun onAudioAlertsChanged(value: Boolean) {
        _uiState.value = _uiState.value.copy(audioAlertsEnabled = value, isSavedSuccess = false)
    }

    fun onOverlayHudChanged(value: Boolean) {
        _uiState.value = _uiState.value.copy(overlayHudEnabled = value, isSavedSuccess = false)
    }

    fun saveFilters() {
        val s = _uiState.value
        val updated = DriverPreference(
            id = s.preference.id,
            minRatePerKm = s.minRatePerKmInput.replace(",", ".").toDoubleOrNull() ?: 2.20,
            minRatePerHour = s.minRatePerHourInput.replace(",", ".").toDoubleOrNull() ?: 40.0,
            minGrossFare = s.minGrossFareInput.replace(",", ".").toDoubleOrNull() ?: 10.0,
            maxPickupDistanceKm = s.maxPickupDistanceKmInput.replace(",", ".").toDoubleOrNull() ?: 3.5,
            maxPickupMinutes = s.maxPickupMinutesInput.replace(",", ".").toDoubleOrNull() ?: 10.0,
            maxStops = if (s.allowIntermediateStops) 2 else 0,
            allowIntermediateStops = s.allowIntermediateStops,
            minProfitMarginPercent = s.minProfitMarginPercentInput.replace(",", ".").toDoubleOrNull() ?: 50.0,
            preferShortTrips = s.preference.preferShortTrips,
            audioAlertsEnabled = s.audioAlertsEnabled,
            overlayHudEnabled = s.overlayHudEnabled
        )

        viewModelScope.launch {
            repository.saveDriverPreference(updated)
            _uiState.value = _uiState.value.copy(isSavedSuccess = true, preference = updated)
        }
    }
}
