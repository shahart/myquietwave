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
        const val NOTIFICATION_ID = 1
        const val EXTRA_NEWS_DURATION = "newsDuration"
        const val EXTRA_NEXT_HOURS = "nextHours"
        const val EXTRA_STATION = "station"
        const val EXTRA_TODO_LIST = "todoList"
        const val EXTRA_LOCATION = "location"
        const val EXTRA_RADIO_PLAYER = "radioPlayer"
    }

    private var job: Job? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var configuredVolume = 0
    private var origVolume = 0

    private var mediaPlayer: MediaPlayer? = null

    private lateinit var mainPendingIntent: PendingIntent

    override fun onCreate() {
        super.onCreate()
        isRunning = true

        // @RequiresApi(8
        startHour = ZonedDateTime.now(ZoneId.systemDefault()).hour
        startSeconds = ZonedDateTime.now(ZoneId.systemDefault()).second

        createNotificationChannel()

        mainPendingIntent = requireNotNull(TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(Intent(this@VolumeCycleService, MainActivity::class.java))
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        })

        startForeground(
            NOTIFICATION_ID,
            buildNotification(
                title = getString(R.string.notif_title_1),
                text = getString(R.string.notif_text_1),
            ),
        )
    }

    private fun buildNotification(
        title: String? = null,
        text: String? = null,
        ongoing: Boolean = true,
        onlyAlertOnce: Boolean = true,
    ): Notification = NotificationCompat.Builder(this, CHANNEL_ID)
        .apply {
            title?.let(::setContentTitle)
            text?.let(::setContentText)
        }
        .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
        .setOngoing(ongoing)
        .setOnlyAlertOnce(onlyAlertOnce)
        .setContentIntent(mainPendingIntent)
        .build()

    private fun updateNotification(text: String?, title: String = getString(R.string.notif_title_1)) {
        getSystemService(NotificationManager::class.java).notify(
            NOTIFICATION_ID,
            buildNotification(title = title, text = text),
        )
    }

    private fun getMediaPlayer(url: String?): MediaPlayer? {
        val stationUrl = Utils.getStationUrl(url)
        val player = MediaPlayer()
        return try {
            player.setDataSource(stationUrl)
            player.prepare()
            player
        } catch (e: Exception) {
            Log.e("myquietwave", "getMediaPlayer. Failed to stream from $stationUrl.", e)
            Firebase.crashlytics.log("ERROR. getMediaPlayer. Failed to stream from $stationUrl$e")
            Firebase.crashlytics.recordException(e)
            getSystemService(NotificationManager::class.java).notify(
                NOTIFICATION_ID,
                buildNotification(
                    text = "Failed to stream from $stationUrl. $e",
                    ongoing = false,
                    onlyAlertOnce = false,
                ),
            )
            player.release()
            null
        }
    }

    private fun startStation(url: String) {
        mediaPlayer?.release()
        mediaPlayer = getMediaPlayer(url)
        mediaPlayer?.start()
    }

    private fun startStationIfNeeded(station: Station): Boolean {
        if (isAudioPlaying() || mediaPlayer?.isPlaying == true) return false
        startStation(station.streamUrl)
        return true
    }

    // @RequiresApi(Build.VERSION_CODES.O) // Unnecessary; SDK_INT is always >= 26
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        job?.cancel()
        job = serviceScope.launch {
            val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            val stream = AudioManager.STREAM_MUSIC

            origVolume = audioManager.getStreamVolume(stream)

            val maxVolume = audioManager.getStreamMaxVolume(stream) // usually 15
            val shabbatVolumeLimit = PlaybackPolicy.shabbatVolumeLimit(maxVolume)
            Log.i("myquietwave", "VolumeCycleService init volume $origVolume out of $maxVolume")

            if (intent == null) {
                Log.e("myquietwave", "VolumeCycleService intent is null")
                Firebase.crashlytics.log("VolumeCycleService intent is null")
            }
            val intentValues = PlaybackIntentValues.fromIntent(intent)

            val now = ZonedDateTime.now(ZoneId.systemDefault())
            val config = PlaybackConfig.fromRawValues(
                station = intentValues.station,
                newsDurationMinutes = intentValues.newsDurationMinutes,
                radioOnly = intentValues.radioOnly,
                scheduleText = intentValues.scheduleText,
                now = now,
            )
            val station = config.station
            val newsDuration = config.newsDurationMinutes
            val radioPlayer = config.radioOnly
            val schedule = config.schedule
            val isNearShabbat = PlaybackPolicy.isNearShabbat(now)

            Log.i("myquietwave", "VolumeCycleService settings: Station ${station.displayName} NewsDuration $newsDuration currentHour ${now.hour}")

            if (radioPlayer) {
                startStationIfNeeded(station)
                updateNotification(text = null)
            }
            
            while (isActive) {
                if (radioPlayer) {
                    delay(500)
                    continue
                }

                var volume50 = configuredVolume //  (maxVolume * volume / 100).coerceAtLeast(1)
                if (isNearShabbat && volume50 > shabbatVolumeLimit + 1) { // 0..15
                    volume50 = PlaybackPolicy.limitVolume(volume50, maxVolume, isNearShabbat = true) + 1
                    Log.w("myquietwave", "VolumeCycleService Volume crossed threshold")
                }
                if (volume50 == 0) {
                    volume50 = 4 // 2 out of 15 = 13% (maxVolume * 20 / 100).coerceAtLeast(1)
                }

                // Set to 20%
                Log.i("myquietwave",
                    "VolumeCycleService started positive volume: $volume50 out of $maxVolume, news duration [minutes] $newsDuration, is near shabbath $isNearShabbat"
                )

                startStationIfNeeded(station)

                audioManager.setStreamVolume(stream, volume50, 0)

                if (configuredVolume == 0) {
                    // delay(30 * 1000L) // first set of volume by the user

                    for (i in 1..60) { // = 30 seconds

                        updateNotification(
                            title = getString(R.string.notif_title_2),
                            text = getString(
                                R.string.notif_text_2,
                                (61 - i) / 2,
                                100 * audioManager.getStreamVolume(stream) / maxVolume,
                            ),
                        )

                        delay(500)

                        if (!isAudioPlaying()) {
                            updateNotification(getString(R.string.notif_text_radio_stopped))

                            // break
                        }

                        var currVolume = audioManager.getStreamVolume(stream)
                        if (isNearShabbat && currVolume > shabbatVolumeLimit) { // 0..15
                            currVolume = shabbatVolumeLimit
                            Log.d("myquietwave", "VolumeCycleService Limit the max volume")
                            audioManager.setStreamVolume(stream, currVolume, 0)
                        }
                        else if (currVolume == 0) {
                            currVolume = 1
                            Log.d("myquietwave", "VolumeCycleService No use to put the volume on zero")
                            audioManager.setStreamVolume(stream, currVolume, 0)
                        }

                    }

                    updateNotification(getString(R.string.notif_text_1))

                    if (!isAudioPlaying()) {
                        updateNotification(getString(R.string.notif_text_radio_stopped))
                    }

                    // delay(30_000)

                }
                else {

                    if (isAudioPlaying()) {
                        Firebase.analytics.logEvent("PlayingNews") {
                            param("currentHour", ZonedDateTime.now(ZoneId.systemDefault()).hour.toString())
                        }
                    } else if (startStationIfNeeded(station)) {
                        Firebase.analytics.logEvent("PlayingGlzNews") {
                            param("currentHour", ZonedDateTime.now(ZoneId.systemDefault()).hour.toString())
                        }
                    }

                    for (i in 1..newsDuration) {

                        updateNotification(
                            newsCountdownText(
                                remainingMinutes = newsDuration - i + 1,
                                volumePercent = 100 * audioManager.getStreamVolume(stream) / maxVolume,
                            ),
                        )

                        delay(60 * 1000L) // 6 minutes
                    }
                }
                // @RequiresApi(8
                val now = ZonedDateTime.now(ZoneId.systemDefault()).minute
                // continue the current news
                if (now < newsDuration) {
                    Log.d("myquietwave", "VolumeCycleService continue positive volume, more delay (as part of news duration) [minutes] " + (newsDuration - now))

                    for (i in 1..newsDuration - now) {
                        updateNotification(
                            newsCountdownText(
                                remainingMinutes = newsDuration - i + 1,
                                volumePercent = 100 * audioManager.getStreamVolume(stream) / maxVolume,
                            ),
                        )

                        delay(60 * 1000L)
                    }
                }

                // this happens only in the Init
                if (configuredVolume == 0) {
                    configuredVolume = audioManager.getStreamVolume(stream)
                    Log.i("myquietwave", "VolumeCycleService configured volume $configuredVolume out of $maxVolume")
                    if (configuredVolume == 0) {
                        configuredVolume = 3 // (maxVolume * 15 / 100).coerceAtLeast(1) // let's start with 1 or 2
                    }
                }

                updateNotification(getString(R.string.notif_text_5))

                val currVolume = audioManager.getStreamVolume(stream)
                if (currVolume != configuredVolume) {
                    Log.w("myquietwave", "VolumeCycleService User has changed the volume manually during the news from $configuredVolume to $currVolume")
                    configuredVolume = currVolume
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
                            updateNotification(text)
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
        getSystemService(NotificationManager::class.java).cancel(NOTIFICATION_ID)
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

    private fun newsCountdownText(remainingMinutes: Int, volumePercent: Int): String =
        if (remainingMinutes > 1) {
            getString(R.string.notif_text_4, remainingMinutes, volumePercent)
        } else {
            getString(R.string.notif_text_4_1, volumePercent)
        }

    private fun isAudioPlaying(): Boolean =
        (getSystemService(AUDIO_SERVICE) as AudioManager).isMusicActive
}
