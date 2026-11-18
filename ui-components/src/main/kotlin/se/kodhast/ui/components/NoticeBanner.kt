package se.kodhast.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.Spacing
import se.kodhast.ui.theme.Tone

/**
 * Ett meddelande överst på en skärm som leder vidare – t.ex. "Notiser är avstängda" eller Dagbokens
 * periodslut: ett [AppCard] i [tone] (standard primärtonen) med [icon], [text] och en pil. Hela kortet
 * är en knapp som läses med [onClickLabel] ("Öppna") och öppnar det meddelandet gäller ([onClick]);
 * ikonen och pilen är rena indikatorer. [detail] är en förklarande rad under [text]. Utan [onClick]
 * (`null`) är kortet bara ett meddelande – ingen pil, ingen knapp – för ett läge som inget i appen kan
 * ändra. Med [action] står åtgärden som en primär [AppButton] under texten i stället för pilen – då är
 * knappen åtgärden och kortet i sig ingen knapp. En åtgärd kräver [onClick].
 */
@Composable
fun NoticeBanner(
    text: String,
    @DrawableRes icon: Int,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    tone: Tone = Tone.Primary,
    onClickLabel: String = stringResource(R.string.open),
    detail: String? = null,
    action: String? = null
) {
    require(action == null || onClick != null) { "NoticeBanner: action kräver onClick" }
    val colors = AppTheme.tone(tone)
    // Hela kortet är knappen – utom när åtgärden är en egen knapp under texten.
    val cardClick = onClick.takeIf { action == null }
    val clickable = cardClick?.let {
        Modifier.clip(AppTheme.shapes.card).clickable(onClickLabel = onClickLabel, role = Role.Button, onClick = it)
    } ?: Modifier
    AppCard(modifier.then(clickable), tone = tone, contentColor = MaterialTheme.colorScheme.onSurface) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
            Icon(painterResource(icon), contentDescription = null, tint = colors.content)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(text, style = AppTheme.typography.itemTitle, color = colors.content)
                if (detail != null) Text(detail, style = AppTheme.typography.caption, color = colors.content)
                action?.let { AppButton(it, checkNotNull(onClick)) }
            }
            if (cardClick != null) Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = colors.content)
        }
    }
}
