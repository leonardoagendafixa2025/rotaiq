package com.rotai.iq.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rotai.iq.core.domain.model.FuelType
import com.rotai.iq.core.ui.designsystem.RotaButton
import com.rotai.iq.core.ui.designsystem.RotaButtonVariant
import com.rotai.iq.core.ui.designsystem.RotaCard
import com.rotai.iq.core.ui.theme.RotaAvoid
import com.rotai.iq.core.ui.theme.RotaBorderMedium
import com.rotai.iq.core.ui.theme.RotaBorderSubtle
import com.rotai.iq.core.ui.theme.RotaCardElevated
import com.rotai.iq.core.ui.theme.RotaDarkCanvas
import com.rotai.iq.core.ui.theme.RotaExcellent
import com.rotai.iq.core.ui.theme.RotaOrangeHover
import com.rotai.iq.core.ui.theme.RotaOrangeLight
import com.rotai.iq.core.ui.theme.RotaOrangePrimary
import com.rotai.iq.core.ui.theme.RotaTextPrimary
import com.rotai.iq.core.ui.theme.RotaTextSecondary
import com.rotai.iq.core.ui.theme.RotaTextWhite

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        RotaDarkCanvas,
                        Color(0xFF0F1117),
                        Color(0xFF090A0E)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header com Progresso
            OnboardingHeader(
                currentStep = state.currentStep,
                totalSteps = state.totalSteps
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Conteúdo dos Passos
            when (state.currentStep) {
                1 -> StepOneVehicle(
                    state = state,
                    onNameChanged = viewModel::onVehicleNameChanged,
                    onModelChanged = viewModel::onVehicleModelChanged,
                    onFuelTypeChanged = viewModel::onFuelTypeChanged,
                    onConsumptionChanged = viewModel::onConsumptionChanged,
                    onFuelPriceChanged = viewModel::onFuelPriceChanged
                )
                2 -> StepTwoCosts(
                    state = state,
                    onInsuranceChanged = viewModel::onInsuranceChanged,
                    onTaxesChanged = viewModel::onTaxesChanged,
                    onMaintenanceChanged = viewModel::onMaintenanceChanged,
                    onEstimatedKmChanged = viewModel::onEstimatedKmChanged
                )
                3 -> StepThreeGoalsAndCopilot(
                    state = state,
                    onDailyNetChanged = viewModel::onDailyNetChanged,
                    onDailyGrossChanged = viewModel::onDailyGrossChanged,
                    onShiftHoursChanged = viewModel::onShiftHoursChanged
                )
            }

            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.errorMessage!!,
                    color = RotaAvoid,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botões de Ação
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.currentStep > 1) {
                    RotaButton(
                        text = "Voltar",
                        onClick = { viewModel.previousStep() },
                        variant = RotaButtonVariant.OUTLINE,
                        modifier = Modifier.weight(1f)
                    )
                }

                RotaButton(
                    text = if (state.currentStep == state.totalSteps) "FINALIZAR E ENTRAR" else "Avançar",
                    onClick = {
                        if (state.currentStep < state.totalSteps) {
                            viewModel.nextStep()
                        } else {
                            viewModel.completeOnboarding(onFinishOnboarding)
                        }
                    },
                    variant = RotaButtonVariant.PRIMARY_ORANGE,
                    enabled = !state.isLoading,
                    modifier = Modifier.weight(if (state.currentStep > 1) 2f else 1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Opção de Pular
            Text(
                text = "Pular configuração e ir para o painel",
                color = RotaTextSecondary,
                fontSize = 13.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { viewModel.skipOnboarding(onFinishOnboarding) }
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OnboardingHeader(
    currentStep: Int,
    totalSteps: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(RotaOrangePrimary, RotaOrangeHover)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "ROTA IQ",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = RotaTextWhite,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Indicador numérico de etapas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepIndicatorItem(
                stepNumber = 1,
                title = "Veículo",
                isActive = currentStep >= 1,
                isCurrent = currentStep == 1
            )
            StepDivider(isCompleted = currentStep > 1)
            StepIndicatorItem(
                stepNumber = 2,
                title = "Custos",
                isActive = currentStep >= 2,
                isCurrent = currentStep == 2
            )
            StepDivider(isCompleted = currentStep > 2)
            StepIndicatorItem(
                stepNumber = 3,
                title = "Metas",
                isActive = currentStep >= 3,
                isCurrent = currentStep == 3
            )
        }
    }
}

@Composable
private fun StepIndicatorItem(
    stepNumber: Int,
    title: String,
    isActive: Boolean,
    isCurrent: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (isCurrent) RotaOrangePrimary
                    else if (isActive) RotaExcellent
                    else RotaCardElevated
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isActive && !isCurrent) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = stepNumber.toString(),
                    color = if (isActive) Color.White else RotaTextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = if (isCurrent) RotaOrangeLight else if (isActive) RotaTextPrimary else RotaTextSecondary,
            fontSize = 11.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun StepDivider(isCompleted: Boolean) {
    Box(
        modifier = Modifier
            .width(44.dp)
            .height(2.dp)
            .background(if (isCompleted) RotaExcellent else RotaBorderSubtle)
    )
}

// ======================================================================
// PASSO 1: VEÍCULO & COMBUSTÍVEL
// ======================================================================
@Composable
private fun StepOneVehicle(
    state: OnboardingUiState,
    onNameChanged: (String) -> Unit,
    onModelChanged: (String) -> Unit,
    onFuelTypeChanged: (FuelType) -> Unit,
    onConsumptionChanged: (String) -> Unit,
    onFuelPriceChanged: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "1. Configure seu Veículo",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = RotaTextWhite
        )
        Text(
            text = "O ROTA IQ calcula seu custo real de combustível a cada km rodado.",
            fontSize = 13.sp,
            color = RotaTextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Seletor de Tipo de Combustível
        Text(
            text = "Tipo de Combustível:",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = RotaTextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FuelChip(
                label = "Gasolina",
                isSelected = state.fuelType == FuelType.GASOLINE,
                onClick = { onFuelTypeChanged(FuelType.GASOLINE) },
                modifier = Modifier.weight(1f)
            )
            FuelChip(
                label = "Etanol",
                isSelected = state.fuelType == FuelType.ETHANOL,
                onClick = { onFuelTypeChanged(FuelType.ETHANOL) },
                modifier = Modifier.weight(1f)
            )
            FuelChip(
                label = "GNV",
                isSelected = state.fuelType == FuelType.CNG,
                onClick = { onFuelTypeChanged(FuelType.CNG) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OnboardingTextField(
            label = "Consumo Médio Urbano (km/l)",
            value = state.consumptionInput,
            onValueChange = onConsumptionChanged,
            keyboardType = KeyboardType.Decimal,
            trailingText = "km/L"
        )

        Spacer(modifier = Modifier.height(14.dp))

        OnboardingTextField(
            label = "Preço Pago no Litro (R$)",
            value = state.fuelPriceInput,
            onValueChange = onFuelPriceChanged,
            keyboardType = KeyboardType.Decimal,
            trailingText = "R$/L"
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Card Dinâmico de Custo por KM
        RotaCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocalGasStation,
                    contentDescription = null,
                    tint = RotaOrangePrimary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "CUSTO POR KM DE COMBUSTÍVEL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RotaTextSecondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "R$ ${String.format("%.2f", state.fuelCostPerKm)} / km",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = RotaOrangeLight
                    )
                }
            }
        }
    }
}

// ======================================================================
// PASSO 2: CUSTOS FIXOS & MANUTENÇÃO
// ======================================================================
@Composable
private fun StepTwoCosts(
    state: OnboardingUiState,
    onInsuranceChanged: (String) -> Unit,
    onTaxesChanged: (String) -> Unit,
    onMaintenanceChanged: (String) -> Unit,
    onEstimatedKmChanged: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "2. Custos Reais & Manutenção",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = RotaTextWhite
        )
        Text(
            text = "Nunca rode sem cobrir seguro, IPVA, pneus e troca de óleo.",
            fontSize = 13.sp,
            color = RotaTextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        OnboardingTextField(
            label = "Seguro ou Parcela Mensal (R$/mês)",
            value = state.insuranceMonthlyInput,
            onValueChange = onInsuranceChanged,
            keyboardType = KeyboardType.Decimal,
            trailingText = "R$/mês"
        )

        Spacer(modifier = Modifier.height(14.dp))

        OnboardingTextField(
            label = "IPVA e Licenciamento Anual (R$/ano)",
            value = state.taxesAnnualInput,
            onValueChange = onTaxesChanged,
            keyboardType = KeyboardType.Decimal,
            trailingText = "R$/ano"
        )

        Spacer(modifier = Modifier.height(14.dp))

        OnboardingTextField(
            label = "Reserva de Manutenção Preventiva (R$/km)",
            value = state.maintenancePerKmInput,
            onValueChange = onMaintenanceChanged,
            keyboardType = KeyboardType.Decimal,
            trailingText = "R$/km"
        )

        Spacer(modifier = Modifier.height(14.dp))

        OnboardingTextField(
            label = "Quilômetros Estimados por Mês (km)",
            value = state.estimatedMonthlyKmInput,
            onValueChange = onEstimatedKmChanged,
            keyboardType = KeyboardType.Number,
            trailingText = "km/mês"
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Card Dinâmico com Custo Total por KM
        RotaCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SEU CUSTO TOTAL DE OPERAÇÃO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RotaTextSecondary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "R$ ${String.format("%.2f", state.totalCostPerKm)} / km",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = RotaOrangePrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(RotaOrangePrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = RotaOrangePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "💡 Qualquer corrida que pague menos de R$ ${String.format("%.2f", state.totalCostPerKm)}/km gera prejuízo real no seu bolso.",
                    fontSize = 12.sp,
                    color = RotaTextSecondary
                )
            }
        }
    }
}

// ======================================================================
// PASSO 3: METAS & COPILOTO
// ======================================================================
@Composable
private fun StepThreeGoalsAndCopilot(
    state: OnboardingUiState,
    onDailyNetChanged: (String) -> Unit,
    onDailyGrossChanged: (String) -> Unit,
    onShiftHoursChanged: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "3. Suas Metas & Copiloto HUD",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = RotaTextWhite
        )
        Text(
            text = "Defina seu lucro líquido diário e deixe o Copiloto filtrar para você.",
            fontSize = 13.sp,
            color = RotaTextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        OnboardingTextField(
            label = "Meta de Lucro Líquido no Bolso (R$/dia)",
            value = state.dailyNetInput,
            onValueChange = onDailyNetChanged,
            keyboardType = KeyboardType.Decimal,
            trailingText = "R$/dia"
        )

        Spacer(modifier = Modifier.height(14.dp))

        OnboardingTextField(
            label = "Horas de Trabalho Pretendidas por Dia",
            value = state.shiftHoursInput,
            onValueChange = onShiftHoursChanged,
            keyboardType = KeyboardType.Decimal,
            trailingText = "horas/dia"
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Card de Projeção Mensal
        RotaCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = null,
                    tint = RotaExcellent,
                    modifier = Modifier.size(34.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "LUCRO LÍQUIDO MENSAL PROJETADO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RotaTextSecondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "R$ ${String.format("%.2f", state.monthlyProjectedProfit)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = RotaExcellent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card do Copiloto Inteligente
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF161922))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = RotaOrangePrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Copiloto Flutuante Ativo",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RotaTextWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Na rua, ao receber uma corrida na Uber ou 99, o HUD Flutuante analisa instantaneamente se ela cumpre sua meta horária e de R$/km antes de você aceitar.",
                        fontSize = 12.sp,
                        color = RotaTextSecondary,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

// ======================================================================
// COMPONENTES AUXILIARES
// ======================================================================
@Composable
private fun FuelChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) RotaOrangePrimary else RotaCardElevated)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else RotaTextSecondary
        )
    }
}

@Composable
private fun OnboardingTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    trailingText: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = RotaTextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            trailingIcon = {
                Text(
                    text = trailingText,
                    fontSize = 12.sp,
                    color = RotaTextSecondary,
                    modifier = Modifier.padding(end = 12.dp)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RotaOrangePrimary,
                unfocusedBorderColor = RotaBorderSubtle,
                focusedTextColor = RotaTextWhite,
                unfocusedTextColor = RotaTextWhite,
                focusedContainerColor = RotaCardElevated,
                unfocusedContainerColor = RotaCardElevated
            ),
            shape = RoundedCornerShape(10.dp)
        )
    }
}
