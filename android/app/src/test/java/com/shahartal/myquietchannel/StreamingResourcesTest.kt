package com.shahartal.myquietchannel

import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.net.HttpURLConnection
import java.net.URI

@RunWith(Parameterized::class)
class StreamingResourcesTest(
    private val resourceName: String,
    private val resourceUrl: String,
) {

    @Test
    fun streamingResourceIsAvailable() {
        val connection = URI(resourceUrl).toURL().openConnection() as HttpURLConnection
        connection.connectTimeout = CONNECT_TIMEOUT_MILLIS
        connection.readTimeout = READ_TIMEOUT_MILLIS
        connection.instanceFollowRedirects = true
        connection.requestMethod = "GET"
        connection.setRequestProperty("Accept", "audio/*, */*")
        connection.setRequestProperty("Range", "bytes=0-0")
        connection.setRequestProperty("User-Agent", "MyQuietChannel stream availability test")

        try {
            val responseCode = connection.responseCode
            assertTrue(
                "$resourceName is unavailable at $resourceUrl: HTTP $responseCode ${connection.responseMessage}",
                responseCode in 200..299,
            )
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        private const val CONNECT_TIMEOUT_MILLIS = 10_000
        private const val READ_TIMEOUT_MILLIS = 10_000

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun streamingResources(): Collection<Array<String>> = listOf(
            arrayOf("GLZ", Utils.GLZ),
            arrayOf("GLGLZ", Utils.GLGLZ),
            arrayOf("GIMMEL", Utils.GIMMEL),
            arrayOf("BET", Utils.BET),
            // arrayOf("FM102", Utils.FM102),
            arrayOf("GALEY_ISRL", Utils.GALEY_ISRL),
            arrayOf("KAN_88", Utils.KAN_88),
            arrayOf("KOL_BARAMA", Utils.KOL_BARAMA),
            arrayOf("KOL_CHAI", Utils.KOL_CHAI),
            arrayOf("KOL_CHAI_MUSIC", Utils.KOL_CHAI_MUSIC),
            arrayOf("MORESHET", Utils.MORESHET),
        )
    }
}
