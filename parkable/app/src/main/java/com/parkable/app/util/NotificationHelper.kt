package com.parkable.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.parkable.app.R

object NotificationHelper {
    private const val CHANNEL_ID = "parkable_bookings"
    private var appContext: Context? = null
    private var nextId = 1

    fun init(context: Context) {
        appContext = context.applicationContext
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Reservas y avisos",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Notificaciones de reservas y plazas" }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun showBookingConfirmed(title: String, address: String) {
        post("¡Reserva confirmada!", "Has reservado: $title — $address")
    }

    private fun post(title: String, body: String) {
        val ctx = appContext ?: return
        val notification = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        ctx.getSystemService(NotificationManager::class.java).notify(nextId++, notification)
    }
}