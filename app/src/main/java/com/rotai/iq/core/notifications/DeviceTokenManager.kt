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
import java.util.UUID

/**
 * Gerenciador de Token e Inscrição de Dispositivos no ROTA IQ.
 * Garante que todo aparelho seja cadastrado no backend mesmo em APK sideloaded
 * ou ambientes sem Google Play Services / Firebase ativo.
 */
object DeviceTokenManager {

    private const val TAG = "DeviceTokenManager"
    private const val PREFS_NAME = "rotaiq_device_prefs"
    private const val KEY_FCM_TOKEN = "fcm_token"
    private const val KEY_DEVICE_UUID = "device_unique_uuid"

    fun getOrCreateDeviceId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var devId = prefs.getString(KEY_DEVICE_UUID, null)
        if (devId.isNullOrBlank()) {
            devId = "device_${UUID.randomUUID().toString().replace("-", "").take(16)}"
            prefs.edit().putString(KEY_DEVICE_UUID, devId).apply()
        }
        return devId
    }

    fun saveLocalToken(context: Context, token: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
    }

    fun getLocalToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_FCM_TOKEN, null)
    }

    /**
     * Envia o token FCM ou o token do dispositivo para o backend registrar no banco de dados.
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

            val deviceId = getOrCreateDeviceId(context)
            val modelName = "${Build.MANUFACTURER} ${Build.MODEL}".trim()

            val payload = JSONObject().apply {
                put("fcm_token", token)
                put("user_id", userId)
                put("platform", "android")
                put("device_id", "$modelName ($deviceId)")
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
     * Possui fallback resiliente: registra imediatamente o dispositivo mesmo se o Firebase
     * não estiver inicializado ou falhar.
     */
    fun syncDevice(context: Context, userId: String? = null) {
        val deviceId = getOrCreateDeviceId(context)
        val initialToken = getLocalToken(context) ?: "rotai_dev_$deviceId"

        // 1. Registro imediato com o backend para garantir contagem em 'Dispositivos elegíveis'
        CoroutineScope(Dispatchers.IO).launch {
            registerTokenWithBackend(context, initialToken, userId)
        }

        // 2. Tenta capturar o token real do Firebase FCM se o Google Services estiver configurado
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    if (!token.isNullOrBlank()) {
                        saveLocalToken(context, token)
                        subscribeToGlobalTopic()
                        CoroutineScope(Dispatchers.IO).launch {
                            registerTokenWithBackend(context, token, userId)
                        }
                    }
                } else {
                    Log.d(TAG, "FCM token pendente ou Play Services ausente: ${task.exception?.message}")
                }
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Firebase FCM não configurado ou Play Services indisponível: ${e.message}")
        }
    }
}
