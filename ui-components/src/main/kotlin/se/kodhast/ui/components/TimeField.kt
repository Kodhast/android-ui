@file:OptIn(ExperimentalMaterial3Api::class)

package se.kodhast.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TimeScroll
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.datetime.LocalTime
import se.kodhast.ui.common.DateFormat
import se.kodhast.ui.theme.AppTheme

/**
 * Tidsfält: visar tiden som "07:00" och öppnar tidsväljaren (24 timmar) vid tryck – samma
 * beteende som appens datumfält ([PickerField]). TalkBack läser fältet som en knapp med etikett och tid.
 */
@Composable
fun TimeField(
    label: String,
    time: LocalTime,
    onTimeChange: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    helper: String? = null
) {
    var picking by rememberSaveable { mutableStateOf(false) }
    val pickLabel = stringResource(R.string.pick_time)
    PickerField(
        label = label,
        shown = DateFormat.time(time),
        pickLabel = pickLabel,
        icon = R.drawable.ic_clock,
        onPick = { picking = true },
        modifier = modifier,
        error = error,
        helper = helper
    )
    if (picking) {
        TimePickerPopup(time, pickLabel, onPick = {
            onTimeChange(it)
            picking = false
        }, onDismiss = { picking = false })
    }
}

/** Tidsväljaren (24 timmar, rullhjul för timme och minut) med [title], OK och Avbryt – för [TimeField] och appens egna tidsrader. Med temats djup får den kanten som [ConfirmDialog]. */
@Composable
fun TimePickerPopup(time: LocalTime, title: String, onPick: (LocalTime) -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val state = rememberTimePickerState(initialHour = time.hour, initialMinute = time.minute, is24Hour = true)
    TimePickerDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.floating(AppTheme.shapes.dialog, shadow = false),
        shape = AppTheme.shapes.dialog,
        confirmButton = { AppButton(stringResource(R.string.ok), onClick = { onPick(LocalTime(state.hour, state.minute)) }, variant = ButtonVariant.Text) },
        title = { Text(title, style = AppTheme.typography.sectionTitle) },
        dismissButton = { AppButton(stringResource(R.string.cancel), onDismiss, variant = ButtonVariant.Text) },
        containerColor = AppTheme.extendedColors.card
    ) {
        // Hjulen ligger nedsänkta i kortet med samma spår som segmentvalet. Material 3 tar hjulens
        // bakgrund ur surfaceContainerLowest, som i ljust läge är lika vit som kortet.
        val scheme = MaterialTheme.colorScheme
        val track = AppTheme.extendedColors.rowTint
        val wheelScheme = remember(scheme, track) { scheme.copy(surfaceContainerLowest = track) }
        MaterialTheme(colorScheme = wheelScheme) {
            TimeScroll(state, TimePickerDefaults.shapes())
        }
    }
}
