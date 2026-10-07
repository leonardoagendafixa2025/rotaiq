package com.rotai.iq.core.domain.engine

import com.rotai.iq.core.domain.model.FuelRecord
import com.rotai.iq.core.domain.model.MaintenanceRecord
import com.rotai.iq.core.domain.model.VehicleExpense
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

enum class DriverTaxCategory(val displayName: String, val exemptRate: Double) {
    PASSENGER_TRANSPORT("Transporte de Passageiros (Uber/99/inDrive)", 0.16), // 16% isento
    CARGO_DELIVERY("Transporte de Encomendas / Entregas", 0.60)              // 60% isento
}

data class TaxReportSummary(
    val year: Int,
    val category: DriverTaxCategory,
    val grossRevenue: Double,
    val exemptPortion: Double,
    val rawTaxablePortion: Double,
    val deductibleFuelExpenses: Double,
    val deductibleMaintenanceExpenses: Double,
    val otherDeductibleExpenses: Double,
    val totalDeductibleExpenses: Double,
    val netTaxableIncome: Double,
    val meiAnnualCeiling: Double = 81000.0,
    val meiUsagePercentage: Double,
    val isExceedingMeiLimit: Boolean,
    val isIrpfExempt: Boolean,
    val taxDiagnosis: String
)

data class CashBookEntry(
    val date: String,
    val type: String, // "RECEITA" ou "DESPESA"
    val category: String,
    val description: String,
    val amount: Double
)

class DriverTaxReportEngine {

    companion object {
        const val MEI_ANNUAL_CEILING = 81000.0
        const val IRPF_ANNUAL_EXEMPTION_LIMIT = 33888.0 // Faixa de isenção anual estimada da RFB
    }

    fun generateAnnualTaxReport(
        year: Int,
        category: DriverTaxCategory = DriverTaxCategory.PASSENGER_TRANSPORT,
        grossRevenue: Double,
        fuelRecords: List<FuelRecord>,
        maintenanceRecords: List<MaintenanceRecord>,
        otherExpenses: List<VehicleExpense>
    ): TaxReportSummary {
        val totalFuel = fuelRecords.sumOf { it.totalPaid }
        val totalMaintenance = maintenanceRecords.sumOf { it.cost }
        val totalOther = otherExpenses.sumOf { it.amount }
        val totalDeductibles = totalFuel + totalMaintenance + totalOther

        val exemptPortion = grossRevenue * category.exemptRate
        val rawTaxablePortion = max(0.0, grossRevenue - exemptPortion)
        val netTaxableIncome = max(0.0, rawTaxablePortion - totalDeductibles)

        val meiUsage = if (MEI_ANNUAL_CEILING > 0) (grossRevenue / MEI_ANNUAL_CEILING) * 100.0 else 0.0
        val isExceedingMei = grossRevenue > MEI_ANNUAL_CEILING
        val isIrpfExempt = netTaxableIncome <= IRPF_ANNUAL_EXEMPTION_LIMIT

        val diagnosis = when {
            isExceedingMei -> {
                String.format(
                    Locale("pt", "BR"),
                    "ATENÇÃO: Faturamento bruto (R$ %.2f) excedeu o teto MEI de R$ 81.000,00 (%.1f%%). Recomenda-se desenquadramento para ME (Simples Nacional).",
                    grossRevenue,
                    meiUsage
                )
            }
            netTaxableIncome == 0.0 -> {
                String.format(
                    Locale("pt", "BR"),
                    "100%% BLINDADO: Suas despesas comprovadas do veículo (R$ %.2f) anularam totalmente o rendimento tributável. Zero imposto IRPF a pagar!",
                    totalDeductibles
                )
            }
            isIrpfExempt -> {
                String.format(
                    Locale("pt", "BR"),
                    "ISENTO DE IRPF: O rendimento tributável apurado (R$ %.2f) ficou abaixo do limite anual de R$ %.2f. Faturamento dentro do MEI (%.1f%% do teto).",
                    netTaxableIncome,
                    IRPF_ANNUAL_EXEMPTION_LIMIT,
                    meiUsage
                )
            }
            else -> {
                String.format(
                    Locale("pt", "BR"),
                    "TRIBUTÁVEL: Rendimento líquido a declarar de R$ %.2f excede a faixa de isenção. Verifique inclusão de mais comprovantes de abastecimento e manutenção.",
                    netTaxableIncome
                )
            }
        }

        return TaxReportSummary(
            year = year,
            category = category,
            grossRevenue = grossRevenue,
            exemptPortion = exemptPortion,
            rawTaxablePortion = rawTaxablePortion,
            deductibleFuelExpenses = totalFuel,
            deductibleMaintenanceExpenses = totalMaintenance,
            otherDeductibleExpenses = totalOther,
            totalDeductibleExpenses = totalDeductibles,
            netTaxableIncome = netTaxableIncome,
            meiAnnualCeiling = MEI_ANNUAL_CEILING,
            meiUsagePercentage = meiUsage,
            isExceedingMeiLimit = isExceedingMei,
            isIrpfExempt = isIrpfExempt,
            taxDiagnosis = diagnosis
        )
    }

    fun generateCashBookCsv(
        grossRevenue: Double,
        fuelRecords: List<FuelRecord>,
        maintenanceRecords: List<MaintenanceRecord>,
        otherExpenses: List<VehicleExpense>
    ): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val entries = mutableListOf<CashBookEntry>()

        // Faturamento
        entries.add(
            CashBookEntry(
                date = dateFormat.format(Date()),
                type = "RECEITA",
                category = "Corridas de Aplicativo",
                description = "Faturamento consolidado em plataformas",
                amount = grossRevenue
            )
        )

        // Abastecimentos
        fuelRecords.forEach { fuel ->
            entries.add(
                CashBookEntry(
                    date = fuel.date,
                    type = "DESPESA",
                    category = "Combustível",
                    description = "Abastecimento ${fuel.fuelType.displayName} (%.2f L)".format(
                        Locale("pt", "BR"),
                        fuel.liters
                    ),
                    amount = fuel.totalPaid
                )
            )
        }

        // Manutenções
        maintenanceRecords.forEach { maint ->
            entries.add(
                CashBookEntry(
                    date = maint.date,
                    type = "DESPESA",
                    category = "Manutenção",
                    description = "${maint.type.displayName} (Odômetro: ${maint.odometerKm} km)",
                    amount = maint.cost
                )
            )
        }

        // Outras despesas
        otherExpenses.forEach { exp ->
            entries.add(
                CashBookEntry(
                    date = exp.date,
                    type = "DESPESA",
                    category = exp.category.displayName,
                    description = exp.description,
                    amount = exp.amount
                )
            )
        }

        val sb = StringBuilder()
        sb.append("Data,Tipo,Categoria,Descricao,Valor(R$)\n")
        entries.forEach { entry ->
            val cleanDesc = entry.description.replace(",", ";").replace("\"", "")
            sb.append("${entry.date},${entry.type},${entry.category},\"$cleanDesc\",%.2f\n".format(Locale.US, entry.amount))
        }

        return sb.toString()
    }
}
