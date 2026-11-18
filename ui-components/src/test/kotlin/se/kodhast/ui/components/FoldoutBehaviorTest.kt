package se.kodhast.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import kotlin.test.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.components.testing.TestTheme

/** Hopfällbara sektioner: hela titelraden växlar, sammanfattningen bara stängd, och en kontroll i raden växlar inte. */
@RunWith(RobolectricTestRunner::class)
class FoldoutBehaviorTest {

    @get:Rule
    val rule = createComposeRule()

    private fun show(content: @Composable () -> Unit) = rule.setContent { TestTheme(content = content) }

    @Test
    fun `hela titelraden fäller ut och ihop, och sammanfattningen syns bara stängd`() {
        show {
            var expanded by remember { mutableStateOf(false) }
            Foldout("Anteckning", expanded, onToggle = { expanded = !expanded }, summary = "Sov dåligt") { Text("Hela texten") }
        }
        rule.onNodeWithText("Sov dåligt").assertExists()
        assertEquals(0, rule.onAllNodesWithText("Hela texten").fetchSemanticsNodes().size)
        val header = rule.onNode(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
        header.assertHeightIsAtLeast(48.dp).assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Hopfälld"))

        header.performClick()
        rule.onNodeWithText("Hela texten").assertExists()
        assertEquals(0, rule.onAllNodesWithText("Sov dåligt").fetchSemanticsNodes().size)
        header.assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Utfälld"))
    }

    @Test
    fun `knappen i rubrikraden gör sitt och växlar inte sektionen`() {
        var toggles = 0
        var adds = 0
        show {
            SectionFoldout(
                "Recept",
                expanded = false,
                onToggle = { toggles++ },
                count = "2",
                trailing = { AppIconButton(R.drawable.ic_add, "Lägg till recept", onClick = { adds++ }, variant = IconButtonVariant.Tonal) }
            ) { Text("Levaxin") }
        }
        rule.onNodeWithContentDescription("Lägg till recept").performClick()
        assertEquals(1, adds)
        assertEquals(0, toggles)
        rule.onNodeWithText("Recept").performClick()
        assertEquals(1, toggles)
    }

    @Test
    fun `sektionens titel läses som rubrik`() {
        show { SectionFoldout("Kosttillskott", expanded = true, onToggle = {}) { Text("D-vitamin") } }
        rule.onNode(isHeading() and androidx.compose.ui.test.hasText("Kosttillskott"), useUnmergedTree = true).assertExists()
        rule.onNodeWithText("D-vitamin").assertExists()
    }

    @Test
    fun `ExpandableContent visar innehållet bara när det är synligt`() {
        var visible by mutableStateOf(false)
        show { ExpandableContent(visible) { Text("Detaljer") } }
        assertEquals(0, rule.onAllNodesWithText("Detaljer").fetchSemanticsNodes().size)
        visible = true
        rule.onNodeWithText("Detaljer").assertExists()
    }
}
