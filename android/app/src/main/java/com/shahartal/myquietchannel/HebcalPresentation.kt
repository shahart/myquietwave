package com.shahartal.myquietchannel

import com.shahartal.myquietchannel.parasha.HebCalZmanimTimesModel
import com.shahartal.myquietchannel.parasha.Item
import java.time.DayOfWeek
import java.time.LocalDate

internal data class LinkedText(val text: String, val link: String)

internal data class DailyLearningSummary(
    val dafYomi: LinkedText?,
    val additionalLearning: List<String>,
    val omer: LinkedText?,
    val selichotText: String?,
)

internal data class MevarchimSummary(
    val title: String,
    val molad: String,
    val wikiUrl: String,
)

internal data class ShabbatSummary(
    val candleTimes: List<String>,
    val havdalahTimes: List<String>,
    val mevarchim: MevarchimSummary?,
)

internal object HebcalPresentation {
    private val dailyLearningLabels = linkedMapOf(
        "mishnayomi" to "משנה יומית",
        "nachyomi" to "נ'ך יומי",
        "dailyPsalms" to "תהלים יומי",
        "tanakhYomi" to "תנ'ך יומי",
    )

    private val bookNames = linkedMapOf(
        "Joshua" to "יהושע",
        "Judges" to "שופטים",
        "I Samuel" to "שמואל א",
        "II Samuel" to "שמואל ב",
        "I Kings" to "מלכים א",
        "II Kings" to "מלכים ב",
        "Isaiah" to "ישעיהו",
        "Jeremiah" to "ירמיהו",
        "Ezekiel" to "יחזקאל",
        "Hosea" to "הושע",
        "Joel" to "יואל",
        "Amos" to "עמוס",
        "Obadiah" to "עובדיה",
        "Jonah" to "יונה",
        "Micah" to "מיכה",
        "Nachum" to "נחום",
        "Habakkuk" to "חבקוק",
        "Zephaniah" to "צפניה",
        "Haggai" to "חגי",
        "Zechariah" to "זכריה",
        "Malachi" to "מלאכי",
    )

    fun dailyLearning(items: List<Item>, isoDate: String): DailyLearningSummary {
        val daf = items.firstOrNull { it.category == "dafyomi" }
            ?.let { LinkedText(it.hebrew, it.link) }
        val additional = items.mapNotNull { item ->
            dailyLearningLabels[item.category]?.let { label -> "$label: ${item.hebrew}" }
        }
        val omer = items.firstOrNull { it.category == "omer" }?.let {
            LinkedText("ספירת העומר (בבוקר): \n${it.hebrew.replace("עומר", "")}", it.link)
        }
        val selichot = items.firstOrNull {
            it.category == "holiday" && it.subcat == "minor" && it.title == "Leil Selichot"
        }?.let { "ליל סליחות ${Utils.switchDate(isoDate)}\n" }
        return DailyLearningSummary(daf, additional, omer, selichot)
    }

    fun shabbat(items: List<Item>): ShabbatSummary {
        val mevarchim = items.firstOrNull { it.category == "mevarchim" }?.let { item ->
            MevarchimSummary(
                title = item.hebrew,
                molad = translateMolad(item.memo.substringAfter(": ", item.memo)),
                wikiUrl = "https://he.wikipedia.org/wiki/${monthWikiTitle(item.hebrew)}",
            )
        }
        return ShabbatSummary(
            candleTimes = items.filter { it.category == "candles" }.map { displayTime(it.date) },
            havdalahTimes = items.filter { it.category == "havdalah" }.map { displayTime(it.date) },
            mevarchim = mevarchim,
        )
    }

    /**
     * Hebcal's Shabbat feed contains a few days on either side of today.  Daily
     * calendar entries from an earlier date must not be presented as upcoming.
     */
    fun isPastDailyCalendarItem(item: Item, today: LocalDate = LocalDate.now()): Boolean {
        val isDailyCalendarItem = item.category == "holiday" ||
            item.category == "roshchodesh" ||
            item.title == "Fast begins" ||
            item.title == "Fast ends"
        if (!isDailyCalendarItem) return false

        val itemDate = runCatching { LocalDate.parse(item.date.substringBefore('T')) }.getOrNull()
        return itemDate == null || itemDate.isBefore(today)
    }

    fun majorHolidayOnNextSaturday(
        items: List<Item>,
        today: LocalDate = LocalDate.now(),
    ): Item? {
        val daysUntilSaturday = (DayOfWeek.SATURDAY.value - today.dayOfWeek.value + 7) % 7
        val nextSaturday = today.plusDays(daysUntilSaturday.toLong())
        return items.firstOrNull { item ->
            item.category == "holiday" &&
                item.subcat == "major" &&
                item.date.substringBefore('T') == nextSaturday.toString()
        }
    }

    fun displayTime(isoDateTime: String): String {
        val rawValue = isoDateTime.substringAfter('T', missingDelimiterValue = "").take(5)
        if (rawValue.length < 5) return ""
        val value = rawValue.removePrefix("0")
        return " $value "
    }

    fun zmanimDetails(times: HebCalZmanimTimesModel): String = listOf(
        "chatzot Night חצות הלילה: ${displayTime(times.chatzotNight)}",
        "alot HaShahar עלות השחר: ${displayTime(times.alotHaShachar)}",
        "dawn: ${displayTime(times.dawn)}",
        "sof Zman Shma מגן אברהם: ${displayTime(times.sofZmanShmaMGA)}",
        "sof Zman Shma: ${displayTime(times.sofZmanShma)}",
        "sof Zman Tfilla מגן אברהם: ${displayTime(times.sofZmanTfillaMGA)}",
        "sof Zman Tfilla: ${displayTime(times.sofZmanTfilla)}",
        "chatzot חצות היום: ${displayTime(times.chatzot)}",
        "",
        "mincha Gedola מנחה גדולה: ${displayTime(times.minchaGedola)}",
        "mincha Ketana מנחה קטנה: ${displayTime(times.minchaKetana)}",
        "plag HaMincha פלג המנחה: ${displayTime(times.plagHaMincha)}",
        "bein HaShmashos בין השמשות: ${displayTime(times.beinHaShmashos)}",
        "Dusk חשיכה: ${displayTime(times.dusk)}",
        "Tzeit צאת הכוכבים: ${displayTime(times.tzeit7083deg)}",
        "Tzeit 72' צאת הכוכבים רבינו תם: ${displayTime(times.tzeit72min)}",
    ).joinToString("\n")

    fun translateBookNames(value: String): String =
        bookNames.entries.fold(value) { result, (english, hebrew) -> result.replace(english, hebrew) }

    private fun translateMolad(value: String): String = value
        .replace("chalakim", "חלקים")
        .replace("and", "ו-")
        .replace("Sunday", "ראשון")
        .replace("Monday", "שני")
        .replace("Tuesday", "שלישי")
        .replace("Wednesday", "רביעי")
        .replace("Thursday", "חמישי")
        .replace("Friday", "שישי")
        .replace("Saturday", "שבת")

    private fun monthWikiTitle(title: String): String {
        val month = title.substringAfter("מברכים חודש", title).trim().replace("סיון", "סיוון")
        return if (month.contains("שבט")) "${month}_(חודש)" else month
    }
}
