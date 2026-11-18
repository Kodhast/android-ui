package se.kodhast.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.dp
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.components.testing.SampleCard
import se.kodhast.ui.components.testing.TestTheme
import se.kodhast.ui.theme.Tone

/** Reglaget, veckoremsan, svep för att radera, chevronen, stegguiden, rullgardinen och sektionskortet – beteende och TalkBack. */
@RunWith(RobolectricTestRunner::class)
class StepsBehaviorTest {

    @get:Rule
    val rule = createComposeRule()

    private fun show(content: @Composable () -> Unit) = rule.setContent { TestTheme(content = content) }

    private fun SemanticsMatcher.Companion.state(text: String) = expectValue(SemanticsProperties.StateDescription, text)

    @Test
    fun `reglaget stannar bara på sina steg inom skalan`() {
        assertEquals(5, snapToStep(6.9f, 0..60, 5))
        assertEquals(10, snapToStep(7.6f, 0..60, 5))
        assertEquals(60, snapToStep(70f, 0..60, 5))
        assertEquals(-10, snapToStep(-12f, -10..10, 1))
        assertEquals(3, snapToStep(3.4f, 0..10, 1))
    }

    @Test
    fun `reglaget läses med etikett och värde, och TalkBacks ändring följer stegen`() {
        var minutes by mutableIntStateOf(10)
        show { AppSlider("Längd", minutes, { minutes = it }, valueText = "$minutes min", valueRange = 0..60, step = 5, stateDescription = "$minutes minuter") }
        rule.onNodeWithContentDescription("Längd").assert(SemanticsMatcher.state("10 minuter"))
        rule.onNodeWithContentDescription("Längd").performSemanticsAction(SemanticsActions.SetProgress) { it(22f) }
        rule.waitForIdle()
        assertEquals(20, minutes)
        rule.onNodeWithContentDescription("Längd").assert(SemanticsMatcher.state("20 minuter"))
        // Huvudet ovanför läses inte separat.
        rule.onNodeWithText("20 min").assertDoesNotExist()
    }

    @Test
    fun `ett avstängt reglage går inte att ändra`() {
        show { AppSlider("Energi", 5, {}, valueText = "5", enabled = false) }
        rule.onNodeWithContentDescription("Energi").assertIsNotEnabled()
    }

    @Test
    fun `veckoremsan väljer en dag, och framtida dagar går inte att välja`() {
        var picked: LocalDate? = null
        show { DateStrip(TODAY, TODAY, { picked = it }, {}, today = TODAY, datesWithEntries = setOf(MONDAY)) }
        rule.onNodeWithContentDescription("mån 16 nov 2026, har poster", substring = true).performClick()
        assertEquals(MONDAY, picked)
        rule.onNodeWithContentDescription("ons 18 nov 2026, idag", substring = true).assertIsSelected()
        rule.onNodeWithContentDescription("fre 20 nov 2026", substring = true).assertIsNotEnabled()
    }

    @Test
    fun `svep byter vecka men aldrig till en vecka med bara framtida dagar`() {
        val weeks = mutableListOf<LocalDate>()
        show { DateStrip(TODAY, TODAY, {}, { weeks += it }, today = TODAY, modifier = Modifier.testTag(STRIP)) }
        rule.onNodeWithTag(STRIP).performTouchInput { swipeRight() }
        rule.onNodeWithTag(STRIP).performTouchInput { swipeLeft() }
        assertEquals(listOf(LocalDate(2026, 11, 9)), weeks)
    }

    @Test
    fun `förra veckan kan bläddras framåt, och bytena finns för TalkBack`() {
        val weeks = mutableListOf<LocalDate>()
        show { DateStrip(LocalDate(2026, 11, 11), LocalDate(2026, 11, 11), {}, { weeks += it }, today = TODAY, modifier = Modifier.testTag(STRIP)) }
        rule.onNodeWithTag(STRIP).performTouchInput { swipeLeft() }
        val actions = rule.onNodeWithTag(STRIP).fetchSemanticsNode().config[SemanticsActions.CustomActions]
        assertEquals(listOf("Föregående vecka", "Nästa vecka"), actions.map { it.label })
        actions.forEach { it.action() }
        assertEquals(listOf(MONDAY, LocalDate(2026, 11, 2), MONDAY), weeks)
    }

    @Test
    fun `dagen klar läses som klar`() {
        show { DateStrip(TODAY, MONDAY, {}, {}, today = TODAY, todayDone = true) }
        rule.onNodeWithContentDescription("ons 18 nov 2026, idag", substring = true).assert(SemanticsMatcher.state("Klar"))
    }

    @Test
    fun `svep åt vänster begär radering, och kortet står kvar`() {
        var requested = 0
        show { SwipeToDelete({ requested++ }) { SampleCard { Text("Promenad") } } }
        rule.onNodeWithText("Promenad").performTouchInput { swipeLeft() }
        rule.waitForIdle()
        assertEquals(1, requested)
        rule.onNodeWithText("Promenad").assertExists()
        assertTrue(rule.onNodeWithText("Promenad").getBoundsInRoot().left >= 0.dp, "kortet har fjädrat tillbaka")
        // Bakgrunden är bara en bild; åtgärden för TalkBack är menyns Radera.
        rule.onNodeWithText("Radera").assertDoesNotExist()
    }

    @Test
    fun `svep åt höger raderar inte`() {
        var requested = 0
        show { SwipeToDelete({ requested++ }) { SampleCard { Text("Promenad") } } }
        rule.onNodeWithText("Promenad").performTouchInput { swipeRight() }
        rule.waitForIdle()
        assertEquals(0, requested)
    }

    @Test
    fun `chevronknappen växlar och läses med åtgärd och läge`() {
        var expanded by mutableStateOf(false)
        show { ExpandButton(expanded, { expanded = !expanded }) }
        rule.onNodeWithContentDescription("Fäll ut").assert(SemanticsMatcher.state("Hopfälld")).performClick()
        rule.onNodeWithContentDescription("Fäll ihop").assert(SemanticsMatcher.state("Utfälld"))
    }

    @Test
    fun `stegguiden visar Nästa, Föregående och sist Spara`() {
        var saved = 0
        show {
            val pager = rememberPagerState(pageCount = { 3 })
            HorizontalPager(pager) { Text("Sida $it") }
            StepIndicator(3, pager.currentPage)
            Row { StepNavigation(pager, onSave = { saved++ }, saveEnabled = true) }
        }
        rule.onNodeWithText("Föregående").assertDoesNotExist()
        rule.onNodeWithText("Nästa").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Föregående").assertExists()
        rule.onNodeWithText("Nästa").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Nästa").assertDoesNotExist()
        rule.onNodeWithText("Spara").assertIsEnabled().performClick()
        assertEquals(1, saved)
        rule.onNodeWithText("Föregående").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Nästa").assertExists()
    }

    @Test
    fun `stegguidens klart-läge är status och läses upp`() {
        show {
            val pager = rememberPagerState(initialPage = 1, pageCount = { 2 })
            Row { StepNavigation(pager, onSave = {}, done = true, doneText = "Sparat", doneAnnouncement = "Måendet är sparat") }
        }
        rule.onNodeWithText("Sparat").assert(SemanticsMatcher.state("Måendet är sparat"))
        rule.onNodeWithText("Spara").assertDoesNotExist()
        rule.onNodeWithText("Föregående").assertIsNotEnabled()
    }

    @Test
    fun `rullgardinen öppnar menyn och läses som rullgardin med sitt värde`() {
        var period = ""
        show { DropdownPill("30 dagar", listOf(AppMenuItem("7 dagar", { period = "7" }), AppMenuItem("90 dagar", { period = "90" }))) }
        val pill = rule.onNodeWithText("30 dagar")
        assertEquals("Välj", pill.fetchSemanticsNode().config[SemanticsActions.OnClick].label)
        pill.performClick()
        rule.onNodeWithText("90 dagar").performClick()
        assertEquals("90", period)
    }

    @Test
    fun `fler-valen står i en meny under chipsen och sätter samma val`() {
        var type by mutableStateOf("Promenad")
        show { ChoiceChips(listOf("Promenad", "Yoga"), type, { type = it }, { it }, more = listOf("Cykling", "Simning"), moreLabel = "Fler typer", morePickLabel = "Välj typ") }
        rule.onNodeWithText("Promenad").assertIsSelected()
        rule.onNodeWithContentDescription("Fler typer, Välj typ").performClick()
        rule.onNodeWithText("Simning").performClick()
        assertEquals("Simning", type)
        rule.onNodeWithContentDescription("Fler typer, Simning").assertExists()
        rule.onNodeWithText("Yoga").performClick()
        assertEquals("Yoga", type)
        rule.onNodeWithContentDescription("Fler typer, Välj typ").assertExists()
    }

    @Test
    fun `utan fler-val finns inget fält`() {
        show { ChoiceChips(listOf("Promenad", "Yoga"), "Yoga", {}, { it }) }
        rule.onNodeWithContentDescription("Fler", substring = true).assertDoesNotExist()
    }

    @Test
    fun `sektionskortet har rubriken och innehållet, och nyckeltalet värde och etikett`() {
        show { SectionCard("Allt klart för idag", count = "3") { KeyFigure("6,8", "snittenergi") } }
        rule.onNodeWithText("Allt klart för idag").assertExists()
        rule.onNodeWithText("3").assertExists()
        rule.onNodeWithText("6,8").assertExists()
        rule.onNodeWithText("snittenergi").assertExists()
    }

    @Test
    fun `rutnätet ställer två per rad och ett ensamt sist på hela bredden`() {
        show {
            Row(Modifier.width(WIDTH)) {
                StatPillGrid(
                    listOf(StatPillItem(R.drawable.ic_clock, "7 h", "Sömn"), StatPillItem(R.drawable.ic_star, "7 842", "Steg"), StatPillItem(R.drawable.ic_calendar, "62", "Vilopuls")),
                    Tone.Primary
                )
            }
        }
        val sleep = rule.onNodeWithText("Sömn", useUnmergedTree = true).getBoundsInRoot()
        val steps = rule.onNodeWithText("Steg", useUnmergedTree = true).getBoundsInRoot()
        val pulse = rule.onNodeWithText("Vilopuls", useUnmergedTree = true).getBoundsInRoot()
        assertEquals(sleep.top, steps.top)
        assertTrue(pulse.top > sleep.bottom)
        assertTrue(steps.left > sleep.left)
        val half = rule.onNodeWithText("7 h").getBoundsInRoot()
        val whole = rule.onNodeWithText("62").getBoundsInRoot()
        assertTrue(whole.right - whole.left > (half.right - half.left) * 2, "det ensamma värdet tar hela bredden")
    }

    private companion object {
        val TODAY = LocalDate(2026, 11, 18)
        val MONDAY = LocalDate(2026, 11, 16)
        const val STRIP = "remsa"
        val WIDTH = 360.dp
    }
}
