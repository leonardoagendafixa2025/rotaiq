package com.rotai.iq.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rotai.iq.core.data.repository.AuthRepository
import com.rotai.iq.core.data.repository.SessionStatus
import com.rotai.iq.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed class SplashDestination {
    data class Navigate(val route: String) : SplashDestination()
}

class SplashViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _destination = MutableSharedFlow<SplashDestination>()
    val destination: SharedFlow<SplashDestination> = _destination.asSharedFlow()

    fun startInitialization() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()

            // Verificação real e assíncrona da sessão e renovação de token se necessário
            val status = authRepository.checkSessionStatus()

            // Garante um tempo visual harmonioso para a animação da logo (entre 800ms e 1200ms)
            val elapsed = System.currentTimeMillis() - startTime
            val minDisplayDuration = 950L
            if (elapsed < minDisplayDuration) {
                delay(minDisplayDuration - elapsed)
            }

            when (status) {
                SessionStatus.AUTHENTICATED -> {
                    // Se onboarding estiver incompleto, poderia ir para onboarding; caso contrário, vai para Home
                    _destination.emit(SplashDestination.Navigate(Screen.Dashboard.route))
                }
                SessionStatus.NEEDS_REFRESH,
                SessionStatus.UNAUTHENTICATED -> {
                    _destination.emit(SplashDestination.Navigate(Screen.Login.route))
                }
            }
        }
    }
}
