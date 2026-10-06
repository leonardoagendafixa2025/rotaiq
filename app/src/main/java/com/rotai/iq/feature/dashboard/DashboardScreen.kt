package com.rotai.iq.feature.dashboard

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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rotai.iq.core.ui.components.CockpitMetricCard
import com.rotai.iq.core.ui.theme.BrandPrimary
import com.rotai.iq.core.ui.theme.ClassAvoid
import com.rotai.iq.core.ui.theme.ClassExcellent
import com.rotai.iq.core.ui.theme.CockpitBackground
import com.rotai.iq.core.ui.theme.CockpitBorder
import com.rotai.iq.core.ui.theme.CockpitSurface
import com.rotai.iq.core.ui.theme.CockpitSurfaceVariant
import com.rotai.iq.core.ui.theme.TextPrimary
import com.rotai.iq.core.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToSimulator: () -> Unit,
    onNavigateToVehicle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CockpitBackground)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Cockpit Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ROTA IQ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = BrandPrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "COPILOTO INTELIGENTE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.5.sp
                )
            }
            // Active Vehicle Cost Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(CockpitSurfaceVariant)
                    .border(1.dp, CockpitBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = BrandPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "R$ %.2f/km".format(Locale("pt", "BR"), state.vehicle.totalCostPerKm),
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Decisive Status Card: "Estou ganhando dinheiro ou perdendo tempo?"
        val statusBg = if (state.isOperatingWell) ClassExcellent.copy(alpha = 0.12f) else ClassAvoid.copy(alpha = 0.12f)
        val statusBorder = if (state.isOperatingWell) ClassExcellent.copy(alpha = 0.5f) else ClassAvoid.copy(alpha = 0.5f)
        val statusText = if (state.isOperatingWell) "OPERANDO COM LUCRO" else "ATENÇÃO AO RITMO"
        val statusIconColor = if (state.isOperatingWell) ClassExcellent else ClassAvoid

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CockpitSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, statusBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STATUS DA OPERAÇÃO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusIconColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = statusText,
                            color = statusIconColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = state.coachingAdvice.remainingMessage,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${state.coachingAdvice.paceProjectionMessage} • ${state.coachingAdvice.requiredRateMessage}",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = (state.coachingAdvice.progressPercentage / 100.0).toFloat().coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = BrandPrimary,
                    trackColor = CockpitSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Simulate CTA
        Button(
            onClick = onNavigateToSimulator,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Calculate,
                contentDescription = null,
                tint = CockpitBackground,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SIMULAR E AVALIAR CORRIDA",
                color = CockpitBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Financial Metrics Grid
        Text(
            text = "DESEMPENHO DO DIA",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CockpitMetricCard(
                title = "Faturamento",
                value = "R$ %.2f".format(Locale("pt", "BR"), state.goal.currentDailyGross),
                subtitle = "Meta: R$ %.2f".format(Locale("pt", "BR"), state.goal.dailyGrossTarget),
                icon = Icons.Default.AttachMoney,
                accentColor = BrandPrimary,
                modifier = Modifier.weight(1f)
            )
            CockpitMetricCard(
                title = "Lucro Líquido",
                value = "R$ %.2f".format(Locale("pt", "BR"), state.goal.currentDailyNet),
                subtitle = "Após custos do carro",
                icon = Icons.Default.TrendingUp,
                accentColor = ClassExcellent,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CockpitMetricCard(
                title = "Horas Online",
                value = "%.1fh".format(state.goal.hoursWorkedToday),
                subtitle = "Turno: %.0fh".format(state.goal.shiftTargetHours),
                icon = Icons.Default.Schedule,
                accentColor = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            CockpitMetricCard(
                title = "Km Rodados",
                value = "%.1f km".format(state.goal.kmDrivenToday),
                subtitle = "Hoje",
                icon = Icons.Default.Route,
                accentColor = TextPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CockpitMetricCard(
                title = "R$/KM",
                value = "R$ %.2f".format(Locale("pt", "BR"), state.goal.targetKmRate),
                subtitle = "Meta mínima",
                accentColor = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            CockpitMetricCard(
                title = "R$/HORA",
                value = "R$ %.2f".format(Locale("pt", "BR"), state.goal.targetHourlyRate),
                subtitle = "Meta mínima",
                accentColor = TextPrimary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
