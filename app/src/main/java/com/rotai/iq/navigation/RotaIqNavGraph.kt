package com.rotai.iq.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rotai.iq.RotaIqViewModelFactory
import com.rotai.iq.core.ui.theme.BrandPrimary
import com.rotai.iq.core.ui.theme.CockpitBackground
import com.rotai.iq.core.ui.theme.CockpitSurface
import com.rotai.iq.core.ui.theme.TextSecondary
import com.rotai.iq.feature.automation.AutomationHubScreen
import com.rotai.iq.feature.automation.AutomationViewModel
import com.rotai.iq.feature.dashboard.DashboardScreen
import com.rotai.iq.feature.dashboard.DashboardViewModel
import com.rotai.iq.feature.finance.FinancialHubScreen
import com.rotai.iq.feature.finance.FinancialHubViewModel
import com.rotai.iq.feature.goals.GoalsScreen
import com.rotai.iq.feature.goals.GoalsViewModel
import com.rotai.iq.feature.rides.HistoryViewModel
import com.rotai.iq.feature.rides.RideHistoryScreen
import com.rotai.iq.feature.rides.RideSimulatorScreen
import com.rotai.iq.feature.rides.RideSimulatorViewModel
import com.rotai.iq.feature.vehicle.VehicleScreen
import com.rotai.iq.feature.vehicle.VehicleViewModel

@Composable
fun RotaIqApp(
    viewModelFactory: RotaIqViewModelFactory,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val screens = listOf(
        Screen.Dashboard,
        Screen.Automation,
        Screen.Finance,
        Screen.Simulator,
        Screen.Vehicle,
        Screen.History
    )

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(
                containerColor = CockpitSurface,
                tonalElevation = 8.dp
            ) {
                screens.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 10.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CockpitBackground,
                            selectedTextColor = BrandPrimary,
                            indicatorColor = BrandPrimary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
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
                    }
                )
            }
            composable(Screen.Automation.route) {
                val vm: AutomationViewModel = viewModel(factory = viewModelFactory)
                AutomationHubScreen(viewModel = vm)
            }
            composable(Screen.Finance.route) {
                val vm: FinancialHubViewModel = viewModel(factory = viewModelFactory)
                FinancialHubScreen(viewModel = vm)
            }
            composable(Screen.Simulator.route) {
                val vm: RideSimulatorViewModel = viewModel(factory = viewModelFactory)
                RideSimulatorScreen(viewModel = vm)
            }
            composable(Screen.Vehicle.route) {
                val vm: VehicleViewModel = viewModel(factory = viewModelFactory)
                VehicleScreen(viewModel = vm)
            }
            composable(Screen.Goals.route) {
                val vm: GoalsViewModel = viewModel(factory = viewModelFactory)
                GoalsScreen(viewModel = vm)
            }
            composable(Screen.History.route) {
                val vm: HistoryViewModel = viewModel(factory = viewModelFactory)
                RideHistoryScreen(viewModel = vm)
            }
        }
    }
}
