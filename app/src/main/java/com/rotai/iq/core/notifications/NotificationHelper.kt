package com.rotai.iq.core.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rotai.iq.MainActivity
import java.net.URL

/**
 * Utilitário central para exibição de Notificações Nativas no Android.
 * Garante entrega com SOM FORTE, VIBRAÇÃO e BANNER FLUTUANTE (HEADS-UP)
 * idêntico aos apps de grande porte (Shopee, iFood, WhatsApp).
 */
object NotificationHelper {

    private const val TAG = "NotificationHelper"

    fun showHeadsUpNotification(
        context: Context,
        title: String,
        body: String,
        deepLink: String = "rotaiq://home",
        channelId: String = NotificationChannels.CHANNEL_MARKETING,
        imageUrl: String? = null,
        campaignId: String? = null
    ) {
        try {
            // Garante que os canais de alta prioridade existam
            NotificationChannels.createChannels(context)

            // 1. Acorda a tela brevemente caso esteja desligada (comportamento Shopee/iFood)
            wakeUpScreen(context)

            // 2. Toca o som de notificação padrão do aparelho imediatamente
            playNotificationSound(context)

            // 3. Monta o PendingIntent para abrir o app no deep link especificado
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink)).apply {
                setClass(context, MainActivity::class.java)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("deep_link_source", "push_notification")
                putExtra("deep_link_target", deepLink)
                putExtra("campaign_id", campaignId)
            }

            val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                (System.currentTimeMillis() % 100000).toInt(),
                intent,
                pendingIntentFlags
            )

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val vibrationPattern = longArrayOf(0, 400, 200, 400)

            // 4. Constrói a notificação com PRIORITY_MAX (Heads-up banner garantido)
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_MAX) // Força exibição de banner flutuante
                .setDefaults(NotificationCompat.DEFAULT_ALL)  // Som, vibração e luz
                .setSound(soundUri)
                .setVibrate(vibrationPattern)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .setColor(0xFFFF7A00.toInt()) // Cor oficial ROTA IQ Laranja (#FF7A00)
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(pendingIntent, false) // Heads-up ativo sobre outros apps

            // 5. Imagem rica opcional (BigPictureStyle)
            if (!imageUrl.isNullOrBlank()) {
                try {
                    val url = URL(imageUrl)
                    val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                    if (bitmap != null) {
                        builder.setLargeIcon(bitmap)
                        builder.setStyle(
                            NotificationCompat.BigPictureStyle()
                                .bigPicture(bitmap)
                                .setSummaryText(body)
                        )
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Não foi possível carregar imagem da notificação: ${e.message}")
                }
            }

            val notificationId = (System.currentTimeMillis() % 100000).toInt()
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            Log.d(TAG, "Notificação exibida com sucesso: '$title'")

        } catch (e: SecurityException) {
            Log.e(TAG, "Permissão POST_NOTIFICATIONS não concedida: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao disparar notificação: ${e.message}", e)
        }
    }

    private fun playNotificationSound(context: Context) {
        try {
            val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, notificationUri)
            ringtone?.play()
        } catch (e: Exception) {
            Log.w(TAG, "Falha ao reproduzir áudio direto: ${e.message}")
        }
    }

    private fun wakeUpScreen(context: Context) {
        try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            if (pm != null && !pm.isInteractive) {
                @Suppress("DEPRECATION")
                val wakeLock = pm.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "rotai:push_heads_up"
                )
                wakeLock.acquire(3000L) // Acorda por 3 segundos
            }
        } catch (e: Exception) {
            Log.w(TAG, "Não foi possível acordar a tela: ${e.message}")
        }
    }
}
