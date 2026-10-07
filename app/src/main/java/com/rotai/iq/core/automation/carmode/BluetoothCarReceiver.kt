package com.rotai.iq.core.automation.carmode

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.rotai.iq.RotaIqApplication

class BluetoothCarReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val action = intent.action ?: return
        val app = context.applicationContext as? RotaIqApplication ?: return
        val carModeManager = app.carModeManager

        val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        }

        val deviceName = try {
            device?.name
        } catch (_: SecurityException) {
            null
        }

        val deviceAddress = device?.address

        when (action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                carModeManager.onBluetoothConnected(
                    deviceName = deviceName,
                    deviceAddress = deviceAddress,
                    context = context
                )
            }
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                carModeManager.onBluetoothDisconnected(
                    deviceName = deviceName,
                    deviceAddress = deviceAddress,
                    context = context
                )
            }
        }
    }
}
