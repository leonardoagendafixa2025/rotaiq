package com.rotai.iq

import com.google.common.truth.Truth.assertThat
import com.rotai.iq.core.domain.engine.MaintenanceSchedulerEngine
import com.rotai.iq.core.domain.model.MaintenanceRecord
import com.rotai.iq.core.domain.model.MaintenanceType
import org.junit.Test

class MaintenanceSchedulerEngineTest {

    @Test
    fun evaluateServiceStatus_serviceUpToDate_returnsOk() {
        val record = MaintenanceRecord(
            date = "2026-09-01",
            odometerKm = 50000.0,
            type = MaintenanceType.OIL_CHANGE,
            description = "Troca de óleo 5W30",
            cost = 250.0,
            nextServiceKm = 60000.0
        )

        // Se o carro está com 53.000 km, faltam 7.000 km -> OK
        val status = MaintenanceSchedulerEngine.evaluateServiceStatus(record, currentOdometerKm = 53000.0)
        assertThat(status.urgency).isEqualTo(MaintenanceSchedulerEngine.ServiceUrgency.OK)
        assertThat(status.kmRemaining).isEqualTo(7000.0)
        assertThat(status.message).contains("em dia")
    }

    @Test
    fun evaluateServiceStatus_serviceUpcoming_returnsUpcomingWarning() {
        val record = MaintenanceRecord(
            date = "2026-09-01",
            odometerKm = 50000.0,
            type = MaintenanceType.OIL_CHANGE,
            description = "Troca de óleo 5W30",
            cost = 250.0,
            nextServiceKm = 60000.0
        )

        // Se o carro está com 59.200 km, faltam 800 km -> UPCOMING
        val status = MaintenanceSchedulerEngine.evaluateServiceStatus(record, currentOdometerKm = 59200.0)
        assertThat(status.urgency).isEqualTo(MaintenanceSchedulerEngine.ServiceUrgency.UPCOMING)
        assertThat(status.kmRemaining).isEqualTo(800.0)
        assertThat(status.message).contains("Faltam apenas 800 km")
    }

    @Test
    fun evaluateServiceStatus_serviceOverdue_returnsOverdueAlert() {
        val record = MaintenanceRecord(
            date = "2026-09-01",
            odometerKm = 50000.0,
            type = MaintenanceType.OIL_CHANGE,
            description = "Troca de óleo 5W30",
            cost = 250.0,
            nextServiceKm = 60000.0
        )

        // Se o carro está com 60.500 km, venceu há 500 km -> OVERDUE
        val status = MaintenanceSchedulerEngine.evaluateServiceStatus(record, currentOdometerKm = 60500.0)
        assertThat(status.urgency).isEqualTo(MaintenanceSchedulerEngine.ServiceUrgency.OVERDUE)
        assertThat(status.kmRemaining).isLessThan(0.0)
        assertThat(status.message).contains("vencida há 500 km")
    }
}
