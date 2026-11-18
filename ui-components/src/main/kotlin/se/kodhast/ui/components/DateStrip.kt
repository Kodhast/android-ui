package se.kodhast.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
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
 * Veckoremsan: veckan som innehåller [week] (måndag–söndag) som sju dagar ([DayTab]) med veckodag, datum och en
 * markering ([DayMarkerDot]). [selectedDate] är fylld i primärfärg; dagar i [datesWithEntries] får en punkt, [today]
 * en punkt med ring – och en bock när [todayDone] (läses som "Klar"). Dagar efter [today] är tonade och går inte att
 * välja. Svep åt höger visar föregående vecka och åt vänster nästa (aldrig en vecka som bara har framtida dagar):
 * [onWeekChange] får den nya veckans måndag, och anroparen äger veckan – så att remsan är deterministisk. Samma byten
 * finns som TalkBack-åtgärder ("Föregående vecka", "Nästa vecka"); varje dag läses med [dayDescription].
 */
@Composable
fun DateStrip(
    week: LocalDate,
    selectedDate: LocalDate,
    onSelect: (LocalDate) -> Unit,
    onWeekChange: (LocalDate) -> Unit,
    today: LocalDate,
    modifier: Modifier = Modifier,
    datesWithEntries: Set<LocalDate> = emptySet(),
    todayDone: Boolean = false
) {
    val monday = week.minus(week.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)
    val previous = monday.minus(ONE_WEEK)
    val next = monday.plus(ONE_WEEK).takeIf { it <= today }
    val change by rememberUpdatedState(onWeekChange)
    val threshold = with(LocalDensity.current) { SWIPE_THRESHOLD.toPx() }
    val previousLabel = stringResource(R.string.date_strip_previous_week)
    val nextLabel = stringResource(R.string.date_strip_next_week)
    Row(
        modifier
            .fillMaxWidth()
            .pointerInput(monday, next) {
                var drag = 0f
                detectHorizontalDragGestures(
                    onDragStart = { drag = 0f },
                    onDragEnd = {
                        when {
                            drag > threshold -> change(previous)
                            drag < -threshold && next != null -> change(next)
                        }
                    }
                ) { _, amount -> drag += amount }
            }
            .semantics {
                customActions = listOfNotNull(
                    CustomAccessibilityAction(previousLabel) {
                        change(previous)
                        true
                    },
                    next?.let {
                        CustomAccessibilityAction(nextLabel) {
                            change(it)
                            true
                        }
                    }
                )
            },
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        val days = remember(monday) { List(DAYS_IN_WEEK) { monday.plus(it, DateTimeUnit.DAY) } }
        days.forEach { day ->
            StripDay(
                day = day,
                selected = day == selectedDate,
                isToday = day == today,
                future = day > today,
                hasEntries = day in datesWithEntries,
                done = todayDone && day == today,
                onClick = { onSelect(day) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StripDay(
    day: LocalDate,
    selected: Boolean,
    isToday: Boolean,
    future: Boolean,
    hasEntries: Boolean,
    done: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val content = if (selected) colors.onPrimary else colors.onSurface
    val muted = if (selected) colors.onPrimary else colors.onSurfaceVariant
    val description = dayDescription(day, isToday, hasEntries)
    val doneLabel = stringResource(R.string.date_strip_done)
    DayTab(
        selected = selected,
        onClick = onClick,
        shape = AppTheme.shapes.row,
        modifier = modifier,
        background = if (selected) colors.primary else AppTheme.extendedColors.card,
        enabled = !future,
        inner = Modifier
            .semantics {
                contentDescription = description
                if (done) stateDescription = doneLabel
            }
            .inactive(future)
            .padding(vertical = Spacing.s),
        verticalArrangement = Arrangement.spacedBy(DAY_MARKER_GAP)
    ) {
        Column(Modifier.clearAndSetSemantics {}, horizontalAlignment = Alignment.CenterHorizontally) {
            DayTabWeekday(day, color = muted)
            Text(day.day.toString(), style = AppTheme.typography.itemTitle, color = content, textAlign = TextAlign.Center, maxLines = 1)
        }
        Box(Modifier.size(MARKER), contentAlignment = Alignment.Center) {
            if (done) {
                Icon(painterResource(R.drawable.ic_check), contentDescription = null, tint = if (selected) colors.onPrimary else colors.primary, modifier = Modifier.size(MARKER))
            } else {
                DayMarkerDot(isToday, hasEntries, selected)
            }
        }
    }
}

/**
 * Dagfliken – skelettet för en dag i en veckorad ([DateStrip] och appens egna veckorader): en centrerad kolumn minst
 * [TOUCH_TARGET] hög, klippt i [shape], med valfri [background], som är en flik (`Role.Tab`) – vald när [selected],
 * tryck ger [onClick], inte valbar när ![enabled]. [inner] läggs efter valet, innanför tryckytan: dagens egen
 * semantik, tonat läge och inre marginal. Veckodagen skrivs med [DayTabWeekday].
 */
@Composable
fun DayTab(
    selected: Boolean,
    onClick: () -> Unit,
    shape: Shape,
    modifier: Modifier = Modifier,
    inner: Modifier = Modifier,
    background: Color? = null,
    enabled: Boolean = true,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(Spacing.xs),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier
            .heightIn(min = TOUCH_TARGET)
            .clip(shape)
            .then(if (background != null) Modifier.background(background) else Modifier)
            .selectable(selected = selected, enabled = enabled, role = Role.Tab, onClick = onClick)
            .then(inner),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = verticalArrangement,
        content = content
    )
}

/** Dagflikens veckodag ("mån") – liten text i [color], som standard `onSurfaceVariant`, centrerad. */
@Composable
fun DayTabWeekday(day: LocalDate, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Text(DateFormat.weekdayShort(day), modifier, style = AppTheme.typography.caption, color = color, textAlign = TextAlign.Center, maxLines = 1)
}

/** Så långt ett svep måste gå för att byta vecka – kortare dragningar är bara ett tryck som gled. */
private val SWIPE_THRESHOLD = 48.dp

/** Ytan under dagens siffra: punkten eller bocken. */
private val MARKER = 14.dp

private const val DAYS_IN_WEEK = 7
private val ONE_WEEK = DatePeriod(days = DAYS_IN_WEEK)
