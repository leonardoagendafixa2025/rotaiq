package com.rotai.iq.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rotai.iq.core.domain.model.EvaluationClassification
import com.rotai.iq.core.domain.model.RideEvaluation
import com.rotai.iq.core.ui.theme.ClassAcceptable
import com.rotai.iq.core.ui.theme.ClassAvoid
import com.rotai.iq.core.ui.theme.ClassBad
import com.rotai.iq.core.ui.theme.ClassExcellent
import com.rotai.iq.core.ui.theme.ClassGood
import com.rotai.iq.core.ui.theme.CockpitBorder
import com.rotai.iq.core.ui.theme.CockpitSurface
import com.rotai.iq.core.ui.theme.CockpitSurfaceVariant
import com.rotai.iq.core.ui.theme.TextPrimary
import com.rotai.iq.core.ui.theme.TextSecondary
import java.util.Locale

fun getClassificationColor(classification: EvaluationClassification): Color {
    return when (classification) {
        EvaluationClassification.EXCELLENT -> ClassExcellent
        EvaluationClassification.GOOD -> ClassGood
        EvaluationClassification.ACCEPTABLE -> ClassAcceptable
        EvaluationClassification.BAD -> ClassBad
        EvaluationClassification.AVOID -> ClassAvoid
    }
}

@Composable
fun ScoreBadge(
    classification: EvaluationClassification,
    score: Int,
    modifier: Modifier = Modifier
) {
    val color = getClassificationColor(classification)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = classification.label,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$score/100",
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
    }
}

@Composable
fun CockpitMetricCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    accentColor: Color = TextPrimary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CockpitSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CockpitBorder)
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
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = accentColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun HudEvaluationCard(
    evaluation: RideEvaluation,
    modifier: Modifier = Modifier
) {
    val classColor = getClassificationColor(evaluation.classification)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CockpitSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, classColor.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header: Status Badge + Platform
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScoreBadge(
                    classification = evaluation.classification,
                    score = evaluation.score
                )
                Text(
                    text = evaluation.offer.platform.displayName,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Gross Fare
            Text(
                text = "R$ %.2f".format(Locale("pt", "BR"), evaluation.grossFare),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Distance & Duration
            Text(
                text = "%.1f km • %d min (total: %.1f km)".format(
                    Locale("pt", "BR"),
                    evaluation.offer.distanceKm,
                    evaluation.totalDurationMinutes.toInt(),
                    evaluation.totalDistanceKm
                ),
                color = TextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Key Rates
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CockpitSurfaceVariant, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "R$/KM", fontSize = 10.sp, color = TextSecondary)
                    Text(
                        text = "R$ %.2f/km".format(Locale("pt", "BR"), evaluation.grossRatePerKm),
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                }
                Column {
                    Text(text = "R$/HORA", fontSize = 10.sp, color = TextSecondary)
                    Text(
                        text = "R$ %.2f/h".format(Locale("pt", "BR"), evaluation.grossRatePerHour),
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                }
                Column {
                    Text(text = "MARGEM", fontSize = 10.sp, color = TextSecondary)
                    Text(
                        text = "%.0f%%".format(evaluation.profitMarginPercent),
                        fontWeight = FontWeight.Bold,
                        color = classColor,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cost & Net Profit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Custo Estimado", fontSize = 12.sp, color = TextSecondary)
                    Text(
                        text = "R$ %.2f".format(Locale("pt", "BR"), evaluation.estimatedCost),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ClassBad
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Lucro Líquido", fontSize = 12.sp, color = TextSecondary)
                    Text(
                        text = "R$ %.2f".format(Locale("pt", "BR"), evaluation.netProfit),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (evaluation.netProfit > 0) ClassExcellent else ClassAvoid
                    )
                }
            }

            // Reasons & Alerts
            if (evaluation.reasons.isNotEmpty() || evaluation.alerts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                ReasonsList(reasons = evaluation.reasons, alerts = evaluation.alerts)
            }
        }
    }
}

@Composable
fun ReasonsList(
    reasons: List<String>,
    alerts: List<String>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        reasons.forEach { reason ->
            Row(
                modifier = Modifier.padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = ClassExcellent,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = reason.removePrefix("✓ "),
                    color = TextPrimary,
                    fontSize = 12.sp
                )
            }
        }
        alerts.forEach { alert ->
            Row(
                modifier = Modifier.padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (alert.startsWith("⛔")) ClassAvoid else ClassAcceptable,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = alert.removePrefix("⚠️ ").removePrefix("⛔ "),
                    color = if (alert.startsWith("⛔")) ClassAvoid else ClassAcceptable,
                    fontSize = 12.sp
                )
            }
        }
    }
}
