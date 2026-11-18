package se.kodhast.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.Tone

/**
 * Ett kort med en rubrikrad: [AppCard] med [SectionHeader] ([title], [icon], [count], [countTone]) överst och
 * [content] under – samma form överallt där ett kort har en rubrik. Med [tone] blir kortet tonat (t.ex. grönt när
 * allt är klart) och rubriken i samma ton; standard ett vanligt kort med rubriken i primärtonen. [contentColor] som i
 * [AppCard].
 */
@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    count: String? = null,
    tone: Tone? = null,
    countTone: Tone = tone ?: Tone.Primary,
    contentColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    AppCard(modifier, tone = tone, contentColor = contentColor) {
        SectionHeader(title, icon = icon, count = count, tone = tone ?: Tone.Primary, countTone = countTone)
        content()
    }
}

/**
 * Ett nyckeltal: [value] stort ("6,8") och [label] under ("snittenergi"), båda i [tone]s innehållsfärg – t.ex. tre i
 * rad på ett tonat [SectionCard]. Etiketten står på högst två rader. Läses som text; slå ihop raden med
 * `semantics(mergeDescendants = true)` så att värde och etikett läses tillsammans.
 */
@Composable
fun KeyFigure(value: String, label: String, modifier: Modifier = Modifier, tone: Tone = Tone.Primary) {
    val colors = AppTheme.tone(tone)
    Column(modifier) {
        Text(value, style = AppTheme.typography.bigNumber, color = colors.content, maxLines = 1)
        Text(label, style = AppTheme.typography.itemSubtitle, color = colors.content, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}
