package com.rotai.iq.feature.auth

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rotai.iq.core.ui.designsystem.RotaButton
import com.rotai.iq.core.ui.designsystem.RotaButtonVariant
import com.rotai.iq.core.ui.designsystem.RotaCard
import com.rotai.iq.core.ui.theme.RotaAvoid
import com.rotai.iq.core.ui.theme.RotaBorderSubtle
import com.rotai.iq.core.ui.theme.RotaCardElevated
import com.rotai.iq.core.ui.theme.RotaDarkCanvas
import com.rotai.iq.core.ui.theme.RotaExcellent
import com.rotai.iq.core.ui.theme.RotaOrangeHover
import com.rotai.iq.core.ui.theme.RotaOrangeLight
import com.rotai.iq.core.ui.theme.RotaOrangePrimary
import com.rotai.iq.core.ui.theme.RotaTextPrimary
import com.rotai.iq.core.ui.theme.RotaTextSecondary
import com.rotai.iq.core.ui.theme.RotaTextWhite

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToSupport: () -> Unit = {},
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cachedProfile = remember { authViewModel.getCachedProfile() }
    val fullName = cachedProfile?.fullName?.ifBlank { "Motorista ROTA IQ" } ?: "Motorista ROTA IQ"
    val email = cachedProfile?.email?.ifBlank { "motorista@rotai.app" } ?: "motorista@rotai.app"
    val isVerified = cachedProfile?.emailVerified ?: false
    val scrollState = rememberScrollState()

    var isResendingEmail by remember { mutableStateOf(false) }
    var emailFeedbackMessage by remember { mutableStateOf<String?>(null) }
    var isFeedbackError by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        RotaDarkCanvas,
                        Color(0xFF0F1117),
                        Color(0xFF090A0E)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Barra superior com Voltar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(RotaCardElevated)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Voltar",
                        tint = RotaTextWhite
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "Meu Perfil",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = RotaTextWhite
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Card Principal do Usuário
            RotaCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar com Iniciais
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(RotaOrangePrimary, RotaOrangeHover)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = fullName.take(1).uppercase(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = fullName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = RotaTextWhite
                    )
                    Text(
                        text = email,
                        fontSize = 13.sp,
                        color = RotaTextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Badge de status do e-mail
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isVerified) RotaExcellent.copy(alpha = 0.15f)
                                else Color(0xFFFFB300).copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isVerified) RotaExcellent else Color(0xFFFFB300),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isVerified) "E-mail Verificado" else "E-mail Pendente de Verificação",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isVerified) RotaExcellent else Color(0xFFFFB300)
                        )
                    }
                }
            }

            // Banner para Reenviar Verificação (se pendente)
            if (!isVerified) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF262013))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MarkEmailRead,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Confirme seu e-mail",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFE082)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Enviamos um link para $email. Clique nele para manter sua conta segura e receber relatórios de fechamento.",
                            fontSize = 12.sp,
                            color = RotaTextSecondary,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        RotaButton(
                            text = if (isResendingEmail) "Enviando..." else "Reenviar link de confirmação",
                            onClick = {
                                isResendingEmail = true
                                authViewModel.resendVerification(email) { success, msg ->
                                    isResendingEmail = false
                                    isFeedbackError = !success
                                    emailFeedbackMessage = msg
                                }
                            },
                            variant = RotaButtonVariant.OUTLINE,
                            enabled = !isResendingEmail,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (emailFeedbackMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = emailFeedbackMessage!!,
                                fontSize = 12.sp,
                                color = if (isFeedbackError) RotaAvoid else RotaExcellent
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Seção de Segurança & Ações da Conta
            Text(
                text = "CONTA E SEGURANÇA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = RotaTextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            ProfileMenuItem(
                icon = Icons.Default.Lock,
                title = "Alterar Senha de Acesso",
                subtitle = "Atualize sua credencial de segurança",
                onClick = onNavigateToChangePassword
            )

            Spacer(modifier = Modifier.height(8.dp))

            ProfileMenuItem(
                icon = Icons.Default.Security,
                title = "Privacidade e Termos de Uso",
                subtitle = "Gerenciamento de dados e conformidade LGPD",
                onClick = onNavigateToPrivacy
            )

            Spacer(modifier = Modifier.height(8.dp))

            ProfileMenuItem(
                icon = Icons.Default.Info,
                title = "Central de Ajuda & Suporte",
                subtitle = "WhatsApp oficial, e-mail e perguntas frequentes",
                onClick = onNavigateToSupport
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botão de Logout Seguro
            RotaButton(
                text = "Sair da Conta",
                onClick = onLogout,
                variant = RotaButtonVariant.OUTLINE,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RotaCardElevated)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(RotaOrangePrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = RotaOrangePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RotaTextWhite
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = RotaTextSecondary
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = RotaTextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
