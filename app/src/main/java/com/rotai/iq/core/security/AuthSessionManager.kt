package com.rotai.iq.core.security

import com.rotai.iq.core.network.AuthResponse
import com.rotai.iq.core.network.UserSessionProfile

/**
 * Gerenciador de Sessão Criptografada do ROTA IQ.
 * Persiste tokens JWT e credenciais de sessão utilizando AES-256-GCM (AndroidSecureStorage).
 * NUNCA armazena senha em texto puro no aparelho.
 */
class AuthSessionManager(
    private val secureStorage: SecureStorage
) {

    companion object {
        private const val KEY_ACCESS_TOKEN = "rotai_jwt_access"
        private const val KEY_REFRESH_TOKEN = "rotai_jwt_refresh"
        private const val KEY_USER_ID = "rotai_user_id"
        private const val KEY_DRIVER_ID = "rotai_driver_id"
        private const val KEY_FULL_NAME = "rotai_full_name"
        private const val KEY_EMAIL = "rotai_email"
        private const val KEY_EMAIL_VERIFIED = "rotai_email_verified"
        private const val KEY_ONBOARDING_DONE = "rotai_onboarding_done"
        private const val KEY_EXPIRES_AT = "rotai_token_expires_at"

        // Tokens de acesso duram 24h por padrão no backend
        private const val ACCESS_TOKEN_VALIDITY_MS = 24 * 60 * 60 * 1000L
    }

    fun saveSession(auth: AuthResponse) {
        val expiresAt = System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY_MS
        secureStorage.saveString(KEY_ACCESS_TOKEN, auth.accessToken)
        secureStorage.saveString(KEY_REFRESH_TOKEN, auth.refreshToken)
        secureStorage.saveString(KEY_USER_ID, auth.userId)
        secureStorage.saveString(KEY_DRIVER_ID, auth.driverId)
        secureStorage.saveString(KEY_FULL_NAME, auth.fullName)
        secureStorage.saveString(KEY_EMAIL, auth.email)
        secureStorage.saveString(KEY_EMAIL_VERIFIED, auth.emailVerified.toString())
        secureStorage.saveString(KEY_EXPIRES_AT, expiresAt.toString())
    }

    fun updateAccessToken(newAccessToken: String) {
        val expiresAt = System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY_MS
        secureStorage.saveString(KEY_ACCESS_TOKEN, newAccessToken)
        secureStorage.saveString(KEY_EXPIRES_AT, expiresAt.toString())
    }

    fun getAccessToken(): String? {
        return secureStorage.getString(KEY_ACCESS_TOKEN)
    }

    fun getRefreshToken(): String? {
        return secureStorage.getString(KEY_REFRESH_TOKEN)
    }

    fun getUserId(): String? {
        return secureStorage.getString(KEY_USER_ID)
    }

    fun getDriverId(): String? {
        return secureStorage.getString(KEY_DRIVER_ID)
    }

    fun getUserEmail(): String? {
        return secureStorage.getString(KEY_EMAIL)
    }

    fun getUserName(): String? {
        return secureStorage.getString(KEY_FULL_NAME)
    }

    fun isEmailVerified(): Boolean {
        return secureStorage.getString(KEY_EMAIL_VERIFIED)?.toBoolean() ?: false
    }

    fun setEmailVerified(verified: Boolean) {
        secureStorage.saveString(KEY_EMAIL_VERIFIED, verified.toString())
    }

    fun isOnboardingCompleted(): Boolean {
        return secureStorage.getString(KEY_ONBOARDING_DONE)?.toBoolean() ?: true
    }

    fun setOnboardingCompleted(completed: Boolean) {
        secureStorage.saveString(KEY_ONBOARDING_DONE, completed.toString())
    }

    fun isLoggedIn(): Boolean {
        val token = getAccessToken()
        val refresh = getRefreshToken()
        return !token.isNullOrBlank() || !refresh.isNullOrBlank()
    }

    fun isAccessTokenExpired(): Boolean {
        val expiresAtStr = secureStorage.getString(KEY_EXPIRES_AT) ?: return true
        val expiresAt = expiresAtStr.toLongOrNull() ?: return true
        // Margem de segurança de 60 segundos
        return System.currentTimeMillis() >= (expiresAt - 60_000L)
    }

    fun getUserProfile(): UserSessionProfile? {
        val uid = getUserId() ?: return null
        val did = getDriverId() ?: return null
        val name = getUserName() ?: "Motorista"
        val email = getUserEmail() ?: ""
        return UserSessionProfile(
            userId = uid,
            driverId = did,
            fullName = name,
            email = email,
            emailVerified = isEmailVerified()
        )
    }

    fun clearSession() {
        secureStorage.remove(KEY_ACCESS_TOKEN)
        secureStorage.remove(KEY_REFRESH_TOKEN)
        secureStorage.remove(KEY_USER_ID)
        secureStorage.remove(KEY_DRIVER_ID)
        secureStorage.remove(KEY_FULL_NAME)
        secureStorage.remove(KEY_EMAIL)
        secureStorage.remove(KEY_EMAIL_VERIFIED)
        secureStorage.remove(KEY_EXPIRES_AT)
    }
}
