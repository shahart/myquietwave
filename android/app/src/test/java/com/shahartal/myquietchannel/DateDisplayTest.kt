package com.shahartal.myquietchannel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class DateDisplayTest {
    @Test
    fun formatsSundayAndFridayUsingHebrewNames() {
        assertEquals("ראשון", DateDisplay.hebrewWeekday("2026-09-20"))
        assertEquals("שישי", DateDisplay.hebrewWeekday("2026-09-18T18:00:00+03:00"))
    }

    @Test
    fun comparesCurrentTimeWithApiTimestamp() {
        assertFalse(DateDisplay.hasTimePassed("2026-09-18T18:00:00+03:00", LocalTime.of(17, 59)))
        assertTrue(DateDisplay.hasTimePassed("2026-09-18T18:00:00+03:00", LocalTime.of(18, 0)))
    }
}
