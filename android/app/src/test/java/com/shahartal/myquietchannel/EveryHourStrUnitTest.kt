package com.shahartal.myquietchannel

import org.junit.Test

import org.junit.Assert.*

class EveryHourStrUnitTest {

    @Test
    fun getEveryHourStr() {

        var plusHours = 4
        val everyHours = 4

        var nextHour = 19 // now
        var nextHoursStr = "19"

        // the real code:

        nextHour += everyHours
        if (nextHour > 24) nextHour -= 24
        nextHoursStr += ", $nextHour"

        while (plusHours < 24) {
            nextHour += everyHours
            plusHours += everyHours
            if (nextHour > 24) nextHour -= 24
            nextHoursStr += ", $nextHour"
        }

        assertEquals(nextHoursStr, "19, 23, 3, 7, 11, 15, 19")
    }

    @Test
    fun getYY() {
        assertEquals("ה'תשפה", Utils.getYY(5785))
    }
}