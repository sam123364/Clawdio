package com.example.intentionalai

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationInterceptorService : NotificationListenerService() {

    private val apiService = ApiService("http://192.168.0.125:18791") // Points to local OpenClaw backend

    override fun onListenerConnected() {
        super.onListenerConnected()
        createNotificationChannel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return

        val packageName = sbn.packageName
        
        // Ignore our own notifications to prevent infinite loops
        if (packageName == applicationContext.packageName) return
        
        // Ignore generic system UI notifications
        if (packageName == "android" || packageName == "com.android.systemui") return

        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: "Unknown Sender"
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        // Cancel the original notification so the user doesn't see it (The "Bouncer" effect)
        // Note: For this to work perfectly, the user's phone must be on Do Not Disturb, 
        // OR the app can forcefully cancel it (if Android allows it for that specific app).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cancelNotification(sbn.key)
        }

        // Send to backend for evaluation
        evaluateNotificationUrgency(packageName, title, text)
    }

    private fun evaluateNotificationUrgency(appName: String, sender: String, message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Send standard POST request to our new endpoint
                val payload = """
                    {
                        "app": "${appName.replace("\"", "")}",
                        "sender": "${sender.replace("\"", "")}",
                        "message": "${message.replace("\"", "")}"
                    }
                """.trimIndent()
                
                // Using the exact same method as ApiService.postRequest but simplified here
                val responseObj = apiService.sendNotificationRaw(payload)
                
                if (responseObj != null) {
                    val isUrgent = responseObj.getBoolean("urgent")
                    if (isUrgent) {
                        val alertMsg = responseObj.getString("alertMessage")
                        triggerUrgentAlarm(alertMsg)
                    }
                    // If not urgent, the backend silently logged it in NOTIFICATIONS.md
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun triggerUrgentAlarm(message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create a High Priority Notification that bypasses Do Not Disturb
        val builder = NotificationCompat.Builder(this, "URGENT_CHANNEL")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🚨 URGENT Assistant Alert")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "URGENT_CHANNEL",
                "Urgent Assistant Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Bypasses DND for urgent intercepted messages."
                setBypassDnd(true) // Crucial for breaking through Do Not Disturb
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
