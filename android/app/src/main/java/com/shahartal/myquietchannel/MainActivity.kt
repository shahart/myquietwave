package com.shahartal.myquietchannel

//import android.R
//import android.app.Notification
//import android.app.NotificationChannel
//import android.content.Context
//import android.media.MediaMetadata
//import android.media.session.MediaController
//import android.media.session.MediaSessionManager
//import android.media.session.PlaybackState
import android.Manifest
import android.app.AlertDialog
//import android.app.Notification
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.icu.util.HebrewCalendar
import android.location.Location
import android.location.LocationManager
import android.media.AudioManager
//import android.net.Uri
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
//import android.widget.ImageView
import android.widget.TextView
//import android.widget.Toast
import androidx.activity.ComponentActivity
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ActivityCompat
//import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
//import com.google.android.play.core.review.ReviewException
//import com.google.android.play.core.review.ReviewManagerFactory
//import com.google.android.play.core.review.model.ReviewErrorCode

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
//import com.shahartal.myquietchannel.VolumeCycleService.Companion.CHANNEL_ID
import com.shahartal.myquietchannel.parasha.HebCal
import com.shahartal.myquietchannel.parasha.HebCalZmanimModel
import com.shahartal.myquietchannel.parasha.RetrofitInstance
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Character
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date
import java.util.Timer
import java.util.TimerTask

class MainActivity : ComponentActivity() {

    private var isServiceRunning = false

    private lateinit var statusText: TextView
    private lateinit var shabesText: TextView
    private lateinit var toggleButton: Button
    private lateinit var shareButton: Button
    private lateinit var rateButton: Button

    private lateinit var editTextNumberNewsDuration: TextView
    // private lateinit var editTextNumberVolume: TextView
    private lateinit var editTextNumberEveryHour: TextView
    private lateinit var textViewNextNews: TextView

    // private lateinit var textViewNewsGlz: TextView
    private lateinit var textViewNewsLinks: TextView
    private lateinit var textViewPosition: TextView
    // private lateinit var textViewNewsKan: TextView

    private lateinit var textViewClock : TextView
    private lateinit var textViewClock2 : TextView
    private lateinit var textViewClockH : TextView
    private lateinit var textViewClock3 : TextView
    private lateinit var textViewClock4dafYomi : TextView
    private lateinit var textViewClock5suns : TextView


    private lateinit var editTextTodo : TextView
    private lateinit var editTextLocation : TextView


    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    fun Context.isDarkThemeOn(): Boolean {
        return resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
    }

//    @RequiresApi(Build.VERSION_CODES.N)
//    fun requestPermissions() {
//        val locationPermissionRequest = registerForActivityResult(
//            ActivityResultContracts.RequestMultiplePermissions()
//        ) { permissions ->
//            when {
//
//                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
//                    // Only approximate location access granted.
//                }
//                else -> {
//                    // No location access granted.
//                }
//            }
//        }
//
//        // Before you perform the actual permission request, check whether your app
//        // already has the permissions, and whether your app needs to show a permission
//        // rationale dialog. For more details, see Request permissions:
//        // https://developer.android.com/training/permissions/requesting#request-permission
//        locationPermissionRequest.launch(
//            arrayOf(
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            )
//        )
//    }

    override fun onResume() {
        super.onResume()

        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val savedName = sharedPreferences.getString("todoList", "פלטה, מיחם, שעון שבת, מנורה קטנה במסדרון, מזגן")
        val savedLocation = sharedPreferences.getString("location", "IL-Jerusalem")

        editTextTodo.text = savedName
        editTextLocation.text = savedLocation

        val locations = resources.getStringArray(R.array.locations)
        val spinner = findViewById<Spinner>(R.id.editTextLocationSpinner)

        if (savedLocation != null && locations.contains(Utils.convertLocationIL(savedLocation))) {
                spinner.setSelection(locations.indexOf(Utils.convertLocationIL(savedLocation)))
        }
        else {
            spinner.setSelection(locations.indexOf("Geo/ GPS-Lat, Lon"))
        }


        fetchShabatZmanim()
        fetchSunsZmanim()

        var newsDuration = sharedPreferences.getInt("newsDuration", 4)
        if (newsDuration > VolumeCycleService.max_news_duration) newsDuration = VolumeCycleService.max_news_duration
        if ("test5" == savedName && newsDuration > 5-1)
            newsDuration = 4
        if (newsDuration < 1)
            newsDuration = 1
        editTextNumberNewsDuration.text = newsDuration.toString()

        var everyHour = sharedPreferences.getInt("everyHour", 4)
        if (everyHour > 12) everyHour = 12
        if (everyHour < 1) everyHour = 1
        editTextNumberEveryHour.text = everyHour.toString()

        val serviceIntent = Intent(this, VolumeCycleService::class.java)
        if (VolumeCycleService.isRunning) {
            textViewNextNews.text = getEveryHourStr(VolumeCycleService.startHour)
            if (! alertMediaIsPlaying("onResume")) {
                stopService(serviceIntent)
                isServiceRunning = false
                textViewNextNews.text = getEveryHourStr()
            }
        }
        else {
            stopService(serviceIntent)
            isServiceRunning = false
            textViewNextNews.text = getEveryHourStr()
        }

        if (isServiceRunning) {
            statusText.text = getString(R.string.title_name_enabled, "" /*BuildConfig.VERSION_NAME*/)
            toggleButton.text = getString(R.string.stop)
            toggleButton.setBackgroundColor(Color.Green.toArgb())
        }
        else {
            statusText.text = getString(R.string.title_name_disabled, "" /*BuildConfig.VERSION_NAME*/)
            toggleButton.text = getString(R.string.start)
            toggleButton.setBackgroundColor(if (isDarkThemeOn()) Color.Black.toArgb() else Color.White.toArgb())
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
        editor.putString("location", editTextLocation.text.toString())

        val newsDurationStr = editTextNumberNewsDuration.text.toString()
        var newsDuration = if (newsDurationStr.isEmpty()) 4 else newsDurationStr.toInt()
        if (newsDuration > VolumeCycleService.max_news_duration) newsDuration = VolumeCycleService.max_news_duration
        if ("test5" == editTextTodo.text.toString() && newsDuration > 5-1)
            newsDuration = 4
        if (newsDuration < 1)
            newsDuration = 1
        editor.putInt("newsDuration", newsDuration)

        val everyHourStr = editTextNumberEveryHour.text.toString()
        var everyHour = if (everyHourStr.isEmpty()) 4 else everyHourStr.toInt()
        if (everyHour > 12) everyHour = 12
        if (everyHour < 1) everyHour = 1
        editor.putInt("everyHour", everyHour)

        editor.apply()
    }

    fun getEveryHourStr(now: Int = -1): String {
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

        // @RequiresApi(8
        var nextHour = if (now == -1) ZonedDateTime.now(ZoneId.systemDefault()).hour + 1 else now + 1 //  + everyHours
        if (nextHour > 24) nextHour -= 24
        // Log.d("", "MainActivity nextHour: " + nextHour)

        var nextHoursStr = // "החדשות הבאות תהיינה בשעות " +
            nextHour.toString()

        var plusHours = everyHours

        // the real code:

        nextHour += everyHours
        if (nextHour > 24) nextHour -= 24
        nextHoursStr += ", $nextHour"

        while (plusHours < 24) {
            nextHour += everyHours
            plusHours += everyHours
            if (nextHour > 24) {
                nextHour -= 24
            }
            nextHoursStr += ", $nextHour"
        }

        return nextHoursStr
    }

    fun fetchDafYomi() {
        textViewClock4dafYomi = findViewById(R.id.textViewClock4dafYomi)
        textViewClock4dafYomi.text = ""
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        var res: String = ""
        try {
            val start = SimpleDateFormat("yyyy-MM-dd").format(Date())
            RetrofitInstance.api.getDafYomi(start, start).enqueue(object : Callback<HebCal> {

                override fun onResponse(call: Call<HebCal>, response: Response<HebCal>) {
                    if (response.isSuccessful) {
                        val hebcal = response.body()
                        hebcal?.items?.forEach { it ->
                            if (it.category == "dafyomi") {
                                textViewClock4dafYomi.text = " הדף היומי " + it.hebrew
                                res = it.link
                            }
                        }
                        if (res.isNotEmpty()) {
                            textViewClock4dafYomi.setOnClickListener {
                                val browserIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    res.toUri()
                                )
                                startActivity(browserIntent)
                            }
                        }
                    } else {
                        Log.w("", "MainActivity fetchDafYomi Error: ${response.code()}")
                        textViewClock4dafYomi.text = sharedPreferences.getString("dafYomi", "")
                    }
                }

                override fun onFailure(call: Call<HebCal>, t: Throwable) {
                    Log.w("", "MainActivity fetchDafYomi unable to fetch hebCal $t", t)
                    textViewClock4dafYomi.text = sharedPreferences.getString("dafYomi", "")
                }
            })
        } catch (e: Exception) {
            Log.e("", "MainActivity fetchDafYomi Exception $e", e)
            textViewClock4dafYomi.text = sharedPreferences.getString("dafYomi", "")
        }
    }

    fun fetchShabatZmanim() {
        textViewClock3 = findViewById(R.id.textViewClock3)
        textViewClock3.text = ""

        // val dow = ZonedDateTime.now(ZoneId.systemDefault()).dayOfWeek
        if ( // (dow == DayOfWeek.THURSDAY || dow == DayOfWeek.FRIDAY || dow == DayOfWeek.SATURDAY) &&
            editTextLocation.text.toString().trim().isNotEmpty()) {

            val firstItem = editTextLocation.text.toString().trim()
            fetchShabatZmanim(firstItem)
        }
        else {
        }


    }

    fun fetchShabatZmanim(loc: String) { // }: String {

        textViewClock3 = findViewById(R.id.textViewClock3)

        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)

        var res: String

        if ((loc.trim().get(0).isLetter())) {
            res = loc + " "
        } else {
            res = " "
        }

        try {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                Log.w("","MainActivity fetchParasha no internet at all")
//                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), 112); // REQUEST_INTERNET_PERMISSION);
//            }

            val call = // if (loc.get(0).isDigit()) RetrofitInstance.api.getShabbatByLoc(loc.split(",")[0].trim(), loc.split(",")[1].trim())
                // else
                if (Character.isDigit(loc.trim().get(0))) {
                    if (loc.contains(",") && Character.isDigit(loc.split(",")[1].trim().get(0)))
                        RetrofitInstance.api.getShabbatByLoc(loc.split(",")[0].trim(), loc.split(",")[1].trim(), Utils.getUe(loc))
                    else
                        RetrofitInstance.api.getShabbatPerGeoNameId(Utils.getCity(loc), Utils.getUe(loc))
                }
                else
                    RetrofitInstance.api.getShabbatPerCity(Utils.getCity(loc), Utils.getUe(loc))

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
                                res += " " + getString(R.string.havdalah) + " " +  truncDate(it.date)
                                textViewClock3.text = res
                                editor.putString("havdalah", getString(R.string.havdalah) + " " +  truncDate(it.date))
                                editor.apply()
                            }
                        }
                        // textViewClock3.text = res
                    } else {
                        Log.w("", "MainActivity fetchParasha Error: ${response.code()}")
                        // textViewClock3.text = "" // ""Not found " + response.code()
                        res += " " + sharedPreferences.getString("candles", "") + " " + sharedPreferences.getString("havdalah", "")
                        textViewClock3.text = res
                    }
                }

                override fun onFailure(call: Call<HebCal>, t: Throwable) {
                    Log.w("", "MainActivity fetchZmanim unable to fetch hebCal $t", t)
                    // textViewClock3.text = "" // ""Failure. Not found " + t
                    res += " " + sharedPreferences.getString("candles", "") + " " + sharedPreferences.getString("havdalah", "")
                    textViewClock3.text = res
                }
            })
        } catch (e: Exception) {
            Log.e("", "MainActivity fetchZmanim Exception $e", e)
            // textViewClock3.text = "" // ""Error. Not found " + e
            res += " " + sharedPreferences.getString("candles", "") + " " + sharedPreferences.getString("havdalah", "")
            textViewClock3.text = res
        }
    }

    fun fetchSunsZmanim() {
        textViewClock5suns = findViewById(R.id.textViewClock5suns)
        textViewClock5suns.text = ""

        // val dow = ZonedDateTime.now(ZoneId.systemDefault()).dayOfWeek
        if ( // (dow == DayOfWeek.THURSDAY || dow == DayOfWeek.FRIDAY || dow == DayOfWeek.SATURDAY) &&
            editTextLocation.text.toString().trim().isNotEmpty()) {

            val firstItem = editTextLocation.text.toString().trim()
            fetchSunsZmanim(firstItem)
        }

    }

    fun fetchSunsZmanim(loc: String) { // }: String {

        textViewClock5suns = findViewById(R.id.textViewClock5suns)

        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)

        var res: String

        res = " "

        try {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                Log.w("","MainActivity fetchParasha no internet at all")
//                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), 112); // REQUEST_INTERNET_PERMISSION);
//            }

            val call =
            // if (loc.get(0).isDigit()) RetrofitInstance.api.getShabbatByLoc(loc.split(",")[0].trim(), loc.split(",")[1].trim())
                // else
                if (Character.isDigit(loc.trim().get(0)))  {
                    if (loc.contains(",") && Character.isDigit(loc.split(",")[1].trim().get(0)))
                        RetrofitInstance.api.getZmanimByLoc(
                            loc.split(",")[0].trim(),
                            loc.split(",")[1].trim(),
                            Utils.getUe(loc)
                        )
                    else
                        RetrofitInstance.api.getZmanimPerGeoNameId(Utils.getCity(loc), Utils.getUe(loc))
                }
            else
                RetrofitInstance.api.getZmanimPerCity(Utils.getCity(loc), Utils.getUe(loc))

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
                            res += " " + getString(R.string.sunset) + " " + truncDate(hebcal.times.sunset)

                            res = hebcal.location.title + " " + res

                            textViewClock5suns.text = res
                            editor.putString(
                                "sunset",
                                getString(R.string.sunset) + " " + truncDate(hebcal.times.sunset)
                            )

                            editor.apply()
                            // textViewClock3.text = res
                        }
                    } else {
                        Log.w("", "MainActivity fetchSunsZmanim Error: ${response.code()}")
                        // textViewClock3.text = "" // ""Not found " + response.code()
                        res += " " + sharedPreferences.getString("sunrise", "") + " " + sharedPreferences.getString("sunset", "")
                        textViewClock5suns.text = res
                    }
                }

                override fun onFailure(call: Call<HebCalZmanimModel>, t: Throwable) {
                    Log.w("", "MainActivity fetchSunsZmanim unable to fetch hebCal $t", t)
                    // textViewClock3.text = "" // ""Failure. Not found " + t
                    res += " " + sharedPreferences.getString("sunrise", "") + " " + sharedPreferences.getString("sunset", "")
                    textViewClock5suns.text = res
                }
            })
        } catch (e: Exception) {
            Log.e("", "MainActivity fetchSunsZmanim Exception $e", e)
            // textViewClock3.text = "" // ""Error. Not found " + e
            res += " " + sharedPreferences.getString("sunrise", "") + " " + sharedPreferences.getString("sunset", "")
            textViewClock5suns.text = res
        }
    }


    fun truncDate(date: String): String {
        return " " + date.substring(date.indexOf("T")+1, date.indexOf("T")+1 +5) + " "
    }

    fun fetchParasha() { // }: String {

        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        textViewClock2 = findViewById(R.id.textViewClock2)
        textViewClockH = findViewById(R.id.textViewClockH)

        try {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
//                Log.w("","MainActivity fetchParasha no internet at all")
//                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), 112); // REQUEST_INTERNET_PERMISSION);
//            }

            RetrofitInstance.api.getShabbatPerCity("IL-Jerusalem", "off").enqueue(object : Callback<HebCal> {

                override fun onResponse(call: Call<HebCal>, response: Response<HebCal>) {
                    if (response.isSuccessful) {
                        val editor = sharedPreferences.edit()
                        // val str = response.body()
                        // Log.i("", "MainActivity fetchParasha " + str)
                        val hebcal = response.body()
                        hebcal?.items?.forEach {
                            if (it.category == "parashat") {
                                // return it.hebrew;

                                val fullText =  " שבת " + it.hebrew
                                val spannableString = SpannableString(fullText)
                                spannableString.setSpan(UnderlineSpan(), " שבת ".length, fullText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                textViewClock2.text = spannableString

                                editor.putString("parashat", " שבת " + it.hebrew)
                                editor.apply()

                                val str: String = it.hebrew
                                textViewClock2.setOnClickListener {
                                    val browserIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        ("https://he.wikipedia.org/wiki/" + str.replace(" ", "_")).toUri()
                                    )
                                    startActivity(browserIntent)
                                }


                                val fullTextH =  " הפטרה " + it.leyning.haftarah
                                val spannableStringH = SpannableString(fullTextH)
                                spannableStringH.setSpan(UnderlineSpan(), " הפטרה ".length, fullTextH.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                textViewClockH.text = spannableStringH

                                editor.putString("haftarah", " הפטרה " + it.leyning.haftarah)
                                editor.apply()

                                val strH: String = it.leyning.haftarah.split(':')[0]
                                textViewClockH.setOnClickListener {
                                    val browserIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        ("https://shahart.github.io/heb-bible/index.html?b=" + strH).toUri()
                                    )
                                    startActivity(browserIntent)
                                }

                            }
                        }
                    } else {
                        Log.w("", "MainActivity fetchParasha Error: ${response.code()}")
                        textViewClock2.text = sharedPreferences.getString("parashat", "")
                        textViewClockH.text = sharedPreferences.getString("haftarah", "")
                    }
                }

                override fun onFailure(call: Call<HebCal>, t: Throwable) {
                    Log.w("", "MainActivity fetchParasha unable to fetch hebCal $t", t)
                    textViewClock2.text = sharedPreferences.getString("parashat", "")
                    textViewClockH.text = sharedPreferences.getString("haftarah", "")
                }
            })
        } catch (e: Exception) {
            Log.e("", "MainActivity fetchParasha Exception $e", e)
            textViewClock2.text = sharedPreferences.getString("parashat", "")
            textViewClockH.text = sharedPreferences.getString("haftarah", "")
        }
    }


    // @RequiresApi(Build.VERSION_CODES.O) // Unnecessary; SDK_INT is always >= 26
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAnalytics = Firebase.analytics

        Log.i("", "MainActivity Version " + BuildConfig.VERSION_NAME)

        setContentView(R.layout.activity_main)

        editTextLocation = findViewById(R.id.editTextLocation)

        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val savedLocation = sharedPreferences.getString("location", "IL-Jerusalem")

        editTextLocation.text = savedLocation
        val locations = resources.getStringArray(R.array.locations)

        val spinner = findViewById<Spinner>(R.id.editTextLocationSpinner)


        if (savedLocation != null && locations.contains(Utils.convertLocationIL(savedLocation))) {
            spinner.setSelection(locations.indexOf(Utils.convertLocationIL(savedLocation)))
        }
        else {
            spinner.setSelection(locations.indexOf("Geo/ GPS-Lat, Lon"))
        }

        if (spinner != null) {
            val adapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, locations)

            spinner.adapter = adapter

            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>,
                                            view: View, position: Int, id: Long) {
                    if (locations[position] != "Geo/ GPS-Lat, Lon" && locations[position].isNotEmpty()) {
                        editTextLocation.text = Utils.convertFromLocationIL(locations[position])
                    }
                    fetchShabatZmanim()
                    fetchSunsZmanim()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // TODO?
                }
            }
        }

        // @RequiresApi(8
        if (ZonedDateTime.now(ZoneId.systemDefault()).dayOfWeek == DayOfWeek.FRIDAY) {
            shabesText = findViewById(R.id.textViewShabes)
            shabesText.text = getString(R.string.shabbath)
        }

        fetchParasha()
        fetchDafYomi()

        editTextTodo = findViewById(R.id.editTextTodo)

        /* val infoIcon: ImageView = findViewById(R.id.info_icon)
        infoIcon.setOnClickListener {
            Toast.makeText(this, "Here you can place your city, with a comma, for Candle lighting and Havdalah times", Toast.LENGTH_LONG).show();
        }*/

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        alertMediaIsPlaying("onCreate")

        shareButton = findViewById(R.id.shareButton)
        rateButton = findViewById(R.id.rateButton)

        shareButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.setType("text/plain")
            val shareLink = "https://play.google.com/store/apps/details?id=$packageName"
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + " " + shareLink)
            startActivity(Intent.createChooser(shareIntent, "Share this app"))
        }

        rateButton.setOnClickListener {
/*
            val manager = ReviewManagerFactory.create(applicationContext )
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // We got the ReviewInfo object
                    val reviewInfo = task.result

                    val flow = manager.launchReviewFlow(this, reviewInfo)
                    flow.addOnCompleteListener { l ->
                        Log.i("", "inapp review finished successfully")
                        // The flow has finished. The API does not indicate whether the user
                        // reviewed or not, or even whether the review dialog was shown. Thus, no
                        // matter the result, we continue our app flow.
                    }

                } else {
                    // There was some problem, log or handle the error code.
                    @ReviewErrorCode val reviewErrorCode = (task.getException() as ReviewException).errorCode

                    Log.e("", "MainActivity reviewManager.requestReviewFlow error code: $reviewErrorCode")

                }
            }
*/
            try {
                startActivity(Intent(Intent.ACTION_VIEW,
                    ("market://details?id=$packageName").toUri()))
            } catch (_: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW,
                    ("https://play.google.com/store/apps/details?id=$packageName").toUri()))
            }
        }

        statusText = findViewById(R.id.statusText)
        toggleButton = findViewById(R.id.toggleButton)
        editTextNumberNewsDuration = findViewById(R.id.editTextDuration)
        // editTextNumberVolume = findViewById(R.id.editTextNumberVolumePercentage)
        editTextNumberEveryHour = findViewById(R.id.editTextEveryHour)
        textViewNextNews = findViewById(R.id.textViewNextNewsStr)

        textViewNewsLinks = findViewById(R.id.textView14)
        textViewNewsLinks.setOnClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                "https://shahart.github.io/automations/links.html".toUri()
            )
            startActivity(browserIntent)
        }

        textViewPosition = findViewById(R.id.textViewLocationLabel)
        textViewPosition.setOnClickListener {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 0)
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                var isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                if (! isGpsEnabled) {
                    Log.w("", "MainActivity fetchZmanim location isGpsDisabled")
                }
                else {
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

                    fusedLocationClient.lastLocation

                        .addOnSuccessListener { location: Location? ->
                            Log.i("", "MainActivity fetchZmanim location success " + location)
                            if (location != null) {
                                // firebaseAnalytics.logEvent(FirebaseAnalytics.Param.LOCATION) {
                                // }
                                val locStr = Utils.roundToDecimalPlaces(location.latitude).toString() + "," +
                                        Utils.roundToDecimalPlaces(location.longitude).toString()
                                editTextLocation.text = locStr
                                spinner.setSelection(locations.indexOf("Geo/ GPS-Lat, Lon"))

                                fetchShabatZmanim(locStr)

                                val alertDialogBuilder = AlertDialog.Builder(this)
                                alertDialogBuilder.setMessage(getString(R.string.ue_usage) + " -- Version: " + BuildConfig.VERSION_NAME)
                                alertDialogBuilder.setNegativeButton(getString(R.string.close_alert)) { dialog: DialogInterface?, which: Int ->
                                    if (! this.isFinishing) {
                                        dialog!!.cancel()
                                    }
                                }
                                val alertDialog = alertDialogBuilder.create()
                                alertDialog.show()

                            }
                        }
                        .addOnFailureListener { exception: Exception ->
                            Log.w("", "MainActivity fetchZmanim location failure " + exception)
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
                if ("test5" == editTextTodo.text.toString() && newsDuration > 5-1) {
                    editTextNumberNewsDuration.text = "4"
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
                if ("test5" == editTextTodo.text.toString() && newsDuration > 5-1) {
                    editTextNumberNewsDuration.text = "4"
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
                    val hebrewCalendar = HebrewCalendar()
                    val hebrewYear = Utils.getYY(hebrewCalendar.get(HebrewCalendar.YEAR))
                    val hebrewMonth = hebrewCalendar.get(HebrewCalendar.MONTH)
                    val hebrewDay = hebrewCalendar.get(HebrewCalendar.DAY_OF_MONTH) // switches at midnight by-design
                    val hebrewDays = arrayOf("א", "ב", "ג", "ד", "ה", "ו", "ז", "ח", "ט", "י", "יא", "יב", "יג", "יד", "טו", "טז", "יז", "יח", "יט", "כ", "כא", "כב", "כג", "כד", "כה", "כו", "כז", "כח", "כט", "ל")
                    val hebrewMonths = arrayOf("תשרי", "חשון", "כסלו", "טבת", "שבט", "אדר", "אדר שני", "ניסן", "אייר", "סיוון", "תמוז", "אב", "אלול")
                    val hebrewMonthName = hebrewMonths[hebrewMonth]
                    val hebrewDayName = hebrewDays[hebrewDay-1]
                    var txt = "$hebrewDayName/$hebrewMonthName/$hebrewYear"
                    // @RequiresApi(8
                    txt += " - " +
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("H:mm:ss"))
                    textViewClock.text = txt
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

            toggleButton.setBackgroundColor(if (isDarkThemeOn()) Color.Black.toArgb() else Color.White.toArgb())
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

            if (isServiceRunning) {

                val serviceIntent = Intent(this, VolumeCycleService::class.java)
                stopService(serviceIntent)
                textViewNextNews.text = getEveryHourStr()
                statusText.text = getString(R.string.title_name_disabled, "" /*BuildConfig.VERSION_NAME*/)
                toggleButton.text = getString(R.string.start)

                editTextNumberNewsDuration.isEnabled = true
                editTextNumberNewsDuration.isClickable = true

                editTextNumberEveryHour.isEnabled = true
                editTextNumberEveryHour.isClickable = true

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
                if ("test5" == editTextTodo.text.toString() && newsDuration > 5-1) {
                    newsDuration = 4
                    editTextNumberNewsDuration.text = "4"
                }
                if (newsDuration < 1) {
                    newsDuration = 1
                    editTextNumberNewsDuration.text = "1"
                }

                serviceIntent.putExtra("newsDuration", newsDuration)

                serviceIntent.putExtra("hours", Integer.valueOf(everyHours))

                // for testing
                serviceIntent.putExtra("todoList", editTextTodo.text.toString())
                serviceIntent.putExtra("location", editTextLocation.text.toString())

                if (alertMediaIsPlaying("onClick to turn on")) {

                    // @RequiresApi(8
                    startForegroundService(serviceIntent)

                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                        param("everyHours", everyHours.toLong())
                        param("newsDuration", newsDuration.toLong())
                        // @RequiresApi(8
                        param("isFriday", if (ZonedDateTime.now(ZoneId.systemDefault()).dayOfWeek == DayOfWeek.FRIDAY) 1L else 0L)
                    }

                    textViewNextNews.text = getEveryHourStr(ZonedDateTime.now(ZoneId.systemDefault()).hour)

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

                    val alertDialogBuilder = AlertDialog.Builder(this)
                    alertDialogBuilder.setMessage(getString(R.string.next_30_sec))
                    alertDialogBuilder.setNegativeButton(getString(R.string.close_alert)) { dialog: DialogInterface?, which: Int ->
                        if (! this.isFinishing) {
                            dialog!!.cancel()
                        }
                    }
                    // alertDialog.setIcon(R.drawable.icon)
                    val alertDialog = alertDialogBuilder.create()
                    alertDialog.show()

                    Thread {

//                        val next_30_alert = (TextView) this.alertDialog.findViewById(android.R.id.message)

                        val audioManager = this.getSystemService(AUDIO_SERVICE) as AudioManager
                        for (i in 1..30) {
                            if (! alertDialog.isShowing) {
                                break
                            }
                            runOnUiThread {
                                if (isServiceRunning) {
                                    alertDialog.setMessage(getString(R.string.next_30_sec_with_sec, (31 - i),
                                        100*audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)/audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)))
                                }
                                else {
                                    if (! this.isFinishing) {
                                        alertDialog.cancel()
                                    }
                                }
//                                if (i == 30) {
//                                    alertDialog.cancel()
//                                }
                            }
                            Thread.sleep(1000)
                        }
                        if (! this.isFinishing) {
                            alertDialog.cancel()
                        }
                    }.start()

                    statusText.text = getString(R.string.title_name_enabled, "" /*BuildConfig.VERSION_NAME*/)
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

                    statusText.text = getString(R.string.title_name_disabled, "" /*BuildConfig.VERSION_NAME*/)
                    toggleButton.text = getString(R.string.start)

                    editTextNumberNewsDuration.isEnabled = true
                    editTextNumberNewsDuration.isClickable = true

                    editTextNumberEveryHour.isEnabled = true
                    editTextNumberEveryHour.isClickable = true

                    toggleButton.setBackgroundColor(if (isDarkThemeOn()) Color.Black.toArgb() else Color.White.toArgb())

                    isServiceRunning = false

                    getSystemService(NotificationManager::class.java).cancel(1)
                    textViewNextNews.text = getEveryHourStr()

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

            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle(getString(R.string.alert_title))
            alertDialog.setMessage(getString(R.string.alert_message))
            alertDialog.setNegativeButton(getString(R.string.close_alert)) { dialog: DialogInterface?, which: Int ->
                if (! this.isFinishing) {
                    dialog!!.cancel()
                }
            }
            // alertDialog.setIcon(R.drawable.icon)
            val dialog = alertDialog.create()
//            alertDialog.create().show()

            dialog.show()

            val timer = Timer()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    dialog.dismiss()
                    timer.cancel()
                }
            }, 10_000)

            if (from == "onResume") {
                Log.w("", "MainActivity Not disabling the app, might be a network glitch")
                return true
            }

            return false
        }
        return isPlaying
    }

}