package com.rotai.iq.feature.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rotai.iq.core.domain.model.FinancialPeriod
import com.rotai.iq.core.domain.model.MaintenanceType
import com.rotai.iq.core.ui.components.CockpitMetricCard
import com.rotai.iq.core.ui.theme.BrandPrimary
import com.rotai.iq.core.ui.theme.ClassAvoid
import com.rotai.iq.core.ui.theme.ClassBad
import com.rotai.iq.core.ui.theme.ClassExcellent
import com.rotai.iq.core.ui.theme.CockpitBackground
import com.rotai.iq.core.ui.theme.CockpitBorder
import com.rotai.iq.core.ui.theme.CockpitSurface
import com.rotai.iq.core.ui.theme.CockpitSurfaceVariant
import com.rotai.iq.core.ui.theme.TextPrimary
import com.rotai.iq.core.ui.theme.TextSecondary
import com.rotai.iq.feature.rides.outlinedColors
import java.util.Locale

@Composable
fun FinancialHubScreen(
    viewModel: FinancialHubViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CockpitBackground)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Cockpit Title
        Text(
            text = "CENTRAL FINANCEIRA",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = BrandPrimary,
            letterSpacing = 0.5.sp
        )
        Text(
            text = "Faturamento, custos reais, combustível, manutenção e lucro líquido",
            fontSize = 13.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Period Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FinancialPeriod.entries.forEach { period ->
                FilterChip(
                    selected = state.selectedPeriod == period,
                    onClick = { viewModel.onPeriodSelected(period) },
                    label = { Text(period.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandPrimary,
                        selectedLabelColor = CockpitBackground,
                        containerColor = CockpitSurface,
                        labelColor = TextPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Financial Overview Card
        val report = state.report
        if (report != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CockpitSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CockpitBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = report.periodLabel.uppercase(),
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "MARGEM: %.0f%%".format(report.profitMarginPercent),
                            color = if (report.profitMarginPercent >= 60) ClassExcellent else ClassAvoid,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Faturamento Bruto", color = TextSecondary, fontSize = 12.sp)
                            Text(
                                text = "R$ %.2f".format(Locale("pt", "BR"), report.grossRevenue),
                                color = TextPrimary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Lucro Líquido Real", color = TextSecondary, fontSize = 12.sp)
                            Text(
                                text = "R$ %.2f".format(Locale("pt", "BR"), report.netProfit),
                                color = if (report.netProfit >= 0) ClassExcellent else ClassAvoid,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Costs breakdown row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CockpitSurfaceVariant, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Combustível", color = TextSecondary, fontSize = 10.sp)
                            Text(
                                text = "R$ %.2f".format(Locale("pt", "BR"), report.fuelCosts),
                                color = ClassBad,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Column {
                            Text(text = "Manutenção", color = TextSecondary, fontSize = 10.sp)
                            Text(
                                text = "R$ %.2f".format(Locale("pt", "BR"), report.maintenanceCosts),
                                color = ClassBad,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Column {
                            Text(text = "Custos Fixos", color = TextSecondary, fontSize = 10.sp)
                            Text(
                                text = "R$ %.2f".format(Locale("pt", "BR"), report.fixedCosts),
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Key Operational Rates
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "R$/KM: R$ %.2f (Líq: R$ %.2f)".format(
                                Locale("pt", "BR"),
                                report.grossRatePerKm,
                                report.netProfitPerKm
                            ),
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "R$/Hora: R$ %.2f (Líq: R$ %.2f)".format(
                                Locale("pt", "BR"),
                                report.grossRatePerHour,
                                report.netProfitPerHour
                            ),
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Tabs: [Visão Geral, Abastecimentos, Manutenção]
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CockpitSurface,
            contentColor = BrandPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = BrandPrimary
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Visão Geral", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Combustível", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Manutenção", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedTab) {
            0 -> {
                // Tab 0: Visão Geral / Métricas
                Text(
                    text = "RESUMO DA EFICIÊNCIA OPERACIONAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CockpitMetricCard(
                        title = "Consumo Médio",
                        value = state.averageFuelConsumption?.let { "%.1f km/L".format(it) } ?: "%.1f km/L (est)".format(state.vehicle.consumptionKmPerLiter),
                        subtitle = "Apurado na bomba",
                        accentColor = BrandPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    CockpitMetricCard(
                        title = "Custo Total do Carro",
                        value = "R$ %.2f/km".format(Locale("pt", "BR"), state.vehicle.totalCostPerKm),
                        subtitle = "Real consolidado",
                        accentColor = ClassBad,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (state.maintenanceAlerts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "ALERTAS DE MANUTENÇÃO PREVENTIVA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    state.maintenanceAlerts.forEach { alert ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = CockpitSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(alert.urgency.colorHex).copy(alpha = 0.5f))
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(alert.urgency.colorHex),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = alert.type.displayName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                    Text(text = alert.message, color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Tab 1: Combustível
                Text(
                    text = "REGISTRAR ABASTECIMENTO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandPrimary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = state.fuelOdometerInput,
                        onValueChange = { viewModel.onFuelOdometerChanged(it) },
                        label = { Text("Hodômetro (km)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = outlinedColors()
                    )
                    OutlinedTextField(
                        value = state.fuelLitersInput,
                        onValueChange = { viewModel.onFuelLitersChanged(it) },
                        label = { Text("Litros") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = outlinedColors()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = state.fuelPriceInput,
                        onValueChange = { viewModel.onFuelPriceChanged(it) },
                        label = { Text("Preço R$/Litro") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = outlinedColors()
                    )
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .background(CockpitSurface, RoundedCornerShape(8.dp))
                            .border(1.dp, CockpitBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Tanque Cheio?", fontSize = 12.sp, color = TextPrimary)
                        Switch(
                            checked = state.fuelIsFullTank,
                            onCheckedChange = { viewModel.onFuelFullTankChanged(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = BrandPrimary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { viewModel.addFuelRecord() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.LocalGasStation, contentDescription = null, tint = CockpitBackground)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SALVAR ABASTECIMENTO", fontWeight = FontWeight.Bold, color = CockpitBackground, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "HISTÓRICO DE ABASTECIMENTOS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                state.fuelRecords.forEach { fuel ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = CockpitSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CockpitBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "R$ %.2f (%.1f L • R$ %.2f/L)".format(Locale("pt", "BR"), fuel.totalPaid, fuel.liters, fuel.pricePerLiter),
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Km: %.0f • %s".format(fuel.odometerKm, fuel.date) +
                                            (fuel.calculatedKmPerLiter?.let { " • %.1f km/L real".format(it) } ?: ""),
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            IconButton(onClick = { viewModel.deleteFuelRecord(fuel.id) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = ClassAvoid.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }

            2 -> {
                // Tab 2: Manutenção
                Text(
                    text = "REGISTRAR SERVIÇO DE MANUTENÇÃO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandPrimary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Maintenance Type Chips
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(MaintenanceType.OIL_CHANGE, MaintenanceType.BRAKES, MaintenanceType.TIRES, MaintenanceType.REVISION).forEach { type ->
                        FilterChip(
                            selected = state.maintType == type,
                            onClick = { viewModel.onMaintTypeChanged(type) },
                            label = { Text(type.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandPrimary,
                                selectedLabelColor = CockpitBackground,
                                containerColor = CockpitSurface,
                                labelColor = TextPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.maintDescInput,
                    onValueChange = { viewModel.onMaintDescChanged(it) },
                    label = { Text("Descrição do Serviço") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = outlinedColors()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = state.maintCostInput,
                        onValueChange = { viewModel.onMaintCostChanged(it) },
                        label = { Text("Custo Total (R$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = outlinedColors()
                    )
                    OutlinedTextField(
                        value = state.maintOdometerInput,
                        onValueChange = { viewModel.onMaintOdometerChanged(it) },
                        label = { Text("Km no Serviço") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = outlinedColors()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { viewModel.addMaintenanceRecord() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Build, contentDescription = null, tint = CockpitBackground)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SALVAR MANUTENÇÃO", fontWeight = FontWeight.Bold, color = CockpitBackground, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "HISTÓRICO DE MANUTENÇÕES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                state.maintenanceRecords.forEach { m ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = CockpitSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CockpitBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${m.type.displayName} - R$ %.2f".format(Locale("pt", "BR"), m.cost),
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "${m.description} • Km: %.0f • %s".format(m.odometerKm, m.date),
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            IconButton(onClick = { viewModel.deleteMaintenanceRecord(m.id) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = ClassAvoid.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }
        }
    }
}
