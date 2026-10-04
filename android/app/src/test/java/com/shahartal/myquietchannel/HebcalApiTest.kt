package com.shahartal.myquietchannel

import com.shahartal.myquietchannel.parasha.RetrofitInstance
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

class HebcalApiTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun allShabbatEndpointsParseRepresentativePayloads() = runBlocking {
        repeat(4) { server.enqueue(jsonResponse(shabbatPayload)) }
        val api = RetrofitInstance.createApi(server.url("/").toString())

        val responses = listOf(
            api.getShabbat(),
            api.getShabbatPerCity("IL-Jerusalem", "off", "2026-10-04", "2026-10-11"),
            api.getShabbatByLoc("-33.9", "151.2", "on", "2026-10-04", "2026-10-11"),
            api.getShabbatPerGeoNameId("293222", "off", "2026-10-04", "2026-10-11"),
        )

        responses.forEach(::assertShabbatPayload)
        assertEquals("/shabbat?cfg=json", server.takeRequest().path)
        assertEquals(
            "/shabbat?cfg=json&city=IL-Jerusalem&ue=off&start=2026-10-04&end=2026-10-11",
            server.takeRequest().path,
        )
        assertEquals(
            "/shabbat?cfg=json&tzid=Asia/Jerusalem&latitude=-33.9&longitude=151.2&ue=on" +
                "&start=2026-10-04&end=2026-10-11",
            server.takeRequest().path,
        )
        assertEquals(
            "/shabbat?cfg=json&geonameid=293222&ue=off&start=2026-10-04&end=2026-10-11",
            server.takeRequest().path,
        )
    }

    @Test
    fun dailyLearningSuspendRequestUsesExpectedPathAndParsesResponse() = runBlocking {
        server.enqueue(jsonResponse("""
            {
              "title": "Hebcal Jewish Calendar",
              "date": "2026-09-24",
              "items": [
                {
                  "title": "Daf Yomi: Zevachim 48",
                  "date": "2026-09-24",
                  "category": "dafyomi",
                  "hebrew": "זבחים מח",
                  "link": "https://www.sefaria.org/Zevachim.48a?lang=bi",
                  "memo": ""
                }
              ]
            }
        """))
        val api = RetrofitInstance.createApi(server.url("/").toString())

        val response = api.getDafYomi("2026-09-18", "2026-09-18")

        assertEquals(
            "/hebcal?v=1&cfg=json&F=on&myomi=on&nyomi=on&dty=on&dps=on&min=on&o=on" +
                "&dw=on&yyomi=on&yys=on&dr1=on&dr3=on&dsm=on&dksa=on&ahsy=on&dshl=on&dcc=on&dpa=on" +
                "&start=2026-09-18&end=2026-09-18",
            server.takeRequest().path,
        )
        assertEquals("dafyomi", response.items.single().category)
        assertEquals("זבחים מח", response.items.single().hebrew)
        assertEquals(
            "https://www.sefaria.org/Zevachim.48a?lang=bi",
            response.items.single().link,
        )
    }

    @Test
    fun yomTovPayloadParsesTheFirstReadingOfItsLeyning() = runBlocking {
        server.enqueue(jsonResponse("""
            {"items":[{"category":"holiday","subcat":"major","title":"Sukkot I","date":"2026-09-26","hebrew":"סוכות יום א׳","yomtov":true,"leyning":{"1":"Leviticus 22:26-23:44","2":"Leviticus 23:1-8","haftarah":"Zechariah 14:1-21"}}]}
        """))
        val api = RetrofitInstance.createApi(server.url("/").toString())

        val leyning = api.getShabbat().items.single().leyning

        assertEquals("Leviticus 22:26-23:44", leyning?.firstReading)
        assertEquals("Zechariah 14:1-21", leyning?.haftarah)
    }

    @Test
    fun allZmanimEndpointsParseRepresentativePayloads() = runBlocking {
        repeat(3) { server.enqueue(jsonResponse(zmanimPayload)) }
        val api = RetrofitInstance.createApi(server.url("/").toString())

        val responses = listOf(
            api.getZmanimPerCity("IL-Jerusalem", "off"),
            api.getZmanimPerGeoNameId("293222", "on"),
            api.getZmanimByLoc("-33.9", "151.2", "off"),
        )

        responses.forEach(::assertZmanimPayload)
        assertEquals("/zmanim?cfg=json&city=IL-Jerusalem&ue=off", server.takeRequest().path)
        assertEquals("/zmanim?cfg=json&geonameid=293222&ue=on", server.takeRequest().path)
        assertEquals("/zmanim?cfg=json&tzid=Asia/Jerusalem&latitude=-33.9&longitude=151.2&ue=off", server.takeRequest().path)
    }

    @Test
    fun httpFailureIsReportedAsException() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(503).setBody("unavailable"))
        val api = RetrofitInstance.createApi(server.url("/").toString())

        try {
            api.getShabbat()
            fail("Expected HTTP failure")
        } catch (error: retrofit2.HttpException) {
            assertEquals(503, error.code())
        }
    }

    @Test
    fun repositoryRoutesGeoNameQueriesToMatchingEndpoints() = runBlocking {
        server.enqueue(jsonResponse(shabbatPayload))
        server.enqueue(jsonResponse(zmanimPayload))
        val repository = NetworkHebcalRepository(
            RetrofitInstance.createApi(server.url("/").toString())
        )
        val query = LocationQuery.GeoName("293222", useElevation = false)

        assertShabbatPayload(repository.shabbat(query, TODAY))
        assertZmanimPayload(repository.zmanim(query))

        assertEquals(
            "/shabbat?cfg=json&geonameid=293222&ue=off&start=2026-10-04&end=2026-10-11",
            server.takeRequest().path,
        )
        assertEquals("/zmanim?cfg=json&geonameid=293222&ue=off", server.takeRequest().path)
    }

    @Test
    fun parashaAsksTheJerusalemFeedForTheUpcomingWeek() = runBlocking {
        server.enqueue(jsonResponse(shabbatPayload))
        val repository = NetworkHebcalRepository(
            RetrofitInstance.createApi(server.url("/").toString())
        )

        assertShabbatPayload(repository.parasha(TODAY))

        assertEquals(
            "/shabbat?cfg=json&city=IL-Jerusalem&ue=off&start=2026-10-04&end=2026-10-11",
            server.takeRequest().path,
        )
    }

    @Test
    fun shabbatRequestsTheUpcomingWeekInsteadOfTheElapsedOne() = runBlocking {
        repeat(3) { server.enqueue(jsonResponse(shabbatPayload)) }
        val repository = NetworkHebcalRepository(
            RetrofitInstance.createApi(server.url("/").toString())
        )

        repository.shabbat(LocationQuery.City("IL-Jerusalem", useElevation = false), TODAY)
        repository.shabbat(LocationQuery.Coordinates("41.88", "-87.63", useElevation = true), TODAY)
        repository.shabbat(LocationQuery.GeoName("293222", useElevation = false), SUNDAY)

        assertTrue(server.takeRequest().path!!.contains("start=2026-10-04&end=2026-10-11"))
        assertTrue(server.takeRequest().path!!.contains("start=2026-10-04&end=2026-10-11"))
        val sundayPath = server.takeRequest().path!!
        assertTrue(sundayPath, sundayPath.contains("start=2026-10-05&end=2026-10-12"))
    }

    @Test
    fun repositoryPropagatesHttpFailures() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(503).setBody("unavailable"))
        val repository = NetworkHebcalRepository(
            RetrofitInstance.createApi(server.url("/").toString())
        )

        try {
            repository.shabbat(LocationQuery.City("IL-Jerusalem", useElevation = false))
            fail("Expected HTTP failure")
        } catch (error: retrofit2.HttpException) {
            assertEquals(503, error.code())
        }
    }

    @Test
    fun repositoryRejectsMalformedSuccessfulPayloads() {
        runBlocking {
            server.enqueue(jsonResponse("{}"))
            val repository = NetworkHebcalRepository(
                RetrofitInstance.createApi(server.url("/").toString())
            )

            val result = repository.zmanim(LocationQuery.City("IL-Jerusalem", useElevation = true))
            assertNull(result.times)
        }
    }

    private fun jsonResponse(body: String): MockResponse = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(body.trimIndent())

    private fun assertShabbatPayload(response: com.shahartal.myquietchannel.parasha.HebCal) {
        val item = response.items.single()
        assertEquals("parashat", item.category)
        assertEquals("פרשת וילך", item.hebrew)
        assertEquals("2026-09-26", item.date)
        assertEquals("Parashat Vayelech", item.title)
        assertEquals("major", item.subcat)
        assertEquals("https://example.test/vayelech", item.link)
        assertEquals("Shabbat Shuva", item.memo)
        assertTrue(item.yomtov)
        assertEquals("Hosea 14:2-10", item.leyning?.haftarah)
        assertEquals("Joel 2:15-27", item.leyning?.haftarah_sephardic)
    }

    private fun assertZmanimPayload(response: com.shahartal.myquietchannel.parasha.HebCalZmanimModel) {
        val times = requireNotNull(response.times)
        assertEquals("Jerusalem", response.location?.title)
        assertEquals("sunrise", times.sunrise)
        assertEquals("sunset", times.sunset)
        assertEquals("beinHaShmashos", times.beinHaShmashos)
        assertEquals("dusk", times.dusk)
        assertEquals("tzeit7083deg", times.tzeit7083deg)
        assertEquals("tzeit72min", times.tzeit72min)
        assertEquals("dawn", times.dawn)
        assertEquals("chatzot", times.chatzot)
        assertEquals("chatzotNight", times.chatzotNight)
        assertEquals("alotHaShachar", times.alotHaShachar)
        assertEquals("minchaGedola", times.minchaGedola)
        assertEquals("plagHaMincha", times.plagHaMincha)
        assertEquals("minchaKetana", times.minchaKetana)
        assertEquals("sofZmanShma", times.sofZmanShma)
        assertEquals("sofZmanTfilla", times.sofZmanTfilla)
        assertEquals("sofZmanShmaMGA", times.sofZmanShmaMGA)
        assertEquals("sofZmanTfillaMGA", times.sofZmanTfillaMGA)
    }

    private companion object {
        val TODAY: LocalDate = LocalDate.of(2026, 10, 4)
        val SUNDAY: LocalDate = LocalDate.of(2026, 10, 5)
        val shabbatPayload = """
            {"items":[{"category":"parashat","hebrew":"פרשת וילך","date":"2026-09-26","title":"Parashat Vayelech","subcat":"major","link":"https://example.test/vayelech","memo":"Shabbat Shuva","yomtov":true,"leyning":{"haftarah":"Hosea 14:2-10","haftarah_sephardic":"Joel 2:15-27"}}]}
        """
        val zmanimPayload = """
            {"location":{"title":"Jerusalem"},"times":{"sunrise":"sunrise","sunset":"sunset","beinHaShmashos":"beinHaShmashos","dusk":"dusk","tzeit7083deg":"tzeit7083deg","tzeit72min":"tzeit72min","dawn":"dawn","chatzot":"chatzot","chatzotNight":"chatzotNight","alotHaShachar":"alotHaShachar","minchaGedola":"minchaGedola","plagHaMincha":"plagHaMincha","minchaKetana":"minchaKetana","sofZmanShma":"sofZmanShma","sofZmanTfilla":"sofZmanTfilla","sofZmanShmaMGA":"sofZmanShmaMGA","sofZmanTfillaMGA":"sofZmanTfillaMGA"}}
        """
    }
}
