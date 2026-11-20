package se.kodhast.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.IconSize
import se.kodhast.ui.theme.Spacing

/**
 * Ett val av få alternativ (t.ex. tema: Ljust · Mörkt · System) som segmentknappar. Med temats djup ligger
 * segmenten i ett sänkt spår (som stegarens) och det valda lyfts upp som en egen knapp med knapparnas skugga,
 * skimmer och ljuskant (#97); ett segment krymper nedtryckt. Utan djup: Material 3:s konturerade segment.
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
        inactiveContainerColor = AppTheme.transparent
    )
    val depth = AppTheme.depth
    val row: @Composable (Modifier) -> Unit = { rowModifier ->
        SingleChoiceSegmentedButtonRow(rowModifier.fillMaxWidth()) {
            options.forEachIndexed { index, label ->
                val shape = SegmentedButtonDefaults.itemShape(index, options.size)
                val selected = index == selectedIndex
                val interactionSource = remember { MutableInteractionSource() }
                val lifted = Modifier.depth(shape, interactionSource, raised = selected, shadow = null)
                SegmentedButton(
                    selected = selected,
                    onClick = { onSelect(index) },
                    shape = shape,
                    modifier = lifted,
                    colors = colors,
                    interactionSource = interactionSource,
                    label = { Text(label, style = AppTheme.typography.pill) }
                )
            }
        }
    }
    if (depth == null) {
        row(modifier)
    } else {
        DepthTouchTarget(modifier) { SunkenSegments(options, selectedIndex, onSelect, depth.shadow, it) }
    }
}

/** Segmenten i ett sänkt spår; det valda en upphöjd pill. Spåret är lika högt som Material 3:s segment. */
@Composable
private fun SunkenSegments(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, shadow: Color, modifier: Modifier = Modifier) {
    val shape = AppTheme.shapes.pill
    Row(
        modifier
            .fillMaxWidth()
            .height(SEGMENT_HEIGHT)
            .background(AppTheme.extendedColors.rowTint, shape)
            .sunken(shape, shadow)
            .padding(TRACK_PADDING)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(TRACK_PADDING)
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            val interactionSource = remember { MutableInteractionSource() }
            val content = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            Row(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .depth(shape, interactionSource, raised = selected)
                    .background(if (selected) MaterialTheme.colorScheme.primaryContainer else AppTheme.transparent, shape)
                    .clip(shape)
                    .selectable(selected, interactionSource = interactionSource, indication = ripple(), role = Role.RadioButton) { onSelect(index) }
                    .padding(horizontal = Spacing.m),
                horizontalArrangement = Arrangement.spacedBy(Spacing.s, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selected) Icon(painterResource(R.drawable.ic_check), contentDescription = null, tint = content, modifier = Modifier.size(IconSize.small))
                Text(label, style = AppTheme.typography.pill, color = content, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/** Spårets höjd (Material 3:s segment) och luften runt segmenten i spåret. */
private val SEGMENT_HEIGHT: Dp = 40.dp
private val TRACK_PADDING: Dp = 3.dp
