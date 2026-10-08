package com.rotai.iq.feature.vehicle

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rotai.iq.core.ui.designsystem.RotaButton
import com.rotai.iq.core.ui.designsystem.RotaButtonVariant
import com.rotai.iq.core.ui.designsystem.RotaCard
import com.rotai.iq.core.ui.designsystem.RotaDialog
import com.rotai.iq.core.ui.designsystem.RotaMetric
import com.rotai.iq.core.ui.theme.CardShapeDefault
import com.rotai.iq.core.ui.theme.CardShapeElevated
import com.rotai.iq.core.ui.theme.RotaBlack
import com.rotai.iq.core.ui.theme.RotaBorderSubtle
import com.rotai.iq.core.ui.theme.RotaCardBackground
import com.rotai.iq.core.ui.theme.RotaCardElevated
import com.rotai.iq.core.ui.theme.RotaDarkCanvas
import com.rotai.iq.core.ui.theme.RotaExcellent
import com.rotai.iq.core.ui.theme.RotaOrangeLight
import com.rotai.iq.core.ui.theme.RotaOrangePrimary
import com.rotai.iq.core.ui.theme.RotaOrangeSubtleBg
import com.rotai.iq.core.ui.theme.RotaTextPrimary
import com.rotai.iq.core.ui.theme.RotaTextSecondary
import com.rotai.iq.core.ui.theme.RotaTextTertiary
import com.rotai.iq.core.ui.theme.RotaTextWhite
import java.util.Locale

@Composable
fun VehicleScreen(
    viewModel: VehicleViewModel,
    modifier: Modifier = Modifier,
    onNavigateToGoals: () -> Unit = {},
    onNavigateToRideFilter: () -> Unit = {},
    onNavigateToPreferences: () -> Unit = {},
    onNavigateToZones: () -> Unit = {},
    onNavigateToAutomation: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }

    var soundAlertsEnabled by remember { mutableStateOf(true) }
    var goalAlertsEnabled by remember { mutableStateOf(true) }
    var deadheadWarningEnabled by remember { mutableStateOf(true) }
    var maintenanceReminderEnabled by remember { mutableStateOf(true) }

    val vehicle = state.vehicle
    val vehicleName = vehicle.name.ifBlank { "Veículo não configurado" }
    val consumption = vehicle.consumptionKmPerLiter
    val costPerKm = state.costProjections.costPerKm

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RotaBlack)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Cabeçalho da Tela: Perfil do Motorista
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar com anel laranja vibrante (clicável para gerenciar assinatura)
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(RotaCardElevated)
                    .border(2.dp, RotaOrangePrimary, CircleShape)
                    .clickable { onNavigateToSubscription() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "IQ",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = RotaOrangePrimary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Motorista ROTA IQ",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = RotaTextWhite
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(RotaOrangeSubtleBg)
                        .border(1.dp, RotaOrangePrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable { onNavigateToSubscription() }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = RotaOrangePrimary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "CONTA PRO ATIVA",
                        color = RotaOrangePrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // CARD VISUAL DO VEÍCULO (Dados Reais do Room)
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
                    Column {
                        Text(
                            text = "MEU VEÍCULO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RotaTextSecondary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = vehicleName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = RotaTextWhite
                        )
                        Text(
                            text = "${vehicle.model} • ${vehicle.fuelType.displayName}",
                            fontSize = 12.sp,
                            color = RotaTextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(RotaDarkCanvas)
                            .border(1.dp, RotaBorderSubtle, CircleShape)
                            .clickable { showEditDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar Veículo",
                            tint = RotaOrangePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Métricas Principais: Consumo e Custo por KM Reais
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VehicleMiniStat(
                        label = "CONSUMO MÉDIO",
                        value = "%.1f km/l".format(Locale("pt", "BR"), consumption),
                        highlight = false,
                        modifier = Modifier.weight(1f)
                    )
                    VehicleMiniStat(
                        label = "CUSTO TOTAL / KM",
                        value = "R$ %.2f/km".format(Locale("pt", "BR"), costPerKm),
                        highlight = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(color = RotaBorderSubtle, thickness = 1.dp)

                Spacer(modifier = Modifier.height(14.dp))

                // 4 Indicadores de Custo do Veículo Calculados em Tempo Real
                Text(
                    text = "COMPONENTES DE CUSTO REAL POR KM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = RotaTextSecondary,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CostPill(
                        name = "Combustível",
                        cost = "R$ %.2f".format(Locale("pt", "BR"), vehicle.fuelCostPerKm),
                        modifier = Modifier.weight(1f)
                    )
                    CostPill(
                        name = "Manutenção",
                        cost = "R$ %.2f".format(Locale("pt", "BR"), vehicle.maintenanceCostPerKm),
                        modifier = Modifier.weight(1f)
                    )
                    CostPill(
                        name = "Custos Fixos",
                        cost = "R$ %.2f".format(Locale("pt", "BR"), vehicle.fixedCostPerKm),
                        modifier = Modifier.weight(1f)
                    )
                    CostPill(
                        name = "Total / km",
                        cost = "R$ %.2f".format(Locale("pt", "BR"), vehicle.totalCostPerKm),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Seção de Menus de Configurações e Preferências
        Text(
            text = "PREFERÊNCIAS & CONFIGURAÇÕES",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = RotaTextSecondary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ProfileMenuRow(
                icon = Icons.Default.DirectionsCar,
                title = "Editar Dados do Veículo",
                subtitle = "Consumo, combustível e custos fixos",
                onClick = { showEditDialog = true }
            )
            ProfileMenuRow(
                icon = Icons.Default.Flag,
                title = "Minhas Metas",
                subtitle = "Meta diária de faturamento e piso de R$/km",
                onClick = onNavigateToGoals
            )
            ProfileMenuRow(
                icon = Icons.Default.Tune,
                title = "Filtros de Aceite de Corrida",
                subtitle = "Piso R$/km, piso R$/hora, raio de embarque e paradas",
                onClick = onNavigateToRideFilter
            )
            ProfileMenuRow(
                icon = Icons.Default.DirectionsCar,
                title = "Preferências & Ferramentas",
                subtitle = "Simulador inDrive, MEI e livro caixa",
                onClick = onNavigateToPreferences
            )
            ProfileMenuRow(
                icon = Icons.Default.Notifications,
                title = "Notificações & Alertas",
                subtitle = "Alertas sonoros e avisos de meta",
                onClick = { showNotificationsDialog = true }
            )
            ProfileMenuRow(
                icon = Icons.Default.RecordVoiceOver,
                title = "Voz & Copiloto TTS",
                subtitle = "Leitura audível no fone de ouvido",
                onClick = onNavigateToAutomation
            )
            ProfileMenuRow(
                icon = Icons.Default.Hearing,
                title = "Acessibilidade & HUD",
                subtitle = "Permissão de sobreposição e leitura do Uber/99",
                onClick = onNavigateToAutomation
            )
            ProfileMenuRow(
                icon = Icons.Default.Security,
                title = "Privacidade & LGPD",
                subtitle = "Exportação de dados e direitos do titular",
                onClick = onNavigateToPrivacy
            )
            ProfileMenuRow(
                icon = Icons.Default.Star,
                title = "Plano & Assinatura Pro",
                subtitle = "Gerenciar plano e desbloqueio ilimitado",
                onClick = onNavigateToSubscription
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Modal de Edição de Veículo
    if (showEditDialog) {
        RotaDialog(
            title = "Configurar Veículo",
            onDismissRequest = { showEditDialog = false },
            confirmButtonText = "Salvar",
            onConfirm = {
                viewModel.saveVehicle()
                showEditDialog = false
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.nameInput,
                    onValueChange = { viewModel.onNameChanged(it) },
                    label = { Text("Nome do Veículo") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RotaOrangePrimary,
                        unfocusedBorderColor = RotaBorderSubtle,
                        focusedTextColor = RotaTextWhite,
                        unfocusedTextColor = RotaTextWhite
                    )
                )

                OutlinedTextField(
                    value = state.consumptionInput,
                    onValueChange = { viewModel.onConsumptionChanged(it) },
                    label = { Text("Consumo Médio (km/L)") },
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
                    label = { Text("Preço do Combustível (R$/L)") },
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
                    value = state.maintenancePerKmInput,
                    onValueChange = { viewModel.onMaintenanceChanged(it) },
                    label = { Text("Custo Manutenção/km (R$)") },
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

    // Modal Interativo de Preferências de Notificações
    if (showNotificationsDialog) {
        RotaDialog(
            title = "Configurar Notificações",
            onDismissRequest = { showNotificationsDialog = false },
            confirmButtonText = "Salvar Preferências",
            onConfirm = { showNotificationsDialog = false }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                NotificationToggleRow(
                    title = "Alertas Sonoros de Demanda",
                    subtitle = "Bipe audível ao detectar corrida com score alto",
                    checked = soundAlertsEnabled,
                    onCheckedChange = { soundAlertsEnabled = it }
                )
                NotificationToggleRow(
                    title = "Notificação de Meta Atingida",
                    subtitle = "Aviso comemorativo ao atingir 100% da meta diária",
                    checked = goalAlertsEnabled,
                    onCheckedChange = { goalAlertsEnabled = it }
                )
                NotificationToggleRow(
                    title = "Alerta de Saída de Rota & Deadhead",
                    subtitle = "Aviso preditivo ao se afastar de zonas com demanda",
                    checked = deadheadWarningEnabled,
                    onCheckedChange = { deadheadWarningEnabled = it }
                )
                NotificationToggleRow(
                    title = "Lembrete de Manutenção Preventiva",
                    subtitle = "Avisos baseados no odômetro e histórico de consumo",
                    checked = maintenanceReminderEnabled,
                    onCheckedChange = { maintenanceReminderEnabled = it }
                )
            }
        }
    }
}

@Composable
private fun VehicleMiniStat(
    label: String,
    value: String,
    highlight: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (highlight) RotaOrangeSubtleBg else RotaDarkCanvas)
            .border(1.dp, if (highlight) RotaOrangePrimary.copy(alpha = 0.35f) else RotaBorderSubtle, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (highlight) RotaOrangePrimary else RotaTextTertiary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = RotaTextWhite
            )
        }
    }
}

@Composable
private fun CostPill(
    name: String,
    cost: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(RotaDarkCanvas)
            .border(1.dp, RotaBorderSubtle, RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = cost,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = RotaOrangeLight
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = name,
                fontSize = 9.sp,
                color = RotaTextTertiary
            )
        }
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    RotaCard(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShapeElevated,
        backgroundColor = RotaCardBackground,
        borderColor = RotaBorderSubtle,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(RotaDarkCanvas)
                        .border(1.dp, RotaBorderSubtle, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = RotaOrangePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RotaTextWhite
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = RotaTextSecondary
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = RotaTextTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun NotificationToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp)
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = RotaTextWhite
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = RotaTextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = RotaOrangePrimary,
                uncheckedThumbColor = RotaTextTertiary,
                uncheckedTrackColor = RotaDarkCanvas
            )
        )
    }
}
