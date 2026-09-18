package com.shahartal.myquietchannel.parasha

import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitInstance {

    internal fun createApi(baseUrl: String): JsonHebCalShabbatApi {
        val gson = GsonBuilder()
            .setStrictness(Strictness.LENIENT)
            .create()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addConverterFactory(ScalarsConverterFactory.create()) // fallback
            .build()
            .create(JsonHebCalShabbatApi::class.java)
    }

    val api: JsonHebCalShabbatApi by lazy { createApi("https://www.hebcal.com/") }
}
