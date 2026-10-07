package com.rotai.iq.feature.finance

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Payments
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rotai.iq.core.domain.model.FinancialPeriod
import com.rotai.iq.core.ui.designsystem.RotaButton
import com.rotai.iq.core.ui.designsystem.RotaButtonVariant
import com.rotai.iq.core.ui.designsystem.RotaCard
import com.rotai.iq.core.ui.designsystem.RotaChip
import com.rotai.iq.core.ui.designsystem.RotaDialog
import com.rotai.iq.core.ui.designsystem.RotaEmptyState
import com.rotai.iq.core.ui.designsystem.RotaMetric
import com.rotai.iq.core.ui.designsystem.RotaProgress
import com.rotai.iq.core.ui.theme.CardShapeDefault
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
    var showFuelDialog by remember { mutableStateOf(false) }

    // Dados reais do banco de dados (Room)
    val report = state.report
    val grossRev = report?.grossRevenue ?: 0.0
    val totalCosts = report?.totalCosts ?: 0.0
    val netProfit = report?.netProfit ?: 0.0
    val ratePerHour = report?.netProfitPerHour ?: 0.0
    val ratePerKm = report?.netProfitPerKm ?: 0.0
    val marginPercent = report?.profitMarginPercent ?: 0.0
    val hasFinancialActivity = grossRev > 0.0 || totalCosts > 0.0

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
                    text = "Gestão de lucratividade e custos reais",
                    fontSize = 12.sp,
                    color = RotaTextSecondary
                )
            }

            // Botão Registro Rápido de Combustível
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(RotaOrangeSubtleBg)
                    .border(1.dp, RotaOrangePrimary.copy(alpha = 0.5f), CircleShape)
                    .clickable { showFuelDialog = true }
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
                isSelected = state.selectedPeriod == FinancialPeriod.DAILY,
                onClick = { viewModel.onPeriodSelected(FinancialPeriod.DAILY) }
            )
            RotaChip(
                text = "Esta Semana",
                isSelected = state.selectedPeriod == FinancialPeriod.WEEKLY,
                onClick = { viewModel.onPeriodSelected(FinancialPeriod.WEEKLY) }
            )
            RotaChip(
                text = "Este Mês",
                isSelected = state.selectedPeriod == FinancialPeriod.MONTHLY,
                onClick = { viewModel.onPeriodSelected(FinancialPeriod.MONTHLY) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!hasFinancialActivity) {
            RotaEmptyState(
                title = "Nenhum dado financeiro para o período",
                description = "Você ainda não possui corridas avaliadas ou despesas registradas neste período. Registre abastecimentos ou avalie corridas para visualizar faturamento e lucro líquido real.",
                icon = Icons.Default.Payments,
                actionButtonText = "Registrar Abastecimento",
                onActionClick = { showFuelDialog = true }
            )
        } else {
            // Card Consolidado de Lucro Líquido Real
            RotaCard(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShapeDefault,
                backgroundColor = RotaCardBackground,
                borderColor = if (netProfit >= 0) RotaExcellent.copy(alpha = 0.4f) else RotaAvoid.copy(alpha = 0.4f),
                borderWidth = 1.dp,
                glowColor = if (netProfit >= 0) RotaGlowGreen else null
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

                        // Badge de Margem Real
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (marginPercent >= 0) RotaExcellent.copy(alpha = 0.15f) else RotaAvoid.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "%.1f%% MARGEM".format(Locale("pt", "BR"), marginPercent),
                                color = if (marginPercent >= 0) RotaExcellent else RotaAvoid,
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
                        color = if (netProfit >= 0) RotaExcellent else RotaAvoid,
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

            // Cards de Rendimento por Hora e por KM Reais
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RotaMetric(
                    title = "Lucro por Hora",
                    value = "R$ %.2f/h".format(Locale("pt", "BR"), ratePerHour),
                    subtitle = if (ratePerHour > 0) "Em operação" else "Sem tempo ativo",
                    accentColor = RotaOrangePrimary,
                    highlight = true,
                    modifier = Modifier.weight(1f)
                )

                RotaMetric(
                    title = "Lucro por KM",
                    value = "R$ %.2f/km".format(Locale("pt", "BR"), ratePerKm),
                    subtitle = "Líquido real",
                    accentColor = RotaTextWhite,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Detalhamento de Custos Operacionais Reais
            if (report != null && totalCosts > 0) {
                val fuel = report.fuelCosts
                val maint = report.maintenanceCosts
                val fixed = report.fixedCosts + report.otherExpenses
                val fuelPct = if (totalCosts > 0) (fuel / totalCosts).toFloat() else 0f
                val maintPct = if (totalCosts > 0) (maint / totalCosts).toFloat() else 0f
                val fixedPct = if (totalCosts > 0) (fixed / totalCosts).toFloat() else 0f

                RotaCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShapeDefault,
                    backgroundColor = RotaCardBackground,
                    borderColor = RotaBorderSubtle
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "DESDOBRAMENTO DE CUSTOS REAIS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RotaTextSecondary,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        CostBreakdownRow(
                            label = "Combustível",
                            amount = "R$ %.2f".format(Locale("pt", "BR"), fuel),
                            percentage = fuelPct,
                            color = RotaOrangePrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        CostBreakdownRow(
                            label = "Manutenção & Desgaste",
                            amount = "R$ %.2f".format(Locale("pt", "BR"), maint),
                            percentage = maintPct,
                            color = Color(0xFF38BDF8)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        CostBreakdownRow(
                            label = "Custos Fixos & Depreciação",
                            amount = "R$ %.2f".format(Locale("pt", "BR"), fixed),
                            percentage = fixedPct,
                            color = RotaTextTertiary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Modal de Registro de Abastecimento
    if (showFuelDialog) {
        RotaDialog(
            title = "Novo Abastecimento",
            onDismissRequest = { showFuelDialog = false },
            confirmButtonText = "Salvar",
            onConfirm = {
                viewModel.addFuelRecord()
                showFuelDialog = false
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = state.fuelOdometerInput,
                    onValueChange = { viewModel.onFuelOdometerChanged(it) },
                    label = { Text("Odômetro atual (km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RotaOrangePrimary,
                        unfocusedBorderColor = RotaBorderSubtle,
                        focusedTextColor = RotaTextWhite,
                        unfocusedTextColor = RotaTextWhite
                    )
                )

                OutlinedTextField(
                    value = state.fuelLitersInput,
                    onValueChange = { viewModel.onFuelLitersChanged(it) },
                    label = { Text("Litros abastecidos") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RotaOrangePrimary,
                        unfocusedBorderColor = RotaBorderSubtle,
                        focusedTextColor = RotaTextWhite,
                        unfocusedTextColor = RotaTextWhite
                    )
                )

                OutlinedTextField(
                    value = state.fuelPriceInput,
                    onValueChange = { viewModel.onFuelPriceChanged(it) },
                    label = { Text("Preço por litro (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RotaOrangePrimary,
                        unfocusedBorderColor = RotaBorderSubtle,
                        focusedTextColor = RotaTextWhite,
                        unfocusedTextColor = RotaTextWhite
                    )
                )
            }
        }
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
            progress = percentage.coerceIn(0f, 1f),
            barColor = color,
            height = 6.dp
        )
    }
}
