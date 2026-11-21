package se.kodhast.ui.components

import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.components.testing.TestTheme

/** Poster som stannar en stund när de lämnar listan (Dagboken #456), och nicken. */
@RunWith(RobolectricTestRunner::class)
class DepartingBehaviorTest {

    @get:Rule
    val rule = createComposeRule()

    private data class Dose(val name: String, val taken: Boolean = false)

    private val all = listOf(Dose("Levaxin"), Dose("Waran"), Dose("Ipren"))
    private var items by mutableStateOf(all)
    private val departedItems = mutableListOf<Dose>()

    private fun show(departed: (Dose) -> Dose? = { it.copy(taken = true) }) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            TestTheme {
                Column {
                    DepartingItems(items, key = { it.name }, departed = departed, exit = DepartingExit.SlideAway, onDeparted = { departedItems += it }) { dose ->
                        ItemRow(if (dose.taken) "${dose.name} tagen" else dose.name)
                    }
                }
            }
        }
        rule.mainClock.advanceTimeByFrame()
    }

    private fun takeWaran() {
        items = all - Dose("Waran")
        // Ändringen görs utanför kompositionen; utan detta ser en stillastående klocka den aldrig.
        Snapshot.sendApplyNotifications()
        rule.mainClock.advanceTimeByFrame()
    }

    private fun exists(text: String) = rule.onAllNodes(androidx.compose.ui.test.hasText(text)).fetchSemanticsNodes().isNotEmpty()

    @Test
    fun `posten står kvar i sin nya version på sin plats under pausen och är borta efter`() {
        show()
        takeWaran()
        rule.mainClock.advanceTimeBy(500)
        rule.onNodeWithText("Waran tagen").assertIsDisplayed()
        val levaxin = rule.onNodeWithText("Levaxin").getBoundsInRoot()
        val waran = rule.onNodeWithText("Waran tagen").getBoundsInRoot()
        val ipren = rule.onNodeWithText("Ipren").getBoundsInRoot()
        assertTrue(levaxin.top < waran.top && waran.top < ipren.top, "kvar mellan grannarna")
        assertEquals(emptyList(), departedItems)

        rule.mainClock.advanceTimeBy(3_000)
        assertTrue(!exists("Waran tagen"))
        assertEquals(listOf(Dose("Waran", taken = true)), departedItems)
    }

    @Test
    fun `utan ny version försvinner posten direkt och onDeparted anropas inte`() {
        show(departed = { null })
        takeWaran()
        assertTrue(!exists("Waran"))
        rule.mainClock.advanceTimeBy(3_000)
        assertEquals(emptyList(), departedItems)
    }

    @Test
    fun `kommer posten tillbaka under pausen stannar den`() {
        show()
        takeWaran()
        rule.mainClock.advanceTimeBy(300)
        items = all
        Snapshot.sendApplyNotifications()
        rule.mainClock.advanceTimeBy(3_000)
        rule.onNodeWithText("Waran").assertIsDisplayed()
        assertTrue(!exists("Waran tagen"))
        assertEquals(emptyList(), departedItems)
    }

    @Test
    fun `med Ta bort animationer försvinner posten utan paus och utan onDeparted`() {
        val resolver = ApplicationProvider.getApplicationContext<android.content.Context>().contentResolver
        Settings.Global.putFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 0f)
        try {
            show()
            takeWaran()
            rule.mainClock.advanceTimeBy(100)
            assertTrue(!exists("Waran tagen"))
            assertTrue(!exists("Waran"))
            // Som en post utan ny version: ingen nick att visa, så inget onDeparted.
            assertEquals(emptyList(), departedItems)
        } finally {
            Settings.Global.putFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        }
    }

    @Test
    fun `nod växer en stund när trigger ändras, och inte första gången`() {
        var count by mutableIntStateOf(0)
        rule.mainClock.autoAdvance = false
        rule.setContent {
            Box(Modifier.size(60.dp).background(Color.White).testTag("ruta"), contentAlignment = Alignment.Center) {
                Box(Modifier.nod(count).size(30.dp).background(Color.Black))
            }
        }
        rule.mainClock.advanceTimeByFrame()
        val still = blackPixels()
        rule.mainClock.advanceTimeBy(500)
        assertEquals(still, blackPixels(), "ingen nick första gången")

        count++
        Snapshot.sendApplyNotifications()
        rule.mainClock.advanceTimeBy(80)
        assertTrue(blackPixels() > still, "större mitt i nicken")
        rule.mainClock.advanceTimeBy(2_000)
        assertEquals(still, blackPixels(), "tillbaka efter nicken")
    }

    private fun blackPixels(): Int {
        val map = rule.onNodeWithTag("ruta").captureToImage().toPixelMap()
        var n = 0
        for (x in 0 until map.width) for (y in 0 until map.height) if (map[x, y].red < 0.5f) n++
        return n
    }
}
