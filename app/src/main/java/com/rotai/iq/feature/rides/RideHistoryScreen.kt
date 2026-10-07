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
import androidx.compose.material.icons.filled.Calculate
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
import com.rotai.iq.core.ui.designsystem.RotaCard
import com.rotai.iq.core.ui.designsystem.RotaChip
import com.rotai.iq.core.ui.designsystem.RotaEmptyState
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
    onNavigateToSimulator: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf("TODAS") }

    val filteredList = state.evaluations.filter { item ->
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
                    text = "Histórico e análise de decisões reais",
                    fontSize = 12.sp,
                    color = RotaTextSecondary
                )
            }

            // Contador de avaliações salvas
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
                    text = "${state.totalCount} salvas",
                    color = RotaTextWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Métricas Rápidas do Histórico Real
        val countTotal = state.totalCount
        val countGood = state.evaluations.count { it.classification == EvaluationClassification.EXCELLENT || it.classification == EvaluationClassification.GOOD }
        val countBad = state.evaluations.count { it.classification == EvaluationClassification.BAD || it.classification == EvaluationClassification.AVOID }

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
                value = "R$ %.2f".format(Locale("pt", "BR"), state.totalSimulatedProfit),
                subtitle = "Total acumulado",
                accentColor = if (state.totalSimulatedProfit > 0) RotaExcellent else RotaTextSecondary,
                modifier = Modifier.weight(1.3f)
            )
            RotaMetric(
                title = "Rejeitadas",
                value = "$countBad",
                subtitle = "Evitadas",
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

        if (state.evaluations.isEmpty()) {
            // Estado Vazio Estrito — Zero Mocks em Produção
            RotaEmptyState(
                title = "Você ainda não possui corridas analisadas",
                description = "Ative o copiloto ou utilize o simulador de ofertas para avaliar corridas com base no custo real do seu veículo.",
                icon = Icons.Default.Calculate,
                actionButtonText = "Simular Corrida",
                onActionClick = onNavigateToSimulator,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else if (filteredList.isEmpty()) {
            RotaEmptyState(
                title = "Nenhuma corrida neste filtro",
                description = "Não há corridas salvas na categoria selecionada ($selectedFilter).",
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            // Lista de Cards de Corridas Reais
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
}

@Composable
private fun RideHistoryCard(
    evaluation: RideEvaluation,
    onDelete: () -> Unit
) {
    val glowColor = getRotaClassificationGlow(evaluation.classification)
    val accentColor = getRotaClassificationColor(evaluation.classification)

    RotaCard(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShapeDefault,
        backgroundColor = RotaCardBackground,
        borderColor = accentColor.copy(alpha = 0.35f),
        borderWidth = 1.dp,
        glowColor = glowColor
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Linha Superior: Score + Plataforma + Ação Deletar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RotaScore(
                        classification = evaluation.classification,
                        score = evaluation.score
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(RotaCardElevated)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = evaluation.offer.platform.displayName.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RotaTextWhite
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remover",
                        tint = RotaTextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Valor Bruto + Lucro Líquido
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "R$ %.2f".format(Locale("pt", "BR"), evaluation.grossFare),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = RotaTextWhite
                    )
                    Text(
                        text = "%.1f km • %.0f min".format(
                            Locale("pt", "BR"),
                            evaluation.totalDistanceKm,
                            evaluation.totalDurationMinutes
                        ),
                        fontSize = 12.sp,
                        color = RotaTextSecondary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "LUCRO ESTIMADO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = RotaTextTertiary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "R$ %.2f".format(Locale("pt", "BR"), evaluation.netProfit),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = if (evaluation.netProfit > 0) RotaExcellent else RotaAvoid
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-métricas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(RotaDarkCanvas)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "R$ %.2f/km".format(Locale("pt", "BR"), evaluation.grossRatePerKm),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = RotaOrangeLight
                )
                Text(
                    text = "R$ %.2f/h".format(Locale("pt", "BR"), evaluation.grossRatePerHour),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = RotaTextWhite
                )
                Text(
                    text = "Custo R$ %.2f".format(Locale("pt", "BR"), evaluation.estimatedCost),
                    fontSize = 12.sp,
                    color = RotaTextTertiary
                )
            }

            if (evaluation.reasons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = evaluation.reasons.first(),
                    fontSize = 11.sp,
                    color = RotaTextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}
