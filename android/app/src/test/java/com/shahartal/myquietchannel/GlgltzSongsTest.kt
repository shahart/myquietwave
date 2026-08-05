package com.shahartal.myquietchannel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GlgltzSongsTest {

    @Test
    fun parsesCurrentAndNextSongs() {
        val xml = """
            <songs>
                <song><titleName>Current title</titleName><artistName>Current artist</artistName><year>2024</year></song>
                <song><titleName>Next title</titleName><artistName>Next artist</artistName><year>2025</year></song>
            </songs>
        """.trimIndent()

        assertEquals(
            GlglzSongs("Current title . Current artist . 2024", "Next title. Next artist. 2025"),
            parseGlglzSongs(xml)
        )
    }

    @Test
    fun omitsNextSongWhenFeedOnlyContainsCurrentSong() {
        val songs = parseGlglzSongs(
            "<song><titleName>Only title</titleName><artistName>Artist</artistName><year>2020</year></song>"
        )

        assertEquals("Only title . Artist . 2020", songs?.current)
        assertNull(songs?.next)
    }

    @Test
    fun returnsNullWhenFeedHasNoTitle() {
        assertNull(parseGlglzSongs("<songs />"))
    }
}
