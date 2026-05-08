package com.example.intentionalai

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat

class BouncerService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var lastApp: String? = null
    private val checkInterval = 500L // Check every 0.5 seconds for instant response

    private val monitorRunnable = object : Runnable {
        override fun run() {
            checkForegroundApp()
            handler.postDelayed(this, checkInterval)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, "bouncer_channel")
            .setContentTitle("Bouncer Active")
            .setContentText("Guarding your attention...")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .build()
        
        startForeground(1, notification)
        handler.post(monitorRunnable)
    }

    private fun checkForegroundApp() {
        val currentApp = UsageTracker.getForegroundApp(this)
        if (currentApp != null && currentApp != packageName && currentApp != lastApp) {
            // Comprehensive list of distracting apps
            val distractingApps = listOf(
                "com.instagram.android", 
                "com.facebook.katana", 
                "com.twitter.android", 
                "com.zhiliaoapp.musically", // TikTok
                "com.snapchat.android",
                "com.google.android.youtube"
            )
            
            if (distractingApps.contains(currentApp)) {
                Log.d("Bouncer", "Distracting app detected: $currentApp")
                
                // Launch MainActivity as a full-screen barrier
                val intent = Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT) // Brings Bouncer to the VERY front
                    putExtra("trigger_app", currentApp)
                }
                startActivity(intent)
            }
            lastApp = currentApp
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "bouncer_channel",
                "Bouncer Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(monitorRunnable)
    }
}
