package com.shahartal.myquietchannel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ParashaCacheTest {
    private val values = mutableMapOf<String, String>()
    private val cache = object : DisplayCache {
        override fun get(key: String): String = values[key].orEmpty()
        override fun put(key: String, value: String) {
            values[key] = value
        }
    }

    @Test
    fun returnsCachedParashaOnlyBeforeItsShabbat() {
        ParashaCache.save(cache, " שבת וילך", "2026-09-26")

        assertEquals(" שבת וילך", ParashaCache.currentValue(cache, LocalDate.parse("2026-09-25")))
        assertNull(ParashaCache.currentValue(cache, LocalDate.parse("2026-09-26")))
    }

    @Test
    fun rejectsLegacyCacheWithoutAShabbatDate() {
        cache.put("parashat", " שבת וילך")

        assertNull(ParashaCache.currentValue(cache, LocalDate.parse("2026-09-25")))
    }
}
