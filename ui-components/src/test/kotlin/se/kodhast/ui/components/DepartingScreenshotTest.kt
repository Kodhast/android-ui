package se.kodhast.ui.components

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.components.testing.SampleCard
import se.kodhast.ui.components.testing.SampleSheet
import se.kodhast.ui.components.testing.captureLightAndDark

/** Skärmdump (ljust + mörkt) av en avbockad post mitt i pausen i [DepartingItems] (Dagboken #456). */
@RunWith(RobolectricTestRunner::class)
class DepartingScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `DepartingItems - avbockad mitt i pausen`() {
        rule.mainClock.autoAdvance = false
        rule.captureLightAndDark("DepartingItems_paus", settle = { mainClock.advanceTimeBy(250) }) {
            var taken by remember { mutableStateOf(emptySet<String>()) }
            LaunchedEffect(Unit) { taken = setOf("Waran 2,5 mg") }
            SampleSheet {
                SampleCard {
                    DepartingItems(
                        items = DOSES.filter { it !in taken },
                        key = { it },
                        departed = { it },
                        exit = DepartingExit.SlideAway
                    ) { dose -> CheckRow(dose, checked = dose in taken, onCheckedChange = {}, subtitle = if (dose in taken) "tagen 08:12" else "08:00") }
                }
            }
        }
    }

    private companion object {
        val DOSES = listOf("Levaxin 50 µg", "Waran 2,5 mg", "Omeprazol 20 mg")
    }
}
