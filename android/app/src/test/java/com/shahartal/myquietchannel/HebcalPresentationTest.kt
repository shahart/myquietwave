package com.shahartal.myquietchannel

import com.shahartal.myquietchannel.parasha.Item
import com.shahartal.myquietchannel.parasha.Leyning
import com.shahartal.myquietchannel.parasha.HebCalZmanimTimesModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

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

    @Test
    fun displayTimeReturnsEmptyForMalformedTimestamp() {
        assertEquals("", HebcalPresentation.displayTime("2026-09-18"))
        assertEquals("", HebcalPresentation.displayTime("not-a-date"))
        assertEquals("", HebcalPresentation.displayTime("2026-09-18T"))
    }

    @Test
    fun skipsPastDailyCalendarItemsButKeepsFutureItems() {
        val today = LocalDate.of(2026, 9, 22)

        assertEquals(
            true,
            HebcalPresentation.isPastDailyCalendarItem(item(category = "holiday", date = "2026-09-21"), today),
        )
        assertEquals(
            true,
            HebcalPresentation.isPastDailyCalendarItem(
                item(category = "", title = "Fast begins", date = "2026-09-21T05:00:00+03:00"),
                today,
            ),
        )
        assertEquals(
            false,
            HebcalPresentation.isPastDailyCalendarItem(item(category = "roshchodesh", date = "2026-09-22"), today),
        )
    }

    @Test
    fun findsMajorHolidayOnTheUpcomingSaturday() {
        val holiday = item(
            category = "holiday",
            title = "Sukkot I",
            subcat = "major",
            date = "2026-09-26",
        )

        assertEquals(
            holiday,
            HebcalPresentation.majorHolidayOnNextSaturday(
                listOf(holiday, item(category = "holiday", subcat = "major", date = "2026-09-27")),
                LocalDate.of(2026, 9, 22),
            ),
        )
    }

    @Test
    fun formatsZmanimDetailsInDisplayOrder() {
        val times = HebCalZmanimTimesModel(
            sunrise = "2026-09-18T06:00:00+03:00",
            sunset = "2026-09-18T18:00:00+03:00",
            beinHaShmashos = "2026-09-18T18:20:00+03:00",
            dusk = "2026-09-18T18:30:00+03:00",
            tzeit7083deg = "2026-09-18T18:40:00+03:00",
            tzeit72min = "2026-09-18T19:12:00+03:00",
            dawn = "2026-09-18T05:30:00+03:00",
            chatzot = "2026-09-18T12:00:00+03:00",
            chatzotNight = "2026-09-18T00:00:00+03:00",
            alotHaShachar = "2026-09-18T04:45:00+03:00",
            minchaGedola = "2026-09-18T13:00:00+03:00",
            plagHaMincha = "2026-09-18T17:00:00+03:00",
            minchaKetana = "2026-09-18T16:00:00+03:00",
            sofZmanShma = "2026-09-18T09:00:00+03:00",
            sofZmanTfilla = "2026-09-18T10:00:00+03:00",
            sofZmanShmaMGA = "2026-09-18T08:30:00+03:00",
            sofZmanTfillaMGA = "2026-09-18T09:30:00+03:00",
        )

        val details = HebcalPresentation.zmanimDetails(times)
        assertEquals("chatzot Night חצות הלילה:  0:00 ", details.lineSequence().first())
        assertEquals("Tzeit 72' צאת הכוכבים רבינו תם:  19:12 ", details.lineSequence().last())
        assertEquals(16, details.lines().size)
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
