package se.kodhast.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.click
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.test.espresso.Espresso
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.components.testing.TestTheme
import se.kodhast.ui.components.testing.sampleOffline

/** Det som ligger ovanpå: ark, menyer, dialoger, datumfält, verktygsrad och emojiväljare. */
@RunWith(RobolectricTestRunner::class)
class OverlayBehaviorTest {

    @get:Rule
    val rule = createComposeRule()

    private fun show(content: @Composable () -> Unit) = rule.setContent { TestTheme(content = content) }

    /** Som i apparna: raden längst ned, så att FAB-menyns val får plats ovanför knappen. */
    private fun showAtBottom(content: @Composable () -> Unit) = show { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) { content() } }

    private var dismissed = 0

    private fun sheet(dirty: Boolean = false, canDismiss: () -> Boolean = { true }) = show {
        AppBottomSheet("Promenad", onDismiss = { dismissed++ }, dirty = dirty, canDismiss = canDismiss) { Text("45 minuter") }
    }

    /** Ett svep från rubriken långt nedåt – längre än arket, som annars fjädrar tillbaka. */
    private fun swipeSheetDown() = rule.onNodeWithText("Promenad").performTouchInput { swipeDown(startY = centerY, endY = centerY + SWIPE_PX) }

    private fun tapOutside() = rule.onNodeWithContentDescription(SCRIM).performClick()

    private fun assertAsks() {
        rule.onNodeWithText("Släng ändringar?").assertIsDisplayed()
        rule.onNodeWithText("Det du ändrat sparas inte.").assertIsDisplayed()
        rule.onNodeWithText("45 minuter").assertIsDisplayed()
        assertEquals(0, dismissed, "arket står kvar tills svaret")
        rule.onNodeWithText("Fortsätt redigera").performClick()
        rule.onNodeWithText("Släng ändringar?").assertDoesNotExist()
        rule.onNodeWithText("45 minuter").assertIsDisplayed()
        assertEquals(0, dismissed)
    }

    @Test
    fun `arket med ändringar frågar vid svep ner, tryck utanför och bakåt – Släng stänger`() {
        sheet(dirty = true)
        swipeSheetDown()
        assertAsks()
        tapOutside()
        assertAsks()
        Espresso.pressBack()
        assertAsks()

        swipeSheetDown()
        rule.onNodeWithText("Släng").performClick()
        rule.waitForIdle()
        assertEquals(1, dismissed)
        rule.onNodeWithText("Släng ändringar?").assertDoesNotExist()
    }

    @Test
    fun `arket utan ändringar stängs direkt`() {
        sheet()
        tapOutside()
        rule.waitForIdle()
        assertEquals(1, dismissed)
        rule.onNodeWithText("Släng ändringar?").assertDoesNotExist()
    }

    @Test
    fun `canDismiss läses i stunden – false håller arket öppet utan fråga`() {
        var allowed = true
        sheet(dirty = true, canDismiss = { allowed })
        allowed = false
        swipeSheetDown()
        tapOutside()
        rule.waitForIdle()
        rule.onNodeWithText("Släng ändringar?").assertDoesNotExist()
        rule.onNodeWithText("45 minuter").assertIsDisplayed()
        assertEquals(0, dismissed)
        allowed = true
        swipeSheetDown()
        rule.onNodeWithText("Släng ändringar?").assertIsDisplayed()
    }

    @Test
    fun `hide döljer arket och anropar onDismiss en gång`() {
        var hide by mutableStateOf(false)
        show { AppBottomSheet("Promenad", onDismiss = { dismissed++ }, dirty = true, hide = hide) { Text("45 minuter") } }
        hide = true
        rule.waitForIdle()
        assertEquals(1, dismissed)
        rule.onNodeWithText("Släng ändringar?").assertDoesNotExist()
    }

    @Test
    fun `efter ett konfigurationsbyte är frågan borta och arket står kvar`() {
        val restoration = StateRestorationTester(rule)
        restoration.setContent {
            TestTheme { AppBottomSheet("Promenad", onDismiss = { dismissed++ }, dirty = true) { Text("45 minuter") } }
        }
        swipeSheetDown()
        rule.onNodeWithText("Släng ändringar?").assertIsDisplayed()
        restoration.emulateSavedInstanceStateRestore()
        rule.onNodeWithText("Släng ändringar?").assertDoesNotExist()
        rule.onNodeWithText("45 minuter").assertIsDisplayed()
    }

    @Test
    fun `knappraden står fast i botten med långt innehåll och felet står ovanför den`() {
        var saved = 0
        show {
            AppBottomSheet("Promenad", onDismiss = {}, error = sampleOffline(), footer = { AppButton("Spara", { saved++ }) }) {
                repeat(40) { Text("Rad $it") }
            }
        }
        rule.onNodeWithText("Spara").assertIsDisplayed().performClick()
        assertEquals(1, saved)
        rule.onNodeWithText("Rad 39").assertIsNotDisplayed()
        rule.onNodeWithText("Ingen anslutning just nu. Det du sparar skickas när nätet är tillbaka.").assertIsDisplayed()
    }

    @Test
    fun `utan knapprad rullar innehållet och felet står sist`() {
        show {
            AppBottomSheet("Promenad", onDismiss = {}, error = sampleOffline()) {
                repeat(40) { Text("Rad $it") }
            }
        }
        rule.onNodeWithText("Rad 0").assertIsDisplayed()
        rule.onNodeWithText("Rad 39").assertIsNotDisplayed()
        rule.onNodeWithText("Ingen anslutning just nu. Det du sparar skickas när nätet är tillbaka.").assertIsNotDisplayed()
    }

    @Test
    fun `menyn öppnas bakom Fler val, ett val stänger den och anropar onClick`() {
        var archived = 0
        show { AppMenu(listOf(AppMenuItem("Arkivera", { archived++ }, R.drawable.ic_archive), AppMenuItem("Radera", {}, destructive = true))) }
        rule.onNodeWithText("Arkivera").assertDoesNotExist()
        rule.onNodeWithContentDescription("Fler val").performClick()
        rule.onNodeWithText("Arkivera").performClick()
        assertEquals(1, archived)
        rule.onNodeWithText("Arkivera").assertDoesNotExist()
    }

    @Test
    fun `en dämpad menypost med undertext visar båda och går att välja`() {
        var picked = 0
        show { AppMenuPopup(listOf(AppMenuItem("Ipren 400 mg", { picked++ }, supporting = "Dagsgräns nådd · 2 av 2", muted = true)), expanded = true, onDismiss = {}) }
        rule.onNodeWithText("Dagsgräns nådd · 2 av 2").assertIsDisplayed()
        rule.onNodeWithText("Ipren 400 mg").performClick()
        assertEquals(1, picked)
    }

    @Test
    fun `en avdelning får en rubrik där den börjar`() {
        var picked = ""
        show {
            AppMenuPopup(
                listOf(
                    AppMenuItem("Loratadin 10 mg", { picked = "l" }),
                    AppMenuItem("Betapred 0,5 mg", { picked = "b" }, section = "Recept"),
                    AppMenuItem("Levaxin 100 µg", { picked = "v" }, section = "Recept")
                ),
                expanded = true,
                onDismiss = {}
            )
        }
        rule.onNodeWithText("RECEPT").assertIsDisplayed()
        rule.onNodeWithText("Levaxin 100 µg").performClick()
        assertEquals("v", picked)
    }

    @Test
    fun `en kryssrad i menyn läses som kryssruta och stänger inte menyn`() {
        var mood by mutableStateOf(true)
        var sleep by mutableStateOf(false)
        var dismissals = 0
        show {
            AppMenuPopup(
                listOf(AppMenuItem("Mående", { mood = !mood }, checked = mood), AppMenuItem("Sömn", { sleep = !sleep }, checked = sleep)),
                expanded = true,
                onDismiss = { dismissals++ }
            )
        }
        rule.onNodeWithText("Mående").assertIsOn().assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox))
        rule.onNodeWithText("Sömn").assertIsOff().performClick()
        rule.onNodeWithText("Sömn").assertIsOn()
        assertEquals(0, dismissals)
    }

    @Test
    fun `asSplitMenu ger split-knappen pilen och menyn, och en tom lista ingen pil`() {
        var recalculated = 0
        assertNull(emptyList<AppMenuItem>().asSplitMenu())
        show { AddSplitButton("Lägg till", {}, menu = listOf(AppMenuItem("Räkna om", { recalculated++ })).asSplitMenu()) }
        rule.onNodeWithContentDescription("Fler val").performClick()
        rule.onNodeWithText("Räkna om").performClick()
        assertEquals(1, recalculated)
    }

    @Test
    fun `bekräftelsen utan nej-knapp har bara en knapp och innehållet under texten`() {
        var confirmed = 0
        show { ConfirmDialog("Avsluta förkylningen?", "Välj slutdatum.", "Avsluta", { confirmed++ }, {}, dismissLabel = null) { Text("Slutdatum") } }
        rule.onNodeWithText("Avbryt").assertDoesNotExist()
        rule.onNodeWithText("Slutdatum").assertIsDisplayed()
        rule.onNodeWithText("Avsluta").performClick()
        assertEquals(1, confirmed)
    }

    @Test
    fun `nej-knappen kan betyda något annat än att stänga`() {
        var declined = 0
        var dismissedDialog = 0
        show { ConfirmDialog("Räkna om?", "Listan ändras.", "Räkna om", {}, { dismissedDialog++ }, dismissLabel = "Behåll listan", onDecline = { declined++ }) }
        rule.onNodeWithText("Behåll listan").performClick()
        assertEquals(1 to 0, declined to dismissedDialog)
    }

    @Test
    fun `raderingen stänger dialogen och raderar`() {
        val calls = mutableListOf<String>()
        show { DeleteConfirmDialog(DeleteAction("Radera Alvedon?", "Receptet tas bort för gott.") { calls += "radera" }, onDismiss = { calls += "stäng" }) }
        rule.onNodeWithText("Radera Alvedon?").assertIsDisplayed()
        rule.onNodeWithText("Radera").performClick()
        assertEquals(listOf("stäng", "radera"), calls)
    }

    @Test
    fun `Släng ändringar har Släng och Fortsätt redigera`() {
        var discarded = 0
        var kept = 0
        show { DiscardChangesDialog({ discarded++ }, { kept++ }) }
        rule.onNodeWithText("Fortsätt redigera").performClick()
        rule.onNodeWithText("Släng").performClick()
        assertEquals(1 to 1, discarded to kept)
    }

    @Test
    fun `datumfältet visar datumet på svenska och OK behåller det valda`() {
        var date by mutableStateOf<LocalDate?>(LocalDate(2026, 12, 19))
        var changes = 0
        show {
            DateField("Avresa", date, {
                date = it
                changes++
            })
        }
        rule.onNodeWithContentDescription("Avresa, lör 19 dec 2026").performClick()
        rule.onNodeWithText("OK").performClick()
        assertEquals(1, changes)
        assertEquals(LocalDate(2026, 12, 19), date)
    }

    @Test
    fun `rensa-knappen tömmer datumet tillbaka till den tomma texten, med sammanhanget för TalkBack`() {
        var date by mutableStateOf<LocalDate?>(LocalDate(2026, 10, 4))
        show { DateField("Slutdatum", date, { date = it }, emptyLabel = "Periodens slut", onClear = { date = null }, context = "doshöjning 2") }
        rule.onNodeWithContentDescription("Slutdatum, doshöjning 2, sön 4 okt 2026").assertExists()
        rule.onNodeWithContentDescription("Till periodens slut, doshöjning 2").performClick()
        assertNull(date)
        rule.onNodeWithContentDescription("Slutdatum, doshöjning 2, Periodens slut").assertExists()
        rule.onNodeWithContentDescription("Till periodens slut, doshöjning 2").assertDoesNotExist()
    }

    @Test
    fun `utan onClear har datumfältet ingen rensa-knapp`() {
        show { DateField("Slutdatum", LocalDate(2026, 10, 4), {}, emptyLabel = "Periodens slut") }
        rule.onNodeWithContentDescription("Till periodens slut").assertDoesNotExist()
    }

    @Test
    fun `verktygsraden väljer flik och huvudknappen gör sitt`() {
        var selected by mutableIntStateOf(0)
        var logged = 0
        show {
            AppFloatingToolbar(
                listOf(ToolbarItem("Idag", R.drawable.ic_clock), ToolbarItem("Packa", R.drawable.ic_box)),
                selected,
                { selected = it },
                action = ToolbarAction("Logga", R.drawable.ic_add) { logged++ }
            )
        }
        rule.onNodeWithContentDescription("Idag").assertIsSelected()
        rule.onNodeWithContentDescription("Packa").assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)).performClick()
        assertEquals(1, selected)
        rule.onNodeWithContentDescription("Logga").performClick()
        assertEquals(1, logged)
    }

    @Test
    fun `huvudknappen med meny fäller ut valen, läser läget och stänger efter ett val`() {
        val picked = mutableListOf<String>()
        show {
            AppFloatingToolbar(
                listOf(ToolbarItem("Översikt", R.drawable.ic_clock), ToolbarItem("Innehav", R.drawable.ic_box)),
                0,
                {},
                action = ToolbarAction(
                    "Lägg till",
                    R.drawable.ic_add,
                    listOf(AppMenuItem("Köp", { picked += "köp" }), AppMenuItem("Sälj", { picked += "sälj" }))
                )
            )
        }
        rule.onNodeWithContentDescription("Lägg till").assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Hopfälld"))
        rule.onNodeWithText("Köp").assertDoesNotExist()
        rule.onNodeWithContentDescription("Lägg till").performClick()
        rule.onNodeWithText("Sälj").performClick()
        assertEquals(listOf("sälj"), picked)
        rule.onNodeWithText("Köp").assertDoesNotExist()
        rule.onNodeWithContentDescription("Lägg till").performClick()
        rule.onNodeWithText("Köp").assertExists()
        Espresso.pressBack()
        rule.onNodeWithText("Köp").assertDoesNotExist()
        assertEquals(listOf("sälj"), picked)
    }

    @Test
    fun `FAB-menyn fäller ut valen, stänger med krysset, bakåt och efter ett val`() {
        val picked = mutableListOf<String>()
        showAtBottom {
            AppFloatingToolbar(
                listOf(ToolbarItem("Idag", R.drawable.ic_clock), ToolbarItem("Dagbok", R.drawable.ic_note)),
                0,
                {},
                action = ToolbarAction(
                    "Logga",
                    R.drawable.ic_add,
                    listOf(AppMenuItem("Mående", { picked += "mående" }, icon = R.drawable.ic_person), AppMenuItem("Dos", { picked += "dos" }))
                ),
                actionMenu = ActionMenuStyle.Fab
            )
        }
        val button = rule.onNodeWithContentDescription("Logga")
        button.assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Hopfälld"))
        rule.onNodeWithText("Mående").assertDoesNotExist()

        button.performClick()
        rule.onNodeWithText("Mående").assertExists()
        rule.onNodeWithText("Dos").performClick()
        assertEquals(listOf("dos"), picked)
        rule.onNodeWithText("Mående").assertDoesNotExist()

        rule.onNodeWithContentDescription("Logga").performClick()
        // Menyns fönster måste finnas innan bakåt skickas, annars går bakåt till fönstret under.
        rule.onNodeWithText("Mående").assertExists()
        Espresso.pressBack()
        rule.onNodeWithText("Mående").assertDoesNotExist()

        rule.onNodeWithContentDescription("Logga").performClick()
        // Krysset står där knappen stod och stänger.
        rule.onNodeWithContentDescription("Stäng").performClick()
        rule.onNodeWithText("Mående").assertDoesNotExist()
        assertEquals(listOf("dos"), picked)
    }

    @Test
    fun `FAB-menyns slöja stänger vid tryck och valen läses som knappar`() {
        var picked = 0
        showAtBottom {
            AppFloatingToolbar(
                listOf(ToolbarItem("Idag", R.drawable.ic_clock)),
                0,
                {},
                action = ToolbarAction("Logga", R.drawable.ic_add, listOf(AppMenuItem("Mående", { picked++ }))),
                actionMenu = ActionMenuStyle.Fab
            )
        }
        rule.onNodeWithContentDescription("Logga").performClick()
        rule.onNodeWithText("Mående").assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
        // Slöjan är menyfönstrets rot; ett tryck i hörnet (inte på ett val) stänger utan att välja.
        rule.onAllNodes(isRoot()).onLast().performTouchInput { click(Offset(1f, 1f)) }
        rule.onNodeWithText("Mående").assertDoesNotExist()
        assertEquals(0, picked)
    }

    @Test
    fun `utan stil i anropet följer verktygsraden appens AppBrandStyle`() {
        showAtBottom {
            CompositionLocalProvider(LocalAppBrandStyle provides AppBrandStyle(actionMenu = ActionMenuStyle.Fab)) {
                AppFloatingToolbar(
                    listOf(ToolbarItem("Idag", R.drawable.ic_clock)),
                    0,
                    {},
                    action = ToolbarAction("Logga", R.drawable.ic_add, listOf(AppMenuItem("Mående", {})))
                )
            }
        }
        rule.onNodeWithContentDescription("Logga").performClick()
        // FAB-menyn har krysset; rullgardinen har inget.
        rule.onNodeWithContentDescription("Stäng").assertExists()
    }

    @Test
    fun `huvudknappen utan meny har inget läge`() {
        show { AppFloatingToolbar(listOf(ToolbarItem("Idag", R.drawable.ic_clock)), 0, {}, action = ToolbarAction("Logga", R.drawable.ic_add) {}) }
        rule.onNodeWithContentDescription("Logga").assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.StateDescription))
    }

    @Test
    fun `emojiväljaren läser varje emoji som ett radioval och väljer`() {
        var picked by mutableStateOf("🦊")
        show { EmojiPicker(listOf("🦊", "🐻", "🐰"), picked, { picked = it }) }
        rule.onNodeWithContentDescription("🦊").assertIsSelected()
        rule.onNodeWithContentDescription("🐻").performClick()
        assertEquals("🐻", picked)
    }

    private companion object {
        /** Material 3:s namn på arkets bakgrund (scrim), som stänger arket vid tryck. */
        const val SCRIM = "Close sheet"

        const val SWIPE_PX = 1500f
    }
}
