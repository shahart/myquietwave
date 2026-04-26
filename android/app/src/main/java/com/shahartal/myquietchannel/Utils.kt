package com.shahartal.myquietchannel

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import java.util.Locale.getDefault
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.pow
import kotlin.math.roundToInt

object Utils {
    
    val GLZ = "https://glzwizzlv.bynetcdn.com/glz_mp3"
    val GLGLZ = "https://glzwizzlv.bynetcdn.com/glglz_mp3"

    val GIMMEL = "https://playerservices.streamtheworld.com/api/livestream-redirect/KAN_GIMMEL.mp3"
    val BET = "https://playerservices.streamtheworld.com/api/livestream-redirect/KAN_BET.mp3"

    val FM102 = "https://cdn88.mediacast.co.il/102fm-tlv/102fm_mp3/icecast.audio"
    val GALEY_ISRL = "https://cdn.cybercdn.live/Galei_Israel/Live/icecast.audio"

    val KAN_88 = "https://27863.live.streamtheworld.com/KAN_88.mp3"
    val KOL_BARAMA = "https://cdn.cybercdn.live/Kol_Barama/Live_Audio/icecast.audio"
    val KOL_CHAI = "https://live.kcm.fm/live-new"
    val KOL_CHAI_MUSIC = "https://live.kcm.fm/livemusic"
    
    fun getStationUrl(url: String?): String {
        if (url == null) return GLZ
        
        if (url == "גלגלצ") return GLGLZ
        if (url == "גלי צהל") return GLZ

        if (url == "רשת ב") return BET
        if (url == "רשת ג") return GIMMEL

        if (url == "FM102") return FM102
        if (url == "גלי ישראל") return GALEY_ISRL
        if (url == "כאן 88") return KAN_88
        if (url == "קול חי") return KOL_CHAI
        if (url == "קול חי מיוזיק") return KOL_CHAI_MUSIC
        if (url == "קול ברמה") return KOL_BARAMA

        return GLZ
    }

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

    fun isBefore(date: String): Boolean {
        val today = LocalDate.now()
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
            for (i in 0..letters.size) {
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

    fun convertLocationIL(loc: String): String {
        if (loc.startsWith("IL-Jerusalem")) return "IL-ירושלים";
        if (loc.startsWith("IL-Tel Aviv")) return "IL-תל אביב";
        if (loc.startsWith("IL-Haifa")) return "IL-חיפה";
        if (loc.startsWith("IL-Eilat")) return "IL-אילת";
        if (loc.startsWith("IL-Be'er Sheva")) return "IL-באר שבע";
		
        if (loc.startsWith("IL-Ashdod")) return "IL-אשדוד";
        if (loc.startsWith("IL-Ashkelon")) return "IL-אשקלון";
        if (loc.startsWith("IL-Bat Yam")) return "IL-בת ים";
        if (loc.startsWith("IL-Beit Shemesh")) return "IL-בית שמש";
        if (loc.startsWith("IL-Bnei Brak")) return "IL-בני ברק";
		
        if (loc.startsWith("IL-Hadera")) return "IL-חדרה";
        if (loc.startsWith("IL-Herzliya")) return "IL-הרצליה";
        if (loc.startsWith("IL-Holon")) return "IL-חולון";
        if (loc.startsWith("IL-Kfar Saba")) return "IL-כפר סבא";
        if (loc.startsWith("IL-Lod")) return "IL-לוד";
		
        if (loc.startsWith("IL-Modiin")) return "IL-מודיעין";
        if (loc.startsWith("IL-Nazareth")) return "IL-נצרת";
        if (loc.startsWith("IL-Netanya")) return "IL-נתניה";
        if (loc.startsWith("IL-Petach Tikvah")) return "IL-פתח תקוה";
        if (loc.startsWith("IL-Ra'anana")) return "IL-רעננה";
		
        if (loc.startsWith("IL-Ramat Gan")) return "IL-רמת גן";
        if (loc.startsWith("IL-Ramla")) return "IL-רמלה";
        if (loc.startsWith("IL-Rishon LeZion")) return "IL-ראשון לציון";
        if (loc.startsWith("IL-Tiberias")) return "IL-טבריה";
        
       	// special treatment
        if (loc.startsWith("IL-Yavne")) return "IL-יבנה";

        if (loc.startsWith("IL-Modiin Ilit")) return "IL-מודיעין עילית";
        if (loc.startsWith("IL-Betar Ilit")) return "IL-ביתר עילית";
        if (loc.startsWith("IL-Zefat")) return "IL-צפת";

        return loc;
    }

    fun convertFromLocationIL(loc: String): String {
        if (loc == "IL-ירושלים") return "IL-Jerusalem";
        if (loc == "IL-תל אביב") return "IL-Tel Aviv";
        if (loc == "IL-חיפה") return "IL-Haifa";
        if (loc == "IL-אילת") return "IL-Eilat";
        if (loc == "IL-באר שבע") return "IL-Be'er Sheva";
		
        if (loc == "IL-אשדוד") return "IL-Ashdod";
        if (loc == "IL-אשקלון") return "IL-Ashkelon";
        if (loc == "IL-בת ים") return "IL-Bat Yam";
        if (loc == "IL-בית שמש") return "IL-Beit Shemesh";
        if (loc == "IL-בני ברק") return "IL-Bnei Brak";

        if (loc == "IL-חדרה") return "IL-Hadera";
        if (loc == "IL-הרצליה") return "IL-Herzliya";
        if (loc == "IL-חולון") return "IL-Holon";
        if (loc == "IL-כפר סבא") return "IL-Kfar Saba";
        if (loc == "IL-לוד") return "IL-Lod";

        if (loc == "IL-מודיעין") return "IL-Modiin";
        if (loc == "IL-נצרת") return "IL-Nazareth";
        if (loc == "IL-נתניה") return "IL-Netanya";
        if (loc == "IL-פתח תקוה") return "IL-Petach Tikvah";
        if (loc == "IL-רעננה") return "IL-Ra'anana";

        if (loc == "IL-רמת גן") return "IL-Ramat Gan";
        if (loc == "IL-רמלה") return "IL-Ramla";
        if (loc == "IL-ראשון לציון") return "IL-Rishon LeZion";
        if (loc == "IL-טבריה") return "IL-Tiberias";

	// special treatment
        if (loc == "IL-יבנה") return "IL-Yavne";
        if (loc == "IL-ביתר עילית") return "IL-Betar Ilit";
        if (loc == "IL-מודיעין עילית") return "IL-Modiin Ilit";
        if (loc == "IL-צפת") return "IL-Zefat";

        return loc;
    }

}