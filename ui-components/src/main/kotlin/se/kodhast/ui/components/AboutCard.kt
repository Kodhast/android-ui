package se.kodhast.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

/**
 * Om-kortet: [appName] med "Version [version]" under och Kodhästs signatur ([MakerMark]) under
 * texten. [extra] är appens egna rader i samma kort efter versionen (t.ex. läkemedelslistans datum,
 * källkoden) – var och en med en [AppDivider] före, som i resten av inställningarna.
 */
@Composable
fun AboutCard(appName: String, version: String, modifier: Modifier = Modifier, extra: @Composable ColumnScope.() -> Unit = {}) {
    AppCard(modifier) {
        ItemRow(appName, subtitle = stringResource(R.string.version_format, version), below = { MakerMark() })
        extra()
    }
}
