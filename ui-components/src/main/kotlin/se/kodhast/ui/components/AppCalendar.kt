package se.kodhast.ui.components

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import se.kodhast.ui.common.DateFormat
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.Spacing

/**
 * Månadskalender: månaden som innehåller [month], veckan börjar på måndag. Dagar i [marked] får en punkt
 * ([DayMarkerDot]) och [today] den markerade punkten med ring; [selectedDate] är fylld i primärfärg i
 * `AppTheme.shapes.row`. Pilarna byter månad via [onMonthChange] (första dagen i den nya månaden). Varje dag
 * är en knapp som TalkBack läser med datum, "idag" och "har poster"; dagar utanför månaden visas inte.
 * [dimFuture] tonar ner dagarna efter [today], gör dem ovalbara och stänger pilen till månaden efter
 * [today]s. Flyttad från Dagbokens `DagbokenCalendar`; standardvärdena bevarar dess utseende.
 */
@Composable
fun AppCalendar(
    month: LocalDate,
    onMonthChange: (LocalDate) -> Unit,
    marked: Set<LocalDate>,
    selectedDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate? = null,
    dimFuture: Boolean = false
) {
    val first = remember(month) { LocalDate(month.year, month.month, 1) }
    val weeks = remember(first) { monthGrid(first) }
    val atEnd = dimFuture && today != null && first.plus(ONE_MONTH) > today
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        // Pilarna i ytans textfärg också där kalendern ligger utan kort som sätter den.
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            MonthRow(first, atEnd, onMonthChange)
        }
        Row(Modifier.clearAndSetSemantics {}) {
            weeks.first().forEach { day ->
                Text(
                    DateFormat.weekdayShort(day),
                    style = AppTheme.typography.caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        weeks.forEach { week ->
            Row {
                week.forEach { day ->
                    Box(Modifier.weight(1f).height(TOUCH_TARGET), contentAlignment = Alignment.Center) {
                        if (day.month == first.month) {
                            val future = dimFuture && today != null && day > today
                            DayCell(day, day in marked, day == selectedDate, day == today, future) { onDateClick(day) }
                        }
                    }
                }
            }
        }
    }
}

/** Månaden som rubrik mellan pilarna; pilen framåt stängd vid [atEnd]. */
@Composable
private fun MonthRow(first: LocalDate, atEnd: Boolean, onMonthChange: (LocalDate) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AppIconButton(R.drawable.ic_chevron_left, stringResource(R.string.calendar_previous_month), { onMonthChange(first.minus(ONE_MONTH)) })
        Text(
            DateFormat.month(first).replaceFirstChar { it.uppercase() },
            style = AppTheme.typography.sectionTitle,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f).semantics { heading() }
        )
        AppIconButton(R.drawable.ic_chevron_right, stringResource(R.string.calendar_next_month), { onMonthChange(first.plus(ONE_MONTH)) }, enabled = !atEnd)
    }
}

@Composable
private fun DayCell(day: LocalDate, marked: Boolean, selected: Boolean, isToday: Boolean, future: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val shape = AppTheme.shapes.row
    val description = dayDescription(day, isToday, marked)
    val interactionSource = remember { MutableInteractionSource() }
    // Med temats djup krymper dagen nedtryckt, och den valda lyfts som ett valt chip.
    val lifted = Modifier.depth(shape, interactionSource, raised = selected, shadow = Lift.Selected, active = !future)
    // Tryckytan är hela cellen (kolumnens bredd × 48 dp). I en smal kolumn (under 48 dp, t.ex. på 360 dp
    // breda skärmar) utvidgar Compose tryckytan till 48 dp åt sidorna.
    Box(
        Modifier
            .fillMaxSize()
            .clip(shape)
            .clickable(interactionSource, LocalIndication.current, enabled = !future, role = Role.Button, onClick = onClick)
            .clearAndSetSemantics {
                contentDescription = description
                this.selected = selected
            }
            .inactive(future),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier
                .size(CELL)
                .then(lifted)
                .clip(shape)
                .then(if (selected) Modifier.background(colors.primary) else Modifier),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DAY_MARKER_GAP, Alignment.CenterVertically)
        ) {
            Text(day.day.toString(), style = AppTheme.typography.body, color = if (selected) colors.onPrimary else colors.onSurface)
            DayMarkerDot(isToday, marked, selected)
        }
    }
}

/**
 * En dags markering i en kalender eller datumremsa – en form för samma sak: [isToday] ger en punkt i
 * `secondary` med ring (ringen i `progressDone.outline`, som klart-färgens kontur, annars `outline`), annars
 * ger [marked] en punkt i primärfärg. På en vald dag ([selected], fylld primärfärg) är punkten och ringen i
 * `onPrimary`. Ytan är alltid lika stor, så att dagar med och utan markering står i linje. Läses inte –
 * dagens knapp säger det ([dayDescription]).
 */
@Composable
fun DayMarkerDot(isToday: Boolean, marked: Boolean, selected: Boolean, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val todayRing = AppTheme.extendedColors.progressDone?.outline ?: colors.outline
    Box(modifier.size(DOT + RING * 2), contentAlignment = Alignment.Center) {
        when {
            isToday -> Dot(colors.secondary, ring = if (selected) colors.onPrimary else todayRing)
            marked -> Dot(if (selected) colors.onPrimary else colors.primary)
        }
    }
}

@Composable
private fun Dot(color: Color, ring: Color? = null) {
    val round = AppTheme.shapes.pill
    val ringed = if (ring == null) Modifier.size(DOT) else Modifier.size(DOT + RING * 2).border(RING, ring, round).padding(RING)
    Box(ringed.background(color, round))
}

/** En dags uppläsning i kalendern och datumremsan: datum, "idag", "har poster". */
@Composable
@ReadOnlyComposable
fun dayDescription(day: LocalDate, isToday: Boolean, marked: Boolean): String = listOfNotNull(
    DateFormat.display(day),
    stringResource(R.string.calendar_today).takeIf { isToday },
    stringResource(R.string.calendar_has_entries).takeIf { marked }
).joinToString(", ")

/** Veckorna (måndag–söndag) som täcker månaden som börjar på [first]. */
private fun monthGrid(first: LocalDate): List<List<LocalDate>> {
    val start = first.minus(first.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)
    val next = first.plus(ONE_MONTH)
    return generateSequence(start) { it.plus(ONE_WEEK) }
        .takeWhile { it < next }
        .map { monday -> List(DAYS_PER_WEEK) { monday.plus(it, DateTimeUnit.DAY) } }
        .toList()
}

private const val DAYS_PER_WEEK = 7
private val ONE_WEEK = DatePeriod(days = DAYS_PER_WEEK)
private val ONE_MONTH = DatePeriod(months = 1)

/** Avståndet mellan dagens siffra och markeringen under den. */
val DAY_MARKER_GAP = 2.dp

/** Den synliga dagen – ryms i kolumnen även på 360 dp breda skärmar. */
private val CELL = 40.dp
private val DOT = 6.dp
private val RING = 1.dp
