package com.shahartal.myquietchannel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LocationQueryTest {
    @Test
    fun parsesCoordinatesIncludingNegativeValues() {
        assertEquals(
            LocationQuery.Coordinates("-33.9", "151.2", useElevation = true),
            LocationQueryParser.parse(" -33.9, 151.2 "),
        )
    }

    @Test
    fun parsesGeoNameAndElevationSuffix() {
        assertEquals(
            LocationQuery.GeoName("293222", useElevation = false),
            LocationQueryParser.parse("293222, UE"),
        )
    }

    @Test
    fun mapsSpecialCitiesToGeoNameIds() {
        assertEquals(
            LocationQuery.GeoName("8199378", useElevation = true),
            LocationQueryParser.parse("IL-Modiin Ilit"),
        )
    }

    @Test
    fun parsesRegularCityAndRejectsBlankInput() {
        assertEquals(
            LocationQuery.City("IL-Jerusalem", useElevation = true),
            LocationQueryParser.parse("IL-Jerusalem"),
        )
        assertNull(LocationQueryParser.parse("  "))
    }

    @Test
    fun locationTranslationsRoundTrip() {
        Station.entries // Ensure the test catches accidental initialization cycles.
        val english = "IL-Mitzpe Ramon"
        assertEquals(english, IsraeliLocationNames.toEnglish(IsraeliLocationNames.toHebrew(english)))
    }
}
