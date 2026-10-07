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
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rotai.iq.core.domain.engine.GoalEngine
import com.rotai.iq.core.domain.model.EvaluationClassification
import com.rotai.iq.core.domain.model.RideEvaluation
import com.rotai.iq.core.ui.designsystem.RotaButton
import com.rotai.iq.core.ui.designsystem.RotaButtonVariant
import com.rotai.iq.core.ui.designsystem.RotaCard
import com.rotai.iq.core.ui.designsystem.RotaEmptyState
import com.rotai.iq.core.ui.designsystem.RotaHeader
import com.rotai.iq.core.ui.designsystem.RotaProgress
import com.rotai.iq.core.ui.designsystem.RotaScore
import com.rotai.iq.core.ui.theme.CardShapeDefault
import com.rotai.iq.core.ui.theme.CardShapeElevated
import com.rotai.iq.core.ui.theme.RotaAttention
import com.rotai.iq.core.ui.theme.RotaAvoid
import com.rotai.iq.core.ui.theme.RotaBlack
import com.rotai.iq.core.ui.theme.RotaBorderSubtle
import com.rotai.iq.core.ui.theme.RotaCardBackground
import com.rotai.iq.core.ui.theme.RotaCardElevated
import com.rotai.iq.core.ui.theme.RotaDarkCanvas
import com.rotai.iq.core.ui.theme.RotaExcellent
import com.rotai.iq.core.ui.theme.RotaGlowGreen
import com.rotai.iq.core.ui.theme.RotaGlowRed
import com.rotai.iq.core.ui.theme.RotaGlowYellow
import com.rotai.iq.core.ui.theme.RotaGood
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
    onNavigateToAdvanced: () -> Unit = {},
    onNavigateToAutomation: () -> Unit = {},
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
            driverName = if (state.vehicle.name.isNotBlank()) state.vehicle.name else "Motorista",
            isOnline = true,
            notificationCount = 0,
            onNotificationClick = { /* Notificações */ },
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
                latestRide = state.latestEvaluation,
                onAccept = onNavigateToSimulator,
                onDecline = { /* Recusar corrida */ }
            )
        } else {
            // ==========================================
            // VISÃO PADRÃO PREMIUM COMPLETA
            // ==========================================

            // 2. Card de Meta de Hoje (DADOS REAIS OU EMPTY STATE)
            val hasGoal = state.goal.dailyGrossTarget > 0.0
            if (!hasGoal) {
                RotaEmptyState(
                    title = "Defina sua primeira meta",
                    description = "Configure sua meta diária e por hora para acompanhar o progresso em tempo real.",
                    icon = Icons.Default.Flag,
                    actionButtonText = "Configurar Metas",
                    onActionClick = onNavigateToVehicle
                )
            } else {
                val targetRev = state.goal.dailyGrossTarget
                val currentRev = state.financialSummary.totalGrossRevenue
                val remainingRev = (targetRev - currentRev).coerceAtLeast(0.0)
                val progressRatio = (currentRev / targetRev).toFloat().coerceIn(0f, 1f)
                val advice = state.coachingAdvice

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
                                    text = "Ritmo: ",
                                    fontSize = 12.sp,
                                    color = RotaTextSecondary
                                )
                                Text(
                                    text = advice.status.label,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (advice.status == GoalEngine.GoalPaceStatus.BEHIND_PACE) RotaAvoid else RotaExcellent
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. CARD PRINCIPAL DE CORRIDA (DADOS REAIS OU EMPTY STATE)
            val latestRide = state.latestEvaluation

            if (latestRide == null) {
                RotaEmptyState(
                    title = "Você ainda não possui corridas analisadas",
                    description = "Utilize o simulador ou ative o copiloto para começar a receber análises de rentabilidade e score em tempo real.",
                    icon = Icons.Default.Calculate,
                    actionButtonText = "Simular e Avaliar Corrida",
                    onActionClick = onNavigateToSimulator
                )
            } else {
                val classificationGlow = when (latestRide.classification) {
                    EvaluationClassification.EXCELLENT -> RotaGlowGreen
                    EvaluationClassification.GOOD -> RotaGlowGreen
                    EvaluationClassification.ACCEPTABLE -> RotaGlowYellow
                    else -> RotaGlowRed
                }
                val borderColor = when (latestRide.classification) {
                    EvaluationClassification.EXCELLENT -> RotaExcellent
                    EvaluationClassification.GOOD -> RotaGood
                    EvaluationClassification.ACCEPTABLE -> RotaAttention
                    else -> RotaAvoid
                }

                RotaCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = RotaCardBackground,
                    borderColor = borderColor.copy(alpha = 0.45f),
                    borderWidth = 1.5.dp,
                    glowColor = classificationGlow,
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
                                classification = latestRide.classification,
                                score = latestRide.score,
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
                                    text = latestRide.offer.platform.displayName.uppercase(),
                                    color = RotaTextWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Valor Gigante Real
                        Text(
                            text = "R$ %.2f".format(Locale("pt", "BR"), latestRide.grossFare),
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                            color = RotaTextWhite,
                            letterSpacing = (-1).sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Grade de Especificações da Corrida
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SpecTile(
                                label = "DISTÂNCIA",
                                value = "%.1f km".format(Locale("pt", "BR"), latestRide.totalDistanceKm),
                                modifier = Modifier.weight(1f)
                            )
                            SpecTile(
                                label = "DURAÇÃO",
                                value = "%.0f min".format(Locale("pt", "BR"), latestRide.totalDurationMinutes),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SpecTile(
                                label = "POR KM",
                                value = "R$ %.2f/km".format(Locale("pt", "BR"), latestRide.grossRatePerKm),
                                highlight = true,
                                modifier = Modifier.weight(1f)
                            )
                            SpecTile(
                                label = "POR HORA",
                                value = "R$ %.2f/h".format(Locale("pt", "BR"), latestRide.grossRatePerHour),
                                highlight = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        HorizontalDivider(color = RotaBorderSubtle, thickness = 1.dp)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Lucro Líquido Estimado Real
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
                                    text = "R$ %.2f".format(Locale("pt", "BR"), latestRide.netProfit),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (latestRide.netProfit > 0) RotaExcellent else RotaAvoid
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "CUSTO ESTIMADO",
                                    fontSize = 11.sp,
                                    color = RotaTextTertiary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "R$ %.2f".format(Locale("pt", "BR"), latestRide.estimatedCost),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RotaTextSecondary
                                )
                            }
                        }

                        if (latestRide.reasons.isNotEmpty() || latestRide.alerts.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                latestRide.reasons.take(3).forEach { reason ->
                                    IntelligenceBullet(text = reason)
                                }
                                latestRide.alerts.take(2).forEach { alert ->
                                    IntelligenceBullet(text = alert, isAlert = true)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        RotaButton(
                            text = "NOVA AVALIAÇÃO",
                            onClick = onNavigateToSimulator,
                            variant = RotaButtonVariant.PRIMARY_ORANGE,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
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
                    subtitle = if (state.vehicle.totalCostPerKm > 0) "R$ %.2f/km".format(Locale("pt", "BR"), state.vehicle.totalCostPerKm) else "Não configurado",
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
                    onClick = onNavigateToAutomation,
                    accentColor = RotaOrangePrimary,
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
private fun IntelligenceBullet(
    text: String,
    isAlert: Boolean = false
) {
    val bulletColor = if (isAlert) RotaAvoid else RotaExcellent

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(bulletColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isAlert) Icons.Default.Warning else Icons.Default.Check,
                contentDescription = null,
                tint = bulletColor,
                modifier = Modifier.size(11.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (isAlert) RotaTextPrimary else RotaTextSecondary
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
    latestRide: RideEvaluation?,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    if (latestRide == null) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0F141C))
                .border(1.dp, RotaBorderSubtle, RoundedCornerShape(24.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MODO MOTORISTA ATIVO",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = RotaOrangePrimary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Aguardando nova oferta...",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = RotaTextWhite
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Assim que uma corrida for detectada ou simulada, o veredito aparecerá aqui em tamanho gigante.",
                fontSize = 13.sp,
                color = RotaTextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            RotaButton(
                text = "SIMULAR OFERTA",
                onClick = onAccept,
                variant = RotaButtonVariant.PRIMARY_ORANGE,
                modifier = Modifier.fillMaxWidth(0.7f)
            )
        }
    } else {
        val verdictColor = when (latestRide.classification) {
            EvaluationClassification.EXCELLENT -> RotaExcellent
            EvaluationClassification.GOOD -> RotaGood
            EvaluationClassification.ACCEPTABLE -> RotaAttention
            else -> RotaAvoid
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0F141C))
                .border(2.dp, verdictColor, RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Veredito Gigante
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(verdictColor.copy(alpha = 0.2f))
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(verdictColor)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = latestRide.classification.label.uppercase(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = verdictColor,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Valor Massivo Real
            Text(
                text = "R$ %.2f".format(Locale("pt", "BR"), latestRide.grossFare),
                fontSize = 58.sp,
                fontWeight = FontWeight.Black,
                color = RotaTextWhite,
                letterSpacing = (-2).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Métricas Grandes
            Text(
                text = "%.1f km  •  %.0f min".format(Locale("pt", "BR"), latestRide.totalDistanceKm, latestRide.totalDurationMinutes),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = RotaTextSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "LUCRO LÍQUIDO: R$ %.2f".format(Locale("pt", "BR"), latestRide.netProfit),
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (latestRide.netProfit > 0) RotaExcellent else RotaAvoid
            )

            Spacer(modifier = Modifier.height(26.dp))

            // Botões Gigantes
            RotaButton(
                text = "AVALIAR OUTRA CORRIDA",
                onClick = onAccept,
                variant = RotaButtonVariant.PRIMARY_ORANGE,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            RotaButton(
                text = "DISPENSAR",
                onClick = onDecline,
                variant = RotaButtonVariant.SECONDARY_DARK,
                icon = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )
        }
    }
}
