package com.rotai.iq

import android.app.Application
import com.rotai.iq.core.automation.carmode.CarModeManager
import com.rotai.iq.core.automation.carmode.CarModePreferences
import com.rotai.iq.core.automation.tts.VoiceAlertManager
import com.rotai.iq.core.data.local.db.RotaIqDatabase
import com.rotai.iq.core.data.repository.CommercialRepository
import com.rotai.iq.core.data.repository.CommercialRepositoryImpl
import com.rotai.iq.core.data.repository.RotaIqRepository
import com.rotai.iq.core.data.repository.RotaIqRepositoryImpl
import com.rotai.iq.core.domain.engine.BillingManager
import com.rotai.iq.core.domain.engine.FeatureGateManager
import com.rotai.iq.core.domain.engine.PixPaymentManager
import com.rotai.iq.core.featureflags.FeatureFlagManager
import com.rotai.iq.core.security.AndroidSecureStorage
import com.rotai.iq.core.security.LgpdManager
import com.rotai.iq.core.security.SecureStorage
import com.rotai.iq.core.telemetry.TelemetryManager

class RotaIqApplication : Application() {

    lateinit var repository: RotaIqRepository
        private set

    lateinit var commercialRepository: CommercialRepository
        private set

    lateinit var voiceAlertManager: VoiceAlertManager
        private set

    lateinit var billingManager: BillingManager
        private set

    lateinit var pixPaymentManager: PixPaymentManager
        private set

    lateinit var featureGateManager: FeatureGateManager
        private set

    lateinit var lgpdManager: LgpdManager
        private set

    lateinit var telemetryManager: TelemetryManager
        private set

    lateinit var featureFlagManager: FeatureFlagManager
        private set

    lateinit var secureStorage: SecureStorage
        private set

    lateinit var authApiClient: com.rotai.iq.core.network.AuthApiClient
        private set

    lateinit var authSessionManager: com.rotai.iq.core.security.AuthSessionManager
        private set

    lateinit var authRepository: com.rotai.iq.core.data.repository.AuthRepository
        private set

    lateinit var carModeManager: CarModeManager
        private set

    override fun onCreate() {
        super.onCreate()
        val database = RotaIqDatabase.getDatabase(this)
        repository = RotaIqRepositoryImpl(database)
        commercialRepository = CommercialRepositoryImpl(this)
        voiceAlertManager = VoiceAlertManager(this)

        val carModePrefs = CarModePreferences(this)
        carModeManager = CarModeManager(carModePrefs, voiceAlertManager)

        billingManager = BillingManager()
        pixPaymentManager = PixPaymentManager()
        featureGateManager = FeatureGateManager()
        lgpdManager = LgpdManager()
        telemetryManager = TelemetryManager()
        com.rotai.iq.core.telemetry.RotaCrashReporter.install(this, telemetryManager)
        featureFlagManager = FeatureFlagManager()
        secureStorage = AndroidSecureStorage(this)
        authApiClient = com.rotai.iq.core.network.AuthApiClient(com.rotai.iq.core.network.NetworkConfig.getBaseUrl(this))
        authSessionManager = com.rotai.iq.core.security.AuthSessionManager(secureStorage)
        authRepository = com.rotai.iq.core.data.repository.AuthRepositoryImpl(authApiClient, authSessionManager)

        // Inicializa Canais de Notificação Android (Geral, Marketing, Sistema, Corridas)
        com.rotai.iq.core.notifications.NotificationChannels.createChannels(this)

        // Sincroniza Token FCM e inscrição no tópico global 'rotaiq_all'
        com.rotai.iq.core.notifications.DeviceTokenManager.syncDevice(this)

        // Inicia monitoramento e sincronização em tempo real de notificações push
        com.rotai.iq.core.notifications.NotificationSyncManager.startSync(this)
    }

    override fun onTerminate() {
        voiceAlertManager.shutdown()
        super.onTerminate()
    }
}
