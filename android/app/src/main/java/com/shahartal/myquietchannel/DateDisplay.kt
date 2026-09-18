package com.shahartal.myquietchannel

import java.time.LocalDate
import java.time.LocalTime

internal object DateDisplay {
    private val hebrewWeekdays = listOf("ראשון", "שני", "שלישי", "רביעי", "חמישי", "שישי", "שבת")

    fun hebrewWeekday(isoDate: String): String {
        val date = LocalDate.parse(isoDate.substringBefore('T'))
        return hebrewWeekdays[date.dayOfWeek.value % 7]
    }

    fun hasTimePassed(isoDateTime: String, now: LocalTime = LocalTime.now()): Boolean {
        val eventTime = LocalTime.parse(isoDateTime.substringAfter('T').take(5))
        return !now.isBefore(eventTime)
    }
}
