package se.kodhast.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.Spacing
import se.kodhast.ui.theme.Tone

/**
 * Hopfällbar sektion – i ett [AppCard] eller som hela innehållet i ett hopfällbart sektionskort. **Hela titelraden**
 * växlar läget ([CollapsibleHeader]: minst 48 dp, läses som knapp med "Fäll ut"/"Fäll ihop" och läget som
 * `stateDescription`; chevronen sist är en ren indikator). [trailing] står före chevronen – en kontroll som hör till
 * sektionen (en periodväljare, en lägg till-knapp); den har egen klickyta och växlar inte sektionen. [summary] står under
 * titeln i stängt läge – det valda värdet eller början av en text (högst två rader). [header] ersätter titeln helt
 * (som i [SectionFoldout]).
 *
 * Med [tintedWhenOpen] ligger hela den utfällda delen, rubrik och innehåll, på en tonad yta med radens hörn, så att
 * det syns vilken rad som är öppen i en lista av utfällbara rader (Husjakt #518): radtonen (`rowTint`) i ljust läge,
 * den starkare `surfaceVariant` i mörkt. Standard `false` = som förut, utan yta.
 */
@Composable
fun Foldout(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    summary: String? = null,
    header: (@Composable RowScope.() -> Unit)? = null,
    contentSpacing: Dp = Spacing.m,
    tintedWhenOpen: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(if (tintedWhenOpen) modifier.openTint(expanded) else modifier) {
        CollapsibleHeader(expanded, onToggle) {
            if (header != null) {
                header()
            } else {
                Column(Modifier.weight(1f).padding(vertical = Spacing.s)) {
                    Text(title, style = AppTheme.typography.itemTitle, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (!expanded && !summary.isNullOrBlank()) {
                        Text(summary, style = AppTheme.typography.itemSubtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            trailing?.invoke()
        }
        ExpandableContent(expanded) {
            Column(Modifier.padding(bottom = Spacing.s), verticalArrangement = Arrangement.spacedBy(contentSpacing), content = content)
        }
    }
}

/**
 * [Foldout] som håller läget själv: börjar i [initiallyExpanded] och sparar läget med `rememberSaveable`, så att det
 * överlever en rotation. [onExpand] körs när sektionen fälls **ut** (inte ihop) – kroken för innehåll som är dyrt att
 * hämta och inte ska laddas förrän någon vill se det (Fonders jämförelsediagram). Resten som i [Foldout].
 */
@Composable
fun Foldout(
    title: String,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
    onExpand: () -> Unit = {},
    trailing: (@Composable () -> Unit)? = null,
    summary: String? = null,
    header: (@Composable RowScope.() -> Unit)? = null,
    contentSpacing: Dp = Spacing.m,
    tintedWhenOpen: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    Foldout(
        title = title,
        expanded = expanded,
        onToggle = {
            expanded = !expanded
            if (expanded) onExpand()
        },
        modifier = modifier,
        trailing = trailing,
        summary = summary,
        header = header,
        contentSpacing = contentSpacing,
        tintedWhenOpen = tintedWhenOpen,
        content = content
    )
}

/** Den tonade ytan bakom en öppen [Foldout]; tonar in och ut med läget. Innehållet dras in lika mycket i båda lägena. */
@Composable
private fun Modifier.openTint(expanded: Boolean): Modifier {
    val colors = MaterialTheme.colorScheme
    val tint = if (colors.background.luminance() < DARK_LUMINANCE) colors.surfaceVariant else AppTheme.extendedColors.rowTint
    val color by animateColorAsState(if (expanded) tint else tint.copy(alpha = 0f), MaterialTheme.motionScheme.defaultEffectsSpec(), label = "utfälld yta")
    return background(color, AppTheme.shapes.row).padding(horizontal = Spacing.s)
}

/** Under den här ljusheten räknas temat som mörkt. */
private const val DARK_LUMINANCE = 0.5f

/**
 * Rubrikvarianten av [Foldout] för ett sektionskort med [SectionHeader]: ikonrutan, titeln och räknaren i [countTone]
 * står i titelraden, som växlar kortet, med chevronen sist. Stängt visas bara raden – räknaren talar om läget utan att man
 * fäller ut. [trailing] står mellan rubriken och chevronen, t.ex. en tonad lägg till-knapp
 * (`AppIconButton(R.drawable.ic_add, "Lägg till recept", onAdd, variant = IconButtonVariant.Tonal)`). [emptyText] står
 * först i det utfällda, som tomraden i en listgrupp – skicka den när sektionen saknar rader ("Inga recept än").
 * Resten som i [Foldout].
 */
@Composable
fun SectionFoldout(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    count: String? = null,
    tone: Tone = Tone.Primary,
    countTone: Tone = tone,
    trailing: (@Composable () -> Unit)? = null,
    emptyText: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Foldout(
        title,
        expanded,
        onToggle,
        modifier,
        trailing = trailing,
        header = { SectionHeader(title, Modifier.weight(1f), icon = icon, count = count, tone = tone, countTone = countTone) },
        contentSpacing = Spacing.xs
    ) {
        emptyText?.let { GroupEmpty(it) }
        content()
    }
}

/** Det som fälls ut: fjädrar fram och fälls snabbt ihop – [Foldout] och appernas egna utfällbara detaljer (t.ex. ett postkort). */
@Composable
fun ExpandableContent(visible: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = expandVertically(MaterialTheme.motionScheme.defaultSpatialSpec()),
        exit = shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec())
    ) { content() }
}
