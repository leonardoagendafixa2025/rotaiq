package com.rotai.iq.feature.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rotai.iq.core.data.repository.CommercialRepository
import com.rotai.iq.core.domain.model.SubscriptionInfo
import com.rotai.iq.core.featureflags.FeatureFlagManager
import com.rotai.iq.core.telemetry.TelemetryEvent
import com.rotai.iq.core.telemetry.TelemetryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

data class AdminMetricsUiState(
    val pendingEvents: List<TelemetryEvent> = emptyMap<String, String>().let { emptyList() },
    val activeFlags: Map<String, Boolean> = emptyMap(),
    val totalEvaluationsToday: Int = 0,
    val estimatedMrrReais: Double = 2990.0,
    val activeSubscribersCount: Int = 104,
    val feedbackMessage: String? = null
)

class AdminMetricsViewModel(
    private val commercialRepository: CommercialRepository,
    private val telemetryManager: TelemetryManager,
    private val featureFlagManager: FeatureFlagManager
) : ViewModel() {

    val subscriptionInfo: StateFlow<SubscriptionInfo> = commercialRepository.getSubscriptionInfo()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SubscriptionInfo.FREE_DEFAULT)

    private val _uiState = MutableStateFlow(
        AdminMetricsUiState(
            pendingEvents = telemetryManager.getPendingEvents(),
            activeFlags = featureFlagManager.getAllFlags(),
            totalEvaluationsToday = commercialRepository.getTodayEvaluationsCount()
        )
    )
    val uiState: StateFlow<AdminMetricsUiState> = _uiState.asStateFlow()

    fun refreshMetrics() {
        _uiState.value = _uiState.value.copy(
            pendingEvents = telemetryManager.getPendingEvents(),
            activeFlags = featureFlagManager.getAllFlags(),
            totalEvaluationsToday = commercialRepository.getTodayEvaluationsCount(),
            feedbackMessage = "Métricas administrativas atualizadas."
        )
    }

    fun toggleFeatureFlag(key: String, enabled: Boolean) {
        featureFlagManager.setOverride(key, enabled)
        _uiState.value = _uiState.value.copy(
            activeFlags = featureFlagManager.getAllFlags(),
            feedbackMessage = "Feature Flag '$key' atualizada para $enabled."
        )
    }

    fun clearTelemetryEvents() {
        telemetryManager.clearEvents()
        _uiState.value = _uiState.value.copy(
            pendingEvents = emptyList(),
            feedbackMessage = "Fila de telemetria limpa com sucesso."
        )
    }
}
