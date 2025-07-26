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
}