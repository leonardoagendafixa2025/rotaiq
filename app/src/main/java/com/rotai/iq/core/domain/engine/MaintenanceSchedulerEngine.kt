package com.rotai.iq.core.domain.engine

import com.rotai.iq.core.domain.model.MaintenanceRecord
import com.rotai.iq.core.domain.model.MaintenanceType

object MaintenanceSchedulerEngine {

    enum class ServiceUrgency(val label: String, val colorHex: Long) {
        OK("Em dia", 0xFF00E676),
        UPCOMING("Próxima da revisão", 0xFFFFD600),
        OVERDUE("Revisão vencida!", 0xFFFF1744)
    }

    data class MaintenanceStatus(
        val type: MaintenanceType,
        val lastServiceKm: Double,
        val nextServiceKm: Double,
        val kmRemaining: Double,
        val urgency: ServiceUrgency,
        val message: String
    )

    fun evaluateServiceStatus(
        record: MaintenanceRecord,
        currentOdometerKm: Double
    ): MaintenanceStatus {
        val nextKm = record.nextServiceKm ?: (record.odometerKm + record.type.typicalIntervalKm)
        val kmRemaining = nextKm - currentOdometerKm

        val urgency = when {
            kmRemaining < 0.0 -> ServiceUrgency.OVERDUE
            kmRemaining <= 1500.0 -> ServiceUrgency.UPCOMING
            else -> ServiceUrgency.OK
        }

        val message = when (urgency) {
            ServiceUrgency.OVERDUE -> "Revisão de ${record.type.displayName} vencida há %.0f km!".format(-kmRemaining)
            ServiceUrgency.UPCOMING -> "Faltam apenas %.0f km para ${record.type.displayName}.".format(kmRemaining)
            ServiceUrgency.OK -> "${record.type.displayName} em dia (faltam %.0f km).".format(kmRemaining)
        }

        return MaintenanceStatus(
            type = record.type,
            lastServiceKm = record.odometerKm,
            nextServiceKm = nextKm,
            kmRemaining = kmRemaining,
            urgency = urgency,
            message = message
        )
    }
}
