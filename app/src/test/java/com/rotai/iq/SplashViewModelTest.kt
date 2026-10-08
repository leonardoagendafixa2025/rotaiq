package com.rotai.iq

import com.google.common.truth.Truth.assertThat
import com.rotai.iq.core.data.repository.SessionStatus
import com.rotai.iq.feature.splash.SplashDestination
import com.rotai.iq.feature.splash.SplashViewModel
import com.rotai.iq.navigation.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeAuthRepository
    private lateinit var splashViewModel: SplashViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeAuthRepository()
        splashViewModel = SplashViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startInitialization_whenUnauthenticated_navigatesToLogin() = runTest {
        fakeRepository.currentStatus = SessionStatus.UNAUTHENTICATED

        var destination: String? = null
        val job = launch {
            splashViewModel.destination.collect {
                if (it is SplashDestination.Navigate) {
                    destination = it.route
                }
            }
        }

        splashViewModel.startInitialization()
        advanceUntilIdle()

        assertThat(destination).isEqualTo(Screen.Login.route)
        job.cancel()
    }

    @Test
    fun startInitialization_whenAuthenticated_navigatesToHome() = runTest {
        fakeRepository.currentStatus = SessionStatus.AUTHENTICATED

        var destination: String? = null
        val job = launch {
            splashViewModel.destination.collect {
                if (it is SplashDestination.Navigate) {
                    destination = it.route
                }
            }
        }

        splashViewModel.startInitialization()
        advanceUntilIdle()

        assertThat(destination).isEqualTo(Screen.Dashboard.route)
        job.cancel()
    }
}
