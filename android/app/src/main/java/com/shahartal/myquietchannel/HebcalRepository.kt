package com.shahartal.myquietchannel

import com.shahartal.myquietchannel.parasha.HebCal
import com.shahartal.myquietchannel.parasha.HebCalZmanimModel
import com.shahartal.myquietchannel.parasha.JsonHebCalShabbatApi
import retrofit2.Call

internal interface HebcalRepository {
    suspend fun dailyLearning(isoDate: String): HebCal
    fun parasha(): Call<HebCal>
    suspend fun shabbat(query: LocationQuery): HebCal
    fun zmanim(query: LocationQuery): Call<HebCalZmanimModel>
}

internal class NetworkHebcalRepository(
    private val api: JsonHebCalShabbatApi,
) : HebcalRepository {
    override suspend fun dailyLearning(isoDate: String): HebCal = api.getDafYomi(isoDate, isoDate)

    override fun parasha(): Call<HebCal> = api.getShabbatPerCity("IL-Jerusalem", "off")

    override suspend fun shabbat(query: LocationQuery): HebCal {
        val ue = LocationQueryParser.ue(query)
        val call = when (query) {
            is LocationQuery.City -> api.getShabbatPerCity(query.name, ue)
            is LocationQuery.GeoName -> api.getShabbatPerGeoNameId(query.id, ue)
            is LocationQuery.Coordinates -> api.getShabbatByLoc(query.latitude, query.longitude, ue)
        }
        return call.executeBody()
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

private fun <T> Call<T>.executeBody(): T {
    val response = execute()
    if (!response.isSuccessful) throw retrofit2.HttpException(response)
    return requireNotNull(response.body()) { "Empty Hebcal response (${response.code()})" }
}
