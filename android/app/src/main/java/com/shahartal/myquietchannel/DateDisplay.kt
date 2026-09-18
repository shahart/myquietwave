package com.shahartal.myquietchannel

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

internal object DateDisplay {
    private val hebrewWeekdays = listOf("ראשון", "שני", "שלישי", "רביעי", "חמישי", "שישי", "שבת")
    private val clockFormatter = DateTimeFormatter.ofPattern("H:mm:ss")

    fun hebrewWeekday(isoDate: String): String {
        val date = LocalDate.parse(isoDate.substringBefore('T'))
        return hebrewWeekday(date)
    }

    fun clockTime(now: LocalDateTime): String = now.format(clockFormatter)

    fun calendarLabel(date: LocalDate): String =
        "${hebrewWeekday(date)} ${date.dayOfMonth}/${date.monthValue}/${date.year}"

    fun hasTimePassed(isoDateTime: String, now: LocalTime = LocalTime.now()): Boolean {
        val eventTime = LocalTime.parse(isoDateTime.substringAfter('T').take(5))
        return !now.isBefore(eventTime)
    }

    private fun hebrewWeekday(date: LocalDate): String = hebrewWeekdays[date.dayOfWeek.value % 7]
}
