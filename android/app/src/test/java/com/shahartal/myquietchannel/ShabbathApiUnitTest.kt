package com.shahartal.myquietchannel

//import com.shahartal.myquietchannel.parasha.HebCal
import com.shahartal.myquietchannel.parasha.RetrofitInstance
import org.junit.Test

import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import kotlinx.coroutines.runBlocking

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ShabbathApiUnitTest {

    // this is not a real unit test as there's no mockWebServer
    fun fetchParasha(): String = runBlocking {

        var res = ""

        val hebcal = RetrofitInstance.api.getShabbat()
                    hebcal.items.forEach {
                        if (it.category == "parashat") {
                            res = it.hebrew
                        }
                    }
        res
    }

    @Test
    fun fetchParasha_works() {
        requireLiveTests()
        val res = fetchParasha()
        assertTrue("res: '$res'", res.contains("פרשת"))
    }

    fun fetchZmanim(): String = runBlocking {

        val city = "il-ramat gan"

        val regex = "^[A-Za-z -.'é]*$".toRegex()
        if (! regex.matches(city))
            fail("failed the regex")

        var res = ""

        var hebcal = RetrofitInstance.api.getShabbatPerCity(
                city,
                ue = Utils.getUe(city)
            )

            hebcal.items.forEach {
                if (it.category == "candles" && (it.memo.isNullOrEmpty() || it.memo.contains("Shabbat") || it.memo.contains("Parashat"))) {
                    res += (it.date + " הדלקת נרות ")
                }
                else if (it.category == "havdalah" && (it.memo.isNullOrEmpty() || it.memo.contains("Shabbath"))) {
                    res += (it.date + " הבדלה ")
                }
            }
        hebcal = RetrofitInstance.api.getShabbatByLoc("31.77", "35.19",
                ue = Utils.getUe(city)
            )

            hebcal.items.forEach {
                if (it.category == "candles" && (it.memo.isNullOrEmpty() || it.memo.contains("Shabbat") || it.memo.contains("Parashat"))) {
                    res += (it.date + " הדלקת נרות ")
                }
                else if (it.category == "havdalah" && (it.memo.isNullOrEmpty() || it.memo.contains("Shabbath"))) {
                    res += (it.date + " הבדלה ")
                }
            }
        res
    }

    @Test
    fun fetchZmanim_works() {
        requireLiveTests()
        val res = fetchZmanim()
        assertTrue("res: '$res'", res.contains("+"))
    }

    fun fetchDafYomi(): String {
        return runBlocking {
            RetrofitInstance.api.getDafYomi("2025-10-20", "2025-10-20")
                .items
                .firstOrNull { it.category == "dafyomi" }
                ?.hebrew
                .orEmpty()
        }
    }

    @Test
    fun fetchDafYomi_works() {
        requireLiveTests()
        val res = fetchDafYomi()
        assertTrue("res: '$res'", res.contains("דף"))
    }

    fun fetchYomZmanim(): String {
        var res = ""
        val city = "IL-Jerusalem"
        val hebcal =
            runBlocking { RetrofitInstance.api.getZmanimPerCity(Utils.getCity(city), Utils.getUe(city)) }
        res = hebcal.times?.sunrise.orEmpty() + ", " + hebcal.times?.chatzot.orEmpty() + ", " + hebcal.times?.sunset.orEmpty()
        return res
    }

    @Test
    fun fetchYomZmanimLoc() {
        requireLiveTests()
        var res = ""
        val city = "IL-Jerusalem"
        val hebcal =
            runBlocking { RetrofitInstance.api.getZmanimByLoc("32", "35", "off") }
        res = hebcal.times?.sunrise.orEmpty() + ", " + hebcal.times?.chatzot.orEmpty() + ", " + hebcal.times?.sunset.orEmpty()
    }

    @Test
    fun fetchYomZmanim_works() {
        requireLiveTests()
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

    private fun requireLiveTests() {
        assumeTrue(
            "Set RUN_LIVE_TESTS=true to call Hebcal",
            System.getenv("RUN_LIVE_TESTS")?.equals("true", ignoreCase = true) == true,
        )
    }
}
