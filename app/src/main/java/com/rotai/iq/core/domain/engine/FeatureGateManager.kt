package com.rotai.iq.core.domain.engine

import com.rotai.iq.core.domain.model.FeatureAccessResult
import com.rotai.iq.core.domain.model.FeatureKey
import com.rotai.iq.core.domain.model.SubscriptionInfo
import com.rotai.iq.core.domain.model.SubscriptionTier

class FeatureGateManager {

    companion object {
        const val FREE_DAILY_EVALUATION_LIMIT = 15
    }

    /**
     * Avalia se o motorista pode acessar uma determinada funcionalidade com base em sua assinatura
     * ativa e no consumo diário de avaliações.
     */
    fun checkAccess(
        feature: FeatureKey,
        subscription: SubscriptionInfo,
        dailyUsageCount: Int = 0
    ): FeatureAccessResult {
        // Se a assinatura Pro estiver ativa (incluindo o período de 7 dias grátis), todas as funcionalidades estão liberadas sem limites
        if (subscription.isProActive) {
            return FeatureAccessResult.Granted(subscription.tier)
        }

        // Se o período de degustação de 7 dias já expirou:
        if (subscription.isTrialExpired) {
            return FeatureAccessResult.Denied(
                reason = "Seu período de teste grátis de 7 dias expirou. Ative o plano mensal por R$ 29,90/mês para continuar utilizando o ROTA IQ.",
                requiredTier = SubscriptionTier.PRO_MONTHLY,
                currentUsage = dailyUsageCount,
                usageLimit = 0
            )
        }

        // Regras para plano FREE (fallback)
        return when (feature) {
            FeatureKey.UNLIMITED_EVALUATIONS -> {
                if (dailyUsageCount < FREE_DAILY_EVALUATION_LIMIT) {
                    FeatureAccessResult.Granted(SubscriptionTier.FREE)
                } else {
                    FeatureAccessResult.Denied(
                        reason = "Você atingiu o limite de $FREE_DAILY_EVALUATION_LIMIT avaliações por dia. Assine o ROTA IQ Pro para avaliações ilimitadas sem interrupções.",
                        requiredTier = SubscriptionTier.PRO_MONTHLY,
                        currentUsage = dailyUsageCount,
                        usageLimit = FREE_DAILY_EVALUATION_LIMIT
                    )
                }
            }

            FeatureKey.FLOATING_HUD -> {
                FeatureAccessResult.Denied(
                    reason = "O HUD Flutuante em tempo real sobre Uber e 99 é exclusivo para assinantes ROTA IQ Pro.",
                    requiredTier = SubscriptionTier.PRO_MONTHLY
                )
            }

            FeatureKey.TTS_AUDIO_COPILOT -> {
                FeatureAccessResult.Denied(
                    reason = "Os alertas por áudio e voz em tempo real são exclusivos para assinantes ROTA IQ Pro.",
                    requiredTier = SubscriptionTier.PRO_MONTHLY
                )
            }

            FeatureKey.DEADHEAD_PREDICTOR -> {
                FeatureAccessResult.Denied(
                    reason = "A previsão preditiva de retorno vazio (Deadhead) e desconto de custo de volta é exclusiva do ROTA IQ Pro.",
                    requiredTier = SubscriptionTier.PRO_MONTHLY
                )
            }

            FeatureKey.PLATFORM_COMPARISON -> {
                FeatureAccessResult.Denied(
                    reason = "O comparativo estratégico de rentabilidade entre Uber, 99 e inDrive é exclusivo do ROTA IQ Pro.",
                    requiredTier = SubscriptionTier.PRO_MONTHLY
                )
            }

            FeatureKey.TAX_EXPORT_REPORT -> {
                FeatureAccessResult.Denied(
                    reason = "A exportação contábil para IRPF e livro caixa em formato fiscal é exclusiva do ROTA IQ Pro.",
                    requiredTier = SubscriptionTier.PRO_MONTHLY
                )
            }

            FeatureKey.PRIORITY_SUPPORT -> {
                FeatureAccessResult.Denied(
                    reason = "O canal direto de atendimento VIP prioritário é exclusivo de assinantes Pro.",
                    requiredTier = SubscriptionTier.PRO_ANNUAL
                )
            }
        }
    }

    /**
     * Atalho para verificar se uma oferta pode ser avaliada agora.
     */
    fun canEvaluateOffer(
        subscription: SubscriptionInfo,
        dailyUsageCount: Int
    ): FeatureAccessResult {
        return checkAccess(FeatureKey.UNLIMITED_EVALUATIONS, subscription, dailyUsageCount)
    }

    /**
     * Retorna a quantidade de avaliações restantes no dia para o motorista.
     */
    fun remainingEvaluationsToday(
        subscription: SubscriptionInfo,
        dailyUsageCount: Int
    ): Int {
        if (subscription.isProActive) return Int.MAX_VALUE
        if (subscription.isTrialExpired) return 0
        val remaining = FREE_DAILY_EVALUATION_LIMIT - dailyUsageCount
        return if (remaining > 0) remaining else 0
    }

    /**
     * Retorna mensagem explicativa e persuasiva de upgrade para a funcionalidade.
     */
    fun getUpgradePitch(feature: FeatureKey): String {
        return when (feature) {
            FeatureKey.UNLIMITED_EVALUATIONS -> "Desbloqueie avaliações ilimitadas e nunca mais perca uma corrida lucrativa!"
            FeatureKey.FLOATING_HUD -> "Veja se a corrida compensa em 1 segundo sem sair do Uber ou 99 com o HUD translúcido."
            FeatureKey.TTS_AUDIO_COPILOT -> "Dirija com segurança total. Ouça no alto-falante se a corrida vale a pena sem tirar os olhos da pista."
            FeatureKey.DEADHEAD_PREDICTOR -> "Evite armadilhas de corridas que te levam para o vazio e devolvem todo o seu lucro em gasolina de retorno."
            FeatureKey.PLATFORM_COMPARISON -> "Descubra qual app está te pagando mais por hora nesta semana e foque seu esforço."
            FeatureKey.TAX_EXPORT_REPORT -> "Economize horas no seu imposto de renda e controle de frotas com relatórios organizados em 1 clique."
            FeatureKey.PRIORITY_SUPPORT -> "Atendimento VIP rápido diretamente com a equipe de engenharia do ROTA IQ."
        }
    }
}
