package se.kodhast.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlin.test.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.common.AuthGate
import se.kodhast.ui.common.UiError
import se.kodhast.ui.components.testing.TestTheme
import se.kodhast.ui.components.testing.sampleOffline

/** Inloggningen (android-ui #53): knappen, upptaget, felet en gång, "Uppdatera appen" och grinden. */
@RunWith(RobolectricTestRunner::class)
class AuthBehaviorTest {

    @get:Rule
    val rule = createComposeRule()

    private fun show(content: @Composable () -> Unit) = rule.setContent { TestTheme(content = content) }

    @Composable
    private fun SampleSignIn(busy: Boolean = false, error: UiError? = null, onErrorShown: () -> Unit = {}, onSignIn: () -> Unit = {}) = SignInScreen(
        appIcon = R.drawable.ic_box,
        appName = "ReseApoteket",
        welcome = "Håll koll på vad ni ska packa i reseapoteket",
        note = "Ett hushåll skapas åt dig första gången.",
        busy = busy,
        error = error,
        onErrorShown = onErrorShown,
        onSignIn = onSignIn
    )

    @Test
    fun `inloggningen visar appen och knappen anropar onSignIn`() {
        var clicks = 0
        show { SampleSignIn(onSignIn = { clicks++ }) }
        rule.onNodeWithText("ReseApoteket").assertExists()
        rule.onNodeWithText("Håll koll på vad ni ska packa i reseapoteket").assertExists()
        rule.onNodeWithText("Ett hushåll skapas åt dig första gången.").assertExists()
        rule.onNode(hasText("Logga in med Google") and hasClickAction()).performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun `upptagen säger Loggar in och spärrar knappen`() {
        var clicks = 0
        show { SampleSignIn(busy = true, onSignIn = { clicks++ }) }
        rule.onNodeWithText("Logga in med Google").assertDoesNotExist()
        rule.onNode(hasText("Loggar in …") and hasClickAction()).assertIsNotEnabled().performClick()
        assertEquals(0, clicks)
    }

    @Test
    fun `felet visas en gång och onErrorShown anropas`() {
        rule.mainClock.autoAdvance = false
        var error by mutableStateOf<UiError?>(sampleOffline())
        var shown = 0
        show {
            SampleSignIn(error = error, onErrorShown = {
                shown++
                error = null
            })
        }
        rule.mainClock.advanceTimeBy(FRAME_MILLIS)
        rule.waitForIdle()
        rule.onNodeWithText("Ingen anslutning just nu. Det du sparar skickas när nätet är tillbaka.").assertExists()
        assertEquals(0, shown)
        rule.mainClock.advanceTimeBy(SHORT_SNACKBAR_MILLIS)
        rule.waitForIdle()
        assertEquals(1, shown)
        rule.mainClock.advanceTimeBy(SHORT_SNACKBAR_MILLIS)
        rule.waitForIdle()
        assertEquals(1, shown, "samma fel visas inte igen")
        rule.onNodeWithText("Ingen anslutning just nu. Det du sparar skickas när nätet är tillbaka.").assertDoesNotExist()
    }

    @Test
    fun `Uppdatera appen visar appens texter och knappen öppnar releasesidan`() {
        val opened = mutableListOf<String>()
        val uriHandler = object : UriHandler {
            override fun openUri(uri: String) {
                opened += uri
            }
        }
        show {
            CompositionLocalProvider(LocalUriHandler provides uriHandler) {
                UpdateRequiredScreen(
                    message = "Ert hushåll har sparats med en nyare version av ReseApoteket.",
                    note = "Ingenting ändras förrän appen är uppdaterad.",
                    releasesUrl = RELEASES_URL
                )
            }
        }
        rule.onNodeWithText("Uppdatera appen").assertExists()
        rule.onNodeWithText("Ert hushåll har sparats med en nyare version av ReseApoteket.").assertExists()
        rule.onNodeWithText("Ingenting ändras förrän appen är uppdaterad.").assertExists()
        rule.onNode(hasText("Hämta senaste versionen") and hasClickAction()).performClick()
        assertEquals(listOf(RELEASES_URL), opened)
    }

    @Test
    fun `grinden visar rätt gren för varje läge`() {
        var gate by mutableStateOf(AuthGate.Loading)
        show {
            AuthGateHost(
                gate = gate,
                signIn = { Text("inloggning") },
                updateRequired = { Text("uppdatera") }
            ) { Text("innehåll") }
        }
        val expected = mapOf(
            AuthGate.SignedOut to "inloggning",
            AuthGate.NeedsAccount to "inloggning",
            AuthGate.UpdateRequired to "uppdatera",
            AuthGate.Ready to "innehåll"
        )
        rule.onNodeWithContentDescription("Laddar").assertExists()
        expected.values.toSet().forEach { rule.onNodeWithText(it).assertDoesNotExist() }
        for ((next, text) in expected) {
            gate = next
            rule.waitForIdle()
            rule.onNodeWithText(text).assertExists()
            rule.onNodeWithContentDescription("Laddar").assertDoesNotExist()
            (expected.values.toSet() - text).forEach { rule.onNodeWithText(it).assertDoesNotExist() }
        }
    }

    private companion object {
        const val RELEASES_URL = "https://github.com/Kodhast/ReseApoteket/releases/latest"

        const val FRAME_MILLIS = 16L

        /** Ett kort meddelande (4 s) och dess in- och utglidning. */
        const val SHORT_SNACKBAR_MILLIS = 5_000L
    }
}
