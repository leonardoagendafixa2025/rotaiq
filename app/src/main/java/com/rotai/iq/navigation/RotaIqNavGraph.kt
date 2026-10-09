package com.rotai.iq.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rotai.iq.RotaIqViewModelFactory
import com.rotai.iq.core.ui.designsystem.RotaBottomBar
import com.rotai.iq.core.ui.designsystem.RotaNavItem
import com.rotai.iq.core.ui.theme.RotaBlack
import com.rotai.iq.feature.advanced.AdvancedToolsScreen
import com.rotai.iq.feature.advanced.AdvancedToolsViewModel
import com.rotai.iq.feature.automation.AutomationHubScreen
import com.rotai.iq.feature.automation.AutomationViewModel
import com.rotai.iq.feature.dashboard.DashboardScreen
import com.rotai.iq.feature.dashboard.DashboardViewModel
import com.rotai.iq.feature.filter.RideFilterScreen
import com.rotai.iq.feature.filter.RideFilterViewModel
import com.rotai.iq.feature.finance.FinancialHubScreen
import com.rotai.iq.feature.finance.FinancialHubViewModel
import com.rotai.iq.feature.geographic.GeoInsightsScreen
import com.rotai.iq.feature.geographic.GeoInsightsViewModel
import com.rotai.iq.feature.goals.GoalsScreen
import com.rotai.iq.feature.goals.GoalsViewModel
import com.rotai.iq.feature.privacy.PrivacySettingsScreen
import com.rotai.iq.feature.privacy.PrivacySettingsViewModel
import com.rotai.iq.feature.rides.HistoryViewModel
import com.rotai.iq.feature.rides.RideHistoryScreen
import com.rotai.iq.feature.rides.RideSimulatorScreen
import com.rotai.iq.feature.rides.RideSimulatorViewModel
import com.rotai.iq.feature.subscription.SubscriptionPaywallScreen
import com.rotai.iq.feature.subscription.SubscriptionPaywallViewModel
import com.rotai.iq.feature.vehicle.VehicleScreen
import com.rotai.iq.feature.vehicle.VehicleViewModel
import com.rotai.iq.feature.auth.AuthViewModel
import com.rotai.iq.feature.auth.ForgotPasswordScreen
import com.rotai.iq.feature.auth.LoginScreen
import com.rotai.iq.feature.auth.RegisterScreen
import com.rotai.iq.feature.splash.SplashScreen
import com.rotai.iq.feature.splash.SplashViewModel

@Composable
fun RotaIqApp(
    viewModelFactory: RotaIqViewModelFactory,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Rotas públicas (não exigem autenticação e não exibem a barra de navegação)
    val authRoutes = setOf(
        Screen.Splash.route,
        Screen.Login.route,
        Screen.Register.route,
        Screen.ForgotPassword.route
    )
    val shouldShowBottomBar = currentRoute !in authRoutes

    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as? com.rotai.iq.RotaIqApplication
    val isAuthenticated = app?.authRepository?.isAuthenticated() ?: false

    // Proteção Obrigatória de Rotas: Redireciona usuário não autenticado para o Login
    if (currentRoute != null && currentRoute !in authRoutes && !isAuthenticated) {
        androidx.compose.runtime.LaunchedEffect(currentRoute) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // Barra de navegação inspirada 1:1 na referência visual:
    // Início, Corridas, Botão Central '+' de Ação Rápida (Simulador), Financeiro, Perfil
    val navItems = listOf(
        RotaNavItem(
            route = Screen.Dashboard.route,
            label = "INÍCIO",
            icon = Icons.Default.Home
        ),
        RotaNavItem(
            route = Screen.History.route,
            label = "CORRIDAS",
            icon = Icons.Default.History
        ),
        RotaNavItem(
            route = Screen.Simulator.route,
            label = "",
            icon = Icons.Default.Add,
            isPrimaryAction = true
        ),
        RotaNavItem(
            route = Screen.Finance.route,
            label = "FINANCEIRO",
            icon = Icons.Default.AccountBalanceWallet
        ),
        RotaNavItem(
            route = Screen.Vehicle.route,
            label = "PERFIL",
            icon = Icons.Default.DirectionsCar
        )
    )

    Scaffold(
        modifier = modifier,
        containerColor = RotaBlack,
        bottomBar = {
            if (shouldShowBottomBar) {
                RotaBottomBar(
                    items = navItems,
                    currentRoute = currentRoute,
                    onItemSelected = { route ->
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                val vm: DashboardViewModel = viewModel(factory = viewModelFactory)
                DashboardScreen(
                    viewModel = vm,
                    onNavigateToSimulator = {
                        navController.navigate(Screen.Simulator.route)
                    },
                    onNavigateToVehicle = {
                        navController.navigate(Screen.Vehicle.route)
                    },
                    onNavigateToSubscription = {
                        navController.navigate(Screen.Subscription.route)
                    },
                    onNavigateToPrivacy = {
                        navController.navigate(Screen.Privacy.route)
                    },
                    onNavigateToAdvanced = {
                        navController.navigate(Screen.AdvancedTools.route)
                    },
                    onNavigateToAutomation = {
                        navController.navigate(Screen.Automation.route)
                    }
                )
            }
            composable(Screen.History.route) {
                val vm: HistoryViewModel = viewModel(factory = viewModelFactory)
                RideHistoryScreen(
                    viewModel = vm,
                    onNavigateToSimulator = {
                        navController.navigate(Screen.Simulator.route)
                    }
                )
            }
            composable(Screen.RideFilter.route) {
                val vm: RideFilterViewModel = viewModel(factory = viewModelFactory)
                RideFilterScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.GeoInsights.route) {
                val vm: GeoInsightsViewModel = viewModel(factory = viewModelFactory)
                GeoInsightsScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Finance.route) {
                val vm: FinancialHubViewModel = viewModel(factory = viewModelFactory)
                FinancialHubScreen(viewModel = vm)
            }
            composable(Screen.Vehicle.route) {
                val vm: VehicleViewModel = viewModel(factory = viewModelFactory)
                val authVm: AuthViewModel = viewModel(factory = viewModelFactory)
                VehicleScreen(
                    viewModel = vm,
                    authViewModel = authVm,
                    onNavigateToGoals = {
                        navController.navigate(Screen.Goals.route)
                    },
                    onNavigateToRideFilter = {
                        navController.navigate(Screen.RideFilter.route)
                    },
                    onNavigateToPreferences = {
                        navController.navigate(Screen.AdvancedTools.route)
                    },
                    onNavigateToZones = {
                        navController.navigate(Screen.RideFilter.route)
                    },
                    onNavigateToAutomation = {
                        navController.navigate(Screen.Automation.route)
                    },
                    onNavigateToPrivacy = {
                        navController.navigate(Screen.Privacy.route)
                    },
                    onNavigateToSubscription = {
                        navController.navigate(Screen.Subscription.route)
                    },
                    onLogout = {
                        authVm.executeLogout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Automation.route) {
                val vm: AutomationViewModel = viewModel(factory = viewModelFactory)
                AutomationHubScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Simulator.route) {
                val vm: RideSimulatorViewModel = viewModel(factory = viewModelFactory)
                RideSimulatorScreen(viewModel = vm)
            }
            composable(Screen.Goals.route) {
                val vm: GoalsViewModel = viewModel(factory = viewModelFactory)
                GoalsScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Subscription.route) {
                val vm: SubscriptionPaywallViewModel = viewModel(factory = viewModelFactory)
                SubscriptionPaywallScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Privacy.route) {
                val vm: PrivacySettingsViewModel = viewModel(factory = viewModelFactory)
                PrivacySettingsScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AdvancedTools.route) {
                val vm: AdvancedToolsViewModel = viewModel(factory = viewModelFactory)
                AdvancedToolsScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Splash.route) {
                val vm: SplashViewModel = viewModel(factory = viewModelFactory)
                SplashScreen(
                    viewModel = vm,
                    onNavigate = { destination ->
                        navController.navigate(destination) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Login.route) {
                val vm: AuthViewModel = viewModel(factory = viewModelFactory)
                LoginScreen(
                    viewModel = vm,
                    onNavigateToHome = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(Screen.ForgotPassword.route)
                    }
                )
            }
            composable(Screen.Register.route) {
                val vm: AuthViewModel = viewModel(factory = viewModelFactory)
                RegisterScreen(
                    viewModel = vm,
                    onNavigateToHome = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateBackToLogin = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.ForgotPassword.route) {
                val vm: AuthViewModel = viewModel(factory = viewModelFactory)
                ForgotPasswordScreen(
                    viewModel = vm,
                    onNavigateBackToLogin = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
