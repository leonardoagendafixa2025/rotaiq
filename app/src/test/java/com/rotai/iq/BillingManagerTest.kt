package com.rotai.iq

import com.rotai.iq.core.domain.engine.BillingManager
import com.rotai.iq.core.domain.engine.PurchaseResult
import com.rotai.iq.core.domain.model.PaymentGateway
import com.rotai.iq.core.domain.model.SubscriptionStatus
import com.rotai.iq.core.domain.model.SubscriptionTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BillingManagerTest {

    private lateinit var billingManager: BillingManager

    @Before
    fun setUp() {
        billingManager = BillingManager()
    }

    @Test
    fun testProcessPurchaseMonthly() {
        val result = billingManager.processPurchase(
            tier = SubscriptionTier.PRO_MONTHLY,
            gateway = PaymentGateway.GOOGLE_PLAY,
            mockToken = "play_token_123"
        )

        assertTrue(result is PurchaseResult.Success)
        val success = result as PurchaseResult.Success
        assertEquals(SubscriptionTier.PRO_MONTHLY, success.subscription.tier)
        assertEquals(SubscriptionStatus.ACTIVE, success.subscription.status)
        assertEquals(PaymentGateway.GOOGLE_PLAY, success.subscription.gateway)
        assertTrue(success.subscription.autoRenew)
        assertTrue(success.subscription.isProActive)
    }

    @Test
    fun testProcessPurchaseAnnual() {
        val result = billingManager.processPurchase(
            tier = SubscriptionTier.PRO_ANNUAL,
            gateway = PaymentGateway.GOOGLE_PLAY
        )

        assertTrue(result is PurchaseResult.Success)
        val success = result as PurchaseResult.Success
        assertEquals(SubscriptionTier.PRO_ANNUAL, success.subscription.tier)
        assertTrue(success.subscription.expiresAtEpochMs!! > System.currentTimeMillis() + (360L * 24 * 60 * 60 * 1000))
    }

    @Test
    fun testActivatePixSubscription() {
        val sub = billingManager.activatePixSubscription(
            tier = SubscriptionTier.PRO_MONTHLY,
            orderId = "ROTAIQ889"
        )

        assertEquals(SubscriptionTier.PRO_MONTHLY, sub.tier)
        assertEquals(SubscriptionStatus.ACTIVE, sub.status)
        assertEquals(PaymentGateway.PIX, sub.gateway)
        assertEquals(false, sub.autoRenew)
        assertTrue(sub.purchaseToken!!.contains("ROTAIQ889"))
    }

    @Test
    fun testCancelSubscriptionKeepsAccessUntilExpiration() {
        val active = billingManager.activatePixSubscription(SubscriptionTier.PRO_MONTHLY, "ORD1")
        val canceled = billingManager.cancelSubscription(active)

        assertEquals(false, canceled.autoRenew)
        assertEquals(SubscriptionStatus.ACTIVE, canceled.status)
        assertTrue(canceled.isProActive)
    }
}
