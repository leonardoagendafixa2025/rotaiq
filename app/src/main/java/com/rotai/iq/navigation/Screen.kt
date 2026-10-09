package com.rotai.iq.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Painel", Icons.Default.Speed)
    object Automation : Screen("automation", "Copiloto", Icons.Default.PlayArrow)
    object RideFilter : Screen("ride_filter", "Filtros de Corrida", Icons.Default.Tune)
    object GeoInsights : Screen("insights", "Zonas", Icons.Default.Map)
    object Finance : Screen("finance", "Financeiro", Icons.Default.AccountBalanceWallet)
    object Simulator : Screen("simulator", "Simulador", Icons.Default.Calculate)
    object Vehicle : Screen("vehicle", "Veículo", Icons.Default.DirectionsCar)
    object Goals : Screen("goals", "Metas", Icons.Default.Flag)
    object History : Screen("history", "Histórico", Icons.Default.History)
    object Subscription : Screen("subscription", "Plano Pro", Icons.Default.Star)
    object Privacy : Screen("privacy", "Privacidade", Icons.Default.Security)
    object AdvancedTools : Screen("advanced", "Avançado", Icons.Default.DirectionsCar)
    object Splash : Screen("splash", "Splash", Icons.Default.Speed)
    object Login : Screen("login", "Login", Icons.Default.Security)
    object Register : Screen("register", "Criar Conta", Icons.Default.Security)
    object ForgotPassword : Screen("forgot_password", "Recuperar Senha", Icons.Default.Security)
    object ChangePassword : Screen("change_password", "Alterar Senha", Icons.Default.Lock)
    object Profile : Screen("profile", "Meu Perfil", Icons.Default.Person)
    object Onboarding : Screen("onboarding", "Boas-vindas", Icons.Default.DirectionsCar)
    object Support : Screen("support", "Suporte & Ajuda", Icons.Default.Person)
}
