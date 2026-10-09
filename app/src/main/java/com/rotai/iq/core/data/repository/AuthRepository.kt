package com.rotai.iq.core.data.repository

import com.rotai.iq.core.network.AuthApiClient
import com.rotai.iq.core.network.AuthResponse
import com.rotai.iq.core.network.ForgotPasswordResult
import com.rotai.iq.core.network.UserSessionProfile
import com.rotai.iq.core.security.AuthSessionManager

enum class SessionStatus {
    AUTHENTICATED,
    NEEDS_REFRESH,
    UNAUTHENTICATED
}

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthResponse>
    suspend fun register(name: String, email: String, password: String, termsAccepted: Boolean): Result<AuthResponse>
    suspend fun refreshSession(): Result<String>
    suspend fun checkSessionStatus(): SessionStatus
    suspend fun forgotPassword(email: String): Result<ForgotPasswordResult>
    suspend fun resetPassword(token: String, newPassword: String): Result<String>
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<String>
    suspend fun verifyEmail(tokenOrEmail: String): Result<String>
    suspend fun logout(): Result<Unit>
    fun getCachedProfile(): UserSessionProfile?
    fun isAuthenticated(): Boolean
    fun isOnboardingCompleted(): Boolean
    fun setOnboardingCompleted(completed: Boolean)
    fun updateServerUrl(url: String)
}

class AuthRepositoryImpl(
    private val apiClient: AuthApiClient,
    private val sessionManager: AuthSessionManager
) : AuthRepository {

    override fun updateServerUrl(url: String) {
        apiClient.setBaseUrl(url)
    }

    override suspend fun login(email: String, password: String): Result<AuthResponse> {
        val result = apiClient.login(email, password)
        if (result.isSuccess) {
            val auth = result.getOrThrow()
            sessionManager.saveSession(auth)
        }
        return result
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        termsAccepted: Boolean
    ): Result<AuthResponse> {
        val result = apiClient.register(
            name = name,
            email = email,
            password = password,
            termsAccepted = termsAccepted
        )
        if (result.isSuccess) {
            val auth = result.getOrThrow()
            sessionManager.saveSession(auth)
        }
        return result
    }

    override suspend fun refreshSession(): Result<String> {
        val refreshToken = sessionManager.getRefreshToken()
        if (refreshToken.isNullOrBlank()) {
            sessionManager.clearSession()
            return Result.failure(Exception("Nenhum refresh token disponível."))
        }

        val result = apiClient.refreshToken(refreshToken)
        if (result.isSuccess) {
            val newAccessToken = result.getOrThrow()
            sessionManager.updateAccessToken(newAccessToken)
        } else {
            sessionManager.clearSession()
        }
        return result
    }

    override suspend fun checkSessionStatus(): SessionStatus {
        if (!sessionManager.isLoggedIn()) {
            return SessionStatus.UNAUTHENTICATED
        }

        if (sessionManager.isAccessTokenExpired()) {
            // Tenta renovação automática e silenciosa
            val refreshResult = refreshSession()
            return if (refreshResult.isSuccess) {
                SessionStatus.AUTHENTICATED
            } else {
                SessionStatus.UNAUTHENTICATED
            }
        }

        return SessionStatus.AUTHENTICATED
    }

    override suspend fun forgotPassword(email: String): Result<ForgotPasswordResult> {
        return apiClient.forgotPassword(email)
    }

    override suspend fun resetPassword(token: String, newPassword: String): Result<String> {
        return apiClient.resetPassword(token, newPassword)
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<String> {
        val token = sessionManager.getAccessToken()
        if (token.isNullOrBlank()) {
            return Result.failure(Exception("Sessão não autenticada. Faça login novamente."))
        }
        return apiClient.changePassword(token, currentPassword, newPassword)
    }

    override suspend fun verifyEmail(tokenOrEmail: String): Result<String> {
        val result = apiClient.verifyEmail(tokenOrEmail)
        if (result.isSuccess) {
            sessionManager.setEmailVerified(true)
        }
        return result
    }

    override suspend fun logout(): Result<Unit> {
        val accessToken = sessionManager.getAccessToken()
        val refreshToken = sessionManager.getRefreshToken()
        val result = apiClient.logout(accessToken, refreshToken)
        sessionManager.clearSession()
        return result
    }

    override fun getCachedProfile(): UserSessionProfile? {
        return sessionManager.getUserProfile()
    }

    override fun isAuthenticated(): Boolean {
        return sessionManager.isLoggedIn()
    }

    override fun isOnboardingCompleted(): Boolean {
        return sessionManager.isOnboardingCompleted()
    }

    override fun setOnboardingCompleted(completed: Boolean) {
        sessionManager.setOnboardingCompleted(completed)
    }
}
