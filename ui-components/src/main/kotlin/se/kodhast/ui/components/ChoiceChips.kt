package se.kodhast.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import se.kodhast.ui.theme.Spacing

/**
 * Ett val bland några få: en [ChipRow] med en [AppFilterChip] per alternativ, det valda markerat
 * (form, enhet, kategori). [label] ger alternativets text. Med [onClear] avmarkerar ett tryck på det
 * valda alternativet valet (anropar [onClear] i stället för [onSelect]) – för val som får vara ej
 * angivna; standard väljer ett tryck alltid.
 *
 * Med [more] står de vanligaste valen ([options], t.ex. favoriterna) som chips och resten under ett fält
 * [moreLabel] (standard "Fler") som öppnar en meny – samma fältyta som datum och tid ([PickerField]). Fältet visar
 * valet när det är ett av [more]; TalkBack läser fältet med [morePickLabel] (standard "Välj") när inget av dem är valt.
 * Ett chip och ett menyval sätter samma val. Utan [more] ser raden ut som förut.
 */
@Composable
fun <T> ChoiceChips(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    onClear: (() -> Unit)? = null,
    more: List<T> = emptyList(),
    moreLabel: String = stringResource(R.string.choice_more),
    morePickLabel: String = stringResource(R.string.choose)
) {
    val chips: @Composable (Modifier) -> Unit = { chipModifier ->
        ChipRow(chipModifier) {
            options.forEach { option ->
                val isSelected = option == selected
                AppFilterChip(label(option), selected = isSelected, onClick = { if (isSelected && onClear != null) onClear() else onSelect(option) })
            }
        }
    }
    if (more.isEmpty()) {
        chips(modifier)
        return
    }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        if (options.isNotEmpty()) chips(Modifier)
        var open by rememberSaveable { mutableStateOf(false) }
        val items = more.map { option -> AppMenuItem(label(option), { onSelect(option) }) }
        Box(Modifier.fillMaxWidth()) {
            PickerField(
                label = moreLabel,
                shown = if (selected in more) label(selected) else "",
                pickLabel = morePickLabel,
                icon = R.drawable.ic_expand_more,
                onPick = { open = true }
            )
            AppMenuPopup(items, expanded = open, onDismiss = { open = false })
        }
    }
}

/**
 * Flera val bland några få (t.ex. var i kroppen det sitter): samma utseende som envalet, men varje
 * alternativ i [selected] är markerat och ett tryck växlar det ([onToggle]). TalkBack läser varje
 * chip med namn och om det är valt. Anropa med namngivet `onToggle`, så att envalet inte väljs.
 */
@Composable
fun <T> ChoiceChips(
    options: List<T>,
    selected: Set<T>,
    onToggle: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier
) {
    ChipRow(modifier) {
        options.forEach { option -> AppFilterChip(label(option), selected = option in selected, onClick = { onToggle(option) }) }
    }
}
