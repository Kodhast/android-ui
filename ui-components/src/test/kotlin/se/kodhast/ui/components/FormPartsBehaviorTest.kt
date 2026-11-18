package se.kodhast.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.common.EditorEvents
import se.kodhast.ui.common.EditorUiState
import se.kodhast.ui.components.testing.TestTheme

/** ReseApotekets generella delar (android-ui #75): stegare med etikett, värderuta, okänt värde, pillrad, inställningssida, arkets underrubrik, formulärets huvud och tom sektion. */
@RunWith(RobolectricTestRunner::class)
class FormPartsBehaviorTest {

    @get:Rule
    val rule = createComposeRule()

    private fun show(content: @Composable () -> Unit) = rule.setContent { TestTheme(content = content) }

    @Test
    fun `stegaren med etikett läser etiketten en gång och ändrar värdet`() {
        var value by mutableIntStateOf(2)
        show { QuantityStepper(value, { value = it }, "doser per dag", range = 1..24, showLabel = true) }
        rule.onNodeWithContentDescription("2 doser per dag").assertIsDisplayed()
        // Den synliga etiketten är dold för TalkBack – stegaren säger den redan.
        rule.onAllNodesWithText("doser per dag").assertCountEquals(0)
        rule.onNodeWithContentDescription("Öka doser per dag").performClick()
        assertEquals(3, value)
    }

    @Test
    fun `stegaren utan etikett visar ingen text utöver värdet`() {
        show { QuantityStepper(2, {}, "doser per dag") }
        rule.onAllNodesWithText("doser per dag", useUnmergedTree = true).assertCountEquals(0)
        rule.onNodeWithText("2").assertIsDisplayed()
    }

    @Test
    fun `värderutan läses som en enhet och upp igen när värdet ändras`() {
        show { ValueTile("För en 7-dagarsresa", "18 tabletter · 2 kartor", supporting = "2 × 1 tablett/dag × (7 + 2) dagar") }
        rule.onNode(hasText("För en 7-dagarsresa") and hasText("18 tabletter · 2 kartor") and hasText("2 × 1 tablett/dag × (7 + 2) dagar"))
            .assertIsDisplayed()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
    }

    @Test
    fun `värderutan utan förklaring visar etikett och värde`() {
        show { ValueTile("Förslag", "12 tabletter") }
        rule.onNodeWithText("Förslag", substring = true).assertIsDisplayed()
        rule.onAllNodesWithText("2 × 1", substring = true).assertCountEquals(0)
    }

    @Test
    fun `okänt värde har bibliotekets text som standard`() {
        show { UnknownValueNotice() }
        rule.onNodeWithText("Det här kan inte visas i den här versionen. Uppdatera appen för att ändra.").assertIsDisplayed()
    }

    @Test
    fun `okänt värde med egen text`() {
        show { UnknownValueNotice(text = "Okänd typ") }
        rule.onNodeWithText("Okänd typ").assertIsDisplayed()
    }

    @Test
    fun `pillraden visar pillren bredvid varandra`() {
        show {
            PillRow {
                InfoPill("Ändrad")
                InfoPill("08:00")
            }
        }
        val first = rule.onNodeWithText("Ändrad").fetchSemanticsNode().boundsInRoot
        val second = rule.onNodeWithText("08:00").fetchSemanticsNode().boundsInRoot
        assertTrue(second.left > first.right, "pillren står på en rad")
        assertEquals(first.center.y, second.center.y, 1f)
    }

    @Test
    fun `inställningssidan har titel, tillbaka och innehåll`() {
        var back = 0
        show {
            SettingsPage("Inställningar", onBack = { back++ }) {
                GroupLabel("Utseende")
                SettingsCard { Text("Tema") }
            }
        }
        rule.onNodeWithText("Inställningar").assertIsDisplayed()
        rule.onNodeWithText("UTSEENDE").assertIsDisplayed()
        rule.onNodeWithText("Tema").assertIsDisplayed()
        rule.onNodeWithContentDescription("Tillbaka").performClick()
        assertEquals(1, back)
    }

    @Test
    fun `inställningskortet visar sitt fel sist och som fel`() {
        show { SettingsCard(error = "Kunde inte logga ut") { Text("Logga ut") } }
        rule.onNodeWithText("Kunde inte logga ut")
            .assertIsDisplayed()
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Error))
        val row = rule.onNodeWithText("Logga ut").fetchSemanticsNode().boundsInRoot
        val error = rule.onNodeWithText("Kunde inte logga ut").fetchSemanticsNode().boundsInRoot
        assertTrue(error.top >= row.bottom, "felet står under innehållet")
    }

    @Test
    fun `inställningskortet utan fel visar bara innehållet`() {
        show { SettingsCard { Text("Logga ut") } }
        rule.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.Error)).assertDoesNotExist()
    }

    @Test
    fun `arkets underrubrik står under rubriken`() {
        show { AppBottomSheet("Levaxin", onDismiss = {}, subtitle = "Anna · 1 tablett dagligen") { Text("Stegare") } }
        val title = rule.onNodeWithText("Levaxin").fetchSemanticsNode().boundsInRoot
        val subtitle = rule.onNodeWithText("Anna · 1 tablett dagligen").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val body = rule.onNodeWithText("Stegare").fetchSemanticsNode().boundsInRoot
        assertTrue(subtitle.top >= title.bottom && body.top >= subtitle.bottom, "rubrik, underrubrik, innehåll")
    }

    @Test
    fun `formulärets huvud står överst och centrerat`() {
        show {
            EntityEditScreen("Ändra person", EditorUiState("Anna"), EditorEvents.None, {}, {}, header = { Text("🦊") }) {
                AppTextField("Anna", {}, "Namn")
            }
        }
        val header = rule.onNodeWithText("🦊").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val field = rule.onNode(hasSetTextAction()).fetchSemanticsNode().boundsInRoot
        assertTrue(header.bottom <= field.top, "huvudet står ovanför fältet")
        assertEquals(field.center.x, header.center.x, 1f)
    }

    @Test
    fun `tom sektion visar sin text bara utfälld`() {
        var expanded by mutableStateOf(true)
        show { SectionFoldout("Recept", expanded, { expanded = !expanded }, count = "0", emptyText = "Inga recept än") {} }
        rule.onNodeWithText("Inga recept än").assertIsDisplayed()
        rule.onNodeWithText("Recept").performClick()
        rule.onNodeWithText("Inga recept än").assertDoesNotExist()
    }
}
