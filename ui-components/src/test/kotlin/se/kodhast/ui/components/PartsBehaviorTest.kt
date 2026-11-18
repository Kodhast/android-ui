package se.kodhast.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.components.testing.TestPalette
import se.kodhast.ui.components.testing.TestTheme
import se.kodhast.ui.components.testing.assertSameColor
import se.kodhast.ui.components.testing.pixelAt
import se.kodhast.ui.components.testing.textStyle

/** Byggstenarna som rader och fält sätts ihop av: avdelare, dagmarkering, fälttext, ledande ruta och krysset. */
@RunWith(RobolectricTestRunner::class)
class PartsBehaviorTest {

    @get:Rule
    val rule = createComposeRule()

    private fun show(content: @Composable () -> Unit) = rule.setContent { TestTheme(content = content) }

    @Test
    fun `AppDivider är en hårfin linje i temats linjefärg och läses inte`() {
        show { AppDivider(Modifier.testTag("linje")) }
        val line = rule.onNodeWithTag("linje").assertHeightIsEqualTo(1.dp)
        assertSameColor(TestPalette.light.outlineVariant, line.pixelAt(rule.density, 40.dp, 0.dp))
        // Bara testets tagg: linjen har inget att läsa upp.
        assertEquals(listOf(SemanticsProperties.TestTag.name), line.fetchSemanticsNode().config.map { it.key.name })
    }

    @Test
    fun `DayMarkerDot har samma storlek med och utan markering`() {
        var marked by mutableStateOf(false)
        var today by mutableStateOf(false)
        show { DayMarkerDot(isToday = today, marked = marked, selected = false, modifier = Modifier.testTag("dag")) }
        val dot = rule.onNodeWithTag("dag")
        val size = dot.fetchSemanticsNode().size
        val blank = dot.pixelAt(rule.density, 4.dp, 4.dp)
        marked = true
        rule.waitForIdle()
        assertEquals(size, dot.fetchSemanticsNode().size)
        assertSameColor(TestPalette.light.primary, dot.pixelAt(rule.density, 4.dp, 4.dp), "markerad dag i primärfärg")
        today = true
        rule.waitForIdle()
        assertEquals(size, dot.fetchSemanticsNode().size)
        assertSameColor(TestPalette.light.secondary, dot.pixelAt(rule.density, 4.dp, 4.dp), "dagens punkt i secondary")
        assertNotEquals(blank, dot.pixelAt(rule.density, 4.dp, 4.dp))
    }

    @Test
    fun `FieldError läses som fel i felfärgen`() {
        show { FieldError("Välj minst en dag") }
        rule.onNodeWithText("Välj minst en dag")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Error, "Välj minst en dag"))
        assertEquals(TestPalette.light.error, rule.onNodeWithText("Välj minst en dag").textStyle().color)
    }

    @Test
    fun `ItemRowText visar titel och undertext och stryker över när klart`() {
        show { ItemRowText("Alvedon 500 mg", "2 tabletter") }
        rule.onNodeWithText("Alvedon 500 mg").assertIsDisplayed()
        rule.onNodeWithText("2 tabletter").assertIsDisplayed()
        assertNotEquals(TextDecoration.LineThrough, rule.onNodeWithText("Alvedon 500 mg").textStyle().textDecoration)
    }

    @Test
    fun `ItemRowText klar stryker över titeln och utan titel visas bara undertexten`() {
        show {
            ItemRowText("Alvedon 500 mg", null, done = true)
            ItemRowText(null, "Bara undertext")
        }
        assertEquals(TextDecoration.LineThrough, rule.onNodeWithText("Alvedon 500 mg").textStyle().textDecoration)
        rule.onNodeWithText("Bara undertext").assertIsDisplayed()
    }

    @Test
    fun `LeadingSlot är en ruta på 48 dp oavsett innehåll`() {
        show { LeadingSlot(Modifier.testTag("ruta")) { Text("A") } }
        rule.onNodeWithTag("ruta").assertWidthIsEqualTo(48.dp).assertHeightIsEqualTo(48.dp)
        rule.onNodeWithText("A").assertIsDisplayed()
    }

    @Test
    fun `MorphingCheck fylls när den kryssas och har ingen egen semantik`() {
        var checked by mutableStateOf(false)
        show { LeadingSlot(Modifier.testTag("ruta")) { MorphingCheck(checked) } }
        val slot = rule.onNodeWithTag("ruta")
        val centre = { slot.pixelAt(rule.density, 24.dp, 24.dp) }
        val empty = centre()
        checked = true
        rule.waitForIdle()
        assertNotEquals(empty, centre(), "kakan fylls")
        assertEquals(0, rule.onNodeWithTag("ruta").fetchSemanticsNode().children.size)
    }
}
