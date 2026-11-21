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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.IconSize
import se.kodhast.ui.theme.Spacing

/**
 * Ett val av få alternativ (t.ex. tema: Ljust · Mörkt · System) som segmentknappar. Med temats djup ligger
 * segmenten i ett sänkt spår (som stegarens) och det valda lyfts upp som en egen knapp med knapparnas skugga,
 * skimmer och ljuskant (#97); vid nytt val glider den till det nya segmentet (#100). Ett segment krymper nedtryckt. Utan djup: Material 3:s konturerade segment.
 *
 * [segmentWidth] styr segmentens bredd i spåret med djup: lika breda (standard) eller efter textens bredd (#98),
 * för val vars texter är olika långa. Utan djup är segmenten alltid lika breda.
 */
@Composable
fun AppSegmentedChoice(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    segmentWidth: SegmentWidth = SegmentWidth.Equal
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
        DepthTouchTarget(modifier) { SunkenSegments(options, selectedIndex, onSelect, depth.shadow, segmentWidth, it) }
    }
}

/** Segmentens bredd i [AppSegmentedChoice]. */
enum class SegmentWidth {
    /** Alla segment lika breda, som Material 3:s segment. */
    Equal,

    /**
     * Varje segment så brett som sin text och luft, det valda också med bocken, och utrymmet som blir över delas
     * lika (#98). Pillen glider och byter bredd, och segmenten byter bredd med den. Räcker inte spåret krymper segmenten i proportion till sin text.
     */
    FitText
}

/**
 * Segmenten i ett sänkt spår; det valda en upphöjd pill. Pillen är en egen yta bakom segmenten som glider till
 * nytt val med temats fjäder (#100), texten byter färg och bocken växer fram. Spåret är lika högt som Material 3:s segment.
 */
@Composable
private fun SunkenSegments(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    shadow: Color,
    segmentWidth: SegmentWidth,
    modifier: Modifier = Modifier
) {
    val shape = AppTheme.shapes.pill
    val naturalWidths = if (segmentWidth == SegmentWidth.FitText) naturalSegmentWidths(options) else null
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
            val sliding = if (naturalWidths == null) {
                Modifier.slidingSegment(options.size, TRACK_PADDING) { position.value }
            } else {
                Modifier.slidingFitSegment(naturalWidths, TRACK_PADDING) { position.value }
            }
            Box(
                sliding
                    .depth(shape, sources[selectedIndex], raised = true)
                    .background(MaterialTheme.colorScheme.primaryContainer, shape)
                    .testTag(SEGMENT_INDICATOR_TAG)
            )
        }
        val segment: @Composable (Int, String, Modifier) -> Unit = { index, label, widthModifier ->
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
                widthModifier
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
                    Box(Modifier.width(CHECK_SPACE * check).clipToBounds()) {
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
        if (naturalWidths == null) {
            Row(Modifier.matchParentSize().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(TRACK_PADDING)) {
                options.forEachIndexed { index, label -> segment(index, label, Modifier.weight(1f)) }
            }
        } else {
            FitSegmentRow(naturalWidths, TRACK_PADDING, { position.value }, Modifier.matchParentSize().selectableGroup()) {
                options.forEachIndexed { index, label -> segment(index, label, Modifier) }
            }
        }
    }
}

/** Varje segments naturliga bredd i px utan bock: texten och luften. Bocken får plats i [withCheck]. */
@Composable
private fun naturalSegmentWidths(options: List<String>): List<Float> {
    val measurer = rememberTextMeasurer()
    val style = AppTheme.typography.pill
    val density = LocalDensity.current
    return remember(options, style, density) {
        val extra = with(density) { (Spacing.m * 2).toPx() }
        options.map { measurer.measure(it, style, maxLines = 1).size.width + extra }
    }
}

/**
 * De naturliga bredderna med plats för bocken ([check] px) i det valda segmentet. Medan pillen glider ([position]
 * mellan två segment) flyttar platsen över, så att segmenten byter bredd i takt med pillen.
 */
internal fun withCheck(natural: List<Float>, position: Float, check: Float): List<Float> = natural.mapIndexed { i, w -> w + check * (1f - abs(position - i)).coerceIn(0f, 1f) }

/**
 * Segmentens bredd i px när de följer texten: den naturliga bredden plus en lika stor del av det som blir över.
 * Räcker inte [available] krymper alla i proportion till sin naturliga bredd.
 */
internal fun fitSegmentWidths(natural: List<Float>, available: Float, gap: Float): List<Float> {
    val room = available - gap * (natural.size - 1)
    val total = natural.sum()
    if (natural.isEmpty() || room.isInfinite()) return natural
    return if (total <= room) {
        val share = (room - total) / natural.size
        natural.map { it + share }
    } else {
        natural.map { (it * room / total).coerceAtLeast(0f) }
    }
}

/** Segmenten efter textens bredd, med [gap] emellan. Varje barn får sin bredd och spårets höjd. */
@Composable
private fun FitSegmentRow(naturalWidths: List<Float>, gap: Dp, position: () -> Float, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(content, modifier) { measurables, constraints ->
        val gapPx = gap.toPx()
        val available = if (constraints.hasBoundedWidth) constraints.maxWidth.toFloat() else Float.POSITIVE_INFINITY
        val widths = fitSegmentWidths(withCheck(naturalWidths, position(), CHECK_SPACE.toPx()), available, gapPx)
        val placeables = measurables.mapIndexed { i, m -> m.measure(Constraints.fixed(widths[i].roundToInt(), constraints.maxHeight)) }
        val width = if (constraints.hasBoundedWidth) constraints.maxWidth else (widths.sum() + gapPx * (widths.size - 1)).roundToInt()
        layout(width, constraints.maxHeight) {
            var x = 0f
            placeables.forEachIndexed { i, p ->
                p.placeRelative(x.roundToInt(), 0)
                x += widths[i] + gapPx
            }
        }
    }
}

/**
 * Som [slidingSegment], men för segment efter textens bredd: pillen tar läge och bredd mellan segmenten den
 * glider mellan, och fortsätter rakt ut när fjädern slår över ändarna.
 */
private fun Modifier.slidingFitSegment(naturalWidths: List<Float>, gap: Dp, position: () -> Float): Modifier = layout { measurable, constraints ->
    if (!constraints.hasBoundedWidth || naturalWidths.isEmpty()) return@layout measurable.measure(Constraints.fixed(0, 0)).let { layout(0, 0) {} }
    val gapPx = gap.toPx()
    val p = position()
    val widths = fitSegmentWidths(withCheck(naturalWidths, p, CHECK_SPACE.toPx()), constraints.maxWidth.toFloat(), gapPx)
    val starts = widths.runningFold(0f) { x, w -> x + w + gapPx }
    val from = if (widths.size == 1) 0 else floor(p).toInt().coerceIn(0, widths.size - 2)
    val to = minOf(from + 1, widths.size - 1)
    val t = if (to == from) 0f else p - from
    val width = (widths[from] + (widths[to] - widths[from]) * t).coerceAtLeast(0f)
    val x = starts[from] + (starts[to] - starts[from]) * t
    val placeable = measurable.measure(Constraints.fixed(width.roundToInt(), constraints.maxHeight))
    layout(constraints.maxWidth, constraints.maxHeight) {
        placeable.placeRelative(x.roundToInt(), 0)
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

/** Bockens plats framför texten i det valda segmentet. */
private val CHECK_SPACE: Dp = IconSize.small + Spacing.s

/** Den valda pillen i spåret, för proven. */
internal const val SEGMENT_INDICATOR_TAG = "segment-indicator"
