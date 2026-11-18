package se.kodhast.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import se.kodhast.ui.common.DateFormat

/**
 * Tidsåtgång: en [QuantityStepper] i minuter (steg om [step]) och snabbval under, i en [LabeledGroup] som visar
 * tiden i timmar och minuter ("1 tim 30 min", `DateFormat.duration`). Ett snabbval som stämmer med värdet är markerat.
 */
@Composable
fun DurationRow(
    minutes: Int,
    onMinutesChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    quickPicks: List<Int> = DurationRowDefaults.QuickPicks,
    step: Int = DurationRowDefaults.STEP,
    label: String = stringResource(R.string.duration)
) {
    LabeledGroup(label, modifier, helper = DateFormat.duration(minutes)) {
        QuantityStepper(minutes, onMinutesChange, stringResource(R.string.minutes), range = 0..DurationRowDefaults.MAX_MINUTES, step = step)
        ChipRow {
            quickPicks.forEach { pick -> AppFilterChip(DateFormat.duration(pick), selected = pick == minutes, onClick = { onMinutesChange(pick) }) }
        }
    }
}

/** [DurationRow]s standardvärden: snabbvalen, steget och taket (ett dygn). */
object DurationRowDefaults {
    val QuickPicks: List<Int> = listOf(15, 30, 45, 60, 90, 120)
    const val STEP = 5
    const val MAX_MINUTES = 24 * 60
}
