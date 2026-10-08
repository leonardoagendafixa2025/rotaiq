package com.rotai.iq.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rotai.iq.core.network.NetworkConfig
import com.rotai.iq.core.ui.designsystem.RotaButton
import com.rotai.iq.core.ui.designsystem.RotaButtonVariant
import com.rotai.iq.core.ui.theme.RotaBorderMedium
import com.rotai.iq.core.ui.theme.RotaCardBackground
import com.rotai.iq.core.ui.theme.RotaOrangePrimary
import com.rotai.iq.core.ui.theme.RotaTextSecondary
import com.rotai.iq.core.ui.theme.RotaTextWhite

@Composable
fun ServerConfigDialog(
    currentUrl: String,
    onDismiss: () -> Unit,
    onSaveUrl: (String) -> Unit
) {
    var inputUrl by remember { mutableStateOf(currentUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RotaCardBackground,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = RotaOrangePrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Configurar Servidor",
                    color = RotaTextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Defina o endereço da API REST para autenticação e sincronização:",
                    color = RotaTextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = inputUrl,
                    onValueChange = { inputUrl = it },
                    label = { Text("URL da API Backend", color = RotaTextSecondary, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = RotaTextWhite,
                        unfocusedTextColor = RotaTextWhite,
                        focusedBorderColor = RotaOrangePrimary,
                        unfocusedBorderColor = RotaBorderMedium,
                        cursorColor = RotaOrangePrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Atalhos rápidos:",
                    color = RotaTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Atalho 1: PC Wi-Fi Local (192.168.100.11)
                QuickUrlChip(
                    label = "Wi-Fi Local (192.168.100.11:8000)",
                    isSelected = inputUrl == NetworkConfig.LAN_DEFAULT_URL,
                    onClick = { inputUrl = NetworkConfig.LAN_DEFAULT_URL }
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Atalho 2: Emulador Oficial (10.0.2.2)
                QuickUrlChip(
                    label = "Emulador Android (10.0.2.2:8000)",
                    isSelected = inputUrl == NetworkConfig.EMULATOR_DEFAULT_URL,
                    onClick = { inputUrl = NetworkConfig.EMULATOR_DEFAULT_URL }
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Atalho 3: USB ADB (127.0.0.1)
                QuickUrlChip(
                    label = "Cabo USB adb reverse (127.0.0.1:8000)",
                    isSelected = inputUrl == NetworkConfig.ADB_REVERSE_URL,
                    onClick = { inputUrl = NetworkConfig.ADB_REVERSE_URL }
                )
            }
        },
        confirmButton = {
            RotaButton(
                text = "Salvar Servidor",
                variant = RotaButtonVariant.PRIMARY_ORANGE,
                onClick = {
                    onSaveUrl(inputUrl.trim().trimEnd('/'))
                    onDismiss()
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = RotaTextSecondary)
            }
        }
    )
}

@Composable
private fun QuickUrlChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0x33FF7A00) else Color(0x14FFFFFF))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Dns,
                contentDescription = null,
                tint = if (isSelected) RotaOrangePrimary else RotaTextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = if (isSelected) RotaTextWhite else RotaTextSecondary,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
