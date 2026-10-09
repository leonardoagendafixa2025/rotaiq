package com.rotai.iq.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rotai.iq.core.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val termsAccepted: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

data class ForgotPasswordUiState(
    val email: String = "",
    val token: String = "",
    val newPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isCodeSent: Boolean = false,
    val isLoading: Boolean = false,
    val infoMessage: String? = null,
    val errorMessage: String? = null
)

sealed class AuthNavEvent {
    object NavigateToHome : AuthNavEvent()
    object NavigateToLogin : AuthNavEvent()
    object NavigateToRegister : AuthNavEvent()
    object NavigateToForgotPassword : AuthNavEvent()
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow(RegisterUiState())
    val registerState: StateFlow<RegisterUiState> = _registerState.asStateFlow()

    private val _forgotState = MutableStateFlow(ForgotPasswordUiState())
    val forgotState: StateFlow<ForgotPasswordUiState> = _forgotState.asStateFlow()

    private val _navEvents = MutableSharedFlow<AuthNavEvent>()
    val navEvents: SharedFlow<AuthNavEvent> = _navEvents.asSharedFlow()

    fun getCurrentServerUrl(context: android.content.Context? = null): String {
        return com.rotai.iq.core.network.NetworkConfig.getBaseUrl(context)
    }

    fun updateServerUrl(context: android.content.Context, newUrl: String) {
        // No-op: servidor fixo na nuvem Vercel
    }

    // -------------------------------------------------------------
    // LOGIN
    // -------------------------------------------------------------
    fun onLoginEmailChanged(email: String) {
        _loginState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onLoginPasswordChanged(password: String) {
        _loginState.update { it.copy(password = password, errorMessage = null) }
    }

    fun toggleLoginPasswordVisibility() {
        _loginState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun executeLogin() {
        val current = _loginState.value
        val email = current.email.trim()
        val password = current.password

        if (email.isBlank()) {
            _loginState.update { it.copy(errorMessage = "Por favor, informe seu e-mail.") }
            return
        }
        if (!isValidEmail(email)) {
            _loginState.update { it.copy(errorMessage = "Formato de e-mail inválido.") }
            return
        }
        if (password.isBlank()) {
            _loginState.update { it.copy(errorMessage = "Por favor, digite sua senha.") }
            return
        }

        viewModelScope.launch {
            _loginState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.login(email, password)
            if (result.isSuccess) {
                _loginState.update { it.copy(isLoading = false, errorMessage = null) }
                _navEvents.emit(AuthNavEvent.NavigateToHome)
            } else {
                val error = result.exceptionOrNull()?.message ?: "Falha ao realizar login."
                _loginState.update { it.copy(isLoading = false, errorMessage = error) }
            }
        }
    }

    // -------------------------------------------------------------
    // CADASTRO (CRIAR CONTA)
    // -------------------------------------------------------------
    fun onRegisterNameChanged(name: String) {
        _registerState.update { it.copy(name = name, errorMessage = null) }
    }

    fun onRegisterEmailChanged(email: String) {
        _registerState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onRegisterPasswordChanged(password: String) {
        _registerState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onRegisterConfirmPasswordChanged(confirm: String) {
        _registerState.update { it.copy(confirmPassword = confirm, errorMessage = null) }
    }

    fun toggleRegisterPasswordVisibility() {
        _registerState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onTermsAcceptedChanged(accepted: Boolean) {
        _registerState.update { it.copy(termsAccepted = accepted, errorMessage = null) }
    }

    fun executeRegister() {
        val current = _registerState.value
        val name = current.name.trim()
        val email = current.email.trim()
        val password = current.password
        val confirm = current.confirmPassword

        if (name.isBlank()) {
            _registerState.update { it.copy(errorMessage = "Nome completo é obrigatório.") }
            return
        }
        if (email.isBlank() || !isValidEmail(email)) {
            _registerState.update { it.copy(errorMessage = "Informe um e-mail válido.") }
            return
        }
        if (password.length < 8) {
            _registerState.update { it.copy(errorMessage = "A senha deve ter no mínimo 8 caracteres.") }
            return
        }
        if (password != confirm) {
            _registerState.update { it.copy(errorMessage = "A senha e a confirmação devem ser iguais.") }
            return
        }
        if (!current.termsAccepted) {
            _registerState.update { it.copy(errorMessage = "Você deve concordar com os Termos de Uso e Política de Privacidade.") }
            return
        }

        viewModelScope.launch {
            _registerState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.register(
                name = name,
                email = email,
                password = password,
                termsAccepted = true
            )
            if (result.isSuccess) {
                _registerState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        successMessage = "Conta criada com sucesso! Enviamos um link de confirmação para seu e-mail."
                    )
                }
                _navEvents.emit(AuthNavEvent.NavigateToHome)
            } else {
                val error = result.exceptionOrNull()?.message ?: "Erro ao criar conta."
                _registerState.update { it.copy(isLoading = false, errorMessage = error) }
            }
        }
    }

    // -------------------------------------------------------------
    // RECUPERAÇÃO DE SENHA
    // -------------------------------------------------------------
    fun onForgotEmailChanged(email: String) {
        _forgotState.update { it.copy(email = email, errorMessage = null, infoMessage = null) }
    }

    fun onForgotTokenChanged(token: String) {
        _forgotState.update { it.copy(token = token, errorMessage = null) }
    }

    fun onForgotNewPasswordChanged(password: String) {
        _forgotState.update { it.copy(newPassword = password, errorMessage = null) }
    }

    fun toggleForgotNewPasswordVisibility() {
        _forgotState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun requestPasswordReset() {
        val email = _forgotState.value.email.trim()
        if (email.isBlank() || !isValidEmail(email)) {
            _forgotState.update { it.copy(errorMessage = "Informe um e-mail válido para recuperação.") }
            return
        }

        viewModelScope.launch {
            _forgotState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.forgotPassword(email)
            if (result.isSuccess) {
                val msg = result.getOrThrow()
                _forgotState.update {
                    it.copy(
                        isLoading = false,
                        isCodeSent = true,
                        infoMessage = msg,
                        errorMessage = null
                    )
                }
            } else {
                val error = result.exceptionOrNull()?.message ?: "Falha ao enviar instruções."
                _forgotState.update { it.copy(isLoading = false, errorMessage = error) }
            }
        }
    }

    fun executeResetPassword() {
        val current = _forgotState.value
        val token = current.token.trim()
        val newPassword = current.newPassword

        if (token.isBlank()) {
            _forgotState.update { it.copy(errorMessage = "Informe o código ou token recebido no e-mail.") }
            return
        }
        if (newPassword.length < 8) {
            _forgotState.update { it.copy(errorMessage = "A nova senha deve possuir no mínimo 8 caracteres.") }
            return
        }

        viewModelScope.launch {
            _forgotState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.resetPassword(token, newPassword)
            if (result.isSuccess) {
                _forgotState.update {
                    it.copy(
                        isLoading = false,
                        infoMessage = "Senha alterada com sucesso! Você já pode entrar com sua nova senha.",
                        errorMessage = null
                    )
                }
                _navEvents.emit(AuthNavEvent.NavigateToLogin)
            } else {
                val error = result.exceptionOrNull()?.message ?: "Falha ao atualizar a senha."
                _forgotState.update { it.copy(isLoading = false, errorMessage = error) }
            }
        }
    }

    // -------------------------------------------------------------
    // LOGOUT
    // -------------------------------------------------------------
    fun executeLogout() {
        viewModelScope.launch {
            authRepository.logout()
            _navEvents.emit(AuthNavEvent.NavigateToLogin)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        return email.matches(emailPattern.toRegex())
    }
}
