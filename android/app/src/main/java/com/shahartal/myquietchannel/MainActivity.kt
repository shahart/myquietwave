package com.shahartal.myquietchannel

//import android.R
import android.Manifest
import android.app.AlertDialog
//import android.app.Notification
//import android.app.NotificationChannel
import android.app.NotificationManager
//import android.content.Context
import android.content.Intent
import android.media.AudioManager
//import android.media.MediaMetadata
//import android.media.session.MediaController
//import android.media.session.MediaSessionManager
//import android.media.session.PlaybackState
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.shahartal.myquietchannel.BuildConfig

class MainActivity : ComponentActivity() {

    private var isServiceRunning = false

    private lateinit var statusText: TextView
    private lateinit var toggleButton: Button

    private lateinit var editTextNumberNewsDuration: TextView
    // private lateinit var editTextNumberVolume: TextView
    private lateinit var editTextNumberEveryHour: TextView
    private lateinit var textViewNextNews: TextView

    private lateinit var textViewNewsGlz: TextView
    private lateinit var textViewNewsKan: TextView

    private lateinit var textViewClock : TextView

    private lateinit var editTextTodo : TextView

    override fun onResume() {
        super.onResume()

        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val savedName = sharedPreferences.getString("todoList", "פלטה, מיחם, שעון שבת, מזגן")

        editTextTodo.text = savedName

        var newsDuration = sharedPreferences.getInt("newsDuration", 4)
        if (newsDuration > VolumeCycleService.max_news_duration) newsDuration = VolumeCycleService.max_news_duration
        if (newsDuration < 1) newsDuration = 1
        editTextNumberNewsDuration.text = newsDuration.toString()

        var everyHour = sharedPreferences.getInt("everyHour", 4)
        if (everyHour > 12) everyHour = 12
        if (everyHour < 1) everyHour = 1
        editTextNumberEveryHour.text = everyHour.toString()

        textViewNextNews.text = getEveryHourStr()

        val serviceIntent = Intent(this, VolumeCycleService::class.java)
        if (VolumeCycleService.isRunning) {
            if (! alertMediaIsPlaying("onResume")) {
                stopService(serviceIntent)
                isServiceRunning = false
            }
        }
        else {
            stopService(serviceIntent)
            isServiceRunning = false
        }

        if (isServiceRunning) {
            statusText.text = getString(R.string.title_name_enabled, BuildConfig.VERSION_NAME)
            toggleButton.text = getString(R.string.stop)
            toggleButton.setBackgroundColor(Color.Green.toArgb())
        }
        else {
            statusText.text = getString(R.string.title_name_disabled, BuildConfig.VERSION_NAME)
            toggleButton.text = getString(R.string.start)
            toggleButton.setBackgroundColor(Color.White.toArgb())
        }

        editTextNumberNewsDuration.isEnabled = ! isServiceRunning
        editTextNumberNewsDuration.isClickable = ! isServiceRunning

        editTextNumberEveryHour.isEnabled = ! isServiceRunning
        editTextNumberEveryHour.isClickable = ! isServiceRunning

        getSystemService(NotificationManager::class.java).cancel(1)

    }

    override fun onPause() {
        super.onPause()

        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("todoList", editTextTodo.text.toString())

        val newsDurationStr = editTextNumberNewsDuration.text.toString()
        var newsDuration = if (newsDurationStr.isEmpty()) 4 else newsDurationStr.toInt()
        if (newsDuration > VolumeCycleService.max_news_duration) newsDuration = VolumeCycleService.max_news_duration
        if (newsDuration < 1) newsDuration = 1
        editor.putInt("newsDuration", newsDuration)

        val everyHourStr = editTextNumberEveryHour.text.toString()
        var everyHour = if (everyHourStr.isEmpty()) 4 else everyHourStr.toInt()
        if (everyHour > 12) everyHour = 12
        if (everyHour < 1) everyHour = 1
        editor.putInt("everyHour", everyHour)

        editor.apply()

    }

    fun getEveryHourStr(): String {
        var everyHoursStr = editTextNumberEveryHour.text.toString()
        if (everyHoursStr == "") {
            everyHoursStr = "4"
            editTextNumberEveryHour.text = "4"
        }
        var everyHours = Integer.valueOf(everyHoursStr)
        if (everyHours > 12) {
            everyHours = 12
            editTextNumberEveryHour.text = "12"
        }
        if (everyHours < 1) {
            everyHours = 1
            editTextNumberEveryHour.text = "1"
        }

        val now = java.time.ZonedDateTime.now(java.time.ZoneId.systemDefault()).hour
        var nextHour = now + 1 //  + everyHours
        if (nextHour > 24) nextHour -= 24
        // Log.d("", "MainActivity nextHour: " + nextHour)

        var nextHoursStr = // "החדשות הבאות תהיינה בשעות " +
            nextHour.toString()

        var plusHours = everyHours

        nextHour += everyHours
        if (nextHour > 24) nextHour -= 24
        nextHoursStr += ", $nextHour"

        while (plusHours < 24) {
            nextHour += everyHours
            plusHours += everyHours
            if (nextHour > 24) nextHour -= 24
            nextHoursStr += ", $nextHour"
        }

        return nextHoursStr
    }

    // @RequiresApi(Build.VERSION_CODES.O) // Unnecessary; SDK_INT is always >= 26
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i("", "MainActivity Version " + BuildConfig.VERSION_NAME)

        setContentView(R.layout.activity_main)

        alertMediaIsPlaying("onCreate")

        editTextTodo = findViewById(R.id.editTextTodo)

        statusText = findViewById(R.id.statusText)
        toggleButton = findViewById(R.id.toggleButton)
        editTextNumberNewsDuration = findViewById(R.id.editTextDuration)
        // editTextNumberVolume = findViewById(R.id.editTextNumberVolumePercentage)
        editTextNumberEveryHour = findViewById(R.id.editTextEveryHour)
        textViewNextNews = findViewById(R.id.textViewNextNewsStr)

        textViewNewsKan = findViewById(R.id.textView13)
        textViewNewsKan.setOnClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                "https://www.kan.org.il/hourly-news/".toUri()
            )
            startActivity(browserIntent)
        }

        textViewNewsGlz = findViewById(R.id.textView12)
        textViewNewsGlz.setOnClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                "https://omny.fm/shows/newsbulletin/playlists/podcast".toUri()
            )
            startActivity(browserIntent)
        }

        editTextNumberEveryHour.setOnFocusChangeListener { _, hasFocus ->
            if (! hasFocus) {
                textViewNextNews.text = getEveryHourStr()

                var newsDurationStr = editTextNumberNewsDuration.text.toString()
                if (newsDurationStr == "") {
                    newsDurationStr = "4"
                    editTextNumberNewsDuration.text = "4"
                }
                val newsDuration = Integer.valueOf(newsDurationStr)
                if (newsDuration > VolumeCycleService.max_news_duration) {
                    editTextNumberNewsDuration.text = VolumeCycleService.max_news_duration.toString()
                }
                if (newsDuration < 1) {
                    editTextNumberNewsDuration.text = "1"
                }

            }
        }

        textViewNextNews.text = getEveryHourStr()

        editTextNumberNewsDuration.setOnFocusChangeListener { _, hasFocus ->
            if (! hasFocus) {
                var newsDurationStr = editTextNumberNewsDuration.text.toString()
                if (newsDurationStr == "") {
                    newsDurationStr = "4"
                    editTextNumberNewsDuration.text = "4"
                }
                val newsDuration = Integer.valueOf(newsDurationStr)
                if (newsDuration > VolumeCycleService.max_news_duration) {
                    editTextNumberNewsDuration.text = VolumeCycleService.max_news_duration.toString()
                }
                if (newsDuration < 1) {
                    editTextNumberNewsDuration.text = "1"
                }
            }
        }

        // seconds

        textViewClock = findViewById(R.id.textViewClock)

        Thread {
            while (true) {
                runOnUiThread {
                    textViewClock.text = LocalDateTime.now().format(DateTimeFormatter.ofPattern("H:mm:ss"))
                        // "Seconds past minute: " + java.time.ZonedDateTime.now(java.time.ZoneId.systemDefault()).second
                }
                Thread.sleep(1000)
            }
        }.start()

        // align UI if needed

        isServiceRunning = VolumeCycleService.isRunning
        Log.i("", "MainActivity isRunning: $isServiceRunning")

        if (! isServiceRunning) {
            toggleButton.text = getString(R.string.start)

            editTextNumberNewsDuration.isEnabled = true
            editTextNumberNewsDuration.isClickable = true

            editTextNumberEveryHour.isEnabled = true
            editTextNumberEveryHour.isClickable = true

            toggleButton.setBackgroundColor(Color.White.toArgb())
        }
        else {
            toggleButton.text = getString(R.string.stop)

            editTextNumberNewsDuration.isEnabled = false
            editTextNumberNewsDuration.isClickable = false

            editTextNumberEveryHour.isEnabled = false
            editTextNumberEveryHour.isClickable = false

            toggleButton.setBackgroundColor(Color.Green.toArgb())
        }

        // Android 13+ needs to ask for notifications permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (! shouldShowRequestPermissionRationale("112")){ // PERMISSION_REQUEST_CODE
                try {
                    Log.i("", "request notifications permission")
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        112
                    )
                    Log.i("", "MainActivity Done. request notifications permission")
                } catch (e: Exception) {
                    Log.e("", "MainActivity failed request notifications permission $e")
                }
            }
        }

        toggleButton.setOnClickListener {
            // Log.d("", "MainActivity isServiceRunning: " + isServiceRunning)

            var everyHoursStr = editTextNumberEveryHour.text.toString()
            if (everyHoursStr == "") {
                everyHoursStr = "4"
                editTextNumberEveryHour.text = "4"
            }
            var everyHours = Integer.valueOf(everyHoursStr)
            if (everyHours > 12) {
                everyHours = 12
                editTextNumberEveryHour.text = "12"
            }
            if (everyHours < 1) {
                everyHours = 1
                editTextNumberEveryHour.text = "1"
            }

            val now = java.time.ZonedDateTime.now(java.time.ZoneId.systemDefault()).hour
            var nextHour = now + 1 //  + everyHours
            if (nextHour > 24) nextHour -= 24
            // Log.d("", "MainActivity nextHour: " + nextHour)

            var nextHoursStr = // "החדשות הבאות תהיינה בשעות " +
                nextHour.toString()

            var plusHours = everyHours

            nextHour += everyHours
            if (nextHour > 24) nextHour -= 24
            nextHoursStr += ", $nextHour"

            while (plusHours < 24) {
                nextHour += everyHours
                plusHours += everyHours
                if (nextHour > 24) nextHour -= 24
                nextHoursStr += ", $nextHour"
            }

            textViewNextNews.text = nextHoursStr

            if (isServiceRunning) {

                val serviceIntent = Intent(this, VolumeCycleService::class.java)
                stopService(serviceIntent)
                statusText.text = getString(R.string.title_name_disabled, BuildConfig.VERSION_NAME)
                toggleButton.text = getString(R.string.start)

                editTextNumberNewsDuration.isEnabled = true
                editTextNumberNewsDuration.isClickable = true

                editTextNumberEveryHour.isEnabled = true
                editTextNumberEveryHour.isClickable = true

                toggleButton.setBackgroundColor(Color.White.toArgb())

                isServiceRunning = false

                getSystemService(NotificationManager::class.java).cancel(1)


            } else {

                val serviceIntent = Intent(this, VolumeCycleService::class.java)

                var newsDurationStr = editTextNumberNewsDuration.text.toString()
                if (newsDurationStr == "") {
                    newsDurationStr = "4"
                    editTextNumberNewsDuration.text = "4"
                }
                var newsDuration = Integer.valueOf(newsDurationStr)
                if (newsDuration > VolumeCycleService.max_news_duration) {
                    newsDuration = VolumeCycleService.max_news_duration
                    editTextNumberNewsDuration.text = VolumeCycleService.max_news_duration.toString()
                }
                if (newsDuration < 1) {
                    newsDuration = 1
                    editTextNumberNewsDuration.text = "1"
                }

                serviceIntent.putExtra("newsDuration", newsDuration)

                serviceIntent.putExtra("hours", Integer.valueOf(everyHours))

                // for testing
                serviceIntent.putExtra("todoList", editTextTodo.text.toString())

                if (alertMediaIsPlaying("onClick to turn on")) {

                    startForegroundService(serviceIntent)

//                    val alertDialog = AlertDialog.Builder(this).create()

//                    try {
//                        val mediaSessionManager =
//                            getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
                        // TODO add the current media that is being used
//                    }
//                    catch (e: Exception) {
//                        Log.w("", "MainActivity Unable to get the active media session " + e)
//                    }

//                    alertDialog.setMessage(getString(R.string.next_30_sec))
                    // alertDialog.setIcon(R.drawable.icon)
//                    alertDialog.show()

                    val alertDialog = AlertDialog.Builder(this).create()
                    alertDialog.setMessage(getString(R.string.next_30_sec))
                    // alertDialog.setIcon(R.drawable.icon)
                    alertDialog.show()

                    Thread {

//                        val next_30_alert = (TextView) this.alertDialog.findViewById(android.R.id.message)

                        for (i in 1..30) {
                            if (! alertDialog.isShowing)
                                break
                            runOnUiThread {
                                if (isServiceRunning) {
                                    alertDialog.setMessage(getString(R.string.next_30_sec_with_sec, (31 - i)))
                                }
                                else {
                                    alertDialog.cancel()
                                }
//                                if (i == 30) {
//                                    alertDialog.cancel()
//                                }
                            }
                            Thread.sleep(1000)
                        }
                        alertDialog.cancel()
                    }.start()

                    statusText.text = getString(R.string.title_name_enabled, BuildConfig.VERSION_NAME)
                    toggleButton.text = getString(R.string.stop)

                    toggleButton.setBackgroundColor(Color.Green.toArgb())

                    editTextNumberNewsDuration.isEnabled = false
                    editTextNumberNewsDuration.isClickable = false

                    editTextNumberEveryHour.isEnabled = false
                    editTextNumberEveryHour.isClickable = false

                    isServiceRunning = true
                }
                else {
                    val serviceIntent = Intent(this, VolumeCycleService::class.java)
                    stopService(serviceIntent)

                    statusText.text = getString(R.string.title_name_disabled, BuildConfig.VERSION_NAME)
                    toggleButton.text = getString(R.string.start)

                    editTextNumberNewsDuration.isEnabled = true
                    editTextNumberNewsDuration.isClickable = true

                    editTextNumberEveryHour.isEnabled = true
                    editTextNumberEveryHour.isClickable = true

                    toggleButton.setBackgroundColor(Color.White.toArgb())

                    isServiceRunning = false

                    getSystemService(NotificationManager::class.java).cancel(1)

                }
            }
        }
    }

    fun alertMediaIsPlaying(from: String): Boolean {
        val audioManager = this.getSystemService(AUDIO_SERVICE) as AudioManager
        val isPlaying = audioManager.isMusicActive()

        Log.i("", "MainActivity isPlaying:$isPlaying from $from")

        var volumeZero = false
        if (from == "onClick to turn on" || (from == "onCreate" && ! VolumeCycleService.isRunning) || from == "onResume") {

            val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            val stream = AudioManager.STREAM_MUSIC

            if (from != "onResume" && 0 == audioManager.getStreamVolume(stream)) {
                volumeZero = true
                Log.w("", "MainActivity from $from and volume is zero")
            }
        }

        if (! isPlaying || volumeZero) {

            val alertDialog = AlertDialog.Builder(this).create()
            alertDialog.setTitle(getString(R.string.alert_title))
            alertDialog.setMessage(getString(R.string.alert_message))
            // alertDialog.setIcon(R.drawable.icon)
            alertDialog.show()
            return false
        }
        return isPlaying
    }

}