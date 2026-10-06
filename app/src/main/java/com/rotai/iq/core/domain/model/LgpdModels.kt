package com.rotai.iq.core.domain.model

data class LgpdConsent(
    val termsAccepted: Boolean = true,
    val privacyPolicyAccepted: Boolean = true,
    val telemetryOptIn: Boolean = true,
    val anonymousBenchmarkingOptIn: Boolean = true,
    val consentTimestampEpochMs: Long = System.currentTimeMillis(),
    val appVersionName: String = "1.0.0"
)

data class LgpdDataExport(
    val exportId: String,
    val exportedAtEpochMs: Long,
    val driverId: String,
    val driverName: String,
    val driverEmail: String,
    val driverCity: String,
    val registeredVehiclesCount: Int,
    val totalRidesEvaluated: Int,
    val totalFuelRecords: Int,
    val totalMaintenanceRecords: Int,
    val totalExpensesCount: Int,
    val subscriptionTier: String,
    val activeConsents: LgpdConsent,
    val rawVehiclesJson: String,
    val rawRecentEvaluationsJson: String
)

enum class DeletionRequestStatus {
    REQUESTED,
    PROCESSING,
    COMPLETED,
    FAILED
}

data class LgpdDeletionRequest(
    val requestId: String,
    val requestedAtEpochMs: Long,
    val driverId: String,
    val reason: String,
    val status: DeletionRequestStatus,
    val completedAtEpochMs: Long? = null
)
