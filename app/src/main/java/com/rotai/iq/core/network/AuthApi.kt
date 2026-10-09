package com.rotai.iq.core.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "bearer",
    val driverId: String,
    val userId: String,
    val fullName: String,
    val email: String,
    val emailVerified: Boolean = false
)

data class RefreshTokenResponse(
    val accessToken: String,
    val tokenType: String = "bearer"
)

data class UserSessionProfile(
    val userId: String,
    val driverId: String,
    val fullName: String,
    val email: String,
    val phone: String? = null,
    val city: String = "São Paulo",
    val state: String = "SP",
    val status: String = "ACTIVE",
    val emailVerified: Boolean = false
)

data class ForgotPasswordResult(
    val message: String,
    val resetToken: String? = null
)

class AuthApiClient(
    private var baseUrl: String = NetworkConfig.getBaseUrl()
) {

    fun getBaseUrl(): String = baseUrl

    fun setBaseUrl(newUrl: String) {
        baseUrl = newUrl.trimEnd('/')
    }

    suspend fun register(
        name: String,
        email: String,
        password: String,
        termsAccepted: Boolean = true,
        termsVersion: String = "1.0",
        privacyVersion: String = "1.0"
    ): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("name", name.trim())
                put("email", email.trim().lowercase())
                put("password", password)
                put("terms_accepted", termsAccepted)
                put("terms_version", termsVersion)
                put("privacy_version", privacyVersion)
            }
            val (code, responseBody) = sendPostRequest("/auth/register", payload.toString(), null)
            if (code in 200..299) {
                val json = JSONObject(responseBody)
                Result.success(
                    AuthResponse(
                        accessToken = json.getString("access_token"),
                        refreshToken = json.getString("refresh_token"),
                        tokenType = json.optString("token_type", "bearer"),
                        driverId = json.getString("driver_id"),
                        userId = json.getString("user_id"),
                        fullName = json.getString("full_name"),
                        email = json.getString("email"),
                        emailVerified = json.optBoolean("email_verified", false)
                    )
                )
            } else {
                val errorMsg = parseErrorMessage(responseBody, "Erro ao criar conta.")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(formatNetworkException(e)))
        }
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("email", email.trim().lowercase())
                put("password", password)
            }
            val (code, responseBody) = sendPostRequest("/auth/login", payload.toString(), null)
            if (code in 200..299) {
                val json = JSONObject(responseBody)
                Result.success(
                    AuthResponse(
                        accessToken = json.getString("access_token"),
                        refreshToken = json.getString("refresh_token"),
                        tokenType = json.optString("token_type", "bearer"),
                        driverId = json.getString("driver_id"),
                        userId = json.getString("user_id"),
                        fullName = json.getString("full_name"),
                        email = json.getString("email"),
                        emailVerified = json.optBoolean("email_verified", false)
                    )
                )
            } else {
                val errorMsg = parseErrorMessage(responseBody, "E-mail ou senha incorretos.")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(formatNetworkException(e)))
        }
    }

    suspend fun refreshToken(refreshToken: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("refresh_token", refreshToken)
            }
            val (code, responseBody) = sendPostRequest("/auth/refresh", payload.toString(), null)
            if (code in 200..299) {
                val json = JSONObject(responseBody)
                Result.success(json.getString("access_token"))
            } else {
                Result.failure(Exception("Sessão expirada. Faça login novamente."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(formatNetworkException(e)))
        }
    }

    suspend fun forgotPassword(email: String): Result<ForgotPasswordResult> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("email", email.trim().lowercase())
            }
            val (code, responseBody) = sendPostRequest("/auth/forgot-password", payload.toString(), null)
            if (code in 200..299) {
                val json = JSONObject(responseBody)
                val msg = json.optString("message", "Instruções enviadas com sucesso.")
                val rawToken = json.optString("reset_token", "").trim()
                val token = if (rawToken.isNotEmpty()) rawToken else null
                Result.success(ForgotPasswordResult(message = msg, resetToken = token))
            } else {
                Result.failure(Exception(parseErrorMessage(responseBody, "Erro ao solicitar recuperação.")))
            }
        } catch (e: Exception) {
            Result.failure(Exception(formatNetworkException(e)))
        }
    }

    suspend fun resetPassword(token: String, newPassword: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("token", token.trim())
                put("new_password", newPassword)
            }
            val (code, responseBody) = sendPostRequest("/auth/reset-password", payload.toString(), null)
            if (code in 200..299) {
                val json = JSONObject(responseBody)
                Result.success(json.optString("message", "Senha alterada com sucesso!"))
            } else {
                Result.failure(Exception(parseErrorMessage(responseBody, "Token inválido ou expirado.")))
            }
        } catch (e: Exception) {
            Result.failure(Exception(formatNetworkException(e)))
        }
    }

    suspend fun changePassword(token: String, currentPassword: String, newPassword: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("current_password", currentPassword)
                put("new_password", newPassword)
            }
            val (code, responseBody) = sendPostRequest("/auth/change-password", payload.toString(), token)
            if (code in 200..299) {
                val json = JSONObject(responseBody)
                Result.success(json.optString("message", "Senha alterada com sucesso!"))
            } else {
                Result.failure(Exception(parseErrorMessage(responseBody, "Erro ao alterar senha.")))
            }
        } catch (e: Exception) {
            Result.failure(Exception(formatNetworkException(e)))
        }
    }

    suspend fun verifyEmail(tokenOrEmail: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                if (tokenOrEmail.contains("@")) {
                    put("email", tokenOrEmail.trim().lowercase())
                } else {
                    put("token", tokenOrEmail.trim())
                }
            }
            val (code, responseBody) = sendPostRequest("/auth/verify-email", payload.toString(), null)
            if (code in 200..299) {
                Result.success("E-mail confirmado com sucesso.")
            } else {
                Result.failure(Exception(parseErrorMessage(responseBody, "Falha na confirmação do e-mail.")))
            }
        } catch (e: Exception) {
            Result.failure(Exception(formatNetworkException(e)))
        }
    }

    suspend fun logout(accessToken: String?, refreshToken: String?): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                if (refreshToken != null) put("refresh_token", refreshToken)
            }
            sendPostRequest("/auth/logout", payload.toString(), accessToken)
            Result.success(Unit)
        } catch (_: Exception) {
            Result.success(Unit)
        }
    }

    suspend fun getProfile(accessToken: String): Result<UserSessionProfile> = withContext(Dispatchers.IO) {
        try {
            val (code, responseBody) = executeRequest("GET", "/auth/me", null, accessToken)
            if (code in 200..299) {
                val json = JSONObject(responseBody)
                Result.success(
                    UserSessionProfile(
                        userId = json.getString("user_id"),
                        driverId = json.getString("driver_id"),
                        fullName = json.getString("full_name"),
                        email = json.getString("email"),
                        phone = if (json.has("phone") && !json.isNull("phone")) json.getString("phone") else null,
                        city = json.optString("city", "São Paulo"),
                        state = json.optString("state", "SP"),
                        status = json.optString("status", "ACTIVE"),
                        emailVerified = json.optBoolean("email_verified", false)
                    )
                )
            } else {
                Result.failure(Exception("Não foi possível carregar o perfil do usuário."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(formatNetworkException(e)))
        }
    }

    private fun sendPostRequest(path: String, jsonBody: String, authToken: String?): Pair<Int, String> {
        return executeRequest("POST", path, jsonBody, authToken)
    }

    private fun executeRequest(
        method: String,
        path: String,
        jsonBody: String?,
        authToken: String?
    ): Pair<Int, String> {
        val candidates = NetworkConfig.getCandidateUrls()
        val orderedUrls = mutableListOf<String>()
        if (baseUrl.isNotBlank()) orderedUrls.add(baseUrl)
        for (c in candidates) {
            if (!orderedUrls.contains(c)) orderedUrls.add(c)
        }

        var lastException: Exception? = null

        for (urlCandidate in orderedUrls) {
            val cleanCandidate = urlCandidate.trimEnd('/')
            val cleanPath = if (path.startsWith("/")) path else "/$path"
            val fullUrl = "$cleanCandidate$cleanPath"

            try {
                val url = URL(fullUrl)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = method
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    if (authToken != null) {
                        setRequestProperty("Authorization", "Bearer $authToken")
                    }
                    connectTimeout = 4000
                    readTimeout = 8000
                    if (jsonBody != null) {
                        doOutput = true
                    }
                }

                if (jsonBody != null) {
                    OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(jsonBody) }
                }

                val code = conn.responseCode
                val responseBody = readStream(if (code in 200..299) conn.inputStream else conn.errorStream)
                conn.disconnect()

                // Atualiza baseUrl para a URL funcional
                if (baseUrl != urlCandidate) {
                    baseUrl = urlCandidate
                }
                return Pair(code, responseBody)
            } catch (e: Exception) {
                lastException = e
            }
        }
        throw lastException ?: Exception("Falha ao comunicar com o servidor ROTA IQ.")
    }

    private fun readStream(stream: java.io.InputStream?): String {
        if (stream == null) return ""
        return BufferedReader(InputStreamReader(stream, "UTF-8")).use { it.readText() }
    }

    private fun parseErrorMessage(responseBody: String, defaultMessage: String): String {
        return try {
            val json = JSONObject(responseBody)
            when {
                json.has("detail") -> json.getString("detail")
                json.has("message") -> json.getString("message")
                else -> defaultMessage
            }
        } catch (_: Exception) {
            defaultMessage
        }
    }

    private fun formatNetworkException(e: Exception): String {
        val msg = e.message ?: ""
        return when {
            msg.contains("failed to connect", ignoreCase = true) ||
            msg.contains("Connection refused", ignoreCase = true) ||
            msg.contains("ENETUNREACH", ignoreCase = true) ||
            msg.contains("No route to host", ignoreCase = true) ->
                "Não foi possível conectar aos servidores em nuvem do ROTA IQ. Verifique sua conexão com a internet."
            msg.contains("timeout", ignoreCase = true) ->
                "Tempo limite esgotado ao contatar os servidores do ROTA IQ. Verifique seu sinal de internet e tente novamente."
            else -> msg.ifBlank { "Erro de conexão com o servidor." }
        }
    }
}
