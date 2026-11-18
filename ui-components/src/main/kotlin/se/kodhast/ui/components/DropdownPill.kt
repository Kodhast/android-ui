package se.kodhast.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import se.kodhast.ui.theme.Tone

/**
 * Kompakt rullgardin, t.ex. i ett korts titelrad: en klickbar [InfoPill] med valt värde ([label]) och en pil, som
 * öppnar [AppMenuPopup] med [items]. Platssnål – mindre än `AppButton` – men tryckytan är minst 48 dp. Läses som en
 * rullgardin med sitt värde och åtgärden [onClickLabel] (standard "Välj"); markera valt alternativ i [items] med en
 * ikon (t.ex. bocken). [tone] som i [InfoPill].
 */
@Composable
fun DropdownPill(
    label: String,
    items: List<AppMenuItem>,
    modifier: Modifier = Modifier,
    tone: Tone = Tone.Primary,
    onClickLabel: String = stringResource(R.string.choose)
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        InfoPill(
            label,
            tone = tone,
            trailingIcon = R.drawable.ic_expand_more,
            role = Role.DropdownList,
            onClickLabel = onClickLabel,
            onClick = { expanded = true }
        )
        AppMenuPopup(items, expanded, onDismiss = { expanded = false })
    }
}
