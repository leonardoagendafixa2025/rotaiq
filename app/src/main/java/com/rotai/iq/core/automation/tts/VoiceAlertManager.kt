package com.rotai.iq.core.automation.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import com.rotai.iq.core.domain.model.RideEvaluation
import java.util.Locale

class VoiceAlertManager(private val context: Context) : TextToSpeech.OnInitListener {

    private val tag = "VoiceAlertManager"
    private var tts: TextToSpeech? = null
    private var isInitialized: Boolean = false
    var isEnabled: Boolean = true

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e(tag, "Falha ao instanciar TextToSpeech", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("pt", "BR"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(tag, "Idioma pt-BR não disponível, utilizando idioma padrão")
                tts?.setLanguage(Locale.getDefault())
            }
            tts?.setSpeechRate(1.15f) // Velocidade ligeiramente acelerada para decisões dinâmicas
            isInitialized = true
            Log.i(tag, "TextToSpeech inicializado com sucesso para pt-BR")
        } else {
            Log.e(tag, "Inicialização do TextToSpeech falhou com status: $status")
            isInitialized = false
        }
    }

    fun speakEvaluation(evaluation: RideEvaluation) {
        if (!isEnabled || !isInitialized) return
        val message = TtsMessageFormatter.formatSpeechMessage(evaluation)
        speak(message)
    }

    fun speak(text: String) {
        if (!isEnabled || !isInitialized) return
        try {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ROTA_IQ_ALERT_${System.currentTimeMillis()}")
        } catch (e: Exception) {
            Log.e(tag, "Erro ao vocalizar mensagem", e)
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e(tag, "Erro ao parar áudio TTS", e)
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        } catch (e: Exception) {
            Log.e(tag, "Erro ao liberar recursos TTS", e)
        }
    }
}
