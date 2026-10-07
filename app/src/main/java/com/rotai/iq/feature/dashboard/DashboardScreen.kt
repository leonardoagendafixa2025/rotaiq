package com.rotai.iq.feature.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rotai.iq.core.domain.model.EvaluationClassification
import com.rotai.iq.core.ui.designsystem.RotaButton
import com.rotai.iq.core.ui.designsystem.RotaButtonVariant
import com.rotai.iq.core.ui.designsystem.RotaCard
import com.rotai.iq.core.ui.designsystem.RotaChip
import com.rotai.iq.core.ui.designsystem.RotaHeader
import com.rotai.iq.core.ui.designsystem.RotaMetric
import com.rotai.iq.core.ui.designsystem.RotaProgress
import com.rotai.iq.core.ui.designsystem.RotaScore
import com.rotai.iq.core.ui.theme.CardShapeDefault
import com.rotai.iq.core.ui.theme.CardShapeElevated
import com.rotai.iq.core.ui.theme.ChipShapeCapsule
import com.rotai.iq.core.ui.theme.RotaBlack
import com.rotai.iq.core.ui.theme.RotaBorderMedium
import com.rotai.iq.core.ui.theme.RotaBorderSubtle
import com.rotai.iq.core.ui.theme.RotaCardBackground
import com.rotai.iq.core.ui.theme.RotaCardElevated
import com.rotai.iq.core.ui.theme.RotaDarkCanvas
import com.rotai.iq.core.ui.theme.RotaExcellent
import com.rotai.iq.core.ui.theme.RotaGlowGreen
import com.rotai.iq.core.ui.theme.RotaOrangeHover
import com.rotai.iq.core.ui.theme.RotaOrangeLight
import com.rotai.iq.core.ui.theme.RotaOrangePrimary
import com.rotai.iq.core.ui.theme.RotaOrangeSubtleBg
import com.rotai.iq.core.ui.theme.RotaTextPrimary
import com.rotai.iq.core.ui.theme.RotaTextSecondary
import com.rotai.iq.core.ui.theme.RotaTextTertiary
import com.rotai.iq.core.ui.theme.RotaTextWhite
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToSimulator: () -> Unit,
    onNavigateToVehicle: () -> Unit,
    onNavigateToSubscription: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToAdvanced: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var isDriverModeActive by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RotaBlack)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 1. Topo Oficial da Aplicação
        RotaHeader(
            driverName = "Leonardo",
            isOnline = true,
            notificationCount = 2,
            onNotificationClick = { /* Abrir notificações */ },
            onProfileClick = onNavigateToVehicle
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Toggle do Modo Motorista (Condução Segura)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(RotaCardBackground)
                .border(1.dp, if (isDriverModeActive) RotaOrangePrimary.copy(alpha = 0.5f) else RotaBorderSubtle, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isDriverModeActive) RotaOrangeSubtleBg else RotaDarkCanvas),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = if (isDriverModeActive) RotaOrangePrimary else RotaTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "MODO MOTORISTA",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDriverModeActive) RotaOrangePrimary else RotaTextPrimary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = if (isDriverModeActive) "Visão simplificada ao volante ativa" else "Alternar para interface ampliada",
                        fontSize = 11.sp,
                        color = RotaTextTertiary
                    )
                }
            }

            Switch(
                checked = isDriverModeActive,
                onCheckedChange = { isDriverModeActive = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = RotaBlack,
                    checkedTrackColor = RotaOrangePrimary,
                    uncheckedThumbColor = RotaTextSecondary,
                    uncheckedTrackColor = RotaDarkCanvas
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isDriverModeActive) {
            // ==========================================
            // MODO MOTORISTA ATIVADO (ALTO CONTRASTE)
            // ==========================================
            DriverModeCockpitView(
                onAccept = onNavigateToSimulator,
                onDecline = { /* Recusar */ }
            )
        } else {
            // ==========================================
            // VISÃO PADRÃO PREMIUM COMPLETA
            // ==========================================

            // 2. Card de Meta de Hoje
            val targetRev = if (state.goal.dailyGrossTarget > 0) state.goal.dailyGrossTarget else 350.0
            val currentRev = if (state.financialSummary.totalGrossRevenue > 0) state.financialSummary.totalGrossRevenue else 247.80
            val remainingRev = (targetRev - currentRev).coerceAtLeast(0.0)
            val progressRatio = (currentRev / targetRev).toFloat().coerceIn(0f, 1f)

            RotaCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = RotaCardBackground,
                borderColor = RotaBorderSubtle,
                shape = CardShapeDefault
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "META DE HOJE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RotaTextSecondary,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "${(progressRatio * 100).toInt()}% concluído",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RotaOrangePrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "R$ %.2f".format(Locale("pt", "BR"), currentRev),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = RotaTextWhite,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = " / R$ %.0f".format(Locale("pt", "BR"), targetRev),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = RotaTextTertiary,
                            modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    RotaProgress(
                        progress = progressRatio,
                        barColor = RotaOrangePrimary,
                        height = 10.dp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Faltam: ",
                                fontSize = 12.sp,
                                color = RotaTextSecondary
                            )
                            Text(
                                text = "R$ %.2f".format(Locale("pt", "BR"), remainingRev),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = RotaOrangeLight
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Estimativa: ",
                                fontSize = 12.sp,
                                color = RotaTextSecondary
                            )
                            Text(
                                text = "R$ 374,00",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = RotaExcellent
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. CARD PRINCIPAL DE CORRIDA (Elemento Mais Importante da Home)
            RotaCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = RotaCardBackground,
                borderColor = RotaExcellent.copy(alpha = 0.45f),
                borderWidth = 1.5.dp,
                glowColor = RotaGlowGreen,
                shape = CardShapeDefault
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Badge de Veredito
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RotaScore(
                            classification = EvaluationClassification.EXCELLENT,
                            score = 96,
                            large = true
                        )

                        // Plataforma Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(RotaCardElevated)
                                .border(1.dp, RotaBorderSubtle, RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "UBERX",
                                color = RotaTextWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Valor Gigante
                    Text(
                        text = "R$ 32,80",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        color = RotaTextWhite,
                        letterSpacing = (-1).sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Grade de Especificações da Corrida (Tiles Escuros)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SpecTile(label = "DISTÂNCIA", value = "9,4 km", modifier = Modifier.weight(1f))
                        SpecTile(label = "DURAÇÃO", value = "26 min", modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SpecTile(label = "POR KM", value = "R$ 3,49/km", highlight = true, modifier = Modifier.weight(1f))
                        SpecTile(label = "POR HORA", value = "R$ 75,69/h", highlight = true, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    HorizontalDivider(color = RotaBorderSubtle, thickness = 1.dp)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Lucro Líquido Estimado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LUCRO ESTIMADO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RotaTextSecondary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "R$ 26,60",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = RotaExcellent
                            )
                        }

                        // Custo do veículo calculado
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "CUSTO ESTIMADO",
                                fontSize = 11.sp,
                                color = RotaTextTertiary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "R$ 6,20",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = RotaTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bullets de Inteligência
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        IntelligenceBullet(text = "Acima da sua meta horária de R$ 45,00/h")
                        IntelligenceBullet(text = "Região favorável (Centro → Baixo tempo morto)")
                        IntelligenceBullet(text = "Bom retorno esperado para novas chamadas")
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Botão Principal Laranja
                    RotaButton(
                        text = "ANALISAR CORRIDA",
                        onClick = onNavigateToSimulator,
                        variant = RotaButtonVariant.PRIMARY_ORANGE,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Ações Rápidas & Ferramentas Inteligentes
            Text(
                text = "FERRAMENTAS EM TEMPO REAL",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = RotaTextSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickToolCard(
                    title = "Simulador",
                    subtitle = "Avaliar manualmente",
                    icon = Icons.Default.Calculate,
                    onClick = onNavigateToSimulator,
                    modifier = Modifier.weight(1f)
                )
                QuickToolCard(
                    title = "Meu Veículo",
                    subtitle = "R$ %.2f/km".format(Locale("pt", "BR"), state.vehicle.totalCostPerKm),
                    icon = Icons.Default.DirectionsCar,
                    onClick = onNavigateToVehicle,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickToolCard(
                    title = "Copiloto & HUD",
                    subtitle = "Automação ativa",
                    icon = Icons.Default.PlayArrow,
                    onClick = onNavigateToAdvanced,
                    modifier = Modifier.weight(1f)
                )
                QuickToolCard(
                    title = "Plano Pro",
                    subtitle = "Recursos liberados",
                    icon = Icons.Default.Star,
                    onClick = onNavigateToSubscription,
                    accentColor = RotaOrangePrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ======================================================================
// COMPONENTES AUXILIARES DA HOME
// ======================================================================

@Composable
private fun SpecTile(
    label: String,
    value: String,
    highlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (highlight) RotaOrangeSubtleBg else RotaCardElevated)
            .border(1.dp, if (highlight) RotaOrangePrimary.copy(alpha = 0.35f) else RotaBorderSubtle, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (highlight) RotaOrangePrimary else RotaTextTertiary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = RotaTextWhite
            )
        }
    }
}

@Composable
private fun IntelligenceBullet(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(RotaExcellent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = RotaExcellent,
                modifier = Modifier.size(11.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = RotaTextSecondary
        )
    }
}

@Composable
private fun QuickToolCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = RotaTextPrimary
) {
    RotaCard(
        modifier = modifier,
        shape = CardShapeElevated,
        backgroundColor = RotaCardBackground,
        borderColor = RotaBorderSubtle,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(RotaDarkCanvas)
                    .border(1.dp, RotaBorderSubtle, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RotaTextWhite
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = RotaTextSecondary
                )
            }
        }
    }
}

@Composable
private fun DriverModeCockpitView(
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF0F141C))
            .border(2.dp, RotaExcellent, RoundedCornerShape(24.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Veredito Gigante
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(30.dp))
                .background(RotaExcellent.copy(alpha = 0.2f))
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(RotaExcellent)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "EXCELENTE",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = RotaExcellent,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Valor Massivo
        Text(
            text = "R$ 32,80",
            fontSize = 58.sp,
            fontWeight = FontWeight.Black,
            color = RotaTextWhite,
            letterSpacing = (-2).sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Métricas Grandes
        Text(
            text = "9,4 km  •  26 min",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = RotaTextSecondary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "LUCRO LÍQUIDO: R$ 26,60",
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = RotaExcellent
        )

        Spacer(modifier = Modifier.height(26.dp))

        // Botões Gigantes
        RotaButton(
            text = "ACEITAR CORRIDA",
            onClick = onAccept,
            variant = RotaButtonVariant.PRIMARY_ORANGE,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        RotaButton(
            text = "IGNORAR",
            onClick = onDecline,
            variant = RotaButtonVariant.SECONDARY_DARK,
            icon = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )
    }
}
