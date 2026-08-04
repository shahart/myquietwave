package com.shahartal.myquietchannel

import com.shahartal.myquietchannel.luach.HebrewDate
import com.shahartal.myquietchannel.luach.Parshios
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Test
import org.junit.Assert.*

class ParshiosTest {

    @Test
    fun `today follows the civil date at midnight`() {
        val clock = Clock.fixed(Instant.parse("2026-08-04T12:00:00Z"), ZoneOffset.UTC)
        val today = HebrewDate.today(clock)
        assertEquals(HebrewDate(5786, 5, 21), today)
        assertEquals("ראה", Parshios.getParshaString(today))
    }
}
