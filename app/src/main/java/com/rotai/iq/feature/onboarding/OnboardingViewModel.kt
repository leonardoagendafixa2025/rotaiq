package com.rotai.iq.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rotai.iq.core.data.repository.AuthRepository
import com.rotai.iq.core.data.repository.RotaIqRepository
import com.rotai.iq.core.domain.model.DriverGoal
import com.rotai.iq.core.domain.model.FuelType
import com.rotai.iq.core.domain.model.Vehicle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val currentStep: Int = 1, // 1: Veículo & Combustível, 2: Custos Fixos, 3: Metas & Copiloto
    val totalSteps: Int = 3,
    
    // Passo 1: Veículo e Combustível
    val vehicleName: String = "Meu Carro",
    val vehicleModel: String = "Onix / HB20 / Polo",
    val fuelType: FuelType = FuelType.GASOLINE,
    val consumptionInput: String = "11.5",
    val fuelPriceInput: String = "5.89",
    
    // Passo 2: Custos Fixos & Manutenção
    val insuranceMonthlyInput: String = "220.0",
    val taxesAnnualInput: String = "1800.0",
    val maintenancePerKmInput: String = "0.18",
    val estimatedMonthlyKmInput: String = "3000.0",
    
    // Passo 3: Metas Diárias
    val dailyGrossInput: String = "300.0",
    val dailyNetInput: String = "220.0",
    val shiftHoursInput: String = "8.0",
    
    val isLoading: Boolean = false,
    val isCompleted: Boolean = false,
    val errorMessage: String? = null
) {
    val consumption: Double get() = consumptionInput.toDoubleOrNull() ?: 11.5
    val fuelPrice: Double get() = fuelPriceInput.toDoubleOrNull() ?: 5.89
    val fuelCostPerKm: Double get() = if (consumption > 0) fuelPrice / consumption else 0.0

    val insuranceMonthly: Double get() = insuranceMonthlyInput.toDoubleOrNull() ?: 220.0
    val taxesAnnual: Double get() = taxesAnnualInput.toDoubleOrNull() ?: 1800.0
    val monthlyTaxes: Double get() = taxesAnnual / 12.0
    val maintenancePerKm: Double get() = maintenancePerKmInput.toDoubleOrNull() ?: 0.18
    val estimatedMonthlyKm: Double get() = (estimatedMonthlyKmInput.toDoubleOrNull() ?: 3000.0).coerceAtLeast(500.0)
    
    val fixedCostPerKm: Double get() = (insuranceMonthly + monthlyTaxes) / estimatedMonthlyKm
    val totalCostPerKm: Double get() = fuelCostPerKm + maintenancePerKm + fixedCostPerKm

    val dailyNet: Double get() = dailyNetInput.toDoubleOrNull() ?: 220.0
    val monthlyProjectedProfit: Double get() = dailyNet * 26.0 // 26 dias úteis no mês
}

class OnboardingViewModel(
    private val repository: RotaIqRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onStepChanged(step: Int) {
        _uiState.update { it.copy(currentStep = step.coerceIn(1, 3)) }
    }

    fun nextStep() {
        val next = _uiState.value.currentStep + 1
        if (next <= _uiState.value.totalSteps) {
            _uiState.update { it.copy(currentStep = next, errorMessage = null) }
        }
    }

    fun previousStep() {
        val prev = _uiState.value.currentStep - 1
        if (prev >= 1) {
            _uiState.update { it.copy(currentStep = prev, errorMessage = null) }
        }
    }

    // Handlers Passo 1
    fun onVehicleNameChanged(value: String) = _uiState.update { it.copy(vehicleName = value) }
    fun onVehicleModelChanged(value: String) = _uiState.update { it.copy(vehicleModel = value) }
    fun onFuelTypeChanged(type: FuelType) = _uiState.update { it.copy(fuelType = type) }
    fun onConsumptionChanged(value: String) = _uiState.update { it.copy(consumptionInput = value) }
    fun onFuelPriceChanged(value: String) = _uiState.update { it.copy(fuelPriceInput = value) }

    // Handlers Passo 2
    fun onInsuranceChanged(value: String) = _uiState.update { it.copy(insuranceMonthlyInput = value) }
    fun onTaxesChanged(value: String) = _uiState.update { it.copy(taxesAnnualInput = value) }
    fun onMaintenanceChanged(value: String) = _uiState.update { it.copy(maintenancePerKmInput = value) }
    fun onEstimatedKmChanged(value: String) = _uiState.update { it.copy(estimatedMonthlyKmInput = value) }

    // Handlers Passo 3
    fun onDailyGrossChanged(value: String) = _uiState.update { it.copy(dailyGrossInput = value) }
    fun onDailyNetChanged(value: String) = _uiState.update { it.copy(dailyNetInput = value) }
    fun onShiftHoursChanged(value: String) = _uiState.update { it.copy(shiftHoursInput = value) }

    fun completeOnboarding(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val state = _uiState.value
                
                // 1. Salva Veículo Configurado
                val vehicle = Vehicle(
                    name = state.vehicleName.ifBlank { "Meu Carro" },
                    model = state.vehicleModel.ifBlank { "Geral" },
                    fuelType = state.fuelType,
                    consumptionKmPerLiter = state.consumption,
                    fuelPricePerLiter = state.fuelPrice,
                    maintenanceCostPerKm = state.maintenancePerKm,
                    monthlyInsuranceCost = state.insuranceMonthly,
                    annualTaxesCost = state.taxesAnnual,
                    estimatedMonthlyKm = state.estimatedMonthlyKm
                )
                repository.saveVehicle(vehicle)

                // 2. Salva Meta do Motorista
                val goal = DriverGoal(
                    dailyGrossTarget = state.dailyGrossInput.toDoubleOrNull() ?: 300.0,
                    dailyNetTarget = state.dailyNet,
                    shiftTargetHours = state.shiftHoursInput.toDoubleOrNull() ?: 8.0,
                    targetKmRate = (state.dailyGrossInput.toDoubleOrNull() ?: 300.0) / 120.0
                )
                repository.saveDriverGoal(goal)

                // 3. Marca Onboarding como Concluído
                authRepository.setOnboardingCompleted(true)

                _uiState.update { it.copy(isLoading = false, isCompleted = true) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Falha ao gravar configurações: ${e.message}"
                    )
                }
            }
        }
    }

    fun skipOnboarding(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.setOnboardingCompleted(true)
            onSuccess()
        }
    }
}
