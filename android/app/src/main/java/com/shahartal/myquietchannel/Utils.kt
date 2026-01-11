package com.shahartal.myquietchannel

import java.util.Locale.getDefault
import kotlin.math.pow
import kotlin.math.roundToInt

object Utils {

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

        return loc;
    }

}