package se.kodhast.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.Spacing

/**
 * En andel av en helhet: [label] och [value] (appens formatering, t.ex. "52,0 %") ovanför samma spår som
 * [AppProgress], fyllt till [fraction] (0–1). För fördelningar – fondtyp, region, risknivå – i stället för
 * ett tårtdiagram.
 *
 * - [target]: en målandel (0–1), ritad som ett lodrätt streck i [targetColor] som sticker ut ovanför och under
 *   spåret, så att målet syns också när spåret är tomt eller fullt. Med [targetValue] blir texten
 *   "52,0 % · mål 45,0 %" och TalkBack läser "Nivå 4: 52,0 %, mål 45,0 %". Utan [targetValue] syns strecket men målet
 *   varken skrivs eller läses – skicka båda.
 * - [color]: fyllnaden; `null` = `primary`. En restpost ("Övrigt", "Okänd region") skickar en dämpad färg
 *   (t.ex. `outline`) så att den skiljer sig från de riktiga kategorierna.
 * - [targetColor]: målstrecket; `null` = `secondary`.
 *
 * Raden har `Spacing.xs` luft ovan och under, så att rader under varandra inte går ihop. Spårets färg och höjd är [AppProgress]s (`ExtendedColors.track`, `TRACK_HEIGHT`). Raden läses som en enhet,
 * med andelen som framsteg.
 */
@Composable
fun ShareBar(
    label: String,
    fraction: Float,
    value: String,
    modifier: Modifier = Modifier,
    target: Float? = null,
    targetValue: String? = null,
    color: Color? = null,
    targetColor: Color? = null
) {
    val colors = MaterialTheme.colorScheme
    val shown = targetValue?.let { stringResource(R.string.share_target_format, value, it) } ?: value
    val description = targetValue?.let { stringResource(R.string.share_target_description_format, label, value, it) }
        ?: stringResource(R.string.field_in_context_format, label, value)
    val share = fraction.coerceIn(0f, 1f)
    Column(
        modifier.fillMaxWidth().padding(vertical = Spacing.xs).clearAndSetSemantics {
            contentDescription = description
            progressBarRangeInfo = ProgressBarRangeInfo(share, 0f..1f)
        },
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                label,
                style = AppTheme.typography.body,
                color = colors.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false).padding(end = Spacing.s)
            )
            Text(shown, style = AppTheme.typography.body.tabular(), color = colors.onSurface)
        }
        val track = AppTheme.extendedColors.track ?: colors.primaryContainer
        val fill = color ?: colors.primary
        // Målstrecket ritas ovanpå spåret och sticker ut utanför det, utan att ta plats: med och utan mål står
        // raderna lika tätt.
        val mark = target?.let { goal ->
            val markColor = targetColor ?: colors.secondary
            Modifier.testTag(SHARE_TARGET_TAG).drawWithContent {
                drawContent()
                val width = TARGET_WIDTH.toPx()
                // Fyllnaden börjar vid startkanten; i höger-till-vänster speglas strecket som den.
                val start = (size.width - width) * goal.coerceIn(0f, 1f)
                val x = if (layoutDirection == LayoutDirection.Rtl) size.width - width - start else start
                val overhang = TARGET_OVERHANG.toPx()
                drawRect(markColor, Offset(x, -overhang), Size(width, size.height + 2 * overhang))
            }
        } ?: Modifier
        Track(share, track, fill, outline = null, Modifier.fillMaxWidth().then(mark))
    }
}

/** Målstrecket: så brett, och så långt utanför spåret upptill och nedtill. */
private val TARGET_WIDTH = 2.dp
private val TARGET_OVERHANG = 3.dp

/** Målstrecket i [ShareBar], för tester. */
internal const val SHARE_TARGET_TAG = "share-target"
