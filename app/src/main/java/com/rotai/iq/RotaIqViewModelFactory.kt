package com.rotai.iq

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rotai.iq.core.data.repository.RotaIqRepository
import com.rotai.iq.feature.advanced.AdvancedToolsViewModel
import com.rotai.iq.feature.automation.AutomationViewModel
import com.rotai.iq.feature.dashboard.DashboardViewModel
import com.rotai.iq.feature.filter.RideFilterViewModel
import com.rotai.iq.feature.finance.FinancialHubViewModel
import com.rotai.iq.feature.geographic.GeoInsightsViewModel
import com.rotai.iq.feature.goals.GoalsViewModel
import com.rotai.iq.feature.privacy.PrivacySettingsViewModel
import com.rotai.iq.feature.rides.HistoryViewModel
import com.rotai.iq.feature.rides.RideSimulatorViewModel
import com.rotai.iq.feature.subscription.SubscriptionPaywallViewModel
import com.rotai.iq.feature.vehicle.VehicleViewModel
import com.rotai.iq.feature.auth.AuthViewModel
import com.rotai.iq.feature.splash.SplashViewModel

class RotaIqViewModelFactory(
    private val repository: RotaIqRepository,
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val app = application as? RotaIqApplication
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(repository) as T
            }
            modelClass.isAssignableFrom(RideSimulatorViewModel::class.java) -> {
                RideSimulatorViewModel(repository) as T
            }
            modelClass.isAssignableFrom(VehicleViewModel::class.java) -> {
                VehicleViewModel(repository, app?.commercialRepository) as T
            }
            modelClass.isAssignableFrom(GoalsViewModel::class.java) -> {
                GoalsViewModel(repository) as T
            }
            modelClass.isAssignableFrom(RideFilterViewModel::class.java) -> {
                RideFilterViewModel(repository) as T
            }
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                HistoryViewModel(repository) as T
            }
            modelClass.isAssignableFrom(FinancialHubViewModel::class.java) -> {
                FinancialHubViewModel(repository) as T
            }
            modelClass.isAssignableFrom(AutomationViewModel::class.java) -> {
                AutomationViewModel(application) as T
            }
            modelClass.isAssignableFrom(GeoInsightsViewModel::class.java) -> {
                GeoInsightsViewModel(repository) as T
            }
            modelClass.isAssignableFrom(SubscriptionPaywallViewModel::class.java) -> {
                if (app != null) {
                    SubscriptionPaywallViewModel(
                        commercialRepository = app.commercialRepository,
                        billingManager = app.billingManager,
                        pixPaymentManager = app.pixPaymentManager,
                        telemetryManager = app.telemetryManager
                    ) as T
                } else {
                    throw IllegalStateException("Application must be RotaIqApplication")
                }
            }
            modelClass.isAssignableFrom(PrivacySettingsViewModel::class.java) -> {
                if (app != null) {
                    PrivacySettingsViewModel(
                        commercialRepository = app.commercialRepository,
                        rotaIqRepository = repository,
                        lgpdManager = app.lgpdManager,
                        telemetryManager = app.telemetryManager
                    ) as T
                } else {
                    throw IllegalStateException("Application must be RotaIqApplication")
                }
            }
            modelClass.isAssignableFrom(AdvancedToolsViewModel::class.java) -> {
                if (app != null) {
                    AdvancedToolsViewModel(
                        repository = repository,
                        carModeManager = app.carModeManager
                    ) as T
                } else {
                    throw IllegalStateException("Application must be RotaIqApplication")
                }
            }
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                if (app != null) {
                    AuthViewModel(app.authRepository) as T
                } else {
                    throw IllegalStateException("Application must be RotaIqApplication")
                }
            }
            modelClass.isAssignableFrom(SplashViewModel::class.java) -> {
                if (app != null) {
                    SplashViewModel(app.authRepository) as T
                } else {
                    throw IllegalStateException("Application must be RotaIqApplication")
                }
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
