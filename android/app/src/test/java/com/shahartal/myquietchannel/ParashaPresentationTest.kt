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
}
