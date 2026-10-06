package com.rotai.iq

import android.app.Application
import com.rotai.iq.core.automation.tts.VoiceAlertManager
import com.rotai.iq.core.data.local.db.RotaIqDatabase
import com.rotai.iq.core.data.repository.RotaIqRepository
import com.rotai.iq.core.data.repository.RotaIqRepositoryImpl

class RotaIqApplication : Application() {

    lateinit var repository: RotaIqRepository
        private set

    lateinit var voiceAlertManager: VoiceAlertManager
        private set

    override fun onCreate() {
        super.onCreate()
        val database = RotaIqDatabase.getDatabase(this)
        repository = RotaIqRepositoryImpl(database)
        voiceAlertManager = VoiceAlertManager(this)
    }

    override fun onTerminate() {
        voiceAlertManager.shutdown()
        super.onTerminate()
    }
}
