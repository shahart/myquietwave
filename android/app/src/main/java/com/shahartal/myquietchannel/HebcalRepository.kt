package com.shahartal.myquietchannel

import com.shahartal.myquietchannel.parasha.HebCal
import com.shahartal.myquietchannel.parasha.HebCalZmanimModel
import com.shahartal.myquietchannel.parasha.JsonHebCalShabbatApi
import java.time.LocalDate

internal interface HebcalRepository {
    suspend fun dailyLearning(isoDate: String): HebCal

    /**
     * The range matters here too: without it the shabbat endpoint answers with whatever week
     * its HTTP cache holds, which is often the elapsed one and then carries no parashat.
     */
    suspend fun parasha(today: LocalDate = LocalDate.now()): HebCal

    suspend fun shabbat(query: LocationQuery, today: LocalDate = LocalDate.now()): HebCal
    suspend fun zmanim(query: LocationQuery): HebCalZmanimModel
}

internal class NetworkHebcalRepository(
    private val api: JsonHebCalShabbatApi,
) : HebcalRepository {
    companion object {
        const val SHABBAT_WINDOW_DAYS = 7L
    }

    override suspend fun dailyLearning(isoDate: String): HebCal = api.getDafYomi(isoDate, isoDate)

    override suspend fun parasha(today: LocalDate): HebCal =
        api.getShabbatPerCity("IL-Jerusalem", "off", today.toString(), endOfShabbatWindow(today))

    override suspend fun shabbat(query: LocationQuery, today: LocalDate): HebCal {
        val ue = LocationQueryParser.ue(query)
        val start = today.toString()
        val end = endOfShabbatWindow(today)
        val call = when (query) {
            is LocationQuery.City -> api.getShabbatPerCity(query.name, ue, start, end)
            is LocationQuery.GeoName -> api.getShabbatPerGeoNameId(query.id, ue, start, end)
            is LocationQuery.Coordinates -> api.getShabbatByLoc(query.latitude, query.longitude, ue, start, end)
        }
        return call
    }

    private fun endOfShabbatWindow(today: LocalDate): String = today.plusDays(SHABBAT_WINDOW_DAYS).toString()

    override suspend fun zmanim(query: LocationQuery): HebCalZmanimModel {
        val ue = LocationQueryParser.ue(query)
        return when (query) {
            is LocationQuery.City -> api.getZmanimPerCity(query.name, ue)
            is LocationQuery.GeoName -> api.getZmanimPerGeoNameId(query.id, ue)
            is LocationQuery.Coordinates -> api.getZmanimByLoc(query.latitude, query.longitude, ue)
        }
    }
}
