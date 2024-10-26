package com.example.vanzareapartamente

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "apartment_channel"
        const val CHANNEL_NAME = "Apartment Notifications"
        const val CHANNEL_DESCRIPTION = "Напоминания о завершении оформления объявления"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("ReminderReceiver", "onReceive вызван: Будильник сработал.")
        sendReminderNotification(context)
    }

    // Создаем канал уведомлений
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Отправляем уведомление
    private fun sendReminderNotification(context: Context) {
        createNotificationChannel(context) // Создаем канал, если его еще нет

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_placeholder) // Проверьте, что иконка доступна
            .setContentTitle("Завершите объявление")
            .setContentText("Вы начали оформление объявления. Завершите его!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            notify(1, builder.build())
        }

        Log.d("ReminderReceiver", "Уведомление отправлено.")
    }
}