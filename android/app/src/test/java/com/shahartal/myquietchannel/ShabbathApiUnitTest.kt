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

        var res = ""

        val response =
            RetrofitInstance.api.getShabbatPerCity("IL-Jerusalem").execute()

        if (/*! */response.isSuccessful) {
            val hebcal = response.body()
            hebcal?.items?.forEach {
                if (it.category == "candles") {
                    res += (it.date + " כניסת שבת ")
                }
                else if (it.category == "havdalah") {
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
}