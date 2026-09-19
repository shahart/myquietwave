package com.shahartal.myquietchannel

import org.junit.Assert.assertEquals
import org.junit.Test

class ParashaPresentationTest {
    @Test
    fun splitsHyphenatedParashaNames() {
        assertEquals(
            ParashaNames("ניצבים", "פרשת וילך"),
            ParashaPresentation.names("ניצבים-וילך"),
        )
    }

    @Test
    fun splitsMaqafParashaNames() {
        assertEquals(
            ParashaNames("ניצבים", "פרשת וילך"),
            ParashaPresentation.names("ניצבים־וילך"),
        )
    }

    @Test
    fun preservesKnownMaqafOnlyNamesForWikiLinks() {
        assertEquals(
            ParashaNames("כי־תצא".replace("־", "_"), null),
            ParashaPresentation.names("כי־תצא"),
        )
    }

    @Test
    fun formatsHaftarahTextsAndReferences() {
        assertEquals(
            HaftarahTexts(
                " הפטרה ישעיהו 1:1\n2:2",
                "Isaiah 1",
                " הפטרה ספרדים יחזקאל 3:1",
                "Ezekiel 3",
            ),
            ParashaPresentation.haftarah("Isaiah 1:1|2:2", "Ezekiel 3:1"),
        )
    }

    @Test
    fun formatsCalendarEventsAndFastTimes() {
        assertEquals(
            "ראש השנה - שישי 18-9-2026",
            ParashaPresentation.calendarEvent("ראש השנה", "2026-09-18"),
        )
        assertEquals("05:42", ParashaPresentation.fastTime("2026-09-18T05:42:00+03:00"))
        assertEquals("", ParashaPresentation.fastTime("2026-09-18"))
    }

    @Test
    fun appendsDistinctNonBlankMemos() {
        val first = ParashaPresentation.appendMemo("", "ראש השנה", "מנהגי היום")
        assertEquals("\n\nראש השנה: מנהגי היום", first)
        assertEquals(first, ParashaPresentation.appendMemo(first, "יום", "מנהגי היום"))
        assertEquals(first, ParashaPresentation.appendMemo(first, "יום", "  "))
    }
}
