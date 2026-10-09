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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rotai.iq.core.ui.designsystem.RotaButton
import com.rotai.iq.core.ui.designsystem.RotaButtonVariant
import com.rotai.iq.core.ui.designsystem.RotaCard
import com.rotai.iq.core.ui.theme.RotaAvoid
import com.rotai.iq.core.ui.theme.RotaBlack
import com.rotai.iq.core.ui.theme.RotaBorderMedium
import com.rotai.iq.core.ui.theme.RotaBorderSubtle
import com.rotai.iq.core.ui.theme.RotaCardBackground
import com.rotai.iq.core.ui.theme.RotaCardElevated
import com.rotai.iq.core.ui.theme.RotaDarkCanvas
import com.rotai.iq.core.ui.theme.RotaOrangeHover
import com.rotai.iq.core.ui.theme.RotaOrangeLight
import com.rotai.iq.core.ui.theme.RotaOrangePrimary
import com.rotai.iq.core.ui.theme.RotaTextPrimary
import com.rotai.iq.core.ui.theme.RotaTextSecondary
import com.rotai.iq.core.ui.theme.RotaTextWhite
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.loginState.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(viewModel) {
        viewModel.navEvents.collectLatest { event ->
            when (event) {
                is AuthNavEvent.NavigateToHome -> onNavigateToHome()
                is AuthNavEvent.NavigateToRegister -> onNavigateToRegister()
                is AuthNavEvent.NavigateToForgotPassword -> onNavigateToForgotPassword()
                else -> {}
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        RotaDarkCanvas,
                        RotaBlack,
                        Color(0xFF060606)
                    )
                )
            )
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Logo Compacta ROTA IQ
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF1E1E22), Color(0xFF121214))),
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = "ROTA IQ",
                    tint = RotaOrangePrimary,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Título
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ROTA",
                    color = RotaTextWhite,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .background(
                            Brush.horizontalGradient(listOf(RotaOrangeHover, RotaOrangePrimary)),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "IQ",
                        color = Color(0xFF0A0A0A),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Entre na sua conta para acessar o copiloto",
                color = RotaTextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Card Principal do Formulário
            RotaCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = RotaCardBackground,
                borderColor = RotaBorderSubtle
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Mensagem de Erro Amigável
                    if (state.errorMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x22FF334B), shape = RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = RotaAvoid,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = state.errorMessage ?: "",
                                    color = RotaAvoid,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Campo E-mail
                    Text(
                        text = "E-MAIL",
                        color = RotaTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.0.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = { viewModel.onLoginEmailChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("seu.email@exemplo.com", color = Color(0xFF555555), fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = RotaTextSecondary, modifier = Modifier.size(20.dp))
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RotaOrangePrimary,
                            unfocusedBorderColor = RotaBorderMedium,
                            focusedTextColor = RotaTextWhite,
                            unfocusedTextColor = RotaTextWhite,
                            focusedContainerColor = RotaCardElevated,
                            unfocusedContainerColor = RotaCardElevated
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Campo Senha
                    Text(
                        text = "SENHA",
                        color = RotaTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.0.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { viewModel.onLoginPasswordChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("••••••••", color = Color(0xFF555555), fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = RotaTextSecondary, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = { viewModel.toggleLoginPasswordVisibility() }) {
                                Icon(
                                    imageVector = if (state.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (state.isPasswordVisible) "Ocultar senha" else "Mostrar senha",
                                    tint = RotaTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            viewModel.executeLogin()
                        }),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RotaOrangePrimary,
                            unfocusedBorderColor = RotaBorderMedium,
                            focusedTextColor = RotaTextWhite,
                            unfocusedTextColor = RotaTextWhite,
                            focusedContainerColor = RotaCardElevated,
                            unfocusedContainerColor = RotaCardElevated
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Link Esqueci minha senha
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Esqueci minha senha",
                            color = RotaOrangeLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onNavigateToForgotPassword() }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botão ENTRAR
                    if (state.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = RotaOrangePrimary,
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 3.dp
                            )
                        }
                    } else {
                        RotaButton(
                            text = "ENTRAR",
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.executeLogin()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            variant = RotaButtonVariant.PRIMARY_ORANGE
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Link Criar Conta
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Ainda não tem conta? ",
                    color = RotaTextSecondary,
                    fontSize = 14.sp
                )
                Text(
                    text = "Criar conta",
                    color = RotaOrangePrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }

        }
    }
}
