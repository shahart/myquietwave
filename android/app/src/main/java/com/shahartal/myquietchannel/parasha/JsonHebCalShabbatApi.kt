package com.shahartal.myquietchannel.parasha

import retrofit2.http.GET
import retrofit2.http.Query

interface JsonHebCalShabbatApi {

    @GET("shabbat?cfg=json")
    suspend fun getShabbat(): HebCal

    /**
     * Omitting start/end makes Hebcal answer with the Shabbat of the current week, which is
     * already over from Sunday onwards, so callers that show upcoming times must send a range.
     */
    @GET("shabbat?cfg=json")
    suspend fun getShabbatPerCity(@Query("city") city: String, @Query("ue") ue: String,
                                  @Query("start") start: String? = null,
                                  @Query("end") end: String? = null): HebCal

    @GET("shabbat?cfg=json&tzid=Asia/Jerusalem")
    suspend fun getShabbatByLoc(@Query("latitude") latitude: String,
                                @Query("longitude") longitude: String, @Query("ue") ue: String,
                                @Query("start") start: String? = null,
                                @Query("end") end: String? = null): HebCal

    @GET("shabbat?cfg=json")
    suspend fun getShabbatPerGeoNameId(@Query("geonameid") geonameid: String, @Query("ue") ue: String,
                                       @Query("start") start: String? = null,
                                       @Query("end") end: String? = null): HebCal

    // https://www.hebcal.com/hebcal?v=1&cfg=json&F=on&start=2025-10-20&end=2025-10-20

    @GET("hebcal?v=1&cfg=json&F=on&myomi=on&nyomi=on&dty=on&dps=on&min=on&o=on" +
            "&dw=on&yyomi=on&yys=on&dr1=on&dr3=on&dsm=on&dksa=on&ahsy=on&dshl=on&dcc=on&dpa=on")
    suspend fun getDafYomi(@Query("start") start: String,
                           @Query("end") end: String): HebCal

    
    @GET("zmanim?cfg=json")
    suspend fun getZmanimPerCity(@Query("city") city: String, @Query("ue") ue: String): HebCalZmanimModel

    @GET("zmanim?cfg=json")
    suspend fun getZmanimPerGeoNameId(@Query("geonameid") geonameid: String, @Query("ue") ue: String): HebCalZmanimModel

    @GET("zmanim?cfg=json&tzid=Asia/Jerusalem")
    suspend fun getZmanimByLoc(@Query("latitude") latitude: String,
                               @Query("longitude") longitude: String, @Query("ue") ue: String): HebCalZmanimModel

}
