package com.shahartal.myquietchannel

import org.junit.Assert.assertEquals
import org.junit.Test

class HaftarahConnectionTest {
    @Test
    fun sourceUrl_removesPrefixCantillationAndNormalizesSeparators() {
        assertEquals(
            "https://kol-kore.org/%D7%A4%D7%A8%D7%A9%D7%95%D7%AA/" +
                "%D7%94%D7%A4%D7%98%D7%A8%D7%94-%D7%A4%D7%A8%D7%A9%D7%AA-%D7%9B%D7%99-%D7%AA%D7%A6%D7%90/",
            HaftarahConnection.sourceUrl("פרשת כִּי־תֵצֵא")
        )
    }

    @Test
    fun holidaySourceUrlsCoverKolKoreSpecialReadingPages() {
        assertEquals(
            listOf(
                "https://kol-kore.org/%D7%A4%D7%A8%D7%A9%D7%95%D7%AA-%D7%9E%D7%99%D7%95%D7%97%D7%93%D7%95%D7%AA/" +
                    "%D7%94%D7%A4%D7%98%D7%A8%D7%94-%D7%A9%D7%9E%D7%99%D7%A0%D7%99-%D7%A2%D7%A6%D7%A8%D7%AA/",
                "https://kol-kore.org/%D7%A4%D7%A8%D7%A9%D7%95%D7%AA-%D7%9E%D7%99%D7%95%D7%97%D7%93%D7%95%D7%AA/" +
                    "%D7%94%D7%A4%D7%98%D7%A8%D7%94-%D7%A9%D7%9E%D7%99%D7%A0%D7%99-%D7%A2%D7%A6%D7%A8%D7%AA-%D7%92%D7%95%D7%9C%D7%93/",
            ),
            HaftarahConnection.holidaySourceUrls("שמיני עצרת"),
        )
    }

    @Test
    fun extract_returnsOnlyConnectionContentFromMatchingSection() {
        val html = """
            <div class="row_four">
              <h2>על הקשר בין ההפטרה לפרשה</h2>
              <div class="content_right">
                הקדמה שלא נכללת
                <p>נושאים בפרשה:</p>
                <ul><li>נושא ראשון</li><li>נושא &amp; שני<br>שורה נוספת</li></ul>
              </div>
            </div>
        """.trimIndent()

        assertEquals(
            "נושאים בפרשה:\n\nנושא ראשון\n\nנושא & שני\nשורה נוספת",
            HaftarahConnection.extract(html)
        )
    }
}
