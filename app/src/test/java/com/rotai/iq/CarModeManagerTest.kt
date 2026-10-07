package com.rotai.iq

import com.rotai.iq.core.automation.carmode.CarModeConfig
import com.rotai.iq.core.automation.carmode.CarModeManager
import com.rotai.iq.core.automation.carmode.InMemoryCarModePreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CarModeManagerTest {

    private lateinit var preferences: InMemoryCarModePreferences
    private lateinit var manager: CarModeManager
    private var overlayStarted = false
    private var overlayStopped = false

    @Before
    fun setUp() {
        overlayStarted = false
        overlayStopped = false
        preferences = InMemoryCarModePreferences(
            CarModeConfig(
                autoStartEnabled = true,
                autoStopEnabled = true,
                targetDeviceName = "MyCarMultimedia",
                targetDeviceAddress = "AA:BB:CC:DD:EE:FF",
                announceCarModeActive = false
            )
        )
        manager = CarModeManager(
            preferences = preferences,
            voiceAlertManager = null,
            overlayServiceStarter = { overlayStarted = true },
            overlayServiceStopper = { overlayStopped = true }
        )
    }

    @Test
    fun onBluetoothConnected_matchesTargetDeviceAndActivatesCarMode() {
        assertFalse(manager.isCarModeActive.value)

        // Tenta conectar dispositivo aleatório
        val ignored = manager.onBluetoothConnected(
            deviceName = "Headphones",
            deviceAddress = "00:11:22:33:44:55",
            context = null
        )
        assertFalse(ignored)
        assertFalse(manager.isCarModeActive.value)

        // Conecta o dispositivo alvo do carro
        val connected = manager.onBluetoothConnected(
            deviceName = "MyCarMultimedia",
            deviceAddress = "AA:BB:CC:DD:EE:FF",
            context = null
        )
        assertTrue(connected)
        assertTrue(manager.isCarModeActive.value)
        assertEquals("MyCarMultimedia", manager.connectedDeviceName.value)
    }

    @Test
    fun onBluetoothDisconnected_deactivatesCarModeWhenTargetDisconnects() {
        manager.onBluetoothConnected(
            deviceName = "MyCarMultimedia",
            deviceAddress = "AA:BB:CC:DD:EE:FF",
            context = null
        )
        assertTrue(manager.isCarModeActive.value)

        manager.onBluetoothDisconnected(
            deviceName = "MyCarMultimedia",
            deviceAddress = "AA:BB:CC:DD:EE:FF",
            context = null
        )
        assertFalse(manager.isCarModeActive.value)
        assertNull(manager.connectedDeviceName.value)
    }

    @Test
    fun toggleManualCarMode_switchesStateExplicitly() {
        assertFalse(manager.isCarModeActive.value)
        manager.toggleManualCarMode(null)
        assertTrue(manager.isCarModeActive.value)
        assertEquals("Modo Manual", manager.connectedDeviceName.value)

        manager.toggleManualCarMode(null)
        assertFalse(manager.isCarModeActive.value)
        assertNull(manager.connectedDeviceName.value)
    }

    @Test
    fun updateConfig_persistsNewSettings() {
        val newConfig = CarModeConfig(
            autoStartEnabled = false,
            autoStopEnabled = false,
            targetDeviceName = "NewCarBluetooth"
        )
        manager.updateConfig(newConfig)
        assertEquals("NewCarBluetooth", manager.config.value.targetDeviceName)
        assertFalse(manager.config.value.autoStartEnabled)
    }
}
