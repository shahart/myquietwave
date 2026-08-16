package com.shahartal.myquietchannel

import android.Manifest
import android.app.AlertDialog
//import android.app.Notification
import android.app.NotificationManager

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.icu.util.HebrewCalendar
import android.location.Location
import android.location.LocationManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.CheckBox
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
//import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.crashlytics.crashlytics
import com.shahartal.myquietchannel.luach.HebrewDate
import com.shahartal.myquietchannel.luach.Parshios
import com.shahartal.myquietchannel.parasha.HebCal
import com.shahartal.myquietchannel.parasha.HebCalZmanimModel
import com.shahartal.myquietchannel.parasha.RetrofitInstance
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale.getDefault
import kotlin.time.Clock

internal data class GlglzSongs(val current: String, val next: String?)

internal fun parseGlglzSongs(xml: String): GlglzSongs? {
    fun first(tag: String) = xml.substringAfter("<$tag>", "").substringBefore("</$tag>", "").trim()
    fun last(tag: String) = xml.substringAfterLast("<$tag>", "").substringBefore("</$tag>", "").trim()

    val title = first("titleName")
    if (title.isBlank()) return null

    val artist = first("artistName").takeUnless { it.startsWith("<?xml") }.orEmpty()
    val year = first("year").takeUnless { artist.isBlank() }.orEmpty()
    val current = listOf(title, artist, year).filter { it.isNotBlank() }.joinToString(" . ")

    val nextTitle = last("titleName")
    if (nextTitle.isBlank() || nextTitle == title) return GlglzSongs(current, null)

    val nextArtist = last("artistName").takeUnless { it.startsWith("<?xml") }.orEmpty()
    val nextYear = last("year").takeUnless { nextArtist.isBlank() }.orEmpty()
    val next = listOf(
        nextTitle,
        nextArtist.takeUnless { it == artist }.orEmpty(),
        nextYear.takeUnless { it == year }.orEmpty()
    ).filter { it.isNotBlank() }.joinToString(". ")

    return GlglzSongs(current, next)
}

class MainActivity : ComponentActivity() {

    val hebrewDays = arrayOf("א", "ב", "ג", "ד", "ה", "ו", "ז", "ח", "ט", "י", "יא", "יב", "יג", "יד", "טו", "טז", "יז", "יח", "יט", "כ", "כא", "כב", "כג", "כד", "כה", "כו", "כז", "כח", "כט", "ל")
    val hebrewMonths = arrayOf("תשרי", "חשון", "כסלו", "טבת", "שבט", "אדר", "אדר", "ניסן", "אייר", "סיוון", "תמוז", "אב", "אלול")

    companion object {
        const val NEXT_HOURS = "17, 21, 7, 12, 15, 18"
    }

    private var isServiceRunning = false

    private lateinit var statusText: TextView
    private lateinit var shabesText: TextView
    private lateinit var toggleButton: Button
    private lateinit var shareButton: Button
    private lateinit var peekSongsButton: Button

    // private lateinit var powerButton: Button

    private var mediaPlayer: MediaPlayer? = null

    private lateinit var editTextNumberNewsDuration: TextView
    private lateinit var textViewNextNews: TextView

    private lateinit var textViewNewsLinks: TextView
    private lateinit var textViewPosition: TextView

    private lateinit var textViewClock : TextView
    private lateinit var textViewHebDate : TextView
    private lateinit var textViewClock_2nd : TextView
    private lateinit var textViewClock_3rd : TextView
    private lateinit var textViewClock2 : TextView
    private lateinit var textViewClock2_2 : TextView
    private lateinit var textViewDate : TextView

    private lateinit var textViewClockH : TextView
    private lateinit var textViewClockHS : TextView

    private lateinit var textViewClock3 : TextView
    private lateinit var textViewClock4dafYomi : TextView
    private lateinit var textViewClock4dafYomiTitle : TextView
    private lateinit var textViewOmer : TextView
    private lateinit var textViewClock5locTitle : TextView
    private lateinit var textViewClock5suns : TextView
    private lateinit var textViewClock6rosh : TextView
    private lateinit var textViewClock7special : TextView
    private lateinit var textViewClock8fast : TextView

    private lateinit var editTextTodo : TextView

    private lateinit var currentSong : TextView
    private lateinit var nextSong : TextView

    private lateinit var spinner : Spinner
    private lateinit var stationsSpinner : Spinner

    private lateinit var editTextLocation : TextView

    private lateinit var radioPlayer : CheckBox

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private fun updateServiceUi() {
        val canEditSchedule = !isServiceRunning && !radioPlayer.isChecked

        statusText.text = getString(
            if (isServiceRunning) R.string.title_name_enabled else R.string.title_name_disabled,
            ""
        )
        statusText.alpha = if (isServiceRunning) 1f else 0.92f

        toggleButton.text = getString(if (isServiceRunning) R.string.stop else R.string.start)
        toggleButton.setBackgroundResource(
            if (isServiceRunning) R.drawable.bg_button_active else R.drawable.bg_button_idle
        )
        toggleButton.setTextColor(ContextCompat.getColor(this, R.color.button_text_light))

//        infoText.text = if (isServiceRunning) {
//            "השירות פעיל עכשיו"
//        } else {
//            "הגדר רדיו והפעל"
//        }

        radioPlayer.isEnabled = !isServiceRunning

        editTextNumberNewsDuration.isEnabled = canEditSchedule
        editTextNumberNewsDuration.isClickable = canEditSchedule

        textViewNextNews.isEnabled = canEditSchedule
        textViewNextNews.isClickable = canEditSchedule

        stationsSpinner.isEnabled = !isServiceRunning
        stationsSpinner.isClickable = !isServiceRunning
    }

    override fun onResume() {
        super.onResume()

        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val savedName = sharedPreferences.getString("todoList", "פלטה, מיחם, שעון שבת, מנורה קטנה במסדרון, מזגן")
        val savedLocation = sharedPreferences.getString("location", "IL-Jerusalem")
        val savedStation = sharedPreferences.getString("station", "גלגלצ")
        val justRadio = sharedPreferences.getString("justRadio", "false") // TODO

        editTextTodo.text = savedName
        editTextLocation.text = savedLocation

        val locations = resources.getStringArray(R.array.locations)
        spinner = findViewById<Spinner>(R.id.editTextLocationSpinner)

        if (savedLocation != null && locations.contains(Utils.convertLocationIL(savedLocation))) {
                spinner.setSelection(locations.indexOf(Utils.convertLocationIL(savedLocation)))
        }
        else {
            spinner.setSelection(locations.indexOf("Geo/ GPS-Lat, Lon"))
        }

        stationsSpinner = findViewById<Spinner>(R.id.editTextStationSpinner)

        if (savedStation != null) {
            when (savedStation) {
                "גלגלצ"  -> stationsSpinner.setSelection(0)
                "גלי צהל"    -> stationsSpinner.setSelection(1)
                "רשת ב"    -> stationsSpinner.setSelection(2)
                "רשת ג" -> stationsSpinner.setSelection(3)
                // "FM102"  -> stationsSpinner.setSelection(4)
                "גלי ישראל" -> stationsSpinner.setSelection(4)
                "כאן 88" -> stationsSpinner.setSelection(5)
                "קול חי"  -> stationsSpinner.setSelection(6)
                "קול חי מיוזיק" -> stationsSpinner.setSelection(7)
                "קול ברמה" -> stationsSpinner.setSelection(8)
                "כאן מורשת" -> stationsSpinner.setSelection(9)
            }
        }

        fetchShabatZmanim()
        fetchSunsZmanim()

        var newsDuration = sharedPreferences.getInt("newsDuration", 4)
        if (newsDuration > VolumeCycleService.max_news_duration) newsDuration = VolumeCycleService.max_news_duration
        if (newsDuration < 1)
            newsDuration = 1
        editTextNumberNewsDuration.text = newsDuration.toString()

        val nextHours = sharedPreferences.getString("nextHours", NEXT_HOURS)
        textViewNextNews.text = nextHours

        val serviceIntent = Intent(this, VolumeCycleService::class.java)
        if (! VolumeCycleService.isRunning) {
            stopService(serviceIntent)
            isServiceRunning = false
            currentSong.text = ""
            nextSong.text = ""
        }

        if (justRadio == "true") {
            editTextNumberNewsDuration.isEnabled = false
            textViewNextNews.isEnabled = false
            radioPlayer.isChecked = true
        }

        updateServiceUi()

        getSystemService(NotificationManager::class.java).cancel(1)
        if (mediaPlayer?.isPlaying == true)
            stationsSpinner.setEnabled(false)

        try {
            if (isServiceRunning) {
                val selectedStation = stationsSpinner.selectedItem.toString()
                // Log.i("myquietwave", "periodic fetchGlglzSong" + selectedStation)
                if (Utils.getStationUrl(selectedStation).contains("glglz") /* || Utils.getStationUrl(selectedStation).contains("glz") */ ) {
                    fetchGlglzSong(if (Utils.getStationUrl(selectedStation).contains("glglz")) "glglz" else "glz")
                }
            }
        } catch (e: Exception) {
            Log.w("myquietwave", "Error in periodic fetchGlglzSong", e)
        }

    }

    override fun onPause() {
        super.onPause()

        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("todoList", editTextTodo.text.toString())
        editor.putString("location", editTextLocation.text.toString())
        editor.putString("station", stationsSpinner.getSelectedItem().toString())
        editor.putString("justRadio", if (radioPlayer.isChecked) "true" else "false")

        val newsDurationStr = editTextNumberNewsDuration.text.toString()
        var newsDuration = if (newsDurationStr.isEmpty()) 4 else newsDurationStr.toInt()
        if (newsDuration > VolumeCycleService.max_news_duration) newsDuration = VolumeCycleService.max_news_duration
        if (newsDuration < 1)
            newsDuration = 1
        editor.putInt("newsDuration", newsDuration)

        editor.putString("nextHours", textViewNextNews.text.toString())

        editor.apply()
    }

    fun fetchDafYomi() {
        textViewClock4dafYomi = findViewById(R.id.textViewClock4dafYomi)
        textViewClock4dafYomiTitle = findViewById(R.id.textViewClock4dafYomiTitle)
        textViewOmer = findViewById(R.id.textViewOmer)
        textViewOmer.text = ""
        textViewClock4dafYomi.text = ""
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        var res = ""
        try {
            val start = SimpleDateFormat("yyyy-MM-dd").format(Date())
            RetrofitInstance.api.getDafYomi(start, start).enqueue(object : Callback<HebCal> {

                override fun onResponse(call: Call<HebCal>, response: Response<HebCal>) {
                    if (response.isSuccessful) {
                        val hebcal = response.body()
                        hebcal?.items?.forEach {
                            if (it.category == "dafyomi") {

                                val fullTextYomi = it.hebrew
                                val spannableStringYomi = SpannableString(fullTextYomi)
                                spannableStringYomi.setSpan(UnderlineSpan(), 0, fullTextYomi.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                                textViewClock4dafYomi.text = spannableStringYomi
                                res = it.link // "https://daf-yomi.com/Dafyomi_Page.aspx" // it.link

                                val editor = sharedPreferences.edit()
                                editor.putString("dafYomi", it.hebrew)
                                editor.apply()
                            }
                        }
                        if (res.isNotEmpty()) {

                            var ttip = "עוד לימודים יומיים:\n\n"
                            var omerLink: String

                            hebcal?.items?.forEach {

                                if (it.category == "mishnayomi") {
                                    ttip +=  "משנה יומית: " + it.hebrew + "\n"
                                }
                                else if (it.category == "nachyomi") {
                                    ttip +=  "נ'ך יומי: " + it.hebrew + "\n"
                                }
                                else if (it.category == "dailyPsalms") {
                                    ttip +=  "תהלים יומי: " + it.hebrew + "\n"
                                }
                                else if (it.category == "tanakhYomi") {
                                    ttip +=  "תנ'ך יומי: " + it.hebrew + "\n"
                                }
                                else if (it.category == "omer") {

                                    val fullTextYomi = "ספירת העומר (בבוקר): " + "\n" + it.hebrew.replace("עומר", "")
                                    textViewOmer.text = fullTextYomi

                                    omerLink = it.link
                                    textViewOmer.setOnClickListener {

                                        val browserIntent = Intent(
                                            Intent.ACTION_VIEW,
                                            omerLink.toUri()
                                        )
                                        startActivity(browserIntent)
                                    }
                                }
                            }

                            textViewClock4dafYomiTitle.setOnClickListener {
                                val alertDialogBuilder = AlertDialog.Builder(this@MainActivity)
                                alertDialogBuilder.setMessage(
                                    ttip
                                )
                                alertDialogBuilder.setNegativeButton(getString(R.string.close_alert)) { dialog: DialogInterface?, _: Int ->
                                    dialog!!.cancel()
                                }
                                val alertDialog = alertDialogBuilder.create()
                                alertDialog.show()
                            }

                            textViewClock4dafYomi.setOnClickListener {

                                val browserIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    "https://daf-yomi.com/Dafyomi_Page.aspx".toUri() // res.toUri()
                                )
                                startActivity(browserIntent)
                            }
                        }
                    } else {
                        Log.w("myquietwave", "MainActivity fetchDafYomi Error: ${response.code()}")
                        textViewClock4dafYomi.text = sharedPreferences.getString("dafYomi", "")
                    }
                }

                override fun onFailure(call: Call<HebCal>, t: Throwable) {
                    Log.w("myquietwave", "MainActivity fetchDafYomi unable to fetch hebCal $t", t)
                    textViewClock4dafYomi.text = sharedPreferences.getString("dafYomi", "")
                }
            })
        } catch (e: Exception) {
            Log.e("myquietwave", "MainActivity fetchDafYomi Exception $e", e)
            textViewClock4dafYomi.text = sharedPreferences.getString("dafYomi", "")
            Firebase.crashlytics.log("MainActivity fetchDafYomi Exception")
            Firebase.crashlytics.recordException(e)
        }
    }

    fun fetchShabatZmanim() {
        textViewClock3 = findViewById(R.id.textViewClock3)
        textViewClock3.text = ""

        if ( // (dow == DayOfWeek.THURSDAY || dow == DayOfWeek.FRIDAY || dow == DayOfWeek.SATURDAY) &&
            editTextLocation.text.toString().trim().isNotEmpty()) {

            val firstItem = editTextLocation.text.toString().trim()
            fetchShabatZmanim(firstItem)
        }

    }

    fun fetchShabatZmanim(loc: String) { // }: String {

        textViewClock3 = findViewById(R.id.textViewClock3)

        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)

        var res: String

        if ((loc.trim().get(0).isLetter())) {
            res = loc + "\n"
        } else {
            res = " "
        }

        try {

            val call = // if (loc.get(0).isDigit()) RetrofitInstance.api.getShabbatByLoc(loc.split(",")[0].trim(), loc.split(",")[1].trim())
                // else
                if (Character.isDigit(loc.trim().get(0)) || loc.trim().get(0) == '-') {
                    if (loc.contains(",") && (Character.isDigit(loc.split(",")[1].trim().get(0)) || loc.split(",")[1].trim().get(0) == '-'))
                        RetrofitInstance.api.getShabbatByLoc(loc.split(",")[0].trim(), loc.split(",")[1].trim(), Utils.getUe(loc))
                    else
                        RetrofitInstance.api.getShabbatPerGeoNameId(Utils.getCity(loc), Utils.getUe(loc))
                }
                else {
                    if (loc.lowercase(getDefault()).contains("il-yavne")) {
                        RetrofitInstance.api.getShabbatPerGeoNameId("293222", Utils.getUe(loc))
                    }
                    else if (loc.lowercase(getDefault()).contains("il-mitzpe ramon")) {
                        RetrofitInstance.api.getShabbatPerGeoNameId("294166", Utils.getUe(loc))
                    }
                    else if (loc.lowercase(getDefault()).contains("il-zefat")) {
                        RetrofitInstance.api.getShabbatPerGeoNameId("293100", Utils.getUe(loc))
                    }
                    else if (loc.lowercase(getDefault()).contains("il-modiin ilit")) {
                        RetrofitInstance.api.getShabbatPerGeoNameId("8199378", Utils.getUe(loc))
                    }
                    else if (loc.lowercase(getDefault()).contains("il-betar ilit")) {
                        RetrofitInstance.api.getShabbatPerGeoNameId("284375", Utils.getUe(loc))
                    }
                    else {
                        RetrofitInstance.api.getShabbatPerCity(Utils.getCity(loc), Utils.getUe(loc))
                    }
                }

            call.enqueue(object : Callback<HebCal> {

                override fun onResponse(call: Call<HebCal>, response: Response<HebCal>) {
                    if (response.isSuccessful) {

                        val editor = sharedPreferences.edit()

                        val hebcal = response.body()
                        var mevarchimHebrew: String? = null
                        hebcal?.items?.forEach {
                            // Check 'res' (local) instead of 'textViewClock3.text' (shared UI state)
                            if (it.category == "candles" && !res.contains(getString(R.string.candleLighting))) {
                                res += "\n" + getString(R.string.candleLighting) + " " + truncDate(it.date) + "\n"
                                editor.putString("candles", getString(R.string.candleLighting) + " " + truncDate(it.date))
                            }
                            else if (it.category == "havdalah" && !res.contains(getString(R.string.havdalah))) {
                                res += "\n" + getString(R.string.havdalah) + " " +  truncDate(it.date) + "\n"
                                editor.putString("havdalah", getString(R.string.havdalah) + " " +  truncDate(it.date))
                            }
                            else if (it.category == "mevarchim") {
                                mevarchimHebrew = it.hebrew
                                res += "\n" + it.hebrew + " " +  "\nהמולד: " + it.memo.
                                    substring(it.memo.indexOf(": ") + 2).
                                        replace("chalakim", "חלקים").
                                    replace("and", "ו-").
                                        replace("Sunday", "ראשון").
                                        replace("Monday", "שני").
                                        replace("Tuesday", "שלישי").
                                        replace("Wednesday", "רביעי").
                                        replace("Thursday", "חמישי").
                                        replace("Friday", "שישי").
                                        replace("Saturday", "שבת") + "\n"

                                // Set the listener for the whole text view if mevarchim exists


                                val str: String = it.hebrew
                                textViewClock3.setOnClickListener {
                                    val browserIntent = Intent(Intent.ACTION_VIEW,
                                        ("https://he.wikipedia.org/wiki/" + str.substring(" מברכים חודש ".length-1).replace("סיון", "סיוון") + (if (str.contains("שבט"))  "_(חודש)" else "")).toUri()
                                    )
                                    startActivity(browserIntent)
                                }
                            }
                        }
                        // Final UI Update: Handle Spannable formatting once building is complete
                        if (mevarchimHebrew != null) {
                            val spannable = SpannableString(res)
                            val start = res.indexOf(mevarchimHebrew!!)
                            if (start != -1) {
                                spannable.setSpan(UnderlineSpan(), start, start + mevarchimHebrew!!.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }
                            textViewClock3.text = spannable
                    } else {
                            textViewClock3.text = res
                        }
                        editor.apply()
                    }
                    else {
                        Log.w("myquietwave", "MainActivity fetchZmanim Error: ${response.code()}")
                        // textViewClock3.text = "" // ""Not found " + response.code()
                        res += " " + sharedPreferences.getString("candles", "") + " " + sharedPreferences.getString("havdalah", "")
                        textViewClock3.text = res
                    }
                }

                override fun onFailure(call: Call<HebCal>, t: Throwable) {
                    Log.w("myquietwave", "MainActivity fetchZmanim unable to fetch hebCal $t", t)
                    // textViewClock3.text = "" // ""Failure. Not found " + t
                    res += " " + sharedPreferences.getString("candles", "") + " " + sharedPreferences.getString("havdalah", "")
                    textViewClock3.text = res
                }
            })
        } catch (e: Exception) {
            Log.e("myquietwave", "MainActivity fetchZmanim Exception $e", e)
            // textViewClock3.text = "" // ""Error. Not found " + e
            res += " " + sharedPreferences.getString("candles", "") + " " + sharedPreferences.getString("havdalah", "")
            textViewClock3.text = res
            Firebase.crashlytics.log("MainActivity fetchZmanim Exception")
            Firebase.crashlytics.recordException(e)
        }
    }

    fun fetchSunsZmanim() {
        textViewClock5suns = findViewById(R.id.textViewClock5suns)
        textViewClock5locTitle = findViewById(R.id.textViewClock5locTitle)
        textViewClock5suns.text = ""

        if ( // (dow == DayOfWeek.THURSDAY || dow == DayOfWeek.FRIDAY || dow == DayOfWeek.SATURDAY) &&
            editTextLocation.text.toString().trim().isNotEmpty()) {

            val firstItem = editTextLocation.text.toString().trim()
            fetchSunsZmanim(firstItem)
        }

    }

    fun fetchSunsZmanim(loc: String) { // }: String {

        textViewClock5suns = findViewById(R.id.textViewClock5suns)
        textViewClock5locTitle = findViewById(R.id.textViewClock5locTitle)

        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)

        var res: String

        res = " "

        try {

            val call =

                if (Character.isDigit(loc.trim().get(0)) || loc.trim().get(0) == '-')  {
                    if (loc.contains(",") && (Character.isDigit(loc.split(",")[1].trim().get(0)) || loc.split(",")[1].trim().get(0) == '-'))
                        RetrofitInstance.api.getZmanimByLoc(
                            loc.split(",")[0].trim(),
                            loc.split(",")[1].trim(),
                            Utils.getUe(loc)
                        )
                    else {
                        RetrofitInstance.api.getZmanimPerGeoNameId(
                            Utils.getCity(loc),
                            Utils.getUe(loc)
                        )
                    }
                }
            else {
                if (loc.lowercase(getDefault()).contains("il-yavne")) {
                    RetrofitInstance.api.getZmanimPerGeoNameId("293222", Utils.getUe(loc))
                }
                else if (loc.lowercase(getDefault()).contains("il-zefat")) {
                    RetrofitInstance.api.getZmanimPerGeoNameId("293100", Utils.getUe(loc))
                }
                else if (loc.lowercase(getDefault()).contains("il-mitzpe ramon")) {
                    RetrofitInstance.api.getZmanimPerGeoNameId("294166", Utils.getUe(loc))
                }
                else if (loc.lowercase(getDefault()).contains("il-modiin ilit")) {
                    RetrofitInstance.api.getZmanimPerGeoNameId("8199378", Utils.getUe(loc))
                }
                else if (loc.lowercase(getDefault()).contains("il-betar ilit")) {
                    RetrofitInstance.api.getZmanimPerGeoNameId("284375", Utils.getUe(loc))
                }
                else {
                    RetrofitInstance.api.getZmanimPerCity(Utils.getCity(loc), Utils.getUe(loc))
                }
            }

            call.enqueue(object : Callback<HebCalZmanimModel> {

                override fun onResponse(call: Call<HebCalZmanimModel>, response: Response<HebCalZmanimModel>) {
                    if (response.isSuccessful) {

                        val editor = sharedPreferences.edit()

                        val hebcal = response.body()
                        if (hebcal != null) {
                            res =
                                "\n" + getString(R.string.sunrise) + " " + truncDate(hebcal.times.sunrise)
                            editor.putString(
                                "sunrise",
                                getString(R.string.sunrise) + " " + truncDate(hebcal.times.sunrise)
                            )
                            res += "\n" + getString(R.string.sunset) + " " + truncDate(hebcal.times.sunset) + " "

                            val now = Date()
                            val hours = now.getHours() // Calendar.get(Calendar.HOUR_OF_DAY)
                            val minutes = now.getMinutes() // Calendar.get(Calendar.MINUTE)

                            val hhmm = hebcal.times.sunset.split('T')[1].substring(0,5).split(':')

                            if (hours > hhmm[0].toInt() || (hours == hhmm[0].toInt() && minutes >= hhmm[1].toInt())) {
                                val hebrewCalendar = HebrewCalendar()
                                hebrewCalendar.add(Calendar.HOUR_OF_DAY, 12)
                                val hebY = hebrewCalendar.get(HebrewCalendar.YEAR)
                                val hebrewYear = Utils.getYY(hebY)
                                val hebrewMonth = hebrewCalendar.get(HebrewCalendar.MONTH)
                                val hebrewDay = hebrewCalendar.get(HebrewCalendar.DAY_OF_MONTH) // switches at midnight by-design
                                var hebrewMonthName = hebrewMonths[hebrewMonth]

                                //return (year * 12 + 17) % 19 >= 12;
                                val x: Int = (hebY * 12 + 17) % 19 // HebrewCalendar.YEARS_IN_CYCLE
                                val isLeapYear = x >= (if (x < 0) -7 else 12)

                                if (isLeapYear) {
                                    if (hebrewMonth == 5)
                                        hebrewMonthName = "אדר א"
                                    else if (hebrewMonth == 6)
                                        hebrewMonthName = "אדר ב"
                                }
                                val hebrewDayName = hebrewDays[hebrewDay-1]
                                textViewHebDate.text = " הערב אור ל- $hebrewDayName $hebrewMonthName $hebrewYear"
                            }

                            textViewClock5locTitle.text = hebcal.location.title

                            textViewClock5suns.text = res
                            editor.putString(
                                "sunset",
                                getString(R.string.sunset) + " " + truncDate(hebcal.times.sunset)
                            )

                            textViewClock5suns.setOnClickListener {
                                val alertDialogBuilder = AlertDialog.Builder(this@MainActivity)
                                alertDialogBuilder.setMessage(
                                    "chatzot Night חצות הלילה: " + truncDate(hebcal.times.chatzotNight) + "\n" +
                                    "alot HaShahar עלות השחר: " + truncDate(hebcal.times.alotHaShachar) + "\n" +
                                    "dawn: " + truncDate(hebcal.times.dawn) + "\n" +

                                    "sof Zman Shma מגן אברהם: " + truncDate(hebcal.times.sofZmanShmaMGA) + "\n" +
                                    "sof Zman Shma: " + truncDate(hebcal.times.sofZmanShma) + "\n" +
                                    "sof Zman Tfilla מגן אברהם: " + truncDate(hebcal.times.sofZmanTfillaMGA) + "\n" +
                                    "sof Zman Tfilla: " + truncDate(hebcal.times.sofZmanTfilla) + "\n" +

                                    "chatzot חצות היום: " + truncDate(hebcal.times.chatzot) + "\n" +

                                            "\n" +

                                    "mincha Gedola מנחה גדולה: " + truncDate(hebcal.times.minchaGedola) + "\n" +
                                    "mincha Ketana מנחה קטנה: " + truncDate(hebcal.times.minchaKetana) + "\n" +
                                    "plag HaMincha פלג המנחה: " + truncDate(hebcal.times.plagHaMincha) + "\n" +

                                    "bein HaShmashos בין השמשות: " + truncDate(hebcal.times.beinHaShmashos) + "\n" +
                                    "Dusk חשיכה: " + truncDate(hebcal.times.dusk) + "\n" +
                                    "Tzeit צאת הכוכבים: " + truncDate(hebcal.times.tzeit7083deg) + "\n" +
                                    "Tzeit 72' צאת הכוכבים רבינו תם: " + truncDate(hebcal.times.tzeit72min)
                                )
                                alertDialogBuilder.setNegativeButton(getString(R.string.close_alert)) { dialog: DialogInterface?, _: Int ->
                                    dialog!!.cancel()
                                }
                                val alertDialog = alertDialogBuilder.create()
                                alertDialog.show()
                            }

                            editor.apply()
                            // textViewClock3.text = res
                        }
                    } else {
                        Log.w("myquietwave", "MainActivity fetchSunsZmanim Error: ${response.code()}")
                        // textViewClock3.text = "" // ""Not found " + response.code()
                        res += " " + sharedPreferences.getString("sunrise", "") + " " + sharedPreferences.getString("sunset", "")
                        textViewClock5suns.text = res
                    }
                }

                override fun onFailure(call: Call<HebCalZmanimModel>, t: Throwable) {
                    Log.w("myquietwave", "MainActivity fetchSunsZmanim unable to fetch hebCal $t", t)
                    // textViewClock3.text = "" // ""Failure. Not found " + t
                    res += " " + sharedPreferences.getString("sunrise", "") + " " + sharedPreferences.getString("sunset", "")
                    textViewClock5suns.text = res
                }
            })
        } catch (e: Exception) {
            Log.e("myquietwave", "MainActivity fetchSunsZmanim Exception $e", e)
            // textViewClock3.text = "" // ""Error. Not found " + e
            res += " " + sharedPreferences.getString("sunrise", "") + " " + sharedPreferences.getString("sunset", "")
            textViewClock5suns.text = res
            Firebase.crashlytics.log("MainActivity fetchSunsZmanim Exception")
            Firebase.crashlytics.recordException(e)
        }
    }


    fun truncDate(date: String): String {
        var res = date.substring(date.indexOf("T")+1, date.indexOf("T")+1 +5)
        if (res.startsWith('0'))
            res = res.substring(1)
        return " " + res + " "
    }

    fun convertEng(hebre: String): String {
        var hebrew = hebre
        hebrew = hebrew.replace("Joshua", "יהושע");
        hebrew = hebrew.replace("Judges", "שופטים");
        hebrew = hebrew.replace("I Samuel", "שמואל א");
        hebrew = hebrew.replace("II Samuel", "שמואל ב");
        hebrew = hebrew.replace("I Kings", "מלכים א");
        hebrew = hebrew.replace("II Kings", "מלכים ב");
        hebrew = hebrew.replace("Isaiah", "ישעיהו");
        hebrew = hebrew.replace("Jeremiah", "ירמיהו");
        hebrew = hebrew.replace("Ezekiel", "יחזקאל");
        hebrew = hebrew.replace("Hosea", "הושע");
        hebrew = hebrew.replace("Joel", "יואל");
        hebrew = hebrew.replace("Amos", "עמוס");
        hebrew = hebrew.replace("Obadiah", "עובדיה")
        hebrew = hebrew.replace("Jonah", "יונה");
        hebrew = hebrew.replace("Micah", "מיכה");
        hebrew = hebrew.replace("Nachum", "נחום");
        hebrew = hebrew.replace("Habakkuk", "חבקוק");
        hebrew = hebrew.replace("Zephaniah", "צפניה");
        hebrew = hebrew.replace("Haggai", "חגי");
        hebrew = hebrew.replace("Zechariah", "זכריה");
        hebrew = hebrew.replace("Malachi", "מלאכי");
        return hebrew;
    }
    fun fetchParasha() { // }: String {

        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)

        textViewClockH = findViewById(R.id.textViewClockH)
        textViewClockHS = findViewById(R.id.textViewClockHS)
        textViewClock6rosh = findViewById(R.id.textViewClock6rosh)
        textViewClock7special = findViewById(R.id.textViewClock7special)
        textViewClock8fast = findViewById(R.id.textViewClock8fast)

        try {

            RetrofitInstance.api.getShabbatPerCity("IL-Jerusalem", "off").enqueue(object : Callback<HebCal> {

                override fun onResponse(call: Call<HebCal>, response: Response<HebCal>) {
                    if (response.isSuccessful) {
                        val editor = sharedPreferences.edit()
                        // val str = response.body()
                        // Log.i("myquietwave", "MainActivity fetchParasha " + str)
                        val hebcal = response.body()
                        var memo = ""
                        hebcal?.items?.forEach {
                            if (it.category == "roshchodesh") {

                                textViewClock6rosh.text = textViewClock6rosh.text.toString() +
                                    it.hebrew + " - " + "ראשון,שני,שלישי,רביעי,חמישי,שישי,שבת,ראשון".split(
                                        ","
                                    )
                                        .get(SimpleDateFormat("yyyy-MM-dd").parse(it.date).day) + " " + Utils.switchDate(it.date) + "\n";
                                val roshchodeshDate = it.date;
                                if (Utils.isBefore(roshchodeshDate)) {
                                    textViewClock6rosh.text = "";
                                } else {
                                    val str: String = it.hebrew
                                    textViewClock6rosh.setOnClickListener {
                                        val browserIntent = Intent(
                                            Intent.ACTION_VIEW,
                                            ("https://he.wikipedia.org/wiki/" + str.substring(" ראש חודש ".length - 1).replace("סיון", "סיוון") + (if (str.contains("שבט"))  "_(חודש)" else "")).toUri()
                                        )
                                        startActivity(browserIntent)
                                    }
                                    val fullTextH = textViewClock6rosh.text
                                    val spannableStringH = SpannableString(fullTextH)
                                    spannableStringH.setSpan(
                                        UnderlineSpan(),
                                        " ראש חודש ".length -1,
                                        fullTextH.length,
                                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )
                                    textViewClock6rosh.text = spannableStringH
                                }

                                if (! memo.contains(it.memo)) {
                                    memo += "\n\n" + it.hebrew + ": " + it.memo
                                }
                            }
                            else if (it.category == "holiday") {
                                val holidayDate = it.date
                                if (! Utils.isBefore(holidayDate)) {

                                    textViewClock7special.text = textViewClock7special.text.toString() + "\n" +
                                        it.hebrew + " - " + "ראשון,שני,שלישי,רביעי,חמישי,שישי,שבת,ראשון".split(
                                        ","
                                    )
                                        .get(SimpleDateFormat("yyyy-MM-dd").parse(it.date).day) + " " + Utils.switchDate(it.date) ;

                                    if (! memo.contains(it.memo)) {
                                        memo += "\n\n" + it.hebrew + ": " + it.memo
                                    }

                                }
                            }
                            else if (it.title == "Fast begins") {

                                textViewClock8fast.text = textViewClock8fast.text.toString() + "  זמני התענית: עלות השחר " +
                                        (it.date.split('T')[1].substring(0,5))

                            }
                            else if (it.title == "Fast ends") {

                                textViewClock8fast.text =
                                    textViewClock8fast.text.toString() + " צאת הכוכבים " +
                                            (it.date.split('T')[1].substring(0,5))

                                if (Utils.isBefore(it.date.split('T')[0])) {
                                    textViewClock8fast.text = ""
                                }
                            }
                           else if (it.category == "parashat") {
                                // return it.hebrew;

                                var str: String = it.hebrew
                                var str2: String = ""

                                if (str.contains("-")) {
                                    str2 = "פרשת " + str.split("-")[1]
                                    str = str.split("-")[0]
                                }

                                if (str.contains("־")) {
                                    str2 = "פרשת " + str.split("־")[1]
                                    str = str.split("־")[0]
                                }

                                val fullText =  " שבת " + str
                                val spannableString = SpannableString(fullText)
                                spannableString.setSpan(UnderlineSpan(), " שבת ".length, fullText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                textViewClock2.text = spannableString

                                editor.putString("parashat", " שבת " + str)
                                editor.apply()

                                textViewClock2.setOnClickListener {
                                    val browserIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        ("https://he.wikipedia.org/wiki/" + str.replace(" ", "_")).toUri()
                                    )
                                    startActivity(browserIntent)
                                }

                                if (str2.length >= 1) {

                                    val fullText =  str2
                                    val spannableString = SpannableString(fullText)
                                    spannableString.setSpan(UnderlineSpan(), 0, fullText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    textViewClock2_2.text = spannableString

                                    textViewClock2_2.setOnClickListener {
                                        val browserIntent = Intent(
                                            Intent.ACTION_VIEW,
                                            ("https://he.wikipedia.org/wiki/" + str2.replace(
                                                " ",
                                                "_"
                                            )).toUri()
                                        )
                                        startActivity(browserIntent)
                                    }
                                }

                                val hebName = convertEng(it.leyning.haftarah.replace("|", "\n"))
                                val fullTextH =  " הפטרה " + hebName
                                val spannableStringH = SpannableString(fullTextH)
                                spannableStringH.setSpan(UnderlineSpan(), " הפטרה ".length, fullTextH.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                textViewClockH.text = spannableStringH

                                editor.putString("haftarah", " הפטרה " + hebName)
                                editor.apply()

                                val strH: String = it.leyning.haftarah.split(':')[0]
                                textViewClockH.setOnClickListener {
                                    val browserIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        ("https://shahart.github.io/heb-bible/index.html?b=" + strH).toUri()
                                    )
                                    startActivity(browserIntent)
                                }

                                // it.leyning.haftarah_sephardic = "Ezekiel 8:25-29:21"
                                if (it.leyning.haftarah_sephardic != null) {

                                    val hebName = convertEng(it.leyning.haftarah_sephardic.replace("|", "\n"))
                                    val fullTextHS =  " הפטרה ספרדים " + hebName
                                    val spannableStringHS = SpannableString(fullTextHS)
                                    spannableStringHS.setSpan(UnderlineSpan(), " הפטרה ספרדים ".length, fullTextHS.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    textViewClockHS.text = spannableStringHS

                                    editor.putString("haftarah_sephardic", " הפטרה ספרדים " + hebName)
                                    editor.apply()

                                    val strHS: String = it.leyning.haftarah_sephardic.split(':')[0]
                                    textViewClockHS.setOnClickListener {
                                        val browserIntent = Intent(
                                            Intent.ACTION_VIEW,
                                            ("https://shahart.github.io/heb-bible/index.html?b=" + strHS).toUri()
                                        )
                                        startActivity(browserIntent)
                                    }
                                }

                            }
                        }
                        if (memo.length > 0) {
                            textViewClock7special.setOnClickListener {
                                val alertDialogBuilder = AlertDialog.Builder(this@MainActivity)
                                alertDialogBuilder.setMessage(memo )
                                alertDialogBuilder.setNegativeButton(getString(R.string.close_alert)) { dialog: DialogInterface?, _: Int ->
                                    dialog!!.cancel()
                                }
                                val alertDialog = alertDialogBuilder.create()
                                alertDialog.show()
                            }
                        }
                        else {
                            textViewClock7special.setOnClickListener {
                            }
                        }
                    } else {
                        Log.w("myquietwave", "MainActivity fetchParasha Error: ${response.code()}")
                        textViewClock2.text = getParasha()
                        textViewClockH.text = sharedPreferences.getString("haftarah", "")
                        textViewClockHS.text = sharedPreferences.getString("haftarah_sephardic", "")
                    }
                }

                override fun onFailure(call: Call<HebCal>, t: Throwable) {
                    Log.w("myquietwave", "MainActivity fetchParasha unable to fetch hebCal $t", t)
                    textViewClock2.text = getParasha()
                    textViewClockH.text = sharedPreferences.getString("haftarah", "")
                    textViewClockHS.text = sharedPreferences.getString("haftarah_sephardic", "")
                }
            })
        } catch (e: Exception) {
            Log.e("myquietwave", "MainActivity fetchParasha Exception $e", e)
            textViewClock2.text = getParasha()
            textViewClockH.text = sharedPreferences.getString("haftarah", "")
            textViewClockHS.text = sharedPreferences.getString("haftarah_sephardic", "")
            Firebase.crashlytics.log("MainActivity fetchParasha Exception")
            Firebase.crashlytics.recordException(e)
        }
    }

    fun getParasha(): String {
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        var res = sharedPreferences.getString("parashat", "").toString();
        if (res == "") {
            res = " שבת פרשת " + Parshios.getParshaString(HebrewDate.today())
        }
        return res
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAnalytics = Firebase.analytics

        Log.i("myquietwave", "MainActivity Version " + BuildConfig.VERSION_NAME)


        editTextLocation = findViewById(R.id.editTextLocation)

        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val savedLocation = sharedPreferences.getString("location", "IL-Jerusalem")
        val savedStation = sharedPreferences.getString("station", "גלגלצ")
        val justRadio = sharedPreferences.getString("justRadio", "false")

        editTextLocation.text = savedLocation
        val locations = resources.getStringArray(R.array.locations)

        spinner = findViewById(R.id.editTextLocationSpinner)
        stationsSpinner = findViewById(R.id.editTextStationSpinner)
        peekSongsButton = findViewById(R.id.peekSongsButton)
        currentSong = findViewById(R.id.textViewCurrentSong)
        nextSong = findViewById(R.id.textViewNextSong)

        if (savedStation != null) {
            when (savedStation) {
                "גלגלצ"  -> stationsSpinner.setSelection(0)
                "גלי צהל"    -> stationsSpinner.setSelection(1)
                "רשת ב"    -> stationsSpinner.setSelection(2)
                "רשת ג" -> stationsSpinner.setSelection(3)
                // "FM102"  -> stationsSpinner.setSelection(4)
                "גלי ישראל" -> stationsSpinner.setSelection(4)
                "כאן 88" -> stationsSpinner.setSelection(5)
                "קול חי"  -> stationsSpinner.setSelection(6)
                "קול חי מיוזיק" -> stationsSpinner.setSelection(7)
                "קול ברמה" -> stationsSpinner.setSelection(8)
                "כאן מורשת" -> stationsSpinner.setSelection(9)
            }
        }

        if (savedLocation != null && locations.contains(Utils.convertLocationIL(savedLocation))) {
            spinner.setSelection(locations.indexOf(Utils.convertLocationIL(savedLocation)))
        }
        else {
            spinner.setSelection(locations.indexOf("Geo/ GPS-Lat, Lon"))
        }

        if (spinner != null) {

            val locations = resources.getStringArray(R.array.locations)
            val stations = resources.getStringArray(R.array.stations)

            val adapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, locations)

            spinner.adapter = adapter

            val stationsAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, stations)

            stationsSpinner.adapter = stationsAdapter

            savedStation?.let { station ->
                stations.indexOf(station).takeIf { it >= 0 }?.let(stationsSpinner::setSelection)
            }

            fun updatePeekSongsButton() {
                val isGlglzSelected = isGlglzStation(stationsSpinner.selectedItem?.toString())
                peekSongsButton.visibility = if (isGlglzSelected) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                if (!isGlglzSelected) {
                    currentSong.text = ""
                    nextSong.text = ""
                }
            }

            stationsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    updatePeekSongsButton()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    peekSongsButton.visibility = View.GONE
                }
            }
            updatePeekSongsButton()

            lifecycleScope.launch {
                delay(200)

                spinner.onItemSelectedListener = object :
                    AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>,
                                                view: View?, position: Int, id: Long) {

                        editTextLocation.text = "IL-Jerusalem"
                        // try/catch so the main functionality- the click on Start will work
                        if (position != null && id != null && position >= 0 && position < locations.size && locations[position].isNotEmpty() && locations[position] != "Geo/ GPS-Lat, Lon") {
                            editTextLocation.text =
                                Utils.convertFromLocationIL(locations[position])
                        }

                        fetchShabatZmanim()
                        fetchSunsZmanim()

                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // TODO?
                    }
                }
            }
        }

        // @RequiresApi(8
        if (ZonedDateTime.now(ZoneId.systemDefault()).dayOfWeek == DayOfWeek.FRIDAY) {
            shabesText = findViewById(R.id.textViewShabes)
            shabesText.text = getString(R.string.shabbath)
        }

        try {
            fetchParasha()
        }
        catch (e: Exception) {
            Firebase.crashlytics.log("WARN. fetchParasha. Cannot format given Object as a Date " + e.toString()) // saw length=1; index=2
            Firebase.crashlytics.recordException(e)
        }

        fetchDafYomi()

        editTextTodo = findViewById(R.id.editTextTodo)

        radioPlayer = findViewById(R.id.radioCheckbox)

        /* val infoIcon: ImageView = findViewById(R.id.info_icon)
        infoIcon.setOnClickListener {
            Toast.makeText(this, "Here you can place your city, with a comma, for Candle lighting and Havdalah times", Toast.LENGTH_LONG).show();
        }*/

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        shareButton = findViewById(R.id.shareButton)

        shareButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.setType("text/plain")
            val shareLink = "https://play.google.com/store/apps/details?id=$packageName"
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + " " + shareLink)
            startActivity(Intent.createChooser(shareIntent, "Share this app"))
        }

        statusText = findViewById(R.id.statusText)
        toggleButton = findViewById(R.id.toggleButton)

        peekSongsButton.setOnClickListener {
            fetchGlglzSong("glglz")
        }

        editTextNumberNewsDuration = findViewById(R.id.editTextDuration)
        textViewNextNews = findViewById(R.id.textViewNextNewsStr)

        textViewNewsLinks = findViewById(R.id.textView14)
        textViewNewsLinks.setOnClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                "https://shahart.github.io/myquietwave/links.html".toUri()
            )
            startActivity(browserIntent)
        }

        textViewPosition = findViewById(R.id.textViewLocationLabel)
        textViewPosition.setOnClickListener {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 0)
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
                val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                if (! isGpsEnabled) {
                    Log.w("myquietwave", "MainActivity fetchZmanim location isGpsDisabled")
                }
                else {
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

                    fusedLocationClient.lastLocation

                        .addOnSuccessListener { location: Location? ->
                            Log.i("myquietwave", "MainActivity fetchZmanim location success " + location)
                            if (location != null) {
                                // firebaseAnalytics.logEvent(FirebaseAnalytics.Param.LOCATION) {
                                // }
                                val locStr = Utils.roundToDecimalPlaces(location.latitude).toString() + "," +
                                        Utils.roundToDecimalPlaces(location.longitude).toString()
                                editTextLocation.text = locStr
                                spinner.setSelection(locations.indexOf("Geo/ GPS-Lat, Lon"))

                                fetchShabatZmanim(locStr)

                                val alertDialogBuilder = AlertDialog.Builder(this)
                                alertDialogBuilder.setMessage(getString(R.string.ue_usage) /* + " -- Version: " + BuildConfig.VERSION_NAME */ )
                                alertDialogBuilder.setNegativeButton(getString(R.string.close_alert)) { dialog: DialogInterface?, _: Int ->
                                    if (! this.isFinishing) {
                                        dialog!!.cancel()
                                    }
                                }
                                val alertDialog = alertDialogBuilder.create()
                                alertDialog.show()

                            }
                        }
                        .addOnFailureListener { exception: Exception ->
                            Log.w("myquietwave", "MainActivity fetchZmanim location failure " + exception)
                        }
                }
            }
        }

        editTextLocation.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                fetchShabatZmanim()
                fetchSunsZmanim()
            }
        }

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
                if (textViewNextNews.text.toString() == "") {
                    textViewNextNews.text = NEXT_HOURS
                }
            }
        }

        // seconds

        textViewClock = findViewById(R.id.textViewClock)
        textViewHebDate = findViewById(R.id.textViewHebDate)
        textViewClock_2nd = findViewById(R.id.textViewClock_2nd)
        textViewClock_3rd = findViewById(R.id.textViewClock_3rd)

        textViewClock2 = findViewById(R.id.textViewClock2)
        textViewClock2_2 = findViewById(R.id.textViewClock2_2)
        textViewDate = findViewById(R.id.textViewDate)

        textViewClock_2nd.text = TimeZone.currentSystemDefault().id
        // todo? ZoneId.short_ids code, like idt, pst
        textViewClock_3rd.text = "UTC" + TimeZone.currentSystemDefault().offsetAt(Clock.System.now()) // kotlinx.datetime.TimeZone.of(textViewClock_2nd.text.toString()).offsetAt(kotlinx.datetime.Instant.fromEpochMilliseconds(System.currentTimeMillis()))

        val hebrewCalendar = HebrewCalendar()
        val hebY = hebrewCalendar.get(HebrewCalendar.YEAR)
        val hebrewYear = Utils.getYY(hebY)
        val hebrewMonth = hebrewCalendar.get(HebrewCalendar.MONTH)
        val hebrewDay = hebrewCalendar.get(HebrewCalendar.DAY_OF_MONTH) // switches at midnight by-design
        var hebrewMonthName = hebrewMonths[hebrewMonth]

        //return (year * 12 + 17) % 19 >= 12;
        val x: Int = (hebY * 12 + 17) % 19 // HebrewCalendar.YEARS_IN_CYCLE
        val isLeapYear = x >= (if (x < 0) -7 else 12)

        if (isLeapYear) {
            if (hebrewMonth == 5)
                hebrewMonthName = "אדר א"
            else if (hebrewMonth == 6)
                hebrewMonthName = "אדר ב"
        }
        val hebrewDayName = hebrewDays[hebrewDay-1]
        textViewHebDate.text = "$hebrewDayName $hebrewMonthName $hebrewYear"

        Thread {
            while (true) {
                runOnUiThread {
                    textViewClock.text = LocalDateTime.now().format(DateTimeFormatter.ofPattern("H:mm:ss"))
                    textViewDate.text = "ראשון,שני,שלישי,רביעי,חמישי,שישי,שבת,ראשון".split(
                        ","
                    )
                        .get(LocalDate.now().dayOfWeek.value) + " " + SimpleDateFormat("d/M/yyyy").format(Date()) // Cannot format given Object as a Date

                }
                Thread.sleep(500)
            }
        }.start()

        // align UI if needed

        isServiceRunning = VolumeCycleService.isRunning
        Log.i("myquietwave", "MainActivity isRunning: $isServiceRunning")
        toggleButton.setAllCaps(false)
        shareButton.setAllCaps(false)
        updateServiceUi()

        // Android 13+ needs to ask for notifications permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (! shouldShowRequestPermissionRationale("112")){ // PERMISSION_REQUEST_CODE
                try {
                    Log.i("myquietwave", "request notifications permission")
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        112
                    )
                    Log.i("myquietwave", "MainActivity Done. request notifications permission")
                } catch (e: Exception) {
                    Log.e("myquietwave", "MainActivity failed request notifications permission $e")
                }
            }
        }

        radioPlayer.setOnClickListener {
            updateServiceUi()
        }

        if (justRadio == "true") {
            radioPlayer.isChecked = true
            updateServiceUi()
        }

        toggleButton.setOnClickListener {
            // Log.d("myquietwave", "MainActivity isServiceRunning: " + isServiceRunning)

            if (mediaPlayer?.isPlaying == true)
                mediaPlayer?.stop()

            if (isServiceRunning) {

                val serviceIntent = Intent(this, VolumeCycleService::class.java)
                stopService(serviceIntent)

                isServiceRunning = false
                currentSong.text = ""
                nextSong.text = ""
                updateServiceUi()

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
                if (textViewNextNews.text.toString() == "") {
                    textViewNextNews.text = NEXT_HOURS
                }

                serviceIntent.putExtra("newsDuration", newsDuration)
                serviceIntent.putExtra("nextHours", textViewNextNews.text.toString())
                serviceIntent.putExtra("station", stationsSpinner.getSelectedItem().toString())
                serviceIntent.putExtra("todoList", editTextTodo.text.toString())
                serviceIntent.putExtra("location", editTextLocation.text.toString())
                serviceIntent.putExtra("radioPlayer", if (radioPlayer.isChecked()) "true" else "false" )

                val selectedStation = stationsSpinner.selectedItem.toString()
                if (Utils.getStationUrl(selectedStation).contains("glglz") /* || Utils.getStationUrl(selectedStation).contains("glz") */) {
                    fetchGlglzSong(if (Utils.getStationUrl(selectedStation).contains("glglz")) "glglz" else "glz")
                }

                if (true) {

                    startForegroundService(serviceIntent)

                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                        param("newsDuration", newsDuration.toLong())
                        param("station", stationsSpinner.getSelectedItem().toString())
                        param("isFriday", if (ZonedDateTime.now(ZoneId.systemDefault()).dayOfWeek == DayOfWeek.FRIDAY) 1L else 0L)
                        param("isRadioPlayer", if (radioPlayer.isChecked()) 1L else 0L)
                    }

                    if (! radioPlayer.isChecked()) {

                        val alertDialogBuilder = AlertDialog.Builder(this)
                        alertDialogBuilder.setMessage(getString(R.string.next_30_sec))
                        alertDialogBuilder.setNegativeButton(getString(R.string.close_alert)) { dialog: DialogInterface?, _: Int ->
                            if (!this.isFinishing) {
                                dialog!!.cancel()
                            }
                        }
                        val alertDialog = alertDialogBuilder.create()
                        alertDialog.show()

                        Thread {

                            val audioManager = this.getSystemService(AUDIO_SERVICE) as AudioManager
                            for (i in 1..30) {
                                if (!alertDialog.isShowing) {
                                    break
                                }
                                runOnUiThread {
                                    if (isServiceRunning) {
                                        alertDialog.setMessage(
                                            getString(
                                                R.string.next_30_sec_with_sec, (31 - i),
                                                100 * audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) / audioManager.getStreamMaxVolume(
                                                    AudioManager.STREAM_MUSIC
                                                )
                                            )
                                        )
                                    } else {
                                        if (!this.isFinishing) {
                                            alertDialog.cancel()
                                        }
                                    }
                                }
                                Thread.sleep(1000)
                            }
                            if (!this.isFinishing) {
                                alertDialog.cancel()
                            }
                        }.start()
                    }

                    isServiceRunning = true
                    updateServiceUi()
                }
            }
        }

        checkForAppUpdate()

        lifecycleScope.launch {
            while (true) {
                delay(90_000)
                try {
                    if (isServiceRunning) {
                        val selectedStation = stationsSpinner.selectedItem.toString()
                        // Log.i("myquietwave", "periodic fetchGlglzSong" + selectedStation)
                        if (Utils.getStationUrl(selectedStation).contains("glglz") /* || Utils.getStationUrl(selectedStation).contains("glz") */ ) {
                            fetchGlglzSong(if (Utils.getStationUrl(selectedStation).contains("glglz")) "glglz" else "glz")
                        }
                    }
                } catch (e: Exception) {
                    Log.w("myquietwave", "Error in periodic fetchGlglzSong", e)
                }
            }
        }
    }

    private lateinit var appUpdateManager: AppUpdateManager
    private val RC_APP_UPDATE = 100 // Request code for the update flow

    private fun checkForAppUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) // Or AppUpdateType.FLEXIBLE
            ) {
                startUpdateFlow(appUpdateInfo)
            }
        }

        appUpdateInfoTask.addOnFailureListener { exception ->
            Log.w("myquietwave", "checkForAppUpdate. Error checking for app update: $exception")
            Firebase.crashlytics.log("WARN checkForAppUpdate Exception. " + exception.toString())
        }
    }

    private fun startUpdateFlow(appUpdateInfo: AppUpdateInfo) {
        appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo,
            AppUpdateType.IMMEDIATE, // Change to AppUpdateType.FLEXIBLE for flexible updates
            this,
            RC_APP_UPDATE
        )
    }

    private fun isGlglzStation(station: String?): Boolean = station != null &&
        Utils.getStationUrl(station).contains("glglz")

    private fun fetchGlglzSong(station: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = "https://glzxml.blob.core.windows.net/dalet/" + station + "-onair/onair.xml"
                val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 15_000
                connection.readTimeout = 15_000
                val xml = try {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } finally {
                    connection.disconnect()
                }
                val songs = parseGlglzSongs(xml)

                withContext(Dispatchers.Main) {
                    if (!isFinishing && isGlglzStation(stationsSpinner.selectedItem?.toString())) {
                        currentSong.text = songs?.current.orEmpty()
                        nextSong.text = songs?.next?.let { getString(R.string.next_song) + " " + it }.orEmpty()
                    }
                }
            } catch (e: Exception) {
                Log.w("myquietwave", "Error fetching Glglz song info", e)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_APP_UPDATE) { //
            if (resultCode != RESULT_OK) {
                Log.e("myquietwave", "Update flow failed! Result code: $resultCode")
                Firebase.crashlytics.log("ERROR. Update flow failed. Result code: " + resultCode.toString())
            }
        }
    }
}
