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
}
