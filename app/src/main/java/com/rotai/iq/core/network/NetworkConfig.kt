package com.rotai.iq.core.network

import android.content.Context
import android.os.Build

/**
 * Configuração de conectividade do ROTA IQ com o Backend oficial.
 * Resolve automaticamente o IP correto conforme o ambiente:
 * - Emulador Android Oficial: 10.0.2.2:8000
 * - Aparelho Físico (Wi-Fi Local): 192.168.100.11:8000
 * - Conexão USB (adb reverse): 127.0.0.1:8000
 * - URL Personalizada salva em SharedPreferences
 */
object NetworkConfig {
    private const val PREFS_NAME = "rota_iq_network_prefs"
    private const val KEY_CUSTOM_BASE_URL = "custom_base_url"

    // IP da máquina host local na rede Wi-Fi
    const val LAN_DEFAULT_HOST = "192.168.100.11"
    const val LAN_DEFAULT_URL = "http://$LAN_DEFAULT_HOST:8000/api/v1"

    // IP do loopback do emulador Android oficial
    const val EMULATOR_DEFAULT_URL = "http://10.0.2.2:8000/api/v1"

    // IP para conexão USB via adb reverse tcp:8000 tcp:8000
    const val ADB_REVERSE_URL = "http://127.0.0.1:8000/api/v1"

    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT)
    }

    fun getBaseUrl(context: Context? = null): String {
        if (context != null) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val custom = prefs.getString(KEY_CUSTOM_BASE_URL, null)
            if (!custom.isNullOrBlank()) {
                return custom.trimEnd('/')
            }
        }
        return if (isEmulator()) EMULATOR_DEFAULT_URL else LAN_DEFAULT_URL
    }

    fun setCustomBaseUrl(context: Context, url: String) {
        val cleanUrl = url.trim().trimEnd('/')
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CUSTOM_BASE_URL, cleanUrl).apply()
    }

    fun getCandidateUrls(context: Context? = null): List<String> {
        val list = mutableListOf<String>()
        val current = getBaseUrl(context)
        list.add(current)
        if (!list.contains(LAN_DEFAULT_URL)) list.add(LAN_DEFAULT_URL)
        if (!list.contains(EMULATOR_DEFAULT_URL)) list.add(EMULATOR_DEFAULT_URL)
        if (!list.contains(ADB_REVERSE_URL)) list.add(ADB_REVERSE_URL)
        return list
    }
}
