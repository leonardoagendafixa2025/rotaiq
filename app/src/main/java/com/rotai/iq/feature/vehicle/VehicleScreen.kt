package com.rotai.iq.feature.vehicle

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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.rotai.iq.core.domain.model.FuelType
import com.rotai.iq.core.ui.components.CockpitMetricCard
import com.rotai.iq.core.ui.theme.BrandPrimary
import com.rotai.iq.core.ui.theme.ClassBad
import com.rotai.iq.core.ui.theme.ClassExcellent
import com.rotai.iq.core.ui.theme.CockpitBackground
import com.rotai.iq.core.ui.theme.CockpitSurface
import com.rotai.iq.core.ui.theme.TextPrimary
import com.rotai.iq.core.ui.theme.TextSecondary
import com.rotai.iq.feature.rides.outlinedColors
import java.util.Locale

@Composable
fun VehicleScreen(
    viewModel: VehicleViewModel,
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
            text = "CUSTO REAL DO VEÍCULO",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = BrandPrimary,
            letterSpacing = 0.5.sp
        )
        Text(
            text = "Configuração precisa de combustível, manutenção e custos fixos",
            fontSize = 13.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Cost Projection Highlight Cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CockpitMetricCard(
                title = "Custo por KM",
                value = "R$ %.2f".format(Locale("pt", "BR"), state.costProjections.costPerKm),
                subtitle = "Total consolidado",
                accentColor = ClassBad,
                modifier = Modifier.weight(1f)
            )
            CockpitMetricCard(
                title = "Custo por Hora",
                value = "R$ %.2f/h".format(Locale("pt", "BR"), state.costProjections.costPerHourEstimated),
                subtitle = "Trânsito urbano médio",
                accentColor = ClassBad,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CockpitMetricCard(
                title = "Fixo Mensal",
                value = "R$ %.2f".format(Locale("pt", "BR"), state.costProjections.monthlyFixedCost),
                subtitle = "Seguro, IPVA, etc.",
                accentColor = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            CockpitMetricCard(
                title = "Custo Anual",
                value = "R$ %.0f".format(Locale("pt", "BR"), state.costProjections.annualEstimatedCost),
                subtitle = "Projetado com km",
                accentColor = TextPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Fuel Type selector
        Text(
            text = "TIPO DE COMBUSTÍVEL",
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
            FuelType.entries.forEach { fuel ->
                FilterChip(
                    selected = state.fuelType == fuel,
                    onClick = { viewModel.onFuelTypeChanged(fuel) },
                    label = { Text(fuel.displayName) },
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

        // Form Fields
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = state.consumptionInput,
                onValueChange = { viewModel.onConsumptionChanged(it) },
                label = { Text("Consumo (km/L)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                colors = outlinedColors()
            )
            OutlinedTextField(
                value = state.fuelPriceInput,
                onValueChange = { viewModel.onFuelPriceChanged(it) },
                label = { Text("Preço Combustível (R$/L)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                colors = outlinedColors()
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = state.maintenancePerKmInput,
                onValueChange = { viewModel.onMaintenanceChanged(it) },
                label = { Text("Manutenção (R$/km)") },
                supportingText = { Text("Pneus, óleo, freios", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                colors = outlinedColors()
            )
            OutlinedTextField(
                value = state.estimatedMonthlyKmInput,
                onValueChange = { viewModel.onMonthlyKmChanged(it) },
                label = { Text("Km Rodados/Mês") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = outlinedColors()
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "CUSTOS FIXOS DO VEÍCULO",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = state.monthlyInsuranceInput,
                onValueChange = { viewModel.onInsuranceChanged(it) },
                label = { Text("Seguro (R$/mês)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                colors = outlinedColors()
            )
            OutlinedTextField(
                value = state.annualTaxesInput,
                onValueChange = { viewModel.onTaxesChanged(it) },
                label = { Text("IPVA/Licenc. (R$/ano)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                colors = outlinedColors()
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = state.monthlyDepreciationInput,
                onValueChange = { viewModel.onDepreciationChanged(it) },
                label = { Text("Depreciação (R$/mês)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                colors = outlinedColors()
            )
            OutlinedTextField(
                value = state.monthlyOtherCostsInput,
                onValueChange = { /* other costs */ },
                label = { Text("Lavagens/Outros (R$/mês)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                colors = outlinedColors()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { viewModel.saveVehicle() },
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
                text = if (state.isSavedSuccess) "CONFIGURAÇÃO SALVA COM SUCESSO" else "SALVAR CUSTO DO VEÍCULO",
                fontWeight = FontWeight.Bold,
                color = CockpitBackground,
                fontSize = 13.sp
            )
        }
    }
}
