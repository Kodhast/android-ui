package se.kodhast.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.components.testing.TestTheme

/** Menyn under avataren (android-ui #102): fast ordning, inloggad och utloggad, utloggningsfrågan, stängning. */
@RunWith(RobolectricTestRunner::class)
class AccountMenuBehaviorTest {

    @get:Rule
    val rule = createComposeRule()

    private val anna = AccountInfo("Anna Berg", "anna.berg@example.com")

    private fun show(content: @Composable () -> Unit) = rule.setContent { TestTheme(content = content) }

    private var settings = 0
    private var about = 0
    private var gallery = 0
    private var signIns = 0
    private var signOuts = 0

    @Composable
    private fun Avatar(account: AccountInfo?, gallery: Boolean = true, signIn: Boolean = true, signInNote: String? = null) = AccountMenuAvatar(
        account = account,
        appName = "Dagboken",
        onOpenSettings = { settings++ },
        onOpenAbout = { about++ },
        onSignIn = if (signIn) ({ signIns++ }) else null,
        onSignOut = { signOuts++ },
        signInNote = signInNote,
        onOpenGallery = if (gallery) ({ this.gallery++ }) else null
    )

    private fun open() = rule.onNodeWithContentDescription("Konto och inställningar").performClick()

    private fun top(text: String) = rule.onNodeWithText(text).getBoundsInRoot().top

    @Test
    fun `inloggad står kontot överst, sedan Inställningar, galleriet, Om och Logga ut sist`() {
        show { Avatar(anna) }
        rule.onNodeWithText("Inställningar").assertDoesNotExist()
        open()
        val order = listOf("Anna Berg", "anna.berg@example.com", "Inställningar", "Komponentgalleri", "Om Dagboken", "Logga ut").map(::top)
        assertEquals(order.sorted(), order)
        rule.onNodeWithText("Logga in med Google").assertDoesNotExist()
    }

    @Test
    fun `utloggad finns ingen kontorad, och Logga in med Google står sist med förklaringen`() {
        show { Avatar(null, gallery = false, signInNote = "För säkerhetskopia på Drive") }
        open()
        rule.onNodeWithText("Komponentgalleri").assertDoesNotExist()
        rule.onNodeWithText("Logga ut").assertDoesNotExist()
        rule.onNodeWithText("För säkerhetskopia på Drive").assertIsDisplayed()
        assertTrue(top("Om Dagboken") < top("Logga in med Google"))
        rule.onNodeWithText("Logga in med Google").performClick()
        assertEquals(1, signIns)
        rule.onNodeWithText("Inställningar").assertDoesNotExist()
    }

    @Test
    fun `utan onSignIn står ingen inloggningsrad`() {
        show { Avatar(null, signIn = false) }
        open()
        rule.onNodeWithText("Om Dagboken").assertIsDisplayed()
        rule.onNodeWithText("Logga in med Google").assertDoesNotExist()
    }

    @Test
    fun `raderna anropar sitt val och stänger menyn`() {
        show { Avatar(anna) }
        open()
        rule.onNodeWithText("Inställningar").performClick()
        rule.onNodeWithText("Om Dagboken").assertDoesNotExist()
        open()
        rule.onNodeWithText("Komponentgalleri").performClick()
        open()
        rule.onNodeWithText("Om Dagboken").performClick()
        assertEquals(listOf(1, 1, 1), listOf(settings, gallery, about))
    }

    @Test
    fun `Logga ut frågar först, Avbryt gör ingenting och Logga ut i frågan loggar ut`() {
        show { Avatar(anna) }
        open()
        rule.onNodeWithText("Logga ut").performClick()
        rule.onNodeWithText("Logga ut?").assertIsDisplayed()
        rule.onNodeWithText("Avbryt").performClick()
        assertEquals(0, signOuts)
        open()
        rule.onNodeWithText("Logga ut").performClick()
        // Menyn är stängd: det enda "Logga ut" som står kvar är frågans knapp.
        rule.onNodeWithText("Logga ut").performClick()
        assertEquals(1, signOuts)
        rule.onNodeWithText("Logga ut?").assertDoesNotExist()
    }

    @Test
    fun `bakåt stänger menyn`() {
        show { Avatar(anna) }
        open()
        rule.onNodeWithText("Inställningar").assertIsDisplayed()
        Espresso.pressBack()
        rule.onNodeWithText("Inställningar").assertDoesNotExist()
    }

    @Test
    fun `AccountMenu utan avatar visas när expanded är satt och onDismiss anropas av ett val`() {
        var dismissed = 0
        show {
            AccountMenu(
                expanded = true,
                onDismiss = { dismissed++ },
                account = null,
                appName = "Fonder",
                onOpenSettings = { settings++ },
                onOpenAbout = {},
                onSignIn = null,
                onSignOut = {}
            )
        }
        rule.onNodeWithText("Om Fonder").assertIsDisplayed()
        rule.onNodeWithText("Inställningar").performClick()
        assertEquals(1, settings)
        assertEquals(1, dismissed)
    }
}
