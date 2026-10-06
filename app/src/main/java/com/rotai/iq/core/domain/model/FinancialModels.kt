package com.rotai.iq.core.domain.model

import java.util.UUID

enum class MaintenanceType(val displayName: String, val typicalIntervalKm: Int) {
    OIL_CHANGE("Troca de Óleo e Filtro", 10000),
    TIRES("Pneus / Alinhamento", 40000),
    BRAKES("Pastilhas e Discos de Freio", 25000),
    SUSPENSION("Suspensão e Amortecedores", 50000),
    REVISION("Revisão Preventiva Geral", 10000),
    OTHER("Outros Serviços", 0);
}

enum class ExpenseCategory(val displayName: String) {
    FUEL("Combustível"),
    MAINTENANCE("Manutenção"),
    INSURANCE("Seguro"),
    TAXES("IPVA e Licenciamento"),
    FINANCING("Financiamento / Parcela"),
    PARKING("Estacionamento"),
    TOLL("Pedágio"),
    CAR_WASH("Lavagem / Estética"),
    OTHER("Outras Despesas");
}

enum class FinancialPeriod(val displayName: String) {
    DAILY("Diário"),
    WEEKLY("Semanal"),
    MONTHLY("Mensal"),
    ANNUAL("Anual");
}

data class FuelRecord(
    val id: String = UUID.randomUUID().toString(),
    val vehicleId: String = "default_vehicle",
    val date: String,                          // YYYY-MM-DD
    val odometerKm: Double,                    // Km atual no painel
    val liters: Double,                        // Litros abastecidos
    val pricePerLiter: Double,                 // Preço pago por litro
    val totalPaid: Double = liters * pricePerLiter,
    val fuelType: FuelType = FuelType.GASOLINE,
    val isFullTank: Boolean = true,            // Abasteceu tanque cheio?
    val calculatedKmPerLiter: Double? = null,   // Calculado se houver abastecimento anterior tanque cheio
    val calculatedCostPerKm: Double? = null,   // R$/km real apurado
    val notes: String? = null,
    val syncedWithServer: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class MaintenanceRecord(
    val id: String = UUID.randomUUID().toString(),
    val vehicleId: String = "default_vehicle",
    val date: String,                          // YYYY-MM-DD
    val odometerKm: Double,                    // Km na realização
    val type: MaintenanceType = MaintenanceType.OIL_CHANGE,
    val description: String,
    val cost: Double,
    val nextServiceKm: Double? = null,         // Previsão do próximo km
    val isCompleted: Boolean = true,
    val notes: String? = null,
    val syncedWithServer: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getKmRemaining(currentOdometerKm: Double): Double? {
        return nextServiceKm?.let { (it - currentOdometerKm).coerceAtLeast(0.0) }
    }
}

data class VehicleExpense(
    val id: String = UUID.randomUUID().toString(),
    val vehicleId: String = "default_vehicle",
    val date: String,                          // YYYY-MM-DD
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val description: String,
    val amount: Double,
    val notes: String? = null,
    val syncedWithServer: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class ComprehensiveFinancialReport(
    val period: FinancialPeriod,
    val periodLabel: String,                   // Ex: "Hoje", "Semana 41", "Outubro 2026", "Ano 2026"
    val grossRevenue: Double,                  // Faturamento bruto
    val fuelCosts: Double,                     // Gastos com combustível
    val maintenanceCosts: Double,              // Gastos com manutenção
    val fixedCosts: Double,                    // Custos fixos (seguro, impostos, depreciação)
    val otherExpenses: Double,                 // Pedágios, estacionamento, lavagens
    val totalCosts: Double = fuelCosts + maintenanceCosts + fixedCosts + otherExpenses,
    val netProfit: Double = grossRevenue - totalCosts,
    val profitMarginPercent: Double = if (grossRevenue > 0) ((netProfit / grossRevenue) * 100.0) else 0.0,
    val totalKmDriven: Double,
    val totalHoursOnline: Double,
    val totalHoursDriving: Double,
    val totalRidesCompleted: Int,
    val grossRatePerKm: Double = if (totalKmDriven > 0) grossRevenue / totalKmDriven else 0.0,
    val netProfitPerKm: Double = if (totalKmDriven > 0) netProfit / totalKmDriven else 0.0,
    val grossRatePerHour: Double = if (totalHoursOnline > 0) grossRevenue / totalHoursOnline else 0.0,
    val netProfitPerHour: Double = if (totalHoursOnline > 0) netProfit / totalHoursOnline else 0.0
)
