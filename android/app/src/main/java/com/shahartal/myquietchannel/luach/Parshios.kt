package com.shahartal.myquietchannel.luach

import java.util.ArrayDeque

/** The weekly Torah-reading calculation from pyluach.parshios. */
object Parshios {
    private val hebrewNames = listOf(
        "בראשית", "נח", "לך לך", "וירא", "חיי שרה", "תולדות", "ויצא", "וישלח",
        "וישב", "מקץ", "ויגש", "ויחי", "שמות", "וארא", "בא", "בשלח", "יתרו",
        "משפטים", "תרומה", "תצוה", "כי תשא", "ויקהל", "פקודי", "ויקרא", "צו",
        "שמיני", "תזריע", "מצורע", "אחרי מות", "קדושים", "אמור", "בהר", "בחוקותי",
        "במדבר", "נשא", "בהעלותך", "שלח", "קרח", "חקת", "בלק", "פינחס", "מטות",
        "מסעי", "דברים", "ואתחנן", "עקב", "ראה", "שופטים", "כי תצא", "כי תבא",
        "נצבים", "וילך", "האזינו", "וזאת הברכה"
    )

    /**
     * Returns the reading for the Shabbos on or following [date], or `null`
     * when that Shabbos has no weekly reading because it is Yom Tov.
     */
    @JvmStatic
    fun getParshaString(date: HebrewDate): String {
        val parsha = getParsha(date) ?: return ""
        return parsha.joinToString(", ") { hebrewNames[it] }
    }

    private fun getParsha(date: HebrewDate): List<Int>? {
        val target = date.followingShabbos()
        return table(target.year)[target.julianDayNumber]
    }

    private fun table(year: Int): Map<Long, List<Int>?> {
        val readings = ArrayDeque<Int>().apply {
            add(51)
            add(52)
            for (parsha in 0..51) add(parsha)
        }
        val result = linkedMapOf<Long, List<Int>?>()
        val leap = CalendarMath.isLeap(year)
        val pesachDay = HebrewDate(year, 1, 15).weekday()
        val roshHashana = HebrewDate(year, 7, 1)
        var shabbos = roshHashana.followingShabbos()
        if (roshHashana.weekday() > 4) readings.removeFirst()

        while (shabbos.year == year) {
            if (isParshaless(shabbos)) {
                result[shabbos.julianDayNumber] = null
            } else {
                val parsha = readings.removeFirst()
                val reading = mutableListOf(parsha)
                val doubleParsha =
                    (parsha == 21 && weeksUntil(HebrewDate(year, 1, 14), shabbos) < 3) ||
                            (parsha in setOf(26, 28) && !leap) ||
                            (parsha == 31 && !leap) ||
                            (parsha == 38 && pesachDay == 5) ||
                            (parsha == 41 && weeksUntil(HebrewDate(year, 5, 9), shabbos) < 2) ||
                            (parsha == 50 && HebrewDate(year + 1, 7, 1).weekday() > 4)
                if (doubleParsha) reading += readings.removeFirst()
                result[shabbos.julianDayNumber] = reading
            }
            shabbos = shabbos.plusDays(7)
        }
        return result
    }

    private fun weeksUntil(later: HebrewDate, earlier: HebrewDate): Long =
        (later.julianDayNumber - earlier.julianDayNumber) / 7L

    private fun isParshaless(date: HebrewDate): Boolean {
        return (date.month == 7 && (date.day in listOf(1, 2, 10) || date.day in 15..23)) ||
                (date.month == 1 && date.day in 15..22) ||
                (date.month == 3 && date.day in listOf(6, 7))
    }
}
