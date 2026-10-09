package com.rotai.iq.navigation

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Gerenciador centralizado de Deep Links do ROTA IQ.
 * Intercepta intents nativos e notificações push e redireciona para a tela correspondente.
 */
object DeepLinkManager {
    private const val TAG = "DeepLinkManager"

    private val _deepLinkEvents = MutableSharedFlow<String>(extraBufferCapacity = 5)
    val deepLinkEvents: SharedFlow<String> = _deepLinkEvents.asSharedFlow()

    fun handleDeepLink(rawTarget: String?) {
        if (rawTarget.isNullOrBlank()) return

        Log.d(TAG, "Processando deep link recebido: $rawTarget")
        val resolvedRoute = resolveRoute(rawTarget.trim())
        if (resolvedRoute != null) {
            Log.d(TAG, "Deep link resolvido com sucesso para rota: $resolvedRoute")
            _deepLinkEvents.tryEmit(resolvedRoute)
        } else {
            Log.w(TAG, "Nenhuma rota correspondente encontrada para deep link: $rawTarget")
        }
    }

    fun resolveRoute(raw: String): String? {
        val clean = raw.lowercase()
            .replace("rotaiq://", "")
            .replace("https://rotaiq-puce.vercel.app/", "")
            .replace("https://rota-iq.vercel.app/", "")
            .trimStart('/')

        return when {
            clean.startsWith("subscription") || clean.startsWith("pro") -> Screen.Subscription.route
            clean.startsWith("rides") || clean.startsWith("history") || clean.startsWith("corridas") -> Screen.History.route
            clean.startsWith("simulator") || clean.startsWith("simulador") -> Screen.Simulator.route
            clean.startsWith("goals") || clean.startsWith("metas") -> Screen.Goals.route
            clean.startsWith("vehicle") || clean.startsWith("veiculo") || clean.startsWith("car") -> Screen.Vehicle.route
            clean.startsWith("profile") || clean.startsWith("perfil") -> Screen.Profile.route
            clean.startsWith("change-password") || clean.startsWith("alterar-senha") -> Screen.ChangePassword.route
            clean.startsWith("privacy") || clean.startsWith("lgpd") -> Screen.Privacy.route
            clean.startsWith("automation") || clean.startsWith("copiloto") -> Screen.Automation.route
            clean.startsWith("insights") || clean.startsWith("zonas") -> Screen.GeoInsights.route
            clean.startsWith("filter") || clean.startsWith("filtros") -> Screen.RideFilter.route
            clean.startsWith("finance") || clean.startsWith("financeiro") -> Screen.Finance.route
            clean.startsWith("dashboard") || clean.startsWith("inicio") || clean.startsWith("home") -> Screen.Dashboard.route
            else -> null
        }
    }
}
