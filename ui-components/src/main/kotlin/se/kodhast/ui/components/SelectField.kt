package se.kodhast.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

/**
 * Fält med rullgardin: ett [PickerField] som visar det valda ([selected] genom [optionLabel]) och öppnar en
 * [AppMenuPopup] med [options] vid tryck. Ett val stänger menyn och går till [onSelect].
 *
 * - [placeholder]: visas nedtonat när inget är valt (t.ex. "Alla fondbolag").
 * - [clearOptionLabel]: ett extra val överst som sätter valet till `null` (t.ex. "Alla fondbolag"); `null` =
 *   inget sådant val, ett värde måste väljas.
 * - [supportingText]: förklaring under fältet, läst tillsammans med det. [error] visas i stället, i felfärg.
 * - TalkBack läser fältet som en knapp med läget "Hopfälld" eller "Utfälld".
 * - [enabled] = `false` låser fältet med valet synligt (ett förifyllt formulär); menyn öppnas inte.
 */
@Composable
fun <T> SelectField(
    label: String,
    options: List<T>,
    selected: T?,
    optionLabel: (T) -> String,
    onSelect: (T?) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    clearOptionLabel: String? = null,
    supportingText: String? = null,
    error: String? = null,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    // Ett låst fält stänger menyn, så att den inte öppnas igen av sig själv när fältet låses upp.
    if (!enabled && expanded) expanded = false
    val choose = { option: T? ->
        expanded = false
        onSelect(option)
    }
    val items = listOfNotNull(clearOptionLabel?.let { AppMenuItem(it, { choose(null) }) }) +
        options.map { option -> AppMenuItem(optionLabel(option), { choose(option) }) }
    Box(modifier) {
        PickerField(
            label = label,
            shown = selected?.let(optionLabel).orEmpty(),
            pickLabel = stringResource(R.string.choose),
            icon = R.drawable.ic_expand_more,
            onPick = { expanded = true },
            error = error,
            helper = supportingText,
            emptyLabel = placeholder,
            enabled = enabled,
            stateDescription = stringResource(if (expanded && enabled) R.string.expanded else R.string.collapsed)
        )
        AppMenuPopup(items, expanded && enabled, onDismiss = { expanded = false })
    }
}
