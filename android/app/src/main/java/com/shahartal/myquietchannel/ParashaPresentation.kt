package com.shahartal.myquietchannel

internal data class ParashaNames(
    val primary: String,
    val secondary: String?,
)

internal data class HaftarahTexts(
    val ashkenazi: String,
    val ashkenaziReference: String,
    val sephardic: String?,
    val sephardicReference: String?,
)

internal object ParashaPresentation {
    private val maqafOnlyNames = setOf("כי־תצא", "כי־תבוא", "שלח־לך", "לך־לך")

    fun names(hebrew: String): ParashaNames {
        var primary = hebrew
        var secondary: String? = null

        if (primary.contains("-")) {
            val parts = primary.split("-", limit = 2)
            primary = parts[0]
            secondary = "פרשת ${parts[1]}"
        }

        if (primary in maqafOnlyNames) {
            primary = primary.replace("־", "_")
            secondary = null
        } else if (primary.contains("־")) {
            val parts = primary.split("־", limit = 2)
            primary = parts[0]
            secondary = "פרשת ${parts[1]}"
        }

        return ParashaNames(primary = primary, secondary = secondary)
    }

    fun haftarah(ashkenazi: String, sephardic: String?): HaftarahTexts {
        fun format(value: String, prefix: String): Pair<String, String> {
            val translated = HebcalPresentation.translateBookNames(value.replace("|", "\n"))
            return " $prefix $translated" to value.substringBefore(':')
        }

        val (text, reference) = format(ashkenazi, "הפטרה")
        val sephardicText = sephardic?.let { format(it, "הפטרה ספרדים") }
        return HaftarahTexts(
            ashkenazi = text,
            ashkenaziReference = reference,
            sephardic = sephardicText?.first,
            sephardicReference = sephardicText?.second,
        )
    }

    fun calendarEvent(hebrew: String, isoDate: String): String =
        "$hebrew - ${DateDisplay.hebrewWeekday(isoDate)} ${Utils.switchDate(isoDate)}"

    fun fastTime(isoDateTime: String): String =
        isoDateTime.substringAfter('T', missingDelimiterValue = "").take(5)

    fun fastBeginning(isoDateTime: String): String =
        " זמני התענית: עלות השחר ${fastTime(isoDateTime)}"

    fun fastEnd(isoDateTime: String): String =
        " צאת הכוכבים ${fastTime(isoDateTime)}"

    fun appendMemo(existing: String, hebrew: String, memo: String?): String {
        val text = memo?.trim().orEmpty()
        if (text.isEmpty() || existing.contains(text)) return existing
        return "$existing\n\n$hebrew: $text"
    }
}
