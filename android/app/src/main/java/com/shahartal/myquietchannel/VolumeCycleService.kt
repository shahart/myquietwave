package com.shahartal.myquietchannel

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.crashlytics.crashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime

class VolumeCycleService : Service() {

    companion object {
        var isRunning = false
        var startHour = -1
        var startSeconds = 0
        const val MAX_NEWS_DURATION = PlaybackPolicy.MAX_NEWS_DURATION_MINUTES
        @Deprecated("Use MAX_NEWS_DURATION", ReplaceWith("MAX_NEWS_DURATION"))
        const val max_news_duration = MAX_NEWS_DURATION
        const val CHANNEL_ID = "VolumeCycleChannel"
    }

    private var job: Job? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)




    private var settedVolume = 0
    private var origVolume = 0

    private var mediaPlayer: MediaPlayer? = null

    lateinit var mainIntent: Intent
    var mainPendingIntent: PendingIntent? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true

        // @RequiresApi(8
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





    private fun getMediaPlayer(url: String?): MediaPlayer? {
        val stationUrl = Utils.getStationUrl(url)
        return MediaPlayer().apply {
            setDataSource(stationUrl)
//            setOnErrorListener(object : MediaPlayer.OnErrorListener {
//                override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
//                    // Log the error (e.g., what=1, extra=-2147483648)
//                    Log.e("myquietwave", "getMediaPlayer. Failed to stream from " + url + ". what=" + what)
//                    Firebase.crashlytics.log("ERROR. getMediaPlayer. Failed to stream from " + url + ". what=" + what) // saw length=1; index=2
////                    Firebase.crashlytics.recordException(e)
//
//                    val notification: Notification =
//                        NotificationCompat.Builder(this@VolumeCycleService, CHANNEL_ID)
//                            .setContentText("Failed to stream from " + url) // + ". " + e.toString())
//                            .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
//                            .setContentIntent(mainPendingIntent)
//                            .build()
//                    getSystemService(NotificationManager::class.java).notify(1, notification)
//
//                    mp.reset() // Move back to Idle state to recover
//                    mp.setDataSource(GLZ)
//
//                    return true // Return true if you handled the error
//                }
//            })
//            prepareAsync()
            try {
                prepare() // Async()
            }
            catch (e: Exception) {
                Log.e("myquietwave", "getMediaPlayer. Failed to stream from $stationUrl.", e)
                Firebase.crashlytics.log("ERROR. getMediaPlayer. Failed to stream from $stationUrl$e")
                Firebase.crashlytics.recordException(e)

                val notification: Notification =
                    NotificationCompat.Builder(this@VolumeCycleService, CHANNEL_ID)
                        .setContentText("Failed to stream from $stationUrl. $e")
                        .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                        .setContentIntent(mainPendingIntent)
                        .build()
                getSystemService(NotificationManager::class.java).notify(1, notification)
//
                return null // getMediaPlayer(GLZ)
            }
        }
    }

    private fun startStation(url: String) {
        mediaPlayer?.release()
        mediaPlayer = getMediaPlayer(url)
        mediaPlayer?.start()
    }

    // @RequiresApi(Build.VERSION_CODES.O) // Unnecessary; SDK_INT is always >= 26
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        job?.cancel()
        val thisService = this
        job = serviceScope.launch {

            val maxPercentage = 50

            val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            val stream = AudioManager.STREAM_MUSIC

            origVolume = audioManager.getStreamVolume(stream)

            val maxVolume = audioManager.getStreamMaxVolume(stream) // usually 15
            Log.i("myquietwave", "VolumeCycleService init volume $origVolume out of $maxVolume")

            var stationValue: String? = Station.GLGLZ.displayName
            var newsDurationValue = AppSettings.DEFAULT_NEWS_DURATION_MINUTES
            var radioOnlyValue = false
            var scheduleValue: String? = NewsSchedule.DEFAULT_TEXT
            if (intent == null) {
                Log.e("myquietwave", "VolumeCycleService intent is null")
                Firebase.crashlytics.log("VolumeCycleService intent is null")
            }
            else {
                newsDurationValue = intent.getIntExtra("newsDuration", AppSettings.DEFAULT_NEWS_DURATION_MINUTES)
                scheduleValue = intent.getStringExtra("nextHours")
                stationValue = intent.getStringExtra("station")
                radioOnlyValue = intent.getBooleanExtra("radioPlayer", false)
            }

            val now = ZonedDateTime.now(ZoneId.systemDefault())
            val config = PlaybackConfig.fromRawValues(
                station = stationValue,
                newsDurationMinutes = newsDurationValue,
                radioOnly = radioOnlyValue,
                scheduleText = scheduleValue,
                now = now,
            )
            val station = config.station
            val newsDuration = config.newsDurationMinutes
            val radioPlayer = config.radioOnly
            val schedule = config.schedule
            val isNearShabbath = PlaybackPolicy.isNearShabbat(now)

            Log.i("myquietwave", "VolumeCycleService settings: Station ${station.displayName} NewsDuration $newsDuration currentHour ${now.hour}")

            if (radioPlayer) {
                if (!isAudioPlaying()) {
                    if (mediaPlayer?.isPlaying != true) {
                        startStation(station.streamUrl)
                    }
                }
                val notification: Notification = NotificationCompat.Builder(thisService, CHANNEL_ID)
                    .setContentTitle(getString(R.string.notif_title_1)) // ""Volume Cycler Running")
                    .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setContentIntent(mainPendingIntent)
                    .build()
                getSystemService(NotificationManager::class.java).notify(1, notification)
            }
            
            while (isActive) {
                if (radioPlayer) {
                    delay(500)
                    continue
                }

                var volume50 = settedVolume //  (maxVolume * volume / 100).coerceAtLeast(1)
                if (isNearShabbath && volume50 > (maxVolume * maxPercentage / 100).coerceAtLeast(1) + 1) { // 0..15
                    volume50 = PlaybackPolicy.limitVolume(volume50, maxVolume, isNearShabbat = true) + 1
                    Log.w("myquietwave", "VolumeCycleService Volume crossed threshold")
                }
                if (volume50 == 0) {
                    volume50 = 4 // 2 out of 15 = 13% (maxVolume * 20 / 100).coerceAtLeast(1)
                }

                // Set to 20%
                Log.i("myquietwave",
                    "VolumeCycleService started positive volume: $volume50 out of $maxVolume, news duration [minutes] $newsDuration, is near shabbath $isNearShabbath"
                )

                if (!isAudioPlaying()) {
                    if (mediaPlayer?.isPlaying != true) {
                        startStation(station.streamUrl)
                    }
                }

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

                        if (!isAudioPlaying()) {
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
                            Log.d("myquietwave", "VolumeCycleService Limit the max volume")
                            audioManager.setStreamVolume(stream, currVolume, 0)
                        }
                        else if (currVolume == 0) {
                            currVolume = 1
                            Log.d("myquietwave", "VolumeCycleService No use to put the volume on zero")
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

                    if (!isAudioPlaying()) {
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

                    if (!isAudioPlaying()) {
                        if (mediaPlayer?.isPlaying != true) {
                            startStation(station.streamUrl)

                            Firebase.analytics.logEvent("PlayingGlzNews") {
                                param("currentHour", ZonedDateTime.now(ZoneId.systemDefault()).hour.toString())
                            }
                        }
                    }
                    else {
                        Firebase.analytics.logEvent("PlayingNews") {
                            param("currentHour", ZonedDateTime.now(ZoneId.systemDefault()).hour.toString())
                        }
                    }

                    for (i in 1..newsDuration) {

                        val notification: Notification =
                            NotificationCompat.Builder(thisService, CHANNEL_ID)
                                .setContentTitle(getString(R.string.notif_title_1))
                                .setContentText(if (newsDuration - i + 1 > 1) getString(R.string.notif_text_4,newsDuration - i + 1, 100*audioManager.getStreamVolume(stream)/maxVolume) else getString(R.string.notif_text_4_1, 100*audioManager.getStreamVolume(stream)/maxVolume))
                                .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                                .setOngoing(true)
                                .setOnlyAlertOnce(true)
                                .setContentIntent(mainPendingIntent)
                                .build()
                        getSystemService(NotificationManager::class.java).notify(1, notification)

                        delay(60 * 1000L) // 6 minutes
                    }
                }
                // @RequiresApi(8
                val now = ZonedDateTime.now(ZoneId.systemDefault()).minute
                // continue the current news
                if (now < newsDuration) {
                    Log.d("myquietwave", "VolumeCycleService continue positive volume, more delay (as part of news duration) [minutes] " + (newsDuration - now))

                    for (i in 1..newsDuration - now) {
                        val notification: Notification =
                            NotificationCompat.Builder(thisService, CHANNEL_ID)
                                .setContentTitle(getString(R.string.notif_title_1))
                                .setContentText(if (newsDuration - i + 1 > 1) getString(R.string.notif_text_4,newsDuration - i + 1, 100*audioManager.getStreamVolume(stream)/maxVolume) else getString(R.string.notif_text_4_1, 100*audioManager.getStreamVolume(stream)/maxVolume))
                                .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                                .setOngoing(true)
                                .setOnlyAlertOnce(true)
                                .setContentIntent(mainPendingIntent)
                                .build()
                        getSystemService(NotificationManager::class.java).notify(1, notification)

                        delay(60 * 1000L)
                    }
                }

                // this happens only in the Init
                if (settedVolume == 0) {
                    settedVolume = audioManager.getStreamVolume(stream)
                    Log.i("myquietwave", "VolumeCycleService setted volume $settedVolume out of $maxVolume")
                    if (settedVolume == 0) {
                        settedVolume = 3 // (maxVolume * 15 / 100).coerceAtLeast(1) // let's start with 1 or 2
                    }
                }

                val notification: Notification = NotificationCompat.Builder(thisService, CHANNEL_ID)
                    .setContentTitle(getString(R.string.notif_title_1))
                    .setContentText(getString(R.string.notif_text_5))
                    .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setContentIntent(mainPendingIntent)
                    .build()
                getSystemService(NotificationManager::class.java).notify(1, notification)

                val currVolume = audioManager.getStreamVolume(stream)
                if (currVolume != settedVolume) {
                    Log.w("myquietwave", "VolumeCycleService User has changed the volume manually during the news from $settedVolume to $currVolume")
                    settedVolume = currVolume
                }

                // infoText.text = "6"
                // Mute
                Log.d("myquietwave","VolumeCycleService started zero volume") // , delay till the next news [minutes] $nextDelay") //  + currentVolume)
                audioManager.setStreamVolume(stream, 0, 0)

                mediaPlayer?.release()
                mediaPlayer = null

                var oldText = ""

                // delay(30_000)

                // WAS: for (i in 1..(nextDelay-1)*2) {
                while (true) {
                    if (schedule.isDue(ZonedDateTime.now(ZoneId.systemDefault()), startSeconds)) {
                        break
                    }
                    delay(1_000)
                    if (ZonedDateTime.now(ZoneId.systemDefault()).second % 2 == 0) {
                        val audioIsPlaying = isAudioPlaying()
                        val text = if (!audioIsPlaying) {
                            getString(R.string.notif_text_radio_stopped)
                        } else {
                            getString(R.string.notif_text_5)
                        }

                        if (oldText != text) {
                            val notification: Notification =
                                NotificationCompat.Builder(thisService, CHANNEL_ID)
                                    .setContentTitle(getString(R.string.notif_title_1))
                                    .setContentText(text)
                                    .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                                    .setOngoing(true)
                                    .setOnlyAlertOnce(true)
                                    .setContentIntent(mainPendingIntent)
                                    .build()
                            getSystemService(NotificationManager::class.java).notify(1, notification)
                        }

                        oldText = text
                    }

                    // infoText.text = "54"
                }
            }
            Log.w("myquietwave", "VolumeCycleService not active any more")
        }
        return START_STICKY // TODO? START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val stream = AudioManager.STREAM_MUSIC
        Log.d("myquietwave", "VolumeCycleService on destroy, volume back to " + origVolume + " from " + audioManager.getStreamVolume(stream) + " out of " +  audioManager.getStreamMaxVolume(stream))
        audioManager.setStreamVolume(stream, origVolume, 0)

        job?.cancel()
        serviceScope.coroutineContext[Job]?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
        isRunning = false
        startHour = -1
        startSeconds = 0
        getSystemService(NotificationManager::class.java).cancel(1)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // @RequiresApi(8
    // @RequiresApi(Build.VERSION_CODES.O)
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

    private fun isAudioPlaying(): Boolean {
        val audioManager = this.getSystemService(AUDIO_SERVICE) as AudioManager
        val isPlaying = audioManager.isMusicActive()
        return isPlaying
    }
}
