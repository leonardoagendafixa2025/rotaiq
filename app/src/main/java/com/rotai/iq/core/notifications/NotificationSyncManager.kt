package com.rotai.iq.core.notifications

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Gerenciador de Sincronização em Tempo Real de Notificações Push do ROTA IQ.
 * Garante que disparos enviados pelo Painel Administrativo cheguem com SOM e BANNER
 * flutuante mesmo em aparelhos sem Google Play Services configurado no Vercel.
 */
object NotificationSyncManager {

    private const val TAG = "NotificationSyncManager"
    private const val PREFS_NAME = "rotaiq_notifications_cache"
    private const val KEY_SEEN_IDS = "seen_notification_ids"

    private var syncJob: Job? = null

    fun startSync(context: Context) {
        if (syncJob?.isActive == true) return

        syncJob = CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "Iniciando monitoramento de notificações push...")
            while (isActive) {
                try {
                    syncOnce(context.applicationContext)
                } catch (e: Exception) {
                    Log.d(TAG, "Ciclo de sincronização de push: ${e.message}")
                }
                delay(20_000L) // Verifica a cada 20 segundos
            }
        }
    }

    fun stopSync() {
        syncJob?.cancel()
        syncJob = null
    }

    suspend fun syncOnce(context: Context) = withContext(Dispatchers.IO) {
        try {
            val baseUrl = com.rotai.iq.core.network.NetworkConfig.getBaseUrl(context)
            val deviceId = DeviceTokenManager.getOrCreateDeviceId(context)
            val url = URL("$baseUrl/notifications/pending?device_id=$deviceId")

            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 6000
            conn.readTimeout = 6000

            if (conn.responseCode in 200..299) {
                val responseStr = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                conn.disconnect()

                val json = JSONObject(responseStr)
                val notifications = json.optJSONArray("notifications") ?: JSONArray()

                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val seenIds = prefs.getStringSet(KEY_SEEN_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()

                for (i in 0 until notifications.length()) {
                    val item = notifications.getJSONObject(i)
                    val id = item.optString("id")
                    if (id.isBlank() || seenIds.contains(id)) continue

                    val title = item.optString("title", "ROTA IQ")
                    val body = item.optString("body", "")
                    val deepLink = item.optString("deep_link", "rotaiq://home")
                    val type = item.optString("type", "MARKETING")
                    val rawImageUrl = item.optString("image_url", "")
                    val imageUrl = if (rawImageUrl.isNotBlank() && rawImageUrl != "null") rawImageUrl else null

                    val channelId = when (type.uppercase()) {
                        "PROMOCAO", "MARKETING" -> NotificationChannels.CHANNEL_MARKETING
                        "SISTEMA" -> NotificationChannels.CHANNEL_SYSTEM
                        "ENGAJAMENTO" -> NotificationChannels.CHANNEL_RIDES
                        else -> NotificationChannels.CHANNEL_GENERAL
                    }

                    // Dispara notificação nativa com som forte, vibração e banner flutuante
                    NotificationHelper.showHeadsUpNotification(
                        context = context,
                        title = title,
                        body = body,
                        deepLink = deepLink,
                        channelId = channelId,
                        imageUrl = if (imageUrl == "null" || imageUrl.isNullOrBlank()) null else imageUrl,
                        campaignId = id
                    )

                    // Marca como vista e envia confirmação de recebimento para métricas reais
                    seenIds.add(id)
                    prefs.edit().putStringSet(KEY_SEEN_IDS, seenIds).apply()
                    acknowledgeNotification(context, id, deviceId)
                }
            } else {
                conn.disconnect()
            }
        } catch (e: Exception) {
            // Conexão offline ou backend reiniciando
        }
    }

    private fun acknowledgeNotification(context: Context, campaignId: String, deviceId: String) {
        try {
            val baseUrl = com.rotai.iq.core.network.NetworkConfig.getBaseUrl(context)
            val url = URL("$baseUrl/notifications/$campaignId/ack?device_id=$deviceId")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            val code = conn.responseCode
            conn.disconnect()
            Log.d(TAG, "Confirmação de recebimento push $campaignId: HTTP $code")
        } catch (e: Exception) {
            Log.d(TAG, "Falha ao enviar ack de push: ${e.message}")
        }
    }
}
