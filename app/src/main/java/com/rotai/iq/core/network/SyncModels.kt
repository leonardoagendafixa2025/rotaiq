package com.rotai.iq.core.network

import com.rotai.iq.core.domain.model.FuelRecord
import com.rotai.iq.core.domain.model.MaintenanceRecord
import com.rotai.iq.core.domain.model.RideEvaluation
import com.rotai.iq.core.domain.model.VehicleExpense

data class SyncPushPayload(
    val deviceId: String,
    val clientTimestamp: Long = System.currentTimeMillis(),
    val fuelRecords: List<FuelRecord> = emptyList(),
    val maintenanceRecords: List<MaintenanceRecord> = emptyList(),
    val expenses: List<VehicleExpense> = emptyList(),
    val evaluations: List<RideEvaluation> = emptyList()
)

data class SyncResponse(
    val success: Boolean,
    val serverTimestamp: Long,
    val acknowledgedFuelIds: List<String> = emptyList(),
    val acknowledgedMaintenanceIds: List<String> = emptyList(),
    val acknowledgedExpenseIds: List<String> = emptyList(),
    val acknowledgedEvaluationIds: List<String> = emptyList(),
    val message: String? = null
)

enum class SyncState {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR_OFFLINE,
    ERROR_SERVER
}

class SyncManager {
    fun preparePushPayload(
        deviceId: String,
        unsyncedFuel: List<FuelRecord>,
        unsyncedMaintenance: List<MaintenanceRecord>,
        unsyncedExpenses: List<VehicleExpense>,
        unsyncedEvaluations: List<RideEvaluation>
    ): SyncPushPayload {
        return SyncPushPayload(
            deviceId = deviceId,
            fuelRecords = unsyncedFuel,
            maintenanceRecords = unsyncedMaintenance,
            expenses = unsyncedExpenses,
            evaluations = unsyncedEvaluations
        )
    }

    fun processSyncResponse(response: SyncResponse): Boolean {
        return response.success
    }
}
