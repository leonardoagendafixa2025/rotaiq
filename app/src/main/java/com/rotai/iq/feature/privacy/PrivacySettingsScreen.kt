package com.rotai.iq.feature.privacy

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PrivacySettingsScreen(
    viewModel: PrivacySettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val consent by viewModel.consentState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E17))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Header Topo
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onNavigateBack) {
                    Text("← Voltar", color = Color(0xFF90A4AE), fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFF00E676),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Privacidade & LGPD",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Controle total sobre seus dados pessoais, telemetria e direitos fundamentais (Lei 13.709/2018).",
                fontSize = 13.sp,
                color = Color(0xFF90A4AE),
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            // Feedback
            uiState.feedbackMessage?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = msg,
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // Seção 1: Consentimentos e Privacidade
            Text(
                text = "PREFERÊNCIAS DE PRIVACIDADE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF78909C),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121826)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    ConsentToggleRow(
                        title = "Telemetria e Diagnóstico",
                        description = "Permite enviar relatórios de erros técnicos sem qualquer identificação pessoal.",
                        checked = consent.telemetryOptIn,
                        onCheckedChange = { viewModel.updateTelemetryOptIn(it) }
                    )

                    Divider(color = Color(0xFF1E2838), modifier = Modifier.padding(vertical = 10.dp))

                    ConsentToggleRow(
                        title = "Benchmarking Anônimo de Regiões",
                        description = "Contribui com médias anônimas de rendimento por zona para calibrar o Heatmap coletivo.",
                        checked = consent.anonymousBenchmarkingOptIn,
                        onCheckedChange = { viewModel.updateBenchmarkingOptIn(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Seção 2: Portabilidade dos Dados (Art. 18, V)
            Text(
                text = "PORTABILIDADE DE DADOS (ART. 18, V)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF78909C),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121826)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Exporte todos os seus dados operacionais, registros de veículo, consumo de combustível e avaliações em um pacote legível JSON.",
                        fontSize = 12.sp,
                        color = Color(0xFFB0BEC5)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { viewModel.exportDriverData() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E5FF)),
                        enabled = !uiState.isExporting
                    ) {
                        if (uiState.isExporting) {
                            CircularProgressIndicator(color = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text(
                                " Exportar Meus Dados (JSON LGPD)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Seção 3: Direito ao Esquecimento (Art. 18, VI)
            Text(
                text = "DIREITO AO ESQUECIMENTO (ART. 18, VI)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE57373),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1215)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF4E1A1A), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Apagar permanentemente seus dados do dispositivo e restaurar as configurações de fábrica do ROTA IQ.",
                        fontSize = 12.sp,
                        color = Color(0xFFFFCDD2)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.promptDeleteAccount() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(
                            " Excluir Minha Conta e Limpar Dados",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        // Modal de Pré-visualização do JSON de Exportação
        uiState.exportJsonPreview?.let { json ->
            AlertDialog(
                onDismissRequest = { viewModel.dismissExportPreview() },
                containerColor = Color(0xFF121826),
                title = {
                    Text("Pacote LGPD Exportado", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
                },
                text = {
                    Column {
                        Text(
                            text = "Abaixo está o conteúdo estruturado dos seus dados pessoais:",
                            color = Color(0xFFB0BEC5),
                            fontSize = 12.sp
                        )

                        Surface(
                            color = Color(0xFF0A0E17),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = json,
                                color = Color(0xFF00E676),
                                fontSize = 10.sp,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .verticalScroll(rememberScrollState())
                            )
                        }

                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("LGPD JSON", json)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "JSON copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF0A0E17))
                            Text(" Copiar JSON Completo", color = Color(0xFF0A0E17), fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissExportPreview() }) {
                        Text("Fechar", color = Color(0xFF90A4AE))
                    }
                }
            )
        }

        // Modal de Confirmação de Exclusão
        if (uiState.showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissDeletePrompt() },
                containerColor = Color(0xFF1E1215),
                title = {
                    Text("Excluir Todos os Dados?", color = Color(0xFFFF1744), fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(
                        text = "Esta ação é irreversível. Todos os seus registros de veículo, consumo, manutenção, metas e histórico de corridas serão apagados permanentemente deste aparelho.",
                        color = Color(0xFFFFCDD2),
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmDeleteAccountAndPurge() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Text("Sim, Excluir Definitivamente")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissDeletePrompt() }) {
                        Text("Cancelar", color = Color(0xFF90A4AE))
                    }
                }
            )
        }
    }
}

@Composable
private fun ConsentToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(text = description, fontSize = 11.sp, color = Color(0xFF90A4AE))
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF00E676),
                checkedTrackColor = Color(0xFF1B5E20),
                uncheckedThumbColor = Color(0xFF78909C),
                uncheckedTrackColor = Color(0xFF263238)
            )
        )
    }
}
