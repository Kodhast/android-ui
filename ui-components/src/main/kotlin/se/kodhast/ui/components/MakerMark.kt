package se.kodhast.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.Spacing

/**
 * Kodhästs signatur: [BrandMark] och [text] (standard "En kodhäst-app") på en rad, i Inställningar
 * eller en sidfot. Texten i appens `caption` och `onSurfaceVariant`; en nod som läses som texten.
 */
@Composable
fun MakerMark(modifier: Modifier = Modifier, text: String = stringResource(R.string.maker_mark)) {
    Row(
        modifier.semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s)
    ) {
        BrandMark(Modifier.size(MARK))
        Text(text, style = AppTheme.typography.caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private val MARK = 20.dp
