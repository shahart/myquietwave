package com.shahartal.myquietchannel

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import java.util.Locale.getDefault
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.pow
import kotlin.math.roundToInt

object Utils {
    
    val GLZ = Station.GLZ.streamUrl
    val GLGLZ = Station.GLGLZ.streamUrl

    val GIMMEL = Station.GIMMEL.streamUrl
    val BET = Station.BET.streamUrl

    val FM102 = Station.FM102.streamUrl
    val GALEY_ISRL = Station.GALEY_ISRAEL.streamUrl

    val KAN_88 = Station.KAN_88.streamUrl
    val KOL_BARAMA = Station.KOL_BARAMA.streamUrl
    val KOL_CHAI = Station.KOL_CHAI.streamUrl
    val KOL_CHAI_MUSIC = Station.KOL_CHAI_MUSIC.streamUrl

    val MORESHET = Station.MORESHET.streamUrl

    // val N12news = "http://ff_engine.streamgates.net/Ch10News.mp3"
    
    fun getStationUrl(url: String?): String =
        Station.fromStreamUrl(url)?.streamUrl ?: Station.fromPersistedValue(url).streamUrl

    fun switchDate(date: String): String {
        try {
            val splits = date.split("-").toTypedArray()
            var res = splits[2] + "-" + splits[1] + "-" + splits[0]
            res = res.replace("-0", "-")
            if (res.startsWith("0")) res = res.substring(1)
            return res
        }
        catch (e: Exception) {
            Firebase.crashlytics.log("WARN. switchDate. Date " + date + " is not in the correct format " + e.toString()) // saw length=1; index=2
            Firebase.crashlytics.recordException(e)
            return date
        }
    }

    fun isBefore(date: String, today: LocalDate = LocalDate.now()): Boolean {
        try {
            if (today <= LocalDate.parse(
                    date,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
                )
            ) {
                return false
            }
        }
        catch (e: Exception) {
            Firebase.crashlytics.log("WARN. isBefore. Date " + date + " is not in the correct format " + e.toString()) // saw length=1; index=2
            Firebase.crashlytics.recordException(e)
            return true
        }
        return true
    }

    fun roundToDecimalPlaces(number: Double): Double {
        val decimalPlaces = 2
        val factor = 10.0.pow(decimalPlaces)
        return (number * factor).roundToInt() / factor
    }

    fun getUe(loc: String): String {
        if (loc.lowercase(getDefault()).contains(", ue") || loc.lowercase(getDefault()).contains(",ue")) {
            return "off"
        }
        else {
            return "on"
        }
    }

    fun getCity(loc: String): String {
        if (loc.contains(",")) {
            return loc.trim().split(",")[0]
        }
        else {
            return loc
        }
    }

    fun getYY(no: Int): String {
        var input = no
        val letters = arrayOf("ה'","ד'","ג'","ב'","א'","ת","ש","ר","ק","צ","פ","ע","ס","נ","מ","ל","כ","י","ט","ח","ז","ו","ה","ד","ג","ב","א")
        val values = arrayOf(5000,4000,3000,2000,1000,400,300,200,100,90,80,70,60,50,40,30,20,10,9,8,7,6,5,4,3,2,1)
        var output = "";
        while (input > 0) {
            for (i in letters.indices) {
                if (input == 16) {
                    return output + "טז"
                }
                if (input == 15) {
                    return output + "טו"
                }
                if (input >= values[i]) {
                    input -= values[i]
                    output += letters[i]
                    break
                }
            }
        }
        return output
    }

    fun convertLocationIL(loc: String): String = IsraeliLocationNames.toHebrew(loc)

    fun convertFromLocationIL(loc: String): String = IsraeliLocationNames.toEnglish(loc)

}
