package com.shahartal.myquietchannel

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

internal object DateDisplay {
    private val hebrewWeekdays = listOf("ראשון", "שני", "שלישי", "רביעי", "חמישי", "שישי", "שבת")
    private val clockFormatter = DateTimeFormatter.ofPattern("H:mm:ss")

    fun hebrewWeekday(isoDate: String): String = runCatching {
        hebrewWeekday(LocalDate.parse(isoDate.substringBefore('T')))
    }.getOrDefault("")

    fun clockTime(now: LocalDateTime): String = now.format(clockFormatter)

    fun calendarLabel(date: LocalDate): String =
        "${hebrewWeekday(date)} ${date.dayOfMonth}/${date.monthValue}/${date.year}"

    fun hasTimePassed(isoDateTime: String, now: LocalTime = LocalTime.now()): Boolean {
        val rawTime = isoDateTime.substringAfter('T', missingDelimiterValue = "").take(5)
        val eventTime = runCatching { LocalTime.parse(rawTime) }.getOrNull() ?: return false
        return !now.isBefore(eventTime)
    }

    private fun hebrewWeekday(date: LocalDate): String = hebrewWeekdays[date.dayOfWeek.value % 7]
}
