package com.rotai.iq

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.rotai.iq.core.notifications.DeviceTokenManager
import com.rotai.iq.core.ui.theme.CockpitBackground
import com.rotai.iq.core.ui.theme.RotaIQTheme
import com.rotai.iq.navigation.RotaIqApp

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    // Gerenciador de permissão de notificação em tempo de execução (Android 13+ / Tiramisu / API 33+)
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Permissão POST_NOTIFICATIONS concedida pelo motorista.")
            DeviceTokenManager.syncDevice(this)
        } else {
            Log.w(TAG, "Permissão POST_NOTIFICATIONS negada pelo usuário.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Solicita permissão de notificação se Android 13+
        checkAndRequestNotificationPermission()

        // 2. Trata Deep Link recebido (via notificação nativa push)
        handleDeepLinkIntent(intent)

        val app = application as RotaIqApplication
        val viewModelFactory = RotaIqViewModelFactory(app.repository, app)

        setContent {
            RotaIQTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CockpitBackground
                ) {
                    RotaIqApp(viewModelFactory = viewModelFactory)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLinkIntent(intent)
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val currentPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (currentPermission != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                DeviceTokenManager.syncDevice(this)
            }
        } else {
            DeviceTokenManager.syncDevice(this)
        }
    }

    private fun handleDeepLinkIntent(intent: Intent?) {
        val dataUri = intent?.data
        val target = intent?.getStringExtra("deep_link_target") ?: dataUri?.toString()
        if (!target.isNullOrBlank()) {
            Log.d(TAG, "Deep Link aberto pelo motorista: $target")
            // A navegação do app interpreta a rota específica (rotaiq://subscription, etc.)
        }
    }
}
