package se.kodhast.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier) {
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
 * Rubrikvarianten av [Foldout] för ett sektionskort med [SectionHeader]: ikonrutan, titeln och räknaren i [countTone]
 * står i titelraden, som växlar kortet, med chevronen sist. Stängt visas bara raden – räknaren talar om läget utan att man
 * fäller ut. [trailing] står mellan rubriken och chevronen, t.ex. en tonad lägg till-knapp
 * (`AppIconButton(R.drawable.ic_add, "Lägg till recept", onAdd, variant = IconButtonVariant.Tonal)`). Resten som i [Foldout].
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
    content: @Composable ColumnScope.() -> Unit
) {
    Foldout(
        title,
        expanded,
        onToggle,
        modifier,
        trailing = trailing,
        header = { SectionHeader(title, Modifier.weight(1f), icon = icon, count = count, tone = tone, countTone = countTone) },
        contentSpacing = Spacing.xs,
        content = content
    )
}

/** Det som fälls ut: fjädrar fram och fälls snabbt ihop – [Foldout] och appernas egna utfällbara detaljer (t.ex. ett postkort). */
@Composable
fun ExpandableContent(visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(MaterialTheme.motionScheme.defaultSpatialSpec()),
        exit = shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec())
    ) { content() }
}
