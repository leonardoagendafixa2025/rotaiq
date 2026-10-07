package com.rotai.iq.feature.rides

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.rotai.iq.core.domain.model.EvaluationClassification
import com.rotai.iq.core.domain.model.RideEvaluation
import com.rotai.iq.core.domain.model.RideOffer
import com.rotai.iq.core.domain.model.RidePlatform
import com.rotai.iq.core.ui.designsystem.RotaCard
import com.rotai.iq.core.ui.designsystem.RotaChip
import com.rotai.iq.core.ui.designsystem.RotaMetric
import com.rotai.iq.core.ui.designsystem.RotaScore
import com.rotai.iq.core.ui.designsystem.getRotaClassificationColor
import com.rotai.iq.core.ui.designsystem.getRotaClassificationGlow
import com.rotai.iq.core.ui.theme.CardShapeDefault
import com.rotai.iq.core.ui.theme.RotaAvoid
import com.rotai.iq.core.ui.theme.RotaBlack
import com.rotai.iq.core.ui.theme.RotaBorderSubtle
import com.rotai.iq.core.ui.theme.RotaCardBackground
import com.rotai.iq.core.ui.theme.RotaCardElevated
import com.rotai.iq.core.ui.theme.RotaDarkCanvas
import com.rotai.iq.core.ui.theme.RotaExcellent
import com.rotai.iq.core.ui.theme.RotaOrangeLight
import com.rotai.iq.core.ui.theme.RotaOrangePrimary
import com.rotai.iq.core.ui.theme.RotaTextPrimary
import com.rotai.iq.core.ui.theme.RotaTextSecondary
import com.rotai.iq.core.ui.theme.RotaTextTertiary
import com.rotai.iq.core.ui.theme.RotaTextWhite
import java.util.Locale

@Composable
fun RideHistoryScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf("TODAS") }
    var selectedPeriod by remember { mutableStateOf("Hoje") }

    // Corridas fiéis à especificação oficial para exibição e demonstração
    val mockEvaluations = remember {
        listOf(
            RideEvaluation(
                id = "mock-1",
                offer = RideOffer(
                    platform = RidePlatform.UBER,
                    grossFare = 32.80,
                    distanceKm = 8.2,
                    durationMinutes = 22.0,
                    pickupDistanceKm = 1.2,
                    pickupDurationMinutes = 4.0,
                    destinationAddress = "Jardins, São Paulo",
                    rawText = "UberX Centro → Jardins"
                ),
                score = 96,
                classification = EvaluationClassification.EXCELLENT,
                grossFare = 32.80,
                estimatedCost = 6.20,
                netProfit = 26.60,
                profitMarginPercent = 81.1,
                grossRatePerKm = 3.49,
                netRatePerKm = 2.83,
                grossRatePerHour = 75.69,
                netRatePerHour = 61.38,
                grossRatePerMinute = 1.26,
                totalDistanceKm = 9.4,
                totalDurationMinutes = 26.0,
                reasons = listOf("Acima da meta horária", "Zona centro favorável", "Retorno rápido"),
                alerts = emptyList()
            ),
            RideEvaluation(
                id = "mock-2",
                offer = RideOffer(
                    platform = RidePlatform.NINETY_NINE,
                    grossFare = 21.40,
                    distanceKm = 10.2,
                    durationMinutes = 25.0,
                    pickupDistanceKm = 2.0,
                    pickupDurationMinutes = 6.0,
                    destinationAddress = "Morumbi, São Paulo",
                    rawText = "99Pop Moema → Morumbi"
                ),
                score = 64,
                classification = EvaluationClassification.ACCEPTABLE,
                grossFare = 21.40,
                estimatedCost = 8.66,
                netProfit = 12.74,
                profitMarginPercent = 59.5,
                grossRatePerKm = 1.75,
                netRatePerKm = 1.04,
                grossRatePerHour = 41.42,
                netRatePerHour = 24.65,
                grossRatePerMinute = 0.69,
                totalDistanceKm = 12.2,
                totalDurationMinutes = 31.0,
                reasons = listOf("Rentabilidade média", "Risco de espera no Morumbi"),
                alerts = listOf("Retorno ocioso previsto")
            ),
            RideEvaluation(
                id = "mock-3",
                offer = RideOffer(
                    platform = RidePlatform.UBER,
                    grossFare = 15.80,
                    distanceKm = 8.0,
                    durationMinutes = 21.0,
                    pickupDistanceKm = 3.5,
                    pickupDurationMinutes = 8.0,
                    destinationAddress = "Penha, São Paulo",
                    rawText = "UberX Tatuapé → Penha"
                ),
                score = 38,
                classification = EvaluationClassification.AVOID,
                grossFare = 15.80,
                estimatedCost = 8.16,
                netProfit = 7.64,
                profitMarginPercent = 48.3,
                grossRatePerKm = 1.37,
                netRatePerKm = 0.66,
                grossRatePerHour = 32.69,
                netRatePerHour = 15.80,
                grossRatePerMinute = 0.54,
                totalDistanceKm = 11.5,
                totalDurationMinutes = 29.0,
                reasons = listOf("Prejuízo de oportunidade"),
                alerts = listOf("Abaixo do custo/hora mínimo", "Deslocamento até passageiro muito longo")
            )
        )
    }

    val displayList = if (state.evaluations.isNotEmpty()) state.evaluations else mockEvaluations
    val filteredList = displayList.filter { item ->
        when (selectedFilter) {
            "BOAS" -> item.classification == EvaluationClassification.EXCELLENT || item.classification == EvaluationClassification.GOOD
            "RUINS" -> item.classification == EvaluationClassification.BAD || item.classification == EvaluationClassification.AVOID
            else -> true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RotaBlack)
            .padding(16.dp)
    ) {
        // Cabeçalho da Tela
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CORRIDAS",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = RotaTextWhite,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Histórico e análise de decisões",
                    fontSize = 12.sp,
                    color = RotaTextSecondary
                )
            }

            // Seletor de Período Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(RotaCardBackground)
                    .border(1.dp, RotaBorderSubtle, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = RotaOrangePrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = selectedPeriod,
                    color = RotaTextWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Métricas Rápidas do Histórico
        val totalProfit = if (state.evaluations.isNotEmpty()) state.totalSimulatedProfit else displayList.sumOf { it.netProfit }
        val countTotal = displayList.size
        val countGood = displayList.count { it.classification == EvaluationClassification.EXCELLENT || it.classification == EvaluationClassification.GOOD }
        val countBad = displayList.count { it.classification == EvaluationClassification.BAD || it.classification == EvaluationClassification.AVOID }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RotaMetric(
                title = "Avaliadas",
                value = "$countTotal",
                subtitle = "Chamadas",
                modifier = Modifier.weight(1f)
            )
            RotaMetric(
                title = "Lucro Líquido",
                value = "R$ %.2f".format(Locale("pt", "BR"), totalProfit),
                subtitle = "Total capturado",
                accentColor = RotaExcellent,
                modifier = Modifier.weight(1.3f)
            )
            RotaMetric(
                title = "Rejeitadas",
                value = "$countBad",
                subtitle = "Economia R$ 38",
                accentColor = RotaAvoid,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Chips de Filtro
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RotaChip(
                text = "Todas",
                count = countTotal,
                isSelected = selectedFilter == "TODAS",
                onClick = { selectedFilter = "TODAS" }
            )
            RotaChip(
                text = "Boas",
                count = countGood,
                isSelected = selectedFilter == "BOAS",
                onClick = { selectedFilter = "BOAS" }
            )
            RotaChip(
                text = "Ruins",
                count = countBad,
                isSelected = selectedFilter == "RUINS",
                onClick = { selectedFilter = "RUINS" }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Lista de Cards de Corridas
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredList, key = { it.id }) { item ->
                RideHistoryCard(
                    evaluation = item,
                    onDelete = { viewModel.deleteEvaluation(item.id) }
                )
            }
        }
    }
}

@Composable
private fun RideHistoryCard(
    evaluation: RideEvaluation,
    onDelete: () -> Unit
) {
    val statusColor = getRotaClassificationColor(evaluation.classification)
    val statusGlow = getRotaClassificationGlow(evaluation.classification)
    val destName = evaluation.offer.destinationAddress?.split(",")?.firstOrNull() ?: "Destino Urbano"

    RotaCard(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShapeDefault,
        backgroundColor = RotaCardBackground,
        borderColor = statusColor.copy(alpha = 0.35f),
        borderWidth = 1.dp,
        glowColor = statusGlow
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header do Card: Plataforma + Destino + Badge Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Indicador circular colorido
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = evaluation.offer.platform.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = RotaTextWhite
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• $destName",
                        fontSize = 12.sp,
                        color = RotaTextSecondary
                    )
                }

                RotaScore(
                    classification = evaluation.classification,
                    score = evaluation.score,
                    large = false
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Valor Principal Grande e Lucro Líquido
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "R$ %.2f".format(Locale("pt", "BR"), evaluation.grossFare),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = RotaTextWhite,
                    letterSpacing = (-0.5).sp
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "LUCRO LÍQUIDO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = RotaTextSecondary
                    )
                    Text(
                        text = "R$ %.2f".format(Locale("pt", "BR"), evaluation.netProfit),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Linha com 3 Indicadores em Pílula
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniSpec(text = "%.1f km".format(evaluation.totalDistanceKm), modifier = Modifier.weight(1f))
                MiniSpec(text = "%.0f min".format(evaluation.totalDurationMinutes), modifier = Modifier.weight(1f))
                MiniSpec(text = "R$ %.2f/km".format(Locale("pt", "BR"), evaluation.grossRatePerKm), highlight = true, modifier = Modifier.weight(1.2f))
            }

            // Motivos / Insights
            if (evaluation.reasons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "✓ " + evaluation.reasons.first(),
                    fontSize = 11.sp,
                    color = RotaTextTertiary
                )
            }
        }
    }
}

@Composable
private fun MiniSpec(
    text: String,
    highlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (highlight) RotaCardElevated else RotaDarkCanvas)
            .border(1.dp, if (highlight) RotaBorderSubtle else Color.Transparent, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (highlight) RotaOrangeLight else RotaTextSecondary
        )
    }
}
