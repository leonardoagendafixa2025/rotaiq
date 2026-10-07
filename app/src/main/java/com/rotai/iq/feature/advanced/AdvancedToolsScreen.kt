package com.rotai.iq.feature.advanced

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rotai.iq.core.domain.engine.CounterOfferAction
import com.rotai.iq.core.domain.engine.DriverTaxCategory
import com.rotai.iq.core.ui.components.CockpitMetricCard
import com.rotai.iq.core.ui.theme.BrandPrimary
import com.rotai.iq.core.ui.theme.BrandSecondary
import com.rotai.iq.core.ui.theme.ClassAcceptable
import com.rotai.iq.core.ui.theme.ClassAvoid
import com.rotai.iq.core.ui.theme.ClassBad
import com.rotai.iq.core.ui.theme.ClassExcellent
import com.rotai.iq.core.ui.theme.CockpitBackground
import com.rotai.iq.core.ui.theme.CockpitBorder
import com.rotai.iq.core.ui.theme.CockpitSurface
import com.rotai.iq.core.ui.theme.CockpitSurfaceVariant
import com.rotai.iq.core.ui.theme.TextPrimary
import com.rotai.iq.core.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun AdvancedToolsScreen(
    viewModel: AdvancedToolsViewModel,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CockpitBackground)
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Voltar",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "RECURSOS AVANÇADOS",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = BrandPrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "INTELIGÊNCIA & MODO CARRO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = state.currentTab.ordinal,
            containerColor = CockpitSurface,
            contentColor = BrandPrimary,
            edgePadding = 0.dp
        ) {
            AdvancedToolsTab.entries.forEach { tab ->
                val isSelected = state.currentTab == tab
                Tab(
                    selected = isSelected,
                    onClick = { viewModel.selectTab(tab) },
                    text = {
                        Text(
                            text = tab.title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) BrandPrimary else TextSecondary
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scrollable Body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            when (state.currentTab) {
                AdvancedToolsTab.CAR_MODE -> CarModeTabContent(
                    state = state,
                    onToggleManual = { viewModel.toggleManualCarMode(context) },
                    onAutoStartChanged = { viewModel.updateAutoStartEnabled(it) },
                    onAutoStopChanged = { viewModel.updateAutoStopEnabled(it) },
                    onAnnounceChanged = { viewModel.updateAnnounceEnabled(it) }
                )
                AdvancedToolsTab.INDRIVE -> InDriveTabContent(
                    state = state,
                    onPassengerFareChange = { viewModel.updatePassengerOffer(it) },
                    onTripKmChange = { viewModel.updateTripDistance(it) },
                    onPickupKmChange = { viewModel.updatePickupDistance(it) },
                    onTripDurationChange = { viewModel.updateTripDuration(it) },
                    onToggleDelivery = { viewModel.toggleDeliveryMode(it) }
                )
                AdvancedToolsTab.TAX_REPORT -> TaxReportTabContent(
                    state = state,
                    onCategoryChange = { viewModel.updateTaxCategory(it) },
                    onExportCsv = { viewModel.exportCashBook() },
                    onDismissMessage = { viewModel.dismissExportMessage() }
                )
                AdvancedToolsTab.REJECTION -> RejectionTabContent(
                    state = state,
                    onBadFareChange = { viewModel.updateBadFare(it) },
                    onBadDistanceChange = { viewModel.updateBadDistance(it) },
                    onBadDurationChange = { viewModel.updateBadDuration(it) },
                    onTargetHourlyChange = { viewModel.updateTargetHourly(it) }
                )
            }
        }
    }
}

@Composable
private fun CarModeTabContent(
    state: AdvancedToolsUiState,
    onToggleManual: () -> Unit,
    onAutoStartChanged: (Boolean) -> Unit,
    onAutoStopChanged: (Boolean) -> Unit,
    onAnnounceChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = if (state.isCarModeActive) ClassExcellent else CockpitBorder,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = CockpitSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (state.isCarModeActive) ClassExcellent else ClassAvoid)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (state.isCarModeActive) "MODO CARRO ATIVO" else "MODO CARRO EM ESPERA",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.isCarModeActive) ClassExcellent else TextSecondary
                    )
                }

                Icon(
                    imageVector = if (state.isCarModeActive) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                    contentDescription = "Bluetooth Status",
                    tint = if (state.isCarModeActive) BrandPrimary else TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (state.connectedDevice != null)
                    "Conectado a: ${state.connectedDevice}"
                else
                    "Nenhum multimídia veicular pareado no momento.",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onToggleManual,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isCarModeActive) ClassAvoid else BrandPrimary
                )
            ) {
                Text(
                    text = if (state.isCarModeActive) "DESATIVAR MODO CARRO" else "ATIVAR MODO CARRO AGORA",
                    fontWeight = FontWeight.Bold,
                    color = CockpitBackground
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "AUTOMAÇÃO BLUETOOTH AO VOLANTE",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = BrandPrimary
    )

    Spacer(modifier = Modifier.height(8.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CockpitSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Iniciar Copiloto Automaticamente",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Abre o HUD flutuante assim que o Bluetooth do carro conecta",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = state.carConfig.autoStartEnabled,
                    onCheckedChange = onAutoStartChanged,
                    colors = SwitchDefaults.colors(checkedThumbColor = BrandPrimary)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Encerrar ao Desconectar",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Fecha os serviços em segundo plano ao desligar a chave",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = state.carConfig.autoStopEnabled,
                    onCheckedChange = onAutoStopChanged,
                    colors = SwitchDefaults.colors(checkedThumbColor = BrandPrimary)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Aviso por Voz (TTS)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Vocaliza status de conexão sem precisar olhar a tela",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = state.carConfig.announceCarModeActive,
                    onCheckedChange = onAnnounceChanged,
                    colors = SwitchDefaults.colors(checkedThumbColor = BrandPrimary)
                )
            }
        }
    }
}

@Composable
private fun InDriveTabContent(
    state: AdvancedToolsUiState,
    onPassengerFareChange: (String) -> Unit,
    onTripKmChange: (String) -> Unit,
    onPickupKmChange: (String) -> Unit,
    onTripDurationChange: (String) -> Unit,
    onToggleDelivery: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CockpitSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SIMULADOR DE OFERTAS INDRIVE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Entrega Flash",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = state.isDeliveryMode,
                        onCheckedChange = onToggleDelivery,
                        colors = SwitchDefaults.colors(checkedThumbColor = BrandPrimary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.passengerOfferInput,
                    onValueChange = onPassengerFareChange,
                    label = { Text("Oferta Passageiro (R$)") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrandPrimary,
                        unfocusedBorderColor = CockpitBorder
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = state.tripDistanceInput,
                    onValueChange = onTripKmChange,
                    label = { Text("Distância (km)") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrandPrimary,
                        unfocusedBorderColor = CockpitBorder
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.pickupDistanceInput,
                    onValueChange = onPickupKmChange,
                    label = { Text("Até o Ponto (km)") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrandPrimary,
                        unfocusedBorderColor = CockpitBorder
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = state.tripDurationInput,
                    onValueChange = onTripDurationChange,
                    label = { Text("Duração (min)") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrandPrimary,
                        unfocusedBorderColor = CockpitBorder
                    )
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    val result = state.inDriveResult
    if (result != null) {
        val actionColor = when (result.recommendedAction) {
            CounterOfferAction.ACCEPT_IMMEDIATELY -> ClassExcellent
            CounterOfferAction.COUNTER_OFFER -> ClassAcceptable
            CounterOfferAction.AVOID_RIDE -> ClassAvoid
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, actionColor, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = CockpitSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = result.recommendedAction.label.uppercase(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = actionColor
                    )
                    Text(
                        text = result.recommendedAction.emoji,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = result.explanation,
                    fontSize = 12.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    CockpitMetricCard(
                        title = "Custo Veículo",
                        value = "R$ %.2f".format(Locale("pt", "BR"), result.tripCost),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    CockpitMetricCard(
                        title = "Lucro Oferta",
                        value = "R$ %.2f".format(Locale("pt", "BR"), result.passengerNetProfit),
                        accentColor = if (result.passengerNetProfit >= 0) ClassExcellent else ClassAvoid,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "OPÇÕES DE CONTRAPROPOSTA DISPONÍVEIS:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                result.steps.take(4).forEach { step ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(
                                if (step.isRecommended) BrandPrimary.copy(alpha = 0.15f) else CockpitSurfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Pedir R$ %.2f (+R$ %.0f)".format(
                                        Locale("pt", "BR"),
                                        step.totalFare,
                                        step.deltaFare
                                    ),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (step.isRecommended) BrandPrimary else TextPrimary
                                )
                                if (step.isRecommended) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "★ RECOMENDADO",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = BrandPrimary
                                    )
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Sobra R$ %.2f".format(Locale("pt", "BR"), step.netProfit),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ClassExcellent
                            )
                            Text(
                                text = "R$ %.2f/h".format(Locale("pt", "BR"), step.netHourlyRate),
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaxReportTabContent(
    state: AdvancedToolsUiState,
    onCategoryChange: (DriverTaxCategory) -> Unit,
    onExportCsv: () -> Unit,
    onDismissMessage: () -> Unit
) {
    val summary = state.taxSummary

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CockpitSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "ENQUADRAMENTO FISCAL DO MOTORISTA",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = BrandPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { onCategoryChange(DriverTaxCategory.PASSENGER_TRANSPORT) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.selectedTaxCategory == DriverTaxCategory.PASSENGER_TRANSPORT)
                            BrandPrimary else CockpitSurfaceVariant
                    )
                ) {
                    Text(
                        text = "Passageiros (16%)",
                        fontSize = 11.sp,
                        color = if (state.selectedTaxCategory == DriverTaxCategory.PASSENGER_TRANSPORT)
                            CockpitBackground else TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onCategoryChange(DriverTaxCategory.CARGO_DELIVERY) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.selectedTaxCategory == DriverTaxCategory.CARGO_DELIVERY)
                            BrandPrimary else CockpitSurfaceVariant
                    )
                ) {
                    Text(
                        text = "Entregas (60%)",
                        fontSize = 11.sp,
                        color = if (state.selectedTaxCategory == DriverTaxCategory.CARGO_DELIVERY)
                            CockpitBackground else TextPrimary
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (summary != null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CockpitSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "MONITORAMENTO DE TETO MEI ANUAL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "R$ %.2f de R$ 81.000,00".format(Locale("pt", "BR"), summary.grossRevenue),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "%.1f%%".format(Locale("pt", "BR"), summary.meiUsagePercentage),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (summary.isExceedingMeiLimit) ClassAvoid else ClassExcellent
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { (summary.meiUsagePercentage / 100.0).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (summary.isExceedingMeiLimit) ClassAvoid else BrandPrimary,
                    trackColor = CockpitSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid DRE Fiscal
        Row(modifier = Modifier.fillMaxWidth()) {
            CockpitMetricCard(
                title = "Parcela Isenta",
                value = "R$ %.2f".format(Locale("pt", "BR"), summary.exemptPortion),
                accentColor = ClassExcellent,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            CockpitMetricCard(
                title = "Despesas Dedutíveis",
                value = "R$ %.2f".format(Locale("pt", "BR"), summary.totalDeductibleExpenses),
                accentColor = BrandPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            CockpitMetricCard(
                title = "Tributável Líquido",
                value = "R$ %.2f".format(Locale("pt", "BR"), summary.netTaxableIncome),
                accentColor = if (summary.isIrpfExempt) ClassExcellent else ClassAvoid,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            CockpitMetricCard(
                title = "Status IRPF",
                value = if (summary.isIrpfExempt) "ISENTO" else "TRIBUTÁVEL",
                accentColor = if (summary.isIrpfExempt) ClassExcellent else ClassAvoid,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (summary.isIrpfExempt) ClassExcellent else ClassAcceptable,
                    RoundedCornerShape(12.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = CockpitSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (summary.isIrpfExempt) Icons.Default.Shield else Icons.Default.Warning,
                        contentDescription = "Diagnóstico Fiscal",
                        tint = if (summary.isIrpfExempt) ClassExcellent else ClassAcceptable
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PARECER CONTÁBIL INTELIGENTE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (summary.isIrpfExempt) ClassExcellent else ClassAcceptable
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = summary.taxDiagnosis,
                    fontSize = 12.sp,
                    color = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.exportSuccessMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ClassExcellent.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.exportSuccessMessage,
                        fontSize = 12.sp,
                        color = ClassExcellent,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismissMessage) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "OK",
                            tint = ClassExcellent
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = onExportCsv,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Exportar",
                tint = CockpitBackground
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "EXPORTAR LIVRO CAIXA EM CSV",
                fontWeight = FontWeight.Bold,
                color = CockpitBackground
            )
        }
    }
}

@Composable
private fun RejectionTabContent(
    state: AdvancedToolsUiState,
    onBadFareChange: (String) -> Unit,
    onBadDistanceChange: (String) -> Unit,
    onBadDurationChange: (String) -> Unit,
    onTargetHourlyChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CockpitSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "SIMULADOR DE REJEIÇÃO ESTRATÉGICA",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = BrandPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.badFareInput,
                    onValueChange = onBadFareChange,
                    label = { Text("Valor Bruto (R$)") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrandPrimary,
                        unfocusedBorderColor = CockpitBorder
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = state.badDistanceInput,
                    onValueChange = onBadDistanceChange,
                    label = { Text("Distância Total (km)") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrandPrimary,
                        unfocusedBorderColor = CockpitBorder
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.badDurationInput,
                    onValueChange = onBadDurationChange,
                    label = { Text("Tempo Total (min)") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrandPrimary,
                        unfocusedBorderColor = CockpitBorder
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = state.targetHourlyInput,
                    onValueChange = onTargetHourlyChange,
                    label = { Text("Meta Horária (R$/h)") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrandPrimary,
                        unfocusedBorderColor = CockpitBorder
                    )
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    val analysis = state.rejectionAnalysis
    if (analysis != null) {
        val verdictColor = if (analysis.badOfferNetProfit <= 0) ClassAvoid else ClassBad

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, verdictColor, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = CockpitSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = analysis.verdict,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = verdictColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = analysis.tacticalAdvice,
                    fontSize = 12.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    CockpitMetricCard(
                        title = "Espera Tolerável",
                        value = "%.0f min".format(analysis.breakEvenWaitMinutes),
                        accentColor = BrandPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    CockpitMetricCard(
                        title = "Custo Evitado",
                        value = "R$ %.2f".format(Locale("pt", "BR"), analysis.avoidedOperatingCost),
                        accentColor = ClassExcellent,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    CockpitMetricCard(
                        title = "Lucro desta Oferta",
                        value = "R$ %.2f".format(Locale("pt", "BR"), analysis.badOfferNetProfit),
                        accentColor = if (analysis.badOfferNetProfit >= 0) ClassAcceptable else ClassAvoid,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    CockpitMetricCard(
                        title = "Retorno por Hora",
                        value = "R$ %.2f/h".format(Locale("pt", "BR"), analysis.badOfferHourlyRate),
                        accentColor = if (analysis.badOfferHourlyRate >= analysis.targetHourlyRate) ClassExcellent else ClassAvoid,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
