package se.kodhast.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import se.kodhast.ui.common.AppUpdate

/**
 * Om-kortet: [appName] med "Version [version]" under och Kodhästs signatur ([MakerMark]) under
 * texten. Finns en nyare version ([update], standard [LocalAppUpdate]) står "Ny version X finns" efter
 * versionen med knappen **Hämta**, som öppnar APK:n i webbläsaren (#125); [onDownload] ersätter det (tester).
 * [extra] är appens egna rader i samma kort efter versionen (t.ex. läkemedelslistans datum,
 * källkoden) – var och en med en [AppDivider] före, som i resten av inställningarna.
 */
@Composable
fun AboutCard(
    appName: String,
    version: String,
    modifier: Modifier = Modifier,
    update: AppUpdate? = LocalAppUpdate.current?.update,
    onDownload: ((AppUpdate) -> Unit)? = null,
    extra: @Composable ColumnScope.() -> Unit = {}
) {
    val uriHandler = LocalUriHandler.current
    AppCard(modifier) {
        ItemRow(appName, subtitle = stringResource(R.string.version_format, version), below = { MakerMark() })
        update?.let {
            AppDivider()
            ItemRow(
                stringResource(R.string.update_available_format, it.version),
                modifier = Modifier.testTag(ABOUT_UPDATE_TAG),
                trailing = {
                    AppButton(
                        stringResource(R.string.update_download),
                        onClick = { if (onDownload != null) onDownload(it) else uriHandler.tryOpenUri(it.downloadUrl) },
                        compact = true,
                        icon = R.drawable.ic_download
                    )
                }
            )
        }
        extra()
    }
}

/** Raden "Ny version X finns" i [AboutCard] (#125). */
const val ABOUT_UPDATE_TAG = "about-update"
