package com.rotai.iq.core.automation.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import com.rotai.iq.core.domain.model.RideEvaluation

object OverlayManager {

    private const val TAG = "OverlayManager"
    private var windowManager: WindowManager? = null
    private var floatingHudView: FloatingHudView? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var autoDismissRunnable: Runnable? = null

    var isOverlayEnabled: Boolean = true

    fun hasOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun showFloatingHud(context: Context, evaluation: RideEvaluation) {
        if (!isOverlayEnabled) {
            Log.d(TAG, "Overlay desativado pelo usuário")
            return
        }

        if (!hasOverlayPermission(context)) {
            Log.w(TAG, "Permissão SYSTEM_ALERT_WINDOW não concedida")
            return
        }

        mainHandler.post {
            try {
                if (windowManager == null) {
                    windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                }

                val wm = windowManager ?: return@post

                // Se já existe uma janela aberta, apenas atualiza o conteúdo
                if (floatingHudView != null) {
                    floatingHudView?.updateWithEvaluation(evaluation)
                    resetAutoDismiss()
                    return@post
                }

                val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                }

                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    layoutType,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                    y = 120 // Espaçamento superior para não cobrir a barra de status
                }

                val hudView = FloatingHudView(
                    context = context,
                    windowManager = wm,
                    layoutParams = params,
                    onCloseClicked = { dismiss() }
                )

                hudView.updateWithEvaluation(evaluation)
                wm.addView(hudView, params)
                floatingHudView = hudView

                resetAutoDismiss()
                Log.i(TAG, "Floating HUD exibido com sucesso para a corrida: nota ${evaluation.score}")

            } catch (e: Exception) {
                Log.e(TAG, "Erro ao adicionar Floating HUD na janela", e)
            }
        }
    }

    private fun resetAutoDismiss() {
        autoDismissRunnable?.let { mainHandler.removeCallbacks(it) }
        autoDismissRunnable = Runnable { dismiss() }
        mainHandler.postDelayed(autoDismissRunnable!!, 15000) // Fecha automaticamente em 15 segundos
    }

    fun dismiss() {
        mainHandler.post {
            try {
                autoDismissRunnable?.let { mainHandler.removeCallbacks(it) }
                autoDismissRunnable = null

                floatingHudView?.let {
                    windowManager?.removeView(it)
                    floatingHudView = null
                    Log.d(TAG, "Floating HUD removido")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao remover Floating HUD", e)
            }
        }
    }
}
