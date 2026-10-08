package com.rotai.iq

import com.google.common.truth.Truth.assertThat
import com.rotai.iq.core.network.AuthResponse
import com.rotai.iq.core.security.AuthSessionManager
import com.rotai.iq.core.security.SecureStorage
import org.junit.Before
import org.junit.Test

class FakeSecureStorage : SecureStorage {
    private val storage = mutableMapOf<String, String>()

    override fun saveString(key: String, value: String) {
        storage[key] = value
    }

    override fun getString(key: String): String? {
        return storage[key]
    }

    override fun remove(key: String) {
        storage.remove(key)
    }

    override fun clearAll() {
        storage.clear()
    }
}

class AuthSessionManagerTest {

    private lateinit var fakeStorage: FakeSecureStorage
    private lateinit var sessionManager: AuthSessionManager

    @Before
    fun setUp() {
        fakeStorage = FakeSecureStorage()
        sessionManager = AuthSessionManager(fakeStorage)
    }

    @Test
    fun saveSession_persistsTokensAndProfileCorrectly() {
        val auth = AuthResponse(
            accessToken = "jwt_access_12345",
            refreshToken = "jwt_refresh_67890",
            tokenType = "bearer",
            driverId = "drv_100",
            userId = "usr_200",
            fullName = "Ayrton Senna",
            email = "ayrton@rotai.com.br",
            emailVerified = true
        )

        sessionManager.saveSession(auth)

        assertThat(sessionManager.isLoggedIn()).isTrue()
        assertThat(sessionManager.getAccessToken()).isEqualTo("jwt_access_12345")
        assertThat(sessionManager.getRefreshToken()).isEqualTo("jwt_refresh_67890")
        assertThat(sessionManager.getUserId()).isEqualTo("usr_200")
        assertThat(sessionManager.getDriverId()).isEqualTo("drv_100")
        assertThat(sessionManager.getUserName()).isEqualTo("Ayrton Senna")
        assertThat(sessionManager.getUserEmail()).isEqualTo("ayrton@rotai.com.br")
        assertThat(sessionManager.isEmailVerified()).isTrue()
    }

    @Test
    fun updateAccessToken_replacesExistingToken() {
        val auth = AuthResponse(
            accessToken = "old_token",
            refreshToken = "refresh_token",
            driverId = "drv_1",
            userId = "usr_1",
            fullName = "Motorista",
            email = "test@rotai.com.br"
        )
        sessionManager.saveSession(auth)

        sessionManager.updateAccessToken("new_refreshed_token")

        assertThat(sessionManager.getAccessToken()).isEqualTo("new_refreshed_token")
        assertThat(sessionManager.getRefreshToken()).isEqualTo("refresh_token")
    }

    @Test
    fun clearSession_removesAllTokensAndLogsOut() {
        val auth = AuthResponse(
            accessToken = "token_to_remove",
            refreshToken = "refresh_to_remove",
            driverId = "drv_1",
            userId = "usr_1",
            fullName = "Motorista",
            email = "test@rotai.com.br"
        )
        sessionManager.saveSession(auth)
        assertThat(sessionManager.isLoggedIn()).isTrue()

        sessionManager.clearSession()

        assertThat(sessionManager.isLoggedIn()).isFalse()
        assertThat(sessionManager.getAccessToken()).isNull()
        assertThat(sessionManager.getRefreshToken()).isNull()
        assertThat(sessionManager.getUserProfile()).isNull()
    }
}
