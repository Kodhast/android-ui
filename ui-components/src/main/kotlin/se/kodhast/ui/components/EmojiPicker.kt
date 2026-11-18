package se.kodhast.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import se.kodhast.ui.theme.AppTheme

/**
 * Välj en emoji ur [emojis]; den valda har [ChoiceGrid]s valmarkering (ringen). Listan är appens
 * (personens avatar, en aktivitetstyp) – biblioteket har ingen egen.
 */
@Composable
fun EmojiPicker(emojis: List<String>, selected: String, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    ChoiceGrid(emojis, selected, onSelect, label = { it }, modifier = modifier, selectionRing = true) { emoji, _ ->
        Text(emoji, style = AppTheme.typography.sectionTitle)
    }
}
