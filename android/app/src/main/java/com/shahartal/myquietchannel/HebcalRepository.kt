package com.shahartal.myquietchannel

import com.shahartal.myquietchannel.parasha.HebCal
import com.shahartal.myquietchannel.parasha.HebCalZmanimModel
import com.shahartal.myquietchannel.parasha.JsonHebCalShabbatApi
import retrofit2.Call

internal interface HebcalRepository {
    fun dailyLearning(isoDate: String): Call<HebCal>
    fun parasha(): Call<HebCal>
    fun shabbat(query: LocationQuery): Call<HebCal>
    fun zmanim(query: LocationQuery): Call<HebCalZmanimModel>
}

internal class NetworkHebcalRepository(
    private val api: JsonHebCalShabbatApi,
) : HebcalRepository {
    override fun dailyLearning(isoDate: String): Call<HebCal> = api.getDafYomi(isoDate, isoDate)

    override fun parasha(): Call<HebCal> = api.getShabbatPerCity("IL-Jerusalem", "off")

    override fun shabbat(query: LocationQuery): Call<HebCal> {
        val ue = LocationQueryParser.ue(query)
        return when (query) {
            is LocationQuery.City -> api.getShabbatPerCity(query.name, ue)
            is LocationQuery.GeoName -> api.getShabbatPerGeoNameId(query.id, ue)
            is LocationQuery.Coordinates -> api.getShabbatByLoc(query.latitude, query.longitude, ue)
        }
    }

    override fun zmanim(query: LocationQuery): Call<HebCalZmanimModel> {
        val ue = LocationQueryParser.ue(query)
        return when (query) {
            is LocationQuery.City -> api.getZmanimPerCity(query.name, ue)
            is LocationQuery.GeoName -> api.getZmanimPerGeoNameId(query.id, ue)
            is LocationQuery.Coordinates -> api.getZmanimByLoc(query.latitude, query.longitude, ue)
        }
    }
}
