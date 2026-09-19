package com.shahartal.myquietchannel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.LocalDate

class UtilsTest {
    @Test
    fun switchesIsoDateToDisplayOrder() {
        assertEquals("18-9-2026", Utils.switchDate("2026-09-18"))
    }

    @Test
    fun comparesDatesAgainstInjectedToday() {
        val today = LocalDate.of(2026, 9, 18)
        assertTrue(Utils.isBefore("2026-09-17", today))
        assertFalse(Utils.isBefore("2026-09-18", today))
        assertFalse(Utils.isBefore("2026-09-19", today))
    }

    @Test
    fun formatsHebrewNumbersIncludingSacredNameExceptions() {
        assertEquals("טו", Utils.getYY(15))
        assertEquals("טז", Utils.getYY(16))
        assertEquals("ה'תשפו", Utils.getYY(5786))
    }

    @Test
    fun rejectsNonPositiveHebrewYears() {
        assertThrows(IllegalArgumentException::class.java) { Utils.getYY(0) }
        assertThrows(IllegalArgumentException::class.java) { Utils.getYY(-1) }
    }
}
