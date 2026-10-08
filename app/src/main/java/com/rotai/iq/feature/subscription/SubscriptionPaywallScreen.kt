package com.rotai.iq.feature.subscription

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rotai.iq.core.domain.model.SubscriptionInfo
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
                    Text("Restaurar Compras", color = Color(0xFF00E5FF), fontSize = 13.sp)
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
                            listOf(Color(0xFFFFD700), Color(0xFFFF9100))
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
                color = Color(0xFFFFD700)
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
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFB71C1C)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = err,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Botões de Ação de Pagamento
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.initiateGooglePlayPurchase() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                enabled = !uiState.isProcessing
            ) {
                if (uiState.isProcessing) {
                    CircularProgressIndicator(color = Color(0xFF0A0E17), modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "ASSINAR COM GOOGLE PLAY",
                        color = Color(0xFF0A0E17),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { viewModel.generatePixOrder() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFF00B0FF)))
                ),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E5FF))
            ) {
                Text(
                    text = "PAGAR COM PIX INSTANTÂNEO",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Matriz Comparativa Grátis vs Pro
            ComparisonMatrixCard()

            Spacer(modifier = Modifier.height(24.dp))

            // Termos e Segurança
            Text(
                text = "Cobrança segura intermediada por Google Play ou Banco Central (PIX). Cancele a qualquer momento nas configurações da sua conta Google Play sem multas.",
                fontSize = 11.sp,
                color = Color(0xFF546E7A),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))
        }

        // Modal PIX Copia e Cola
        uiState.activePixOrder?.let { order ->
            AlertDialog(
                onDismissRequest = { viewModel.dismissPixOrder() },
                containerColor = Color(0xFF121826),
                title = {
                    Text("Pagamento PIX Oficial", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
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
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF0A0E17))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("COPIAR CÓDIGO PIX", color = Color(0xFF0A0E17), fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { viewModel.confirmPixPayment() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("JÁ PAGUEI NO MEU BANCO — ATIVAR PRO", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissPixOrder() }) {
                        Text("Fechar", color = Color(0xFF90A4AE))
                    }
                }
            )
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
            .padding(vertical = 6.dp)
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
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF78909C)
                )
                Text(
                    text = subscription.tier.displayName.uppercase(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (subscription.isProActive) Color(0xFF00E676) else Color.White
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (subscription.isProActive) Color(0xFF00E676) else Color(0xFF37474F))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (subscription.isProActive) "ATIVO" else "BÁSICO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (subscription.isProActive) Color(0xFF0A0E17) else Color(0xFFB0BEC5)
                )
            }
        }
    }
}

@Composable
private fun PlanOptionCard(
    plan: com.rotai.iq.core.domain.model.SubscriptionPlan,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFFFFD700) else Color(0xFF263238)
    val containerColor = if (isSelected) Color(0xFF1A1F2C) else Color(0xFF121826)

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = plan.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = plan.headline,
                        fontSize = 12.sp,
                        color = Color(0xFF90A4AE)
                    )
                }

                plan.savingsBadge?.let { badge ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE65100))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = plan.formattedPrice,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isSelected) Color(0xFFFFD700) else Color(0xFF00E676)
                )
                Text(
                    text = " ${plan.periodSuffix}",
                    fontSize = 12.sp,
                    color = Color(0xFF90A4AE),
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }

            Divider(
                color = Color(0xFF1E2838),
                modifier = Modifier.padding(vertical = 10.dp)
            )

            plan.highlights.forEach { highlight ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = highlight,
                        fontSize = 12.sp,
                        color = Color(0xFFECEFF1)
                    )
                }
            }
        }
    }
}

@Composable
private fun ComparisonMatrixCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121826)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "COMPARATIVO DE RECURSOS",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF90A4AE)
            )

            Spacer(modifier = Modifier.height(10.dp))

            MatrixRow("Avaliações por dia", "15/dia", "Ilimitadas", isProOnly = false)
            MatrixRow("HUD Flutuante Dinâmico", "Não", "Sim", isProOnly = true)
            MatrixRow("Copiloto por Voz (TTS)", "Não", "Sim", isProOnly = true)
            MatrixRow("Risco Deadhead (Volta Vazia)", "Não", "Sim", isProOnly = true)
            MatrixRow("Comparativo Uber vs 99", "Não", "Sim", isProOnly = true)
            MatrixRow("Exportação IRPF / Fiscal", "Não", "Sim", isProOnly = true)
            MatrixRow("Suporte Prioritário VIP", "Não", "Sim", isProOnly = true)
        }
    }
}

@Composable
private fun MatrixRow(feature: String, free: String, pro: String, isProOnly: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = feature, fontSize = 12.sp, color = Color(0xFFCFD8DC), modifier = Modifier.weight(1.5f))
        Text(
            text = free,
            fontSize = 11.sp,
            color = Color(0xFF78909C),
            modifier = Modifier.weight(0.8f),
            textAlign = TextAlign.Center
        )
        Text(
            text = pro,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isProOnly) Color(0xFFFFD700) else Color(0xFF00E676),
            modifier = Modifier.weight(0.9f),
            textAlign = TextAlign.End
        )
    }
}
