package com.shahartal.myquietchannel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class ContentRepositoriesTest {
    @Test
    fun songRepositoryBuildsFeedUrlAndParsesSongs() {
        var requestedUrl = ""
        val repository = NetworkSongRepository { url ->
            requestedUrl = url
            "<song><titleName>Title</titleName><artistName>Artist</artistName><year>2026</year></song>"
        }

        assertEquals("Title . Artist . 2026", repository.currentAndNext(Station.GLGLZ)?.current)
        assertEquals(
            "https://glzxml.blob.core.windows.net/dalet/glglz-onair/onair.xml",
            requestedUrl,
        )
    }

    @Test
    fun songRepositorySkipsStationsWithoutMetadataFeed() {
        val repository = NetworkSongRepository { error("HTTP must not be called") }
        assertNull(repository.currentAndNext(Station.KAN_88))
    }

    @Test
    fun haftarahRepositoryCachesSuccessfulContent() {
        var calls = 0
        val repository = CachedHaftarahRepository(client = {
            calls++
            HAFTARAH_HTML
        })

        assertEquals("נושאים בפרשה:\n\nנושא", repository.connection("https://example.test"))
        assertEquals("נושאים בפרשה:\n\nנושא", repository.connection("https://example.test"))
        assertEquals(1, calls)
    }

    @Test
    fun haftarahRepositoryRetriesServerErrorsButNotClientErrors() {
        var serverCalls = 0
        val retrying = CachedHaftarahRepository(client = {
            serverCalls++
            if (serverCalls == 1) throw HttpStatusException(503)
            HAFTARAH_HTML
        })
        assertEquals("נושאים בפרשה:\n\nנושא", retrying.connection("server-error"))
        assertEquals(2, serverCalls)

        var clientCalls = 0
        val nonRetrying = CachedHaftarahRepository(client = {
            clientCalls++
            throw HttpStatusException(404)
        })
        assertThrows(HttpStatusException::class.java) { nonRetrying.connection("client-error") }
        assertEquals(1, clientCalls)
    }

    @Test
    fun haftarahRepositoryRejectsInvalidRetryCount() {
        assertThrows(IllegalArgumentException::class.java) {
            CachedHaftarahRepository(client = { HAFTARAH_HTML }, maxAttempts = 0)
        }
    }

    companion object {
        private val HAFTARAH_HTML = """
            <div class="row_four">
              <h2>על הקשר בין ההפטרה לפרשה</h2>
              <div class="content_right"><p>נושאים בפרשה:</p><p>נושא</p></div>
            </div>
        """.trimIndent()
    }
}
