package se.kodhast.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.Spacing

/**
 * Blockerande väntan på något användaren just startat och måste invänta (t.ex. en import som läser en fil):
 * laddningsindikatorn bredvid [title] och [message] i en dialog som inte går att stänga, varken med bakåt
 * eller tryck utanför. Visa den bara medan arbetet pågår; bakgrundsarbete som inte blockerar visas i
 * stället med sidhuvudets prickar (`LocalTopBarSyncing`).
 *
 * Indikatorn är [AppLoading]s (Kodhästs prickar med `AppBrandStyle.dotLoading`). Dialogen har kortens färg
 * och form, med temats djup kortens kant. TalkBack läser texten när dialogen visas.
 */
@Composable
fun LoadingDialog(title: String, message: String, modifier: Modifier = Modifier) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        val shape = AppTheme.shapes.card
        Row(
            modifier
                .testTag(LOADING_DIALOG_TAG)
                .floating(shape, shadow = false)
                .background(AppTheme.extendedColors.card, shape)
                .padding(Spacing.xl)
                .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.l)
        ) {
            InlineLoading(stringResource(R.string.loading), MaterialTheme.colorScheme.primary, Modifier.size(INDICATOR_SIZE))
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(title, style = AppTheme.typography.itemTitle, color = MaterialTheme.colorScheme.onSurface)
                Text(message, style = AppTheme.typography.body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private val INDICATOR_SIZE = 40.dp

/** Dialogens yta i [LoadingDialog], för tester. */
internal const val LOADING_DIALOG_TAG = "loading-dialog"
