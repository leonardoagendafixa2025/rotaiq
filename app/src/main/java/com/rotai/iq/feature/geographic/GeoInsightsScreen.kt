package com.rotai.iq.feature.geographic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rotai.iq.core.domain.model.DemandLevel
import com.rotai.iq.core.ui.components.CockpitMetricCard
import com.rotai.iq.core.ui.theme.BrandPrimary
import com.rotai.iq.core.ui.theme.ClassAvoid
import com.rotai.iq.core.ui.theme.ClassExcellent
import com.rotai.iq.core.ui.theme.ClassGood
import com.rotai.iq.core.ui.theme.CockpitBackground
import com.rotai.iq.core.ui.theme.CockpitBorder
import com.rotai.iq.core.ui.theme.CockpitSurface
import com.rotai.iq.core.ui.theme.TextPrimary
import com.rotai.iq.core.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun GeoInsightsScreen(
    viewModel: GeoInsightsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CockpitBackground)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Cabeçalho Principal
        Text(
            text = "Inteligência Geográfica & Zonas",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Previsão de demanda por horário, risco de deadhead e comparativo de plataformas",
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
        )

        // Pill do Horário Atual e Turno
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CockpitSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CockpitBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = BrandPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "TURNO ATUAL: ${state.timeSlot.displayName.uppercase()}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Janela horária ativa: ${state.timeSlot.timeRange}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Abas de Navegação (Zonas, Deadhead, Uber vs 99)
        TabRow(
            selectedTabIndex = state.currentTab,
            containerColor = CockpitSurface,
            contentColor = BrandPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[state.currentTab]),
                    color = BrandPrimary
                )
            }
        ) {
            Tab(
                selected = state.currentTab == 0,
                onClick = { viewModel.setTab(0) },
                text = { Text("Zonas & Demanda", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = state.currentTab == 1,
                onClick = { viewModel.setTab(1) },
                text = { Text("Risco Deadhead", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = state.currentTab == 2,
                onClick = { viewModel.setTab(2) },
                text = { Text("Uber vs 99", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (state.currentTab) {
            0 -> ZonesTabContent(state = state)
            1 -> DeadheadSimulatorTabContent(
                state = state,
                onZoneSelected = { viewModel.selectZone(it) },
                onInputsChanged = { fare, dist, pick -> viewModel.updateInputs(fare, dist, pick) }
            )
            2 -> PlatformComparisonTabContent(state = state)
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun ZonesTabContent(state: GeoInsightsUiState) {
    Text(
        text = "HEATMAP METROPOLITANO DE DEMANDA",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    state.zones.forEach { item ->
        val zone = item.zone
        val demand = item.currentDemand
        val color = Color(demand.hexColor)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            colors = CardDefaults.cardColors(containerColor = CockpitSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = zone.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(color.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = demand.displayName.take(16),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Chance Retorno: ${(zone.returnTripProbability * 100).toInt()}%",
                        fontSize = 12.sp,
                        color = if (zone.returnTripProbability >= 0.75) ClassGood else Color(0xFFFFB300),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Espera Média: ${zone.averageWaitTimeMinutes.toInt()} min",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    if (zone.deadheadKmToCenter > 0) {
                        Text(
                            text = "Volta Centro: ${zone.deadheadKmToCenter.toInt()} km",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                zone.notes?.let { notes ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = notes,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DeadheadSimulatorTabContent(
    state: GeoInsightsUiState,
    onZoneSelected: (com.rotai.iq.core.domain.model.GeoZone) -> Unit,
    onInputsChanged: (String, String, String) -> Unit
) {
    Text(
        text = "SIMULADOR DE RISCO DE VOLTA VAZIA",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 6.dp)
    )
    Text(
        text = "Descubra se o valor da corrida compensa o risco de não conseguir passageiro na volta.",
        fontSize = 12.sp,
        color = TextSecondary,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    // Seletor de Destino
    Text(
        text = "Selecione o Bairro / Região de Destino:",
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.White,
        modifier = Modifier.padding(bottom = 6.dp)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        state.zones.forEach { item ->
            val zone = item.zone
            val isSelected = zone.id == state.selectedZone.id
            FilterChip(
                selected = isSelected,
                onClick = { onZoneSelected(zone) },
                label = { Text(zone.name.take(20), fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BrandPrimary,
                    selectedLabelColor = Color.Black
                )
            )
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Campos de Entrada da Oferta
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = state.simFare,
            onValueChange = { onInputsChanged(it, state.simDistanceKm, state.simPickupKm) },
            label = { Text("Valor (R$)", fontSize = 11.sp) },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        OutlinedTextField(
            value = state.simDistanceKm,
            onValueChange = { onInputsChanged(state.simFare, it, state.simPickupKm) },
            label = { Text("Viagem (km)", fontSize = 11.sp) },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        OutlinedTextField(
            value = state.simPickupKm,
            onValueChange = { onInputsChanged(state.simFare, state.simDistanceKm, it) },
            label = { Text("Busca (km)", fontSize = 11.sp) },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Resultado da Análise de Deadhead
    state.deadheadAnalysis?.let { analysis ->
        val trap = analysis.isDeadheadTrap
        val cardBorderColor = if (trap) ClassAvoid else ClassExcellent

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (trap) Color(0xFF2B1014) else Color(0xFF0F261B)
            ),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, cardBorderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (trap) "⚠️ ARMADILHA DE DEADHEAD" else "✓ DESTINO COM RETORNO VIÁVEL",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = cardBorderColor
                    )
                    Text(
                        text = "Retorno: ${(analysis.returnProbability * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Métricas Comparativas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CockpitMetricCard(
                        title = "Lucro Original",
                        value = "R$ %.2f".format(Locale("pt", "BR"), analysis.originalNetProfit),
                        subtitle = "Sem volta vazia",
                        modifier = Modifier.weight(1f)
                    )
                    CockpitMetricCard(
                        title = "Lucro Real Ajustado",
                        value = "R$ %.2f".format(Locale("pt", "BR"), analysis.adjustedNetProfit),
                        subtitle = "Após volta vazia",
                        accentColor = if (analysis.adjustedNetProfit > 0) ClassGood else ClassAvoid,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CockpitMetricCard(
                        title = "Km Volta Vazia",
                        value = "%.1f km".format(analysis.expectedEmptyReturnKm),
                        subtitle = "Deslocamento sem passageiro",
                        modifier = Modifier.weight(1f)
                    )
                    CockpitMetricCard(
                        title = "Custo Retorno",
                        value = "-R$ %.2f".format(Locale("pt", "BR"), analysis.emptyReturnCost),
                        subtitle = "Gasto em combustível",
                        accentColor = Color(0xFFFF7043),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Alerta e Orientação
                analysis.alertMessage?.let { alert ->
                    Text(
                        text = alert,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB300),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Text(
                    text = analysis.coachingRecommendation,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun PlatformComparisonTabContent(state: GeoInsightsUiState) {
    val report = state.platformReport

    Text(
        text = "COMPARATIVO OPERACIONAL: UBER VS 99",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 6.dp)
    )

    // Card de Recomendação Estratégica
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141923)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BrandPrimary.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CompareArrows,
                contentDescription = null,
                tint = BrandPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = report.recommendation,
                fontSize = 13.sp,
                color = Color.White,
                lineHeight = 18.sp
            )
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    if (report.summaries.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CockpitSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Nenhuma corrida avaliada ainda",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Ao utilizar o simulador ou o copiloto automático na Uber e 99, este painel consolidará o lucro real por hora e margem de cada app.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    } else {
        report.summaries.forEach { summary ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CockpitBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = summary.platform.displayName.uppercase(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = BrandPrimary
                        )
                        Text(
                            text = "${summary.totalOffersEvaluated} corridas",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CockpitMetricCard(
                            title = "Lucro por Hora",
                            value = "R$ %.2f/h".format(Locale("pt", "BR"), summary.averageNetRatePerHour),
                            accentColor = ClassGood,
                            modifier = Modifier.weight(1f)
                        )
                        CockpitMetricCard(
                            title = "R$/KM Bruto",
                            value = "R$ %.2f/km".format(Locale("pt", "BR"), summary.averageGrossRatePerKm),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CockpitMetricCard(
                            title = "Ticket Médio",
                            value = "R$ %.2f".format(Locale("pt", "BR"), summary.averageGrossFare),
                            modifier = Modifier.weight(1f)
                        )
                        CockpitMetricCard(
                            title = "Margem Líquida",
                            value = "%.1f%%".format(summary.profitMarginPercent),
                            accentColor = BrandPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
