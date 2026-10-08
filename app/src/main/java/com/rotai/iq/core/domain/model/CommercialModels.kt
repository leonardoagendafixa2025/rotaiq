package com.rotai.iq.core.domain.model

enum class SubscriptionTier(
    val code: String,
    val displayName: String,
    val monthlyEquivalentPrice: Double,
    val fullPrice: Double,
    val billingPeriodMonths: Int,
    val isPro: Boolean
) {
    FREE(
        code = "free",
        displayName = "Gratuito",
        monthlyEquivalentPrice = 0.0,
        fullPrice = 0.0,
        billingPeriodMonths = 0,
        isPro = false
    ),
    PRO_MONTHLY(
        code = "pro_monthly",
        displayName = "Pro Mensal",
        monthlyEquivalentPrice = 29.90,
        fullPrice = 29.90,
        billingPeriodMonths = 1,
        isPro = true
    ),
    PRO_ANNUAL(
        code = "pro_annual",
        displayName = "Pro Anual (33% OFF)",
        monthlyEquivalentPrice = 19.99,
        fullPrice = 239.90,
        billingPeriodMonths = 12,
        isPro = true
    )
}

enum class SubscriptionStatus {
    ACTIVE,
    TRIALING,
    GRACE_PERIOD,
    EXPIRED,
    CANCELED,
    NONE
}

enum class PaymentGateway(val displayName: String) {
    GOOGLE_PLAY("Google Play Billing"),
    PIX("PIX Banco Central"),
    CREDIT_CARD("Cartão de Crédito"),
    SANDBOX("Ambiente de Testes / Sandbox"),
    NONE("Nenhum")
}

data class SubscriptionPlan(
    val tier: SubscriptionTier,
    val title: String,
    val headline: String,
    val formattedPrice: String,
    val periodSuffix: String,
    val savingsBadge: String?,
    val highlights: List<String>
)

data class SubscriptionInfo(
    val tier: SubscriptionTier = SubscriptionTier.FREE,
    val status: SubscriptionStatus = SubscriptionStatus.NONE,
    val expiresAtEpochMs: Long? = null,
    val trialStartedAtEpochMs: Long? = null,
    val autoRenew: Boolean = false,
    val gateway: PaymentGateway = PaymentGateway.NONE,
    val purchaseToken: String? = null
) {
    /**
     * Indica se o usuário está dentro dos 7 dias de degustação gratuita.
     */
    val isTrialActive: Boolean
        get() = status == SubscriptionStatus.TRIALING &&
                (expiresAtEpochMs == null || expiresAtEpochMs > System.currentTimeMillis())

    /**
     * Dias restantes do período de teste gratuito de 7 dias (arredondado para cima).
     */
    val trialDaysRemaining: Int
        get() {
            if (!isTrialActive || expiresAtEpochMs == null) return 0
            val diffMs = expiresAtEpochMs - System.currentTimeMillis()
            return if (diffMs > 0) kotlin.math.ceil(diffMs.toDouble() / (1000.0 * 60.0 * 60.0 * 24.0)).toInt() else 0
        }

    /**
     * Indica se o teste gratuito de 7 dias já expirou e o usuário precisa assinar para continuar.
     */
    val isTrialExpired: Boolean
        get() = status == SubscriptionStatus.EXPIRED ||
                (status == SubscriptionStatus.TRIALING && expiresAtEpochMs != null && expiresAtEpochMs <= System.currentTimeMillis())

    /**
     * Se os recursos Pro estão liberados (seja por assinatura paga ativa ou trial de 7 dias ativo).
     */
    val isProActive: Boolean
        get() = tier.isPro && (
            status == SubscriptionStatus.ACTIVE ||
            status == SubscriptionStatus.TRIALING ||
            status == SubscriptionStatus.GRACE_PERIOD
        ) && (expiresAtEpochMs == null || expiresAtEpochMs > System.currentTimeMillis())

    companion object {
        val FREE_DEFAULT = SubscriptionInfo(
            tier = SubscriptionTier.FREE,
            status = SubscriptionStatus.NONE,
            expiresAtEpochMs = null,
            trialStartedAtEpochMs = null,
            autoRenew = false,
            gateway = PaymentGateway.NONE
        )

        /**
         * Cria o plano de teste gratuito com 7 dias de acesso Pro completo.
         */
        fun createTrial(durationDays: Long = 7): SubscriptionInfo {
            val now = System.currentTimeMillis()
            val expires = now + (durationDays * 24 * 60 * 60 * 1000L)
            return SubscriptionInfo(
                tier = SubscriptionTier.PRO_MONTHLY,
                status = SubscriptionStatus.TRIALING,
                expiresAtEpochMs = expires,
                trialStartedAtEpochMs = now,
                autoRenew = false,
                gateway = PaymentGateway.NONE
            )
        }
    }
}

enum class FeatureKey(val title: String, val description: String) {
    UNLIMITED_EVALUATIONS("Avaliações Ilimitadas de Corridas", "Avalie quantas corridas quiser sem limite diário."),
    FLOATING_HUD("HUD Flutuante Dinâmico", "Card flutuante translúcido diretamente sobre o Uber e 99."),
    TTS_AUDIO_COPILOT("Copiloto por Voz (TTS)", "Anúncio por voz imediato se a corrida vale a pena ou deve ser evitada."),
    DEADHEAD_PREDICTOR("Preditor de Retorno Vazio (Deadhead)", "Previsão de probabilidade de volta vazia e lucro líquido real ajustado."),
    PLATFORM_COMPARISON("Comparativo Avançado Uber vs 99", "Descubra qual aplicativo rende mais por hora e por km."),
    TAX_EXPORT_REPORT("Exportação Fiscal e Contábil", "Exportação de relatórios em CSV e JSON para IRPF e livro caixa."),
    PRIORITY_SUPPORT("Suporte Prioritário", "Atendimento prioritário via canal direto para assinantes Pro.")
}

sealed class FeatureAccessResult {
    data class Granted(val tier: SubscriptionTier) : FeatureAccessResult()
    data class Denied(
        val reason: String,
        val requiredTier: SubscriptionTier = SubscriptionTier.PRO_MONTHLY,
        val currentUsage: Int? = null,
        val usageLimit: Int? = null
    ) : FeatureAccessResult()

    val isGranted: Boolean get() = this is Granted
}

data class PixPaymentOrder(
    val orderId: String,
    val planTier: SubscriptionTier,
    val amountCents: Int,
    val amountReais: Double,
    val pixKey: String,
    val pixCopiaECola: String,
    val qrCodeEmv: String,
    val expiresAtEpochMs: Long,
    val status: String = "PENDING" // PENDING, PAID, EXPIRED
) {
    val isExpired: Boolean get() = System.currentTimeMillis() > expiresAtEpochMs
}
