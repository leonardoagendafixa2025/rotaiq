package com.rotai.iq.feature.vehicle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rotai.iq.core.data.repository.RotaIqRepository
import com.rotai.iq.core.domain.engine.VehicleCostEngine
import com.rotai.iq.core.domain.model.FuelType
import com.rotai.iq.core.domain.model.Vehicle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VehicleUiState(
    val vehicle: Vehicle = Vehicle(),
    val nameInput: String = "Meu Carro",
    val modelInput: String = "Sedan / Hatch",
    val plateInput: String = "ABC-1D23",
    val fuelType: FuelType = FuelType.GASOLINE,
    val consumptionInput: String = "11.0",
    val fuelPriceInput: String = "5.89",
    val maintenancePerKmInput: String = "0.18",
    val monthlyInsuranceInput: String = "220.0",
    val annualTaxesInput: String = "1800.0",
    val monthlyDepreciationInput: String = "350.0",
    val monthlyOtherCostsInput: String = "150.0",
    val estimatedMonthlyKmInput: String = "3000.0",
    val costProjections: VehicleCostEngine.CostProjections = VehicleCostEngine.calculateProjections(Vehicle()),
    val isSavedSuccess: Boolean = false
)

class VehicleViewModel(
    private val repository: RotaIqRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehicleUiState())
    val uiState: StateFlow<VehicleUiState> = _uiState.asStateFlow()

    init {
        loadVehicle()
    }

    private fun loadVehicle() {
        viewModelScope.launch {
            repository.getActiveVehicle().collect { vehicle ->
                _uiState.value = _uiState.value.copy(
                    vehicle = vehicle,
                    nameInput = vehicle.name,
                    modelInput = vehicle.model,
                    plateInput = vehicle.plate,
                    fuelType = vehicle.fuelType,
                    consumptionInput = vehicle.consumptionKmPerLiter.toString(),
                    fuelPriceInput = vehicle.fuelPricePerLiter.toString(),
                    maintenancePerKmInput = vehicle.maintenanceCostPerKm.toString(),
                    monthlyInsuranceInput = vehicle.monthlyInsuranceCost.toString(),
                    annualTaxesInput = vehicle.annualTaxesCost.toString(),
                    monthlyDepreciationInput = vehicle.monthlyDepreciation.toString(),
                    monthlyOtherCostsInput = vehicle.monthlyOtherCosts.toString(),
                    estimatedMonthlyKmInput = vehicle.estimatedMonthlyKm.toString(),
                    costProjections = VehicleCostEngine.calculateProjections(vehicle)
                )
            }
        }
    }

    fun onFuelTypeChanged(fuelType: FuelType) {
        _uiState.value = _uiState.value.copy(fuelType = fuelType)
        updateCalculations()
    }

    fun onConsumptionChanged(value: String) {
        _uiState.value = _uiState.value.copy(consumptionInput = value)
        updateCalculations()
    }

    fun onFuelPriceChanged(value: String) {
        _uiState.value = _uiState.value.copy(fuelPriceInput = value)
        updateCalculations()
    }

    fun onMaintenanceChanged(value: String) {
        _uiState.value = _uiState.value.copy(maintenancePerKmInput = value)
        updateCalculations()
    }

    fun onInsuranceChanged(value: String) {
        _uiState.value = _uiState.value.copy(monthlyInsuranceInput = value)
        updateCalculations()
    }

    fun onTaxesChanged(value: String) {
        _uiState.value = _uiState.value.copy(annualTaxesInput = value)
        updateCalculations()
    }

    fun onDepreciationChanged(value: String) {
        _uiState.value = _uiState.value.copy(monthlyDepreciationInput = value)
        updateCalculations()
    }

    fun onMonthlyKmChanged(value: String) {
        _uiState.value = _uiState.value.copy(estimatedMonthlyKmInput = value)
        updateCalculations()
    }

    private fun buildVehicleFromInputs(): Vehicle {
        val s = _uiState.value
        return Vehicle(
            id = s.vehicle.id,
            name = s.nameInput,
            model = s.modelInput,
            plate = s.plateInput,
            fuelType = s.fuelType,
            consumptionKmPerLiter = s.consumptionInput.replace(",", ".").toDoubleOrNull() ?: 10.0,
            fuelPricePerLiter = s.fuelPriceInput.replace(",", ".").toDoubleOrNull() ?: 5.0,
            maintenanceCostPerKm = s.maintenancePerKmInput.replace(",", ".").toDoubleOrNull() ?: 0.15,
            monthlyInsuranceCost = s.monthlyInsuranceInput.replace(",", ".").toDoubleOrNull() ?: 200.0,
            annualTaxesCost = s.annualTaxesInput.replace(",", ".").toDoubleOrNull() ?: 1500.0,
            monthlyDepreciation = s.monthlyDepreciationInput.replace(",", ".").toDoubleOrNull() ?: 300.0,
            monthlyOtherCosts = s.monthlyOtherCostsInput.replace(",", ".").toDoubleOrNull() ?: 100.0,
            estimatedMonthlyKm = s.estimatedMonthlyKmInput.replace(",", ".").toDoubleOrNull() ?: 3000.0,
            isActive = true
        )
    }

    private fun updateCalculations() {
        val updated = buildVehicleFromInputs()
        val projections = VehicleCostEngine.calculateProjections(updated)
        _uiState.value = _uiState.value.copy(
            vehicle = updated,
            costProjections = projections,
            isSavedSuccess = false
        )
    }

    fun saveVehicle() {
        val vehicleToSave = buildVehicleFromInputs()
        viewModelScope.launch {
            repository.saveVehicle(vehicleToSave)
            _uiState.value = _uiState.value.copy(isSavedSuccess = true)
        }
    }
}
