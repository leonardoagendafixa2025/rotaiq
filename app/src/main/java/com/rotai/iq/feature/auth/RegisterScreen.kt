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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.rotai.iq.core.ui.theme.RotaExcellent
import com.rotai.iq.core.ui.theme.RotaOrangeLight
import com.rotai.iq.core.ui.theme.RotaOrangePrimary
import com.rotai.iq.core.ui.theme.RotaTextPrimary
import com.rotai.iq.core.ui.theme.RotaTextSecondary
import com.rotai.iq.core.ui.theme.RotaTextWhite
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateBackToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.registerState.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(viewModel) {
        viewModel.navEvents.collectLatest { event ->
            when (event) {
                is AuthNavEvent.NavigateToHome -> onNavigateToHome()
                is AuthNavEvent.NavigateToLogin -> onNavigateBackToLogin()
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
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Botão Voltar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBackToLogin) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = RotaTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Voltar ao Login",
                    color = RotaTextSecondary,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Título
            Text(
                text = "CRIAR SUA CONTA",
                color = RotaTextWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.0.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Cadastre-se para desbloquear todo o poder do ROTA IQ",
                color = RotaTextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Card Formulário
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
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = RotaAvoid, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = state.errorMessage ?: "", color = RotaAvoid, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Mensagem de Sucesso (Ex: E-mail enviado)
                    if (state.successMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x2200E676), shape = RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RotaExcellent, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = state.successMessage ?: "", color = RotaExcellent, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Nome Completo
                    Text(text = "NOME COMPLETO", color = RotaTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.0.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = { viewModel.onRegisterNameChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Seu Nome", color = Color(0xFF555555), fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = RotaTextSecondary, modifier = Modifier.size(20.dp)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // E-mail
                    Text(text = "E-MAIL", color = RotaTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.0.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = { viewModel.onRegisterEmailChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("seu.email@exemplo.com", color = Color(0xFF555555), fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = RotaTextSecondary, modifier = Modifier.size(20.dp)) },
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Senha (mínimo 8 caracteres)
                    Text(text = "SENHA (MÍNIMO 8 CARACTERES)", color = RotaTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.0.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { viewModel.onRegisterPasswordChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Mínimo 8 caracteres", color = Color(0xFF555555), fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = RotaTextSecondary, modifier = Modifier.size(20.dp)) },
                        trailingIcon = {
                            IconButton(onClick = { viewModel.toggleRegisterPasswordVisibility() }) {
                                Icon(
                                    imageVector = if (state.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = RotaTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Confirmar Senha
                    Text(text = "CONFIRMAR SENHA", color = RotaTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.0.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = state.confirmPassword,
                        onValueChange = { viewModel.onRegisterConfirmPasswordChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Repita a senha", color = Color(0xFF555555), fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = RotaTextSecondary, modifier = Modifier.size(20.dp)) },
                        visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            viewModel.executeRegister()
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Checkbox de Termos e Privacidade
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onTermsAcceptedChanged(!state.termsAccepted) }
                    ) {
                        Checkbox(
                            checked = state.termsAccepted,
                            onCheckedChange = { viewModel.onTermsAcceptedChanged(it) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = RotaOrangePrimary,
                                uncheckedColor = RotaBorderMedium,
                                checkmarkColor = Color(0xFF0A0A0A)
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Li e concordo com os Termos de Uso e Política de Privacidade.",
                            color = RotaTextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botão CRIAR CONTA
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
                            text = "CRIAR CONTA",
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.executeRegister()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            variant = RotaButtonVariant.PRIMARY_ORANGE
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Link Voltar ao Login
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "Já possui uma conta? ", color = RotaTextSecondary, fontSize = 14.sp)
                Text(
                    text = "Entrar",
                    color = RotaOrangePrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateBackToLogin() }
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
