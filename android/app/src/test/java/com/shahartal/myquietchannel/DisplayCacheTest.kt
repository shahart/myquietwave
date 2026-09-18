package com.shahartal.myquietchannel

import org.junit.Assert.assertEquals
import org.junit.Test

class DisplayCacheTest {
    @Test
    fun cacheContractSupportsMissingAndUpdatedValues() {
        val values = mutableMapOf<String, String>()
        val cache = object : DisplayCache {
            override fun get(key: String): String = values[key].orEmpty()
            override fun put(key: String, value: String) {
                values[key] = value
            }
        }

        assertEquals("", cache.get("sunrise"))
        cache.put("sunrise", "זריחה 06:00")
        assertEquals("זריחה 06:00", cache.get("sunrise"))
    }
}
