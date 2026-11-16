package se.kodhast.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import se.kodhast.ui.theme.AppTheme

/**
 * Ett val av få alternativ (t.ex. tema: Ljust · Mörkt · System) som segmentknappar. Med temats djup får det
 * valda segmentet skimmer och ljuskant (ingen skugga – segmenten sitter ihop).
 */
@Composable
fun AppSegmentedChoice(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SegmentedButtonDefaults.colors(
        activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
        activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        inactiveContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f)
    )
    val depth = AppTheme.depth
    val row: @Composable (Modifier) -> Unit = { rowModifier ->
        SingleChoiceSegmentedButtonRow(rowModifier.fillMaxWidth()) {
            options.forEachIndexed { index, label ->
                val shape = SegmentedButtonDefaults.itemShape(index, options.size)
                val selected = index == selectedIndex
                SegmentedButton(
                    selected = selected,
                    onClick = { onSelect(index) },
                    shape = shape,
                    modifier = if (depth != null && selected) Modifier.sheen(shape, depth) else Modifier,
                    colors = colors,
                    label = { Text(label, style = AppTheme.typography.pill) }
                )
            }
        }
    }
    if (depth == null) {
        row(modifier)
    } else {
        DepthTouchTarget(modifier, row)
    }
}
