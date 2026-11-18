package se.kodhast.ui.common

import java.time.format.DateTimeFormatter
import java.time.temporal.IsoFields
import java.util.Locale
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toJavaLocalDate

/**
 * Det enda sättet att visa och konvertera datum och tider i UI – för biblioteket (`DateField`, `TimeField`) och apparna.
 * Svenska, utan punkterna som svensk CLDR sätter efter förkortningar.
 */
object DateFormat {
    private val swedish = Locale.forLanguageTag("sv-SE")
    private val display = DateTimeFormatter.ofPattern("EEE d MMM yyyy", swedish)
    private val short = DateTimeFormatter.ofPattern("d MMM", swedish)
    private val weekdayShortDay = DateTimeFormatter.ofPattern("EEE d MMM", swedish)
    private val weekdayShortDayYear = DateTimeFormatter.ofPattern("EEE d MMM yyyy", swedish)
    private val monthYear = DateTimeFormatter.ofPattern("LLLL yyyy", swedish)
    private val weekdayName = DateTimeFormatter.ofPattern("EEE", swedish)
    private val weekdayFull = DateTimeFormatter.ofPattern("EEEE", swedish)
    private val dayMonth = DateTimeFormatter.ofPattern("EEEE d MMMM", swedish)
    private val dayMonthYear = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", swedish)
    private const val DAY_MILLIS = 86_400_000L

    /** "lör 19 dec 2026". */
    fun display(date: LocalDate): String = display.format(date.toJavaLocalDate()).replace(".", "")

    /** "2 okt" – t.ex. i förvalda namn ("Resa 2 okt"). */
    fun short(date: LocalDate): String = short.format(date.toJavaLocalDate()).replace(".", "")

    /** "tis 6 okt"; [capitalized] ger "Tis 6 okt" som rubrik, [withYear] "tis 6 okt 2025" för en dag ett annat år. */
    fun weekdayDay(date: LocalDate, capitalized: Boolean = false, withYear: Boolean = false): String = (if (withYear) weekdayShortDayYear else weekdayShortDay).format(date.toJavaLocalDate()).replace(".", "")
        .let { if (capitalized) it.replaceFirstChar { c -> c.titlecase(swedish) } else it }

    /** "oktober 2026" – en kalenders rubrik. */
    fun month(date: LocalDate): String = monthYear.format(date.toJavaLocalDate())

    /** "mån" – kortnamnet på dagens veckodag. */
    fun weekdayShort(date: LocalDate): String = weekdayShort(date.dayOfWeek)

    /** "mån" – veckodagens kortnamn. */
    fun weekdayShort(day: DayOfWeek): String = weekdayName.format(java.time.DayOfWeek.of(day.isoDayNumber)).replace(".", "")

    /** "måndag" – veckodagens hela namn. */
    fun weekdayLong(day: DayOfWeek): String = weekdayFull.format(java.time.DayOfWeek.of(day.isoDayNumber))

    /** "Söndag 4 oktober" med stor bokstav först; [withYear] lägger till året. */
    fun dayAndMonth(date: LocalDate, withYear: Boolean = false): String = weekdayDayAndMonth(date, withYear).replaceFirstChar { it.titlecase(swedish) }

    /** "söndag 4 oktober" – som [dayAndMonth] efter en etikett ("Idag · söndag 4 oktober"). */
    fun weekdayDayAndMonth(date: LocalDate, withYear: Boolean = false): String = (if (withYear) dayMonthYear else dayMonth).format(date.toJavaLocalDate())

    /** ISO-veckans nummer: veckan börjar på måndag och vecka 1 har årets första torsdag. */
    fun isoWeek(date: LocalDate): Int = date.toJavaLocalDate().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)

    /** "7" – dagen i månaden. */
    fun dayOfMonth(date: LocalDate): String = date.day.toString()

    /** "07:00" – 24 timmar, alltid två siffror. */
    fun time(time: LocalTime): String = "%02d:%02d".format(Locale.ROOT, time.hour, time.minute)

    /** Midnatt UTC, som Material-datumväljaren räknar. */
    fun toEpochMillis(date: LocalDate): Long = date.toEpochDays().toLong() * DAY_MILLIS

    fun fromEpochMillis(millis: Long): LocalDate = LocalDate.fromEpochDays(Math.floorDiv(millis, DAY_MILLIS).toInt())
}
