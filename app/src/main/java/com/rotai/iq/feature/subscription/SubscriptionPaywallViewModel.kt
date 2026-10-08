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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SubscriptionPaywallUiState(
    val selectedTier: SubscriptionTier = SubscriptionTier.PRO_ANNUAL,
    val showPaymentMethodSelector: Boolean = false,
    val activePixOrder: PixPaymentOrder? = null,
    val isProcessing: Boolean = false,
    val isCheckingPayment: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val plans: List<SubscriptionPlan> = listOf(
        SubscriptionPlan(
            tier = SubscriptionTier.PRO_MONTHLY,
            title = "Pro Mensal",
            headline = "7 Dias Grátis • Cancele quando quiser",
            formattedPrice = "R$ 29,90",
            periodSuffix = "/mês (após 7 dias de teste)",
            savingsBadge = "7 DIAS GRÁTIS",
            highlights = listOf(
                "7 dias de teste grátis com acesso ilimitado",
                "Após o teste: apenas R$ 29,90/mês",
                "Avaliações ilimitadas de ofertas",
                "HUD Flutuante translúcido ao volante",
                "Copiloto inteligente por voz (TTS)",
                "Preditor de retorno vazio (Deadhead)"
            )
        ),
        SubscriptionPlan(
            tier = SubscriptionTier.PRO_ANNUAL,
            title = "Pro Anual",
            headline = "Melhor Custo-Benefício (33% OFF)",
            formattedPrice = "R$ 19,99",
            periodSuffix = "/mês (R$ 239,90/ano)",
            savingsBadge = "33% OFF • Economize R$ 118,90",
            highlights = listOf(
                "Tudo do plano Pro com desconto máximo",
                "Economia equivalente a 4 meses grátis",
                "Acesso prioritário a novas IAs de rota",
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

    /**
     * Abre a seleção obrigatória do plano de pagamento (o motorista não atualiza apenas no clique).
     */
    fun openPaymentSelection() {
        _uiState.value = _uiState.value.copy(
            showPaymentMethodSelector = true,
            errorMessage = null,
            successMessage = null
        )
    }

    fun dismissPaymentSelection() {
        _uiState.value = _uiState.value.copy(showPaymentMethodSelector = false)
    }

    /**
     * Opção 1: Pagamento via PIX Instantâneo Oficial
     */
    fun choosePixPayment() {
        val tier = _uiState.value.selectedTier
        val order = pixPaymentManager.createPixOrder(tier)
        _uiState.value = _uiState.value.copy(
            showPaymentMethodSelector = false,
            activePixOrder = order,
            errorMessage = null
        )
        telemetryManager.recordEvent(
            TelemetryManager.EVENT_PIX_GENERATED,
            mapOf("order_id" to order.orderId, "amount" to order.amountReais.toString())
        )
    }

    /**
     * Opção 2: Cartão de Crédito / Google Play
     */
    fun chooseCardPayment() {
        _uiState.value = _uiState.value.copy(
            showPaymentMethodSelector = false,
            errorMessage = "O pagamento por Cartão via Google Play está disponível apenas para downloads diretos da Play Store. Para ativar sua conta agora, utilize a opção PIX Instantâneo."
        )
    }

    /**
     * Opção 3: Boleto Bancário
     */
    fun chooseBoletoPayment() {
        _uiState.value = _uiState.value.copy(
            showPaymentMethodSelector = false,
            errorMessage = "O boleto bancário leva até 2 dias úteis para compensação. Recomendamos o PIX Instantâneo para ativação imediata em 30 segundos."
        )
    }

    /**
     * Verifica se o pagamento PIX foi detectado no banco. Não ativa de graça.
     */
    fun checkPixPaymentStatus() {
        val currentOrder = _uiState.value.activePixOrder ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingPayment = true, errorMessage = null)
            delay(1500) // Simulação de checagem bancária
            _uiState.value = _uiState.value.copy(
                isCheckingPayment = false,
                errorMessage = "Pagamento ainda não localizado no banco para o pedido ${currentOrder.orderId}. Conclua a transferência via PIX no seu aplicativo bancário e clique em verificar novamente."
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
