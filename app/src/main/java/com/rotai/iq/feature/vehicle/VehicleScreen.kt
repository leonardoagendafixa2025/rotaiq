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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rotai.iq.core.ui.designsystem.RotaCard
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

@Composable
fun VehicleScreen(
    viewModel: VehicleViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val vehicleName = if (state.vehicle.name.isNotBlank() && state.vehicle.name != "Meu Carro") state.vehicle.name else "Toyota Corolla"
    val vehicleYear = "2022"
    val consumption = if (state.vehicle.consumptionKmPerLiter > 0) state.vehicle.consumptionKmPerLiter else 11.8
    val costPerKm = if (state.costProjections.costPerKm > 0) state.costProjections.costPerKm else 0.71

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
            // Avatar com anel laranja vibrante
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(RotaCardElevated)
                    .border(2.dp, RotaOrangePrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "LD",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = RotaOrangePrimary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Leonardo",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = RotaTextWhite
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Badge de Plano
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(RotaOrangeSubtleBg)
                        .border(1.dp, RotaOrangePrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
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
                        text = "PLANO PRO ATIVO",
                        color = RotaOrangePrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // CARD VISUAL DO VEÍCULO (Especificação Oficial)
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
                            text = "Ano $vehicleYear • Gasolina Aditivada",
                            fontSize = 12.sp,
                            color = RotaTextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(RotaDarkCanvas)
                            .border(1.dp, RotaBorderSubtle, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = RotaOrangePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Métricas Principais: Consumo e Custo por KM
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VehicleMiniStat(
                        label = "CONSUMO MÉDIO",
                        value = "%.1f km/l".format(consumption),
                        highlight = false,
                        modifier = Modifier.weight(1f)
                    )
                    VehicleMiniStat(
                        label = "CUSTO ESTIMADO",
                        value = "R$ %.2f/km".format(costPerKm),
                        highlight = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(color = RotaBorderSubtle, thickness = 1.dp)

                Spacer(modifier = Modifier.height(14.dp))

                // 4 Indicadores de Custo do Veículo
                Text(
                    text = "COMPONENTES DE CUSTO REAL",
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
                    CostPill(name = "Combustível", cost = "R$ 0,47", modifier = Modifier.weight(1f))
                    CostPill(name = "Manutenção", cost = "R$ 0,12", modifier = Modifier.weight(1f))
                    CostPill(name = "Seguro", cost = "R$ 0,08", modifier = Modifier.weight(1f))
                    CostPill(name = "Depreciação", cost = "R$ 0,04", modifier = Modifier.weight(1f))
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
            ProfileMenuRow(icon = Icons.Default.Flag, title = "Minhas Metas", subtitle = "Meta diária e piso/km")
            ProfileMenuRow(icon = Icons.Default.Tune, title = "Preferências de Corrida", subtitle = "Filtro de passageiro e raio")
            ProfileMenuRow(icon = Icons.Default.Map, title = "Áreas & Zonas Favoritas", subtitle = "Alertas de saída de rota")
            ProfileMenuRow(icon = Icons.Default.Notifications, title = "Notificações", subtitle = "Alertas de alta demanda")
            ProfileMenuRow(icon = Icons.Default.RecordVoiceOver, title = "Voz & Copiloto TTS", subtitle = "Leitura audível no fone")
            ProfileMenuRow(icon = Icons.Default.Hearing, title = "Acessibilidade", subtitle = "Overlay e leitura de tela")
            ProfileMenuRow(icon = Icons.Default.Security, title = "Privacidade & LGPD", subtitle = "Armazenamento local seguro")
        }

        Spacer(modifier = Modifier.height(24.dp))
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
