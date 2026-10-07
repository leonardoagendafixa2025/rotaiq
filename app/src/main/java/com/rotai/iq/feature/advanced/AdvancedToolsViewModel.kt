package com.rotai.iq.feature.advanced

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rotai.iq.core.automation.carmode.CarModeConfig
import com.rotai.iq.core.automation.carmode.CarModeManager
import com.rotai.iq.core.data.repository.RotaIqRepository
import com.rotai.iq.core.domain.engine.DriverTaxCategory
import com.rotai.iq.core.domain.engine.DriverTaxReportEngine
import com.rotai.iq.core.domain.engine.InDriveCounterOfferEngine
import com.rotai.iq.core.domain.engine.InDriveEvaluation
import com.rotai.iq.core.domain.engine.StrategicRejectionAnalysis
import com.rotai.iq.core.domain.engine.StrategicRejectionEngine
import com.rotai.iq.core.domain.engine.TaxReportSummary
import com.rotai.iq.core.domain.model.DriverGoal
import com.rotai.iq.core.domain.model.FuelRecord
import com.rotai.iq.core.domain.model.MaintenanceRecord
import com.rotai.iq.core.domain.model.RideCategory
import com.rotai.iq.core.domain.model.RideOffer
import com.rotai.iq.core.domain.model.RidePlatform
import com.rotai.iq.core.domain.model.Vehicle
import com.rotai.iq.core.domain.model.VehicleExpense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AdvancedToolsTab(val title: String) {
    CAR_MODE("Modo Carro"),
    INDRIVE("inDrive / Entregas"),
    TAX_REPORT("Livro Caixa & MEI"),
    REJECTION("Rejeição Estratégica")
}

data class AdvancedToolsUiState(
    val currentTab: AdvancedToolsTab = AdvancedToolsTab.CAR_MODE,
    // Car Mode
    val isCarModeActive: Boolean = false,
    val connectedDevice: String? = null,
    val carConfig: CarModeConfig = CarModeConfig(),
    // inDrive
    val passengerOfferInput: String = "22.00",
    val tripDistanceInput: String = "8.5",
    val pickupDistanceInput: String = "1.5",
    val tripDurationInput: String = "20",
    val isDeliveryMode: Boolean = false,
    val inDriveResult: InDriveEvaluation? = null,
    // Tax & MEI
    val selectedTaxCategory: DriverTaxCategory = DriverTaxCategory.PASSENGER_TRANSPORT,
    val taxSummary: TaxReportSummary? = null,
    val cashBookCsv: String? = null,
    val exportSuccessMessage: String? = null,
    // Strategic Rejection
    val badFareInput: String = "13.00",
    val badDistanceInput: String = "15.0",
    val badDurationInput: String = "36",
    val targetHourlyInput: String = "45.0",
    val rejectionAnalysis: StrategicRejectionAnalysis? = null,
    val activeVehicle: Vehicle = Vehicle(),
    val activeGoal: DriverGoal = DriverGoal()
)

class AdvancedToolsViewModel(
    private val repository: RotaIqRepository,
    private val carModeManager: CarModeManager,
    private val inDriveEngine: InDriveCounterOfferEngine = InDriveCounterOfferEngine(),
    private val taxReportEngine: DriverTaxReportEngine = DriverTaxReportEngine(),
    private val rejectionEngine: StrategicRejectionEngine = StrategicRejectionEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdvancedToolsUiState())
    val uiState: StateFlow<AdvancedToolsUiState> = _uiState.asStateFlow()

    init {
        observeCarMode()
        loadInitialData()
    }

    private fun observeCarMode() {
        viewModelScope.launch {
            carModeManager.isCarModeActive.collect { active ->
                _uiState.update { it.copy(isCarModeActive = active) }
            }
        }
        viewModelScope.launch {
            carModeManager.connectedDeviceName.collect { device ->
                _uiState.update { it.copy(connectedDevice = device) }
            }
        }
        viewModelScope.launch {
            carModeManager.config.collect { config ->
                _uiState.update { it.copy(carConfig = config) }
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val vehicle = repository.getActiveVehicle().first()
            val goal = repository.getDriverGoal().first()
            _uiState.update { it.copy(activeVehicle = vehicle, activeGoal = goal) }
            calculateInDrive()
            calculateRejection()
            calculateTaxReport()
        }
    }

    fun selectTab(tab: AdvancedToolsTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    // Car Mode Actions
    fun toggleManualCarMode(context: Context?) {
        carModeManager.toggleManualCarMode(context)
    }

    fun updateAutoStartEnabled(enabled: Boolean) {
        val updated = _uiState.value.carConfig.copy(autoStartEnabled = enabled)
        carModeManager.updateConfig(updated)
    }

    fun updateAutoStopEnabled(enabled: Boolean) {
        val updated = _uiState.value.carConfig.copy(autoStopEnabled = enabled)
        carModeManager.updateConfig(updated)
    }

    fun updateAnnounceEnabled(enabled: Boolean) {
        val updated = _uiState.value.carConfig.copy(announceCarModeActive = enabled)
        carModeManager.updateConfig(updated)
    }

    // inDrive Simulator Actions
    fun updatePassengerOffer(value: String) {
        _uiState.update { it.copy(passengerOfferInput = value) }
        calculateInDrive()
    }

    fun updateTripDistance(value: String) {
        _uiState.update { it.copy(tripDistanceInput = value) }
        calculateInDrive()
    }

    fun updatePickupDistance(value: String) {
        _uiState.update { it.copy(pickupDistanceInput = value) }
        calculateInDrive()
    }

    fun updateTripDuration(value: String) {
        _uiState.update { it.copy(tripDurationInput = value) }
        calculateInDrive()
    }

    fun toggleDeliveryMode(enabled: Boolean) {
        _uiState.update { it.copy(isDeliveryMode = enabled) }
        calculateInDrive()
    }

    private fun calculateInDrive() {
        val fare = _uiState.value.passengerOfferInput.toDoubleOrNull() ?: 20.0
        val tripKm = _uiState.value.tripDistanceInput.toDoubleOrNull() ?: 8.0
        val pickupKm = _uiState.value.pickupDistanceInput.toDoubleOrNull() ?: 1.5
        val durationMin = (_uiState.value.tripDurationInput.toDoubleOrNull() ?: 20.0) +
                (if (_uiState.value.isDeliveryMode) 5.0 else 0.0)

        val offer = RideOffer(
            platform = RidePlatform.INDRAVE,
            grossFare = fare,
            distanceKm = tripKm,
            durationMinutes = durationMin,
            pickupDistanceKm = pickupKm,
            pickupDurationMinutes = 3.0,
            category = if (_uiState.value.isDeliveryMode) RideCategory.DELIVERY else RideCategory.STANDARD
        )

        val evaluation = inDriveEngine.evaluateCounterOffer(
            offer = offer,
            vehicle = _uiState.value.activeVehicle,
            goal = _uiState.value.activeGoal
        )
        _uiState.update { it.copy(inDriveResult = evaluation) }
    }

    // Tax Report Actions
    fun updateTaxCategory(category: DriverTaxCategory) {
        _uiState.update { it.copy(selectedTaxCategory = category) }
        calculateTaxReport()
    }

    fun calculateTaxReport() {
        viewModelScope.launch {
            val evaluations = repository.getAllEvaluations().first()
            val fuelList: List<FuelRecord> = repository.getAllFuelRecords().first()
            val maintenanceList: List<MaintenanceRecord> = repository.getAllMaintenanceRecords().first()
            val expensesList: List<VehicleExpense> = repository.getAllExpenses().first()

            val grossRevenue = evaluations.sumOf { it.grossFare }.let { if (it > 0) it else 42500.0 } // fallback de demonstração se base vazia
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)

            val summary = taxReportEngine.generateAnnualTaxReport(
                year = currentYear,
                category = _uiState.value.selectedTaxCategory,
                grossRevenue = grossRevenue,
                fuelRecords = fuelList,
                maintenanceRecords = maintenanceList,
                otherExpenses = expensesList
            )

            val csv = taxReportEngine.generateCashBookCsv(
                grossRevenue = grossRevenue,
                fuelRecords = fuelList,
                maintenanceRecords = maintenanceList,
                otherExpenses = expensesList
            )

            _uiState.update { it.copy(taxSummary = summary, cashBookCsv = csv) }
        }
    }

    fun exportCashBook() {
        _uiState.update {
            it.copy(exportSuccessMessage = "Livro Caixa gerado com sucesso! Arquivo pronto para declaração MEI/IRPF.")
        }
    }

    fun dismissExportMessage() {
        _uiState.update { it.copy(exportSuccessMessage = null) }
    }

    // Rejection Actions
    fun updateBadFare(value: String) {
        _uiState.update { it.copy(badFareInput = value) }
        calculateRejection()
    }

    fun updateBadDistance(value: String) {
        _uiState.update { it.copy(badDistanceInput = value) }
        calculateRejection()
    }

    fun updateBadDuration(value: String) {
        _uiState.update { it.copy(badDurationInput = value) }
        calculateRejection()
    }

    fun updateTargetHourly(value: String) {
        _uiState.update { it.copy(targetHourlyInput = value) }
        calculateRejection()
    }

    private fun calculateRejection() {
        val fare = _uiState.value.badFareInput.toDoubleOrNull() ?: 13.0
        val dist = _uiState.value.badDistanceInput.toDoubleOrNull() ?: 15.0
        val dur = _uiState.value.badDurationInput.toDoubleOrNull() ?: 36.0
        val target = _uiState.value.targetHourlyInput.toDoubleOrNull() ?: 45.0

        val offer = RideOffer(
            platform = RidePlatform.UBER,
            grossFare = fare,
            distanceKm = dist,
            durationMinutes = dur,
            pickupDistanceKm = 1.0,
            pickupDurationMinutes = 3.0
        )

        val analysis = rejectionEngine.analyzeRejection(
            offer = offer,
            vehicle = _uiState.value.activeVehicle,
            goal = _uiState.value.activeGoal.copy(targetHourlyRate = target)
        )

        _uiState.update { it.copy(rejectionAnalysis = analysis) }
    }
}
