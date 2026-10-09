package com.rotai.iq

import com.google.common.truth.Truth.assertThat
import com.rotai.iq.core.data.repository.AuthRepository
import com.rotai.iq.core.data.repository.SessionStatus
import com.rotai.iq.core.network.AuthResponse
import com.rotai.iq.core.network.UserSessionProfile
import com.rotai.iq.core.network.ForgotPasswordResult
import com.rotai.iq.feature.auth.AuthNavEvent
import com.rotai.iq.feature.auth.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class FakeAuthRepository : AuthRepository {
    var shouldFailLogin = false
    var shouldFailRegister = false
    var registeredUsers = mutableMapOf<String, String>()
    var loggedIn = false
    var currentStatus = SessionStatus.UNAUTHENTICATED

    override suspend fun login(email: String, password: String): Result<AuthResponse> {
        if (shouldFailLogin) {
            return Result.failure(Exception("E-mail ou senha incorretos."))
        }
        loggedIn = true
        currentStatus = SessionStatus.AUTHENTICATED
        return Result.success(
            AuthResponse(
                accessToken = "valid_access_token",
                refreshToken = "valid_refresh_token",
                driverId = "drv_1",
                userId = "usr_1",
                fullName = "Piloto de Teste",
                email = email
            )
        )
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        termsAccepted: Boolean
    ): Result<AuthResponse> {
        if (shouldFailRegister) {
            return Result.failure(Exception("Já existe uma conta cadastrada com este e-mail."))
        }
        registeredUsers[email] = password
        loggedIn = true
        currentStatus = SessionStatus.AUTHENTICATED
        return Result.success(
            AuthResponse(
                accessToken = "valid_access_token",
                refreshToken = "valid_refresh_token",
                driverId = "drv_1",
                userId = "usr_1",
                fullName = name,
                email = email
            )
        )
    }

    override suspend fun refreshSession(): Result<String> {
        return Result.success("new_token")
    }

    override suspend fun checkSessionStatus(): SessionStatus {
        return currentStatus
    }

    override suspend fun forgotPassword(email: String): Result<ForgotPasswordResult> {
        return Result.success(ForgotPasswordResult("Instruções de recuperação enviadas.", "fake_token_reset_999"))
    }

    override suspend fun resetPassword(token: String, newPassword: String): Result<String> {
        return Result.success("Senha alterada com sucesso!")
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<String> {
        return if (currentPassword == "SenhaAtual123") {
            Result.success("Senha alterada com sucesso!")
        } else {
            Result.failure(Exception("Senha atual informada está incorreta."))
        }
    }

    override suspend fun verifyEmail(tokenOrEmail: String): Result<String> {
        return Result.success("E-mail confirmado!")
    }

    override suspend fun logout(): Result<Unit> {
        loggedIn = false
        currentStatus = SessionStatus.UNAUTHENTICATED
        return Result.success(Unit)
    }

    override fun getCachedProfile(): UserSessionProfile? {
        return null
    }

    override fun isAuthenticated(): Boolean {
        return loggedIn
    }

    override fun isOnboardingCompleted(): Boolean {
        return true
    }

    override fun setOnboardingCompleted(completed: Boolean) {}

    override fun updateServerUrl(url: String) {}
}

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeAuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeAuthRepository()
        viewModel = AuthViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun login_withInvalidEmail_showsErrorMessage() = runTest {
        viewModel.onLoginEmailChanged("invalid-email")
        viewModel.onLoginPasswordChanged("password123")

        viewModel.executeLogin()

        val state = viewModel.loginState.value
        assertThat(state.errorMessage).isEqualTo("Formato de e-mail inválido.")
    }

    @Test
    fun login_withEmptyPassword_showsErrorMessage() = runTest {
        viewModel.onLoginEmailChanged("user@rotai.com.br")
        viewModel.onLoginPasswordChanged("")

        viewModel.executeLogin()

        val state = viewModel.loginState.value
        assertThat(state.errorMessage).isEqualTo("Por favor, digite sua senha.")
    }

    @Test
    fun login_withValidCredentials_emitsNavigateToHome() = runTest {
        viewModel.onLoginEmailChanged("user@rotai.com.br")
        viewModel.onLoginPasswordChanged("correct_password")

        var navEvent: AuthNavEvent? = null
        val job = launch {
            viewModel.navEvents.collect { navEvent = it }
        }

        viewModel.executeLogin()
        advanceUntilIdle()

        assertThat(navEvent).isEqualTo(AuthNavEvent.NavigateToHome)
        assertThat(viewModel.loginState.value.errorMessage).isNull()
        job.cancel()
    }

    @Test
    fun login_whenCredentialsWrong_showsFriendlyError() = runTest {
        fakeRepository.shouldFailLogin = true
        viewModel.onLoginEmailChanged("user@rotai.com.br")
        viewModel.onLoginPasswordChanged("wrong_pass")

        viewModel.executeLogin()
        advanceUntilIdle()

        val state = viewModel.loginState.value
        assertThat(state.errorMessage).isEqualTo("E-mail ou senha incorretos.")
    }

    @Test
    fun register_withShortPassword_showsErrorMessage() = runTest {
        viewModel.onRegisterNameChanged("Piloto")
        viewModel.onRegisterEmailChanged("piloto@rotai.com.br")
        viewModel.onRegisterPasswordChanged("1234567") // 7 caracteres < 8
        viewModel.onRegisterConfirmPasswordChanged("1234567")
        viewModel.onTermsAcceptedChanged(true)

        viewModel.executeRegister()

        val state = viewModel.registerState.value
        assertThat(state.errorMessage).isEqualTo("A senha deve ter no mínimo 8 caracteres.")
    }

    @Test
    fun register_withMismatchedPasswords_showsErrorMessage() = runTest {
        viewModel.onRegisterNameChanged("Piloto")
        viewModel.onRegisterEmailChanged("piloto@rotai.com.br")
        viewModel.onRegisterPasswordChanged("Password123")
        viewModel.onRegisterConfirmPasswordChanged("DifferentPassword123")
        viewModel.onTermsAcceptedChanged(true)

        viewModel.executeRegister()

        val state = viewModel.registerState.value
        assertThat(state.errorMessage).isEqualTo("A senha e a confirmação devem ser iguais.")
    }

    @Test
    fun register_withoutTermsAcceptance_showsErrorMessage() = runTest {
        viewModel.onRegisterNameChanged("Piloto")
        viewModel.onRegisterEmailChanged("piloto@rotai.com.br")
        viewModel.onRegisterPasswordChanged("Password123")
        viewModel.onRegisterConfirmPasswordChanged("Password123")
        viewModel.onTermsAcceptedChanged(false)

        viewModel.executeRegister()

        val state = viewModel.registerState.value
        assertThat(state.errorMessage).isEqualTo("Você deve concordar com os Termos de Uso e Política de Privacidade.")
    }

    @Test
    fun register_withValidData_emitsNavigateToHome() = runTest {
        viewModel.onRegisterNameChanged("Piloto Ayrton")
        viewModel.onRegisterEmailChanged("ayrton@rotai.com.br")
        viewModel.onRegisterPasswordChanged("SenhaForte123")
        viewModel.onRegisterConfirmPasswordChanged("SenhaForte123")
        viewModel.onTermsAcceptedChanged(true)

        var navEvent: AuthNavEvent? = null
        val job = launch {
            viewModel.navEvents.collect { navEvent = it }
        }

        viewModel.executeRegister()
        advanceUntilIdle()

        assertThat(navEvent).isEqualTo(AuthNavEvent.NavigateToHome)
        assertThat(viewModel.registerState.value.successMessage).contains("link de confirmação")
        job.cancel()
    }

    @Test
    fun forgotPassword_withValidEmail_populatesTokenAndMessage() = runTest {
        viewModel.onForgotEmailChanged("motorista@rotai.com.br")
        viewModel.requestPasswordReset()
        advanceUntilIdle()

        val state = viewModel.forgotState.value
        assertThat(state.isCodeSent).isTrue()
        assertThat(state.token).isEqualTo("fake_token_reset_999")
        assertThat(state.infoMessage).isEqualTo("Instruções de recuperação enviadas.")
    }

    @Test
    fun changePassword_validationAndSuccess() = runTest {
        // 1. Senha atual vazia
        viewModel.onChangeCurrentPasswordChanged("")
        viewModel.onChangeNewPasswordChanged("NovaSenhaForte123")
        viewModel.onChangeConfirmPasswordChanged("NovaSenhaForte123")
        viewModel.executeChangePassword()
        assertThat(viewModel.changePasswordState.value.errorMessage).isEqualTo("Informe sua senha atual.")

        // 2. Nova senha muito curta
        viewModel.onChangeCurrentPasswordChanged("SenhaAtual123")
        viewModel.onChangeNewPasswordChanged("curta")
        viewModel.onChangeConfirmPasswordChanged("curta")
        viewModel.executeChangePassword()
        assertThat(viewModel.changePasswordState.value.errorMessage).isEqualTo("A nova senha deve possuir no mínimo 8 caracteres.")

        // 3. Nova senha igual à atual
        viewModel.onChangeCurrentPasswordChanged("SenhaAtual123")
        viewModel.onChangeNewPasswordChanged("SenhaAtual123")
        viewModel.onChangeConfirmPasswordChanged("SenhaAtual123")
        viewModel.executeChangePassword()
        assertThat(viewModel.changePasswordState.value.errorMessage).isEqualTo("A nova senha deve ser diferente da senha atual.")

        // 4. Confirmação diferente
        viewModel.onChangeCurrentPasswordChanged("SenhaAtual123")
        viewModel.onChangeNewPasswordChanged("NovaSenhaForte123")
        viewModel.onChangeConfirmPasswordChanged("OutraSenhaForte456")
        viewModel.executeChangePassword()
        assertThat(viewModel.changePasswordState.value.errorMessage).isEqualTo("A confirmação de senha não confere.")

        // 5. Sucesso
        viewModel.onChangeConfirmPasswordChanged("NovaSenhaForte123")
        var callbackSuccessCalled = false
        viewModel.executeChangePassword { callbackSuccessCalled = true }
        advanceUntilIdle()

        val state = viewModel.changePasswordState.value
        assertThat(state.successMessage).isEqualTo("Senha alterada com sucesso!")
        assertThat(callbackSuccessCalled).isTrue()
    }
}
