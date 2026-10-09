package com.rotai.iq.core.network

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class VersionCheckResult(
    val forceUpdate: Boolean = false,
    val recommendedUpdate: Boolean = false,
    val latestVersion: String = "1.0.0",
    val minVersion: String = "1.0.0",
    val updateUrl: String = "https://rotaiq-puce.vercel.app/download",
    val releaseNotes: String = ""
)

object AppUpdateChecker {
    private const val CURRENT_APP_VERSION = "1.0.0"

    suspend fun checkVersion(context: Context? = null): VersionCheckResult = withContext(Dispatchers.IO) {
        try {
            val baseUrl = NetworkConfig.getBaseUrl(context)
            val urlString = "$baseUrl/app/version-check?current_version=$CURRENT_APP_VERSION&platform=android"
            val url = URL(urlString)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("Accept", "application/json")
            }

            if (conn.responseCode in 200..299) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                VersionCheckResult(
                    forceUpdate = json.optBoolean("force_update", false),
                    recommendedUpdate = json.optBoolean("recommended_update", false),
                    latestVersion = json.optString("latest_version", CURRENT_APP_VERSION),
                    minVersion = json.optString("min_supported_version", CURRENT_APP_VERSION),
                    updateUrl = json.optString("update_url", "https://rotaiq-puce.vercel.app/download"),
                    releaseNotes = json.optString("release_notes", "")
                )
            } else {
                VersionCheckResult()
            }
        } catch (_: Exception) {
            VersionCheckResult()
        }
    }
}
