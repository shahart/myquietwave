package com.shahartal.myquietchannel

import java.util.Locale

internal sealed interface LocationQuery {
    val useElevation: Boolean

    data class City(val name: String, override val useElevation: Boolean) : LocationQuery
    data class GeoName(val id: String, override val useElevation: Boolean) : LocationQuery
    data class Coordinates(
        val latitude: String,
        val longitude: String,
        override val useElevation: Boolean,
    ) : LocationQuery
}

internal object LocationQueryParser {
    private val specialGeoNames = mapOf(
        "il-yavne" to "293222",
        "il-zefat" to "293100",
        "il-mitzpe ramon" to "294166",
        "il-modiin ilit" to "8199378",
        "il-betar ilit" to "284375",
    )

    fun parse(rawValue: String): LocationQuery? {
        val useElevation = !Regex(",\\s*ue\\s*$", RegexOption.IGNORE_CASE).containsMatchIn(rawValue)
        val value = rawValue.replace(Regex(",\\s*ue\\s*$", RegexOption.IGNORE_CASE), "").trim()
        if (value.isEmpty()) return null

        val coordinateParts = value.split(',').map(String::trim)
        if (coordinateParts.size == 2 &&
            coordinateParts[0].toDoubleOrNull() != null &&
            coordinateParts[1].toDoubleOrNull() != null
        ) {
            return LocationQuery.Coordinates(coordinateParts[0], coordinateParts[1], useElevation)
        }

        if (value.all(Char::isDigit)) return LocationQuery.GeoName(value, useElevation)

        specialGeoNames.entries.firstOrNull {
            value.lowercase(Locale.ROOT).contains(it.key)
        }?.let { return LocationQuery.GeoName(it.value, useElevation) }

        return LocationQuery.City(value, useElevation)
    }

    fun ue(query: LocationQuery): String = if (query.useElevation) "on" else "off"
}

internal object IsraeliLocationNames {
    private val englishToHebrew = linkedMapOf(
        "IL-Jerusalem" to "IL-ירושלים",
        "IL-Tel Aviv" to "IL-תל אביב",
        "IL-Haifa" to "IL-חיפה",
        "IL-Eilat" to "IL-אילת",
        "IL-Be'er Sheva" to "IL-באר שבע",
        "IL-Ashdod" to "IL-אשדוד",
        "IL-Ashkelon" to "IL-אשקלון",
        "IL-Bat Yam" to "IL-בת ים",
        "IL-Beit Shemesh" to "IL-בית שמש",
        "IL-Bnei Brak" to "IL-בני ברק",
        "IL-Hadera" to "IL-חדרה",
        "IL-Herzliya" to "IL-הרצליה",
        "IL-Holon" to "IL-חולון",
        "IL-Kfar Saba" to "IL-כפר סבא",
        "IL-Lod" to "IL-לוד",
        "IL-Modiin Ilit" to "IL-מודיעין עילית",
        "IL-Modiin" to "IL-מודיעין",
        "IL-Nazareth" to "IL-נצרת",
        "IL-Netanya" to "IL-נתניה",
        "IL-Petach Tikvah" to "IL-פתח תקוה",
        "IL-Ra'anana" to "IL-רעננה",
        "IL-Ramat Gan" to "IL-רמת גן",
        "IL-Ramla" to "IL-רמלה",
        "IL-Rishon LeZion" to "IL-ראשון לציון",
        "IL-Tiberias" to "IL-טבריה",
        "IL-Yavne" to "IL-יבנה",
        "IL-Mitzpe Ramon" to "IL-מצפה רמון",
        "IL-Betar Ilit" to "IL-ביתר עילית",
        "IL-Zefat" to "IL-צפת",
    )
    private val hebrewToEnglish = englishToHebrew.entries.associate { (english, hebrew) -> hebrew to english }

    fun toHebrew(value: String): String {
        val normalized = value.trim()
        return englishToHebrew.entries.firstOrNull { normalized.startsWith(it.key) }?.value ?: normalized
    }

    fun toEnglish(value: String): String {
        val normalized = value.trim()
        return hebrewToEnglish[normalized] ?: normalized
    }
}
