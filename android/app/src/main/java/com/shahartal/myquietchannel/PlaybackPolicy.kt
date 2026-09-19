package com.shahartal.myquietchannel

import java.time.DayOfWeek
import java.time.ZonedDateTime

internal data class ScheduleTime(val hour: Int, val minute: Int)

internal data class NewsSchedule(
    val times: Set<ScheduleTime>,
) {
    fun isDue(now: ZonedDateTime, serviceStartSecond: Int): Boolean {
        val boundary = serviceStartSecond.coerceIn(0, 59)
        return ScheduleTime(now.hour, now.minute) in times && now.second >= boundary - 1
    }

    companion object {
        const val DEFAULT_TEXT = "17, 21, 7, 12, 15, 18"

        fun parse(value: String?): NewsSchedule {
            val source = value?.takeIf { it.isNotBlank() } ?: DEFAULT_TEXT
            val times = source
                .split(',')
                .mapNotNull(::parseTime)
                .toSet()
            return NewsSchedule(times.ifEmpty { parse(DEFAULT_TEXT).times })
        }

        private fun parseTime(token: String): ScheduleTime? {
            val parts = token.trim().split(':')
            if (parts.size !in 1..2) return null
            val hour = parts[0].toIntOrNull()?.takeIf { it in 0..23 } ?: return null
            val minute = if (parts.size == 2) {
                parts[1].toIntOrNull()?.takeIf { it in 0..59 } ?: return null
            } else {
                0
            }
            return ScheduleTime(hour, minute)
        }
    }
}

internal object PlaybackPolicy {
    const val MAX_NEWS_DURATION_MINUTES = 59
    const val FRIDAY_MAX_NEWS_DURATION_MINUTES = 6

    fun isNearShabbat(now: ZonedDateTime): Boolean =
        now.dayOfWeek == DayOfWeek.FRIDAY && now.hour >= 12

    fun normalizeDuration(value: Int, isNearShabbat: Boolean): Int = value
        .coerceIn(1, MAX_NEWS_DURATION_MINUTES)
        .let { if (isNearShabbat) it.coerceAtMost(FRIDAY_MAX_NEWS_DURATION_MINUTES) else it }

    fun limitVolume(value: Int, maximumVolume: Int, isNearShabbat: Boolean): Int {
        val nonZero = value.coerceIn(1, maximumVolume.coerceAtLeast(1))
        if (!isNearShabbat) return nonZero
        return nonZero.coerceAtMost(shabbatVolumeLimit(maximumVolume))
    }

    fun shabbatVolumeLimit(maximumVolume: Int): Int =
        (maximumVolume * 50 / 100).coerceAtLeast(1)

    fun cycleStartVolume(configuredVolume: Int, maximumVolume: Int, isNearShabbat: Boolean): Int {
        var volume = configuredVolume
        val shabbatLimit = shabbatVolumeLimit(maximumVolume)
        if (isNearShabbat && volume > shabbatLimit + 1) {
            volume = limitVolume(volume, maximumVolume, isNearShabbat = true) + 1
        }
        return if (volume == 0) 4 else volume
    }
}

internal data class PlaybackConfig(
    val station: Station,
    val newsDurationMinutes: Int,
    val radioOnly: Boolean,
    val schedule: NewsSchedule,
) {
    companion object {
        fun fromRawValues(
            station: String?,
            newsDurationMinutes: Int,
            radioOnly: Boolean,
            scheduleText: String?,
            now: ZonedDateTime,
        ): PlaybackConfig = PlaybackConfig(
            station = Station.fromPersistedValue(station),
            newsDurationMinutes = PlaybackPolicy.normalizeDuration(
                newsDurationMinutes,
                PlaybackPolicy.isNearShabbat(now),
            ),
            radioOnly = radioOnly,
            schedule = NewsSchedule.parse(scheduleText ?: NewsSchedule.DEFAULT_TEXT),
        )
    }
}
