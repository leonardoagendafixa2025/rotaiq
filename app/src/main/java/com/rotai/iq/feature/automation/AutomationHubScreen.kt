package com.rotai.iq.feature.automation

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.rotai.iq.core.ui.components.HudEvaluationCard
import com.rotai.iq.core.ui.theme.BrandPrimary
import com.rotai.iq.core.ui.theme.ClassAvoid
import com.rotai.iq.core.ui.theme.ClassGood
import java.util.Locale

@Composable
fun AutomationHubScreen(
    viewModel: AutomationViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val isAccessibilityActive by viewModel.isAccessibilityActive.collectAsState()
    val isOverlayPermissionGranted by viewModel.isOverlayPermissionGranted.collectAsState()
    val isOverlayEnabled by viewModel.isOverlayEnabled.collectAsState()
    val isVoiceAlertEnabled by viewModel.isVoiceAlertEnabled.collectAsState()
    val latestEvaluation by viewModel.latestEvaluation.collectAsState()
    val evaluationsCount by viewModel.evaluationsCount.collectAsState()

    // Atualiza status de permissões quando o usuário retorna das telas de configuração do Android
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissions(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E17))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        if (onNavigateBack != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = BrandPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "VOLTAR",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF90A4AE)
                )
            }
        }

        // Título e Subtítulo
        Text(
            text = "Copiloto & Automação ao Volante",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Decisão instantânea na tela e por voz sem tirar os olhos do trânsito",
            fontSize = 13.sp,
            color = Color(0xFF90A4AE),
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )

        // Status Geral do Copiloto (Badge em destaque)
        val allConfigured = isAccessibilityActive && isOverlayPermissionGranted
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (allConfigured) ClassGood else Color(0xFFFFB300),
                    RoundedCornerShape(12.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (allConfigured) Color(0xFF0D2818) else Color(0xFF2E2005)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (allConfigured) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (allConfigured) ClassGood else Color(0xFFFFB300),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (allConfigured) "MODO COPILOTO OPERACIONAL" else "CONFIGURAÇÃO PENDENTE",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (allConfigured) ClassGood else Color(0xFFFFB300)
                    )
                    Text(
                        text = if (allConfigured) {
                            "O ROTA IQ está pronto para avaliar ofertas Uber e 99 automaticamente."
                        } else {
                            "Ative o Serviço de Acessibilidade e a Sobreposição para operação automática."
                        },
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card 1: Serviço de Acessibilidade
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141923)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = BrandPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "1. Leitura de Tela (Acessibilidade)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = if (isAccessibilityActive) "ATIVADO" else "DESATIVADO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAccessibilityActive) ClassGood else ClassAvoid
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Lê em tempo real os cartões de oferta de corrida do Uber e da 99 assim que surgem na tela. Não acessa mensagens pessoais, dados bancários ou senhas.",
                    fontSize = 12.sp,
                    color = Color(0xFFB0BEC5),
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAccessibilityActive) Color(0xFF263238) else BrandPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isAccessibilityActive) "Gerenciar Acessibilidade no Android" else "Ativar Serviço de Acessibilidade",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Card 2: Sobreposição de Tela (HUD Flutuante)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141923)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = Color(0xFF00D2FF),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "2. HUD Flutuante (Sobreposição)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = if (isOverlayPermissionGranted) "PERMITIDO" else "PENDENTE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverlayPermissionGranted) ClassGood else ClassAvoid
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Projeta o card com Score (0-100), Lucro Líquido Real e R$/h diretamente sobre o aplicativo de corrida em menos de 100ms.",
                    fontSize = 12.sp,
                    color = Color(0xFFB0BEC5),
                    lineHeight = 16.sp
                )

                if (!isOverlayPermissionGranted) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B0FF)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Autorizar Sobreposição de Outros Apps",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Habilitar Exibição Automática do HUD",
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        Switch(
                            checked = isOverlayEnabled,
                            onCheckedChange = { viewModel.setOverlayEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ClassGood
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Card 3: Síntese de Voz (TTS)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141923)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Hearing,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "3. Copiloto Vocal (Síntese TTS)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = isVoiceAlertEnabled,
                        onCheckedChange = { viewModel.setVoiceAlertEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BrandPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Vocaliza em português conciso se a corrida vale a pena ou deve ser evitada, permitindo que você decida sem tirar os olhos do trânsito.",
                    fontSize = 12.sp,
                    color = Color(0xFFB0BEC5),
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { viewModel.testVoiceAlert() },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ouvir Teste de Áudio do Copiloto",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Seção: Demonstração e Simulação em Tempo Real
        Text(
            text = "Laboratório de Teste do Copiloto",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Dispare simulações para ver o HUD flutuante e ouvir o áudio em tempo real agora mesmo:",
            fontSize = 12.sp,
            color = Color(0xFF90A4AE),
            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.simulateLiveOffer(isProfitable = true) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Oferta Boa (Uber)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Button(
                onClick = { viewModel.simulateLiveOffer(isProfitable = false) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Oferta Ruim (99)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = { viewModel.dismissFloatingHud() },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Fechar HUD Flutuante Atual",
                fontSize = 12.sp,
                color = Color(0xFFB0BEC5)
            )
        }

        // Exibição da Última Corrida Avaliada
        latestEvaluation?.let { eval ->
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Última Avaliação Capturada ao Vivo ($evaluationsCount analisadas)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            HudEvaluationCard(evaluation = eval)
        }

        // Card de Conformidade e Segurança
        Spacer(modifier = Modifier.height(20.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101726)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFF00D2FF),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Segurança & LGPD: O ROTA IQ opera 100% on-device. Suas informações de faturamento e dados de corrida permanecem exclusivamente no seu telefone.",
                    fontSize = 11.sp,
                    color = Color(0xFF90A4AE),
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
