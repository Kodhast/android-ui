package se.kodhast.ui.navigation.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import se.kodhast.ui.components.AppTopBar
import se.kodhast.ui.components.TopBarSize
import se.kodhast.ui.navigation.R
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.Spacing

/** En sektion i [ComponentGallery]: rubriken och komponenterna under den. */
data class GallerySection(val title: String, val content: @Composable () -> Unit)

/**
 * Alla komponenter på en skärm, i appens tema och med påhittad data: först bibliotekets sektioner
 * (varje publik komponent i `ui-components` och `ui-navigation`, i sina viktigaste lägen), sedan
 * appens egna ([appSections], t.ex. det som hör till appens domän). Appen visar galleriet bara i
 * debug-bygget. Bibliotekets `UiConsistencyTest` kontrollerar att varje publik komponent finns här.
 */
@Composable
fun ComponentGallery(onBack: () -> Unit, modifier: Modifier = Modifier, appSections: List<GallerySection> = emptyList()) {
    // Innehållsfärgen som appens Scaffold ger en skärm – galleriet kan visas utan, och då vore text och ikoner svarta i mörkt läge.
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) { Gallery(onBack, appSections, modifier) }
}

@Composable
private fun Gallery(onBack: () -> Unit, appSections: List<GallerySection>, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        AppTopBar(stringResource(R.string.gallery_title), size = TopBarSize.Small, onBack = onBack)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = Spacing.l, vertical = Spacing.s),
            verticalArrangement = Arrangement.spacedBy(Spacing.l)
        ) {
            (librarySections() + appSections).forEach { Section(it) }
        }
    }
}

@Composable
private fun Section(section: GallerySection) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        Text(section.title, style = AppTheme.typography.headline, modifier = Modifier.semantics { heading() })
        section.content()
    }
}
