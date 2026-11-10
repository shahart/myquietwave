package com.shahartal.myquietchannel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class HaftarahArticleTest {
    @Test
    fun sourceUrlRemovesPrefixCantillationAndNormalizesSeparators() {
        assertEquals(
            "https://www.haftara.co.il/%D7%94%D7%A4%D7%98%D7%A8%D7%AA-%D7%A4%D7%A8%D7%A9%D7%AA-%D7%9B%D7%99-%D7%AA%D7%A6%D7%90/",
            HaftarahArticleParser.sourceUrl("פרשת כִּי־תֵצֵא"),
        )
    }

    @Test
    fun extractReturnsArticleBodyAsReadableText() {
        val html = """
            <article>
              <div class="entry-content">
                <h1>הפטרת פרשת תולדות</h1>
                <p>פתיחה &amp; מבוא<br>שורה שנייה</p>
                <div><p>פסקה נוספת</p></div>
              </div>
            </article>
        """.trimIndent()

        assertEquals(
            "הפטרת פרשת תולדות\n\nפתיחה & מבוא\nשורה שנייה\n\nפסקה נוספת",
            HaftarahArticleParser.extract(html),
        )
    }

    @Test
    fun extractFailsWhenTheArticleBodyIsMissing() {
        assertThrows(IllegalStateException::class.java) {
            HaftarahArticleParser.extract("<html><body>No article</body></html>")
        }
    }

    @Test
    fun repositoryFetchesTheSelectedParashaArticleAndCachesIt() {
        var calls = 0
        var requestedUrl = ""
        val repository = CachedHaftarahArticleRepository { url ->
            calls++
            requestedUrl = url
            "<div class=\"entry-content\"><p>תוכן להפטרה</p></div>"
        }

        val first = repository.article("פרשת תולדות")
        val second = repository.article("פרשת תולדות")

        assertEquals(
            "https://www.haftara.co.il/%D7%94%D7%A4%D7%98%D7%A8%D7%AA-%D7%A4%D7%A8%D7%A9%D7%AA-%D7%AA%D7%95%D7%9C%D7%93%D7%95%D7%AA/",
            requestedUrl,
        )
        assertEquals("תוכן להפטרה", first.text)
        assertEquals(first, second)
        assertEquals(1, calls)
    }
}
