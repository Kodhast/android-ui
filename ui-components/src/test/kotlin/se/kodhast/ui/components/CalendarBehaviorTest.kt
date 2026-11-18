package se.kodhast.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlin.test.assertEquals
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.common.ListUiState
import se.kodhast.ui.components.testing.TestTheme

/** Kalendern, Välj dag och listans rullning till en rad (android-ui #68). Idag är onsdag 18 november 2026. */
@RunWith(RobolectricTestRunner::class)
class CalendarBehaviorTest {

    @get:Rule
    val rule = createComposeRule()

    private val today = LocalDate(2026, 11, 18)
    private val marked = setOf(LocalDate(2026, 11, 7), today)

    private fun show(content: @Composable () -> Unit) = rule.setContent { TestTheme(content = content) }

    @Test
    fun `kalendern läser datum, idag och har poster, och framtiden går inte att välja`() {
        val picked = mutableListOf<LocalDate>()
        show { AppCalendar(today, {}, marked, LocalDate(2026, 11, 7), { picked += it }, today = today, dimFuture = true) }
        rule.onNodeWithText("November 2026").assertIsDisplayed()
        rule.onNodeWithContentDescription("ons 18 nov 2026, idag, har poster").assertIsEnabled()
        rule.onNodeWithContentDescription("lör 7 nov 2026, har poster").assertIsSelected().performClick()
        rule.onNodeWithContentDescription("tors 19 nov 2026").assertIsNotEnabled()
        rule.onNodeWithContentDescription("Nästa månad").assertIsNotEnabled()
        assertEquals(listOf(LocalDate(2026, 11, 7)), picked)
    }

    @Test
    fun `pilarna byter månad`() {
        show {
            var month by remember { mutableStateOf(today) }
            AppCalendar(month, { month = it }, marked, null, {}, today = today, dimFuture = true)
        }
        rule.onNodeWithContentDescription("Föregående månad").performClick()
        rule.onNodeWithText("Oktober 2026").assertIsDisplayed()
        rule.onNodeWithContentDescription("Nästa månad").assertIsEnabled().performClick()
        rule.onNodeWithText("November 2026").assertIsDisplayed()
    }

    @Test
    fun `Välj dag öppnar arket, och en dag eller Idag stänger det och ges till appen`() {
        val picked = mutableListOf<LocalDate>()
        show { DayPicker(marked, today, { picked += it }) }
        rule.onNodeWithText("Välj dag").performClick()
        rule.onNodeWithText("November 2026").assertIsDisplayed()
        rule.onNodeWithContentDescription("lör 7 nov 2026, har poster").performClick()
        rule.onNodeWithText("November 2026").assertDoesNotExist()
        rule.onNodeWithText("Välj dag").performClick()
        rule.onNodeWithText("Idag").performClick()
        rule.onNodeWithText("November 2026").assertDoesNotExist()
        assertEquals(listOf(LocalDate(2026, 11, 7), today), picked)
    }

    private val days = (1..30).map { "Dag $it" }
    private val group: (String) -> ListGroup = { ListGroup("Rubrik ${it.substringAfter(" ").toInt() / 3}") }

    @Test
    fun `listan rullar till gruppens rubrik och släpper begäran`() {
        var request by mutableStateOf<ListScrollRequest?>(null)
        var scrolled = 0
        show {
            EntityListScreen("Dagbok", ListUiState.Content(days.toImmutableList()), EmptyContent(R.drawable.ic_box, "Tomt", "Inget här"), null, key = { it }, group = group, scrollTo = request, onScrolled = {
                scrolled++
                request = null
            }) { ItemRow(it) }
        }
        rule.onNodeWithText("Rubrik 8").assertDoesNotExist()
        request = ListScrollRequest("Dag 24")
        rule.waitForIdle()
        rule.onNodeWithText("Rubrik 8").assertIsDisplayed()
        assertEquals(1, scrolled)
    }

    @Test
    fun `en rad som inte finns än rullas till när den kommer`() {
        var rows by mutableStateOf(days.take(6))
        var scrolled = 0
        val request = ListScrollRequest("Dag 27")
        show {
            EntityListScreen("Dagbok", ListUiState.Content(rows.toImmutableList()), EmptyContent(R.drawable.ic_box, "Tomt", "Inget här"), null, key = { it }, group = group, scrollTo = request, onScrolled = { scrolled++ }) { ItemRow(it) }
        }
        rule.waitForIdle()
        assertEquals(0, scrolled)
        rows = days
        rule.waitForIdle()
        rule.onNodeWithText("Rubrik 9").assertIsDisplayed()
        assertEquals(1, scrolled)
    }

    @Test
    fun `med header, tom fast grupp och undergrupper rullar listan till en rad mitt i en grupp`() {
        var request by mutableStateOf<ListScrollRequest?>(null)
        var scrolled = 0
        val fixed = ListGroup("Tom grupp", emptyText = "Inget här än")
        show {
            EntityListScreen(
                "Dagbok",
                ListUiState.Content(days.toImmutableList()),
                EmptyContent(R.drawable.ic_box, "Tomt", "Inget här"),
                null,
                key = { it },
                group = group,
                groups = listOf(fixed),
                header = { Text("Överst") },
                subgroup = { ListSubgroup(if (it.substringAfter(" ").toInt() % 3 == 2) "Sent" else "Tidigt", Color.Gray) },
                scrollTo = request,
                onScrolled = { scrolled++ }
            ) { ItemRow(it) }
        }
        request = ListScrollRequest("Dag 29")
        rule.waitForIdle()
        rule.onNodeWithText("Dag 29").assertIsDisplayed()
        rule.onNodeWithText("Överst").assertDoesNotExist()
        assertEquals(1, scrolled)
    }
}
