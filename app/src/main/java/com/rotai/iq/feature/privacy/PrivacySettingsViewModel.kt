package com.rotai.iq.feature.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rotai.iq.core.data.repository.CommercialRepository
import com.rotai.iq.core.data.repository.RotaIqRepository
import com.rotai.iq.core.domain.model.LgpdConsent
import com.rotai.iq.core.security.LgpdManager
import com.rotai.iq.core.telemetry.TelemetryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PrivacySettingsUiState(
    val exportJsonPreview: String? = null,
    val isExporting: Boolean = false,
    val isDeleting: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val deleteConfirmationText: String = "",
    val deleteErrorMessage: String? = null,
    val feedbackMessage: String? = null
)

class PrivacySettingsViewModel(
    private val commercialRepository: CommercialRepository,
    private val rotaIqRepository: RotaIqRepository,
    private val lgpdManager: LgpdManager,
    private val telemetryManager: TelemetryManager
) : ViewModel() {

    val consentState: StateFlow<LgpdConsent> = commercialRepository.getLgpdConsent()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LgpdConsent())

    private val _uiState = MutableStateFlow(PrivacySettingsUiState())
    val uiState: StateFlow<PrivacySettingsUiState> = _uiState.asStateFlow()

    fun updateTelemetryOptIn(optIn: Boolean) {
        viewModelScope.launch {
            val current = consentState.value
            val updated = current.copy(telemetryOptIn = optIn)
            commercialRepository.saveLgpdConsent(updated)
        }
    }

    fun updateBenchmarkingOptIn(optIn: Boolean) {
        viewModelScope.launch {
            val current = consentState.value
            val updated = current.copy(anonymousBenchmarkingOptIn = optIn)
            commercialRepository.saveLgpdConsent(updated)
        }
    }

    fun onDeleteConfirmationTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(
            deleteConfirmationText = text,
            deleteErrorMessage = null
        )
    }

    fun exportDriverData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            val vehicle = rotaIqRepository.getActiveVehicle().first()
            val rides = rotaIqRepository.getAllEvaluations().first()
            val fuels = rotaIqRepository.getAllFuelRecords().first()
            val maints = rotaIqRepository.getAllMaintenanceRecords().first()
            val expenses = rotaIqRepository.getAllExpenses().first()
            val sub = commercialRepository.getSubscriptionInfo().first()
            val consent = consentState.value

            val exportPkg = lgpdManager.generateExportPackage(
                driverId = "DRV-1082",
                driverName = "Motorista ROTA IQ",
                driverEmail = "motorista@rotai.app",
                driverCity = "São Paulo - SP",
                registeredVehiclesCount = if (vehicle.plate.isNotBlank()) 1 else 0,
                totalRidesEvaluated = rides.size,
                totalFuelRecords = fuels.size,
                totalMaintenanceRecords = maints.size,
                totalExpensesCount = expenses.size,
                subscriptionTier = sub.tier.displayName,
                consent = consent
            )

            val jsonString = lgpdManager.exportToJsonString(exportPkg)
            telemetryManager.recordEvent(TelemetryManager.EVENT_LGPD_EXPORT_REQUESTED)

            _uiState.value = _uiState.value.copy(
                isExporting = false,
                exportJsonPreview = jsonString,
                feedbackMessage = "Dados exportados com sucesso em conformidade com o Art. 18 da LGPD."
            )
        }
    }

    fun promptDeleteAccount() {
        _uiState.value = _uiState.value.copy(
            showDeleteConfirmation = true,
            deleteConfirmationText = "",
            deleteErrorMessage = null
        )
    }

    fun dismissDeletePrompt() {
        _uiState.value = _uiState.value.copy(
            showDeleteConfirmation = false,
            deleteConfirmationText = "",
            deleteErrorMessage = null
        )
    }

    fun confirmDeleteAccountAndPurge() {
        val input = _uiState.value.deleteConfirmationText.trim()
        if (!input.equals("EXCLUIR", ignoreCase = true)) {
            _uiState.value = _uiState.value.copy(
                deleteErrorMessage = "Digite 'EXCLUIR' exatamente para autorizar a remoção permanente."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDeleting = true,
                showDeleteConfirmation = false,
                deleteConfirmationText = "",
                deleteErrorMessage = null
            )
            commercialRepository.purgeAllUserData()
            _uiState.value = _uiState.value.copy(
                isDeleting = false,
                feedbackMessage = "Todos os seus dados foram expurgados permanentemente deste dispositivo."
            )
        }
    }

    fun dismissExportPreview() {
        _uiState.value = _uiState.value.copy(exportJsonPreview = null)
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(feedbackMessage = null)
    }
}
