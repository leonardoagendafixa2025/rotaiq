package com.rotai.iq.feature.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rotai.iq.core.data.repository.RotaIqRepository
import com.rotai.iq.core.domain.engine.AdvancedFinancialEngine
import com.rotai.iq.core.domain.engine.FuelEngine
import com.rotai.iq.core.domain.engine.MaintenanceSchedulerEngine
import com.rotai.iq.core.domain.model.ComprehensiveFinancialReport
import com.rotai.iq.core.domain.model.ExpenseCategory
import com.rotai.iq.core.domain.model.FinancialPeriod
import com.rotai.iq.core.domain.model.FuelRecord
import com.rotai.iq.core.domain.model.FuelType
import com.rotai.iq.core.domain.model.MaintenanceRecord
import com.rotai.iq.core.domain.model.MaintenanceType
import com.rotai.iq.core.domain.model.RideEvaluation
import com.rotai.iq.core.domain.model.Vehicle
import com.rotai.iq.core.domain.model.VehicleExpense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FinancialHubUiState(
    val selectedPeriod: FinancialPeriod = FinancialPeriod.DAILY,
    val report: ComprehensiveFinancialReport? = null,
    val fuelRecords: List<FuelRecord> = emptyList(),
    val maintenanceRecords: List<MaintenanceRecord> = emptyList(),
    val maintenanceAlerts: List<MaintenanceSchedulerEngine.MaintenanceStatus> = emptyList(),
    val expenses: List<VehicleExpense> = emptyList(),
    val averageFuelConsumption: Double? = null,
    val vehicle: Vehicle = Vehicle(),
    // Formulario de Abastecimento
    val fuelOdometerInput: String = "",
    val fuelLitersInput: String = "",
    val fuelPriceInput: String = "5.89",
    val fuelIsFullTank: Boolean = true,
    // Formulario de Manutenção
    val maintType: MaintenanceType = MaintenanceType.OIL_CHANGE,
    val maintDescInput: String = "Troca de óleo e filtro 5W30",
    val maintCostInput: String = "250.00",
    val maintOdometerInput: String = "",
    val maintNextKmInput: String = "",
    val feedbackMessage: String? = null
)

class FinancialHubViewModel(
    private val repository: RotaIqRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinancialHubUiState())
    val uiState: StateFlow<FinancialHubUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                repository.getActiveVehicle(),
                repository.getAllEvaluations(),
                repository.getAllFuelRecords(),
                repository.getAllMaintenanceRecords(),
                repository.getAllExpenses()
            ) { vehicle, rides, fuel, maint, expenses ->
                val avgConsumption = FuelEngine.calculateAverageConsumption(fuel)
                val alerts = maint.map {
                    val currentOdometer = fuel.firstOrNull()?.odometerKm ?: 50000.0
                    MaintenanceSchedulerEngine.evaluateServiceStatus(it, currentOdometer)
                }

                val currentPeriod = _uiState.value.selectedPeriod
                val report = generateReportForPeriod(
                    period = currentPeriod,
                    rides = rides,
                    fuel = fuel,
                    maint = maint,
                    expenses = expenses,
                    vehicle = vehicle
                )

                _uiState.value.copy(
                    vehicle = vehicle,
                    report = report,
                    fuelRecords = fuel,
                    maintenanceRecords = maint,
                    maintenanceAlerts = alerts,
                    expenses = expenses,
                    averageFuelConsumption = avgConsumption
                )
            }.collect { updatedState ->
                _uiState.value = updatedState
            }
        }
    }

    fun onPeriodSelected(period: FinancialPeriod) {
        val s = _uiState.value
        val newReport = generateReportForPeriod(
            period = period,
            rides = emptyList(), // será atualizado na próxima emissão ou recalculado
            fuel = s.fuelRecords,
            maint = s.maintenanceRecords,
            expenses = s.expenses,
            vehicle = s.vehicle
        )
        _uiState.value = _uiState.value.copy(
            selectedPeriod = period,
            report = newReport
        )
        // Recarregar com dados atuais
        loadData()
    }

    private fun generateReportForPeriod(
        period: FinancialPeriod,
        rides: List<RideEvaluation>,
        fuel: List<FuelRecord>,
        maint: List<MaintenanceRecord>,
        expenses: List<VehicleExpense>,
        vehicle: Vehicle
    ): ComprehensiveFinancialReport {
        val label = when (period) {
            FinancialPeriod.DAILY -> "Hoje (${SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date())})"
            FinancialPeriod.WEEKLY -> "Esta Semana"
            FinancialPeriod.MONTHLY -> "Este Mês (${SimpleDateFormat("MMMM/yyyy", Locale("pt", "BR")).format(Date())})"
            FinancialPeriod.ANNUAL -> "Ano ${SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())}"
        }

        return AdvancedFinancialEngine.generateReport(
            period = period,
            periodLabel = label,
            rides = rides,
            fuelRecords = fuel,
            maintenanceRecords = maint,
            otherExpenses = expenses,
            vehicle = vehicle,
            hoursOnline = 8.0,
            hoursDriving = 6.5
        )
    }

    // Ações de formulário de combustível
    fun onFuelOdometerChanged(value: String) { _uiState.value = _uiState.value.copy(fuelOdometerInput = value) }
    fun onFuelLitersChanged(value: String) { _uiState.value = _uiState.value.copy(fuelLitersInput = value) }
    fun onFuelPriceChanged(value: String) { _uiState.value = _uiState.value.copy(fuelPriceInput = value) }
    fun onFuelFullTankChanged(value: Boolean) { _uiState.value = _uiState.value.copy(fuelIsFullTank = value) }

    fun addFuelRecord() {
        val s = _uiState.value
        val odo = s.fuelOdometerInput.replace(",", ".").toDoubleOrNull() ?: return
        val liters = s.fuelLitersInput.replace(",", ".").toDoubleOrNull() ?: return
        val price = s.fuelPriceInput.replace(",", ".").toDoubleOrNull() ?: return
        val total = liters * price

        viewModelScope.launch {
            val lastTwo = repository.getLastTwoFullTankRecords()
            val prev = lastTwo.firstOrNull()

            var kmPerL: Double? = null
            var costPerKm: Double? = null

            if (s.fuelIsFullTank && prev != null && prev.isFullTank && odo > prev.odometerKm) {
                val dist = odo - prev.odometerKm
                kmPerL = dist / liters
                costPerKm = total / dist
            }

            val record = FuelRecord(
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                odometerKm = odo,
                liters = liters,
                pricePerLiter = price,
                totalPaid = total,
                fuelType = s.vehicle.fuelType,
                isFullTank = s.fuelIsFullTank,
                calculatedKmPerLiter = kmPerL,
                calculatedCostPerKm = costPerKm
            )
            repository.saveFuelRecord(record)
            _uiState.value = _uiState.value.copy(
                fuelOdometerInput = "",
                fuelLitersInput = "",
                feedbackMessage = "Abastecimento registrado com sucesso!"
            )
        }
    }

    // Ações de formulário de manutenção
    fun onMaintTypeChanged(type: MaintenanceType) { _uiState.value = _uiState.value.copy(maintType = type) }
    fun onMaintDescChanged(value: String) { _uiState.value = _uiState.value.copy(maintDescInput = value) }
    fun onMaintCostChanged(value: String) { _uiState.value = _uiState.value.copy(maintCostInput = value) }
    fun onMaintOdometerChanged(value: String) { _uiState.value = _uiState.value.copy(maintOdometerInput = value) }
    fun onMaintNextKmChanged(value: String) { _uiState.value = _uiState.value.copy(maintNextKmInput = value) }

    fun addMaintenanceRecord() {
        val s = _uiState.value
        val cost = s.maintCostInput.replace(",", ".").toDoubleOrNull() ?: return
        val odo = s.maintOdometerInput.replace(",", ".").toDoubleOrNull() ?: 50000.0
        val nextKm = s.maintNextKmInput.replace(",", ".").toDoubleOrNull() ?: (odo + s.maintType.typicalIntervalKm)

        viewModelScope.launch {
            val record = MaintenanceRecord(
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                odometerKm = odo,
                type = s.maintType,
                description = s.maintDescInput,
                cost = cost,
                nextServiceKm = nextKm
            )
            repository.saveMaintenanceRecord(record)
            _uiState.value = _uiState.value.copy(
                maintCostInput = "",
                maintOdometerInput = "",
                maintNextKmInput = "",
                feedbackMessage = "Manutenção registrada com sucesso!"
            )
        }
    }

    fun deleteFuelRecord(id: String) {
        viewModelScope.launch { repository.deleteFuelRecord(id) }
    }

    fun deleteMaintenanceRecord(id: String) {
        viewModelScope.launch { repository.deleteMaintenanceRecord(id) }
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(feedbackMessage = null)
    }
}
