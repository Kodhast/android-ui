package se.kodhast.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTouchHeightIsEqualTo
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.components.testing.SampleSwatches
import se.kodhast.ui.components.testing.TestTheme
import se.kodhast.ui.theme.AppDepth

/** Temats djup (`AppTheme.depth`): tryckytor och klick som förut, och att knappen krymper bara när den kan tryckas. */
@RunWith(RobolectricTestRunner::class)
class DepthBehaviorTest {

    @get:Rule
    val rule = createComposeRule()

    private fun show(depth: Boolean = true, content: @Composable () -> Unit) = rule.setContent { TestTheme(depth = depth, content = content) }

    /** Bredden på [tag] medan den hålls nedtryckt, delad med bredden i vila. */
    // Bredden nedtryckt delat med bredden i vila – mätt på det klickbara elementet, inte på tryckytan runt det.
    private fun pressedWidthRatio(node: SemanticsNodeInteraction = rule.onNode(hasClickAction())): Float {
        rule.mainClock.autoAdvance = false
        val rest = node.fetchSemanticsNode().boundsInRoot.width
        node.performTouchInput { down(center) }
        rule.mainClock.advanceTimeBy(SETTLE_MILLIS)
        return node.fetchSemanticsNode().boundsInRoot.width / rest
    }

    @Test
    fun `knappen med djup krymper nedtryckt`() {
        show { AppButton("Spara", {}) }
        val ratio = pressedWidthRatio()
        assertTrue(ratio < 0.99f, "nedtryckt/vila = $ratio")
    }

    @Test
    fun `knappen utan djup krymper inte`() {
        show(depth = false) { AppButton("Spara", {}) }
        assertEquals(1f, pressedWidthRatio())
    }

    @Test
    fun `en inaktiv knapp krymper inte med djup`() {
        show { AppButton("Spara", {}, enabled = false) }
        assertEquals(1f, pressedWidthRatio())
    }

    @Test
    fun `knappen med djup anropar onClick och har 48 dp tryckyta även kompakt`() {
        var clicks = 0
        show { AppButton("Spara", { clicks++ }, compact = true) }
        rule.onNodeWithText("Spara").assertTouchHeightIsEqualTo(48.dp).performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun `knappar, chips och segment med djup tar 48 dp plats i layouten`() {
        show {
            Column {
                // Rutan runt varje element mäter platsen i layouten; elementets egen nod har sitt eget mått.
                Box(Modifier.testTag("knapp")) { AppButton("Spara", {}, compact = true) }
                Box(Modifier.testTag("chip")) { AppFilterChip("Tablett", false, {}) }
                Box(Modifier.testTag("segment")) { AppSegmentedChoice(listOf("Ljust", "Mörkt"), 0, {}) }
            }
        }
        listOf("knapp", "chip", "segment").forEach { rule.onNodeWithTag(it, useUnmergedTree = true).assertHeightIsAtLeast(48.dp) }
    }

    @Test
    fun `anroparens semantik hamnar på det klickbara elementet med djup`() {
        // Dagboken sätter stateDescription på ett chip via modifier; med djup hamnade den på en ruta utanför chipet.
        fun state(text: String) = SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, text)
        show {
            Column {
                AppFilterChip("Imigran", selected = false, onClick = {}, modifier = Modifier.semantics { stateDescription = "Bara idag" })
                AppButton("Spara", {}, modifier = Modifier.testTag("knapp"))
            }
        }
        rule.onNodeWithText("Imigran").assert(state("Bara idag")).assert(hasClickAction())
        rule.onNodeWithTag("knapp").assert(hasClickAction())
    }

    @Test
    fun `chipet med djup väljs och har 48 dp tryckyta`() {
        var selected by mutableStateOf(false)
        show { AppFilterChip("Tablett", selected, { selected = !selected }) }
        rule.onNodeWithText("Tablett").assertTouchHeightIsEqualTo(48.dp).performClick()
        rule.onNodeWithText("Tablett").assertIsSelected()
    }

    @Test
    fun `segmentvalet med djup byter val`() {
        var segment by mutableIntStateOf(0)
        show { AppSegmentedChoice(listOf("Ljust", "Mörkt", "System"), segment, { segment = it }) }
        rule.onNodeWithText("Mörkt").performClick()
        assertEquals(1, segment)
    }

    @Test
    fun `det klickbara kortet med djup anropar onClick och krymper nedtryckt`() {
        var clicks = 0
        show { AppCard(Modifier.testTag("kort"), onClick = { clicks++ }) { ItemRow("Resa till Kreta") } }
        rule.onNodeWithTag("kort").performClick()
        assertEquals(1, clicks)
        assertTrue(pressedWidthRatio(rule.onNodeWithTag("kort")) < 1f)
    }

    @Test
    fun `det klickbara kortet med skimmer anropar onClick`() {
        var clicks = 0
        rule.setContent {
            TestTheme(depth = true, cardSheen = true) { AppCard(Modifier.testTag("kort"), onClick = { clicks++ }) { ItemRow("Resa till Kreta") } }
        }
        rule.onNodeWithTag("kort").performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun `kortens skimmer är av som standard`() {
        assertNull(AppDepth(Color.White, Color.White, Color.Black, Color.Black, Color.Black).cardSheen)
    }

    @Test
    fun `ikonknappen med djup anropar onClick`() {
        var clicks = 0
        show { AppIconButton(R.drawable.ic_arrow_back, "Tillbaka", { clicks++ }, variant = IconButtonVariant.Tonal) }
        rule.onNodeWithContentDescription("Tillbaka").assertTouchHeightIsEqualTo(48.dp).performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun `stegarens knappar med djup ändrar värdet och krymper nedtryckta`() {
        var value by mutableIntStateOf(2)
        show { QuantityStepper(value, { value = it }, "doser per dag") }
        rule.onNodeWithContentDescription("Öka doser per dag").performClick()
        assertEquals(3, value)
        assertTrue(pressedWidthRatio(rule.onNodeWithContentDescription("Minska doser per dag")) < 0.95f)
    }

    @Test
    fun `stegarens inaktiva knapp krymper inte med djup`() {
        show { QuantityStepper(0, {}, "doser per dag") }
        assertEquals(1f, pressedWidthRatio(rule.onNodeWithContentDescription("Minska doser per dag")))
    }

    @Test
    fun `växeln med djup slås om och har 48 dp tryckyta`() {
        var on by mutableStateOf(false)
        show { Box(Modifier.testTag("växel")) { SwitchControl(on, { on = it }, label = "Påminn") } }
        rule.onNodeWithContentDescription("Påminn").assertTouchHeightIsEqualTo(48.dp).performClick()
        assertTrue(on)
        rule.onNodeWithTag("växel", useUnmergedTree = true).assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun `raden med växel och djup slås om`() {
        var on by mutableStateOf(true)
        show { SwitchRow("Påminn kvällen innan", on, { on = it }) }
        rule.onNodeWithText("Påminn kvällen innan").performClick()
        assertEquals(false, on)
    }

    @Test
    fun `fältet med djup tar emot text och visar fel`() {
        var text by mutableStateOf("")
        show { AppTextField(text, { text = it }, "Dos", error = "Fyll i dosen".takeIf { text.isEmpty() }) }
        rule.onNodeWithText("Dos").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Error))
        rule.onNodeWithText("Dos").performTextInput("2")
        assertEquals("2", text)
        rule.onNodeWithText("Dos").assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Error))
    }

    @Test
    fun `färgväljaren med djup byter val`() {
        var selected by mutableStateOf(SampleSwatches.hexes.first())
        show { ColorSwatchPicker(SampleSwatches.hexes, selected, { selected = it }, SampleSwatches::color, { SampleSwatches.label(it) }, SwatchMarker.GridRing) }
        val second = SampleSwatches.hexes[1]
        rule.onNodeWithContentDescription(SampleSwatches.label(second)).performClick()
        assertEquals(second, selected)
    }

    private companion object {
        const val SETTLE_MILLIS = 1_000L
    }
}
