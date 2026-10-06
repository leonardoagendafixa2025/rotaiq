package com.rotai.iq.feature.rides

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rotai.iq.core.data.repository.RotaIqRepository
import com.rotai.iq.core.domain.model.RideEvaluation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryUiState(
    val evaluations: List<RideEvaluation> = emptyList(),
    val totalCount: Int = 0,
    val totalSimulatedProfit: Double = 0.0,
    val averageScore: Double = 0.0,
    val isLoading: Boolean = true
)

class HistoryViewModel(
    private val repository: RotaIqRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            repository.getAllEvaluations().collect { list ->
                val totalProfit = list.sumOf { it.netProfit }
                val avgScore = if (list.isNotEmpty()) list.map { it.score }.average() else 0.0

                _uiState.value = HistoryUiState(
                    evaluations = list,
                    totalCount = list.size,
                    totalSimulatedProfit = totalProfit,
                    averageScore = avgScore,
                    isLoading = false
                )
            }
        }
    }

    fun deleteEvaluation(id: String) {
        viewModelScope.launch {
            repository.deleteEvaluation(id)
        }
    }
}
