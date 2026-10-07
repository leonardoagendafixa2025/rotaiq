package com.rotai.iq.core.domain.engine

import com.rotai.iq.core.domain.model.PaymentGateway
import com.rotai.iq.core.domain.model.SubscriptionInfo
import com.rotai.iq.core.domain.model.SubscriptionStatus
import com.rotai.iq.core.domain.model.SubscriptionTier
import java.util.UUID

sealed class PurchaseResult {
    data class Success(val subscription: SubscriptionInfo) : PurchaseResult()
    data class UserCanceled(val message: String = "Operação cancelada pelo usuário.") : PurchaseResult()
    data class Error(val message: String, val errorCode: Int? = null) : PurchaseResult()
}

class BillingManager {

    companion object {
        const val SKU_PRO_MONTHLY = "rotai_pro_monthly"
        const val SKU_PRO_ANNUAL = "rotai_pro_annual"
    }

    /**
     * Mapeia SKU para SubscriptionTier.
     */
    fun getTierForSku(sku: String): SubscriptionTier {
        return when (sku) {
            SKU_PRO_MONTHLY -> SubscriptionTier.PRO_MONTHLY
            SKU_PRO_ANNUAL -> SubscriptionTier.PRO_ANNUAL
            else -> SubscriptionTier.FREE
        }
    }

    /**
     * Processa a confirmação de uma compra do Google Play ou Sandbox.
     */
    fun processPurchase(
        tier: SubscriptionTier,
        gateway: PaymentGateway = PaymentGateway.GOOGLE_PLAY,
        externalToken: String? = null
    ): PurchaseResult {
        if (!tier.isPro) {
            return PurchaseResult.Error("Plano inválido para faturamento Pro: ${tier.displayName}")
        }

        val durationDays = if (tier == SubscriptionTier.PRO_ANNUAL) 365L else 30L
        val expiresAt = System.currentTimeMillis() + (durationDays * 24 * 60 * 60 * 1000)
        val token = externalToken ?: "tok_" + UUID.randomUUID().toString().take(12)

        val updatedSubscription = SubscriptionInfo(
            tier = tier,
            status = SubscriptionStatus.ACTIVE,
            expiresAtEpochMs = expiresAt,
            autoRenew = gateway == PaymentGateway.GOOGLE_PLAY,
            gateway = gateway,
            purchaseToken = token
        )

        return PurchaseResult.Success(updatedSubscription)
    }

    /**
     * Ativa assinatura via confirmação de PIX aprovado.
     */
    fun activatePixSubscription(
        tier: SubscriptionTier,
        orderId: String
    ): SubscriptionInfo {
        val durationDays = if (tier == SubscriptionTier.PRO_ANNUAL) 365L else 30L
        val expiresAt = System.currentTimeMillis() + (durationDays * 24 * 60 * 60 * 1000)

        return SubscriptionInfo(
            tier = tier,
            status = SubscriptionStatus.ACTIVE,
            expiresAtEpochMs = expiresAt,
            autoRenew = false,
            gateway = PaymentGateway.PIX,
            purchaseToken = "pix_order_$orderId"
        )
    }

    /**
     * Cancela renovação automática ou expira plano.
     */
    fun cancelSubscription(current: SubscriptionInfo): SubscriptionInfo {
        return current.copy(
            autoRenew = false,
            status = if (current.expiresAtEpochMs != null && current.expiresAtEpochMs > System.currentTimeMillis()) {
                SubscriptionStatus.ACTIVE // Mantém ativo até expirar
            } else {
                SubscriptionStatus.CANCELED
            }
        )
    }
}
