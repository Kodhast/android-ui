package se.kodhast.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import se.kodhast.ui.theme.Spacing

/**
 * Några [InfoPill] tätt bredvid varandra, t.ex. etiketterna sist i en listrad ("Ändrad", "08:00"). Bryts inte och
 * rullar inte – för fler eller valbara chips, se [ChipRow].
 */
@Composable
fun PillRow(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(Spacing.xs), verticalAlignment = Alignment.CenterVertically) { content() }
}
