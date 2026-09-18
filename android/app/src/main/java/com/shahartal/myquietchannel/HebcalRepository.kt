package com.shahartal.myquietchannel

import com.shahartal.myquietchannel.parasha.HebCal
import com.shahartal.myquietchannel.parasha.HebCalZmanimModel
import com.shahartal.myquietchannel.parasha.JsonHebCalShabbatApi

internal interface HebcalRepository {
    suspend fun dailyLearning(isoDate: String): HebCal
    suspend fun parasha(): HebCal
    suspend fun shabbat(query: LocationQuery): HebCal
    suspend fun zmanim(query: LocationQuery): HebCalZmanimModel
}

internal class NetworkHebcalRepository(
    private val api: JsonHebCalShabbatApi,
) : HebcalRepository {
    override suspend fun dailyLearning(isoDate: String): HebCal = api.getDafYomi(isoDate, isoDate)

    override suspend fun parasha(): HebCal = api.getShabbatPerCity("IL-Jerusalem", "off")

    override suspend fun shabbat(query: LocationQuery): HebCal {
        val ue = LocationQueryParser.ue(query)
        val call = when (query) {
            is LocationQuery.City -> api.getShabbatPerCity(query.name, ue)
            is LocationQuery.GeoName -> api.getShabbatPerGeoNameId(query.id, ue)
            is LocationQuery.Coordinates -> api.getShabbatByLoc(query.latitude, query.longitude, ue)
        }
        return call
    }

    override suspend fun zmanim(query: LocationQuery): HebCalZmanimModel {
        val ue = LocationQueryParser.ue(query)
        return when (query) {
            is LocationQuery.City -> api.getZmanimPerCity(query.name, ue)
            is LocationQuery.GeoName -> api.getZmanimPerGeoNameId(query.id, ue)
            is LocationQuery.Coordinates -> api.getZmanimByLoc(query.latitude, query.longitude, ue)
        }
    }
}
