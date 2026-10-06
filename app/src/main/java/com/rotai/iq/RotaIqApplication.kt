package com.rotai.iq

import android.app.Application
import com.rotai.iq.core.data.local.db.RotaIqDatabase
import com.rotai.iq.core.data.repository.RotaIqRepository
import com.rotai.iq.core.data.repository.RotaIqRepositoryImpl

class RotaIqApplication : Application() {

    lateinit var repository: RotaIqRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = RotaIqDatabase.getDatabase(this)
        repository = RotaIqRepositoryImpl(database)
    }
}
