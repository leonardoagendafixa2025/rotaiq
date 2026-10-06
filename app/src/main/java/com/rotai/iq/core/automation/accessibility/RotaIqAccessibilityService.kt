package com.rotai.iq.core.automation.accessibility

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.rotai.iq.RotaIqApplication
import com.rotai.iq.core.automation.overlay.OverlayManager
import com.rotai.iq.core.domain.engine.RideEvaluationEngine
import com.rotai.iq.core.domain.model.DriverGoal
import com.rotai.iq.core.domain.model.DriverPreference
import com.rotai.iq.core.domain.model.RideEvaluation
import com.rotai.iq.core.domain.model.Vehicle
import com.rotai.iq.core.domain.parser.PlatformDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RotaIqAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "RotaIqAccessibility"

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        private val _latestEvaluation = MutableStateFlow<RideEvaluation?>(null)
        val latestEvaluation: StateFlow<RideEvaluation?> = _latestEvaluation.asStateFlow()

        private val _evaluationsCount = MutableStateFlow(0)
        val evaluationsCount: StateFlow<Int> = _evaluationsCount.asStateFlow()
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val evaluationEngine = RideEvaluationEngine()
    private val detector = PlatformDetector()
    private var lastProcessedTextHash = 0
    private var lastProcessedTimestamp = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        _isServiceRunning.value = true
        Log.i(TAG, "RotaIqAccessibilityService conectado e ativo")
    }

    override fun onDestroy() {
        _isServiceRunning.value = false
        super.onDestroy()
        Log.i(TAG, "RotaIqAccessibilityService encerrado")
    }

    override fun onInterrupt() {
        Log.w(TAG, "RotaIqAccessibilityService interrompido pelo sistema")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val eventType = event.eventType
        if (eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        val packageName = event.packageName?.toString() ?: return
        if (!isMonitoredPackage(packageName)) {
            return
        }

        val rootNode = rootInActiveWindow ?: return
        val texts = AccessibilityNodeExtractor.extractAllTexts(rootNode)
        if (texts.isEmpty()) return

        val combinedText = AccessibilityNodeExtractor.buildCombinedText(texts)
        val currentHash = combinedText.hashCode()
        val now = System.currentTimeMillis()

        // Debounce: ignora mesmo texto capturado em intervalo inferior a 2 segundos
        if (currentHash == lastProcessedTextHash && (now - lastProcessedTimestamp) < 2000L) {
            return
        }

        lastProcessedTextHash = currentHash
        lastProcessedTimestamp = now

        processScreenText(combinedText, packageName)
    }

    private fun isMonitoredPackage(pkg: String): Boolean {
        return pkg.contains("uber", ignoreCase = true) ||
                pkg.contains("99", ignoreCase = true) ||
                pkg.contains("taxis99", ignoreCase = true) ||
                pkg == packageName // Permite auto-teste no próprio app ROTA IQ
    }

    private fun processScreenText(text: String, packageName: String? = null) {
        val offer = detector.detectAndParse(text, packageName) ?: return

        // Se a oferta não tem valor monetário identificado, não é um cartão válido
        if (offer.grossFare <= 0.0) return

        serviceScope.launch {
            try {
                val app = application as? RotaIqApplication ?: return@launch
                val repo = app.repository

                val vehicle = repo.getActiveVehicle().first()
                val preferences = repo.getDriverPreference().first()
                val goal = repo.getDriverGoal().first()

                val evaluation = evaluationEngine.evaluate(
                    offer = offer,
                    vehicle = vehicle,
                    preferences = preferences,
                    goal = goal
                )

                _latestEvaluation.value = evaluation
                _evaluationsCount.value += 1

                // 1. Exibir Floating HUD
                OverlayManager.showFloatingHud(this@RotaIqAccessibilityService, evaluation)

                // 2. Alerta Vocal TTS se habilitado
                app.voiceAlertManager.speakEvaluation(evaluation)

                // 3. Salvar no histórico
                repo.saveEvaluation(evaluation)

                Log.i(TAG, "Oferta processada com sucesso: ${offer.platform.displayName} - R$ ${offer.grossFare} - Nota: ${evaluation.score}")

            } catch (e: Exception) {
                Log.e(TAG, "Erro ao avaliar oferta em tempo real", e)
            }
        }
    }
}
