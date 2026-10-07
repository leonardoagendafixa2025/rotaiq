package com.rotai.iq

import com.rotai.iq.core.network.RemotePlan
import com.rotai.iq.core.network.SupabaseConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SupabaseSyncClientTest {

    @Test
    fun supabaseConfig_containsValidProjectEndpoints() {
        assertNotNull(SupabaseConfig.PROJECT_URL)
        assertTrue(SupabaseConfig.PROJECT_URL.startsWith("https://"))
        assertTrue(SupabaseConfig.REST_ENDPOINT.contains("/rest/v1"))
        assertTrue(SupabaseConfig.PUBLISHABLE_KEY.startsWith("sb_publishable_"))
    }

    @Test
    fun remotePlan_instantiatesCorrectly() {
        val plan = RemotePlan(
            code = "pro_monthly",
            name = "ROTA IQ Pro Mensal",
            priceCents = 2990,
            interval = "month"
        )
        assertEquals("pro_monthly", plan.code)
        assertEquals(2990, plan.priceCents)
        assertEquals(29.90, plan.priceCents / 100.0, 0.01)
    }
}
