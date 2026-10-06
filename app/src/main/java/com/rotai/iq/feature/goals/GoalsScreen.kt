package com.rotai.iq.feature.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rotai.iq.core.ui.theme.BrandPrimary
import com.rotai.iq.core.ui.theme.ClassExcellent
import com.rotai.iq.core.ui.theme.CockpitBackground
import com.rotai.iq.core.ui.theme.CockpitBorder
import com.rotai.iq.core.ui.theme.CockpitSurface
import com.rotai.iq.core.ui.theme.TextPrimary
import com.rotai.iq.core.ui.theme.TextSecondary
import com.rotai.iq.feature.rides.outlinedColors

@Composable
fun GoalsScreen(
    viewModel: GoalsViewModel,
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
        Text(
            text = "METAS E PRODUTIVIDADE",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = BrandPrimary,
            letterSpacing = 0.5.sp
        )
        Text(
            text = "Defina seus objetivos e acompanhe o ritmo em tempo real",
            fontSize = 13.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Live Coaching Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CockpitSurface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BrandPrimary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ORIENTAÇÃO DO COPILOTO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandPrimary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.coachingAdvice.remainingMessage,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = state.coachingAdvice.paceProjectionMessage,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = state.coachingAdvice.requiredRateMessage,
                    fontSize = 13.sp,
                    color = ClassExcellent,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "PARÂMETROS DE META DIÁRIA",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = state.dailyGrossInput,
                onValueChange = { viewModel.onDailyGrossChanged(it) },
                label = { Text("Meta Bruta (R$)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                colors = outlinedColors()
            )
            OutlinedTextField(
                value = state.dailyNetInput,
                onValueChange = { viewModel.onDailyNetChanged(it) },
                label = { Text("Meta Líquida (R$)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                colors = outlinedColors()
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = state.targetHourlyInput,
                onValueChange = { viewModel.onTargetHourlyChanged(it) },
                label = { Text("Meta Mínima R$/h") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                colors = outlinedColors()
            )
            OutlinedTextField(
                value = state.targetKmInput,
                onValueChange = { viewModel.onTargetKmChanged(it) },
                label = { Text("Meta Mínima R$/km") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                colors = outlinedColors()
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = state.shiftHoursInput,
            onValueChange = { viewModel.onShiftHoursChanged(it) },
            label = { Text("Horas Planejadas do Turno") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            colors = outlinedColors()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "PROGRESSO ACUMULADO HOJE",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = state.currentGrossInput,
                onValueChange = { viewModel.onCurrentGrossChanged(it) },
                label = { Text("Faturamento Atual (R$)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                colors = outlinedColors()
            )
            OutlinedTextField(
                value = state.hoursWorkedInput,
                onValueChange = { viewModel.onHoursWorkedChanged(it) },
                label = { Text("Horas Trabalhadas") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                colors = outlinedColors()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { viewModel.saveGoal() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (state.isSavedSuccess) ClassExcellent else BrandPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = null,
                tint = CockpitBackground
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (state.isSavedSuccess) "METAS SALVAS COM SUCESSO" else "SALVAR METAS",
                fontWeight = FontWeight.Bold,
                color = CockpitBackground,
                fontSize = 13.sp
            )
        }
    }
}
