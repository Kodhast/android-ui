package se.kodhast.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.components.testing.TestTheme

/** D-3: standardknappen är tonad, och bara de fyllda knapparna och FAB:en märks som vyns primära handling. */
@RunWith(RobolectricTestRunner::class)
class PrimaryActionBehaviorTest {

    @get:Rule
    val rule = createComposeRule()

    private val primary = SemanticsMatcher.expectValue(PrimaryAction, true)
    private val notPrimary = SemanticsMatcher.keyNotDefined(PrimaryAction)

    private fun show(depth: Boolean, content: @Composable () -> Unit) = rule.setContent { TestTheme(depth = depth, content = content) }

    private fun assertMarks(depth: Boolean) {
        show(depth) {
            Column {
                AppButton("Standard", {})
                ButtonVariant.entries.forEach { AppButton(it.name, {}, variant = it) }
                AppFloatingToolbar(listOf(ToolbarItem("Idag", R.drawable.ic_box)), 0, {}, action = ToolbarAction("Lägg till", R.drawable.ic_add) {})
            }
        }
        rule.onNodeWithText("Standard").assert(notPrimary)
        ButtonVariant.entries.forEach { variant ->
            rule.onNodeWithText(variant.name).assert(if (variant in FILLED) primary else notPrimary)
        }
        rule.onNodeWithContentDescription("Lägg till").assert(primary)
    }

    @Test
    fun `fyllda knappar och FAB-knappen märks, standard och resten inte`() = assertMarks(depth = false)

    @Test
    fun `märkningen gäller också med temats djup`() = assertMarks(depth = true)
}
