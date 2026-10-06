package com.rotai.iq

import com.google.common.truth.Truth.assertThat
import com.rotai.iq.core.domain.model.FuelRecord
import com.rotai.iq.core.domain.model.MaintenanceRecord
import com.rotai.iq.core.domain.model.MaintenanceType
import com.rotai.iq.core.domain.model.VehicleExpense
import com.rotai.iq.core.domain.model.ExpenseCategory
import com.rotai.iq.core.network.SyncManager
import com.rotai.iq.core.network.SyncResponse
import org.junit.Test

class SyncManagerTest {

    private val syncManager = SyncManager()

    @Test
    fun preparePushPayload_aggregatesUnsyncedDataCorrectly() {
        val fuel = listOf(
            FuelRecord(
                id = "f-1",
                date = "2026-10-06",
                odometerKm = 50000.0,
                liters = 40.0,
                pricePerLiter = 5.50,
                isFullTank = true
            )
        )
        val maintenance = listOf(
            MaintenanceRecord(
                id = "m-1",
                date = "2026-10-06",
                description = "Troca de óleo e filtros",
                type = MaintenanceType.OIL_CHANGE,
                cost = 250.0,
                odometerKm = 50000.0
            )
        )
        val expenses = listOf(
            VehicleExpense(
                id = "e-1",
                date = "2026-10-06",
                category = ExpenseCategory.CAR_WASH,
                amount = 50.0,
                description = "Lavagem completa"
            )
        )

        val payload = syncManager.preparePushPayload(
            deviceId = "test-device-uuid-123",
            unsyncedFuel = fuel,
            unsyncedMaintenance = maintenance,
            unsyncedExpenses = expenses,
            unsyncedEvaluations = emptyList()
        )

        assertThat(payload.deviceId).isEqualTo("test-device-uuid-123")
        assertThat(payload.fuelRecords).hasSize(1)
        assertThat(payload.maintenanceRecords).hasSize(1)
        assertThat(payload.expenses).hasSize(1)
        assertThat(payload.evaluations).isEmpty()
        assertThat(payload.clientTimestamp).isGreaterThan(0L)
    }

    @Test
    fun processSyncResponse_handlesSuccessAndFailure() {
        val successResponse = SyncResponse(
            success = true,
            serverTimestamp = 1700000000L,
            acknowledgedFuelIds = listOf("f-1"),
            acknowledgedMaintenanceIds = listOf("m-1"),
            message = "Sync OK"
        )
        assertThat(syncManager.processSyncResponse(successResponse)).isTrue()

        val failureResponse = SyncResponse(
            success = false,
            serverTimestamp = 1700000000L,
            message = "Database lock error"
        )
        assertThat(syncManager.processSyncResponse(failureResponse)).isFalse()
    }
}
