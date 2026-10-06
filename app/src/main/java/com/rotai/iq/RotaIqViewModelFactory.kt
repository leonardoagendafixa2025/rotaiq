package com.rotai.iq

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rotai.iq.core.data.repository.RotaIqRepository
import com.rotai.iq.feature.dashboard.DashboardViewModel
import com.rotai.iq.feature.goals.GoalsViewModel
import com.rotai.iq.feature.rides.HistoryViewModel
import com.rotai.iq.feature.rides.RideSimulatorViewModel
import com.rotai.iq.feature.vehicle.VehicleViewModel

class RotaIqViewModelFactory(
    private val repository: RotaIqRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(repository) as T
            }
            modelClass.isAssignableFrom(RideSimulatorViewModel::class.java) -> {
                RideSimulatorViewModel(repository) as T
            }
            modelClass.isAssignableFrom(VehicleViewModel::class.java) -> {
                VehicleViewModel(repository) as T
            }
            modelClass.isAssignableFrom(GoalsViewModel::class.java) -> {
                GoalsViewModel(repository) as T
            }
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                HistoryViewModel(repository) as T
            }
            modelClass.isAssignableFrom(com.rotai.iq.feature.finance.FinancialHubViewModel::class.java) -> {
                com.rotai.iq.feature.finance.FinancialHubViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
