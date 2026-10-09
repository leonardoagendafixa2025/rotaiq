package com.rotai.iq.core.network

import android.content.Context

/**
 * Configuração definitiva de conectividade do ROTA IQ com o Backend oficial em nuvem.
 * O aplicativo conecta-se exclusivamente ao backend de produção na Vercel:
 * https://rotaiq-puce.vercel.app/api/v1
 */
object NetworkConfig {
    private const val PREFS_NAME = "rota_iq_network_prefs"
    private const val KEY_CUSTOM_BASE_URL = "custom_base_url"

    // URL Oficial e Única de Produção na Nuvem (Vercel)
    const val PROD_DEFAULT_URL = "https://rotaiq-puce.vercel.app/api/v1"

    /**
     * Retorna sempre e exclusivamente a URL oficial da nuvem na Vercel.
     * Limpa preventivamente qualquer resquício de IP local salvo anteriormente no SharedPreferences.
     */
    fun getBaseUrl(context: Context? = null): String {
        if (context != null) {
            try {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                if (prefs.contains(KEY_CUSTOM_BASE_URL)) {
                    prefs.edit().remove(KEY_CUSTOM_BASE_URL).apply()
                }
            } catch (_: Exception) {}
        }
        return PROD_DEFAULT_URL
    }

    fun setCustomBaseUrl(context: Context, url: String) {
        // No-op em produção: o servidor é fixo e exclusivo na nuvem Vercel
    }

    fun getCandidateUrls(context: Context? = null): List<String> {
        return listOf(PROD_DEFAULT_URL)
    }
}

