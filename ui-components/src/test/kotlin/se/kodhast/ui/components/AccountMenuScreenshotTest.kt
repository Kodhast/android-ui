package se.kodhast.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.components.testing.captureScreenLightAndDark
import se.kodhast.ui.components.testing.clickWithoutRipple
import se.kodhast.ui.theme.Spacing

/** Skärmdumpar (ljust + mörkt, med djup) av menyn under avataren (android-ui #102), förankrad uppe till höger som i sidhuvudet. */
@RunWith(RobolectricTestRunner::class)
class AccountMenuScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Composable
    private fun TopEnd(content: @Composable () -> Unit) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(Spacing.s), contentAlignment = Alignment.TopEnd) { content() }
    }

    private val openMenu: ComposeContentTestRule.() -> Unit = {
        onNodeWithContentDescription("Konto och inställningar").clickWithoutRipple()
    }

    @Test
    fun `AccountMenu - inloggad med galleriet`() = rule.captureScreenLightAndDark("AccountMenu_inloggad", depth = true) {
        TopEnd {
            Box {
                AccountAvatar("Anna Berg", onClick = {}, online = true)
                AccountMenu(
                    expanded = true,
                    onDismiss = {},
                    account = AccountInfo("Anna Berg", "anna.berg@example.com"),
                    appName = "ReseApoteket",
                    onOpenSettings = {},
                    onOpenAbout = {},
                    onSignIn = {},
                    onSignOut = {},
                    onOpenGallery = {}
                )
            }
        }
    }

    @Test
    fun `AccountMenuAvatar - utloggad med förklaring`() = rule.captureScreenLightAndDark("AccountMenuAvatar_utloggad", open = openMenu, depth = true) {
        TopEnd {
            AccountMenuAvatar(
                account = null,
                appName = "Fonder",
                onOpenSettings = {},
                onOpenAbout = {},
                onSignIn = {},
                onSignOut = {},
                signInNote = "För säkerhetskopia på Drive"
            )
        }
    }
}
