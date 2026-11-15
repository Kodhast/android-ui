package se.kodhast.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.Spacing
import se.kodhast.ui.theme.Tone

/**
 * Sektionsrubrik med valfri ikonruta och räknare ("4 / 5") i sektionens [tone]. [countTone] färgar
 * bara räknaren – en statuspill ("Krävs", "Kontrollerad") bredvid en ikonruta i sektionens ton;
 * standard samma ton som sektionen. Med [working] visar rubriken en liten laddningsindikator efter
 * titeln medan sektionen hämtar data (Fonders kort); TalkBack läser [workingDescription], standard
 * "Uppdaterar <titel>". Med `AppBrandStyle.sectionDots` ([LocalAppBrandStyle]) står Kodhästs två prickar
 * i `primary` före titeln när rubriken saknar ikonruta; titeln ändras inte.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    count: String? = null,
    tone: Tone = Tone.Primary,
    countTone: Tone = tone,
    working: Boolean = false,
    workingDescription: String? = null
) {
    val colors = AppTheme.tone(tone)
    Row(
        modifier.fillMaxWidth().padding(horizontal = Spacing.xs, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s)
    ) {
        if (icon != null) {
            Box(Modifier.size(TILE).background(colors.container, AppTheme.shapes.smallTile), contentAlignment = Alignment.Center) {
                Icon(painterResource(icon), contentDescription = null, tint = colors.content, modifier = Modifier.size(ICON))
            }
        } else if (LocalAppBrandStyle.current.sectionDots) {
            BrandDots(MaterialTheme.colorScheme.primary, Modifier.size(DOTS_WIDTH, DOTS_HEIGHT).testTag(SECTION_DOTS_TAG))
        }
        Text(
            title,
            style = AppTheme.typography.sectionTitle,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f).semantics { heading() }
        )
        if (working) {
            val description = workingDescription ?: stringResource(R.string.section_working, title)
            InlineLoading(description, colors.content, Modifier.size(WORKING))
        }
        count?.let { InfoPill(it, tone = countTone) }
    }
}

private val TILE = 32.dp
private val ICON = 18.dp
private val WORKING = 24.dp
private val DOTS_WIDTH = 18.dp
private val DOTS_HEIGHT = 6.dp

/** Prickarna före titeln i [SectionHeader] (`AppBrandStyle.sectionDots`), för tester. */
internal const val SECTION_DOTS_TAG = "section-dots"
