package se.kodhast.ui.components

import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.common.ListUiState
import se.kodhast.ui.components.testing.TestTheme

/**
 * Det som kom i 1.10.0: knappen i en grupps rubrik, hopfällt läge utifrån, grupper som alltid visas,
 * menyn med arkivväxelns egen text och sektionsrubrikens radbrytning med bubbla.
 */
@RunWith(RobolectricTestRunner::class)
class ListGroupBehaviorTest {

    @get:Rule
    val rule = createComposeRule()

    private val empty = EmptyContent(R.drawable.ic_box, "Inget att packa än", "Räkna om för att få förslag.", actions = listOf(EmptyAction("Räkna om", {})))

    private fun show(content: @Composable () -> Unit) = rule.setContent { TestTheme(content = content) }

    private fun count(text: String) = rule.onAllNodesWithText(text).fetchSemanticsNodes().size

    private fun lines(text: String): Int {
        val results = mutableListOf<TextLayoutResult>()
        rule.onNodeWithText(text).fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action?.invoke(results)
        return results.single().lineCount
    }

    @Test
    fun `knappen i rubriken trycks också i en hopfälld grupp utan att fälla ut den`() {
        var added = 0
        show {
            var open by remember { mutableStateOf(false) }
            val group = ListGroup("Mediciner", count = "1 / 2", action = GroupAction(R.drawable.ic_add, "Lägg till i Mediciner") { added++ }, fold = GroupFold(open) { open = !open })
            EntityListScreen("Gran Canaria", ListUiState.Content(persistentListOf("Alvedon")), empty, null, key = { it }, group = { group }) { ItemRow(it) }
        }
        rule.onNodeWithText("Alvedon").assertDoesNotExist()
        rule.onNodeWithText("1 / 2").assertIsDisplayed()
        rule.onNodeWithContentDescription("Lägg till i Mediciner").performClick()
        assertEquals(1, added)
        rule.onNodeWithText("Alvedon").assertDoesNotExist()
        rule.onNodeWithText("Mediciner").performClick()
        rule.onNodeWithText("Alvedon").assertIsDisplayed()
    }

    @Test
    fun `knappen finns också i en grupp som inte fälls ihop`() {
        var added = 0
        show {
            val group = ListGroup("Övrigt", action = GroupAction(R.drawable.ic_add, "Lägg till i Övrigt") { added++ })
            EntityListScreen("Gran Canaria", ListUiState.Content(persistentListOf("Solkräm")), empty, null, key = { it }, group = { group }) { ItemRow(it) }
        }
        rule.onNodeWithContentDescription("Lägg till i Övrigt").performClick()
        assertEquals(1, added)
        rule.onNodeWithText("Solkräm").assertIsDisplayed()
    }

    @Test
    fun `hopfällt läge utifrån styr gruppen och tryck på rubriken går till appen`() {
        val toggles = mutableListOf<Boolean>()
        var open by mutableStateOf(true)
        show {
            val group = ListGroup("Mediciner", fold = GroupFold(open) { toggles += !open })
            EntityListScreen("Gran Canaria", ListUiState.Content(persistentListOf("Alvedon")), empty, null, key = { it }, group = { group }) { ItemRow(it) }
        }
        rule.onNodeWithText("Alvedon").assertIsDisplayed()
        rule.onNodeWithText("Mediciner").performClick()
        assertEquals(listOf(false), toggles, "listan fäller inte själv – appen håller läget")
        rule.onNodeWithText("Alvedon").assertIsDisplayed()
        open = false
        rule.onNodeWithText("Alvedon").assertDoesNotExist()
    }

    @Test
    fun `grupperna som alltid visas står först i ordning, också utan rader, med sin tomma text`() {
        val medicines = ListGroup("Mediciner", emptyText = "Inget här än")
        val other = ListGroup("Övrigt", emptyText = "Inget här än")
        val hidden = ListGroup("Dolda")
        show {
            EntityListScreen(
                "Gran Canaria",
                ListUiState.Content(persistentListOf("D1", "Solkräm")),
                empty,
                null,
                key = { it },
                group = { if (it == "D1") hidden else other },
                groups = listOf(medicines, other)
            ) { ItemRow(it) }
        }
        val y = listOf("Mediciner", "Inget här än", "Övrigt", "Solkräm", "Dolda").map { rule.onNodeWithText(it).fetchSemanticsNode().positionInRoot.y }
        assertEquals(y.sorted(), y, "Mediciner (tom), Övrigt, sedan övriga grupper")
        assertEquals(1, count("Inget här än"), "Övrigt har en rad och ingen tom text")
    }

    @Test
    fun `en grupp som byggs om per rad med nya lambdor är fortfarande en grupp`() {
        show {
            EntityListScreen(
                "Gran Canaria",
                ListUiState.Content(persistentListOf("Alvedon", "Ipren")),
                empty,
                null,
                key = { it },
                group = { row -> ListGroup("Mediciner", action = GroupAction(R.drawable.ic_add, "Lägg till i Mediciner") { println(row) }) },
                groups = listOf(ListGroup("Mediciner", action = GroupAction(R.drawable.ic_add, "Lägg till i Mediciner") {}, emptyText = "Inget här än"))
            ) { ItemRow(it) }
        }
        assertEquals(1, count("Mediciner"))
        assertEquals(0, count("Inget här än"))
        rule.onNodeWithText("Ipren").assertIsDisplayed()
    }

    @Test
    fun `en tom lista med grupper visar grupperna och det tomma budskapet som kort`() {
        show {
            EntityListScreen<String>(
                "Gran Canaria",
                ListUiState.Empty,
                empty,
                null,
                key = { it },
                group = { ListGroup("Övrigt") },
                groups = listOf(ListGroup("Mediciner", emptyText = "Inget här än"), ListGroup("Övrigt", emptyText = "Inget här än"))
            ) {}
        }
        listOf("Inget att packa än", "Räkna om", "Mediciner", "Övrigt").forEach { rule.onNodeWithText(it).assertIsDisplayed() }
        assertEquals(2, count("Inget här än"))
        rule.onNodeWithTag(ENTITY_LIST_ADD_TAG).assertDoesNotExist()
    }

    @Test
    fun `menyns val och arkivväxeln med egen text står i samma meny`() {
        var shown = false
        show {
            EntityListScreen(
                "Gran Canaria",
                ListUiState.Content(persistentListOf("Alvedon")),
                empty,
                null,
                key = { it },
                menu = listOf(AppMenuItem("Räkna om", {})),
                archive = ListArchive(showing = shown, showLabel = "Visa dolda (2)", hideLabel = "Göm dolda", onEvent = { shown = !shown })
            ) { ItemRow(it) }
        }
        assertEquals(1, rule.onAllNodes(hasContentDescription("Fler val")).fetchSemanticsNodes().size, "en enda ⋮")
        rule.onNodeWithContentDescription("Fler val").performClick()
        rule.onNodeWithText("Räkna om").assertIsDisplayed()
        rule.onNodeWithText("Visa dolda (2)").performClick()
        assertTrue(shown)
    }

    @Test
    fun `arkivväxeln utan egen text heter som förut`() {
        show {
            EntityListScreen("Personer", ListUiState.Content(persistentListOf("Anna")), empty, null, key = { it }, archive = ListArchive()) { ItemRow(it) }
        }
        rule.onNodeWithContentDescription("Fler val").performClick()
        rule.onNodeWithText("Visa arkiverade").assertIsDisplayed()
    }

    @Test
    fun `en sektionsrubrik bryts mellan ord på två rader och har ingen bubbla`() {
        show { SectionHeader(TWO_WORDS, Modifier.width(NARROW)) }
        assertEquals(2, lines(TWO_WORDS))
        rule.onNodeWithText(TWO_WORDS).performTouchInput { longClick() }
        assertEquals(1, count(TWO_WORDS))
    }

    @Test
    fun `ett ord som inte ryms kortas på en rad och ett långt tryck visar hela titeln`() {
        show { SectionHeader(ONE_WORD, Modifier.width(NARROW), count = "1 / 2") }
        assertEquals(1, lines(ONE_WORD), "inget ord bryts mitt i")
        rule.onNodeWithText(ONE_WORD).performTouchInput { longClick() }
        assertEquals(2, count(ONE_WORD), "bubblan visar hela titeln")
    }

    @Test
    fun `en kort titel har ingen bubbla`() {
        show { SectionHeader("Övrigt", count = "3") }
        rule.onNodeWithText("Övrigt").performTouchInput { longClick() }
        assertEquals(1, count("Övrigt"))
    }

    @Test
    fun `en radbrytning mitt i ett ord känns igen`() {
        lateinit var mid: TextLayoutResult
        lateinit var between: TextLayoutResult
        show {
            Text(ONE_WORD + ONE_WORD, Modifier.width(60.dp), onTextLayout = { mid = it })
            Text("Krämer och salvor", Modifier.width(60.dp), onTextLayout = { between = it })
        }
        rule.waitForIdle()
        assertTrue(mid.breaksMidWord(ONE_WORD + ONE_WORD))
        assertFalse(between.breaksMidWord("Krämer och salvor"))
    }

    private companion object {
        const val TWO_WORDS = "Krämer & salvor"
        const val ONE_WORD = "Kosttillskott"
        val NARROW = 80.dp
    }
}
