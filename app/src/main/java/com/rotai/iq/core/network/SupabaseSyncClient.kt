package com.rotai.iq.core.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class RemotePlan(
    val code: String,
    val name: String,
    val priceCents: Int,
    val interval: String
)

class SupabaseSyncClient(
    private val baseUrl: String = SupabaseConfig.REST_ENDPOINT,
    private val apiKey: String = SupabaseConfig.PUBLISHABLE_KEY
) {

    suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/subscription_plans?select=code&limit=1")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("apikey", apiKey)
                setRequestProperty("Authorization", "Bearer $apiKey")
                connectTimeout = 5000
                readTimeout = 5000
            }
            val code = conn.responseCode
            conn.disconnect()
            code in 200..299
        } catch (_: Exception) {
            false
        }
    }

    suspend fun fetchSubscriptionPlans(): List<RemotePlan> = withContext(Dispatchers.IO) {
        val plans = mutableListOf<RemotePlan>()
        try {
            val url = URL("$baseUrl/subscription_plans?select=*&is_active=eq.true&order=price_cents.asc")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("apikey", apiKey)
                setRequestProperty("Authorization", "Bearer $apiKey")
                connectTimeout = 8000
                readTimeout = 8000
            }

            if (conn.responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val responseText = reader.readText()
                reader.close()

                val jsonArray = JSONArray(responseText)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    plans.add(
                        RemotePlan(
                            code = obj.optString("code"),
                            name = obj.optString("name"),
                            priceCents = obj.optInt("price_cents"),
                            interval = obj.optString("interval")
                        )
                    )
                }
            }
            conn.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        plans
    }
}
