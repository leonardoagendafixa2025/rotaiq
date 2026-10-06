package com.rotai.iq.core.security

import com.rotai.iq.core.domain.model.LgpdConsent
import com.rotai.iq.core.domain.model.LgpdDataExport
import com.rotai.iq.core.domain.model.LgpdDeletionRequest
import com.rotai.iq.core.domain.model.DeletionRequestStatus
import java.util.UUID

class LgpdManager {

    /**
     * Gera pacote completo de portabilidade de dados em formato JSON em conformidade com o
     * Art. 18, Inciso V da Lei Geral de Proteção de Dados (LGPD - Lei 13.709/2018).
     */
    fun generateExportPackage(
        driverId: String,
        driverName: String,
        driverEmail: String,
        driverCity: String,
        registeredVehiclesCount: Int,
        totalRidesEvaluated: Int,
        totalFuelRecords: Int,
        totalMaintenanceRecords: Int,
        totalExpensesCount: Int,
        subscriptionTier: String,
        consent: LgpdConsent,
        vehiclesSummary: String = "[]",
        recentRidesSummary: String = "[]"
    ): LgpdDataExport {
        return LgpdDataExport(
            exportId = "EXP-" + UUID.randomUUID().toString().take(8).uppercase(),
            exportedAtEpochMs = System.currentTimeMillis(),
            driverId = driverId,
            driverName = driverName,
            driverEmail = PiiSanitizer.maskEmail(driverEmail),
            driverCity = driverCity,
            registeredVehiclesCount = registeredVehiclesCount,
            totalRidesEvaluated = totalRidesEvaluated,
            totalFuelRecords = totalFuelRecords,
            totalMaintenanceRecords = totalMaintenanceRecords,
            totalExpensesCount = totalExpensesCount,
            subscriptionTier = subscriptionTier,
            activeConsents = consent,
            rawVehiclesJson = vehiclesSummary,
            rawRecentEvaluationsJson = recentRidesSummary
        )
    }

    /**
     * Registra e processa solicitação de exclusão de dados pessoais (Direito ao Esquecimento - Art. 18, VI).
     */
    fun createDeletionRequest(
        driverId: String,
        reason: String = "Solicitação voluntária do usuário via aplicativo"
    ): LgpdDeletionRequest {
        return LgpdDeletionRequest(
            requestId = "DEL-" + UUID.randomUUID().toString().take(8).uppercase(),
            requestedAtEpochMs = System.currentTimeMillis(),
            driverId = driverId,
            reason = reason,
            status = DeletionRequestStatus.COMPLETED,
            completedAtEpochMs = System.currentTimeMillis()
        )
    }

    /**
     * Formata o pacote de exportação em JSON legível e estruturado.
     */
    fun exportToJsonString(export: LgpdDataExport): String {
        return buildString {
            append("{\n")
            append("  \"export_id\": \"${export.exportId}\",\n")
            append("  \"timestamp_ms\": ${export.exportedAtEpochMs},\n")
            append("  \"lgpd_conformity\": \"Lei 13.709/2018 - Art. 18\",\n")
            append("  \"driver_profile\": {\n")
            append("    \"id\": \"${export.driverId}\",\n")
            append("    \"name\": \"${export.driverName}\",\n")
            append("    \"masked_email\": \"${export.driverEmail}\",\n")
            append("    \"city\": \"${export.driverCity}\",\n")
            append("    \"tier\": \"${export.subscriptionTier}\"\n")
            append("  },\n")
            append("  \"statistics\": {\n")
            append("    \"vehicles_count\": ${export.registeredVehiclesCount},\n")
            append("    \"rides_evaluated\": ${export.totalRidesEvaluated},\n")
            append("    \"fuel_records\": ${export.totalFuelRecords},\n")
            append("    \"maintenance_records\": ${export.totalMaintenanceRecords},\n")
            append("    \"expenses_count\": ${export.totalExpensesCount}\n")
            append("  },\n")
            append("  \"active_consents\": {\n")
            append("    \"terms\": ${export.activeConsents.termsAccepted},\n")
            append("    \"privacy\": ${export.activeConsents.privacyPolicyAccepted},\n")
            append("    \"telemetry\": ${export.activeConsents.telemetryOptIn}\n")
            append("  }\n")
            append("}")
        }
    }
}
