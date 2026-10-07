package com.rotai.iq.core.automation.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.rotai.iq.core.domain.model.EvaluationClassification
import com.rotai.iq.core.domain.model.RideEvaluation
import java.util.Locale

@SuppressLint("ViewConstructor")
class FloatingHudView(
    context: Context,
    private val windowManager: WindowManager,
    private val layoutParams: WindowManager.LayoutParams,
    private val onCloseClicked: () -> Unit
) : FrameLayout(context) {

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    private val container: LinearLayout
    private val scoreText: TextView
    private val classBadge: TextView
    private val profitText: TextView
    private val metricsText: TextView
    private val reasonsText: TextView

    init {
        // Container principal do Card HUD — Dark Premium ROTA IQ
        container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(14), dp(18), dp(16))
            val bg = GradientDrawable().apply {
                setColor(Color.parseColor("#0E0E0E"))
                cornerRadius = dp(20).toFloat()
                setStroke(dp(2), Color.parseColor("#00E676"))
            }
            background = bg
            elevation = dp(16).toFloat()
        }

        // Header: Score + Classificação + Botão Fechar
        val header = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            weightSum = 1f
        }

        scoreText = TextView(context).apply {
            textSize = 24f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#00E676"))
            text = "96"
        }

        classBadge = TextView(context).apply {
            textSize = 12f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            setPadding(dp(10), dp(4), dp(10), dp(4))
            val badgeBg = GradientDrawable().apply {
                setColor(Color.parseColor("#00E676"))
                cornerRadius = dp(8).toFloat()
            }
            background = badgeBg
            text = "EXCELENTE"
        }

        val spacer = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, 0, 1f)
        }

        val closeBtn = TextView(context).apply {
            text = "✕"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#757575"))
            setPadding(dp(8), dp(4), dp(8), dp(4))
            setOnClickListener { onCloseClicked() }
        }

        header.addView(scoreText)
        val scoreMargin = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { leftMargin = dp(10) }
        header.addView(classBadge, scoreMargin)
        header.addView(spacer)
        header.addView(closeBtn)
        container.addView(header)

        // Linha de Lucro Líquido Real ("LUCRO ESTIMADO")
        profitText = TextView(context).apply {
            textSize = 22f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            text = "LUCRO: R$ 26,60"
            setPadding(0, dp(8), 0, dp(2))
        }
        container.addView(profitText)

        // Métricas Operacionais com destaque Laranja Vibrante
        metricsText = TextView(context).apply {
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#FF7A00"))
            text = "R$ 75,69/h  •  R$ 3,49/km  (9,4 km)"
        }
        container.addView(metricsText)

        // Justificativas e Alertas
        reasonsText = TextView(context).apply {
            textSize = 11f
            setTextColor(Color.parseColor("#9E9E9E"))
            text = "✓ Acima da meta • Região Centro favorável"
            setPadding(0, dp(6), 0, 0)
        }
        container.addView(reasonsText)

        addView(container)

        // Suporte a arrastar pela tela (Drag & Drop)
        setupDragListener()
    }

    private fun setupDragListener() {
        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                    layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                    try {
                        windowManager.updateViewLayout(this, layoutParams)
                    } catch (e: Exception) {
                        // Janela pode ter sido desmontada
                    }
                    true
                }
                else -> false
            }
        }
    }

    fun updateWithEvaluation(evaluation: RideEvaluation) {
        val colorHex = when (evaluation.classification) {
            EvaluationClassification.EXCELLENT -> "#00E676"
            EvaluationClassification.GOOD -> "#76FF03"
            EvaluationClassification.ACCEPTABLE -> "#FFD600"
            EvaluationClassification.BAD -> "#FF9100"
            EvaluationClassification.AVOID -> "#FF334B"
        }
        val mainColor = Color.parseColor(colorHex)

        // Atualiza borda do container com o veredito da corrida
        (container.background as? GradientDrawable)?.setStroke(dp(2), mainColor)

        // Score e Badge
        scoreText.text = evaluation.score.toString()
        scoreText.setTextColor(mainColor)

        classBadge.text = evaluation.classification.label.uppercase()
        (classBadge.background as? GradientDrawable)?.setColor(mainColor)

        // Lucro Líquido
        profitText.text = if (evaluation.netProfit >= 0) {
            "LUCRO: R$ %.2f".format(Locale("pt", "BR"), evaluation.netProfit)
        } else {
            "PREJUÍZO: -R$ %.2f".format(Locale("pt", "BR"), -evaluation.netProfit)
        }
        profitText.setTextColor(if (evaluation.netProfit >= 0) Color.WHITE else Color.parseColor("#FF334B"))

        // Métricas
        metricsText.text = "R$ %.2f/h  •  R$ %.2f/km  (%.1f km)".format(
            Locale("pt", "BR"),
            evaluation.netRatePerHour,
            evaluation.grossRatePerKm,
            evaluation.offer.totalDistanceKm
        )

        // Alertas / Motivos
        val firstAlert = evaluation.alerts.firstOrNull()
        val firstReason = evaluation.reasons.firstOrNull() ?: "Decisão ROTA IQ pronta"
        reasonsText.text = if (!firstAlert.isNullOrBlank()) {
            "⚠️ $firstAlert"
        } else {
            "✓ $firstReason"
        }
        reasonsText.setTextColor(if (!firstAlert.isNullOrBlank()) Color.parseColor("#FFD600") else Color.parseColor("#9E9E9E"))
    }

    private fun dp(value: Int): Int {
        return (value * context.resources.displayMetrics.density).toInt()
    }
}
