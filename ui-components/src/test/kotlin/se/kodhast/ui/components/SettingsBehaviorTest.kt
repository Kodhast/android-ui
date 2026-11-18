package se.kodhast.ui.components

import android.Manifest
import android.app.Application
import android.provider.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import se.kodhast.ui.components.testing.TestTheme

/** Inställningarnas delar (android-ui #54): kontoraden, Logga ut, Om, NoticeBanner och notisbehörigheten. */
@RunWith(RobolectricTestRunner::class)
class SettingsBehaviorTest {

    @get:Rule
    val rule = createComposeRule()

    private fun show(content: @Composable () -> Unit) = rule.setContent { TestTheme(content = content) }

    @Test
    fun `kontoraden visar namn och e-post`() {
        show { AccountRow("Anna Lind", "anna.lind@exempel.se", photoUrl = null) }
        rule.onNodeWithText("Anna Lind").assertIsDisplayed()
        rule.onNodeWithText("anna.lind@exempel.se").assertIsDisplayed()
        rule.onNodeWithText("Inloggad med Google").assertDoesNotExist()
    }

    @Test
    fun `kontoraden utan e-post säger Inloggad med Google`() {
        show { AccountRow("Anna Lind", email = null, photoUrl = null) }
        rule.onNodeWithText("Anna Lind").assertIsDisplayed()
        rule.onNodeWithText("Inloggad med Google").assertIsDisplayed()
    }

    @Test
    fun `kontoraden utan namn har e-posten som titel och ingen upprepning`() {
        show { AccountRow(name = null, email = "anna.lind@exempel.se", photoUrl = null) }
        rule.onAllNodesWithText("anna.lind@exempel.se").assertCountEquals(1)
        rule.onNodeWithText("Inloggad med Google").assertIsDisplayed()
    }

    @Test
    fun `kontoraden utan namn och e-post säger Inloggad med Google en gång`() {
        show { AccountRow(name = null, email = null, photoUrl = null) }
        rule.onAllNodesWithText("Inloggad med Google").assertCountEquals(1)
    }

    @Test
    fun `Logga ut frågar först och loggar ut efter bekräftelsen`() {
        var signedOut = 0
        show { SignOutRow({ signedOut++ }) }
        rule.onNodeWithText("Logga ut").performClick()
        rule.onNodeWithText("Logga ut?").assertIsDisplayed()
        rule.onNodeWithText("Det du sparat finns kvar i ditt konto och kommer tillbaka när du loggar in igen.").assertIsDisplayed()
        assertEquals(0, signedOut)
        // Raden och bekräftelseknappen heter båda "Logga ut"; knappen ligger i dialogen, sist.
        rule.onAllNodesWithText("Logga ut").let { it[it.fetchSemanticsNodes().size - 1] }.performClick()
        assertEquals(1, signedOut)
        rule.onNodeWithText("Logga ut?").assertDoesNotExist()
    }

    @Test
    fun `Avbryt i frågan loggar inte ut`() {
        var signedOut = 0
        show { SignOutRow({ signedOut++ }, message = "Hushållets data finns kvar i molnet.") }
        rule.onNodeWithText("Logga ut").performClick()
        rule.onNodeWithText("Hushållets data finns kvar i molnet.").assertIsDisplayed()
        rule.onNodeWithText("Avbryt").performClick()
        rule.onNodeWithText("Logga ut?").assertDoesNotExist()
        assertEquals(0, signedOut)
    }

    @Test
    fun `Om-kortet visar appen, versionen, signaturen och appens rader`() {
        show { AboutCard("ReseApoteket", "0.12.0") { Text("Läkemedelslista") } }
        rule.onNodeWithText("ReseApoteket").assertIsDisplayed()
        rule.onNodeWithText("Version 0.12.0").assertIsDisplayed()
        rule.onNodeWithText("En kodhäst-app").assertIsDisplayed()
        rule.onNodeWithText("Läkemedelslista").assertIsDisplayed()
    }

    @Test
    fun `NoticeBanner är en knapp som öppnar det meddelandet gäller`() {
        var opened = 0
        show { NoticeBanner("Notiser är avstängda", R.drawable.ic_clock, { opened++ }) }
        rule.onNodeWithText("Notiser är avstängda")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .performClick()
        assertEquals(1, opened)
    }

    @Test
    fun `NoticeBanner utan onClick är bara ett meddelande med sin förklaring – ingen knapp`() {
        show { NoticeBanner("Health Connect saknas", R.drawable.ic_clock, onClick = null, detail = "Klockans data visas när Health Connect är kopplat.") }
        rule.onNodeWithText("Health Connect saknas").assertIsDisplayed().assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Role))
        rule.onNodeWithText("Klockans data visas när Health Connect är kopplat.").assertIsDisplayed()
        rule.onNode(hasClickAction()).assertDoesNotExist()
    }

    @Test
    fun `NoticeBanner med åtgärd har knappen under texten – kortet självt är ingen knapp`() {
        var opened = 0
        show {
            NoticeBanner(
                "Health Connect saknas",
                R.drawable.ic_clock,
                { opened++ },
                detail = "Installera Health Connect från Play Butik för att se klockans data.",
                action = "Installera"
            )
        }
        rule.onNodeWithText("Health Connect saknas").assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Role))
        rule.onAllNodes(hasClickAction()).assertCountEquals(1)
        rule.onNodeWithText("Installera").assertHeightIsAtLeast(48.dp).performClick()
        assertEquals(1, opened)
    }

    @Test
    fun `NoticeBanner med åtgärd men utan onClick fäller`() {
        assertFailsWith<IllegalArgumentException> {
            show { NoticeBanner("Health Connect saknas", R.drawable.ic_clock, onClick = null, action = "Installera") }
        }
    }

    @Composable
    private fun AccessProbe(onAccess: (NotificationAccess) -> Unit = {}) {
        val access = rememberNotificationAccess()
        onAccess(access)
        Text(if (access.allowed) "tillåtet" else "avstängt")
    }

    @Test
    fun `notisbehörigheten nekad ger avstängt`() {
        shadowOf(app()).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)
        show { AccessProbe() }
        rule.onNodeWithText("avstängt").assertExists()
    }

    @Test
    fun `notisbehörigheten beviljad ger tillåtet`() {
        shadowOf(app()).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        show { AccessProbe() }
        rule.onNodeWithText("tillåtet").assertExists()
    }

    @Test
    fun `avstängda notiser i systemet ger avstängt trots behörigheten`() {
        shadowOf(app()).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        shadowOf(app().getSystemService(android.app.NotificationManager::class.java)).setNotificationsEnabled(false)
        show { AccessProbe() }
        rule.onNodeWithText("avstängt").assertExists()
    }

    @Test
    fun `openSettings öppnar appens notisinställningar`() {
        var access: NotificationAccess? = null
        show { AccessProbe { access = it } }
        rule.runOnIdle { checkNotNull(access).openSettings() }
        val started = shadowOf(app()).nextStartedActivity
        assertEquals(Settings.ACTION_APP_NOTIFICATION_SETTINGS, started.action)
        assertEquals(app().packageName, started.getStringExtra(Settings.EXTRA_APP_PACKAGE))
    }

    private fun app(): Application = RuntimeEnvironment.getApplication()
}
