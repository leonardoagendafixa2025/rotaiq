package com.rotai.iq.feature.geographic

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.rotai.iq.core.domain.engine.GeoHeatmapEngine
import com.rotai.iq.core.domain.model.DemandLevel
import com.rotai.iq.core.ui.designsystem.RotaButton
import com.rotai.iq.core.ui.designsystem.RotaButtonVariant
import com.rotai.iq.core.ui.designsystem.RotaCard
import com.rotai.iq.core.ui.designsystem.RotaChip
import com.rotai.iq.core.ui.designsystem.RotaEmptyState
import com.rotai.iq.core.ui.designsystem.RotaMetric
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
import com.rotai.iq.core.ui.theme.RotaOrangeGlow
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
fun GeoInsightsScreen(
    viewModel: GeoInsightsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var selectedTab by remember { mutableStateOf("Zonas") }

    val topDemandZone = state.zones.maxByOrNull { it.currentDemand }
    val bestPlatform = state.platformReport.bestPlatformByHourlyRate
    val hasComparisons = state.platformReport.summaries.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RotaBlack)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Cabeçalho da Tela com Badge AI Copilot
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "INTELIGÊNCIA DE ROTAS",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = RotaTextWhite,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Zonas de demanda e copiloto operacional",
                    fontSize = 12.sp,
                    color = RotaTextSecondary
                )
            }

            // AI Copilot Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(RotaOrangeSubtleBg)
                    .border(1.dp, RotaOrangePrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = RotaOrangePrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "COPILOTO",
                    color = RotaOrangePrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card de Recomendação Estratégica Real
        RotaCard(
            modifier = Modifier.fillMaxWidth(),
            shape = CardShapeDefault,
            backgroundColor = RotaCardBackground,
            borderColor = RotaOrangePrimary.copy(alpha = 0.6f),
            borderWidth = 1.5.dp,
            glowColor = RotaOrangeGlow
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(RotaOrangeSubtleBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = RotaOrangePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "ESTRATÉGIA DO TURNO ATUAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = RotaOrangePrimary,
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val strategyText = if (topDemandZone != null) {
                    "Para o turno ${state.timeSlot.displayName} (${state.timeSlot.timeRange}), a zona de maior concentração de demanda mapeada é ${topDemandZone.zone.name}."
                } else {
                    "Acumule histórico de corridas para receber orientações estratégicas personalizadas de horário e região."
                }

                Text(
                    text = strategyText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = RotaTextWhite,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Turno: ",
                        fontSize = 12.sp,
                        color = RotaTextSecondary
                    )
                    Text(
                        text = "${state.timeSlot.displayName} (${state.timeSlot.timeRange})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RotaOrangeLight
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cards de Inteligência
        Text(
            text = "INTELIGÊNCIA ROTA IQ",
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
            InsightCard(
                title = "Turno Ativo",
                highlight = state.timeSlot.displayName,
                badge = state.timeSlot.timeRange,
                badgeColor = RotaOrangePrimary,
                icon = Icons.Default.Schedule,
                modifier = Modifier.weight(1f)
            )

            InsightCard(
                title = "Maior Demanda",
                highlight = topDemandZone?.zone?.name ?: "Mapeando",
                badge = topDemandZone?.currentDemand?.displayName ?: "Aguardando",
                badgeColor = RotaExcellent,
                icon = Icons.Default.LocationOn,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        InsightCard(
            title = "Melhor Plataforma Calculada",
            highlight = bestPlatform?.displayName ?: "Dados Insuficientes",
            badge = if (bestPlatform != null) state.platformReport.recommendation else "Avalie corridas para comparar",
            badgeColor = if (bestPlatform != null) RotaExcellent else RotaTextSecondary,
            icon = Icons.AutoMirrored.Filled.CompareArrows,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Seletor de Abas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RotaChip(
                text = "Zonas de Demanda",
                isSelected = selectedTab == "Zonas",
                onClick = { selectedTab = "Zonas" }
            )
            RotaChip(
                text = "Simulador Deadhead",
                isSelected = selectedTab == "Deadhead",
                onClick = { selectedTab = "Deadhead" }
            )
            RotaChip(
                text = "Comparativo Apps",
                isSelected = selectedTab == "Comparativo",
                onClick = { selectedTab = "Comparativo" }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == "Zonas") {
            // Lista de Zonas de Demanda
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.zones.forEach { zoneItem ->
                    ZoneDemandCard(
                        name = zoneItem.zone.name,
                        demandLevel = zoneItem.currentDemand,
                        returnProbability = zoneItem.zone.returnTripProbability,
                        waitTimeMinutes = zoneItem.zone.averageWaitTimeMinutes
                    )
                }
            }
        } else if (selectedTab == "Deadhead") {
            // Simulador Preditivo de Volta Vazia (Deadhead Trap Detector)
            DeadheadSimulatorSection(state = state, viewModel = viewModel)
        } else {
            // Comparativo de Plataformas Real ou Empty State
            if (!hasComparisons) {
                RotaEmptyState(
                    title = "Dados insuficientes para comparar plataformas",
                    description = "O ROTA IQ precisa que você avalie corridas de ao menos uma plataforma para calcular com precisão o lucro/hora e a taxa de retorno de cada app.",
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                val totalGross = state.platformReport.summaries.sumOf { it.totalGrossRevenue }

                RotaCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShapeDefault,
                    backgroundColor = RotaCardBackground,
                    borderColor = RotaBorderSubtle
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "COMPARATIVO REAL DE PLATAFORMAS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = RotaTextWhite
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        state.platformReport.summaries.forEachIndexed { index, summary ->
                            if (index > 0) Spacer(modifier = Modifier.height(10.dp))
                            val sharePct = if (totalGross > 0) ((summary.totalGrossRevenue / totalGross) * 100).toInt() else 0
                            val isBest = summary.platform == state.platformReport.bestPlatformByHourlyRate

                            PlatformCompareRow(
                                platform = summary.platform.displayName,
                                share = "$sharePct% do faturamento • ${summary.totalOffersEvaluated} avaliações",
                                profitPerHour = "R$ %.2f/h".format(Locale("pt", "BR"), summary.averageNetRatePerHour),
                                isBest = isBest
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun InsightCard(
    title: String,
    highlight: String,
    badge: String,
    badgeColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    RotaCard(
        modifier = modifier,
        shape = CardShapeElevated,
        backgroundColor = RotaCardBackground,
        borderColor = RotaBorderSubtle
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = RotaTextSecondary
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RotaOrangePrimary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = highlight,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = RotaTextWhite
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeColor.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ZoneDemandCard(
    name: String,
    demandLevel: DemandLevel,
    returnProbability: Double,
    waitTimeMinutes: Double
) {
    val (statusLabel, statusColor) = when (demandLevel) {
        DemandLevel.VERY_HIGH, DemandLevel.HIGH -> Pair("ALTA DEMANDA", RotaExcellent)
        DemandLevel.BALANCED -> Pair("EQUILIBRADA", RotaAttention)
        DemandLevel.LOW -> Pair("BAIXA DEMANDA", RotaAvoid)
        DemandLevel.DEAD_ZONE, DemandLevel.HIGH_RISK -> Pair("ÁREA DE RISCO", RotaAvoid)
    }

    RotaCard(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShapeElevated,
        backgroundColor = RotaCardBackground,
        borderColor = RotaBorderSubtle
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = RotaTextWhite
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Retorno: %.0f%% • Espera média: %.0f min".format(Locale("pt", "BR"), returnProbability * 100, waitTimeMinutes),
                    fontSize = 11.sp,
                    color = RotaTextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(statusColor.copy(alpha = 0.15f))
                    .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = statusLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = statusColor
                )
            }
        }
    }
}

@Composable
private fun PlatformCompareRow(
    platform: String,
    share: String,
    profitPerHour: String,
    isBest: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isBest) RotaOrangeSubtleBg else RotaDarkCanvas)
            .border(1.dp, if (isBest) RotaOrangePrimary.copy(alpha = 0.4f) else RotaBorderSubtle, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = platform,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = if (isBest) RotaOrangePrimary else RotaTextWhite
            )
            Text(
                text = share,
                fontSize = 11.sp,
                color = RotaTextSecondary
            )
        }

        Text(
            text = profitPerHour,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            color = if (isBest) RotaExcellent else RotaTextWhite
        )
    }
}

@Composable
private fun DeadheadSimulatorSection(
    state: GeoInsightsUiState,
    viewModel: GeoInsightsViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // 1. Seletor de Zona de Destino
        Text(
            text = "SELECIONE A REGIÃO DE DESTINO DA CORRIDA",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = RotaOrangePrimary,
            letterSpacing = 0.8.sp
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val allZones = GeoHeatmapEngine.getDefaultZones()
            allZones.forEach { zone ->
                val isSelected = zone.id == state.selectedZone.id
                RotaCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShapeElevated,
                    backgroundColor = if (isSelected) RotaOrangeSubtleBg else RotaCardBackground,
                    borderColor = if (isSelected) RotaOrangePrimary else RotaBorderSubtle,
                    borderWidth = if (isSelected) 1.5.dp else 1.dp,
                    onClick = { viewModel.selectZone(zone) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = zone.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) RotaOrangePrimary else RotaTextWhite
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Retorno local: %.0f%% • Distância ao centro: %.0f km".format(
                                    Locale("pt", "BR"),
                                    zone.returnTripProbability * 100.0,
                                    zone.deadheadKmToCenter
                                ),
                                fontSize = 11.sp,
                                color = RotaTextSecondary
                            )
                        }

                        if (zone.isAvoidZone) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(RotaAvoid.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "EVITAR",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = RotaAvoid
                                )
                            }
                        } else if (zone.returnTripProbability >= 0.85) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(RotaExcellent.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "ALTO RETORNO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = RotaExcellent
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 2. Parâmetros da Oferta
        Text(
            text = "PARÂMETROS DA OFERTA EM TEMPO REAL",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = RotaTextSecondary,
            letterSpacing = 0.8.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.simFare,
                onValueChange = { viewModel.updateInputs(it, state.simDistanceKm, state.simPickupKm) },
                label = { Text("Valor R$", fontSize = 11.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = RotaTextWhite,
                    unfocusedTextColor = RotaTextWhite,
                    focusedBorderColor = RotaOrangePrimary,
                    unfocusedBorderColor = RotaBorderSubtle,
                    focusedLabelColor = RotaOrangePrimary,
                    unfocusedLabelColor = RotaTextSecondary,
                    cursorColor = RotaOrangePrimary
                ),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = state.simDistanceKm,
                onValueChange = { viewModel.updateInputs(state.simFare, it, state.simPickupKm) },
                label = { Text("Viagem km", fontSize = 11.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = RotaTextWhite,
                    unfocusedTextColor = RotaTextWhite,
                    focusedBorderColor = RotaOrangePrimary,
                    unfocusedBorderColor = RotaBorderSubtle,
                    focusedLabelColor = RotaOrangePrimary,
                    unfocusedLabelColor = RotaTextSecondary,
                    cursorColor = RotaOrangePrimary
                ),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = state.simPickupKm,
                onValueChange = { viewModel.updateInputs(state.simFare, state.simDistanceKm, it) },
                label = { Text("Busca km", fontSize = 11.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = RotaTextWhite,
                    unfocusedTextColor = RotaTextWhite,
                    focusedBorderColor = RotaOrangePrimary,
                    unfocusedBorderColor = RotaBorderSubtle,
                    focusedLabelColor = RotaOrangePrimary,
                    unfocusedLabelColor = RotaTextSecondary,
                    cursorColor = RotaOrangePrimary
                ),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 3. Resultado do Diagnóstico Preditivo
        state.deadheadAnalysis?.let { analysis ->
            val isTrap = analysis.isDeadheadTrap
            val bannerColor = if (isTrap) RotaAvoid else RotaExcellent
            val bannerTitle = if (isTrap) "ARMADILHA DE DEADHEAD DETECTADA" else "DESTINO FAVORÁVEL"

            RotaCard(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShapeDefault,
                backgroundColor = RotaCardBackground,
                borderColor = bannerColor.copy(alpha = 0.8f),
                borderWidth = 1.5.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isTrap) Icons.Default.Warning else Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = bannerColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = bannerTitle,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = bannerColor,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(bannerColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Retorno: %.0f%%".format(Locale("pt", "BR"), analysis.returnProbability * 100.0),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = bannerColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(RotaDarkCanvas)
                                .border(1.dp, RotaBorderSubtle, RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "LUCRO ORIGINAL",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RotaTextSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "R$ %.2f".format(Locale("pt", "BR"), analysis.originalNetProfit),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = RotaTextWhite
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(RotaDarkCanvas)
                                .border(1.dp, RotaBorderSubtle, RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "CUSTO VOLTA VAZIA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RotaAvoid
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "- R$ %.2f".format(Locale("pt", "BR"), analysis.emptyReturnCost),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = RotaAvoid
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (analysis.adjustedNetProfit > 0) RotaOrangeSubtleBg else RotaAvoid.copy(alpha = 0.1f))
                            .border(
                                1.dp,
                                if (analysis.adjustedNetProfit > 0) RotaOrangePrimary.copy(alpha = 0.5f) else RotaAvoid.copy(alpha = 0.5f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "LUCRO REAL AJUSTADO (SOBRA LIMPO)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (analysis.adjustedNetProfit > 0) RotaOrangePrimary else RotaAvoid,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "R$ %.2f".format(Locale("pt", "BR"), analysis.adjustedNetProfit),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (analysis.adjustedNetProfit > 0) RotaExcellent else RotaAvoid
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "KM VAZIO ESPERADO",
                                    fontSize = 10.sp,
                                    color = RotaTextSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "%.1f km".format(Locale("pt", "BR"), analysis.expectedEmptyReturnKm),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = RotaTextWhite
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = analysis.coachingRecommendation,
                        fontSize = 12.sp,
                        color = RotaTextSecondary,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

