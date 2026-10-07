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
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.rotai.iq.core.domain.model.DemandLevel
import com.rotai.iq.core.ui.designsystem.RotaButton
import com.rotai.iq.core.ui.designsystem.RotaButtonVariant
import com.rotai.iq.core.ui.designsystem.RotaCard
import com.rotai.iq.core.ui.designsystem.RotaChip
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

@Composable
fun GeoInsightsScreen(
    viewModel: GeoInsightsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var selectedTab by remember { mutableStateOf("Zonas") }

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
                    text = "ANÁLISE ESTRATÉGICA",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = RotaTextWhite,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Inteligência preditiva para faturar mais rápido",
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
                    text = "AI COPILOT",
                    color = RotaOrangePrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card de Recomendação Principal em Destaque (Glow Laranja)
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
                        text = "ESTRATÉGIA RECOMENDADA DE HOJE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = RotaOrangePrimary,
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "\"Hoje, entre 18h e 21h, sua melhor estratégia é permanecer na região Centro. Demanda 35% superior e menor tempo de espera entre chamadas.\"",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = RotaTextWhite,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Turno Atual: ",
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

        // 3 Cards de Inteligência da Especificação
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
                title = "Melhor Horário",
                highlight = "18:00 — 21:00",
                badge = "+18% lucro",
                badgeColor = RotaExcellent,
                icon = Icons.Default.Schedule,
                modifier = Modifier.weight(1f)
            )

            InsightCard(
                title = "Melhor Região",
                highlight = "Centro",
                badge = "R$ 4,21/km",
                badgeColor = RotaOrangePrimary,
                icon = Icons.Default.LocationOn,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        InsightCard(
            title = "Melhor Plataforma Hoje",
            highlight = "Uber",
            badge = "+12% lucro/hora vs 99",
            badgeColor = RotaExcellent,
            icon = Icons.Default.CompareArrows,
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
        } else {
            // Comparativo de Plataformas
            RotaCard(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShapeDefault,
                backgroundColor = RotaCardBackground,
                borderColor = RotaBorderSubtle
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "UBER vs 99 POP vs inDRIVE",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = RotaTextWhite
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    PlatformCompareRow(
                        platform = "Uber",
                        share = "58% do faturamento",
                        profitPerHour = "R$ 51,20/h",
                        isBest = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    PlatformCompareRow(
                        platform = "99Pop",
                        share = "32% do faturamento",
                        profitPerHour = "R$ 44,80/h",
                        isBest = false
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    PlatformCompareRow(
                        platform = "inDrive",
                        share = "10% do faturamento",
                        profitPerHour = "R$ 38,10/h",
                        isBest = false
                    )
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
                    color = badgeColor
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
                    text = "Retorno: %.0f%% • Espera média: %.0f min".format(returnProbability * 100, waitTimeMinutes),
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
