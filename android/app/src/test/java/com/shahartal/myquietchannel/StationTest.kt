package com.shahartal.myquietchannel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StationTest {
    @Test
    fun everyStationRoundTripsThroughPersistedNameAndUrl() {
        Station.entries.forEach { station ->
            assertEquals(station, Station.fromPersistedValue(station.displayName))
            assertEquals(station, Station.fromStreamUrl(station.streamUrl))
            assertEquals(station.streamUrl, Utils.getStationUrl(station.displayName))
        }
    }

    @Test
    fun unknownStationFallsBackToGlglz() {
        assertEquals(Station.GLGLZ, Station.fromPersistedValue("missing"))
        assertEquals(Station.GLGLZ.streamUrl, Utils.getStationUrl(null))
        assertNull(Station.fromStreamUrl("https://example.com/radio"))
    }

    @Test
    fun persistedStationNamesIgnoreSurroundingWhitespace() {
        assertEquals(Station.KAN_88, Station.fromPersistedValue("  ${Station.KAN_88.displayName}  "))
        assertEquals(Station.GLGLZ, Station.fromPersistedValue("   "))
    }

    @Test
    fun onlyGlglzOffersSongMetadata() {
        assertEquals("glglz", Station.GLGLZ.songFeedName)
        assertEquals(listOf(Station.GLGLZ), Station.entries.filter { it.songFeedName != null })
    }
}
