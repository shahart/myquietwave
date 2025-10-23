package com.shahartal.myquietchannel

//import com.shahartal.myquietchannel.parasha.HebCal
import com.shahartal.myquietchannel.parasha.RetrofitInstance
import org.junit.Test

import org.junit.Assert.*
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ShabbathApiUnitTest {

    // this is not a real unit test as there's no mockWebServer
    fun fetchParasha(): String {

        var res = ""

        val response =
        RetrofitInstance.api.getShabbat().execute()// enqueue(object : Callback<HebCal> {

//            override fun onResponse(call: Call<HebCal>, response: Response<HebCal>) {
                if (/*! */response.isSuccessful) {
                    // val str = response.body()
                    // Log.i("", "MainActivity fetchParasha " + str)
                    val hebcal = response.body()
                    hebcal?.items?.forEach {
                        if (it.category == "parashat") {
                            res = it.hebrew
                        }
                    }
                }
//            }

//            override fun onFailure(call: Call<HebCal>, t: Throwable) {
//            }
//        })

        else {
            fail(response.toString() + response.body())
        }

        return res
    }

    @Test
    fun fetchParasha_works() {
        val res = fetchParasha()
        assertTrue("res: '$res'", res.contains("פרשת"))
    }

    fun fetchZmanim(): String {

        val city = "il-ramat gan"

        val regex = "^[A-Za-z -.'é]*$".toRegex()
        if (! regex.matches(city))
            fail("failed the regex")

        var res = ""

        val response =
            RetrofitInstance.api.getShabbatPerCity(city).execute()

        if (/*! */response.isSuccessful) {
            val hebcal = response.body()
            hebcal?.items?.forEach {
                if (it.category == "candles" && (it.memo.isNullOrEmpty() || it.memo.contains("Shabbat") || it.memo.contains("Parashat"))) {
                    res += (it.date + " הדלקת נרות ")
                }
                else if (it.category == "havdalah" && (it.memo.isNullOrEmpty() || it.memo.contains("Shabbath"))) {
                    res += (it.date + " הבדלה ")
                }
            }
        }

        else {
            fail(response.toString() + response.body())
        }

        return res
    }

    @Test
    fun fetchZmanim_works() {
        val res = fetchZmanim()
        assertTrue("res: '$res'", res.contains("+"))
    }

    fun fetchDafYomi(): String {
        var res = ""
        val response =
            RetrofitInstance.api.getDafYomi("2025-10-20", "2025-10-20").execute()
        if (/*! */response.isSuccessful) {
            val hebcal = response.body()
            hebcal?.items?.forEach {
                if (it.category == "dafyomi") {
                    res = it.hebrew
                }
            }
        }
        else {
            fail(response.toString() + response.body())
        }
        return res
    }

    @Test
    fun fetchDafYomi_works() {
        val res = fetchDafYomi()
        assertTrue("res: '$res'", res.contains("דף"))
    }
}