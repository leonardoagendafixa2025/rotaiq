package com.rotai.iq

import com.rotai.iq.core.telemetry.TelemetryManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TelemetryManagerTest {

    private lateinit var telemetryManager: TelemetryManager

    @Before
    fun setUp() {
        telemetryManager = TelemetryManager(maxQueueSize = 5, retentionDays = 30)
    }

    @Test
    fun testRecordEventSanitizesPiiAutomatically() {
        telemetryManager.recordEvent(
            eventName = TelemetryManager.EVENT_RIDE_EVALUATED,
            properties = mapOf(
                "driver_cpf" to "123.456.789-00",
                "plate" to "ABC-1234",
                "fare" to "25.50"
            )
        )

        val events = telemetryManager.getPendingEvents()
        assertEquals(1, events.size)

        val props = events[0].properties
        assertEquals("***.456.789-**", props["driver_cpf"])
        assertEquals("ABC-****", props["plate"])
        assertEquals("25.50", props["fare"])
    }

    @Test
    fun testQueueOverflowRemovesOldest() {
        // Inserir 6 eventos com limite maxQueueSize = 5
        for (i in 1..6) {
            telemetryManager.recordEvent("event_$i", mapOf("index" to i.toString()))
        }

        val events = telemetryManager.getPendingEvents()
        assertEquals(5, events.size)
        // O primeiro evento deve ser o 2 (o 1 foi descartado por rotação)
        assertEquals("event_2", events[0].eventName)
        assertEquals("event_6", events[4].eventName)
    }

    @Test
    fun testClearEvents() {
        telemetryManager.recordEvent("test_event")
        assertEquals(1, telemetryManager.getQueueSize())

        telemetryManager.clearEvents()
        assertEquals(0, telemetryManager.getQueueSize())
    }
}
