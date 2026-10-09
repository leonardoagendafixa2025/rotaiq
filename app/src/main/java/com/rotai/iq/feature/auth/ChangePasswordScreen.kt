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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.rotai.iq.core.ui.theme.RotaOrangePrimary
import com.rotai.iq.core.ui.theme.RotaTextPrimary
import com.rotai.iq.core.ui.theme.RotaTextSecondary
import com.rotai.iq.core.ui.theme.RotaTextWhite

/**
 * Tela de Alteração de Senha Segura para usuários autenticados (P1-005).
 */
@Composable
fun ChangePasswordScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.changePasswordState.collectAsState()
    val focusManager = LocalFocusManager.current

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
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Botão Voltar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onNavigateBack() }
                    .padding(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = RotaTextSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Voltar",
                    color = RotaTextSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Título e Subtítulo
            Text(
                text = "Alterar Senha",
                color = RotaTextWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Atualize sua credencial de acesso ao ROTA IQ com segurança.",
                color = RotaTextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            RotaCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Senha Atual
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "SENHA ATUAL",
                            color = RotaTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        OutlinedTextField(
                            value = state.currentPassword,
                            onValueChange = { viewModel.onChangeCurrentPasswordChanged(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Digite sua senha atual", color = RotaTextSecondary.copy(alpha = 0.6f)) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = RotaTextSecondary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { viewModel.toggleCurrentPasswordVisibility() }) {
                                    Icon(
                                        imageVector = if (state.isCurrentPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Alternar visibilidade",
                                        tint = RotaTextSecondary
                                    )
                                }
                            },
                            visualTransformation = if (state.isCurrentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RotaOrangePrimary,
                                unfocusedBorderColor = RotaBorderMedium,
                                focusedTextColor = RotaTextWhite,
                                unfocusedTextColor = RotaTextWhite,
                                focusedContainerColor = RotaCardElevated,
                                unfocusedContainerColor = RotaCardBackground
                            )
                        )
                    }

                    // Nova Senha
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "NOVA SENHA (MÍNIMO 8 CARACTERES)",
                            color = RotaTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        OutlinedTextField(
                            value = state.newPassword,
                            onValueChange = { viewModel.onChangeNewPasswordChanged(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Nova senha", color = RotaTextSecondary.copy(alpha = 0.6f)) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = RotaTextSecondary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { viewModel.toggleNewPasswordVisibility() }) {
                                    Icon(
                                        imageVector = if (state.isNewPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Alternar visibilidade",
                                        tint = RotaTextSecondary
                                    )
                                }
                            },
                            visualTransformation = if (state.isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RotaOrangePrimary,
                                unfocusedBorderColor = RotaBorderMedium,
                                focusedTextColor = RotaTextWhite,
                                unfocusedTextColor = RotaTextWhite,
                                focusedContainerColor = RotaCardElevated,
                                unfocusedContainerColor = RotaCardBackground
                            )
                        )
                    }

                    // Confirmar Nova Senha
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "CONFIRMAR NOVA SENHA",
                            color = RotaTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        OutlinedTextField(
                            value = state.confirmPassword,
                            onValueChange = { viewModel.onChangeConfirmPasswordChanged(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Repita a nova senha", color = RotaTextSecondary.copy(alpha = 0.6f)) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = RotaTextSecondary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { viewModel.toggleConfirmPasswordVisibility() }) {
                                    Icon(
                                        imageVector = if (state.isConfirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Alternar visibilidade",
                                        tint = RotaTextSecondary
                                    )
                                }
                            },
                            visualTransformation = if (state.isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RotaOrangePrimary,
                                unfocusedBorderColor = RotaBorderMedium,
                                focusedTextColor = RotaTextWhite,
                                unfocusedTextColor = RotaTextWhite,
                                focusedContainerColor = RotaCardElevated,
                                unfocusedContainerColor = RotaCardBackground
                            )
                        )
                    }

                    // Mensagem de Erro
                    if (!state.errorMessage.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(RotaAvoid.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = RotaAvoid, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = state.errorMessage ?: "", color = RotaAvoid, fontSize = 13.sp)
                        }
                    }

                    // Mensagem de Sucesso
                    if (!state.successMessage.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(RotaExcellent.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RotaExcellent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = state.successMessage ?: "", color = RotaExcellent, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botão Salvar
                    RotaButton(
                        text = if (state.isLoading) "Salvando..." else "Salvar Nova Senha",
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.executeChangePassword()
                        },
                        variant = RotaButtonVariant.PRIMARY_ORANGE,
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
