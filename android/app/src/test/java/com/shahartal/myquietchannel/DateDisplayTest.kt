package com.shahartal.myquietchannel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class DateDisplayTest {
    @Test
    fun formatsSundayAndFridayUsingHebrewNames() {
        assertEquals("ראשון", DateDisplay.hebrewWeekday("2026-09-20"))
        assertEquals("שישי", DateDisplay.hebrewWeekday("2026-09-18T18:00:00+03:00"))
    }

    @Test
    fun malformedDateProducesEmptyWeekday() {
        assertEquals("", DateDisplay.hebrewWeekday("not-a-date"))
        assertEquals("", DateDisplay.hebrewWeekday("2026-13-40"))
    }

    @Test
    fun comparesCurrentTimeWithApiTimestamp() {
        assertFalse(DateDisplay.hasTimePassed("2026-09-18T18:00:00+03:00", LocalTime.of(17, 59)))
        assertTrue(DateDisplay.hasTimePassed("2026-09-18T18:00:00+03:00", LocalTime.of(18, 0)))
    }

    @Test
    fun malformedApiTimestampIsNotConsideredPassed() {
        assertFalse(DateDisplay.hasTimePassed("2026-09-18", LocalTime.NOON))
        assertFalse(DateDisplay.hasTimePassed("2026-09-18Tinvalid", LocalTime.NOON))
    }

    @Test
    fun formatsClockAndCalendarLabels() {
        assertEquals("7:05:09", DateDisplay.clockTime(LocalDateTime.of(2026, 9, 20, 7, 5, 9)))
        assertEquals("ראשון 20/9/2026", DateDisplay.calendarLabel(LocalDate.of(2026, 9, 20)))
    }
}
