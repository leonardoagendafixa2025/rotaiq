package com.rotai.iq.core.automation.carmode

import android.content.Context
import com.rotai.iq.core.automation.overlay.OverlayService
import com.rotai.iq.core.automation.tts.VoiceAlertManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CarModeManager(
    private val preferences: CarModePreferencesDataSource,
    private val voiceAlertManager: VoiceAlertManager? = null,
    private val overlayServiceStarter: ((Context) -> Unit)? = { context -> OverlayService.startService(context) },
    private val overlayServiceStopper: ((Context) -> Unit)? = { context -> OverlayService.stopService(context) }
) {

    private val _isCarModeActive = MutableStateFlow(false)
    val isCarModeActive: StateFlow<Boolean> = _isCarModeActive.asStateFlow()

    private val _connectedDeviceName = MutableStateFlow<String?>(null)
    val connectedDeviceName: StateFlow<String?> = _connectedDeviceName.asStateFlow()

    private val _config = MutableStateFlow(preferences.getConfig())
    val config: StateFlow<CarModeConfig> = _config.asStateFlow()

    fun updateConfig(newConfig: CarModeConfig) {
        preferences.saveConfig(newConfig)
        _config.value = newConfig
    }

    fun onBluetoothConnected(
        deviceName: String?,
        deviceAddress: String?,
        context: Context?
    ): Boolean {
        val currentConfig = _config.value
        val matchesTarget = if (currentConfig.targetDeviceAddress != null) {
            currentConfig.targetDeviceAddress.equals(deviceAddress, ignoreCase = true)
        } else if (currentConfig.targetDeviceName != null) {
            currentConfig.targetDeviceName.equals(deviceName, ignoreCase = true)
        } else {
            // Se nenhum dispositivo específico foi travado, aceita qualquer conexão de áudio do carro
            true
        }

        if (matchesTarget) {
            _isCarModeActive.value = true
            _connectedDeviceName.value = deviceName ?: "Dispositivo Veicular"

            if (currentConfig.autoStartEnabled && context != null) {
                overlayServiceStarter?.invoke(context)
            }

            if (currentConfig.announceCarModeActive) {
                voiceAlertManager?.speak("Modo Carro ativado. Copiloto ROTA IQ pronto.")
            }
            return true
        }
        return false
    }

    fun onBluetoothDisconnected(
        deviceName: String?,
        deviceAddress: String?,
        context: Context?
    ): Boolean {
        val currentConfig = _config.value
        val matchesTarget = if (currentConfig.targetDeviceAddress != null) {
            currentConfig.targetDeviceAddress.equals(deviceAddress, ignoreCase = true)
        } else if (currentConfig.targetDeviceName != null) {
            currentConfig.targetDeviceName.equals(deviceName, ignoreCase = true)
        } else {
            true
        }

        if (matchesTarget) {
            _isCarModeActive.value = false
            _connectedDeviceName.value = null

            if (currentConfig.autoStopEnabled && context != null) {
                overlayServiceStopper?.invoke(context)
            }

            if (currentConfig.announceCarModeActive) {
                voiceAlertManager?.speak("Modo Carro desconectado.")
            }
            return true
        }
        return false
    }

    fun toggleManualCarMode(context: Context?) {
        val nextState = !_isCarModeActive.value
        _isCarModeActive.value = nextState
        if (nextState) {
            _connectedDeviceName.value = "Modo Manual"
            if (context != null) overlayServiceStarter?.invoke(context)
            voiceAlertManager?.speak("Modo Carro ativado manualmente.")
        } else {
            _connectedDeviceName.value = null
            if (context != null) overlayServiceStopper?.invoke(context)
            voiceAlertManager?.speak("Modo Carro desativado.")
        }
    }
}
