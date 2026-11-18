package se.kodhast.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation3.runtime.NavEntry
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.common.SyncUiState
import se.kodhast.ui.components.AccountAvatar
import se.kodhast.ui.components.AppBrandStyle
import se.kodhast.ui.components.LocalAppBrandStyle
import se.kodhast.ui.components.testing.TestTheme
import se.kodhast.ui.components.testing.sampleOffline

/** Navigationsramen: verktygsraden i flikarnas rot, dold på undersidor, plusknappen, kontot, synkläget och tillbaka. */
@RunWith(RobolectricTestRunner::class)
class AppNavHostTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val backStack = testBackStack()

    private fun show(entries: (TestKey) -> NavEntry<TestKey> = plainScreens) = rule.setContent { TestTheme { TestNavHost(backStack, entries) } }

    /** En skärm med en räknare i `rememberSaveable`, för att se om dess sparade tillstånd lever kvar. */
    private val counters: (TestKey) -> NavEntry<TestKey> = { key ->
        NavEntry(key) {
            var taps by rememberSaveable { mutableIntStateOf(0) }
            Text("$key: $taps", Modifier.clickable { taps++ })
        }
    }

    @Test
    fun `verktygsraden byter flik och visar flikens skärm`() {
        show()
        rule.onNodeWithText("Skärm: Home").assertIsDisplayed()
        rule.onNodeWithContentDescription("Personer").performClick()
        rule.onNodeWithText("Skärm: People").assertIsDisplayed()
        assertEquals(People, backStack.currentTab)
    }

    @Test
    fun `undersidor döljer verktygsraden`() {
        show()
        rule.runOnIdle { backStack.push(Edit()) }
        rule.onNodeWithText("Skärm: Edit(id=null)").assertIsDisplayed()
        rule.onNodeWithContentDescription("Personer").assertDoesNotExist()
        rule.runOnIdle { backStack.pop() }
        rule.onNodeWithContentDescription("Personer").assertIsDisplayed()
    }

    @Test
    fun `en flik behåller sitt sparade tillstånd när man byter flik och tillbaka`() {
        show(counters)
        rule.onNodeWithContentDescription("Personer").performClick()
        rule.onNodeWithText("People: 0").performClick()
        rule.onNodeWithText("People: 1").assertIsDisplayed()
        rule.onNodeWithContentDescription("Gemensamt").performClick()
        rule.onNodeWithContentDescription("Personer").performClick()
        rule.onNodeWithText("People: 1").assertIsDisplayed()
    }

    @Test
    fun `en stängd undersida börjar om när den öppnas igen`() {
        show(counters)
        rule.runOnIdle { backStack.push(Edit()) }
        rule.onNodeWithText("Edit(id=null): 0").performClick()
        rule.onNodeWithText("Edit(id=null): 1").assertIsDisplayed()
        rule.runOnIdle { backStack.pop() }
        rule.runOnIdle { backStack.push(Edit()) }
        rule.onNodeWithText("Edit(id=null): 0").assertIsDisplayed()
    }

    @Test
    fun `plusknappens meny ger det valda, och utan åtgärd finns ingen plusknapp`() {
        val picked = mutableListOf<String>()
        var action by mutableStateOf(addAction { picked += it }.takeIf { false })
        rule.setContent { TestTheme { TestNavHost(backStack, action = action) } }
        rule.onNodeWithContentDescription("Lägg till").assertDoesNotExist()
        action = addAction { picked += it }
        rule.onNodeWithContentDescription("Lägg till").performClick()
        rule.onNodeWithText("Ny person").performClick()
        assertEquals(listOf("Ny person"), picked)
    }

    @Test
    fun `systemets tillbaka stänger översta skärmen och säger vilken, i första flikens rot stänger systemet appen`() {
        val popped = mutableListOf<TestKey>()
        rule.setContent { TestTheme { AppNavHost(backStack, TAB_ITEMS, plainScreens, onPopped = { popped += it }) } }
        rule.runOnIdle { backStack.select(People) }
        rule.runOnIdle { backStack.push(Settings) }
        rule.onNodeWithText("Skärm: Settings").assertIsDisplayed()

        rule.runOnIdle { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.onNodeWithText("Skärm: People").assertIsDisplayed()
        rule.runOnIdle { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.onNodeWithText("Skärm: Home").assertIsDisplayed()
        rule.runOnIdle { rule.activity.onBackPressedDispatcher.onBackPressed() }

        rule.runOnIdle {
            assertEquals(listOf(Settings, People), popped)
            assertTrue(rule.activity.isFinishing, "i första flikens rot går tillbaka till systemet")
        }
    }

    @Test
    fun `sidhuvudet har kontot från ramen och prickarna läser synkläget`() {
        var sync by mutableStateOf(SyncUiState())
        var opened = 0
        rule.setContent {
            TestTheme {
                CompositionLocalProvider(LocalAppBrandStyle provides AppBrandStyle(headerDots = true)) {
                    AppNavHost(backStack, TAB_ITEMS, headerScreens, sync = sync, account = { AccountAvatar("Testa Testsson", onClick = { opened++ }) })
                }
            }
        }
        rule.onNodeWithContentDescription(PENDING).assertDoesNotExist()
        sync = SyncUiState(pending = true)
        rule.onNodeWithContentDescription(PENDING).assertExists()
        rule.onNodeWithContentDescription("Konto och inställningar").performClick()
        assertEquals(1, opened)
    }

    @Test
    fun `en nekad skrivning visas en gång med orsaken och släpps sedan`() {
        var shown = 0
        rule.mainClock.autoAdvance = false
        rule.setContent {
            TestTheme { AppNavHost(backStack, TAB_ITEMS, plainScreens, sync = SyncUiState(writeError = OFFLINE), onSyncErrorShown = { shown++ }) }
        }
        rule.mainClock.advanceTimeBy(SHOWN_MILLIS)
        rule.onNodeWithText("En ändring kunde inte sparas och har ångrats. Ingen anslutning just nu. Det du sparar skickas när nätet är tillbaka.").assertIsDisplayed()
        assertEquals(0, shown)
        rule.mainClock.advanceTimeBy(LONG_SNACKBAR_MILLIS)
        rule.waitForIdle()
        assertEquals(1, shown)
    }

    @Test
    fun `verktygsraden och back stacken har lika många flikar`() {
        assertFailsWith<IllegalArgumentException> {
            rule.setContent { TestTheme { AppNavHost(backStack, TAB_ITEMS.take(2), plainScreens) } }
        }
    }

    private companion object {
        const val PENDING = "Ändringar väntar på servern"
        const val SHOWN_MILLIS = 500L
        const val LONG_SNACKBAR_MILLIS = 11_000L
        val OFFLINE = sampleOffline()
    }
}
