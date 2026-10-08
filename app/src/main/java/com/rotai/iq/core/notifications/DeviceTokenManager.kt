package com.rotai.iq.core.notifications

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Gerenciador de Token e Inscrição em Tópicos do Firebase Cloud Messaging.
 * Responsável por enviar o token FCM para o backend ROTA IQ e assinar o tópico global 'rotaiq_all'.
 */
object DeviceTokenManager {

    private const val TAG = "DeviceTokenManager"
    private const val PREFS_NAME = "rotaiq_device_prefs"
    private const val KEY_FCM_TOKEN = "fcm_token"
    private const val DEFAULT_BACKEND_URL = "http://10.0.2.2:8000/api/v1" // 10.0.2.2 mapeia para o localhost no emulador Android

    fun saveLocalToken(context: Context, token: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
    }

    fun getLocalToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_FCM_TOKEN, null)
    }

    /**
     * Envia o token FCM para o backend registrar no banco de dados.
     */
    suspend fun registerTokenWithBackend(
        context: Context,
        token: String,
        userId: String? = null,
        notificationsEnabled: Boolean = true
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val baseUrl = com.rotai.iq.core.network.NetworkConfig.getBaseUrl(context)
            val url = URL("$baseUrl/devices/register")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.doOutput = true

            val payload = JSONObject().apply {
                put("fcm_token", token)
                put("user_id", userId)
                put("platform", "android")
                put("device_id", "${Build.MANUFACTURER}_${Build.MODEL}")
                put("app_version", "1.0.0")
                put("os_version", "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                put("notifications_enabled", notificationsEnabled)
            }

            OutputStreamWriter(conn.outputStream).use { writer ->
                writer.write(payload.toString())
                writer.flush()
            }

            val responseCode = conn.responseCode
            conn.disconnect()
            Log.d(TAG, "Registro de token no backend status: $responseCode")
            responseCode in 200..299
        } catch (e: Exception) {
            Log.w(TAG, "Erro ao registrar token FCM no backend: ${e.message}")
            false
        }
    }

    /**
     * Inscreve o aplicativo no tópico global padrão 'rotaiq_all' para broadcast.
     */
    fun subscribeToGlobalTopic() {
        try {
            FirebaseMessaging.getInstance().subscribeToTopic("rotaiq_all")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Inscrição com sucesso no tópico global 'rotaiq_all'")
                    } else {
                        Log.w(TAG, "Falha ao se inscrever no tópico 'rotaiq_all'")
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseMessaging indisponível ou sem Google Play Services: ${e.message}")
        }
    }

    /**
     * Obtém o token FCM atual e sincroniza com o backend e tópicos.
     */
    fun syncDevice(context: Context, userId: String? = null) {
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    if (!token.isNullOrBlank()) {
                        saveLocalToken(context, token)
                        subscribeToGlobalTopic()
                        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                            registerTokenWithBackend(context, token, userId)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Não foi possível obter token FCM imediatamente: ${e.message}")
        }
    }
}
