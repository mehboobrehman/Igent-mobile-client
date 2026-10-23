package com.igent.mobile.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.igent.mobile.MainActivity
import com.igent.mobile.R

class IgentFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM token: $token")
        // TODO: Send token to application server if user is logged in
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        val data = remoteMessage.data
        val notification = remoteMessage.notification

        val title = notification?.title ?: data["title"] ?: getString(R.string.app_name)
        val body = notification?.body ?: data["message"] ?: data["body"] ?: "You have a new update"
        val targetScreen = data["screen"] ?: data["type"] ?: "chat"

        sendNotification(title, body, targetScreen)
    }

    private fun sendNotification(title: String, messageBody: String, targetScreen: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(EXTRA_TARGET_SCREEN, targetScreen)
        }

        val requestCode = System.currentTimeMillis().toInt()
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = when (targetScreen.lowercase()) {
            "stocks", "stock" -> CHANNEL_ID_STOCKS
            "rss", "news" -> CHANNEL_ID_RSS
            else -> CHANNEL_ID_GENERAL
        }

        createNotificationChannels(this)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(requestCode, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "IgentFCMService"
        const val EXTRA_TARGET_SCREEN = "extra_target_screen"

        const val CHANNEL_ID_GENERAL = "igent_general_channel"
        const val CHANNEL_ID_STOCKS = "igent_stocks_channel"
        const val CHANNEL_ID_RSS = "igent_rss_channel"

        fun createNotificationChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val generalChannel = NotificationChannel(
                    CHANNEL_ID_GENERAL,
                    "General Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "General announcements and alerts"
                }

                val stocksChannel = NotificationChannel(
                    CHANNEL_ID_STOCKS,
                    "Stock Market Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "PSX price updates and market trends"
                }

                val rssChannel = NotificationChannel(
                    CHANNEL_ID_RSS,
                    "RSS Feed Alerts",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "New articles and news updates"
                }

                notificationManager.createNotificationChannels(
                    listOf(generalChannel, stocksChannel, rssChannel)
                )
            }
        }
    }
}
