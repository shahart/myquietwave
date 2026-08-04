package com.shahartal.myquietchannel.luach


import java.time.Clock
import java.time.LocalDate

/**
 * A Hebrew calendar date. Months are numbered from Nissan: Nissan is 1 and
 * Tishrei is 7; Adar II is 13 in a leap year.
 */
data class HebrewDate(val year: Int, val month: Int, val day: Int) {
    init {
        require(year >= 1) { "Year must be >= 1." }
        require(month in 1..13) { "$month is an invalid month." }
        require(CalendarMath.isLeap(year) || month != 13) { "$year is not a leap year." }
        val monthLength = CalendarMath.monthLength(year, month)
        require(day in 1..monthLength) { "Given month has $monthLength days." }
    }

    internal val julianDayNumber: Long
        get() {
            var result = CalendarMath.elapsedDays(year)
            for (candidate in CalendarMath.months(year)) {
                if (candidate == month) break
                result += CalendarMath.monthLength(year, candidate)
            }
            return result + day - 1L + CalendarMath.HEBREW_EPOCH
        }

    internal fun weekday(): Int = ((julianDayNumber + 1L) % 7L).toInt() + 1

    internal fun followingShabbos(): HebrewDate = plusDays(7 - weekday())

    internal fun plusDays(days: Int): HebrewDate =
        fromJulianDayNumber(julianDayNumber + days)

    companion object {
        /** Uses the civil date at midnight, matching pyluach's `HebrewDate.today()`. */
        @JvmStatic
        fun today(): HebrewDate = today(Clock.systemDefaultZone())

        /** Clock overload for deterministic callers and tests. */
        @JvmStatic
        fun today(clock: Clock): HebrewDate = fromLocalDate(LocalDate.now(clock))

        @JvmStatic
        fun fromLocalDate(date: LocalDate): HebrewDate =
            fromJulianDayNumber(date.toEpochDay() + CalendarMath.UNIX_EPOCH_JDN)

        internal fun fromJulianDayNumber(jdn: Long): HebrewDate {
            require(jdn > CalendarMath.HEBREW_EPOCH) { "Date is before creation." }
            val relativeDay = jdn - CalendarMath.HEBREW_EPOCH
            var year = (relativeDay / 365L).toInt() + 2
            var firstDay = CalendarMath.elapsedDays(year)
            while (firstDay > relativeDay) {
                year--
                firstDay = CalendarMath.elapsedDays(year)
            }

            var daysRemaining = relativeDay - firstDay
            for (month in CalendarMath.months(year)) {
                val length = CalendarMath.monthLength(year, month)
                if (daysRemaining >= length) {
                    daysRemaining -= length
                } else {
                    return HebrewDate(year, month, daysRemaining.toInt() + 1)
                }
            }
            error("Could not convert Julian day $jdn")
        }
    }
}

internal object CalendarMath {
    const val HEBREW_EPOCH = 347_997L
    const val UNIX_EPOCH_JDN = 2_440_588L

    fun isLeap(year: Int): Boolean = ((7L * year + 1L) % 19L) < 7L

    fun elapsedDays(year: Int): Long {
        val monthsElapsed = (235L * year - 234L) / 19L
        val partsElapsed = 204L + 793L * (monthsElapsed % 1080L)
        val hoursElapsed = 5L + 12L * monthsElapsed +
                793L * (monthsElapsed / 1080L) + partsElapsed / 1080L
        val conjunctionDay = 1L + 29L * monthsElapsed + hoursElapsed / 24L
        val conjunctionParts = 1080L * (hoursElapsed % 24L) + partsElapsed % 1080L

        var postponedDay = if (
            conjunctionParts >= 19_440L ||
            (conjunctionDay % 7L == 2L && conjunctionParts >= 9_924L && !isLeap(year)) ||
            (conjunctionDay % 7L == 1L && conjunctionParts >= 16_789L && isLeap(year - 1))
        ) conjunctionDay + 1L else conjunctionDay

        if (postponedDay % 7L in setOf(0L, 3L, 5L)) postponedDay++
        return postponedDay
    }

    private fun daysInYear(year: Int): Long = elapsedDays(year + 1) - elapsedDays(year)

    fun monthLength(year: Int, month: Int): Int = when (month) {
        1, 3, 5, 7, 11 -> 30
        2, 4, 6, 10, 13 -> 29
        12 -> if (isLeap(year)) 30 else 29
        8 -> if (daysInYear(year) % 10L == 5L) 30 else 29
        9 -> if (daysInYear(year) % 10L == 3L) 29 else 30
        else -> throw IllegalArgumentException("Invalid month: $month")
    }

    fun months(year: Int): List<Int> = if (isLeap(year)) {
        listOf(7, 8, 9, 10, 11, 12, 13, 1, 2, 3, 4, 5, 6)
    } else {
        listOf(7, 8, 9, 10, 11, 12, 1, 2, 3, 4, 5, 6)
    }
}
