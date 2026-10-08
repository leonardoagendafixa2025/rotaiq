package com.rotai.iq.feature.filter

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun RideFilterScreen(
    viewModel: RideFilterViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null
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
        if (onNavigateBack != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = BrandPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "VOLTAR",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            }
        }

        // TÍTULO E CABEÇALHO PRINCIPAL
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(BrandPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = BrandPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "MEUS FILTROS DE CORRIDA",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandPrimary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Regras inegociáveis para aprovar ou recusar ofertas",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // FEEDBACK DE SUCESSO
        if (state.isSavedSuccess) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF00381B)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ClassExcellent)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = ClassExcellent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Filtros salvos! O HUD e o Copiloto de Voz já estão usando seus novos limites.",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // CARD 1: PISO DE R$/KM
        FilterConfigCard(
            title = "PISO DE VALOR POR KM (R$/KM)",
            subtitle = "Reprova na hora corridas que pagam menos que este valor por km",
            inputValue = state.minRatePerKmInput,
            onValueChange = { viewModel.onMinRatePerKmChanged(it) },
            unit = "R$/km",
            presets = listOf("2.00", "2.50", "3.00", "3.50"),
            onPresetClick = { viewModel.onMinRatePerKmChanged(it) }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // CARD 2: PISO DE R$/HORA
        FilterConfigCard(
            title = "PISO DE FATURAMENTO POR HORA (R$/H)",
            subtitle = "Protege você de congestionamentos e corridas lentas",
            inputValue = state.minRatePerHourInput,
            onValueChange = { viewModel.onMinRatePerHourChanged(it) },
            unit = "R$/h",
            presets = listOf("35.0", "45.0", "55.0", "65.0"),
            onPresetClick = { viewModel.onMinRatePerHourChanged(it) }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // CARD 3: VALOR MÍNIMO DA CORRIDA
        FilterConfigCard(
            title = "VALOR TOTAL MÍNIMO (R$)",
            subtitle = "Elimina viagens curtas com valor muito baixo que não compensam a espera",
            inputValue = state.minGrossFareInput,
            onValueChange = { viewModel.onMinGrossFareChanged(it) },
            unit = "R$",
            presets = listOf("8.0", "10.0", "12.0", "15.0"),
            onPresetClick = { viewModel.onMinGrossFareChanged(it) }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // CARD 4: DESLOCAMENTO ATÉ O PASSAGEIRO (DEADHEAD)
        FilterConfigCard(
            title = "DISTÂNCIA MÁXIMA ATÉ PASSAGEIRO (KM)",
            subtitle = "Evita rodar quilômetros de graça para buscar o passageiro longe",
            inputValue = state.maxPickupDistanceKmInput,
            onValueChange = { viewModel.onMaxPickupDistanceKmChanged(it) },
            unit = "km",
            presets = listOf("2.0", "3.0", "4.0", "5.0"),
            onPresetClick = { viewModel.onMaxPickupDistanceKmChanged(it) }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // CARD 5: TEMPO ATÉ O EMBARQUE
        FilterConfigCard(
            title = "TEMPO MÁXIMO ATÉ EMBARQUE (MINUTOS)",
            subtitle = "Tempo máximo aceitável para chegar ao passageiro",
            inputValue = state.maxPickupMinutesInput,
            onValueChange = { viewModel.onMaxPickupMinutesChanged(it) },
            unit = "min",
            presets = listOf("5.0", "8.0", "10.0", "15.0"),
            onPresetClick = { viewModel.onMaxPickupMinutesChanged(it) }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // CARD 6: REGRAS ESPECIAIS & SWITCHES
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CockpitSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CockpitBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "REGRAS ADICIONAIS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandPrimary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                FilterSwitchRow(
                    title = "Rejeitar Corridas com Múltiplas Paradas",
                    subtitle = "Reprova se a viagem tiver paradas adicionais não remuneradas",
                    checked = !state.allowIntermediateStops,
                    onCheckedChange = { viewModel.onAllowIntermediateStopsChanged(!it) }
                )

                HorizontalDivider(color = CockpitBorder, modifier = Modifier.padding(vertical = 10.dp))

                FilterSwitchRow(
                    title = "Avisar no Copiloto de Voz (TTS)",
                    subtitle = "Anuncia no fone bluetooth se a corrida bateu seu filtro",
                    checked = state.audioAlertsEnabled,
                    onCheckedChange = { viewModel.onAudioAlertsChanged(it) }
                )

                HorizontalDivider(color = CockpitBorder, modifier = Modifier.padding(vertical = 10.dp))

                FilterSwitchRow(
                    title = "Badge de Filtro no HUD Flutuante",
                    subtitle = "Exibe ✓ BATEU FILTRO em verde ou ✕ REPROVADA em vermelho",
                    checked = state.overlayHudEnabled,
                    onCheckedChange = { viewModel.onOverlayHudChanged(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CARD 7: SIMULAÇÃO EM TEMPO REAL DO FILTRO
        val minKm = state.minRatePerKmInput.replace(",", ".").toDoubleOrNull() ?: 2.50
        val maxPickup = state.maxPickupDistanceKmInput.replace(",", ".").toDoubleOrNull() ?: 3.0

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CockpitSurfaceVariant),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CockpitBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "COMO O FILTRO DECIDIRÁ POR VOCÊ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Exemplo Positivo
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = ClassExcellent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "R$ 30,00 por 9 km (R$ 3,33/km, 1,2 km busca) -> ",
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "APROVADA",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ClassExcellent
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Exemplo Negativo
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HighlightOff,
                        contentDescription = null,
                        tint = ClassAvoid,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "R$ 18,00 por 14 km (R$ 1,28/km) -> ",
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "REPROVADA (abaixo de R$ %.2f)".format(Locale.US, minKm),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ClassAvoid
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // BOTÃO SALVAR FILTROS
        Button(
            onClick = { viewModel.saveFilters() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = CockpitBackground)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SALVAR MEUS FILTROS DE CORRIDA",
                fontWeight = FontWeight.ExtraBold,
                color = CockpitBackground,
                fontSize = 14.sp,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun FilterConfigCard(
    title: String,
    subtitle: String,
    inputValue: String,
    onValueChange: (String) -> Unit,
    unit: String,
    presets: List<String>,
    onPresetClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CockpitSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CockpitBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = BrandPrimary,
                letterSpacing = 1.sp
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = inputValue,
                onValueChange = onValueChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                trailingIcon = {
                    Text(
                        text = unit,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPrimary,
                    unfocusedBorderColor = CockpitBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = BrandPrimary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Atalhos rápidos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { preset ->
                    val isSelected = inputValue.trim() == preset.trim()
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) BrandPrimary else CockpitSurfaceVariant)
                            .border(
                                1.dp,
                                if (isSelected) BrandPrimary else CockpitBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onPresetClick(preset) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = preset,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) CockpitBackground else TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSwitchRow(
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
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CockpitBackground,
                checkedTrackColor = BrandPrimary,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = CockpitSurfaceVariant
            )
        )
    }
}
