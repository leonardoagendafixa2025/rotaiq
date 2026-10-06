package com.rotai.iq.feature.rides

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rotai.iq.core.domain.model.RidePlatform
import com.rotai.iq.core.ui.components.HudEvaluationCard
import com.rotai.iq.core.ui.theme.BrandPrimary
import com.rotai.iq.core.ui.theme.ClassExcellent
import com.rotai.iq.core.ui.theme.CockpitBackground
import com.rotai.iq.core.ui.theme.CockpitBorder
import com.rotai.iq.core.ui.theme.CockpitSurface
import com.rotai.iq.core.ui.theme.TextPrimary
import com.rotai.iq.core.ui.theme.TextSecondary

@Composable
fun RideSimulatorScreen(
    viewModel: RideSimulatorViewModel,
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
            text = "SIMULADOR DE OFERTAS",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = BrandPrimary,
            letterSpacing = 0.5.sp
        )
        Text(
            text = "Avaliação multifatorial de corridas com custos reais",
            fontSize = 13.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Platform selection chips
        Text(
            text = "PLATAFORMA",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(RidePlatform.UBER, RidePlatform.NINETY_NINE, RidePlatform.INDRAVE).forEach { platform ->
                FilterChip(
                    selected = state.selectedPlatform == platform,
                    onClick = { viewModel.onPlatformChanged(platform) },
                    label = { Text(platform.displayName) },
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

        // Preset Quick Chips
        Text(
            text = "CENÁRIOS PRONTOS PARA TESTAR",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SuggestionChip(
                onClick = {
                    viewModel.loadPreset(
                        platform = RidePlatform.UBER,
                        grossFare = 32.80,
                        distanceKm = 9.4,
                        durationMinutes = 26.0,
                        pickupDistKm = 1.0,
                        pickupDurMin = 3.0,
                        stops = 0
                    )
                },
                label = { Text("🟢 Excelente R$ 32,80 (9,4 km)") }
            )
            SuggestionChip(
                onClick = {
                    viewModel.loadPreset(
                        platform = RidePlatform.UBER,
                        grossFare = 11.50,
                        distanceKm = 3.0,
                        durationMinutes = 14.0,
                        pickupDistKm = 4.5,
                        pickupDurMin = 12.0,
                        stops = 0
                    )
                },
                label = { Text("🔴 Embarque Longe (Ruim)") }
            )
            SuggestionChip(
                onClick = {
                    viewModel.loadPreset(
                        platform = RidePlatform.NINETY_NINE,
                        grossFare = 45.00,
                        distanceKm = 24.0,
                        durationMinutes = 65.0,
                        pickupDistKm = 2.0,
                        pickupDurMin = 6.0,
                        stops = 3
                    )
                },
                label = { Text("🟡 3 Paradas R$ 45,00") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input Form
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = state.grossFareInput,
                onValueChange = { viewModel.onGrossFareChanged(it) },
                label = { Text("Valor Ofertado (R$)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                colors = outlinedColors()
            )
            OutlinedTextField(
                value = state.distanceKmInput,
                onValueChange = { viewModel.onDistanceKmChanged(it) },
                label = { Text("Distância (km)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                colors = outlinedColors()
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = state.durationMinutesInput,
                onValueChange = { viewModel.onDurationMinutesChanged(it) },
                label = { Text("Duração (min)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                colors = outlinedColors()
            )
            OutlinedTextField(
                value = state.pickupDistanceKmInput,
                onValueChange = { viewModel.onPickupDistanceChanged(it) },
                label = { Text("Até Passageiro (km)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                colors = outlinedColors()
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = state.pickupDurationMinutesInput,
                onValueChange = { viewModel.onPickupDurationChanged(it) },
                label = { Text("Tempo Embarque (min)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                colors = outlinedColors()
            )
            OutlinedTextField(
                value = state.stopsCountInput,
                onValueChange = { viewModel.onStopsCountChanged(it) },
                label = { Text("Qtd. Paradas") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = outlinedColors()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.evaluateCurrentInput() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = CockpitBackground)
            Spacer(modifier = Modifier.width(6.dp))
            Text("CALCULAR DECISÃO", fontWeight = FontWeight.Bold, color = CockpitBackground, fontSize = 14.sp)
        }

        // HUD Result Card
        if (state.currentEvaluation != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "RESULTADO DO MOTOR DE DECISÃO",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            HudEvaluationCard(evaluation = state.currentEvaluation!!)

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { viewModel.saveEvaluationToHistory() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isSaved) ClassExcellent else CockpitSurface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (state.isSaved) Icons.Default.BookmarkAdded else Icons.Default.Bookmark,
                    contentDescription = null,
                    tint = if (state.isSaved) CockpitBackground else TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state.isSaved) "SALVO NO HISTÓRICO COM SUCESSO" else "SALVAR NO HISTÓRICO",
                    fontWeight = FontWeight.Bold,
                    color = if (state.isSaved) CockpitBackground else TextPrimary,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun outlinedColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = BrandPrimary,
    unfocusedBorderColor = CockpitBorder,
    focusedLabelColor = BrandPrimary,
    unfocusedLabelColor = TextSecondary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedContainerColor = CockpitSurface,
    unfocusedContainerColor = CockpitSurface
)
