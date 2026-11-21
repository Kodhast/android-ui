package se.kodhast.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.dp
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.components.testing.TestTheme
import se.kodhast.ui.theme.AppTheme

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

    @Test
    fun `Foldout utan läge håller det själv, och onExpand körs bara när den fälls ut`() {
        var expands = 0
        show { Foldout("Varför byta?", onExpand = { expands++ }) { Text("Lägre avgift") } }
        rule.onNodeWithText("Lägre avgift").assertDoesNotExist()
        rule.onNodeWithText("Varför byta?").performClick()
        rule.onNodeWithText("Lägre avgift").assertExists()
        assertEquals(1, expands)
        rule.onNodeWithText("Varför byta?").performClick()
        rule.onNodeWithText("Lägre avgift").assertDoesNotExist()
        assertEquals(1, expands)
    }

    @Test
    fun `Foldout utan läge börjar utfälld med initiallyExpanded och behåller läget efter återskapande`() {
        val restore = StateRestorationTester(rule)
        restore.setContent { TestTheme { Foldout("Varför byta?", initiallyExpanded = true) { Text("Lägre avgift") } } }
        rule.onNodeWithText("Lägre avgift").assertExists()
        rule.onNodeWithText("Varför byta?").performClick()
        restore.emulateSavedInstanceStateRestore()
        rule.onNodeWithText("Lägre avgift").assertDoesNotExist()
    }

    @Test
    fun `tonat öppet läge lägger radtonen bakom den öppna delen, stängd ingen yta`() {
        var expanded by mutableStateOf(true)
        var tint = Color.Unspecified
        var card = Color.Unspecified
        rule.setContent {
            TestTheme {
                tint = AppTheme.extendedColors.rowTint
                card = AppTheme.extendedColors.card
                Box(Modifier.background(card)) {
                    Foldout("Uppgifter", expanded, { expanded = !expanded }, Modifier.testTag("foldout"), tintedWhenOpen = true) { Text("Boarea") }
                }
            }
        }
        fun edge() = rule.onRoot().captureToImage().toPixelMap().let { it[it.width - 2, it.height / 2] }
        rule.waitForIdle()
        assertEquals(tint, edge())
        expanded = false
        rule.waitForIdle()
        assertEquals(card, edge())
    }

    @Test
    fun `Radera står sist i det utfällda, efter radens knappar, och frågar innan något raderas`() {
        var deleted = 0
        var followed = 0
        show {
            Foldout(
                "Globalfond Index A",
                expanded = true,
                onToggle = {},
                actions = { AppButton("Bevaka alternativ", { followed++ }, variant = ButtonVariant.Secondary, compact = true) },
                delete = DeleteAction("Radera transaktion?", "Transaktionen raderas permanent.") { deleted++ }
            ) { Text("84,1203 andelar") }
        }
        val follow = rule.onNodeWithText("Bevaka alternativ").getBoundsInRoot()
        val delete = rule.onNodeWithText("Radera").getBoundsInRoot()
        assertTrue(delete.left > follow.left, "Radera står sist")
        rule.onNodeWithText("Radera").performClick()
        rule.onNodeWithText("Radera transaktion?").assertExists()
        assertEquals(0, deleted, "inget raderas förrän dialogen svarat")
        rule.onAllNodesWithText("Radera")[1].performClick()
        assertEquals(1, deleted)
        rule.onNodeWithText("Radera transaktion?").assertDoesNotExist()
    }

    @Test
    fun `svep åt vänster på raden frågar i samma dialog, och Avbryt lämnar raden`() {
        var deleted = 0
        show {
            AppCard {
                Foldout("Sverige Småbolag", expanded = false, onToggle = {}, summary = "Sälj", delete = DeleteAction("Radera transaktion?", "–") { deleted++ }) { Text("–") }
            }
        }
        rule.onNodeWithText("Sverige Småbolag").performTouchInput { swipeLeft() }
        rule.waitForIdle()
        rule.onNodeWithText("Radera transaktion?").assertExists()
        rule.onNodeWithText("Avbryt").performClick()
        assertEquals(0, deleted)
        assertTrue(rule.onNodeWithText("Sverige Småbolag").getBoundsInRoot().left >= 0.dp, "raden har fjädrat tillbaka")
    }

    @Test
    fun `utan delete finns varken Radera eller svep`() {
        show { Foldout("Anteckning", expanded = true, onToggle = {}) { Text("Text") } }
        rule.onNodeWithText("Radera").assertDoesNotExist()
        rule.onNodeWithText("Anteckning").performTouchInput { swipeLeft() }
        rule.waitForIdle()
        rule.onNodeWithText("Text").assertExists()
    }
}
