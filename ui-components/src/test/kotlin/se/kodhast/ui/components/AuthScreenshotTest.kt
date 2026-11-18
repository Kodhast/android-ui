package se.kodhast.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.common.AuthGate
import se.kodhast.ui.common.UiError
import se.kodhast.ui.components.testing.captureLightAndDark
import se.kodhast.ui.components.testing.sampleOffline

/** Skärmdumpar av inloggningen och "Uppdatera appen" (ljust + mörkt). */
@RunWith(RobolectricTestRunner::class)
class AuthScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Composable
    private fun SampleSignIn(busy: Boolean = false, error: UiError? = null) = InGate(AuthGate.SignedOut) {
        SignInScreen(
            appIcon = R.drawable.ic_box,
            appName = "ReseApoteket",
            welcome = "Håll koll på vad ni ska packa i reseapoteket",
            note = "Du loggar in med ditt Google-konto. Ett hushåll skapas åt dig första gången.",
            busy = busy,
            error = error,
            onErrorShown = {},
            onSignIn = {}
        )
    }

    /** I grinden, som appen visar dem – grinden ger appens bakgrund. */
    @Composable
    private fun InGate(gate: AuthGate, screen: @Composable () -> Unit) = AuthGateHost(gate, signIn = screen, updateRequired = screen) {}

    @Test
    fun `SignInScreen - vila`() = captureLightAndDark("SignInScreen_vila") { SampleSignIn() }

    @Test
    fun `SignInScreen - upptagen`() = captureLightAndDark("SignInScreen_upptagen") { SampleSignIn(busy = true) }

    @Test
    fun `SignInScreen - fel`() {
        // Klockan står still och går en fast tid före varje bild – annars spolar waitForIdle fram tills meddelandet försvunnit.
        rule.mainClock.autoAdvance = false
        rule.captureLightAndDark("SignInScreen_fel", settle = {
            mainClock.advanceTimeBy(SHOWN_MILLIS)
            waitForIdle()
        }) { SampleSignIn(error = remember { sampleOffline() }) }
    }

    @Test
    fun `UpdateRequiredScreen - standard`() = captureLightAndDark("UpdateRequiredScreen_standard") {
        InGate(AuthGate.UpdateRequired) {
            UpdateRequiredScreen(
                message = "Ert hushåll har sparats med en nyare version av ReseApoteket. Installera den senaste versionen för att fortsätta.",
                note = "Ingenting av hushållets data ändras förrän appen är uppdaterad.",
                releasesUrl = "https://github.com/Kodhast/ReseApoteket/releases/latest"
            )
        }
    }

    @Test
    fun `AuthGateHost - laddning`() = captureLightAndDark("AuthGateHost_laddning") {
        AuthGateHost(AuthGate.Loading, signIn = {}, updateRequired = {}) {}
    }

    private companion object {
        /** Inne i bild, långt före meddelandets fyra sekunder – också i den andra bilden. */
        const val SHOWN_MILLIS = 600L
    }
}
