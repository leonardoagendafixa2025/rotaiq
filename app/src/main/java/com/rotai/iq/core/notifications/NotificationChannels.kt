package com.rotai.iq.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * Gerenciador dos Canais de Notificação Nativos do Android (API 26+).
 * Permite que o motorista configure individualmente o que deseja receber nas configurações do aparelho.
 */
object NotificationChannels {

    const val CHANNEL_GENERAL = "rotaiq_general"
    const val CHANNEL_MARKETING = "rotaiq_marketing"
    const val CHANNEL_SYSTEM = "rotaiq_system"
    const val CHANNEL_RIDES = "rotaiq_rides"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = android.media.AudioAttributes.Builder()
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                .build()
            val vibrationPattern = longArrayOf(0, 350, 200, 350)

            // 1. Canal Geral (Alta prioridade / Heads-up com som)
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "Geral — ROTA IQ",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações gerais, comunicados e alertas em tempo real."
                enableVibration(true)
                this.vibrationPattern = vibrationPattern
                setSound(soundUri, audioAttributes)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                enableLights(true)
                lightColor = android.graphics.Color.parseColor("#FF7A00")
                setShowBadge(true)
            }

            // 2. Canal de Promoções & Marketing (Alta prioridade / Heads-up com som como Shopee/iFood)
            val marketingChannel = NotificationChannel(
                CHANNEL_MARKETING,
                "Promoções & Ofertas Pro",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Descontos no ROTA IQ Pro, novidades comerciais e benefícios exclusivos."
                enableVibration(true)
                this.vibrationPattern = vibrationPattern
                setSound(soundUri, audioAttributes)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                enableLights(true)
                lightColor = android.graphics.Color.parseColor("#FF7A00")
                setShowBadge(true)
            }

            // 3. Canal do Sistema & Segurança (Alta prioridade / Heads-up com som)
            val systemChannel = NotificationChannel(
                CHANNEL_SYSTEM,
                "Alertas do Sistema",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos importantes de conta, segurança e manutenção técnica."
                enableVibration(true)
                this.vibrationPattern = vibrationPattern
                setSound(soundUri, audioAttributes)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                enableLights(true)
                lightColor = android.graphics.Color.parseColor("#FF7A00")
                setShowBadge(true)
            }

            // 4. Canal de Oportunidades & Corridas (Alta prioridade / Heads-up com som)
            val ridesChannel = NotificationChannel(
                CHANNEL_RIDES,
                "Oportunidades & Praças em Alta",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas de alta demanda e picos de corrida na sua região."
                enableVibration(true)
                this.vibrationPattern = vibrationPattern
                setSound(soundUri, audioAttributes)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                enableLights(true)
                lightColor = android.graphics.Color.parseColor("#FF7A00")
                setShowBadge(true)
            }

            notificationManager.createNotificationChannels(
                listOf(generalChannel, marketingChannel, systemChannel, ridesChannel)
            )
        }
    }
}
