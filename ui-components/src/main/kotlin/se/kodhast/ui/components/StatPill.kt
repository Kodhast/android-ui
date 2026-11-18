package se.kodhast.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.Spacing
import se.kodhast.ui.theme.Tone

/**
 * Mätvärde: ikon, värde ("7 842", "—" när datapunkten saknas) och etikett ("Steg") i en tonad yta ([tone]).
 * Läses som en enhet. Med [onClick] är hela ytan en knapp med minst 48 dp och [onClickLabel] beskriver åtgärden
 * ("Begär åtkomst"); utan är den ren avläsning. [action] skriver åtgärden sist i pillen ("Ge åtkomst") och är då
 * också det TalkBack läser som åtgärd, om inte [onClickLabel] säger annat. [action] och [onClick] hör ihop, som i
 * [NoticeBanner]: en åtgärd kräver [onClick].
 *
 * [tone] saknar standard: tonen är appens (Dagboken `Tone.Neutral`).
 */
@Composable
fun StatPill(
    @DrawableRes icon: Int,
    value: String,
    label: String,
    tone: Tone,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    action: String? = null
) {
    require(action == null || onClick != null) { "StatPill: action kräver onClick" }
    val colors = AppTheme.tone(tone)
    val interaction = if (onClick == null) {
        Modifier.semantics(mergeDescendants = true) {}
    } else {
        Modifier.heightIn(min = TOUCH_TARGET).clickable(onClickLabel = onClickLabel ?: action, role = Role.Button, onClick = onClick)
    }
    Row(
        modifier.clip(AppTheme.shapes.row).background(colors.container).then(interaction).padding(horizontal = Spacing.l, vertical = Spacing.m),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = colors.content, modifier = Modifier.size(ICON))
        Column(if (action != null) Modifier.weight(1f) else Modifier) {
            Text(value, style = AppTheme.typography.quantity, color = colors.content, maxLines = 1)
            Text(label, style = AppTheme.typography.itemSubtitle, color = colors.content, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (action != null) Text(action, style = AppTheme.typography.button, color = colors.content, maxLines = 1)
    }
}

/** Radens ikonstorlek, som krysset i `MorphingCheck` och flikarna. */
private val ICON = 22.dp
