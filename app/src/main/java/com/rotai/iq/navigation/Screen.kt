package com.rotai.iq.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Painel", Icons.Default.Speed)
    object Automation : Screen("automation", "Copiloto", Icons.Default.PlayArrow)
    object GeoInsights : Screen("insights", "Zonas", Icons.Default.Map)
    object Finance : Screen("finance", "Financeiro", Icons.Default.AccountBalanceWallet)
    object Simulator : Screen("simulator", "Simulador", Icons.Default.Calculate)
    object Vehicle : Screen("vehicle", "Veículo", Icons.Default.DirectionsCar)
    object Goals : Screen("goals", "Metas", Icons.Default.Flag)
    object History : Screen("history", "Histórico", Icons.Default.History)
}
