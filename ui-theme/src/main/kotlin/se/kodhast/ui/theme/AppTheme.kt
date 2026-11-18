package se.kodhast.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Temat som komponenterna läser: Material 3 Expressive med appens [colorScheme], [typeScale] och
 * [motionScheme], plus appens [extendedColors] och namngivna [typography]. Appen väljer ljust eller
 * mörkt och skickar in rätt färger; biblioteket har inga egna färger eller typsnitt. Formerna ([shapes])
 * är bibliotekets och samma i alla appar – ange inga egna (bara Fonder har kvar sina, tills vidare).
 * [depth] ger knappar, kort och valda element djup och tryckkänsla ([AppDepth]); standard `null` = platt.
 */
@Composable
fun AppTheme(
    colorScheme: ColorScheme,
    extendedColors: ExtendedColors,
    typeScale: Typography,
    shapes: AppShapes = DefaultShapes,
    typography: AppTypography = remember(typeScale) { AppTypography.from(typeScale) },
    motionScheme: MotionScheme = MotionScheme.expressive(),
    depth: AppDepth? = null,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors,
        LocalAppShapes provides shapes,
        LocalAppTypography provides typography,
        LocalAppDepth provides depth
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            motionScheme = motionScheme,
            typography = typeScale,
            content = content
        )
    }
}

/** Det aktuella temat, för komponenter och feature-kod. */
object AppTheme {
    val extendedColors: ExtendedColors
        @Composable @ReadOnlyComposable
        get() = LocalExtendedColors.current

    val shapes: AppShapes
        @Composable @ReadOnlyComposable
        get() = LocalAppShapes.current

    val typography: AppTypography
        @Composable @ReadOnlyComposable
        get() = LocalAppTypography.current

    /** Ingen fyllnad: ytan släpper igenom det den ligger på (t.ex. ovalda segment). Ingen färg, därför inte appens. */
    val transparent: Color = Color.Transparent

    /** Djupet ([AppDepth]); `null` = platt. */
    val depth: AppDepth?
        @Composable @ReadOnlyComposable
        get() = LocalAppDepth.current

    /** Färgerna för en [Tone] i aktuellt tema. */
    @Composable
    @ReadOnlyComposable
    fun tone(tone: Tone): ToneColors = extendedColors.tone(tone)
}

private val DefaultShapes = AppShapes()

private fun missing(what: String): Nothing = error("$what saknas – lägg innehållet i AppTheme(…)")

internal val LocalExtendedColors = staticCompositionLocalOf<ExtendedColors> { missing("ExtendedColors") }
internal val LocalAppShapes = staticCompositionLocalOf<AppShapes> { missing("AppShapes") }
internal val LocalAppTypography = staticCompositionLocalOf<AppTypography> { missing("AppTypography") }
internal val LocalAppDepth = staticCompositionLocalOf<AppDepth?> { null }
