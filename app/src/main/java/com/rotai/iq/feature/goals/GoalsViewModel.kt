package com.rotai.iq.feature.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rotai.iq.core.data.repository.RotaIqRepository
import com.rotai.iq.core.domain.engine.GoalEngine
import com.rotai.iq.core.domain.model.DriverGoal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GoalsUiState(
    val goal: DriverGoal = DriverGoal(),
    val dailyGrossInput: String = "300.0",
    val dailyNetInput: String = "220.0",
    val targetHourlyInput: String = "45.0",
    val targetKmInput: String = "2.40",
    val shiftHoursInput: String = "8.0",
    val currentGrossInput: String = "0.0",
    val hoursWorkedInput: String = "0.0",
    val coachingAdvice: GoalEngine.GoalCoachingAdvice = GoalEngine.evaluateGoalProgress(DriverGoal()),
    val isSavedSuccess: Boolean = false
)

class GoalsViewModel(
    private val repository: RotaIqRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    init {
        loadGoal()
    }

    private fun loadGoal() {
        viewModelScope.launch {
            repository.getDriverGoal().collect { goal ->
                val advice = GoalEngine.evaluateGoalProgress(goal)
                _uiState.value = _uiState.value.copy(
                    goal = goal,
                    dailyGrossInput = goal.dailyGrossTarget.toString(),
                    dailyNetInput = goal.dailyNetTarget.toString(),
                    targetHourlyInput = goal.targetHourlyRate.toString(),
                    targetKmInput = goal.targetKmRate.toString(),
                    shiftHoursInput = goal.shiftTargetHours.toString(),
                    currentGrossInput = goal.currentDailyGross.toString(),
                    hoursWorkedInput = goal.hoursWorkedToday.toString(),
                    coachingAdvice = advice
                )
            }
        }
    }

    fun onDailyGrossChanged(value: String) {
        _uiState.value = _uiState.value.copy(dailyGrossInput = value)
        updateCalculations()
    }

    fun onDailyNetChanged(value: String) {
        _uiState.value = _uiState.value.copy(dailyNetInput = value)
        updateCalculations()
    }

    fun onTargetHourlyChanged(value: String) {
        _uiState.value = _uiState.value.copy(targetHourlyInput = value)
        updateCalculations()
    }

    fun onTargetKmChanged(value: String) {
        _uiState.value = _uiState.value.copy(targetKmInput = value)
        updateCalculations()
    }

    fun onShiftHoursChanged(value: String) {
        _uiState.value = _uiState.value.copy(shiftHoursInput = value)
        updateCalculations()
    }

    fun onCurrentGrossChanged(value: String) {
        _uiState.value = _uiState.value.copy(currentGrossInput = value)
        updateCalculations()
    }

    fun onHoursWorkedChanged(value: String) {
        _uiState.value = _uiState.value.copy(hoursWorkedInput = value)
        updateCalculations()
    }

    private fun buildGoalFromInputs(): DriverGoal {
        val s = _uiState.value
        return DriverGoal(
            id = s.goal.id,
            dailyGrossTarget = s.dailyGrossInput.replace(",", ".").toDoubleOrNull() ?: 300.0,
            dailyNetTarget = s.dailyNetInput.replace(",", ".").toDoubleOrNull() ?: 220.0,
            targetHourlyRate = s.targetHourlyInput.replace(",", ".").toDoubleOrNull() ?: 45.0,
            targetKmRate = s.targetKmInput.replace(",", ".").toDoubleOrNull() ?: 2.40,
            shiftTargetHours = s.shiftHoursInput.replace(",", ".").toDoubleOrNull() ?: 8.0,
            currentDailyGross = s.currentGrossInput.replace(",", ".").toDoubleOrNull() ?: 0.0,
            currentDailyNet = (s.currentGrossInput.replace(",", ".").toDoubleOrNull() ?: 0.0) * 0.75,
            hoursWorkedToday = s.hoursWorkedInput.replace(",", ".").toDoubleOrNull() ?: 0.0
        )
    }

    private fun updateCalculations() {
        val updated = buildGoalFromInputs()
        val advice = GoalEngine.evaluateGoalProgress(updated)
        _uiState.value = _uiState.value.copy(
            goal = updated,
            coachingAdvice = advice,
            isSavedSuccess = false
        )
    }

    fun saveGoal() {
        val toSave = buildGoalFromInputs()
        viewModelScope.launch {
            repository.saveDriverGoal(toSave)
            _uiState.value = _uiState.value.copy(isSavedSuccess = true)
        }
    }
}
