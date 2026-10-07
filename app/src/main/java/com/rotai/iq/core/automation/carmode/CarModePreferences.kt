package com.rotai.iq.core.automation.carmode

import android.content.Context
import android.content.SharedPreferences

data class CarModeConfig(
    val autoStartEnabled: Boolean = false,
    val autoStopEnabled: Boolean = false,
    val targetDeviceName: String? = null,
    val targetDeviceAddress: String? = null,
    val highSpeedTtsEnabled: Boolean = true,
    val announceCarModeActive: Boolean = true
)

interface CarModePreferencesDataSource {
    fun getConfig(): CarModeConfig
    fun saveConfig(config: CarModeConfig)
}

class CarModePreferences(context: Context) : CarModePreferencesDataSource {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    override fun getConfig(): CarModeConfig {
        return CarModeConfig(
            autoStartEnabled = prefs.getBoolean(KEY_AUTO_START, false),
            autoStopEnabled = prefs.getBoolean(KEY_AUTO_STOP, false),
            targetDeviceName = prefs.getString(KEY_TARGET_DEVICE_NAME, null),
            targetDeviceAddress = prefs.getString(KEY_TARGET_DEVICE_ADDRESS, null),
            highSpeedTtsEnabled = prefs.getBoolean(KEY_HIGH_SPEED_TTS, true),
            announceCarModeActive = prefs.getBoolean(KEY_ANNOUNCE_ACTIVE, true)
        )
    }

    override fun saveConfig(config: CarModeConfig) {
        prefs.edit()
            .putBoolean(KEY_AUTO_START, config.autoStartEnabled)
            .putBoolean(KEY_AUTO_STOP, config.autoStopEnabled)
            .putString(KEY_TARGET_DEVICE_NAME, config.targetDeviceName)
            .putString(KEY_TARGET_DEVICE_ADDRESS, config.targetDeviceAddress)
            .putBoolean(KEY_HIGH_SPEED_TTS, config.highSpeedTtsEnabled)
            .putBoolean(KEY_ANNOUNCE_ACTIVE, config.announceCarModeActive)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "rota_iq_car_mode_prefs"
        private const val KEY_AUTO_START = "auto_start_enabled"
        private const val KEY_AUTO_STOP = "auto_stop_enabled"
        private const val KEY_TARGET_DEVICE_NAME = "target_device_name"
        private const val KEY_TARGET_DEVICE_ADDRESS = "target_device_address"
        private const val KEY_HIGH_SPEED_TTS = "high_speed_tts"
        private const val KEY_ANNOUNCE_ACTIVE = "announce_car_mode"
    }
}

class InMemoryCarModePreferences(
    private var config: CarModeConfig = CarModeConfig()
) : CarModePreferencesDataSource {
    override fun getConfig(): CarModeConfig = config
    override fun saveConfig(config: CarModeConfig) {
        this.config = config
    }
}
