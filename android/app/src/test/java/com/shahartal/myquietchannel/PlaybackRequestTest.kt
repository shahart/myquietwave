package com.shahartal.myquietchannel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackRequestTest {
    @Test
    fun playbackIntentValuesUseDefaultsForNullIntent() {
        assertEquals(
            PlaybackIntentValues(
                station = Station.GLGLZ.displayName,
                newsDurationMinutes = AppSettings.DEFAULT_NEWS_DURATION_MINUTES,
                scheduleText = null,
                radioOnly = false,
            ),
            PlaybackIntentValues.fromIntent(null),
        )
    }

    @Test
    fun normalizesUiValuesAndAppliesDefaults() {
        val request = PlaybackStartRequest.fromUiValues(
            station = "כאן 88",
            newsDuration = "99",
            schedule = " ",
            todo = null,
            location = "IL-Jerusalem",
            radioOnly = false,
        )

        assertEquals(Station.KAN_88, request.station)
        assertEquals(PlaybackPolicy.MAX_NEWS_DURATION_MINUTES, request.newsDurationMinutes)
        assertEquals(NewsSchedule.DEFAULT_TEXT, request.scheduleText)
        assertEquals("", request.todo)
        assertFalse(request.radioOnly)
    }

    @Test
    fun preservesRadioModeAndExplicitSchedule() {
        val request = PlaybackStartRequest.fromUiValues(
            station = "גלגלצ",
            newsDuration = "2",
            schedule = "8, 12:30",
            todo = "מיחם",
            location = "31.7,35.2",
            radioOnly = true,
        )

        assertEquals(Station.GLGLZ, request.station)
        assertEquals("8, 12:30", request.scheduleText)
        assertEquals("מיחם", request.todo)
        assertEquals("31.7,35.2", request.location)
        assertTrue(request.radioOnly)
    }
}
