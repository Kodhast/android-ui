package se.kodhast.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
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
 *
 * Titeln får det som blir över bredvid räknaren och står på högst två rader, bruten mellan ord. Bara när
 * ett ord inte ryms på en rad kortas titeln med "…" (aldrig avbruten mitt i ordet), och då visar ett långt
 * tryck hela titeln i en bubbla ([FullTextBubble], som [ItemRow]). TalkBack läser alltid hela titeln.
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
        HeaderTitle(title, Modifier.weight(1f))
        if (working) {
            val description = workingDescription ?: stringResource(R.string.section_working, title)
            InlineLoading(description, colors.content, Modifier.size(WORKING))
        }
        count?.let { InfoPill(it, tone = countTone) }
    }
}

/** Titeln i [SectionHeader]: två rader mellan ord, annars en rad med "…" och bubblan. */
@Composable
private fun HeaderTitle(title: String, modifier: Modifier = Modifier) {
    val style = AppTheme.typography.sectionTitle
    val measurer = rememberTextMeasurer()
    var truncated by remember { mutableStateOf(false) }
    // Bubblans ruta mäter sig efter innehållet: bredden ges därför till en yttre ruta och fylls inuti.
    Box(modifier) {
        FullTextBubble(truncated, { Text(title, style = AppTheme.typography.itemTitle) }) {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val width = constraints.maxWidth
                val lines = remember(title, style, width) {
                    val layout = measurer.measure(title, style, maxLines = TITLE_MAX_LINES, constraints = Constraints(maxWidth = width))
                    if (layout.breaksMidWord(title)) 1 else TITLE_MAX_LINES
                }
                Text(
                    title,
                    style = style,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = lines,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { truncated = it.hasVisualOverflow },
                    modifier = Modifier.semantics { heading() }
                )
            }
        }
    }
}

/** Sant om någon radbrytning i [text] hamnar mitt i ett ord (ett ord bredare än raden). */
internal fun TextLayoutResult.breaksMidWord(text: String): Boolean = (0 until lineCount - 1).any { line ->
    val end = getLineEnd(line)
    end in 1 until text.length && !text[end - 1].isWhitespace() && text[end - 1] != '-' && !text[end].isWhitespace()
}

private const val TITLE_MAX_LINES = 2
private val TILE = 32.dp
private val ICON = 18.dp
private val WORKING = 24.dp
private val DOTS_WIDTH = 18.dp
private val DOTS_HEIGHT = 6.dp

/** Prickarna före titeln i [SectionHeader] (`AppBrandStyle.sectionDots`), för tester. */
internal const val SECTION_DOTS_TAG = "section-dots"
