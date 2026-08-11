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
                    // Log.i("myquietwave", "MainActivity fetchParasha " + str)
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

        var response =
            RetrofitInstance.api.getShabbatPerCity(
                city,
                ue = Utils.getUe(city)
            ).execute()

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

        response =
            RetrofitInstance.api.getShabbatByLoc("31.77", "35.19",
                ue = Utils.getUe(city)
            ).execute()

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

    fun fetchYomZmanim(): String {
        var res = ""
        val city = "IL-Jerusalem"
        val response =
            RetrofitInstance.api.getZmanimPerCity(Utils.getCity(city), Utils.getUe(city)).execute()
        if (/*! */response.isSuccessful) {
            val hebcal = response.body()
            if (hebcal != null) {
                res = hebcal.times.sunrise + ", " + hebcal.times.chatzot + ", " + hebcal.times.sunset
            }
        }
        else {
            fail(response.toString() + response.body())
        }
        return res
    }

    @Test
    fun fetchYomZmanimLoc() {
        var res = ""
        val city = "IL-Jerusalem"
        val response =
            RetrofitInstance.api.getZmanimByLoc("32", "35", "off").execute()
        if (response.isSuccessful) {
            val hebcal = response.body()
            if (hebcal != null) {
                res = hebcal.times.sunrise + ", " + hebcal.times.chatzot + ", " + hebcal.times.sunset
            }
        }
        else {
            fail(response.toString() + response.body())
        }
    }

    @Test
    fun fetchYomZmanim_works() {
        val res = fetchYomZmanim()
        assertTrue("res: '$res'", res.contains(", "))
    }

    @Test
    fun getUe_defaultsToOn() {
        assertEquals("on", Utils.getUe("IL-Jerusalem"))
        assertEquals("on", Utils.getUe("32.0853, 34.7818"))
    }

    @Test
    fun getUe_detectsExplicitUeOffSuffix() {
        assertEquals("off", Utils.getUe("IL-Jerusalem, ue"))
        assertEquals("off", Utils.getUe("IL-Jerusalem,ue"))
        assertEquals("off", Utils.getUe("IL-Jerusalem, UE"))
    }

    @Test
    fun getCity_stripsApiOptionSuffix() {
        assertEquals("IL-Jerusalem", Utils.getCity("IL-Jerusalem, ue"))
        assertEquals("293222", Utils.getCity("293222, ue"))
    }

    @Test
    fun getCity_leavesPlainLocationUnchanged() {
        assertEquals("IL-Ramat Gan", Utils.getCity("IL-Ramat Gan"))
    }

    @Test
    fun convertLocationIL_handlesSpecialShabbathApiCities() {
        assertEquals("IL-יבנה", Utils.convertLocationIL("IL-Yavne"))
        assertEquals("IL-מצפה רמון", Utils.convertLocationIL("IL-Mitzpe Ramon"))
        assertEquals("IL-מודיעין עילית", Utils.convertLocationIL("IL-Modiin Ilit"))
        assertEquals("IL-ביתר עילית", Utils.convertLocationIL("IL-Betar Ilit"))
        assertEquals("IL-צפת", Utils.convertLocationIL("IL-Zefat"))
    }

    @Test
    fun convertFromLocationIL_handlesSpecialShabbathApiCities() {
        assertEquals("IL-Yavne", Utils.convertFromLocationIL("IL-יבנה"))
        assertEquals("IL-Mitzpe Ramon", Utils.convertFromLocationIL("IL-מצפה רמון"))
        assertEquals("IL-Modiin Ilit", Utils.convertFromLocationIL("IL-מודיעין עילית"))
        assertEquals("IL-Betar Ilit", Utils.convertFromLocationIL("IL-ביתר עילית"))
        assertEquals("IL-Zefat", Utils.convertFromLocationIL("IL-צפת"))
    }
}
