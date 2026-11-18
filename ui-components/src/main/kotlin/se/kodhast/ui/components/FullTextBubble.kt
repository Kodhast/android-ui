@file:OptIn(ExperimentalMaterial3Api::class)

package se.kodhast.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Bubblan med hela texten vid långt tryck när en text kortats med "…" – en gång, för [ItemRow] och
 * [SectionHeader]. [enabled] = false (texten ryms) lämnar långtrycket åt det som ligger under.
 */
@Composable
internal fun FullTextBubble(
    enabled: Boolean,
    bubble: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = { PlainTooltip { bubble() } },
        state = rememberTooltipState(),
        modifier = modifier,
        enableUserInput = enabled,
        content = content
    )
}
