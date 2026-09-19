package com.shahartal.myquietchannel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class HebrewDateDisplayTest {
    @Test
    fun formatsRegularMonthAndDay() {
        assertEquals("טו תשרי ${Utils.getYY(5785)}", HebrewDateDisplay.format(5785, 0, 15))
    }

    @Test
    fun namesAdarMonthsInLeapYears() {
        assertEquals("א אדר א ${Utils.getYY(5784)}", HebrewDateDisplay.format(5784, 5, 1))
        assertEquals("ב אדר ב ${Utils.getYY(5784)}", HebrewDateDisplay.format(5784, 6, 2))
    }

    @Test
    fun rejectsInvalidCalendarIndexes() {
        assertThrows(IllegalArgumentException::class.java) { HebrewDateDisplay.format(0, 0, 1) }
        assertThrows(IllegalArgumentException::class.java) { HebrewDateDisplay.format(5785, -1, 1) }
        assertThrows(IllegalArgumentException::class.java) { HebrewDateDisplay.format(5785, 13, 1) }
        assertThrows(IllegalArgumentException::class.java) { HebrewDateDisplay.format(5785, 0, 0) }
    }
}
