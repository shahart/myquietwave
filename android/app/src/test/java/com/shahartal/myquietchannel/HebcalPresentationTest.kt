package com.shahartal.myquietchannel

import com.shahartal.myquietchannel.parasha.Item
import com.shahartal.myquietchannel.parasha.Leyning
import com.shahartal.myquietchannel.parasha.HebCalZmanimTimesModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.OffsetDateTime

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
        assertEquals(
            listOf(
                "חובות הלבבות יומי",
                "משנה ברורה עמוד יומי",
                "דף בהלכה דרשו יומי",
                "עמוד בהלכה דרשו יומי",
            ),
            summary.pendingLearning,
        )
    }

    @Test
    fun labelsTheAdditionalDailyStudies() {
        val summary = HebcalPresentation.dailyLearning(
            listOf(
                item(category = "mishnayomi", hebrew = "אהלות 7:1-2"),
                item(category = "nachyomi", hebrew = "ירמיהו י״ט"),
                item(category = "dailyPsalms", hebrew = "תהלים צ״ז-ק״ג"),
                item(category = "tanakhYomi", hebrew = "דברי הימים ס׳ כד", memo = "II Chronicles 34:2-35:5"),
                item(category = "yerushalmi", subcat = "vilna", hebrew = "שבועות דף לה"),
                item(category = "yerushalmi", subcat = "schottenstein", hebrew = "יבמות דף נד"),
                item(category = "dailyRambam1", hebrew = "הלכות גירושין פרק י"),
                item(category = "dailyRambam3", hebrew = "הלכות מקואות פרק 8-10"),
                item(category = "seferHaMitzvot", hebrew = "Day 241: P109"),
                item(category = "chofetzChaim", hebrew = "עשיין 5-6"),
                item(category = "shemiratHaLashon", hebrew = "Book I, שער הזכירה 3.1-3.5"),
                item(category = "arukhHaShulchanYomi", hebrew = "אורח חיים שנב:ג-שנג:ו"),
                item(category = "kitzurShulchanAruch", hebrew = "צט:ג-ק:ג"),
            ),
            "2026-10-01",
        )

        assertEquals(
            listOf(
                "משנה יומית: אהלות 7:1-2",
                "נ'ך יומי: ירמיהו י״ט",
                "תהלים יומי: תהלים צ״ז-ק״ג",
                "תנ'ך יומי: II Chronicles 34:2-35:5 - דברי הימים ס׳ כד",
                "ירושלמי יומי (ווילנא): שבועות דף לה",
                "ירושלמי יומי (שוטנשטיין): יבמות דף נד",
                "רמב״ם יומי: הלכות גירושין פרק י",
                "רמב״ם יומי 3 פרקים: הלכות מקואות פרק 8-10",
                "ספר המצוות: Day 241: P109",
                "החפץ חיים יומי: עשיין 5-6",
                "שמירת הלשון יומי: Book I, שער הזכירה 3.1-3.5",
                "ערוך השולחן יומי: אורח חיים שנב:ג-שנג:ו",
                "קיצור שולחן ערוך יומי: צט:ג-ק:ג",
            ),
            summary.additionalLearning,
        )
    }

    @Test
    fun tanakhYomiFallsBackToItsHebrewWhenTheMemoIsMissing() {
        val summary = HebcalPresentation.dailyLearning(
            listOf(item(category = "tanakhYomi", hebrew = "דברי הימים ס׳ כד")),
            "2026-10-01",
        )

        assertEquals(listOf("תנ'ך יומי: דברי הימים ס׳ כד"), summary.additionalLearning)
    }

    @Test
    fun mapsShabbatTimesAndMolad() {
        val summary = HebcalPresentation.shabbat(
            listOf(
                item(category = "candles", date = "2026-09-18T18:07:00+03:00"),
                item(category = "havdalah", date = "2026-09-19T19:20:00+03:00"),
                item(category = "mevarchim", hebrew = " מברכים חודש שבט", memo = "Molad: Monday and 4 chalakim"),
            ),
            now = OffsetDateTime.parse("2026-09-17T12:00:00+03:00"),
        )

        assertEquals(listOf(ShabbatTime(" 18:07 ", isPast = false)), summary.candleTimes)
        assertEquals(listOf(ShabbatTime(" 19:20 ", isPast = false)), summary.havdalahTimes)
        assertEquals("שני ו- 4 חלקים", summary.mevarchim?.molad)
        assertEquals("https://he.wikipedia.org/wiki/שבט_(חודש)", summary.mevarchim?.wikiUrl)
    }

    @Test
    fun marksPastCandleAndHavdalahTimes() {
        val now = OffsetDateTime.parse("2026-09-20T12:00:00+03:00")
        val summary = HebcalPresentation.shabbat(
            listOf(
                item(category = "candles", date = "2026-09-18T18:20:00+03:00"),
                item(category = "candles", date = "2026-09-25T18:07:00+03:00"),
                item(category = "havdalah", date = "2026-09-19T19:20:00+03:00"),
                item(category = "havdalah", date = "2026-09-26T19:25:00+03:00"),
            ),
            now = now,
        )

        assertEquals(
            listOf(ShabbatTime(" 18:20 ", isPast = true), ShabbatTime(" 18:07 ", isPast = false)),
            summary.candleTimes,
        )
        assertEquals(
            listOf(ShabbatTime(" 19:20 ", isPast = true), ShabbatTime(" 19:25 ", isPast = false)),
            summary.havdalahTimes,
        )
    }

    @Test
    fun treatsUnparsableShabbatTimesAsUpcoming() {
        val now = OffsetDateTime.parse("2026-09-20T12:00:00+03:00")

        assertEquals(false, HebcalPresentation.hasTimePassed("not-a-date", now))
        assertEquals(false, HebcalPresentation.hasTimePassed("2026-09-18", now))
        assertEquals(true, HebcalPresentation.hasTimePassed("2026-09-20T12:00:00+03:00", now))
        assertEquals(false, HebcalPresentation.hasTimePassed("2026-09-20T12:01:00+03:00", now))
    }

    @Test
    fun translatesHaftarahBookNamesAndFormatsMidnight() {
        assertEquals("ישעיהו 1:1", HebcalPresentation.translateBookNames("Isaiah 1:1"))
        assertEquals("ויקרא 22:26-23:44", HebcalPresentation.translateBookNames("Leviticus 22:26-23:44"))
        assertEquals("בראשית 1:1", HebcalPresentation.translateBookNames("Genesis 1:1"))
        assertEquals("דברים 11:16-21", HebcalPresentation.translateBookNames("Deuteronomy 11:16-21"))
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
    fun exposesTheReadingsOfAYomTovOnSaturday() {
        val holiday = item(
            category = "holiday",
            hebrew = "סוכות יום א׳",
            title = "Sukkot I",
            subcat = "major",
            date = "2026-09-26",
            leyning = Leyning(
                haftarah = "Zechariah 14:1-21",
                firstReading = "Leviticus 22:26-23:44",
            ),
        )

        val found = HebcalPresentation.majorHolidayOnNextSaturday(
            listOf(holiday),
            LocalDate.of(2026, 9, 22),
        )?.leyning

        assertEquals(
            "ויקרא 22:26-23:44",
            ParashaPresentation.leyningReading(found?.firstReading.orEmpty()),
        )
        assertEquals(
            " הפטרה זכריה 14:1-21",
            ParashaPresentation.haftarah(found?.haftarah.orEmpty(), null).ashkenazi,
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

        val (sunriseDetails, sunsetDetails) = HebcalPresentation.zmanimDetailsSections(times)
        assertEquals(8, sunriseDetails.lines().size)
        assertEquals(7, sunsetDetails.lines().size)
        assertEquals("chatzot Night חצות הלילה:  0:00 ", sunriseDetails.lineSequence().first())
        assertEquals("Tzeit 72' צאת הכוכבים רבינו תם:  19:12 ", sunsetDetails.lineSequence().last())
    }

    private fun item(
        category: String,
        hebrew: String = "",
        memo: String = "",
        date: String = "2026-09-18",
        title: String = "",
        subcat: String = "",
        link: String = "",
        leyning: Leyning = Leyning("", null),
    ) = Item(category, hebrew, leyning, memo, date, title, subcat, link)
}
