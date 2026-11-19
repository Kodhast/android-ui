package se.kodhast.ui.components

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Test
import se.kodhast.ui.theme.AppDepth
import se.kodhast.ui.theme.AppShapes
import se.kodhast.ui.theme.IconSize
import se.kodhast.ui.theme.PROGRESS_STROKE
import se.kodhast.ui.theme.Spacing
import se.kodhast.ui.theme.TRACK_HEIGHT

/**
 * Håller `tokens.json` i roten aktuell. Filen är källan till talen för webbiblioteket (Kodhast/web-ui):
 * mått i dp (= CSS-px), skrivna ur temat och sidhuvudet. Faller testet är filen inaktuell – skriv om den med
 * `UPDATE_TOKENS=1 ./gradlew :ui-components:testDebugUnitTest --tests '*TokensJsonTest'` och checka in den.
 */
class TokensJsonTest {

    private val density = Density(1f)

    @Test
    fun tokensJsonÄrAktuell() {
        val file = File("../tokens.json")
        val expected = render()
        if (System.getenv("UPDATE_TOKENS") == "1") file.writeText(expected)
        assertTrue(file.exists(), "tokens.json saknas – skriv den med UPDATE_TOKENS=1 (se klassens kommentar)")
        assertEquals(expected, file.readText(), "tokens.json är inaktuell – skriv om den med UPDATE_TOKENS=1 (se klassens kommentar)")
    }

    private fun render(): String {
        val shapes = AppShapes()
        val sections = listOf(
            "spacing" to mapOf(
                "xs" to Spacing.xs,
                "s" to Spacing.s,
                "m" to Spacing.m,
                "l" to Spacing.l,
                "xl" to Spacing.xl,
                "xxl" to Spacing.xxl
            ).mapValues { num(it.value) },
            "iconSize" to mapOf(
                "marker" to IconSize.marker,
                "pill" to IconSize.pill,
                "small" to IconSize.small,
                "button" to IconSize.button,
                "row" to IconSize.row,
                "standard" to IconSize.standard
            ).mapValues { num(it.value) },
            "radius" to mapOf(
                "card" to num(shapes.cardRadius),
                "row" to radius(shapes.row),
                "pill" to radius(shapes.pill),
                "field" to radius(shapes.field),
                "menu" to radius(shapes.menu),
                "sheet" to radius(shapes.sheet),
                "smallTile" to radius(shapes.smallTile),
                "iconTile" to radius(shapes.iconTile),
                "fab" to radius(shapes.fab),
                "dialog" to radius(shapes.dialog)
            ),
            "track" to mapOf("height" to num(TRACK_HEIGHT), "progressStroke" to num(PROGRESS_STROKE)),
            "depth" to mapOf("pressedScale" to AppDepth(Color.Black, Color.Black, Color.Black, Color.Black, Color.Black).pressedScale.toString()),
            "header" to mapOf(
                "expanded" to num(EXPANDED),
                "collapsed" to num(COLLAPSED),
                "appIcon" to num(APP_ICON),
                "collapsedAppIcon" to num(COLLAPSED_APP_ICON)
            )
        )
        return sections.joinToString(prefix = "{\n", separator = ",\n", postfix = "\n}\n") { (name, values) ->
            "  \"$name\": {\n" + values.entries.joinToString(",\n") { "    \"${it.key}\": ${it.value}" } + "\n  }"
        }
    }

    private fun num(dp: Dp): String = dp.value.let { if (it % 1f == 0f) it.toInt().toString() else it.toString() }

    /** Hörnradien i dp; helt rundad (CircleShape) är "full", och ett rundat bara upptill är `[tl, tr, br, bl]`. */
    private fun radius(shape: Shape): String {
        val s = shape as CornerBasedShape
        val box = Size(1000f, 1000f)
        fun corner(c: CornerSize): String = if (c.toPx(box, density) >= 500f) "\"full\"" else c.toPx(box, density).let { if (it % 1f == 0f) it.toInt().toString() else it.toString() }
        val all = listOf(s.topStart, s.topEnd, s.bottomEnd, s.bottomStart).map(::corner)
        return if (all.toSet().size == 1) all.first() else all.joinToString(prefix = "[", postfix = "]")
    }
}
