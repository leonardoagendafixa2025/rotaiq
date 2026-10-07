package com.rotai.iq

import com.rotai.iq.core.domain.engine.DriverTaxCategory
import com.rotai.iq.core.domain.engine.DriverTaxReportEngine
import com.rotai.iq.core.domain.model.ExpenseCategory
import com.rotai.iq.core.domain.model.FuelRecord
import com.rotai.iq.core.domain.model.FuelType
import com.rotai.iq.core.domain.model.MaintenanceRecord
import com.rotai.iq.core.domain.model.MaintenanceType
import com.rotai.iq.core.domain.model.VehicleExpense
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DriverTaxReportEngineTest {

    private lateinit var engine: DriverTaxReportEngine

    @Before
    fun setUp() {
        engine = DriverTaxReportEngine()
    }

    @Test
    fun generateAnnualTaxReport_calculatesPassengerTransportExemptionCorrectly() {
        val gross = 50000.0 // R$ 50.000 de faturamento
        val fuelRecords = listOf(
            FuelRecord(date = "2026-05-10", odometerKm = 10000.0, liters = 40.0, pricePerLiter = 5.50, totalPaid = 220.0, fuelType = FuelType.GASOLINE),
            FuelRecord(date = "2026-05-20", odometerKm = 10500.0, liters = 40.0, pricePerLiter = 5.50, totalPaid = 220.0, fuelType = FuelType.GASOLINE)
        )
        val maintenance = listOf(
            MaintenanceRecord(date = "2026-06-01", odometerKm = 10000.0, type = MaintenanceType.OIL_CHANGE, description = "Óleo", cost = 280.0)
        )
        val expenses = listOf(
            VehicleExpense(date = "2026-01-15", category = ExpenseCategory.TAXES, description = "IPVA", amount = 1500.0)
        )

        val report = engine.generateAnnualTaxReport(
            year = 2026,
            category = DriverTaxCategory.PASSENGER_TRANSPORT,
            grossRevenue = gross,
            fuelRecords = fuelRecords,
            maintenanceRecords = maintenance,
            otherExpenses = expenses
        )

        // 16% isento para passageiros = 50.000 * 0.16 = 8.000
        assertEquals(8000.0, report.exemptPortion, 0.01)
        // Parcela tributável antes de despesas = 50.000 - 8.000 = 42.000
        assertEquals(42000.0, report.rawTaxablePortion, 0.01)
        // Total despesas dedutíveis: 440 + 280 + 1500 = 2220
        assertEquals(2220.0, report.totalDeductibleExpenses, 0.01)
        // Tributável líquido final: 42.000 - 2.220 = 39.780
        assertEquals(39780.0, report.netTaxableIncome, 0.01)
        // Teto MEI 81k: 50.000 / 81.000 = ~61.7%
        assertFalse(report.isExceedingMeiLimit)
        assertEquals(61.72, report.meiUsagePercentage, 0.5)
    }

    @Test
    fun generateAnnualTaxReport_calculatesDeliveryTransport60PercentExemption() {
        val gross = 40000.0
        val report = engine.generateAnnualTaxReport(
            year = 2026,
            category = DriverTaxCategory.CARGO_DELIVERY,
            grossRevenue = gross,
            fuelRecords = emptyList(),
            maintenanceRecords = emptyList(),
            otherExpenses = emptyList()
        )

        // 60% de 40.000 = 24.000 isento
        assertEquals(24000.0, report.exemptPortion, 0.01)
        assertEquals(16000.0, report.rawTaxablePortion, 0.01)
        assertEquals(16000.0, report.netTaxableIncome, 0.01)
        // 16.000 é menor que a faixa de isenção anual de 33.888
        assertTrue(report.isIrpfExempt)
    }

    @Test
    fun generateCashBookCsv_outputsFormattedCsvRows() {
        val fuel = listOf(
            FuelRecord(date = "2026-04-01", odometerKm = 5000.0, liters = 30.0, pricePerLiter = 5.0, totalPaid = 150.0)
        )
        val csv = engine.generateCashBookCsv(
            grossRevenue = 12000.0,
            fuelRecords = fuel,
            maintenanceRecords = emptyList(),
            otherExpenses = emptyList()
        )

        assertTrue(csv.contains("Data,Tipo,Categoria,Descricao,Valor(R$)"))
        assertTrue(csv.contains("RECEITA,Corridas de Aplicativo"))
        assertTrue(csv.contains("DESPESA,Combustível"))
        assertTrue(csv.contains("150.00"))
    }
}
