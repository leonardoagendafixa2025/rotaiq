package com.rotai.iq.feature.automation

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rotai.iq.RotaIqApplication
import com.rotai.iq.core.automation.accessibility.RotaIqAccessibilityService
import com.rotai.iq.core.automation.overlay.OverlayManager
import com.rotai.iq.core.domain.engine.RideEvaluationEngine
import com.rotai.iq.core.domain.model.DriverGoal
import com.rotai.iq.core.domain.model.DriverPreference
import com.rotai.iq.core.domain.model.RideCategory
import com.rotai.iq.core.domain.model.RideEvaluation
import com.rotai.iq.core.domain.model.RideOffer
import com.rotai.iq.core.domain.model.RidePlatform
import com.rotai.iq.core.domain.model.Vehicle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AutomationViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as RotaIqApplication
    private val voiceAlertManager = app.voiceAlertManager
    private val repository = app.repository
    private val evaluationEngine = RideEvaluationEngine()

    val isAccessibilityActive: StateFlow<Boolean> = RotaIqAccessibilityService.isServiceRunning
    val latestEvaluation: StateFlow<RideEvaluation?> = RotaIqAccessibilityService.latestEvaluation
    val evaluationsCount: StateFlow<Int> = RotaIqAccessibilityService.evaluationsCount

    private val _isOverlayPermissionGranted = MutableStateFlow(OverlayManager.hasOverlayPermission(application))
    val isOverlayPermissionGranted: StateFlow<Boolean> = _isOverlayPermissionGranted.asStateFlow()

    private val _isOverlayEnabled = MutableStateFlow(OverlayManager.isOverlayEnabled)
    val isOverlayEnabled: StateFlow<Boolean> = _isOverlayEnabled.asStateFlow()

    private val _isVoiceAlertEnabled = MutableStateFlow(voiceAlertManager.isEnabled)
    val isVoiceAlertEnabled: StateFlow<Boolean> = _isVoiceAlertEnabled.asStateFlow()

    fun refreshPermissions(context: Context) {
        _isOverlayPermissionGranted.value = OverlayManager.hasOverlayPermission(context)
    }

    fun setOverlayEnabled(enabled: Boolean) {
        OverlayManager.isOverlayEnabled = enabled
        _isOverlayEnabled.value = enabled
        if (!enabled) {
            OverlayManager.dismiss()
        }
    }

    fun setVoiceAlertEnabled(enabled: Boolean) {
        voiceAlertManager.isEnabled = enabled
        _isVoiceAlertEnabled.value = enabled
    }

    fun testVoiceAlert() {
        voiceAlertManager.speak("Atenção motorista: copiloto vocal do ROTA IQ ativo e operacional.")
    }

    fun dismissFloatingHud() {
        OverlayManager.dismiss()
    }

    fun simulateLiveOffer(isProfitable: Boolean) {
        viewModelScope.launch {
            val vehicle = repository.getActiveVehicle().first()
            val preferences = repository.getDriverPreference().first()
            val goal = repository.getDriverGoal().first()

            val offer = if (isProfitable) {
                RideOffer(
                    platform = RidePlatform.UBER,
                    grossFare = 34.50,
                    distanceKm = 8.5,
                    durationMinutes = 22.0,
                    pickupDistanceKm = 1.2,
                    pickupDurationMinutes = 4.0,
                    stopsCount = 0,
                    category = RideCategory.UBER_X,
                    rawText = "UberX R$ 34,50\nEmbarque a 1,2 km (4 min)\nViagem 8,5 km (22 min)"
                )
            } else {
                RideOffer(
                    platform = RidePlatform.NINETY_NINE,
                    grossFare = 14.20,
                    distanceKm = 21.0,
                    durationMinutes = 48.0,
                    pickupDistanceKm = 5.5,
                    pickupDurationMinutes = 15.0,
                    stopsCount = 2,
                    category = RideCategory.POP_99,
                    rawText = "99Pop R$ 14,20\nBuscar a 5,5 km\nDestino 21 km • 2 paradas"
                )
            }

            val evaluation = evaluationEngine.evaluate(
                offer = offer,
                vehicle = vehicle,
                preferences = preferences,
                goal = goal
            )

            // Salva no repositório local
            repository.saveEvaluation(evaluation)

            // Dispara Overlay e Voz
            val context = getApplication<Application>()
            OverlayManager.showFloatingHud(context, evaluation)
            voiceAlertManager.speakEvaluation(evaluation)
        }
    }
}
