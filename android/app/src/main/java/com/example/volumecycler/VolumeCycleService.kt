package com.example.volumecycler

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class VolumeCycleService : Service() {
    private val CHANNEL_ID = "VolumeCycleChannel"
    private var job: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Volume Cycler Running")
            .setContentText("Cycling volume every hour.")
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
            .build()
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        job = CoroutineScope(Dispatchers.Default).launch {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val stream = AudioManager.STREAM_MUSIC
            val maxVolume = audioManager.getStreamMaxVolume(stream)
            val volume30 = (maxVolume * 0.3).toInt().coerceAtLeast(1)
            while (isActive) {
                // Set to 30%
                audioManager.setStreamVolume(stream, volume30, 0)
                delay(6 * 60 * 1000L) // 6 minutes
                // Mute
                audioManager.setStreamVolume(stream, 0, 0)
                delay(54 * 60 * 1000L) // 54 minutes
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        job?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Volume Cycle Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
} 