package se.kodhast.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.unit.dp
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.Spacing

/**
 * Framstegsraden: hur många av [total] som är klara ([done]), som text ovanför ett rundat spår som fylls
 * animerat (temats `motionScheme`, fjädrande). Texterna är appens: [label] ("9 av 14 packat", "4 av 9 klara")
 * och [doneLabel] när allt är klart ("14 av 14 · allt packat", "9 av 9 · allt klart").
 *
 * Färgerna kommer ur temat: spåret `ExtendedColors.track`, fyllnaden `primary` och, när allt är klart,
 * `ExtendedColors.progressDone` (fyllnad och valfri kontur). [celebrate] = `false` håller raden i
 * primärfärgen med [label] även när allt är klart (t.ex. en tidigare dag). [compact] ger ett kort spår
 * och "9 / 14" bredvid – för en listrad. TalkBack läser texten och framsteget (`progressBarRangeInfo`);
 * [doneDescription] ger klart-läget en egen talad text (standard `null` = [doneLabel]).
 */
@Composable
fun AppProgress(
    done: Int,
    total: Int,
    label: (done: Int, total: Int) -> String,
    doneLabel: (total: Int) -> String,
    modifier: Modifier = Modifier,
    celebrate: Boolean = true,
    compact: Boolean = false,
    doneDescription: ((total: Int) -> String)? = null
) {
    val count = done.coerceIn(0, total.coerceAtLeast(0))
    val complete = celebrate && total > 0 && count == total
    val fraction = if (total > 0) count.toFloat() / total else 0f
    val motion = MaterialTheme.motionScheme
    val animated by animateFloatAsState(fraction, motion.slowSpatialSpec(), label = "framsteg")
    val colors = MaterialTheme.colorScheme
    val extended = AppTheme.extendedColors
    val doneColors = extended.progressDone
    val fill by animateColorAsState(
        if (complete) doneColors?.fill ?: colors.primary else colors.primary,
        motion.defaultEffectsSpec(),
        label = "framstegsfärg"
    )
    val outline = if (complete) doneColors?.outline else null
    val text = if (complete) doneLabel(total) else label(count, total)
    val semantics = Modifier.clearAndSetSemantics {
        contentDescription = if (complete && doneDescription != null) doneDescription(total) else text
        progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f)
    }
    val track = extended.track ?: colors.primaryContainer
    if (compact) {
        Row(
            modifier.then(semantics),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.s)
        ) {
            Track(animated, track, fill, outline, Modifier.width(COMPACT_WIDTH))
            Text("$count / $total", style = AppTheme.typography.pill, color = colors.onSurfaceVariant)
        }
        return
    }
    Column(modifier.fillMaxWidth().then(semantics), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text(text, style = AppTheme.typography.itemSubtitle, color = if (complete) colors.onSurface else colors.onSurfaceVariant)
        Track(animated, track, fill, outline, Modifier.fillMaxWidth())
    }
}

/** Spåret och fyllnaden; fjädern får slå över lite (Expressive), men fyllnaden stannar inom spåret. */
@Composable
private fun Track(fraction: Float, track: Color, fill: Color, outline: Color?, modifier: Modifier = Modifier) {
    val shape = AppTheme.shapes.pill
    Box(modifier.height(TRACK_HEIGHT).clip(shape).background(track)) {
        val border = if (outline != null) Modifier.border(OUTLINE_WIDTH, outline, shape) else Modifier
        Box(
            Modifier
                .testTag(PROGRESS_FILL_TAG)
                .fillMaxHeight()
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .clip(shape)
                .background(fill)
                .then(border)
        )
    }
}

/** Fyllnaden i [AppProgress], för tester. */
internal const val PROGRESS_FILL_TAG = "progress-fill"

private val TRACK_HEIGHT = 8.dp
private val COMPACT_WIDTH = 120.dp
private val OUTLINE_WIDTH = 1.dp
