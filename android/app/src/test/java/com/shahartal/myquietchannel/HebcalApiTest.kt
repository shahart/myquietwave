package com.shahartal.myquietchannel

import com.shahartal.myquietchannel.parasha.RetrofitInstance
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.runBlocking

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
    fun shabbatCityRequestUsesExpectedPathAndParsesResponse() = runBlocking {
        server.enqueue(jsonResponse("""
            {"title":"Shabbat","date":"2026-09-18","items":[
              {"title":"Parashat","date":"2026-09-19","category":"parashat","hebrew":"פרשת וילך","link":"https://example.test","memo":"","subcat":"","leyning":{"torah":"","haftarah":""}}
            ]}
        """))
        val api = RetrofitInstance.createApi(server.url("/").toString())

        val response = api.getShabbatPerCity("IL-Jerusalem", "off")

        assertEquals("פרשת וילך", response.items.single().hebrew)
        assertEquals(
            "/shabbat?cfg=json&city=IL-Jerusalem&ue=off",
            server.takeRequest().path,
        )
    }

    @Test
    fun dailyLearningSuspendRequestUsesExpectedPathAndParsesResponse() = runBlocking {
        server.enqueue(jsonResponse("""{"title":"","date":"","items":[]}"""))
        val api = RetrofitInstance.createApi(server.url("/").toString())

        api.getDafYomi("2026-09-18", "2026-09-18")

        assertEquals(
            "/hebcal?v=1&cfg=json&F=on&myomi=on&nyomi=on&dty=on&dps=on&o=on&min=on&start=2026-09-18&end=2026-09-18",
            server.takeRequest().path,
        )
    }

    @Test
    fun coordinateRequestEncodesCoordinatesAndElevationChoice() = runBlocking {
        server.enqueue(jsonResponse("""{"title":"","date":"","items":[]}"""))
        val api = RetrofitInstance.createApi(server.url("/").toString())

        api.getShabbatByLoc("-33.9", "151.2", "on")

        assertEquals(
            "/shabbat?cfg=json&tzid=Asia/Jerusalem&latitude=-33.9&longitude=151.2&ue=on",
            server.takeRequest().path,
        )
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
        server.enqueue(jsonResponse("""{"title":"","date":"","items":[]}"""))
        server.enqueue(jsonResponse("""{"date":"","location":{},"times":{}}"""))
        val repository = NetworkHebcalRepository(
            RetrofitInstance.createApi(server.url("/").toString())
        )
        val query = LocationQuery.GeoName("293222", useElevation = false)

        repository.shabbat(query)
        repository.zmanim(query)

        assertEquals("/shabbat?cfg=json&geonameid=293222&ue=off", server.takeRequest().path)
        assertEquals("/zmanim?cfg=json&geonameid=293222&ue=off", server.takeRequest().path)
    }

    private fun jsonResponse(body: String): MockResponse = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(body.trimIndent())
}
