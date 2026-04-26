package com.shahartal.myquietchannel

import android.Manifest
import android.app.AlertDialog
//import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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
import com.shahartal.myquietchannel.parasha.HebCal
import com.shahartal.myquietchannel.parasha.HebCalZmanimModel
import com.shahartal.myquietchannel.parasha.RetrofitInstance
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    private lateinit var spinner : Spinner
    private lateinit var stationsSpinner : Spinner

    private lateinit var editTextLocation : TextView

    private lateinit var radioPlayer : CheckBox

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    fun Context.isDarkThemeOn(): Boolean {
        return resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
    }

    override fun onResume() {
        super.onResume()

        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val savedName = sharedPreferences.getString("todoList", "פלטה, מיחם, שעון שבת, מנורה קטנה במסדרון, מזגן")
        val savedLocation = sharedPreferences.getString("location", "IL-Jerusalem")
        val savedStation = sharedPreferences.getString("station", "GLZ")
        val justRadio = sharedPreferences.getString("justRadio", "false")

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
                "גלי צהל"    -> stationsSpinner.setSelection(0)
                "גלגלצ"  -> stationsSpinner.setSelection(1)
                "רשת ב"    -> stationsSpinner.setSelection(2)
                "רשת ג" -> stationsSpinner.setSelection(3)
                "FM102"  -> stationsSpinner.setSelection(4)
                "גלי ישראל" -> stationsSpinner.setSelection(5)
                "כאן 88" -> stationsSpinner.setSelection(6)
                "קול חי"  -> stationsSpinner.setSelection(7)
                "קול חי מיוזיק" -> stationsSpinner.setSelection(8)
                "קול ברמה" -> stationsSpinner.setSelection(9)
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
        }

        if (justRadio == "true") {
            editTextNumberNewsDuration.isEnabled = false
            textViewNextNews.isEnabled = false
            radioPlayer.isChecked = true
        }
        if (isServiceRunning) {
            statusText.text = getString(R.string.title_name_enabled, "" /*BuildConfig.VERSION_NAME*/)
            toggleButton.text = getString(R.string.stop)
            toggleButton.setBackgroundColor(Color.Green.toArgb())
            radioPlayer.isEnabled = false
        }
        else {
            statusText.text = getString(R.string.title_name_disabled, "" /*BuildConfig.VERSION_NAME*/)
            toggleButton.text = getString(R.string.start)
            toggleButton.setBackgroundColor(if (isDarkThemeOn()) Color.Black.toArgb() else Color.White.toArgb())
            radioPlayer.isEnabled = true
        }

        editTextNumberNewsDuration.isEnabled = ! isServiceRunning && ! radioPlayer.isChecked
        editTextNumberNewsDuration.isClickable = ! isServiceRunning && ! radioPlayer.isChecked

        textViewNextNews.isEnabled = ! isServiceRunning && ! radioPlayer.isChecked
        textViewNextNews.isClickable = ! isServiceRunning && ! radioPlayer.isChecked

        stationsSpinner.isEnabled = ! isServiceRunning
        stationsSpinner.isClickable = ! isServiceRunning

        getSystemService(NotificationManager::class.java).cancel(1)
        
        if (mediaPlayer?.isPlaying == true)
            stationsSpinner.setEnabled(false)

    }

    override fun onPause() {
        super.onPause()

        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("todoList", editTextTodo.text.toString())
        editor.putString("location", editTextLocation.text.toString())
        editor.putString("station", stationsSpinner.getSelectedItem().toString())
        editor.putString("justRadio", if (radioPlayer.isChecked()) "true" else "false")

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

                            var ttip = "עוד לימודים יומיים:\n\nדף יומי צורת הדף\n https://daf-yomi.com/Dafyomi_Page.aspx\n\n"
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
                                    res.toUri()
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
                if (Character.isDigit(loc.trim().get(0))) {
                    if (loc.contains(",") && Character.isDigit(loc.split(",")[1].trim().get(0)))
                        RetrofitInstance.api.getShabbatByLoc(loc.split(",")[0].trim(), loc.split(",")[1].trim(), Utils.getUe(loc))
                    else
                        RetrofitInstance.api.getShabbatPerGeoNameId(Utils.getCity(loc), Utils.getUe(loc))
                }
                else {
                    if (loc.lowercase(getDefault()).contains("il-yavne")) {
                        RetrofitInstance.api.getShabbatPerGeoNameId("293222", Utils.getUe(loc))
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
                        hebcal?.items?.forEach {
                            if (it.category == "candles" && (it.memo.isNullOrEmpty() || it.memo.contains("Shabbat") || it.memo.contains("Parashat"))) {
                                res += " " + getString(R.string.candleLighting) + " " + truncDate(it.date)
                                textViewClock3.text = res
                                editor.putString("candles", getString(R.string.candleLighting) + " " + truncDate(it.date))
                                editor.apply()
                            }
                            else if (it.category == "havdalah" && (it.memo.isNullOrEmpty() || it.memo.contains("Shabbat"))) {
                                res += "\n" + getString(R.string.havdalah) + " " +  truncDate(it.date)
                                textViewClock3.text = res
                                editor.putString("havdalah", getString(R.string.havdalah) + " " +  truncDate(it.date))
                                editor.apply()
                            }
                            else if (it.category == "mevarchim") {
                                // hebrew = מברכים חודש שבט
                                res += "\n" + it.hebrew + " " +  "\nהמולד: " + it.memo.
                                    substring(it.memo.indexOf(": ") + 2).
                                        replace("chalakim", "חלקים").
                                    replace("and", "ו-").
                                        // replace("Molad", "מולד").
                                        replace("Sunday", "ראשון").
                                        replace("Monday", "שני").
                                        replace("Tuesday", "שלישי").
                                        replace("Wednesday", "רביעי").
                                        replace("Thursday", "חמישי").
                                        replace("Friday", "שישי").
                                        replace("Saturday", "שבת") + "\n"

                                // todo this doesn't work, maybe because the string is too long for the mobile screen?!
                                val spannableStringYomi = SpannableString(res)
                                spannableStringYomi.setSpan(UnderlineSpan(), res.indexOf(it.hebrew), res.indexOf(it.hebrew) + it.hebrew.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                                textViewClock3.text = spannableStringYomi

                                val str: String = it.hebrew
                                textViewClock3.setOnClickListener {
                                    val browserIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        // ("https://he.wikipedia.org/wiki/" + str ).toUri()
                                        ("https://he.wikipedia.org/wiki/" + str.substring(" מברכים חודש ".length-1) + (if (str.contains("שבט"))  "_(חודש)" else "")).toUri()
                                    )
                                    startActivity(browserIntent)
                                }
                            }
                        }
                        // textViewClock3.text = res
                    } else {
                        Log.w("myquietwave", "MainActivity fetchParasha Error: ${response.code()}")
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

                if (Character.isDigit(loc.trim().get(0)))  {
                    if (loc.contains(",") && Character.isDigit(loc.split(",")[1].trim().get(0)))
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
                                " " + getString(R.string.sunrise) + " " + truncDate(hebcal.times.sunrise)
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
        return " " + date.substring(date.indexOf("T")+1, date.indexOf("T")+1 +5) + " "
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
                                            ("https://he.wikipedia.org/wiki/" + str.substring(" ראש חודש ".length - 1) + (if (str.contains("שבט"))  "_(חודש)" else "")).toUri()
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

                                val fullText =  " שבת " + it.hebrew
                                val spannableString = SpannableString(fullText)
                                spannableString.setSpan(UnderlineSpan(), " שבת ".length, fullText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                textViewClock2.text = spannableString

                                editor.putString("parashat", " שבת " + it.hebrew)
                                editor.apply()

                                var str: String = it.hebrew
								
				if (str.contains("-"))
					str = str.split("-")[0]
								
                                textViewClock2.setOnClickListener {
                                    val browserIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        ("https://he.wikipedia.org/wiki/" + str.replace(" ", "_")).toUri()
                                    )
                                    startActivity(browserIntent)
                                }

                                val fullTextH =  " הפטרה " + it.leyning.haftarah.replace("|", "\n")
                                val spannableStringH = SpannableString(fullTextH)
                                spannableStringH.setSpan(UnderlineSpan(), " הפטרה ".length, fullTextH.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                textViewClockH.text = spannableStringH

                                editor.putString("haftarah", " הפטרה " + it.leyning.haftarah.replace("|", "\n"))
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

                                    val fullTextHS =  " הפטרה ספרדים " + it.leyning.haftarah_sephardic.replace("|", "\n")
                                    val spannableStringHS = SpannableString(fullTextHS)
                                    spannableStringHS.setSpan(UnderlineSpan(), " הפטרה ספרדים ".length, fullTextHS.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    textViewClockHS.text = spannableStringHS

                                    editor.putString("haftarah_sephardic", " הפטרה ספרדים " + it.leyning.haftarah_sephardic.replace("|", "\n"))
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
                        textViewClock2.text = sharedPreferences.getString("parashat", "")
                        textViewClockH.text = sharedPreferences.getString("haftarah", "")
                        textViewClockHS.text = sharedPreferences.getString("haftarah_sephardic", "")
                    }
                }

                override fun onFailure(call: Call<HebCal>, t: Throwable) {
                    Log.w("myquietwave", "MainActivity fetchParasha unable to fetch hebCal $t", t)
                    textViewClock2.text = sharedPreferences.getString("parashat", "")
                    textViewClockH.text = sharedPreferences.getString("haftarah", "")
                    textViewClockHS.text = sharedPreferences.getString("haftarah_sephardic", "")
                }
            })
        } catch (e: Exception) {
            Log.e("myquietwave", "MainActivity fetchParasha Exception $e", e)
            textViewClock2.text = sharedPreferences.getString("parashat", "")
            textViewClockH.text = sharedPreferences.getString("haftarah", "")
            textViewClockHS.text = sharedPreferences.getString("haftarah_sephardic", "")
            Firebase.crashlytics.log("MainActivity fetchParasha Exception")
            Firebase.crashlytics.recordException(e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAnalytics = Firebase.analytics

        Log.i("myquietwave", "MainActivity Version " + BuildConfig.VERSION_NAME)

        setContentView(R.layout.activity_main)

        editTextLocation = findViewById(R.id.editTextLocation)

        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val savedLocation = sharedPreferences.getString("location", "IL-Jerusalem")
        val savedStation = sharedPreferences.getString("station", "GLZ")
        val justRadio = sharedPreferences.getString("justRadio", "false")

        editTextLocation.text = savedLocation
        val locations = resources.getStringArray(R.array.locations)

        val spinner = findViewById<Spinner>(R.id.editTextLocationSpinner)
        val stationsSpinner = findViewById<Spinner>(R.id.editTextStationSpinner)

        if (savedStation != null) {
            when (savedStation) {
                "גלי צהל"    -> stationsSpinner.setSelection(0)
                "גלגלצ"  -> stationsSpinner.setSelection(1)
                "רשת ב"    -> stationsSpinner.setSelection(2)
                "רשת ג" -> stationsSpinner.setSelection(3)
                "FM102"  -> stationsSpinner.setSelection(4)
                "גלי ישראל" -> stationsSpinner.setSelection(5)
                "כאן 88" -> stationsSpinner.setSelection(6)
                "קול חי"  -> stationsSpinner.setSelection(7)
                "קול חי מיוזיק" -> stationsSpinner.setSelection(8)
                "קול ברמה" -> stationsSpinner.setSelection(9)
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
                        .get(LocalDate.now().dayOfWeek.value) + " " + SimpleDateFormat("dd-M-yyyy").format(Date()) // Cannot format given Object as a Date

                }
                Thread.sleep(500)
            }
        }.start()

        // align UI if needed

        isServiceRunning = VolumeCycleService.isRunning
        Log.i("myquietwave", "MainActivity isRunning: $isServiceRunning")

        if (! isServiceRunning) {
            toggleButton.text = getString(R.string.start)

            editTextNumberNewsDuration.isEnabled = true && ! radioPlayer.isChecked
            editTextNumberNewsDuration.isClickable = true && ! radioPlayer.isChecked

            textViewNextNews.isEnabled = true && ! radioPlayer.isChecked
            textViewNextNews.isClickable = true && ! radioPlayer.isChecked

            stationsSpinner.isEnabled = true
            stationsSpinner.isClickable = true

            toggleButton.setBackgroundColor(if (isDarkThemeOn()) Color.Black.toArgb() else Color.White.toArgb())
        }
        else {
            toggleButton.text = getString(R.string.stop)

            editTextNumberNewsDuration.isEnabled = false
            editTextNumberNewsDuration.isClickable = false

            textViewNextNews.isEnabled = false
            textViewNextNews.isClickable = false

            stationsSpinner.isEnabled = false
            stationsSpinner.isClickable = false

            toggleButton.setBackgroundColor(Color.Green.toArgb())
        }

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
            editTextNumberNewsDuration.isEnabled = ! radioPlayer.isChecked
            textViewNextNews.isEnabled = ! radioPlayer.isChecked
        }
        if (justRadio == "true") {
            editTextNumberNewsDuration.isEnabled = false
            textViewNextNews.isEnabled = false
            radioPlayer.isChecked = true
        }

        toggleButton.setOnClickListener {
            // Log.d("myquietwave", "MainActivity isServiceRunning: " + isServiceRunning)

            if (mediaPlayer?.isPlaying == true)
                mediaPlayer?.stop()

            if (isServiceRunning) {

                val serviceIntent = Intent(this, VolumeCycleService::class.java)
                stopService(serviceIntent)
                statusText.text = getString(R.string.title_name_disabled, "" /*BuildConfig.VERSION_NAME*/)
                toggleButton.text = getString(R.string.start)

                editTextNumberNewsDuration.isEnabled = true && ! radioPlayer.isChecked
                editTextNumberNewsDuration.isClickable = true && ! radioPlayer.isChecked

                textViewNextNews.isEnabled = true && ! radioPlayer.isChecked
                textViewNextNews.isClickable = true && ! radioPlayer.isChecked

                stationsSpinner.isEnabled = true
                stationsSpinner.isClickable = true
                radioPlayer.isEnabled = true

                toggleButton.setBackgroundColor(if (isDarkThemeOn()) Color.Black.toArgb() else Color.White.toArgb())

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
                if (textViewNextNews.text.toString() == "") {
                    textViewNextNews.text = NEXT_HOURS
                }

                serviceIntent.putExtra("newsDuration", newsDuration)
                serviceIntent.putExtra("nextHours", textViewNextNews.text.toString())
                serviceIntent.putExtra("station", stationsSpinner.getSelectedItem().toString())
                serviceIntent.putExtra("todoList", editTextTodo.text.toString())
                serviceIntent.putExtra("location", editTextLocation.text.toString())
                serviceIntent.putExtra("radioPlayer", if (radioPlayer.isChecked()) "true" else "false" )

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
                            if (! this.isFinishing) {
                                dialog!!.cancel()
                            }
                        }
                        val alertDialog = alertDialogBuilder.create()
                        alertDialog.show()

                        Thread {

                            val audioManager = this.getSystemService(AUDIO_SERVICE) as AudioManager
                            for (i in 1..30) {
                                if (! alertDialog.isShowing) {
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
                                        if (! this.isFinishing) {
                                            alertDialog.cancel()
                                        }
                                    }
                                }
                                Thread.sleep(1000)
                            }
                            if (! this.isFinishing) {
                                alertDialog.cancel()
                            }
                        }.start()
                    }

                    statusText.text = getString(R.string.title_name_enabled, "" /*BuildConfig.VERSION_NAME*/)
                    toggleButton.text = getString(R.string.stop)

                    toggleButton.setBackgroundColor(Color.Green.toArgb())

                    editTextNumberNewsDuration.isEnabled = false
                    editTextNumberNewsDuration.isClickable = false

                    textViewNextNews.isEnabled = false
                    textViewNextNews.isClickable = false

                    stationsSpinner.isEnabled = false
                    stationsSpinner.isClickable = false
                    radioPlayer.isEnabled = false

                    isServiceRunning = true
                }
            }
        }

        checkForAppUpdate()

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