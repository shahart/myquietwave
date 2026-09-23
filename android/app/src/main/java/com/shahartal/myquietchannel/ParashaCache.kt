package com.shahartal.myquietchannel

import java.time.LocalDate

internal object ParashaCache {
    private const val PARASHA_KEY = "parashat"
    private const val SHABBAT_DATE_KEY = "parashat_shabbat_date"

    fun save(cache: DisplayCache, displayValue: String, shabbatDate: String) {
        cache.put(PARASHA_KEY, displayValue)
        cache.put(SHABBAT_DATE_KEY, shabbatDate)
    }

    /** A parasha remains useful until the Saturday for which it was fetched. */
    fun currentValue(cache: DisplayCache, today: LocalDate = LocalDate.now()): String? {
        val value = cache.get(PARASHA_KEY).takeIf { it.isNotBlank() } ?: return null
        val shabbatDate = runCatching { LocalDate.parse(cache.get(SHABBAT_DATE_KEY)) }.getOrNull()
            ?: return null
        return value.takeIf { today.isBefore(shabbatDate) }
    }
}
