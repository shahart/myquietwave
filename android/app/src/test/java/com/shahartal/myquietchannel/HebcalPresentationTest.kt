package com.shahartal.myquietchannel

import com.shahartal.myquietchannel.parasha.Item
import com.shahartal.myquietchannel.parasha.Leyning
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HebcalPresentationTest {
    @Test
    fun mapsDailyLearningIntoDisplayData() {
        val summary = HebcalPresentation.dailyLearning(
            listOf(
                item(category = "dafyomi", hebrew = "זבחים דף א", link = "daf"),
                item(category = "mishnayomi", hebrew = "ברכות א"),
                item(category = "omer", hebrew = "היום יום אחד לעומר", link = "omer"),
                item(category = "holiday", title = "Leil Selichot", subcat = "minor"),
            ),
            "2026-09-05",
        )

        assertEquals(LinkedText("זבחים דף א", "daf"), summary.dafYomi)
        assertEquals(listOf("משנה יומית: ברכות א"), summary.additionalLearning)
        assertEquals("omer", summary.omer?.link)
        assertEquals("ליל סליחות 5-9-2026\n", summary.selichotText)
    }

    @Test
    fun dailyLearningHandlesEmptyResponses() {
        val summary = HebcalPresentation.dailyLearning(emptyList(), "2026-09-05")
        assertNull(summary.dafYomi)
        assertNull(summary.omer)
        assertEquals(emptyList<String>(), summary.additionalLearning)
    }

    @Test
    fun mapsShabbatTimesAndMolad() {
        val summary = HebcalPresentation.shabbat(
            listOf(
                item(category = "candles", date = "2026-09-18T18:07:00+03:00"),
                item(category = "havdalah", date = "2026-09-19T19:20:00+03:00"),
                item(category = "mevarchim", hebrew = " מברכים חודש שבט", memo = "Molad: Monday and 4 chalakim"),
            )
        )

        assertEquals(listOf(" 18:07 "), summary.candleTimes)
        assertEquals(listOf(" 19:20 "), summary.havdalahTimes)
        assertEquals("שני ו- 4 חלקים", summary.mevarchim?.molad)
        assertEquals("https://he.wikipedia.org/wiki/שבט_(חודש)", summary.mevarchim?.wikiUrl)
    }

    @Test
    fun translatesHaftarahBookNamesAndFormatsMidnight() {
        assertEquals("ישעיהו 1:1", HebcalPresentation.translateBookNames("Isaiah 1:1"))
        assertEquals(" 0:05 ", HebcalPresentation.displayTime("2026-09-18T00:05:00+03:00"))
    }

    private fun item(
        category: String,
        hebrew: String = "",
        memo: String = "",
        date: String = "2026-09-18",
        title: String = "",
        subcat: String = "",
        link: String = "",
    ) = Item(category, hebrew, Leyning("", null), memo, date, title, subcat, link)
}
