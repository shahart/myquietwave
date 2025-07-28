package com.shahartal.myquietchannel

//import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioManager
//import android.os.Build
import android.os.IBinder
import android.util.Log
//import android.widget.TextView
//import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import kotlinx.coroutines.*
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime

class VolumeCycleService : Service() {

    companion object {
        var isRunning = false
        var startHour = -1
        var startSeconds = 0
        const val max_news_duration = 59
        const val CHANNEL_ID = "VolumeCycleChannel"
    }

    private var job: Job? = null

    private var minutesPerHours = 60

    private var settedVolume = 0
    private var origVolume = 0

    var mainIntent = Intent(this, MainActivity::class.java)
    var mainPendingIntent: PendingIntent? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        startHour = ZonedDateTime.now(ZoneId.systemDefault()).hour
        startSeconds = ZonedDateTime.now(ZoneId.systemDefault()).second

        createNotificationChannel()

        mainIntent = Intent(this, MainActivity::class.java)

        mainPendingIntent = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(mainIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notif_title_1)) // ""Volume Cycler Running")
            .setContentText(getString(R.string.notif_text_1)) // ""Cycling volume every X hours.")
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(mainPendingIntent)
            .build()
        startForeground(1, notification)
    }

    // @RequiresApi(Build.VERSION_CODES.O) // Unnecessary; SDK_INT is always >= 26
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val thisService = this
        job = CoroutineScope(Dispatchers.Default).launch {

            val maxPercentage = 50

            val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            val stream = AudioManager.STREAM_MUSIC

            origVolume = audioManager.getStreamVolume(stream)

            val maxVolume = audioManager.getStreamMaxVolume(stream) // usually 15
            Log.i("", "VolumeCycleService init volume $origVolume out of $maxVolume")
            // val currentVolume = audioManager.getStreamVolume(stream)

            var isTest5 = false
            var newsDuration = 4
            var hours = 4
            if (intent == null) {
                Log.e("", "VolumeCycleService intent is null")
            }
            else {
                isTest5 = ("test5" == intent.getStringExtra("todoList"))
                newsDuration = intent.getIntExtra("newsDuration", 4)
                hours = intent.getIntExtra("hours", 4)
                Log.i("", "VolumeCycleService Initial input: NewsDuration $newsDuration everyHours $hours isTest5 $isTest5")
            }

            val isNearShabbath = isTest5
                || (
                ZonedDateTime.now(ZoneId.systemDefault()).dayOfWeek == DayOfWeek.FRIDAY &&
                    ZonedDateTime.now(ZoneId.systemDefault()).hour >= 12)

            if (newsDuration > max_news_duration) {
                newsDuration = max_news_duration
            }
            if (isNearShabbath && newsDuration > 6) {
                newsDuration = 6
            }
            if (newsDuration < 1) {
                newsDuration = 1
            }

            if (isTest5) {
                minutesPerHours = 5
                Log.w("", "VolumeCycleService test mode 5 minutes")
                if (newsDuration > minutesPerHours-1) {
                    Log.w("", "VolumeCycleService newsDuration > 5")
                }
            }

//            var volume = intent.getIntExtra("volume", 2)
//            if (volume > audioManager.getStreamMaxVolume(stream)) volume = 2
//            if (volume < 1) volume = 1

            if (hours > 12) {
                hours = 12
            }
            if (hours < 1) {
                hours = 1
            }

            Log.i("", "VolumeCycleService settings: NewsDuration $newsDuration everyHours $hours")

            val initSeconds = ZonedDateTime.now(ZoneId.systemDefault()).second

            while (isActive) {

                var volume50 = settedVolume //  (maxVolume * volume / 100).coerceAtLeast(1)
                if (isNearShabbath && volume50 > (maxVolume * maxPercentage / 100).coerceAtLeast(1) + 1) { // 0..15
                    volume50 = (maxVolume * maxPercentage / 100).coerceAtLeast(1) + 1
                    Log.w("", "VolumeCycleService Volume crossed threshold")
                }
                if (volume50 == 0) {
                    volume50 = 3 // 2 out of 15 = 13% (maxVolume * 20 / 100).coerceAtLeast(1)
                }

                // Set to 20%
                Log.i("",
                    "VolumeCycleService started positive volume: $volume50 out of $maxVolume, news duration [minutes] $newsDuration, is near shabbath $isNearShabbath"
                )


                audioManager.setStreamVolume(stream, volume50, 0)

                if (settedVolume == 0) {
                    // delay(30 * 1000L) // first set of volume by the user

                    for (i in 1..60) { // = 30 seconds

                        val notification: Notification = NotificationCompat.Builder(thisService, CHANNEL_ID)
                            .setContentTitle(getString(R.string.notif_title_2))
                            .setContentText(getString(R.string.notif_text_2, ((61-i)/2), 100*audioManager.getStreamVolume(stream)/maxVolume))
                            .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                            .setOngoing(true)
                            .setOnlyAlertOnce(true)
                            .setContentIntent(mainPendingIntent)
                            .build()
                        getSystemService(NotificationManager::class.java).notify(1, notification)

                        delay(500)

                        if (! alertMediaIsPlaying("500msec")) {
                            // getSystemService(NotificationManager::class.java).cancel(1)
                            // break
                            val notification: Notification = NotificationCompat.Builder(thisService, CHANNEL_ID)
                                .setContentTitle(getString(R.string.notif_title_1))
                                .setContentText(getString(R.string.notif_text_radio_stopped))
                                .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                                .setOngoing(true)
                                .setOnlyAlertOnce(true)
                                .setContentIntent(mainPendingIntent)
                                .build()
                            getSystemService(NotificationManager::class.java).notify(1, notification)
                            // break
                        }

                        var currVolume = audioManager.getStreamVolume(stream)
                        if (isNearShabbath && currVolume > (maxVolume * maxPercentage / 100).coerceAtLeast(1)) { // 0..15
                            currVolume = (maxVolume * maxPercentage / 100).coerceAtLeast(1)
                            Log.d("", "VolumeCycleService Limit the max volume")
                            audioManager.setStreamVolume(stream, currVolume, 0)
                        }
                        else if (currVolume == 0) {
                            currVolume = 1
                            Log.d("", "VolumeCycleService No use to put the volume on zero")
                            audioManager.setStreamVolume(stream, currVolume, 0)
                        }

                    }

                    val notification: Notification = NotificationCompat.Builder(thisService, CHANNEL_ID)
                        .setContentTitle(getString(R.string.notif_title_1))
                        .setContentText(getString(R.string.notif_text_1))
                        .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                        .setOngoing(true)
                        .setOnlyAlertOnce(true)
                        .setContentIntent(mainPendingIntent)
                        .build()
                    getSystemService(NotificationManager::class.java).notify(1, notification)

                    if (! alertMediaIsPlaying("done set the volume")) {
                        // getSystemService(NotificationManager::class.java).cancel(1)
                        val notification: Notification = NotificationCompat.Builder(thisService, CHANNEL_ID)
                            .setContentTitle(getString(R.string.notif_title_1))
                            .setContentText(getString(R.string.notif_text_radio_stopped))
                            .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                            .setOngoing(true)
                            .setOnlyAlertOnce(true)
                            .setContentIntent(mainPendingIntent)
                            .build()
                        getSystemService(NotificationManager::class.java).notify(1, notification)
                    }

                    // delay(30_000)

                }
                else {

                    alertMediaIsPlaying("playing news")

                    for (i in 1..newsDuration) {

                        val notification: Notification =
                            NotificationCompat.Builder(thisService, CHANNEL_ID)
                                .setContentTitle(getString(R.string.notif_title_1))
                                .setContentText(getString(R.string.notif_text_4,newsDuration - i + 1, 100*audioManager.getStreamVolume(stream)/maxVolume))
                                .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                                .setOngoing(true)
                                .setOnlyAlertOnce(true)
                                .setContentIntent(mainPendingIntent)
                                .build()
                        getSystemService(NotificationManager::class.java).notify(1, notification)

                        delay(60 * 1000L) // 6 minutes
                    }
                }
                var now = ZonedDateTime.now(ZoneId.systemDefault()).minute
                // continue the current news
                if (now < newsDuration) {
                    Log.d("", "VolumeCycleService continue positive volume, more delay (as part of news duration) [minutes] " + (newsDuration - now))

                    for (i in 1..newsDuration - now) {
                        val notification: Notification =
                            NotificationCompat.Builder(thisService, CHANNEL_ID)
                                .setContentTitle(getString(R.string.notif_title_1))
                                .setContentText(getString(R.string.notif_text_4,newsDuration - i + 1, 100*audioManager.getStreamVolume(stream)/maxVolume))
                                .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                                .setOngoing(true)
                                .setOnlyAlertOnce(true)
                                .setContentIntent(mainPendingIntent)
                                .build()
                        getSystemService(NotificationManager::class.java).notify(1, notification)

                        delay(60 * 1000L)
                    }
                }

                var nextDelay = (minutesPerHours * hours - newsDuration)

                now = ZonedDateTime.now(ZoneId.systemDefault()).hour
                var nextHour = now

                // this happens only in the Init
                if (settedVolume == 0) {
                    settedVolume = audioManager.getStreamVolume(stream)
                    Log.i("", "VolumeCycleService setted volume $settedVolume out of $maxVolume")
                    if (settedVolume == 0) {
                        settedVolume = 3 // (maxVolume * 15 / 100).coerceAtLeast(1) // let's start with 1 or 2
                    }
                    now = ZonedDateTime.now(ZoneId.systemDefault()).minute
                    nextDelay = minutesPerHours - now

                    if (minutesPerHours != 60) {
                        nextDelay = minutesPerHours - now % minutesPerHours
                    }

                    nextHour += 1
                }
                else {
                    nextHour += hours
                }

                if (nextHour > 24) {
                    nextHour -= 24
                }

                val notification: Notification = NotificationCompat.Builder(thisService, CHANNEL_ID)
                    .setContentTitle(getString(R.string.notif_title_1))
                    .setContentText(getString(R.string.notif_text_5, "" + nextHour + ":00:" + (if (initSeconds < 10) "0$initSeconds" else initSeconds)))
                    .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setContentIntent(mainPendingIntent)
                    .build()
                getSystemService(NotificationManager::class.java).notify(1, notification)

                val currVolume = audioManager.getStreamVolume(stream)
                if (currVolume != settedVolume) {
                    Log.w("", "VolumeCycleService User has changed the volume manually during the news from $settedVolume to $currVolume")
                    settedVolume = currVolume
                }

                // infoText.text = "6"
                // Mute
                Log.d("","VolumeCycleService started zero volume, delay till the next news [minutes] $nextDelay") //  + currentVolume)
                audioManager.setStreamVolume(stream, 0, 0)

                // delay(30_000)

                // WAS: for (i in 1..(nextDelay-1)*2) {
                while (
                    (ZonedDateTime.now(ZoneId.systemDefault()).minute != 0 || (isTest5 && ZonedDateTime.now(ZoneId.systemDefault()).minute % 5 != 0)) &&
                       ZonedDateTime.now(ZoneId.systemDefault()).hour != nextHour)
                {
                    if (ZonedDateTime.now(ZoneId.systemDefault()).minute == 0 &&
                        ZonedDateTime.now(ZoneId.systemDefault()).hour == nextHour &&
                        (ZonedDateTime.now(ZoneId.systemDefault()).second >= startSeconds ||
                        ZonedDateTime.now(ZoneId.systemDefault()).second >= 30)) {
                        break
                    }
                    delay(30 * 1000L) // 54 minutes
                    var text: String
                    text = if (! alertMediaIsPlaying("waiting for news")) {
                        // cancel("VolumeCycleService media was stopped, cancelling", null)
                        // break
                        getString(R.string.notif_text_radio_stopped)
                    } else {
                        getString(R.string.notif_text_5, "" + nextHour + ":00:" + (if (initSeconds < 10) "0$initSeconds" else initSeconds))
                    }

                    val notification: Notification = NotificationCompat.Builder(thisService, CHANNEL_ID)
                        .setContentTitle(getString(R.string.notif_title_1))
                        .setContentText(text)
                        .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                        .setOngoing(true)
                        .setOnlyAlertOnce(true)
                        .setContentIntent(mainPendingIntent)
                        .build()
                    getSystemService(NotificationManager::class.java).notify(1, notification)

                    // infoText.text = "54"
                }
            }
            Log.w("", "VolumeCycleService not active any more")
        }
        return START_STICKY // TODO? START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val stream = AudioManager.STREAM_MUSIC
        Log.d("", "VolumeCycleService on destroy, volume back to " + origVolume + " from " + audioManager.getStreamVolume(stream) + " out of " +  audioManager.getStreamMaxVolume(stream))
        audioManager.setStreamVolume(stream, origVolume, 0)

        job?.cancel()
        super.onDestroy()
        isRunning = false
        startHour = -1
        startSeconds = 0
        getSystemService(NotificationManager::class.java).cancel(1)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Unnecessary; SDK_INT is always >= 26
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Volume Cycle Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        // }
    }

    fun alertMediaIsPlaying(from: String): Boolean {
        val audioManager = this.getSystemService(AUDIO_SERVICE) as AudioManager
        val isPlaying = audioManager.isMusicActive()
        if (! isPlaying) {
            Log.w("", "VolumeCycleService isPlaying: $isPlaying from $from")
//
//            val alertDialog = AlertDialog.Builder(this).create()
//            alertDialog.setTitle("האם רדיו מנוגן?" + isPlaying)
//            alertDialog.setMessage("האפליקציה לא מנגנת בעצמה את הרדיו, עליכם מראש לבחור ניגון כלשהו")
//            alertDialog.setNegativeButton(getString(R.string.close_alert)) { dialog: DialogInterface?, which: Int ->
//                dialog!!.cancel()
//            }
//            // alertDialog.setIcon(R.drawable.icon)
//            alertDialog.show()
        }
        return isPlaying
    }
}