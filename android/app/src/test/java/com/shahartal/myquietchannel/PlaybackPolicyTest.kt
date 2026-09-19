package com.shahartal.myquietchannel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class PlaybackPolicyTest {
    private val zone = ZoneId.of("Asia/Jerusalem")

    @Test
    fun scheduleParsesHoursAndExplicitMinutes() {
        assertEquals(
            setOf(ScheduleTime(7, 0), ScheduleTime(12, 30), ScheduleTime(21, 5)),
            NewsSchedule.parse("7, 12:30,21:05").times,
        )
    }

    @Test
    fun scheduleIgnoresMalformedAndOutOfRangeEntries() {
        assertEquals(
            setOf(ScheduleTime(18, 0)),
            NewsSchedule.parse("nope, 24, 12:60, 18, 1:2:3").times,
        )
    }

    @Test
    fun blankScheduleUsesDefaultTimes() {
        val expected = NewsSchedule.parse(NewsSchedule.DEFAULT_TEXT).times

        assertEquals(expected, NewsSchedule.parse(null).times)
        assertEquals(expected, NewsSchedule.parse("").times)
        assertEquals(expected, NewsSchedule.parse("   ").times)
    }

    @Test
    fun scheduleWithOnlyInvalidEntriesUsesDefaultTimes() {
        assertEquals(
            NewsSchedule.parse(NewsSchedule.DEFAULT_TEXT).times,
            NewsSchedule.parse("invalid, 24:99, 1:2:3").times,
        )
    }

    @Test
    fun scheduleWaitsUntilServiceStartSecondBoundary() {
        val schedule = NewsSchedule.parse("12:30")
        assertFalse(schedule.isDue(at(12, 30, 8), serviceStartSecond = 10))
        assertTrue(schedule.isDue(at(12, 30, 9), serviceStartSecond = 10))
        assertFalse(schedule.isDue(at(12, 31, 10), serviceStartSecond = 10))
    }

    @Test
    fun scheduleClampsInvalidServiceStartSeconds() {
        val schedule = NewsSchedule.parse("12:30")

        assertTrue(schedule.isDue(at(12, 30, 0), serviceStartSecond = -10))
        assertFalse(schedule.isDue(at(12, 30, 57), serviceStartSecond = 70))
        assertTrue(schedule.isDue(at(12, 30, 59), serviceStartSecond = 70))
    }

    @Test
    fun durationIsClampedAndFridayAfternoonIsLimitedToSixMinutes() {
        assertEquals(1, PlaybackPolicy.normalizeDuration(0, isNearShabbat = false))
        assertEquals(59, PlaybackPolicy.normalizeDuration(99, isNearShabbat = false))
        assertEquals(6, PlaybackPolicy.normalizeDuration(20, isNearShabbat = true))
    }

    @Test
    fun nearShabbatBeginsFridayAtNoon() {
        assertFalse(PlaybackPolicy.isNearShabbat(at(11, 59, 59)))
        assertTrue(PlaybackPolicy.isNearShabbat(at(12, 0, 0)))
    }

    @Test
    fun shabbatVolumeLimitHandlesSmallAndTypicalStreams() {
        assertEquals(1, PlaybackPolicy.shabbatVolumeLimit(1))
        assertEquals(7, PlaybackPolicy.shabbatVolumeLimit(15))
    }

    @Test
    fun volumeIsKeptWithinDeviceBoundsAndNeverMuted() {
        assertEquals(1, PlaybackPolicy.limitVolume(0, maximumVolume = 15, isNearShabbat = false))
        assertEquals(15, PlaybackPolicy.limitVolume(99, maximumVolume = 15, isNearShabbat = false))
        assertEquals(1, PlaybackPolicy.limitVolume(99, maximumVolume = 0, isNearShabbat = false))
        assertEquals(7, PlaybackPolicy.limitVolume(99, maximumVolume = 15, isNearShabbat = true))
    }

    @Test
    fun cycleStartVolumeAppliesShabbatAndInitialDefaults() {
        assertEquals(4, PlaybackPolicy.cycleStartVolume(0, maximumVolume = 15, isNearShabbat = false))
        assertEquals(8, PlaybackPolicy.cycleStartVolume(15, maximumVolume = 15, isNearShabbat = true))
        assertEquals(15, PlaybackPolicy.cycleStartVolume(15, maximumVolume = 15, isNearShabbat = false))
    }

    @Test
    fun configuredVolumeUsesSafeStartupMinimum() {
        assertEquals(3, PlaybackPolicy.configuredVolumeAfterStartup(0))
        assertEquals(3, PlaybackPolicy.configuredVolumeAfterStartup(2))
        assertEquals(8, PlaybackPolicy.configuredVolumeAfterStartup(8))
    }

    @Test
    fun restoredVolumeStaysWithinDeviceBounds() {
        assertEquals(0, PlaybackPolicy.restoredVolume(-1, 15))
        assertEquals(15, PlaybackPolicy.restoredVolume(99, 15))
        assertEquals(0, PlaybackPolicy.restoredVolume(4, 0))
    }

    @Test
    fun playbackConfigNormalizesRawServiceInput() {
        val config = PlaybackConfig.fromRawValues(
            station = "כאן 88",
            newsDurationMinutes = 20,
            radioOnly = true,
            scheduleText = "8, 12:30",
            now = at(12, 0, 0),
        )

        assertEquals(Station.KAN_88, config.station)
        assertEquals(6, config.newsDurationMinutes)
        assertTrue(config.radioOnly)
        assertEquals(setOf(ScheduleTime(8, 0), ScheduleTime(12, 30)), config.schedule.times)
    }

    private fun at(hour: Int, minute: Int, second: Int): ZonedDateTime =
        ZonedDateTime.of(2026, 9, 18, hour, minute, second, 0, zone)
}
