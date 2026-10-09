package com.rotai.iq.feature.support

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rotai.iq.core.ui.designsystem.RotaButton
import com.rotai.iq.core.ui.designsystem.RotaButtonVariant
import com.rotai.iq.core.ui.designsystem.RotaCard
import com.rotai.iq.core.ui.theme.CardShapeDefault
import com.rotai.iq.core.ui.theme.RotaBlack
import com.rotai.iq.core.ui.theme.RotaBorderSubtle
import com.rotai.iq.core.ui.theme.RotaCardBackground
import com.rotai.iq.core.ui.theme.RotaCardElevated
import com.rotai.iq.core.ui.theme.RotaDarkCanvas
import com.rotai.iq.core.ui.theme.RotaExcellent
import com.rotai.iq.core.ui.theme.RotaOrangePrimary
import com.rotai.iq.core.ui.theme.RotaOrangeSubtleBg
import com.rotai.iq.core.ui.theme.RotaTextPrimary
import com.rotai.iq.core.ui.theme.RotaTextSecondary
import com.rotai.iq.core.ui.theme.RotaTextTertiary
import com.rotai.iq.core.ui.theme.RotaTextWhite

data class FaqItem(
    val id: Int,
    val question: String,
    val answer: String
)

private val FAQ_ITEMS = listOf(
    FaqItem(
        id = 1,
        question = "Como o ROTA IQ calcula o custo real por quilômetro?",
        answer = "O cálculo combina o custo de combustível (preço do litro dividido pelo consumo do veículo em km/l) somado aos custos fixos proporcionais (seguro, IPVA, licenciamento e depreciação) e manutenção preventiva estimada."
    ),
    FaqItem(
        id = 2,
        question = "Como ativar o Copiloto e o HUD Flutuante sobre o Uber/99?",
        answer = "Acesse a aba 'Copiloto' e conceda a permissão de 'Sobreposição a outros apps' e 'Serviço de Acessibilidade'. O HUD aparecerá automaticamente na tela sempre que uma nova corrida for ofertada na plataforma."
    ),
    FaqItem(
        id = 3,
        question = "Como funciona a assinatura do Plano Pro via Pix?",
        answer = "Na aba 'Plano Pro', selecione o plano desejado e gere o QR Code Pix. O pagamento é confirmado instantaneamente via webhook e seus recursos ilimitados são liberados no mesmo segundo no aplicativo."
    ),
    FaqItem(
        id = 4,
        question = "Meus dados de corridas e ganhos são confidenciais?",
        answer = "Sim! Seguimos rigorosamente a LGPD (Lei 13.709/2018). Seus dados pessoais sensíveis são sanitizados no dispositivo com criptografia AES-256 e você pode solicitar exportação ou esquecimento a qualquer momento na aba Privacidade."
    )
)

@Composable
fun SupportScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val expandedItems = remember { mutableStateMapOf<Int, Boolean>() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RotaBlack)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Topo com Voltar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = RotaTextWhite
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "SUPORTE & AJUDA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = RotaOrangePrimary,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Central de Atendimento",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = RotaTextWhite
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Canais de Contato Direto
        Text(
            text = "FALE COM NOSSA EQUIPE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = RotaTextSecondary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ContactChannelCard(
                title = "WhatsApp Oficial",
                subtitle = "Resposta rápida",
                icon = Icons.Default.Chat,
                accentColor = RotaExcellent,
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://wa.me/5511999998888?text=Olá,%20preciso%20de%20ajuda%20com%20o%20ROTA%20IQ")
                    )
                    context.startActivity(intent)
                },
                modifier = Modifier.weight(1f)
            )

            ContactChannelCard(
                title = "E-mail Suporte",
                subtitle = "suporte@rotai.app",
                icon = Icons.Default.Email,
                accentColor = RotaOrangePrimary,
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_SENDTO,
                        Uri.parse("mailto:suporte@rotai.app?subject=Suporte%20ROTA%20IQ")
                    )
                    context.startActivity(intent)
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Seção FAQ (Perguntas Frequentes)
        Text(
            text = "DÚVIDAS FREQUENTES (FAQ)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = RotaTextSecondary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FAQ_ITEMS.forEach { item ->
                val isExpanded = expandedItems[item.id] ?: false
                FaqAccordionCard(
                    item = item,
                    isExpanded = isExpanded,
                    onToggle = { expandedItems[item.id] = !isExpanded }
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Card de Versão e Diagnóstico
        RotaCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = RotaCardBackground,
            borderColor = RotaBorderSubtle,
            shape = CardShapeDefault
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(RotaDarkCanvas),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = RotaOrangePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "ROTA IQ Android",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = RotaTextWhite
                    )
                    Text(
                        text = "Versão 1.0.0 (Build 9 Oficial • Produção)",
                        fontSize = 11.sp,
                        color = RotaTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ContactChannelCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    RotaCard(
        modifier = modifier,
        backgroundColor = RotaCardBackground,
        borderColor = RotaBorderSubtle,
        onClick = onClick,
        shape = CardShapeDefault
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = RotaTextWhite
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = RotaTextTertiary
            )
        }
    }
}

@Composable
private fun FaqAccordionCard(
    item: FaqItem,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    RotaCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = RotaCardBackground,
        borderColor = if (isExpanded) RotaOrangePrimary.copy(alpha = 0.5f) else RotaBorderSubtle,
        onClick = onToggle,
        shape = CardShapeDefault
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.question,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isExpanded) RotaOrangePrimary else RotaTextWhite,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = if (isExpanded) RotaOrangePrimary else RotaTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = RotaBorderSubtle, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = item.answer,
                        fontSize = 12.sp,
                        color = RotaTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
