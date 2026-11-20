package se.kodhast.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.BasicText
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorProducer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.IconSize
import se.kodhast.ui.theme.Spacing

/**
 * Ett val av få alternativ (t.ex. tema: Ljust · Mörkt · System) som segmentknappar. Med temats djup ligger
 * segmenten i ett sänkt spår (som stegarens) och det valda lyfts upp som en egen knapp med knapparnas skugga,
 * skimmer och ljuskant (#97); vid nytt val glider den till det nya segmentet (#100). Ett segment krymper nedtryckt. Utan djup: Material 3:s konturerade segment.
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

/**
 * Segmenten i ett sänkt spår; det valda en upphöjd pill. Pillen är en egen yta bakom segmenten som glider till
 * nytt val med temats fjäder (#100), texten byter färg och bocken växer fram. Spåret är lika högt som Material 3:s segment.
 */
@Composable
private fun SunkenSegments(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, shadow: Color, modifier: Modifier = Modifier) {
    val shape = AppTheme.shapes.pill
    val motion = MaterialTheme.motionScheme
    val sources = remember(options.size) { List(options.size) { MutableInteractionSource() } }
    val valid = selectedIndex in options.indices
    // Inget giltigt val: pillen döljs men står kvar i spåret, så att den inte glider in utifrån vid nästa val.
    val position = animateFloatAsState(selectedIndex.coerceIn(0, maxOf(options.size - 1, 0)).toFloat(), motion.defaultSpatialSpec(), label = "segmentval")
    Box(
        modifier
            .fillMaxWidth()
            .height(SEGMENT_HEIGHT)
            .background(AppTheme.extendedColors.rowTint, shape)
            .sunken(shape, shadow)
            .padding(TRACK_PADDING)
    ) {
        if (valid) {
            // Pillen trycks med det valda segmentet: den krymper och skuggan sjunker in, som ett valt segment gjorde förut.
            Box(
                Modifier
                    .slidingSegment(options.size, TRACK_PADDING) { position.value }
                    .depth(shape, sources[selectedIndex], raised = true)
                    .background(MaterialTheme.colorScheme.primaryContainer, shape)
                    .testTag(SEGMENT_INDICATOR_TAG)
            )
        }
        Row(Modifier.matchParentSize().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(TRACK_PADDING)) {
            options.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                val selectedColor = MaterialTheme.colorScheme.onPrimaryContainer
                // Texten byter färg när pillen passerar: helt vald när pillen står på segmentet, ovald ett segment bort.
                // Läses först när texten ritas, så glidet komponerar inte om segmenten.
                val content = ColorProducer {
                    if (valid) lerp(unselectedColor, selectedColor, (1f - abs(position.value - index)).coerceIn(0f, 1f)) else unselectedColor
                }
                val check by animateFloatAsState(if (selected) 1f else 0f, motion.defaultEffectsSpec(), label = "segmentets bock")
                Row(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .depth(shape, sources[index], raised = false)
                        .clip(shape)
                        .selectable(selected, interactionSource = sources[index], indication = ripple(), role = Role.RadioButton) { onSelect(index) }
                        .padding(horizontal = Spacing.m),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bocken växer fram i stället för att texten hoppar åt sidan.
                    if (check > 0f) {
                        Box(Modifier.width((IconSize.small + Spacing.s) * check).clipToBounds()) {
                            Icon(
                                painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                tint = selectedColor,
                                modifier = Modifier
                                    .wrapContentWidth(Alignment.Start, unbounded = true)
                                    .padding(end = Spacing.s)
                                    .size(IconSize.small)
                                    .graphicsLayer {
                                        alpha = check
                                        scaleX = check
                                        scaleY = check
                                        transformOrigin = TransformOrigin(0f, 0.5f)
                                    }
                            )
                        }
                    }
                    BasicText(label, style = AppTheme.typography.pill, color = content, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

/**
 * Den valda pillens bredd (ett av [count] lika breda segment med [gap] emellan) och läge vid segment [position],
 * som får vara mellan två segment medan pillen glider. Läget läses först vid placeringen, så glidet mäter inte om.
 */
private fun Modifier.slidingSegment(count: Int, gap: Dp, position: () -> Float): Modifier = layout { measurable, constraints ->
    // Utan begränsad bredd (en rullbar rad) får segmenten ingen bredd av Row:s weight; pillen heller ingen.
    if (!constraints.hasBoundedWidth) return@layout measurable.measure(Constraints.fixed(0, 0)).let { layout(0, 0) {} }
    val gapPx = gap.toPx()
    val width = ((constraints.maxWidth - gapPx * (count - 1)) / count).coerceAtLeast(0f)
    val placeable = measurable.measure(Constraints.fixed(width.roundToInt(), constraints.maxHeight))
    layout(constraints.maxWidth, constraints.maxHeight) {
        placeable.placeRelative(((width + gapPx) * position()).roundToInt(), 0)
    }
}

/** Spårets höjd (Material 3:s segment) och luften runt segmenten i spåret. */
private val SEGMENT_HEIGHT: Dp = 40.dp
private val TRACK_PADDING: Dp = 3.dp

/** Den valda pillen i spåret, för proven. */
internal const val SEGMENT_INDICATOR_TAG = "segment-indicator"
