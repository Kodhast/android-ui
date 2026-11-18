package se.kodhast.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.datetime.LocalDate

/**
 * "Välj dag" i en lista över datum (Kodhäst-standard, android-ui #68): ett chip med kalenderikon som står
 * först i listans filterrad och öppnar ett ark med [AppCalendar] – dagar i [marked] har en punkt, [today] är
 * markerad, framtiden är nedtonad och går inte att välja. Ett tryck på en dag (eller "Idag") stänger arket och
 * ger dagen till [onPick]; appen rullar då listan dit (`EntityListScreen`s `scrollTo`). Arket öppnas på
 * [today]s månad. Ingen vy att växla mellan och inget läge kvar efter valet.
 */
@Composable
fun DayPicker(
    marked: Set<LocalDate>,
    today: LocalDate,
    onPick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.day_picker_label)
) {
    var open by rememberSaveable { mutableStateOf(false) }
    AppFilterChip(label, selected = false, onClick = { open = true }, modifier = modifier, icon = R.drawable.ic_calendar)
    if (open) {
        // Månaden sparas som text: LocalDate ryms inte i en Bundle.
        var month by rememberSaveable { mutableStateOf(today.toString()) }
        val pick: (LocalDate) -> Unit = { day ->
            open = false
            onPick(day)
        }
        AppBottomSheet(
            title = label,
            onDismiss = { open = false },
            footer = { AppButton(stringResource(R.string.day_picker_today), { pick(today) }, variant = ButtonVariant.Text) }
        ) {
            AppCalendar(
                month = LocalDate.parse(month),
                onMonthChange = { month = it.toString() },
                marked = marked,
                selectedDate = null,
                onDateClick = pick,
                today = today,
                dimFuture = true
            )
        }
    }
}
