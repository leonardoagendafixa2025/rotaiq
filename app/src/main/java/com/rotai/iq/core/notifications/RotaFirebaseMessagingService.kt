package com.rotai.iq.core.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rotai.iq.MainActivity
import com.rotai.iq.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL

/**
 * Serviço Oficial de Push Notifications Firebase Cloud Messaging (FCM).
 * Responsável por:
 * 1. Capturar tokens novos e registrá-los no backend com metadados do dispositivo
 * 2. Receber mensagens push tanto em foreground quanto em background
 * 3. Exibir notificações NATIVAS no sistema Android com deep link e imagem
 */
class RotaFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "RotaFCMService"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Novo token FCM obtido: $token")
        
        // Salva token localmente e envia para o backend
        DeviceTokenManager.saveLocalToken(applicationContext, token)
        CoroutineScope(Dispatchers.IO).launch {
            DeviceTokenManager.registerTokenWithBackend(applicationContext, token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Mensagem push recebida de: ${remoteMessage.from}")

        // 1. Extrai título e mensagem do payload notification ou data
        val notificationPayload = remoteMessage.notification
        val dataPayload = remoteMessage.data

        val title = notificationPayload?.title 
            ?: dataPayload["title"] 
            ?: "ROTA IQ"

        val body = notificationPayload?.body 
            ?: dataPayload["body"] 
            ?: "Você tem uma nova notificação."

        val deepLink = dataPayload["deep_link"] 
            ?: "rotaiq://home"

        val campaignType = dataPayload["campaign_type"] 
            ?: dataPayload["type"] 
            ?: "MARKETING"

        val imageUrl = notificationPayload?.imageUrl?.toString() 
            ?: dataPayload["image_url"]

        // 2. Determina o canal de notificação apropriado
        val channelId = when (campaignType.uppercase()) {
            "PROMOCAO", "MARKETING" -> NotificationChannels.CHANNEL_MARKETING
            "SISTEMA" -> NotificationChannels.CHANNEL_SYSTEM
            "ENGAJAMENTO" -> NotificationChannels.CHANNEL_RIDES
            else -> NotificationChannels.CHANNEL_GENERAL
        }

        // 3. Monta a notificação nativa do sistema operacional Android
        showNativeNotification(
            title = title,
            body = body,
            deepLink = deepLink,
            channelId = channelId,
            imageUrl = imageUrl
        )
    }

    private fun showNativeNotification(
        title: String,
        body: String,
        deepLink: String,
        channelId: String,
        imageUrl: String?
    ) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink)).apply {
            setClass(applicationContext, MainActivity::class.java)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("deep_link_source", "push_notification")
            putExtra("deep_link_target", deepLink)
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            pendingIntentFlags
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setColor(0xFFFF7A00.toInt()) // Laranja ROTA IQ (#FF7A00)
            .setContentIntent(pendingIntent)

        // Se houver imagem opcional, carrega no estilo BigPictureStyle
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
                Log.w(TAG, "Não foi possível carregar imagem remota da notificação: ${e.message}")
            }
        }

        try {
            val notificationId = (System.currentTimeMillis() % 100000).toInt()
            NotificationManagerCompat.from(this).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            Log.e(TAG, "Permissão POST_NOTIFICATIONS não concedida pelo usuário: ${e.message}")
        }
    }
}
