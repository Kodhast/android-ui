package se.kodhast.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.Spacing
import se.kodhast.ui.theme.Tone

/**
 * Ett beräknat värde i en tonad ruta: [label] ("För en 7-dagarsresa"), [value] i stor stil ("18 tabletter · 2 kartor")
 * och valfri [supporting] (förklaringen). Läses som en enhet, och läses upp igen när värdet ändras – t.ex. förslaget
 * som räknas om medan ett formulär fylls i. [tone] är rutans ton (standard `Tone.Primary`).
 */
@Composable
fun ValueTile(label: String, value: String, modifier: Modifier = Modifier, supporting: String? = null, tone: Tone = Tone.Primary) {
    val colors = AppTheme.tone(tone)
    Column(
        modifier
            .fillMaxWidth()
            .background(colors.container, AppTheme.shapes.row)
            .padding(horizontal = Spacing.l, vertical = Spacing.m)
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        verticalArrangement = Arrangement.spacedBy(LINE_GAP)
    ) {
        Text(label, style = AppTheme.typography.pill, color = colors.content)
        Text(value, style = AppTheme.typography.quantity, color = colors.content)
        supporting?.let { Text(it, style = AppTheme.typography.itemSubtitle, color = colors.content) }
    }
}

private val LINE_GAP = 2.dp
