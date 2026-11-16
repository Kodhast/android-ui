package se.kodhast.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTouchHeightIsEqualTo
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.components.testing.TestTheme

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
                AppButton("Spara", {}, Modifier.testTag("knapp"), compact = true)
                AppFilterChip("Tablett", false, {}, Modifier.testTag("chip"))
                AppSegmentedChoice(listOf("Ljust", "Mörkt"), 0, {}, Modifier.testTag("segment"))
            }
        }
        listOf("knapp", "chip", "segment").forEach { rule.onNodeWithTag(it, useUnmergedTree = true).assertHeightIsAtLeast(48.dp) }
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
    fun `ikonknappen med djup anropar onClick`() {
        var clicks = 0
        show { AppIconButton(R.drawable.ic_arrow_back, "Tillbaka", { clicks++ }, variant = IconButtonVariant.Tonal) }
        rule.onNodeWithContentDescription("Tillbaka").assertTouchHeightIsEqualTo(48.dp).performClick()
        assertEquals(1, clicks)
    }

    private companion object {
        const val SETTLE_MILLIS = 1_000L
    }
}
