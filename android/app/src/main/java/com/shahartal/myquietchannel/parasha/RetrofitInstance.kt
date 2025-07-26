package com.shahartal.myquietchannel.parasha

import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitInstance {

    private val retrofit by lazy {

        val gson = GsonBuilder()
            .setStrictness(Strictness.LENIENT)
            .create()

        Retrofit.Builder()
            .baseUrl("https://www.hebcal.com/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addConverterFactory(ScalarsConverterFactory.create()) // fallback
            .build()
    }

    val api: JsonHebCalShabbatApi by lazy {
        retrofit.create(JsonHebCalShabbatApi::class.java)
    }
}