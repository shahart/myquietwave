package com.shahartal.myquietchannel

internal object HebrewDateDisplay {
    private val dayNames = arrayOf(
        "א", "ב", "ג", "ד", "ה", "ו", "ז", "ח", "ט", "י",
        "יא", "יב", "יג", "יד", "טו", "טז", "יז", "יח", "יט", "כ",
        "כא", "כב", "כג", "כד", "כה", "כו", "כז", "כח", "כט", "ל",
    )

    private val monthNames = arrayOf(
        "תשרי", "חשון", "כסלו", "טבת", "שבט", "אדר", "אדר",
        "ניסן", "אייר", "סיוון", "תמוז", "אב", "אלול",
    )

    fun format(year: Int, month: Int, day: Int): String {
        val monthName = when {
            isLeapYear(year) && month == 5 -> "אדר א"
            isLeapYear(year) && month == 6 -> "אדר ב"
            else -> monthNames[month]
        }
        return "${dayNames[day - 1]} $monthName ${Utils.getYY(year)}"
    }

    private fun isLeapYear(year: Int): Boolean {
        val cyclePosition = (year * 12 + 17) % 19
        return cyclePosition >= (if (cyclePosition < 0) -7 else 12)
    }
}
