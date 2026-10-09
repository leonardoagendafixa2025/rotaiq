package com.rotai.iq.core.telemetry

import android.content.Context
import android.util.Log
import com.rotai.iq.core.security.PiiSanitizer

/**
 * ROTA IQ — CAPTURADOR E RELATOR RESILIENTE DE FALHAS
 * 
 * Captura exceções não tratadas no app Android, sanitiza dados pessoais (LGPD),
 * persiste o log de auditoria no TelemetryManager e transfere para o tratador nativo do SO.
 */
class RotaCrashReporter private constructor(
    private val context: Context,
    private val telemetryManager: TelemetryManager,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    companion object {
        private const val TAG = "RotaCrashReporter"

        @Volatile
        private var isInstalled = false

        fun install(context: Context, telemetryManager: TelemetryManager) {
            if (isInstalled) return
            synchronized(this) {
                if (isInstalled) return
                val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
                val reporter = RotaCrashReporter(context.applicationContext, telemetryManager, defaultHandler)
                Thread.setDefaultUncaughtExceptionHandler(reporter)
                isInstalled = true
                Log.i(TAG, "RotaCrashReporter ativado com sucesso para monitoramento resiliente.")
            }
        }
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            val sanitizedMessage = PiiSanitizer.sanitizeText(throwable.message ?: "Exceção não documentada")
            Log.e(TAG, "Exceção não tratada capturada na thread [${thread.name}]: $sanitizedMessage", throwable)
            
            // Grava o evento na telemetria local
            telemetryManager.recordCrash(throwable, isFatal = true)
        } catch (e: Throwable) {
            Log.e(TAG, "Falha interna ao registrar crash report", e)
        } finally {
            // Repassa para o handler padrão do sistema operacional
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
