package com.shahartal.myquietchannel.parasha

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface JsonHebCalShabbatApi {

    @GET("shabbat?cfg=json")
    fun getShabbat(): Call<HebCal>

    @GET("shabbat?cfg=json")
    fun getShabbatPerCity(@Query("city") city: String): Call<HebCal>

    @GET("shabbat?cfg=json")
    fun getShabbatByLoc(@Query("latitude") latitude: String,
                        @Query("longitude") longitude: String): Call<HebCal>

    // https://www.hebcal.com/hebcal?v=1&cfg=json&F=on&start=2025-10-20&end=2025-10-20

    @GET("hebcal?v=1&cfg=json&F=on")
    fun getDafYomi(@Query("start") start: String,
                   @Query("end") end: String): Call<HebCal>

}