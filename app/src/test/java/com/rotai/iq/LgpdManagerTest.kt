package com.rotai.iq

import com.rotai.iq.core.domain.model.LgpdConsent
import com.rotai.iq.core.domain.model.DeletionRequestStatus
import com.rotai.iq.core.security.LgpdManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LgpdManagerTest {

    private lateinit var lgpdManager: LgpdManager

    @Before
    fun setUp() {
        lgpdManager = LgpdManager()
    }

    @Test
    fun testGenerateExportPackageConformity() {
        val consent = LgpdConsent(
            termsAccepted = true,
            privacyPolicyAccepted = true,
            telemetryOptIn = true
        )

        val pkg = lgpdManager.generateExportPackage(
            driverId = "DRV-123",
            driverName = "Carlos Motorista",
            driverEmail = "carlos@gmail.com",
            driverCity = "São Paulo",
            registeredVehiclesCount = 1,
            totalRidesEvaluated = 120,
            totalFuelRecords = 15,
            totalMaintenanceRecords = 4,
            totalExpensesCount = 10,
            subscriptionTier = "Pro Mensal",
            consent = consent
        )

        assertTrue(pkg.exportId.startsWith("EXP-"))
        assertEquals("c***s@gmail.com", pkg.driverEmail)
        assertEquals(120, pkg.totalRidesEvaluated)

        val json = lgpdManager.exportToJsonString(pkg)
        assertTrue(json.contains("Lei 13.709/2018 - Art. 18"))
        assertTrue(json.contains("c***s@gmail.com"))
        assertTrue(json.contains("\"rides_evaluated\": 120"))
    }

    @Test
    fun testCreateDeletionRequest() {
        val request = lgpdManager.createDeletionRequest(
            driverId = "DRV-123",
            reason = "Solicitação voluntária"
        )

        assertTrue(request.requestId.startsWith("DEL-"))
        assertEquals(DeletionRequestStatus.COMPLETED, request.status)
        assertNotNull(request.completedAtEpochMs)
    }
}
