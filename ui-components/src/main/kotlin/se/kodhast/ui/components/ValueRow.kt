package se.kodhast.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.Spacing
import se.kodhast.ui.theme.Tone

/**
 * Etikett till vänster och värde till höger, t.ex. "Totalresultat … +8,6 % · +32 860 kr" eller "Årlig avgift …
 * Hämtar …". Värdet skrivs med tabulära siffror, så att tal i rader under varandra står i linje. Raden läses
 * som en enhet av TalkBack.
 *
 * - [supportingLabel]: en dämpad rad under etiketten – vad värdet bygger på ("0,888 % på kapitalunderlaget").
 * - [supportingValue]: en dämpad rad under värdet (t.ex. kronorna under en procent); flera rader med `\n`.
 * - [valueColor] färgar värdet (appens vinst-/förlustfärg); [valueTone] tar färgen ur temats toner i stället.
 *   [valueColor] går före [valueTone]; båda `null` = `onSurface`. Delar av värdet kan ha egna färger i ett
 *   [AnnotatedString].
 * - [muted]: hela raden dämpad – en underordnad siffra som inte ska konkurrera med huvudtalet.
 * - [valueMuted]: bara värdet dämpat – ett ungefärligt belopp, eller en text i stället för ett värde ("Visas
 *   efter ett år", "Hämtar …"). Båda går före färgerna.
 *
 * En lång etikett bryts på högst två rader och klipps sedan, så att värdet alltid får plats.
 */
@Composable
fun ValueRow(
    label: String,
    value: AnnotatedString,
    modifier: Modifier = Modifier,
    supportingLabel: String? = null,
    supportingValue: String? = null,
    valueColor: Color? = null,
    valueTone: Tone? = null,
    muted: Boolean = false,
    valueMuted: Boolean = false
) {
    val colors = MaterialTheme.colorScheme
    val typography = AppTheme.typography
    val dimmed = colors.onSurfaceVariant
    val shownValueColor = when {
        muted || valueMuted -> dimmed
        valueColor != null -> valueColor
        valueTone != null -> AppTheme.tone(valueTone).content
        else -> colors.onSurface
    }
    Row(
        modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f, fill = false).padding(end = Spacing.s)) {
            Text(label, style = typography.body, color = if (muted) dimmed else colors.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
            supportingLabel?.let { Text(it, style = typography.itemSubtitle, color = dimmed) }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(value, style = typography.body.tabular(), color = shownValueColor, textAlign = TextAlign.End)
            supportingValue?.let { Text(it, style = typography.itemSubtitle.tabular(), color = dimmed, textAlign = TextAlign.End) }
        }
    }
}

/** [ValueRow] med ett värde i en färg. */
@Composable
fun ValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    supportingLabel: String? = null,
    supportingValue: String? = null,
    valueColor: Color? = null,
    valueTone: Tone? = null,
    muted: Boolean = false,
    valueMuted: Boolean = false
) = ValueRow(label, AnnotatedString(value), modifier, supportingLabel, supportingValue, valueColor, valueTone, muted, valueMuted)

/** Tabulära siffror (lika breda), så att tal i rader under varandra står i linje. Typsnittet är temats. */
internal fun TextStyle.tabular(): TextStyle = copy(fontFeatureSettings = listOfNotNull(fontFeatureSettings, TABULAR).joinToString(", "))

private const val TABULAR = "tnum"
