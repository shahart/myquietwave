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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
//import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.crashlytics.crashlytics
import com.shahartal.myquietchannel.luach.HebrewDate
import com.shahartal.myquietchannel.luach.Parshios
import com.shahartal.myquietchannel.databinding.ActivityMainBinding
import com.shahartal.myquietchannel.parasha.RetrofitInstance
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
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

    companion object {
        const val NEXT_HOURS = NewsSchedule.DEFAULT_TEXT
        private const val LOCATION_PERMISSION_REQUEST_CODE = 0
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 112
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var displayCache: DisplayCache

    private var isServiceRunning = false

    private lateinit var statusText: TextView
    private lateinit var shabesText: TextView
    private lateinit var toggleButton: Button
    private lateinit var shareButton: Button
    private lateinit var peekSongsButton: Button

    // private lateinit var powerButton: Button

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
    private lateinit var haftarahConnectionButton: Button

    private var haftarahConnectionSourceUrl: String? = null
    private val songRepository: SongRepository by lazy {
        NetworkSongRepository(UrlConnectionTextHttpClient())
    }
    private val hebcalRepository: HebcalRepository by lazy {
        NetworkHebcalRepository(RetrofitInstance.api)
    }
    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this, MainViewModelFactory(hebcalRepository))[MainViewModel::class.java]
    }
    private val haftarahRepository: HaftarahRepository by lazy {
        CachedHaftarahRepository(
            UrlConnectionTextHttpClient(connectTimeoutMillis = 30_000, readTimeoutMillis = 30_000)
        )
    }

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

    private fun initializeViewReferences() {
        statusText = binding.statusText
        shabesText = binding.textViewShabes
        toggleButton = binding.toggleButton
        shareButton = binding.shareButton
        peekSongsButton = binding.peekSongsButton
        editTextNumberNewsDuration = binding.editTextDuration
        textViewNextNews = binding.textViewNextNewsStr
        textViewNewsLinks = binding.textView14
        textViewPosition = binding.textViewLocationLabel
        textViewClock = binding.textViewClock
        textViewHebDate = binding.textViewHebDate
        textViewClock_2nd = binding.textViewClock2nd
        textViewClock_3rd = binding.textViewClock3rd
        textViewClock2 = binding.textViewClock2
        textViewClock2_2 = binding.textViewClock22
        textViewDate = binding.textViewDate
        textViewClockH = binding.textViewClockH
        textViewClockHS = binding.textViewClockHS
        haftarahConnectionButton = binding.haftarahConnectionButton
        textViewClock3 = binding.textViewClock3
        textViewClock4dafYomi = binding.textViewClock4dafYomi
        textViewClock4dafYomiTitle = binding.textViewClock4dafYomiTitle
        textViewOmer = binding.textViewOmer
        textViewClock5locTitle = binding.textViewClock5locTitle
        textViewClock5suns = binding.textViewClock5suns
        textViewClock6rosh = binding.textViewClock6rosh
        textViewClock7special = binding.textViewClock7special
        textViewClock8fast = binding.textViewClock8fast
        editTextTodo = binding.editTextTodo
        currentSong = binding.textViewCurrentSong
        nextSong = binding.textViewNextSong
        spinner = binding.editTextLocationSpinner
        stationsSpinner = binding.editTextStationSpinner
        editTextLocation = binding.editTextLocation
        radioPlayer = binding.radioCheckbox
    }

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

    private fun normalizedNewsDuration(): Int = PlaybackPolicy.normalizeDuration(
        editTextNumberNewsDuration.text.toString().toIntOrNull()
            ?: AppSettings.DEFAULT_NEWS_DURATION_MINUTES,
        isNearShabbat = false,
    ).also { editTextNumberNewsDuration.text = it.toString() }

    private fun underlined(text: String, start: Int = 0): SpannableString =
        SpannableString(text).apply {
            if (start in 0 until text.length) {
                setSpan(UnderlineSpan(), start, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }

    private fun showMessageDialog(message: CharSequence) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setNegativeButton(R.string.close_alert, null)
            .show()
    }

    private fun buildServiceIntent(): Pair<Intent, Int> {
        if (textViewNextNews.text.toString().isEmpty()) {
            textViewNextNews.text = NEXT_HOURS
        }

        val request = PlaybackStartRequest.fromUiValues(
            station = stationsSpinner.selectedItem?.toString(),
            newsDuration = editTextNumberNewsDuration.text.toString(),
            schedule = textViewNextNews.text.toString(),
            todo = editTextTodo.text.toString(),
            location = editTextLocation.text.toString(),
            radioOnly = radioPlayer.isChecked,
        )
        val intent = Intent(this, VolumeCycleService::class.java).apply {
            putExtra(VolumeCycleService.EXTRA_NEWS_DURATION, request.newsDurationMinutes)
            putExtra(VolumeCycleService.EXTRA_NEXT_HOURS, request.scheduleText)
            putExtra(VolumeCycleService.EXTRA_STATION, request.station.displayName)
            putExtra(VolumeCycleService.EXTRA_TODO_LIST, request.todo)
            putExtra(VolumeCycleService.EXTRA_LOCATION, request.location)
            putExtra(VolumeCycleService.EXTRA_RADIO_PLAYER, request.radioOnly)
        }
        return intent to request.newsDurationMinutes
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED ||
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
        ) {
            return
        }

        try {
            Log.i("myquietwave", "request notifications permission")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE,
            )
            Log.i("myquietwave", "MainActivity Done. request notifications permission")
        } catch (error: Exception) {
            Log.e("myquietwave", "MainActivity failed request notifications permission $error")
        }
    }

    private fun startClockUpdates() {
        lifecycleScope.launch {
            while (isActive) {
                val now = java.time.LocalDateTime.now()
                textViewClock.text = DateDisplay.clockTime(now)
                textViewDate.text = DateDisplay.calendarLabel(now.toLocalDate())
                delay(500)
            }
        }
    }

    private fun startPeriodicSongRefresh() {
        lifecycleScope.launch {
            while (isActive) {
                delay(90_000)
                try {
                    refreshCurrentSongIfNeeded()
                } catch (error: Exception) {
                    Log.w("myquietwave", "Error in periodic fetchGlglzSong", error)
                }
            }
        }
    }

    private fun refreshCurrentSongIfNeeded() {
        if (!isServiceRunning) return
        Station.fromPersistedValue(stationsSpinner.selectedItem?.toString())
            .takeIf { it.songFeedName != null }
            ?.let(::fetchGlglzSong)
    }

    override fun onResume() {
        super.onResume()

        val settings = settingsRepository.load()

        editTextTodo.text = settings.todo
        editTextLocation.text = settings.location

        val locations = resources.getStringArray(R.array.locations)
        if (locations.contains(Utils.convertLocationIL(settings.location))) {
                spinner.setSelection(locations.indexOf(Utils.convertLocationIL(settings.location)))
        }
        else {
            spinner.setSelection(locations.indexOf("Geo/ GPS-Lat, Lon"))
        }

        resources.getStringArray(R.array.stations).indexOf(settings.station.displayName)
            .takeIf { it >= 0 }
            ?.let(stationsSpinner::setSelection)

        fetchShabatZmanim()
        fetchSunsZmanim()

        editTextNumberNewsDuration.text = settings.newsDurationMinutes.toString()
        textViewNextNews.text = settings.scheduleText

        val serviceIntent = Intent(this, VolumeCycleService::class.java)
        if (! VolumeCycleService.isRunning) {
            stopService(serviceIntent)
            isServiceRunning = false
            currentSong.text = ""
            nextSong.text = ""
        }

        radioPlayer.isChecked = settings.radioOnly

        updateServiceUi()

        getSystemService(NotificationManager::class.java).cancel(VolumeCycleService.NOTIFICATION_ID)
        try {
            refreshCurrentSongIfNeeded()
        } catch (e: Exception) {
            Log.w("myquietwave", "Error in periodic fetchGlglzSong", e)
        }

    }

    override fun onPause() {
        super.onPause()

        settingsRepository.save(
            AppSettings(
                todo = editTextTodo.text.toString(),
                location = editTextLocation.text.toString(),
                station = Station.fromPersistedValue(stationsSpinner.selectedItem?.toString()),
                radioOnly = radioPlayer.isChecked,
                newsDurationMinutes = editTextNumberNewsDuration.text.toString().toIntOrNull()
                    ?: AppSettings.DEFAULT_NEWS_DURATION_MINUTES,
                scheduleText = textViewNextNews.text.toString(),
            )
        )
    }

    fun fetchDafYomi() {
        textViewOmer.text = ""
        textViewClock4dafYomi.text = ""
        mainViewModel.fetchDailyLearning()
    }

    fun fetchShabatZmanim() {
        textViewClock3.text = ""

        if ( // (dow == DayOfWeek.THURSDAY || dow == DayOfWeek.FRIDAY || dow == DayOfWeek.SATURDAY) &&
            editTextLocation.text.toString().trim().isNotEmpty()) {

            val firstItem = editTextLocation.text.toString().trim()
            fetchShabatZmanim(firstItem)
        }

    }

    fun fetchShabatZmanim(loc: String) { // }: String {
        val trimmedLocation = loc.trim()
        var res: String
        if (trimmedLocation.firstOrNull()?.isLetter() == true) {
            res = trimmedLocation + "\n"
        } else {
            res = " "
        }

        val query = try {
            requireNotNull(LocationQueryParser.parse(loc))
        } catch (error: Exception) {
            Log.e("myquietwave", "MainActivity fetchZmanim invalid location", error)
            showShabbatFallback(res)
            return
        }

        mainViewModel.fetchShabbat(query)
    }

    fun fetchSunsZmanim() {
        textViewClock5suns.text = ""

        if ( // (dow == DayOfWeek.THURSDAY || dow == DayOfWeek.FRIDAY || dow == DayOfWeek.SATURDAY) &&
            editTextLocation.text.toString().trim().isNotEmpty()) {

            val firstItem = editTextLocation.text.toString().trim()
            fetchSunsZmanim(firstItem)
        }

    }

    fun fetchSunsZmanim(loc: String) { // }: String {
        val query = LocationQueryParser.parse(loc)
        if (query == null) {
            showSunFallback(" ")
            return
        }
        mainViewModel.fetchZmanim(query)
    }
    fun fetchParasha() { // }: String {
        lifecycleScope.launch {
            try {
                val hebcal = withContext(Dispatchers.IO) { hebcalRepository.parasha() }
                        val currentItems = hebcal.items.filterNot(HebcalPresentation::isPastDailyCalendarItem)
                        val majorHolidayOnNextSaturday =
                            HebcalPresentation.majorHolidayOnNextSaturday(currentItems)
                        textViewClock6rosh.text = ""
                        textViewClock7special.text = ""
                        textViewClock8fast.text = ""
                        // val str = response.body()
                        // Log.i("myquietwave", "MainActivity fetchParasha " + str)
                        var memo = ""
                        currentItems.forEach {
                            if (it.category == "roshchodesh") {

                                textViewClock6rosh.text = textViewClock6rosh.text.toString() +
                                    ParashaPresentation.calendarEvent(it.hebrew, it.date) + "\n"
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

                                memo = ParashaPresentation.appendMemo(memo, it.hebrew, it.memo)
                            }
                            else if (it.category == "holiday") {
                                val holidayDate = it.date
                                if (! Utils.isBefore(holidayDate)) {

                                    textViewClock7special.text = textViewClock7special.text.toString() + "\n" +
                                        ParashaPresentation.calendarEvent(it.hebrew, it.date)

                                    memo = ParashaPresentation.appendMemo(memo, it.hebrew, it.memo)

                                }
                            }
                            else if (it.title == "Fast begins") {

                                textViewClock8fast.text = textViewClock8fast.text.toString() +
                                        ParashaPresentation.fastBeginning(it.date)

                            }
                            else if (it.title == "Fast ends") {

                                textViewClock8fast.text =
                                    textViewClock8fast.text.toString() + ParashaPresentation.fastEnd(it.date)

                                if (Utils.isBefore(it.date.substringBefore('T'))) {
                                    textViewClock8fast.text = ""
                                }
                            }
                           else if (it.category == "parashat") {
                                // return it.hebrew;

                                haftarahConnectionSourceUrl = HaftarahConnection.sourceUrl(it.hebrew)
                                haftarahConnectionButton.isEnabled = true

                                val parashaNames = ParashaPresentation.names(it.hebrew)
                                val str = parashaNames.primary
                                val str2 = parashaNames.secondary.orEmpty()

                                val fullText =  " שבת " + str
                                val spannableString = SpannableString(fullText)
                                spannableString.setSpan(UnderlineSpan(), " שבת ".length, fullText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                textViewClock2.text = spannableString

                                displayCache.put("parashat", " שבת " + str)

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
                                else {
                                    textViewClock2_2.text = ""
                                    textViewClock2_2.setOnClickListener(null)
                                }

                                val haftarah = ParashaPresentation.haftarah(
                                    it.leyning.haftarah,
                                    it.leyning.haftarah_sephardic,
                                )
                                val fullTextH = haftarah.ashkenazi
                                val spannableStringH = SpannableString(fullTextH)
                                spannableStringH.setSpan(UnderlineSpan(), " הפטרה ".length, fullTextH.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                textViewClockH.text = spannableStringH

                                displayCache.put("haftarah", fullTextH)

                                textViewClockH.setOnClickListener {
                                    val browserIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        ("https://shahart.github.io/heb-bible/index.html?b=" + haftarah.ashkenaziReference).toUri()
                                    )
                                    startActivity(browserIntent)
                                }

                                haftarah.sephardic?.let { fullTextHS ->
                                    val spannableStringHS = SpannableString(fullTextHS)
                                    spannableStringHS.setSpan(UnderlineSpan(), " הפטרה ספרדים ".length, fullTextHS.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    textViewClockHS.text = spannableStringHS

                                    displayCache.put("haftarah_sephardic", fullTextHS)

                                    textViewClockHS.setOnClickListener {
                                        val browserIntent = Intent(
                                            Intent.ACTION_VIEW,
                                            ("https://shahart.github.io/heb-bible/index.html?b=" + haftarah.sephardicReference).toUri()
                                        )
                                        startActivity(browserIntent)
                                    }
                                }
                                if (haftarah.sephardic == null) {
                                    textViewClockHS.text = ""
                                    textViewClockHS.setOnClickListener(null)
                                }

                            }
                        }
                        majorHolidayOnNextSaturday?.let(::showMajorHolidayInsteadOfParasha)
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
            } catch (e: Exception) {
            Log.e("myquietwave", "MainActivity fetchParasha Exception $e", e)
            showParashaFallback()
            Firebase.crashlytics.log("MainActivity fetchParasha Exception")
            Firebase.crashlytics.recordException(e)
            }
        }
    }

    private fun showShabbatFallback(prefix: String) {
        textViewClock3.text = prefix + " " + displayCache.get("candles") + " " + displayCache.get("havdalah")
    }

    private fun showSunFallback(prefix: String) {
        textViewClock5suns.text = prefix + " " + displayCache.get("sunrise") + " " + displayCache.get("sunset")
    }

    private fun showParashaFallback() {
        textViewClock2.text = getParasha()
        textViewClockH.text = displayCache.get("haftarah")
        textViewClockHS.text = displayCache.get("haftarah_sephardic")
    }

    private fun showMajorHolidayInsteadOfParasha(holiday: com.shahartal.myquietchannel.parasha.Item) {
        val holidayDisplay = if (holiday.yomtov) "יום טוב ${holiday.hebrew}" else holiday.hebrew
        textViewClock2.text = holidayDisplay
        textViewClock2.setOnClickListener(null)
        textViewClock2_2.text = ""
        textViewClock2_2.setOnClickListener(null)
        textViewClockH.text = ""
        textViewClockH.setOnClickListener(null)
        textViewClockHS.text = ""
        textViewClockHS.setOnClickListener(null)
        haftarahConnectionSourceUrl = null
        haftarahConnectionButton.isEnabled = false
        displayCache.put("parashat", holidayDisplay)
        displayCache.put("haftarah", "")
        displayCache.put("haftarah_sephardic", "")
    }

    fun getParasha(): String {
        var res = displayCache.get("parashat")
        if (res.isBlank()) {
            res = " שבת פרשת " + Parshios.getParshaString(HebrewDate.today())
        }
        return res
    }

    private fun showHaftarahConnection() {
        val sourceUrl = haftarahConnectionSourceUrl ?: return
        haftarahConnectionButton.isEnabled = false

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.haftarah_connection)
            .setMessage(R.string.haftarah_connection_loading)
            .setNegativeButton(R.string.close_alert, null)
//            .setNeutralButton(R.string.haftarah_connection_source) { _, _ ->
//                startActivity(Intent(Intent.ACTION_VIEW, sourceUrl.toUri()))
//            }
            .create()
        dialog.show()

        lifecycleScope.launch {
            val content = try {
                withContext(Dispatchers.IO) { haftarahRepository.connection(sourceUrl) }
            } catch (error: Exception) {
                Log.w("myquietwave", "Unable to fetch the haftarah connection", error)
                "" // getString(R.string.haftarah_connection_error)
            }

            if (content.isNotEmpty()) {
                if (dialog.isShowing) dialog.setMessage(content)
                if (haftarahConnectionSourceUrl == sourceUrl) {
                    haftarahConnectionButton.isEnabled = true
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeViewReferences()
        settingsRepository = SettingsRepository(
            getSharedPreferences(SettingsRepository.PREFERENCES_NAME, MODE_PRIVATE)
        )
        displayCache = SharedPreferencesDisplayCache(
            getSharedPreferences(SettingsRepository.PREFERENCES_NAME, MODE_PRIVATE)
        )
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    mainViewModel.dailyLearning.collect { state ->
                    state.summary?.let { summary ->
                        summary.selichotText?.let { textViewClock7special.text = it }
                        summary.omer?.let { omer ->
                            textViewOmer.text = omer.text
                            textViewOmer.setOnClickListener { openUrl(omer.link) }
                        }
                        summary.dafYomi?.let { dafYomi ->
                            textViewClock4dafYomi.text = underlined(dafYomi.text)
                            displayCache.put("dafYomi", dafYomi.text)
                            val tooltip = buildString {
                                append("עוד לימודים יומיים:\n\n")
                                summary.additionalLearning.forEach { append(it).append('\n') }
                            }
                            textViewClock4dafYomiTitle.setOnClickListener { showMessageDialog(tooltip) }
                            textViewClock4dafYomi.setOnClickListener {
                                openUrl("https://daf-yomi.com/Dafyomi_Page.aspx")
                            }
                        }
                    }
                    state.error?.let { error ->
                        Log.e("myquietwave", "MainActivity fetchDafYomi failed", error)
                        textViewClock4dafYomi.text = displayCache.get("dafYomi")
                        Firebase.crashlytics.log("MainActivity fetchDafYomi Exception")
                        Firebase.crashlytics.recordException(error)
                    }
                    }
                }

                launch {
                    mainViewModel.shabbat.collect { state ->
                        state.summary?.let { summary ->
                            var res = ""
                            var resH = ""
                            if (summary.candleTimes.isNotEmpty()) {
                                res = "\n${getString(R.string.candleLighting)} ${summary.candleTimes.joinToString(" ")}"
                                if (summary.candleTimes.size > 1) res += "\n"
                                displayCache.put("candles", getString(R.string.candleLighting) + " " + summary.candleTimes.last())
                            }
                            if (summary.havdalahTimes.isNotEmpty()) {
                                resH = "\n${getString(R.string.havdalah)} ${summary.havdalahTimes.joinToString(" ")}"
                                if (summary.havdalahTimes.size > 1) resH += "\n"
                                displayCache.put("havdalah", getString(R.string.havdalah) + " " + summary.havdalahTimes.last())
                            }
                            summary.mevarchim?.let { mevarchim ->
                                res += "\n${mevarchim.title} \nהמולד: ${mevarchim.molad}\n"
                                textViewClock3.setOnClickListener { openUrl(mevarchim.wikiUrl) }
                                val spannable = SpannableString(res + resH)
                                val start = res.indexOf(mevarchim.title)
                                if (start != -1) spannable.setSpan(
                                    UnderlineSpan(), start, start + mevarchim.title.length,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                                )
                                textViewClock3.text = spannable
                            } ?: run { textViewClock3.text = res + resH }
                        }
                        state.error?.let { error ->
                            Log.w("myquietwave", "MainActivity fetchShabbat failed", error)
                            showShabbatFallback("")
                            Firebase.crashlytics.log("MainActivity fetchShabbat Exception")
                            Firebase.crashlytics.recordException(error)
                        }
                    }
                }

                launch {
                    mainViewModel.zmanim.collect { state ->
                        state.model?.let { model ->
                            val sunrise = HebcalPresentation.displayTime(model.times.sunrise)
                            val sunset = HebcalPresentation.displayTime(model.times.sunset)
                            val text = "\n${getString(R.string.sunrise)} $sunrise\n${getString(R.string.sunset)} $sunset "
                            displayCache.put("sunrise", getString(R.string.sunrise) + " " + sunrise)
                            displayCache.put("sunset", getString(R.string.sunset) + " " + sunset)
                            if (DateDisplay.hasTimePassed(model.times.sunset)) {
                                val hebrewCalendar = HebrewCalendar()
                                hebrewCalendar.add(Calendar.HOUR_OF_DAY, 12)
                                textViewHebDate.text = " הערב אור ל- ${HebrewDateDisplay.format(
                                    hebrewCalendar.get(HebrewCalendar.YEAR),
                                    hebrewCalendar.get(HebrewCalendar.MONTH),
                                    hebrewCalendar.get(HebrewCalendar.DAY_OF_MONTH),
                                )}"
                            }
                            textViewClock5locTitle.text = model.location.title
                            textViewClock5suns.text = text
                            textViewClock5suns.setOnClickListener {
                                AlertDialog.Builder(this@MainActivity)
                                    .setMessage(HebcalPresentation.zmanimDetails(model.times))
                                    .setNegativeButton(getString(R.string.close_alert)) { dialog, _ -> dialog.cancel() }
                                    .show()
                            }
                        }
                        state.error?.let { error ->
                            Log.w("myquietwave", "MainActivity fetchSunsZmanim failed", error)
                            showSunFallback(" ")
                            Firebase.crashlytics.log("MainActivity fetchSunsZmanim Exception")
                            Firebase.crashlytics.recordException(error)
                        }
                    }
                }
            }
        }

        haftarahConnectionButton.setOnClickListener { showHaftarahConnection() }

        firebaseAnalytics = Firebase.analytics

        Log.i("myquietwave", "MainActivity Version " + BuildConfig.VERSION_NAME)

        val settings = settingsRepository.load()
        val savedLocation = settings.location
        val savedStation = settings.station.displayName
        val justRadio = settings.radioOnly

        editTextLocation.text = savedLocation
        val locations = resources.getStringArray(R.array.locations)

        run {
            val stations = resources.getStringArray(R.array.stations)

            val adapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, locations)

            spinner.adapter = adapter

            val stationsAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, stations)

            stationsSpinner.adapter = stationsAdapter

            val savedLocationIndex = locations.indexOf(Utils.convertLocationIL(savedLocation))
                .takeIf { it >= 0 }
                ?: locations.indexOf("Geo/ GPS-Lat, Lon")
            spinner.setSelection(savedLocationIndex)
            stations.indexOf(savedStation).takeIf { it >= 0 }?.let(stationsSpinner::setSelection)

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

            var ignoreInitialLocationSelection = true
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    if (ignoreInitialLocationSelection) return
                    val selectedLocation = locations.getOrNull(position).orEmpty()
                    if (selectedLocation.isBlank() || selectedLocation == "Geo/ GPS-Lat, Lon") return
                    editTextLocation.text = Utils.convertFromLocationIL(selectedLocation)
                    fetchShabatZmanim()
                    fetchSunsZmanim()
                }

                override fun onNothingSelected(parent: AdapterView<*>) = Unit
            }
            spinner.post { ignoreInitialLocationSelection = false }
        }

        if (ZonedDateTime.now(ZoneId.systemDefault()).dayOfWeek == DayOfWeek.FRIDAY) {
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

        /* val infoIcon: ImageView = findViewById(R.id.info_icon)
        infoIcon.setOnClickListener {
            Toast.makeText(this, "Here you can place your city, with a comma, for Candle lighting and Havdalah times", Toast.LENGTH_LONG).show();
        }*/

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        shareButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.setType("text/plain")
            val shareLink = "https://play.google.com/store/apps/details?id=$packageName"
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + " " + shareLink)
            startActivity(Intent.createChooser(shareIntent, "Share this app"))
        }

        peekSongsButton.setOnClickListener {
            fetchGlglzSong(Station.GLGLZ)
        }

        textViewNewsLinks.setOnClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                "https://shahart.github.io/myquietwave/links.html".toUri()
            )
            startActivity(browserIntent)
        }

        textViewPosition.setOnClickListener {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE,
                )
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
                normalizedNewsDuration()
                if (textViewNextNews.text.toString() == "") {
                    textViewNextNews.text = NEXT_HOURS
                }
            }
        }
        textViewNextNews.setOnClickListener { normalizedNewsDuration() }

        // seconds

        textViewClock_2nd.text = TimeZone.currentSystemDefault().id
        // todo? ZoneId.short_ids code, like idt, pst
        textViewClock_3rd.text = "UTC" + TimeZone.currentSystemDefault().offsetAt(Clock.System.now()) // kotlinx.datetime.TimeZone.of(textViewClock_2nd.text.toString()).offsetAt(kotlinx.datetime.Instant.fromEpochMilliseconds(System.currentTimeMillis()))

        val hebrewCalendar = HebrewCalendar()
        val hebY = hebrewCalendar.get(HebrewCalendar.YEAR)
        val hebrewMonth = hebrewCalendar.get(HebrewCalendar.MONTH)
        val hebrewDay = hebrewCalendar.get(HebrewCalendar.DAY_OF_MONTH) // switches at midnight by-design
        textViewHebDate.text = HebrewDateDisplay.format(hebY, hebrewMonth, hebrewDay)

        startClockUpdates()

        // align UI if needed

        isServiceRunning = VolumeCycleService.isRunning
        Log.i("myquietwave", "MainActivity isRunning: $isServiceRunning")
        toggleButton.setAllCaps(false)
        shareButton.setAllCaps(false)
        updateServiceUi()

        requestNotificationPermissionIfNeeded()

        radioPlayer.setOnClickListener {
            updateServiceUi()
        }

        if (justRadio) {
            radioPlayer.isChecked = true
            updateServiceUi()
        }

        toggleButton.setOnClickListener {
            // Log.d("myquietwave", "MainActivity isServiceRunning: " + isServiceRunning)

            if (isServiceRunning) {

                val serviceIntent = Intent(this, VolumeCycleService::class.java)
                stopService(serviceIntent)

                isServiceRunning = false
                currentSong.text = ""
                nextSong.text = ""
                updateServiceUi()

                getSystemService(NotificationManager::class.java).cancel(VolumeCycleService.NOTIFICATION_ID)

            } else {

                val (serviceIntent, newsDuration) = buildServiceIntent()

                val selectedStation = Station.fromPersistedValue(stationsSpinner.selectedItem?.toString())
                selectedStation.takeIf { it.songFeedName != null }?.let(::fetchGlglzSong)

                    startForegroundService(serviceIntent)
                    isServiceRunning = true
                    updateServiceUi()

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

                        lifecycleScope.launch {
                            val audioManager = this@MainActivity.getSystemService(AUDIO_SERVICE) as AudioManager
                            for (i in 1..30) {
                                if (!alertDialog.isShowing) break
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
                                    if (!this@MainActivity.isFinishing) {
                                        alertDialog.cancel()
                                    }
                                }
                                delay(1_000)
                            }
                            if (!this@MainActivity.isFinishing) {
                                alertDialog.cancel()
                            }
                        }
                    }
            }
        }

        checkForAppUpdate()

        startPeriodicSongRefresh()
    }

    private val appUpdateManager: AppUpdateManager by lazy {
        AppUpdateManagerFactory.create(this)
    }
    private val appUpdateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode != RESULT_OK) {
            Log.e("myquietwave", "Update flow failed! Result code: ${result.resultCode}")
            Firebase.crashlytics.log("ERROR. Update flow failed. Result code: ${result.resultCode}")
        }
    }

    private fun checkForAppUpdate() {
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
            appUpdateLauncher,
            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
        )
    }

    private fun isGlglzStation(station: String?): Boolean =
        station != null && Station.fromPersistedValue(station).songFeedName != null

    private fun fetchGlglzSong(station: Station) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val songs = songRepository.currentAndNext(station)

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

}
