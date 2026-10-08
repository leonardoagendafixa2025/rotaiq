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

            // 1. Canal Geral (Alta prioridade)
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "Geral — ROTA IQ",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações gerais, atualizações e comunicados da plataforma."
                enableVibration(true)
                setShowBadge(true)
            }

            // 2. Canal de Promoções & Marketing
            val marketingChannel = NotificationChannel(
                CHANNEL_MARKETING,
                "Promoções & Ofertas Pro",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Descontos no ROTA IQ Pro, novidades comerciais e benefícios exclusivos."
                setShowBadge(true)
            }

            // 3. Canal do Sistema & Segurança
            val systemChannel = NotificationChannel(
                CHANNEL_SYSTEM,
                "Alertas do Sistema",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos importantes de conta, segurança e manutenção técnica."
                enableVibration(true)
            }

            // 4. Canal de Oportunidades & Corridas
            val ridesChannel = NotificationChannel(
                CHANNEL_RIDES,
                "Oportunidades & Praças em Alta",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas de alta demanda e picos de corrida na sua região."
                enableVibration(true)
            }

            notificationManager.createNotificationChannels(
                listOf(generalChannel, marketingChannel, systemChannel, ridesChannel)
            )
        }
    }
}
