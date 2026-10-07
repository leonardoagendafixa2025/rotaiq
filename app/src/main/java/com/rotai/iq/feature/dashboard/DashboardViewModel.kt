package com.rotai.iq.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rotai.iq.core.data.repository.RotaIqRepository
import com.rotai.iq.core.domain.engine.GoalEngine
import com.rotai.iq.core.domain.model.DailyFinancialSummary
import com.rotai.iq.core.domain.model.DriverGoal
import com.rotai.iq.core.domain.model.RideEvaluation
import com.rotai.iq.core.domain.model.Vehicle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DashboardUiState(
    val vehicle: Vehicle = Vehicle(),
    val goal: DriverGoal = DriverGoal(),
    val financialSummary: DailyFinancialSummary = DailyFinancialSummary(
        date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    ),
    val latestEvaluation: RideEvaluation? = null,
    val coachingAdvice: GoalEngine.GoalCoachingAdvice = GoalEngine.evaluateGoalProgress(DriverGoal()),
    val isOperatingWell: Boolean = true,
    val isLoading: Boolean = false
)

class DashboardViewModel(
    private val repository: RotaIqRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        viewModelScope.launch {
            combine(
                repository.getActiveVehicle(),
                repository.getDriverGoal(),
                repository.getDailyFinancial(today),
                repository.getAllEvaluations()
            ) { vehicle, goal, financial, evaluations ->
                val summary = financial ?: DailyFinancialSummary(date = today)
                val advice = GoalEngine.evaluateGoalProgress(goal)
                val isWell = advice.status != GoalEngine.GoalPaceStatus.BEHIND_PACE

                DashboardUiState(
                    vehicle = vehicle,
                    goal = goal,
                    financialSummary = summary,
                    latestEvaluation = evaluations.firstOrNull(),
                    coachingAdvice = advice,
                    isOperatingWell = isWell,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
