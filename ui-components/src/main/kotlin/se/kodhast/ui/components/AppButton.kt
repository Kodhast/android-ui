package se.kodhast.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.IconSize
import se.kodhast.ui.theme.PROGRESS_STROKE
import se.kodhast.ui.theme.Spacing
import se.kodhast.ui.theme.Tone

enum class ButtonVariant {
    Primary,
    Secondary,

    /** Konturerad: ett jämbördigt alternativ bredvid en fylld knapp (Fonder). */
    Outlined,
    Text,

    /** Permanent radering – bara i en `ConfirmDialog` med `destructive = true`. */
    Destructive
}

/**
 * Appens knappstil, som skiljer sig mellan apparna – sätts en gång runt appens innehåll med
 * [LocalAppButtonStyle], så att även ramarnas och dialogernas knappar följer den. Standardvärdena är
 * ReseApotekets och Dagbokens knappar.
 *
 * - [minHeight]: knappens höjd (Fonder: Material 3:s 40 dp). `compact` går före.
 * - [morph]: Expressive-formmorf – rund i vila, fyrkantigare nedtryckt (`ButtonDefaults.shapes()`)
 *   i stället för den fasta pillformen.
 * - [secondaryTone]: färgerna för [ButtonVariant.Secondary]; `null` = Material 3:s tonade
 *   (`secondaryContainer`). Fonder: `Tone.Primary` (primärbehållaren).
 */
@Immutable
data class AppButtonStyle(
    val minHeight: Dp = AppButtonDefaults.MinHeight,
    val morph: Boolean = false,
    val secondaryTone: Tone? = null
)

/** Standardvärden för [AppButton]. */
object AppButtonDefaults {
    /** Tryckyta och höjd enligt mockupen – stor nog för tummen. */
    val MinHeight: Dp = 56.dp

    /** Den lägre knappen för rubrikraden (`compact`). */
    val CompactHeight: Dp = 40.dp
}

/** Knappstilen för [AppButton] i innehållet; standard [AppButtonStyle] (ReseApoteket, Dagboken). */
val LocalAppButtonStyle = staticCompositionLocalOf { AppButtonStyle() }

/**
 * Den enda knappen (skill shared-ui-components). [loading] visar en snurra i stället för
 * ikonen och gör knappen inaktiv – samma "arbetar"-läge överallt. [compact] ger en lägre knapp
 * för rubrikraden (t.ex. "Spara" i `EntityEditScreen`); tryckytan är ändå minst 48 dp.
 * [contentDescription] läses av TalkBack i stället för etiketten – för en knapp som upprepas med samma
 * korta etikett ("Följ") och ska säga vad den gäller; `null` läser etiketten. [style] är appens
 * knappstil (höjd, formmorf, tonad färg), standard [LocalAppButtonStyle]. Med temats djup (`AppTheme.depth`)
 * krymper knappen nedtryckt, och de fyllda och tonade får skugga, skimmer och ljuskant som sjunker bort när
 * knappen trycks – inte när den är inaktiv eller arbetar.
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    @DrawableRes icon: Int? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    compact: Boolean = false,
    contentDescription: String? = null,
    style: AppButtonStyle = LocalAppButtonStyle.current
) = ButtonBase(text, onClick, variant, icon, enabled, loading, compact, contentDescription, style, modifier)

@Composable
private fun ButtonBase(
    text: String,
    onClick: () -> Unit,
    variant: ButtonVariant,
    @DrawableRes icon: Int?,
    enabled: Boolean,
    loading: Boolean,
    compact: Boolean,
    contentDescription: String?,
    style: AppButtonStyle,
    modifier: Modifier = Modifier
) {
    val described = contentDescription?.let { modifier.semantics { this.contentDescription = it } } ?: modifier
    val minHeight = if (compact) AppButtonDefaults.CompactHeight else style.minHeight
    val active = enabled && !loading
    val content: @Composable RowScope.() -> Unit = { ButtonContent(text, icon, loading) }
    // Varianterna är Material 3:s knappar med sina standardvärden – samma som Button, FilledTonalButton,
    // OutlinedButton och TextButton skickar vidare till Button.
    val colors = when (variant) {
        ButtonVariant.Primary -> ButtonDefaults.buttonColors()

        ButtonVariant.Secondary -> secondaryButtonColors(style)

        ButtonVariant.Outlined -> ButtonDefaults.outlinedButtonColors()

        ButtonVariant.Text -> ButtonDefaults.textButtonColors()

        ButtonVariant.Destructive -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        )
    }.busy(loading)
    val elevation = when (variant) {
        ButtonVariant.Primary, ButtonVariant.Destructive -> ButtonDefaults.buttonElevation()
        ButtonVariant.Secondary -> ButtonDefaults.filledTonalButtonElevation()
        ButtonVariant.Outlined, ButtonVariant.Text -> null
    }
    val border = if (variant == ButtonVariant.Outlined) ButtonDefaults.outlinedButtonBorder(active) else null
    val interactionSource = remember { MutableInteractionSource() }
    val depth = AppTheme.depth
    val button: @Composable (Modifier) -> Unit = { sized ->
        if (style.morph) {
            // Formmorfens knappar har Expressive-luften efter höjden – samma som Material 3:s standard där.
            Button(onClick, ButtonDefaults.shapes(), sized, active, colors, elevation, border, ButtonDefaults.contentPaddingFor(ButtonDefaults.MinHeight), interactionSource, content)
        } else {
            val padding = if (variant == ButtonVariant.Text) ButtonDefaults.TextButtonContentPadding else ButtonDefaults.ContentPadding
            Button(onClick, sized, active, AppTheme.shapes.pill, colors, elevation, border, padding, interactionSource, content)
        }
    }
    if (depth == null) {
        button(described.heightIn(min = minHeight))
    } else {
        val lift = rememberLift(interactionSource, active)
        val liftValue = { lift.value }
        val strength = if (variant == ButtonVariant.Secondary) SECONDARY_SHADOW else 1f
        val decorated = Modifier
            .depthSurface(depth, AppTheme.shapes.pill, liftValue, raised = active && variant in RAISED, strength = strength)
            .heightIn(min = minHeight)
        val description = contentDescription?.let { Modifier.semantics { this.contentDescription = it } } ?: Modifier
        DepthTouchTarget(modifier) { target -> button(target.then(description).then(decorated)) }
    }
}

/** De fyllda knapparna, som får skugga, skimmer och ljuskant med djupet; konturerade och textknappar bara krymper. */
private val RAISED = setOf(ButtonVariant.Primary, ButtonVariant.Secondary, ButtonVariant.Destructive)

/** En tonad knapp lyfter hälften så mycket som en fylld – den är ett andrahandsval. */
internal const val SECONDARY_SHADOW = 0.5f

/** Färgerna för [ButtonVariant.Secondary] i knappstilen [style] – också [AddSplitButton]:s. */
@Composable
internal fun secondaryButtonColors(style: AppButtonStyle): ButtonColors = style.secondaryTone?.let { AppTheme.tone(it) }
    ?.let { ButtonDefaults.filledTonalButtonColors(containerColor = it.container, contentColor = it.content) }
    ?: ButtonDefaults.filledTonalButtonColors()

/** En knapp som arbetar är inaktiv men behåller sina färger – den ser upptagen ut, inte avstängd. */
private fun ButtonColors.busy(loading: Boolean) = if (loading) copy(disabledContainerColor = containerColor, disabledContentColor = contentColor) else this

@Composable
private fun ButtonContent(text: String, @DrawableRes icon: Int?, loading: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        when {
            loading -> CircularProgressIndicator(
                modifier = Modifier.size(IconSize.button),
                color = LocalContentColor.current,
                strokeWidth = PROGRESS_STROKE
            )

            icon != null -> Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(IconSize.button))
        }
        if (loading || icon != null) Spacer(Modifier.width(Spacing.s))
        Text(text, style = AppTheme.typography.button)
    }
}
