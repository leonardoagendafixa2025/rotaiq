package com.rotai.iq.feature.rides

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rotai.iq.core.ui.components.CockpitMetricCard
import com.rotai.iq.core.ui.components.ScoreBadge
import com.rotai.iq.core.ui.theme.BrandPrimary
import com.rotai.iq.core.ui.theme.ClassAvoid
import com.rotai.iq.core.ui.theme.ClassExcellent
import com.rotai.iq.core.ui.theme.CockpitBackground
import com.rotai.iq.core.ui.theme.CockpitBorder
import com.rotai.iq.core.ui.theme.CockpitSurface
import com.rotai.iq.core.ui.theme.TextPrimary
import com.rotai.iq.core.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun RideHistoryScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CockpitBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "HISTÓRICO DE AVALIAÇÕES",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = BrandPrimary,
            letterSpacing = 0.5.sp
        )
        Text(
            text = "Registro local de todas as ofertas analisadas",
            fontSize = 13.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // History Summary
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CockpitMetricCard(
                title = "Total Avaliadas",
                value = "${state.totalCount}",
                subtitle = "Corridas",
                accentColor = BrandPrimary,
                modifier = Modifier.weight(1f)
            )
            CockpitMetricCard(
                title = "Lucro Simulado",
                value = "R$ %.2f".format(Locale("pt", "BR"), state.totalSimulatedProfit),
                subtitle = "Potencial",
                accentColor = ClassExcellent,
                modifier = Modifier.weight(1f)
            )
            CockpitMetricCard(
                title = "Score Médio",
                value = "%.0f".format(state.averageScore),
                subtitle = "/100",
                accentColor = TextPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.evaluations.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Nenhuma corrida avaliada ainda.",
                    color = TextSecondary,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Use o Simulador para testar e salvar ofertas.",
                    color = BrandPrimary,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.evaluations, key = { it.id }) { evaluation ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                ScoreBadge(
                                    classification = evaluation.classification,
                                    score = evaluation.score
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = evaluation.offer.platform.displayName,
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    IconButton(
                                        onClick = { viewModel.deleteEvaluation(evaluation.id) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remover",
                                            tint = ClassAvoid.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "R$ %.2f".format(Locale("pt", "BR"), evaluation.grossFare),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "%.1f km • %d min • R$ %.2f/km".format(
                                            Locale("pt", "BR"),
                                            evaluation.offer.distanceKm,
                                            evaluation.offer.durationMinutes.toInt(),
                                            evaluation.grossRatePerKm
                                        ),
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Lucro: R$ %.2f".format(Locale("pt", "BR"), evaluation.netProfit),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (evaluation.netProfit > 0) ClassExcellent else ClassAvoid
                                    )
                                    Text(
                                        text = "Custo: R$ %.2f".format(Locale("pt", "BR"), evaluation.estimatedCost),
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
