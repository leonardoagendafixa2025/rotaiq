package com.rotai.iq

import com.rotai.iq.core.domain.engine.FeatureGateManager
import com.rotai.iq.core.domain.model.FeatureAccessResult
import com.rotai.iq.core.domain.model.FeatureKey
import com.rotai.iq.core.domain.model.PaymentGateway
import com.rotai.iq.core.domain.model.SubscriptionInfo
import com.rotai.iq.core.domain.model.SubscriptionStatus
import com.rotai.iq.core.domain.model.SubscriptionTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FeatureGateManagerTest {

    private lateinit var featureGateManager: FeatureGateManager

    @Before
    fun setUp() {
        featureGateManager = FeatureGateManager()
    }

    @Test
    fun testFreeTierDailyEvaluationLimit() {
        val freeSub = SubscriptionInfo.FREE_DEFAULT

        // Abaixo do limite de 15 avaliações
        val access10 = featureGateManager.canEvaluateOffer(freeSub, dailyUsageCount = 10)
        assertTrue(access10.isGranted)
        assertEquals(5, featureGateManager.remainingEvaluationsToday(freeSub, dailyUsageCount = 10))

        // Exatamente no limite (15 avaliações realizadas) -> Bloqueado
        val access15 = featureGateManager.canEvaluateOffer(freeSub, dailyUsageCount = 15)
        assertFalse(access15.isGranted)
        assertTrue(access15 is FeatureAccessResult.Denied)
        assertEquals(0, featureGateManager.remainingEvaluationsToday(freeSub, dailyUsageCount = 15))

        // Acima do limite
        val access20 = featureGateManager.canEvaluateOffer(freeSub, dailyUsageCount = 20)
        assertFalse(access20.isGranted)
    }

    @Test
    fun testProTierUnlimitedAccess() {
        val proSub = SubscriptionInfo(
            tier = SubscriptionTier.PRO_MONTHLY,
            status = SubscriptionStatus.ACTIVE,
            expiresAtEpochMs = System.currentTimeMillis() + 86400000L,
            gateway = PaymentGateway.GOOGLE_PLAY
        )

        // Avaliação de corridas ilimitada
        val evalAccess = featureGateManager.canEvaluateOffer(proSub, dailyUsageCount = 100)
        assertTrue(evalAccess.isGranted)
        assertEquals(Int.MAX_VALUE, featureGateManager.remainingEvaluationsToday(proSub, dailyUsageCount = 100))

        // Recursos exclusivos Pro liberados
        assertTrue(featureGateManager.checkAccess(FeatureKey.FLOATING_HUD, proSub).isGranted)
        assertTrue(featureGateManager.checkAccess(FeatureKey.TTS_AUDIO_COPILOT, proSub).isGranted)
        assertTrue(featureGateManager.checkAccess(FeatureKey.DEADHEAD_PREDICTOR, proSub).isGranted)
        assertTrue(featureGateManager.checkAccess(FeatureKey.PLATFORM_COMPARISON, proSub).isGranted)
        assertTrue(featureGateManager.checkAccess(FeatureKey.TAX_EXPORT_REPORT, proSub).isGranted)
    }

    @Test
    fun testFreeTierBlockedFromProFeatures() {
        val freeSub = SubscriptionInfo.FREE_DEFAULT

        val hudAccess = featureGateManager.checkAccess(FeatureKey.FLOATING_HUD, freeSub)
        assertFalse(hudAccess.isGranted)

        val ttsAccess = featureGateManager.checkAccess(FeatureKey.TTS_AUDIO_COPILOT, freeSub)
        assertFalse(ttsAccess.isGranted)

        val deadheadAccess = featureGateManager.checkAccess(FeatureKey.DEADHEAD_PREDICTOR, freeSub)
        assertFalse(deadheadAccess.isGranted)

        val compareAccess = featureGateManager.checkAccess(FeatureKey.PLATFORM_COMPARISON, freeSub)
        assertFalse(compareAccess.isGranted)
    }

    @Test
    fun testExpiredProSubscriptionFallsBackToFree() {
        val expiredPro = SubscriptionInfo(
            tier = SubscriptionTier.PRO_MONTHLY,
            status = SubscriptionStatus.ACTIVE,
            expiresAtEpochMs = System.currentTimeMillis() - 1000L, // Expirado
            gateway = PaymentGateway.PIX
        )

        assertFalse(expiredPro.isProActive)
        val hudAccess = featureGateManager.checkAccess(FeatureKey.FLOATING_HUD, expiredPro)
        assertFalse(hudAccess.isGranted)
    }
}
