package com.rotai.iq.feature.subscription

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rotai.iq.core.domain.model.SubscriptionInfo
import com.rotai.iq.core.domain.model.SubscriptionPlan
import com.rotai.iq.core.domain.model.SubscriptionTier

@Composable
fun SubscriptionPaywallScreen(
    viewModel: SubscriptionPaywallViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val subscriptionInfo by viewModel.subscriptionInfo.collectAsState()
    val context = LocalContext.current

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E17))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Topo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onNavigateBack) {
                    Text("← Voltar", color = Color(0xFF90A4AE), fontSize = 14.sp)
                }
                TextButton(onClick = { viewModel.restorePurchases() }) {
                    Text("Restaurar Compras", color = Color(0xFFFF7A00), fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ícone Coroa / Estrela Pro
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFFF9533), Color(0xFFFF7A00))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Pro Crown",
                    tint = Color(0xFF0A0E17),
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "ROTA IQ PRO",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
                color = Color(0xFFFF7A00)
            )

            Text(
                text = "Tome decisões com precisão cirúrgica ao volante e maximize seu faturamento líquido real.",
                fontSize = 14.sp,
                color = Color(0xFFB0BEC5),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Status da assinatura atual
            CurrentSubscriptionStatusCard(subscription = subscriptionInfo)

            Spacer(modifier = Modifier.height(16.dp))

            // Cards de Seleção de Plano
            uiState.plans.forEach { plan ->
                val isSelected = uiState.selectedTier == plan.tier
                PlanOptionCard(
                    plan = plan,
                    isSelected = isSelected,
                    onClick = { viewModel.selectTier(plan.tier) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Feedback de Sucesso ou Erro
            uiState.successMessage?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = msg,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            uiState.errorMessage?.let { err ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4A1212)),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFFF334B).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = err,
                        color = Color(0xFFFF8A80),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Botão Principal: Não atualiza diretamente no clique; direciona para escolher o plano de pagamento
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.openPaymentSelection() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A00)),
                enabled = !uiState.isProcessing
            ) {
                if (uiState.isProcessing) {
                    CircularProgressIndicator(color = Color(0xFF0A0E17), modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "ESCOLHER FORMA DE PAGAMENTO →",
                        color = Color(0xFF0A0E17),
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Matriz Comparativa Grátis vs Pro
            ComparisonMatrixCard()

            Spacer(modifier = Modifier.height(24.dp))

            // Termos e Segurança
            Text(
                text = "Pagamento intermediado com segurança pelo Banco Central do Brasil (PIX) e gateways oficiais. Cancele quando quiser sem multas.",
                fontSize = 11.sp,
                color = Color(0xFF546E7A),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))
        }

        // ======================================================================
        // MODAL 1: SELEÇÃO DA FORMA DE PAGAMENTO (OBRIGATÓRIO ESCOLHER O PAGAMENTO)
        // ======================================================================
        if (uiState.showPaymentMethodSelector) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissPaymentSelection() },
                containerColor = Color(0xFF141418),
                title = {
                    Column {
                        Text(
                            text = "Forma de Pagamento",
                            color = Color(0xFFFF7A00),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Plano selecionado: ${uiState.selectedTier.displayName} • R$ ${String.format(java.util.Locale.US, "%.2f", uiState.selectedTier.fullPrice)}",
                            color = Color(0xFFB0BEC5),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                text = {
                    Column {
                        // Opção 1: PIX Instantâneo Oficial
                        PaymentMethodItem(
                            icon = Icons.Default.CheckCircle,
                            iconColor = Color(0xFF00E676),
                            title = "PIX Instantâneo Oficial",
                            subtitle = "QR Code & Copia e Cola • Liberação automática",
                            badge = "RECOMENDADO",
                            badgeColor = Color(0xFF00E676),
                            onClick = { viewModel.choosePixPayment() }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Opção 2: Cartão de Crédito
                        PaymentMethodItem(
                            icon = Icons.Default.Lock,
                            iconColor = Color(0xFF29B6F6),
                            title = "Cartão de Crédito / Google Play",
                            subtitle = "Assinatura recorrente mensal ou anual",
                            badge = null,
                            badgeColor = Color.Transparent,
                            onClick = { viewModel.chooseCardPayment() }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Opção 3: Boleto Bancário
                        PaymentMethodItem(
                            icon = Icons.Default.AccountBalanceWallet,
                            iconColor = Color(0xFFFFB300),
                            title = "Boleto Bancário",
                            subtitle = "Compensação em até 2 dias úteis",
                            badge = null,
                            badgeColor = Color.Transparent,
                            onClick = { viewModel.chooseBoletoPayment() }
                        )
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissPaymentSelection() }) {
                        Text("Fechar", color = Color(0xFF90A4AE))
                    }
                }
            )
        }

        // ======================================================================
        // MODAL 2: CHECKOUT PIX (CHAVE REAL + VERIFICAÇÃO NO BANCO)
        // ======================================================================
        uiState.activePixOrder?.let { order ->
            AlertDialog(
                onDismissRequest = { viewModel.dismissPixOrder() },
                containerColor = Color(0xFF141418),
                title = {
                    Text("Pagamento PIX Oficial", color = Color(0xFFFF7A00), fontWeight = FontWeight.Bold)
                },
                text = {
                    Column {
                        Text(
                            text = "Plano: ${order.planTier.displayName}",
                            color = Color(0xFFECEFF1),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Valor: R$ ${String.format(java.util.Locale.US, "%.2f", order.amountReais)}",
                            color = Color(0xFF00E676),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Text(
                            text = "Copie o código abaixo e cole no seu aplicativo bancário na opção 'PIX Copia e Cola':",
                            color = Color(0xFFB0BEC5),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        Surface(
                            color = Color(0xFF0A0E17),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF263238)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Text(
                                text = order.pixCopiaECola,
                                color = Color(0xFFFFD700),
                                fontSize = 10.sp,
                                maxLines = 4,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("PIX Copia e Cola", order.pixCopiaECola)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Código PIX copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A00))
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("COPIAR CÓDIGO PIX", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { viewModel.checkPixPaymentStatus() },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, Color(0xFF00E676)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E676)),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !uiState.isCheckingPayment
                        ) {
                            if (uiState.isCheckingPayment) {
                                CircularProgressIndicator(color = Color(0xFF00E676), modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("VERIFICANDO COM O BANCO...", color = Color(0xFF00E676), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("VERIFICAR STATUS DO PAGAMENTO", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissPixOrder() }) {
                        Text("Fechar", color = Color(0xFF90A4AE))
                    }
                }
            )
        }
    }
}

@Composable
private fun PaymentMethodItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    badge: String?,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)),
        border = BorderStroke(1.dp, Color(0xFF2E2E38))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    if (badge != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(badgeColor.copy(alpha = 0.2f))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(badge, color = badgeColor, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
                Text(subtitle, color = Color(0xFF9E9E9E), fontSize = 11.sp)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Color(0xFF757575), modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun CurrentSubscriptionStatusCard(subscription: SubscriptionInfo) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (subscription.isProActive) Color(0xFF10281E) else Color(0xFF161E2E)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (subscription.isProActive) Color(0xFF00E676) else Color(0xFF263238),
                RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "SEU PLANO ATUAL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF90A4AE),
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (subscription.isProActive) subscription.tier.displayName else "Plano Gratuito",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (subscription.isProActive) Color(0xFF00E676) else Color(0xFFECEFF1)
                )
                subscription.expiresAtEpochMs?.let { exp ->
                    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                    Text(
                        text = "Válido até ${sdf.format(java.util.Date(exp))}",
                        fontSize = 11.sp,
                        color = Color(0xFF78909C)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (subscription.isProActive) Color(0xFF00E676) else Color(0xFF37474F))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (subscription.isProActive) "ATIVO" else "LIMITADO",
                    color = if (subscription.isProActive) Color(0xFF0A0E17) else Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun PlanOptionCard(
    plan: SubscriptionPlan,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFFFF7A00) else Color(0xFF1E2838)
    val bgColor = if (isSelected) Color(0xFF141924) else Color(0xFF0E131E)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = plan.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        plan.savingsBadge?.let { badge ->
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFFF7A00))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = badge,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0A0E17)
                                )
                            }
                        }
                    }
                    Text(
                        text = plan.headline,
                        fontSize = 12.sp,
                        color = Color(0xFF90A4AE)
                    )
                }

                // Radio Indicator
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .border(2.dp, if (isSelected) Color(0xFFFF7A00) else Color(0xFF546E7A), CircleShape)
                        .background(if (isSelected) Color(0xFFFF7A00) else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0A0E17))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Preço
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = plan.formattedPrice,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00E676)
                )
                Text(
                    text = " ${plan.periodSuffix}",
                    fontSize = 12.sp,
                    color = Color(0xFFB0BEC5),
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFF1E2838), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Lista de Benefícios
            plan.highlights.forEach { highlight ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = highlight,
                        fontSize = 12.sp,
                        color = Color(0xFFCFD8DC)
                    )
                }
            }
        }
    }
}

@Composable
private fun ComparisonMatrixCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0E131E)),
        border = BorderStroke(1.dp, Color(0xFF1E2838))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "COMPARATIVO DIRETO",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF90A4AE),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            ComparisonRow("Avaliações de Corridas", "15 por dia", "ILIMITADO")
            ComparisonRow("HUD Flutuante Dinâmico", "Básico", "COMPLETO")
            ComparisonRow("Copiloto por Voz (TTS)", "Bloqueado", "INCLUSO")
            ComparisonRow("Detector de Volta Vazia", "Bloqueado", "INCLUSO")
            ComparisonRow("Comparador Uber vs 99", "Apenas Uber", "TODOS OS APPS")
            ComparisonRow("Exportação Contábil IRPF", "Bloqueado", "INCLUSO")
        }
    }
}

@Composable
private fun ComparisonRow(feature: String, free: String, pro: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(feature, fontSize = 12.sp, color = Color(0xFFECEFF1), modifier = Modifier.weight(1.5f))
        Text(free, fontSize = 11.sp, color = Color(0xFF78909C), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        Text(pro, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E676), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
    }
}
