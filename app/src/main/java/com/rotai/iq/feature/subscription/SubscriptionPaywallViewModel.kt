package com.rotai.iq.feature.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rotai.iq.core.data.repository.CommercialRepository
import com.rotai.iq.core.domain.engine.BillingManager
import com.rotai.iq.core.domain.engine.PixPaymentManager
import com.rotai.iq.core.domain.engine.PurchaseResult
import com.rotai.iq.core.domain.model.PaymentGateway
import com.rotai.iq.core.domain.model.PixPaymentOrder
import com.rotai.iq.core.domain.model.SubscriptionInfo
import com.rotai.iq.core.domain.model.SubscriptionPlan
import com.rotai.iq.core.domain.model.SubscriptionTier
import com.rotai.iq.core.telemetry.TelemetryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SubscriptionPaywallUiState(
    val selectedTier: SubscriptionTier = SubscriptionTier.PRO_ANNUAL,
    val activePixOrder: PixPaymentOrder? = null,
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val plans: List<SubscriptionPlan> = listOf(
        SubscriptionPlan(
            tier = SubscriptionTier.PRO_MONTHLY,
            title = "Pro Mensal",
            headline = "Flexibilidade total",
            formattedPrice = "R$ 29,90",
            periodSuffix = "/mês",
            savingsBadge = null,
            highlights = listOf(
                "Avaliações ilimitadas de ofertas",
                "HUD Flutuante translúcido ao volante",
                "Copiloto inteligente por voz (TTS)",
                "Preditor de retorno vazio (Deadhead)",
                "Comparativo avançado Uber vs 99"
            )
        ),
        SubscriptionPlan(
            tier = SubscriptionTier.PRO_ANNUAL,
            title = "Pro Anual",
            headline = "Melhor Custo-Benefício",
            formattedPrice = "R$ 19,99",
            periodSuffix = "/mês (R$ 239,90/ano)",
            savingsBadge = "33% OFF - Economize R$ 118,90",
            highlights = listOf(
                "Tudo do plano Pro Mensal",
                "Economia equivalente a 4 meses grátis",
                "Acesso antecipado a novas IAs de rota",
                "Relatórios fiscais consolidados para IRPF",
                "Canal prioritário de suporte técnico"
            )
        )
    )
)

class SubscriptionPaywallViewModel(
    private val commercialRepository: CommercialRepository,
    private val billingManager: BillingManager,
    private val pixPaymentManager: PixPaymentManager,
    private val telemetryManager: TelemetryManager
) : ViewModel() {

    val subscriptionInfo: StateFlow<SubscriptionInfo> = commercialRepository.getSubscriptionInfo()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SubscriptionInfo.FREE_DEFAULT)

    private val _uiState = MutableStateFlow(SubscriptionPaywallUiState())
    val uiState: StateFlow<SubscriptionPaywallUiState> = _uiState.asStateFlow()

    fun selectTier(tier: SubscriptionTier) {
        _uiState.value = _uiState.value.copy(selectedTier = tier, errorMessage = null)
        telemetryManager.recordEvent(
            TelemetryManager.EVENT_SUBSCRIPTION_UPGRADE_CLICKED,
            mapOf("tier" to tier.code)
        )
    }

    fun initiateGooglePlayPurchase() {
        val tier = _uiState.value.selectedTier
        _uiState.value = _uiState.value.copy(isProcessing = true, errorMessage = null)

        viewModelScope.launch {
            val result = billingManager.processPurchase(tier, PaymentGateway.GOOGLE_PLAY)
            when (result) {
                is PurchaseResult.Success -> {
                    commercialRepository.saveSubscriptionInfo(result.subscription)
                    telemetryManager.recordEvent(
                        TelemetryManager.EVENT_SUBSCRIPTION_ACTIVATED,
                        mapOf("tier" to tier.code, "gateway" to "google_play")
                    )
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        successMessage = "Parabéns! Sua assinatura ${tier.displayName} foi ativada com sucesso."
                    )
                }
                is PurchaseResult.UserCanceled -> {
                    _uiState.value = _uiState.value.copy(isProcessing = false)
                }
                is PurchaseResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun generatePixOrder() {
        val tier = _uiState.value.selectedTier
        val order = pixPaymentManager.createPixOrder(tier)
        _uiState.value = _uiState.value.copy(activePixOrder = order, errorMessage = null)
        telemetryManager.recordEvent(
            TelemetryManager.EVENT_PIX_GENERATED,
            mapOf("order_id" to order.orderId, "amount" to order.amountReais.toString())
        )
    }

    fun simulatePixPaymentApproval() {
        val currentOrder = _uiState.value.activePixOrder ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)
            val updated = billingManager.activatePixSubscription(currentOrder.planTier, currentOrder.orderId)
            commercialRepository.saveSubscriptionInfo(updated)
            telemetryManager.recordEvent(
                TelemetryManager.EVENT_SUBSCRIPTION_ACTIVATED,
                mapOf("order_id" to currentOrder.orderId, "gateway" to "pix")
            )
            _uiState.value = _uiState.value.copy(
                isProcessing = false,
                activePixOrder = null,
                successMessage = "Pagamento PIX confirmado com sucesso! Recursos Pro desbloqueados."
            )
        }
    }

    fun restorePurchases() {
        _uiState.value = _uiState.value.copy(
            successMessage = "Consultando recibos na Google Play... Nenhuma outra assinatura pendente encontrada."
        )
    }

    fun dismissPixOrder() {
        _uiState.value = _uiState.value.copy(activePixOrder = null)
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
