package com.rotai.iq.core.ui.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rotai.iq.core.domain.model.EvaluationClassification
import com.rotai.iq.core.ui.theme.ButtonShapePill
import com.rotai.iq.core.ui.theme.CardShapeDefault
import com.rotai.iq.core.ui.theme.CardShapeElevated
import com.rotai.iq.core.ui.theme.ChipShapeCapsule
import com.rotai.iq.core.ui.theme.RotaAttention
import com.rotai.iq.core.ui.theme.RotaAvoid
import com.rotai.iq.core.ui.theme.RotaBorderMedium
import com.rotai.iq.core.ui.theme.RotaBorderSubtle
import com.rotai.iq.core.ui.theme.RotaCardBackground
import com.rotai.iq.core.ui.theme.RotaCardElevated
import com.rotai.iq.core.ui.theme.RotaDarkCanvas
import com.rotai.iq.core.ui.theme.RotaExcellent
import com.rotai.iq.core.ui.theme.RotaGlowGreen
import com.rotai.iq.core.ui.theme.RotaGlowRed
import com.rotai.iq.core.ui.theme.RotaGlowYellow
import com.rotai.iq.core.ui.theme.RotaGood
import com.rotai.iq.core.ui.theme.RotaOrangeHover
import com.rotai.iq.core.ui.theme.RotaOrangeLight
import com.rotai.iq.core.ui.theme.RotaOrangePrimary
import com.rotai.iq.core.ui.theme.RotaOrangeSubtleBg
import com.rotai.iq.core.ui.theme.RotaTextPrimary
import com.rotai.iq.core.ui.theme.RotaTextSecondary
import com.rotai.iq.core.ui.theme.RotaTextTertiary
import com.rotai.iq.core.ui.theme.RotaTextWhite
import com.rotai.iq.core.ui.theme.RotaWarning

// ======================================================================
// 1. ROTA CARD — Dark Premium com profundidade, borda sutil e glow opcional
// ======================================================================

@Composable
fun RotaCard(
    modifier: Modifier = Modifier,
    shape: Shape = CardShapeDefault,
    backgroundColor: Color = RotaCardBackground,
    borderColor: Color = RotaBorderSubtle,
    borderWidth: Dp = 1.dp,
    glowColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    val cardModifier = if (glowColor != null) {
        modifier
            .border(2.dp, glowColor, shape)
            .border(borderWidth, borderColor, shape)
    } else {
        modifier.border(borderWidth, borderColor, shape)
    }

    Card(
        modifier = cardModifier
            .clip(shape)
            .then(clickableModifier),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        content()
    }
}

// ======================================================================
// 2. ROTA BUTTON — Botão Laranja Vibrante ou Dark Outline
// ======================================================================

enum class RotaButtonVariant {
    PRIMARY_ORANGE,
    SECONDARY_DARK,
    OUTLINE,
    DANGER
}

@Composable
fun RotaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: RotaButtonVariant = RotaButtonVariant.PRIMARY_ORANGE,
    icon: ImageVector? = Icons.AutoMirrored.Filled.ArrowForward,
    iconAtEnd: Boolean = true,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.97f else 1.0f

    val bgBrush = when (variant) {
        RotaButtonVariant.PRIMARY_ORANGE -> Brush.horizontalGradient(
            colors = listOf(RotaOrangeHover, RotaOrangePrimary, RotaOrangeLight)
        )
        RotaButtonVariant.SECONDARY_DARK -> Brush.horizontalGradient(
            colors = listOf(RotaCardElevated, RotaCardElevated)
        )
        RotaButtonVariant.OUTLINE -> Brush.horizontalGradient(
            colors = listOf(Color.Transparent, Color.Transparent)
        )
        RotaButtonVariant.DANGER -> Brush.horizontalGradient(
            colors = listOf(RotaAvoid, Color(0xFFC62828))
        )
    }

    val contentColor = when (variant) {
        RotaButtonVariant.PRIMARY_ORANGE -> Color(0xFF0A0A0A)
        RotaButtonVariant.SECONDARY_DARK -> RotaTextWhite
        RotaButtonVariant.OUTLINE -> RotaOrangePrimary
        RotaButtonVariant.DANGER -> RotaTextWhite
    }

    val borderModifier = when (variant) {
        RotaButtonVariant.OUTLINE -> Modifier.border(1.5.dp, RotaOrangePrimary, ButtonShapePill)
        RotaButtonVariant.SECONDARY_DARK -> Modifier.border(1.dp, RotaBorderMedium, ButtonShapePill)
        else -> Modifier
    }

    Box(
        modifier = modifier
            .scale(scale)
            .height(54.dp)
            .clip(ButtonShapePill)
            .background(if (enabled) bgBrush else Brush.linearGradient(listOf(Color(0xFF2C2C2E), Color(0xFF2C2C2E))))
            .then(borderModifier)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 22.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null && !iconAtEnd) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = text,
                color = contentColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )

            if (icon != null && iconAtEnd) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ======================================================================
// 3. ROTA METRIC — Destaque absoluto no valor com hierarquia legível
// ======================================================================

@Composable
fun RotaMetric(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    accentColor: Color = RotaTextWhite,
    icon: ImageVector? = null,
    highlight: Boolean = false
) {
    RotaCard(
        modifier = modifier,
        shape = CardShapeElevated,
        backgroundColor = if (highlight) RotaOrangeSubtleBg else RotaCardBackground,
        borderColor = if (highlight) RotaOrangePrimary.copy(alpha = 0.4f) else RotaBorderSubtle
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = RotaTextSecondary,
                    letterSpacing = 0.8.sp
                )
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (highlight) RotaOrangePrimary else RotaTextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = accentColor,
                letterSpacing = (-0.5).sp
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = RotaTextTertiary
                )
            }
        }
    }
}

// ======================================================================
// 4. ROTA SCORE — Badge pill de Veredito com Glow suave
// ======================================================================

fun getRotaClassificationColor(classification: EvaluationClassification): Color {
    return when (classification) {
        EvaluationClassification.EXCELLENT -> RotaExcellent
        EvaluationClassification.GOOD -> RotaGood
        EvaluationClassification.ACCEPTABLE -> RotaAttention
        EvaluationClassification.BAD -> RotaWarning
        EvaluationClassification.AVOID -> RotaAvoid
    }
}

fun getRotaClassificationGlow(classification: EvaluationClassification): Color {
    return when (classification) {
        EvaluationClassification.EXCELLENT -> RotaGlowGreen
        EvaluationClassification.GOOD -> RotaGlowGreen
        EvaluationClassification.ACCEPTABLE -> RotaGlowYellow
        EvaluationClassification.BAD -> RotaGlowYellow
        EvaluationClassification.AVOID -> RotaGlowRed
    }
}

@Composable
fun RotaScore(
    classification: EvaluationClassification,
    score: Int,
    modifier: Modifier = Modifier,
    large: Boolean = false
) {
    val color = getRotaClassificationColor(classification)

    Row(
        modifier = modifier
            .clip(ChipShapeCapsule)
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.55f), ChipShapeCapsule)
            .padding(
                horizontal = if (large) 14.dp else 10.dp,
                vertical = if (large) 8.dp else 5.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (large) 10.dp else 8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = classification.label,
            color = color,
            fontWeight = FontWeight.ExtraBold,
            fontSize = if (large) 13.sp else 11.sp,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$score/100",
            color = RotaTextWhite,
            fontWeight = FontWeight.Bold,
            fontSize = if (large) 13.sp else 11.sp
        )
    }
}

// ======================================================================
// 5. ROTA HEADER — Cabeçalho Oficial do Aplicativo
// ======================================================================

@Composable
fun RotaHeader(
    driverName: String = "Motorista",
    isOnline: Boolean = true,
    isSyncing: Boolean = false,
    lastSyncTime: String? = null,
    notificationCount: Int = 2,
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Marca e Saudação
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ROTA ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = RotaTextWhite,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "IQ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = RotaOrangePrimary,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Bom trabalho, $driverName 👋",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = RotaTextSecondary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Status Online & Nuvem (P3-006)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isOnline) RotaExcellent.copy(alpha = 0.12f) else RotaTextTertiary.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isOnline) RotaExcellent else RotaTextTertiary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOnline) "ONLINE" else "OFFLINE",
                        color = if (isOnline) RotaExcellent else RotaTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(RotaCardBackground)
                        .border(0.5.dp, RotaBorderSubtle, RoundedCornerShape(12.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isSyncing) "🔄 Sincronizando..." else (if (lastSyncTime != null) "✓ $lastSyncTime" else "✓ Sincronizado"),
                        color = if (isSyncing) RotaOrangeLight else RotaTextTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Ações: Notificações + Avatar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Sino de Notificações
            Box {
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(RotaCardBackground)
                        .border(1.dp, RotaBorderSubtle, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notificações",
                        tint = RotaTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (notificationCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(RotaOrangePrimary)
                    )
                }
            }

            // Avatar do Motorista
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(RotaCardElevated, RotaCardBackground)
                        )
                    )
                    .border(1.5.dp, RotaOrangePrimary, CircleShape)
                    .clickable(onClick = onProfileClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = driverName.take(2).uppercase(),
                    color = RotaOrangePrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }
        }
    }
}

// ======================================================================
// 6. ROTA PROGRESS — Barra de Progresso com Gradiente Laranja/Verde
// ======================================================================

@Composable
fun RotaProgress(
    progress: Float, // 0.0f a 1.0f
    modifier: Modifier = Modifier,
    barColor: Color = RotaOrangePrimary,
    trackColor: Color = Color(0xFF1E1E1E),
    height: Dp = 8.dp
) {
    val clamped = progress.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(clamped)
                .height(height)
                .clip(RoundedCornerShape(height / 2))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(barColor.copy(alpha = 0.8f), barColor)
                    )
                )
        )
    }
}

// ======================================================================
// 7. ROTA CHIP — Cápsulas de Filtro / Categorias
// ======================================================================

@Composable
fun RotaChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    count: Int? = null
) {
    val bg = if (isSelected) RotaOrangePrimary else RotaCardBackground
    val contentColor = if (isSelected) Color(0xFF0A0A0A) else RotaTextSecondary
    val borderColor = if (isSelected) RotaOrangePrimary else RotaBorderSubtle

    Row(
        modifier = modifier
            .clip(ChipShapeCapsule)
            .background(bg)
            .border(1.dp, borderColor, ChipShapeCapsule)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        Text(
            text = text,
            color = contentColor,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
        )

        if (count != null) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) Color(0x33000000) else RotaCardElevated)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$count",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
        }
    }
}

// ======================================================================
// 8. ROTA STATUS — Veredito e Status de Condução
// ======================================================================

@Composable
fun RotaStatus(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

// ======================================================================
// 9. ROTA CHART — Gráfico Minimalista de Faturamento e Meta
// ======================================================================

data class ChartDay(val dayLabel: String, val amount: Double, val isCurrentDay: Boolean = false)

@Composable
fun RotaChart(
    days: List<ChartDay>,
    goalAmount: Double,
    modifier: Modifier = Modifier
) {
    val maxVal = (days.maxOfOrNull { it.amount } ?: goalAmount).coerceAtLeast(goalAmount)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            days.forEach { day ->
                val ratio = (day.amount / maxVal).toFloat().coerceIn(0.08f, 1f)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f)
                ) {
                    val barColor = if (day.isCurrentDay) RotaOrangePrimary else Color(0xFF262626)
                    val glowModifier = if (day.isCurrentDay) {
                        Modifier.border(1.dp, RotaOrangeHover, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    } else Modifier

                    Box(
                        modifier = Modifier
                            .width(26.dp)
                            .fillMaxWidth(0.6f)
                            .height((100 * ratio).dp)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(barColor)
                            .then(glowModifier)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = day.dayLabel,
                        fontSize = 11.sp,
                        fontWeight = if (day.isCurrentDay) FontWeight.Bold else FontWeight.Normal,
                        color = if (day.isCurrentDay) RotaOrangePrimary else RotaTextTertiary
                    )
                }
            }
        }
    }
}

// ======================================================================
// 10. ROTA EMPTY STATE — Estado Vazio sem inventar dados
// ======================================================================

@Composable
fun RotaEmptyState(
    title: String,
    description: String? = null,
    icon: ImageVector = Icons.Default.Info,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    RotaCard(
        modifier = modifier.fillMaxWidth(),
        shape = CardShapeDefault,
        backgroundColor = RotaCardBackground,
        borderColor = RotaBorderSubtle
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(RotaDarkCanvas)
                    .border(1.dp, RotaOrangePrimary.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RotaOrangePrimary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = RotaTextWhite,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (!description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = RotaTextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 18.sp
                )
            }

            if (actionButtonText != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(20.dp))
                RotaButton(
                    text = actionButtonText,
                    onClick = onActionClick,
                    variant = RotaButtonVariant.PRIMARY_ORANGE,
                    modifier = Modifier.fillMaxWidth(0.85f)
                )
            }
        }
    }
}

// ======================================================================
// 11. ROTA LOADING — Indicador de Carregamento Premium
// ======================================================================

@Composable
fun RotaLoading(
    message: String = "Carregando...",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = RotaOrangePrimary,
            modifier = Modifier.size(36.dp),
            strokeWidth = 3.dp
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = message,
            fontSize = 13.sp,
            color = RotaTextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

// ======================================================================
// 12. ROTA ERROR — Exibição de Falha / Erro com Ação de Repetir
// ======================================================================

@Composable
fun RotaError(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    RotaCard(
        modifier = modifier.fillMaxWidth(),
        shape = CardShapeDefault,
        backgroundColor = RotaCardBackground,
        borderColor = RotaAvoid.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(RotaAvoid.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = RotaAvoid,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                color = RotaTextPrimary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (onRetry != null) {
                Spacer(modifier = Modifier.height(14.dp))
                RotaButton(
                    text = "Tentar novamente",
                    onClick = onRetry,
                    variant = RotaButtonVariant.OUTLINE,
                    modifier = Modifier.fillMaxWidth(0.6f)
                )
            }
        }
    }
}

// ======================================================================
// 13. ROTA DIALOG — Modal de Confirmação e Ação com Estilo ROTA IQ
// ======================================================================

@Composable
fun RotaDialog(
    title: String,
    onDismissRequest: () -> Unit,
    confirmButtonText: String = "Confirmar",
    onConfirm: () -> Unit,
    dismissButtonText: String? = "Cancelar",
    content: @Composable () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismissRequest) {
        RotaCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = CardShapeElevated,
            backgroundColor = RotaCardElevated,
            borderColor = RotaBorderMedium
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = RotaTextWhite,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                content()
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (dismissButtonText != null) {
                        RotaButton(
                            text = dismissButtonText,
                            onClick = onDismissRequest,
                            variant = RotaButtonVariant.SECONDARY_DARK,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    RotaButton(
                        text = confirmButtonText,
                        onClick = onConfirm,
                        variant = RotaButtonVariant.PRIMARY_ORANGE
                    )
                }
            }
        }
    }
}

