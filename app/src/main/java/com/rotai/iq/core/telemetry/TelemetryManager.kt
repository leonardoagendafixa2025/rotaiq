package com.rotai.iq.core.telemetry

import com.rotai.iq.core.security.PiiSanitizer
import java.util.UUID

data class TelemetryEvent(
    val eventId: String = UUID.randomUUID().toString(),
    val eventName: String,
    val timestampEpochMs: Long = System.currentTimeMillis(),
    val properties: Map<String, String> = emptyMap()
)

class TelemetryManager(
    private val maxQueueSize: Int = 500,
    private val retentionDays: Int = 30
) {
    private val eventQueue = mutableListOf<TelemetryEvent>()

    companion object {
        const val EVENT_RIDE_EVALUATED = "ride_evaluated"
        const val EVENT_SUBSCRIPTION_UPGRADE_CLICKED = "subscription_upgrade_clicked"
        const val EVENT_SUBSCRIPTION_ACTIVATED = "subscription_activated"
        const val EVENT_FEATURE_GATED = "feature_gated"
        const val EVENT_PIX_GENERATED = "pix_order_generated"
        const val EVENT_HUD_TRIGGERED = "hud_overlay_triggered"
        const val EVENT_LGPD_EXPORT_REQUESTED = "lgpd_export_requested"
    }

    /**
     * Registra evento sanitizando automaticamente qualquer dado pessoal nas propriedades.
     */
    fun recordEvent(eventName: String, properties: Map<String, String> = emptyMap()) {
        val sanitizedProperties = properties.mapValues { (_, value) ->
            PiiSanitizer.sanitizeText(value)
        }

        val event = TelemetryEvent(
            eventName = eventName,
            properties = sanitizedProperties
        )

        synchronized(eventQueue) {
            pruneExpiredEvents()
            if (eventQueue.size >= maxQueueSize) {
                eventQueue.removeAt(0) // Remove o mais antigo
            }
            eventQueue.add(event)
        }
    }

    fun getPendingEvents(): List<TelemetryEvent> {
        synchronized(eventQueue) {
            return eventQueue.toList()
        }
    }

    fun clearEvents() {
        synchronized(eventQueue) {
            eventQueue.clear()
        }
    }

    fun getQueueSize(): Int {
        synchronized(eventQueue) {
            return eventQueue.size
        }
    }

    private fun pruneExpiredEvents() {
        val cutoff = System.currentTimeMillis() - (retentionDays * 24L * 60 * 60 * 1000)
        eventQueue.removeAll { it.timestampEpochMs < cutoff }
    }
}
