package se.kodhast.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.dp
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.components.testing.Blue
import se.kodhast.ui.components.testing.TestTheme
import se.kodhast.ui.theme.Tone

/**
 * Postens delar från Dagboken (android-ui #57): mätvärdet, favoritstjärnan, datum och tid, tidsåtgången,
 * påminnelseraden och anteckningskortet (flyttade från Dagbokens komponenttester).
 */
@RunWith(RobolectricTestRunner::class)
class EntryPartsBehaviorTest {

    @get:Rule
    val rule = createComposeRule()

    private fun show(content: @Composable () -> Unit) = rule.setContent { TestTheme(content = content) }

    private val button = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)

    private fun state(value: String) = SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, value)

    @Test
    fun `mätvärdet läses som en enhet och är en knapp med 48 dp bara med onClick`() {
        var clicks = 0
        show {
            Column {
                StatPill(R.drawable.ic_check, "7 842", "Steg", Tone.Blue)
                StatPill(R.drawable.ic_clock, "—", "Sömn", Tone.Primary, onClick = { clicks++ }, onClickLabel = "Begär åtkomst")
            }
        }
        rule.onNodeWithText("Steg").fetchSemanticsNode().let { node ->
            assertTrue(SemanticsActions.OnClick !in node.config, "ren avläsning är ingen knapp")
        }
        rule.onNodeWithText("Sömn").assert(button).assertHeightIsAtLeast(48.dp).performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun `ett mätvärde med åtgärd skriver den sist och läser den som åtgärd`() {
        var clicks = 0
        show { StatPill(R.drawable.ic_clock, "2 saknas", "Träning · Syremättnad", Tone.Primary, onClick = { clicks++ }, action = "Ge åtkomst") }
        rule.onNodeWithText("Ge åtkomst").assertIsDisplayed()
        val pill = rule.onNodeWithText("2 saknas").assert(button).assertHeightIsAtLeast(48.dp)
        assertEquals("Ge åtkomst", pill.fetchSemanticsNode().config[SemanticsActions.OnClick].label)
        pill.performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun `StatPill med åtgärd men utan onClick fäller – som NoticeBanner`() {
        assertFailsWith<IllegalArgumentException> {
            show { StatPill(R.drawable.ic_clock, "2 saknas", "Träning", Tone.Primary, action = "Ge åtkomst") }
        }
    }

    @Test
    fun `FavoriteStar säger vad ett tryck gör och växlar`() {
        var favorite by mutableStateOf(false)
        show { FavoriteStar("Alvedon", favorite, { favorite = !favorite }) }
        rule.onNodeWithContentDescription("Markera Alvedon som favorit").assert(state("Inte favorit")).performClick()
        assertTrue(favorite)
        rule.onNodeWithContentDescription("Ta bort Alvedon som favorit").assert(state("Favorit"))
    }

    @Test
    fun `favoriteMenuItem har samma text och stjärna som FavoriteStar och växlar`() {
        var toggled = 0
        var off: AppMenuItem? = null
        var on: AppMenuItem? = null
        show {
            off = favoriteMenuItem("Alvedon", false) { toggled++ }
            on = favoriteMenuItem("Alvedon", true) { toggled++ }
        }
        assertEquals("Markera Alvedon som favorit", off?.label)
        assertEquals(R.drawable.ic_star, off?.icon)
        assertEquals("Ta bort Alvedon som favorit", on?.label)
        assertEquals(R.drawable.ic_star_filled, on?.icon)
        off?.onClick?.invoke()
        assertEquals(1, toggled)
    }

    @Test
    fun `FavoriteMark läses som Favorit`() {
        show { FavoriteMark() }
        rule.onNodeWithContentDescription("Favorit").assertIsDisplayed()
    }

    @Test
    fun `datum och tid på en rad läses som två fält`() {
        show { DateTimeRow(LocalDate(2026, 10, 4), LocalTime(8, 30), {}, {}) }
        rule.onNodeWithContentDescription("Datum, sön 4 okt 2026").assertIsDisplayed()
        rule.onNodeWithContentDescription("Tid, 08:30").assertIsDisplayed()
    }

    @Test
    fun `tidsåtgången stegar om fem minuter och snabbvalen sätter värdet`() {
        var minutes by mutableIntStateOf(45)
        show { DurationRow(minutes, { minutes = it }) }
        rule.onNodeWithContentDescription("Öka minuter").performClick()
        assertEquals(50, minutes)
        rule.onNodeWithText("1 tim").performClick()
        assertEquals(60, minutes)
        rule.onNodeWithText("1 tim 30 min").performClick()
        assertEquals(90, minutes)
    }

    @Test
    fun `påminnelseraden - raden väljer tid och reglaget slår av och på`() {
        var on by mutableStateOf(true)
        var time by mutableStateOf(LocalTime(8, 0))
        show { ReminderTimeRow("Efter frukost", time, { time = it }, enabled = on, onEnabledChange = { on = it }) }
        rule.onNodeWithContentDescription("Efter frukost")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.ToggleableState, ToggleableState.On))
            .performClick()
        assertEquals(false, on)
        rule.onNodeWithText("08:00").performClick()
        rule.onNodeWithText("OK").performClick()
        assertEquals(LocalTime(8, 0), time)
        assertEquals(false, on)
    }

    @Test
    fun `en nedtonad påminnelse tonar texten men inte reglaget, som går att slå på`() {
        var on by mutableStateOf(false)
        show {
            Column {
                ReminderTimeRow("Natt", LocalTime(22, 0), {}, enabled = on, onEnabledChange = { on = it })
                ReminderTimeRow("Kväll", LocalTime(19, 0), {}, enabled = true, onEnabledChange = {})
                ReminderTimeRow("Morgon", LocalTime(7, 0), {}, enabled = true, onEnabledChange = {}, inactive = true)
            }
        }
        fun darkest(text: String) = rule.onNodeWithText(text, useUnmergedTree = true).captureToImage().toPixelMap().let { map ->
            (0 until map.width).minOf { x -> (0 until map.height).minOf { y -> map[x, y].red } }
        }
        assertTrue(darkest("Natt") > darkest("Kväll") + 0.2f, "avslagen påminnelse: texten nedtonad")
        assertTrue(darkest("Morgon") > darkest("Kväll") + 0.2f, "gruppen av: texten nedtonad")
        // Reglaget har full kontrast: samma bild som ett reglage i en vanlig rad.
        val switch = { name: String -> rule.onNodeWithContentDescription(name).captureToImage().toPixelMap().let { m -> List(m.width * m.height) { m[it % m.width, it / m.width] } } }
        assertEquals(switch("Kväll"), switch("Morgon"), "reglaget tonas inte ned")
        rule.onNodeWithContentDescription("Natt").assertHasClickAction().performClick()
        assertEquals(true, on)
    }

    @Test
    fun `anteckningen visar början av texten och skrivs utfälld, och ett tömt fält är ingen anteckning`() {
        var note by mutableStateOf<String?>("Sov dåligt.")
        show { NoteCard(note, { note = it }) }
        rule.onNodeWithText("Sov dåligt.").assertIsDisplayed()
        rule.onNodeWithText("Anteckning").assert(state("Hopfälld")).performClick()
        rule.onNodeWithText("Sov dåligt.").performTextReplacement("Sov bra.")
        assertEquals("Sov bra.", note)
        rule.onNodeWithText("Sov bra.").performTextReplacement("")
        assertNull(note, "ett tömt fält ger null, aldrig en tom text")
        rule.onNodeWithText("Lägg till en anteckning").assertIsDisplayed()
    }

    @Test
    fun `anteckningens fel syns också i stängt läge`() {
        show { NoteCard("Sov dåligt.", {}, error = "Värdet går inte att spara") }
        rule.onNodeWithText("Värdet går inte att spara").assertIsDisplayed()
    }

    @Test
    fun `anteckningens tak är en parameter`() {
        var note by mutableStateOf<String?>(null)
        show { NoteCard(note, { note = it }, initiallyExpanded = true, maxLength = 5) }
        rule.onNodeWithText("Anteckning").performTextReplacement("Sov dåligt.")
        assertTrue((note?.length ?: 0) <= 5, "taket gäller: $note")
    }
}
