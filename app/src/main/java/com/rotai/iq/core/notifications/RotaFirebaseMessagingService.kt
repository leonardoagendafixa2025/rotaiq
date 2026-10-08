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

        val campaignId = dataPayload["campaign_id"]

        // 3. Monta a notificação nativa do sistema Android com som, vibração e banner flutuante
        NotificationHelper.showHeadsUpNotification(
            context = applicationContext,
            title = title,
            body = body,
            deepLink = deepLink,
            channelId = channelId,
            imageUrl = imageUrl,
            campaignId = campaignId
        )
    }
}
