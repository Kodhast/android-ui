@file:OptIn(ExperimentalMaterial3Api::class)

package se.kodhast.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.Spacing
import se.kodhast.ui.theme.TRACK_HEIGHT

/**
 * Skjutreglaget – det enda i alla appar: [label] som [GroupLabel], värdet som text ([valueText]) och valfri [badge]
 * (t.ex. en nivå som tonad `InfoPill`) ovanför ett spår. Värdet är ett heltal i [valueRange] och ägs av anroparen;
 * [step] låter reglaget bara stanna på var [step]:e värde från `valueRange.first` (standard varje heltal).
 *
 * Spåret är hela skalan svagt och den fyllda delen i full färg, båda med [trackBrush] (standard temats `primary`;
 * en skala skickar sin gradient, t.ex. `Brush.horizontalGradient(...)`). Går skalan under noll (−10…+10) utgår den
 * fyllda delen från nollan, som markeras med ett streck mitt på spåret. Knoppen är en ring i [thumbColor] i en
 * 48 dp stor yta, så att reglaget alltid är minst 48 dp högt. Färgen bär aldrig informationen ensam – texten visar
 * värdet. TalkBack läser [label] och [stateDescription] (standard [valueText], t.ex. "7, Hög"); huvudet ovanför
 * läses inte separat.
 */
@Composable
fun AppSlider(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    valueText: String,
    modifier: Modifier = Modifier,
    valueRange: IntRange = 0..10,
    step: Int = 1,
    enabled: Boolean = true,
    stateDescription: String = valueText,
    trackBrush: Brush? = null,
    thumbColor: Color = MaterialTheme.colorScheme.primary,
    badge: (@Composable () -> Unit)? = null
) {
    require(step >= 1) { "AppSlider: step måste vara minst 1" }
    val brush = trackBrush ?: SolidColor(MaterialTheme.colorScheme.primary)
    val span = (valueRange.last - valueRange.first).coerceAtLeast(1)
    val fraction = (value.coerceIn(valueRange) - valueRange.first).toFloat() / span
    val anchor = if (valueRange.first < 0 && valueRange.last > 0) -valueRange.first.toFloat() / span else 0f
    Column(modifier.fillMaxWidth().inactive(!enabled), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Row(Modifier.clearAndSetSemantics {}, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            GroupLabel(label, Modifier.weight(1f))
            Text(valueText, style = AppTheme.typography.quantity, color = MaterialTheme.colorScheme.onSurface)
            badge?.invoke()
        }
        val slider = remember(span, valueRange.first, step) {
            SliderState(value.toFloat(), (span / step - 1).coerceAtLeast(0), valueRange.first.toFloat()..valueRange.last.toFloat())
        }
        // Värdet ägs av anroparen; reglaget visar alltid det.
        SideEffect { slider.value = value.toFloat() }
        Slider(
            state = slider,
            onValueChange = { onValueChange(snapToStep(it, valueRange, step)) },
            modifier = Modifier.fillMaxWidth().semantics {
                contentDescription = label
                this.stateDescription = stateDescription
            },
            enabled = enabled,
            thumb = { SliderThumb(thumbColor) },
            track = { SliderTrack(brush, fraction, anchor) }
        )
    }
}

/** Reglagets läge [position] som värde: närmaste [step]-steg från `valueRange.first`, inom [valueRange]. */
internal fun snapToStep(position: Float, valueRange: IntRange, step: Int): Int = (valueRange.first + ((position - valueRange.first) / step).roundToInt() * step).coerceIn(valueRange)

@Composable
private fun SliderThumb(color: Color) {
    val round = AppTheme.shapes.pill
    Box(Modifier.size(TOUCH_TARGET), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(THUMB)
                .background(AppTheme.extendedColors.card, round)
                .border(THUMB_BORDER, color, round)
        )
    }
}

@Composable
private fun SliderTrack(brush: Brush, fraction: Float, anchor: Float) {
    val zeroMark = MaterialTheme.colorScheme.onSurfaceVariant
    Canvas(Modifier.fillMaxWidth().height(TRACK_HEIGHT)) {
        val y = size.height / 2
        val stroke = size.height
        drawLine(brush, Offset(0f, y), Offset(size.width, y), stroke, StrokeCap.Round, alpha = TRACK_ALPHA)
        val from = minOf(anchor, fraction) * size.width
        val to = maxOf(anchor, fraction) * size.width
        if (to > from) drawLine(brush, Offset(from, y), Offset(to, y), stroke, StrokeCap.Round)
        if (anchor > 0f) {
            val x = anchor * size.width
            drawLine(zeroMark, Offset(x, 0f), Offset(x, size.height), ZERO_MARK.toPx())
        }
    }
}

private val THUMB = 24.dp
private val THUMB_BORDER = 4.dp
private val ZERO_MARK = 2.dp
private const val TRACK_ALPHA = 0.3f
