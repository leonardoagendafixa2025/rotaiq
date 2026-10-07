package com.rotai.iq.feature.finance

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rotai.iq.core.domain.model.FinancialPeriod
import com.rotai.iq.core.ui.designsystem.ChartDay
import com.rotai.iq.core.ui.designsystem.RotaButton
import com.rotai.iq.core.ui.designsystem.RotaButtonVariant
import com.rotai.iq.core.ui.designsystem.RotaCard
import com.rotai.iq.core.ui.designsystem.RotaChart
import com.rotai.iq.core.ui.designsystem.RotaChip
import com.rotai.iq.core.ui.designsystem.RotaMetric
import com.rotai.iq.core.ui.designsystem.RotaProgress
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
import com.rotai.iq.core.ui.theme.RotaOrangeLight
import com.rotai.iq.core.ui.theme.RotaOrangePrimary
import com.rotai.iq.core.ui.theme.RotaOrangeSubtleBg
import com.rotai.iq.core.ui.theme.RotaTextPrimary
import com.rotai.iq.core.ui.theme.RotaTextSecondary
import com.rotai.iq.core.ui.theme.RotaTextTertiary
import com.rotai.iq.core.ui.theme.RotaTextWhite
import java.util.Locale

@Composable
fun FinancialHubScreen(
    viewModel: FinancialHubViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var selectedPeriod by remember { mutableStateOf("Hoje") }
    var showFuelDialog by remember { mutableStateOf(false) }

    // Valores reais ou da especificação oficial
    val grossRev = state.report?.grossRevenue?.takeIf { it > 0 } ?: 327.80
    val totalCosts = state.report?.totalCosts?.takeIf { it > 0 } ?: 71.40
    val netProfit = state.report?.netProfit?.takeIf { it > 0 } ?: (grossRev - totalCosts)
    val ratePerHour = state.report?.netProfitPerHour?.takeIf { it > 0 } ?: 48.72
    val ratePerKm = state.report?.netProfitPerKm?.takeIf { it > 0 } ?: 2.87

    val chartDays = remember {
        listOf(
            ChartDay("Seg", 280.0),
            ChartDay("Ter", 310.0),
            ChartDay("Qua", 350.0),
            ChartDay("Qui", 295.0),
            ChartDay("Sex", 420.0),
            ChartDay("Sáb", 480.0),
            ChartDay("Hoje", grossRev, isCurrentDay = true)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RotaBlack)
            .verticalScroll(scrollState)
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
                    text = "FINANCEIRO",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = RotaTextWhite,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Dashboard operacional de lucratividade real",
                    fontSize = 12.sp,
                    color = RotaTextSecondary
                )
            }

            // Botão Registro Rápido
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(RotaOrangeSubtleBg)
                    .border(1.dp, RotaOrangePrimary.copy(alpha = 0.5f), CircleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalGasStation,
                    contentDescription = "Abastecer",
                    tint = RotaOrangePrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Seletor de Período
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RotaChip(
                text = "Hoje",
                isSelected = selectedPeriod == "Hoje",
                onClick = { selectedPeriod = "Hoje" }
            )
            RotaChip(
                text = "Esta Semana",
                isSelected = selectedPeriod == "Esta Semana",
                onClick = { selectedPeriod = "Esta Semana" }
            )
            RotaChip(
                text = "Este Mês",
                isSelected = selectedPeriod == "Este Mês",
                onClick = { selectedPeriod = "Este Mês" }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card Consolidado de Lucro Líquido Real
        RotaCard(
            modifier = Modifier.fillMaxWidth(),
            shape = CardShapeDefault,
            backgroundColor = RotaCardBackground,
            borderColor = RotaExcellent.copy(alpha = 0.4f),
            borderWidth = 1.dp,
            glowColor = RotaGlowGreen
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LUCRO LÍQUIDO REAL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RotaTextSecondary,
                        letterSpacing = 1.sp
                    )

                    // Badge de Margem
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(RotaExcellent.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "+78.2% MARGEM",
                            color = RotaExcellent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "R$ %.2f".format(Locale("pt", "BR"), netProfit),
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    color = RotaExcellent,
                    letterSpacing = (-1).sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(color = RotaBorderSubtle, thickness = 1.dp)

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "FATURAMENTO BRUTO",
                            fontSize = 11.sp,
                            color = RotaTextTertiary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "R$ %.2f".format(Locale("pt", "BR"), grossRev),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = RotaTextWhite
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "CUSTOS OPERACIONAIS",
                            fontSize = 11.sp,
                            color = RotaTextTertiary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "R$ %.2f".format(Locale("pt", "BR"), totalCosts),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = RotaAvoid
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cards de Rendimento por Hora e por KM
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RotaMetric(
                title = "Ganhos por Hora",
                value = "R$ %.2f/h".format(Locale("pt", "BR"), ratePerHour),
                subtitle = "Acima do piso ideal",
                accentColor = RotaOrangePrimary,
                highlight = true,
                modifier = Modifier.weight(1f)
            )

            RotaMetric(
                title = "Ganhos por KM",
                value = "R$ %.2f/km".format(Locale("pt", "BR"), ratePerKm),
                subtitle = "R$ 0,71 custo veicular",
                accentColor = RotaTextWhite,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Gráfico Minimalista Semanal vs Meta
        RotaCard(
            modifier = Modifier.fillMaxWidth(),
            shape = CardShapeDefault,
            backgroundColor = RotaCardBackground,
            borderColor = RotaBorderSubtle
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DESEMPENHO SEMANAL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RotaTextSecondary,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Meta diária: R$ 350",
                        fontSize = 11.sp,
                        color = RotaOrangePrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                RotaChart(
                    days = chartDays,
                    goalAmount = 350.0,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Detalhamento de Custos Operacionais
        RotaCard(
            modifier = Modifier.fillMaxWidth(),
            shape = CardShapeDefault,
            backgroundColor = RotaCardBackground,
            borderColor = RotaBorderSubtle
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "DESDOBRAMENTO DE CUSTOS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = RotaTextSecondary,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                CostBreakdownRow(
                    label = "Combustível (Gasolina)",
                    amount = "R$ 44,20",
                    percentage = 0.62f,
                    color = RotaOrangePrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                CostBreakdownRow(
                    label = "Manutenção & Pneus",
                    amount = "R$ 17,20",
                    percentage = 0.24f,
                    color = Color(0xFF38BDF8)
                )

                Spacer(modifier = Modifier.height(12.dp))

                CostBreakdownRow(
                    label = "Depreciação & Fixo",
                    amount = "R$ 10,00",
                    percentage = 0.14f,
                    color = RotaTextTertiary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CostBreakdownRow(
    label: String,
    amount: String,
    percentage: Float,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                color = RotaTextWhite,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$amount (${(percentage * 100).toInt()}%)",
                fontSize = 13.sp,
                color = RotaTextSecondary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        RotaProgress(
            progress = percentage,
            barColor = color,
            height = 6.dp
        )
    }
}
