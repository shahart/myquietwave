package com.shahartal.myquietchannel

import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

internal fun interface TextHttpClient {
    fun get(url: String): String
}

internal class UrlConnectionTextHttpClient(
    private val connectTimeoutMillis: Int = 15_000,
    private val readTimeoutMillis: Int = 15_000,
) : TextHttpClient {
    override fun get(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = connectTimeoutMillis
            connection.readTimeout = readTimeoutMillis
            val status = connection.responseCode
            if (status !in 200..299) throw HttpStatusException(status)
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}

internal class HttpStatusException(val status: Int) : IllegalStateException("HTTP $status")

internal interface SongRepository {
    fun currentAndNext(station: Station): GlglzSongs?
}

internal class NetworkSongRepository(
    private val client: TextHttpClient,
) : SongRepository {
    override fun currentAndNext(station: Station): GlglzSongs? {
        val feed = station.songFeedName ?: return null
        val url = "https://glzxml.blob.core.windows.net/dalet/$feed-onair/onair.xml"
        return parseGlglzSongs(client.get(url))
    }
}

internal interface HaftarahRepository {
    fun connection(sourceUrl: String): String
}

internal class CachedHaftarahRepository(
    private val client: TextHttpClient,
    private val maxAttempts: Int = 2,
) : HaftarahRepository {
    init {
        require(maxAttempts > 0) { "maxAttempts must be positive" }
    }

    private val cache = ConcurrentHashMap<String, String>()

    override fun connection(sourceUrl: String): String = synchronized(cache) {
        cache[sourceUrl] ?: run {
            var lastError: Exception? = null
            repeat(maxAttempts) {
                try {
                    return@run HaftarahConnection.extract(client.get(sourceUrl)).also {
                        cache[sourceUrl] = it
                    }
                } catch (error: Exception) {
                    lastError = error
                    if (error is HttpStatusException && error.status < 500) throw error
                }
            }
            throw lastError ?: IllegalStateException("Unable to fetch the haftarah connection")
        }
    }
}
