package se.kodhast.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.components.testing.Blue
import se.kodhast.ui.components.testing.Green
import se.kodhast.ui.components.testing.SampleSheet
import se.kodhast.ui.components.testing.captureLightAndDark
import se.kodhast.ui.components.testing.captureScreenLightAndDark
import se.kodhast.ui.components.testing.clickWithoutRipple
import se.kodhast.ui.theme.Spacing
import se.kodhast.ui.theme.Tone

/** Skärmdumpar av inställningarnas delar (ljust + mörkt), med påhittad person. */
@RunWith(RobolectricTestRunner::class)
class SettingsScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Composable
    private fun Sample(content: @Composable () -> Unit) = Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.m)) { content() }

    @Test
    fun `AccountRow - standard`() = captureLightAndDark("AccountRow_standard") {
        Sample {
            AppCard {
                AccountRow("Anna Lind", "anna.lind@exempel.se", photoUrl = null)
                AppDivider()
                AccountRow("Anna Lind", email = null, photoUrl = null)
            }
        }
    }

    @Test
    fun `SignOutRow - standard`() = captureLightAndDark("SignOutRow_standard") {
        Sample {
            AppCard {
                AccountRow("Anna Lind", "anna.lind@exempel.se", photoUrl = null)
                AppDivider()
                SignOutRow("ReseApoteket", {})
            }
        }
    }

    @Test
    fun `SignOutRow - fråga`() = rule.captureScreenLightAndDark("SignOutRow_fraga", open = { onNodeWithText("Logga ut").clickWithoutRipple() }) {
        Sample { AppCard { SignOutRow("ReseApoteket", {}) } }
    }

    @Test
    fun `AboutCard - standard`() = captureLightAndDark("AboutCard_standard") {
        Sample {
            AboutCard("ReseApoteket", "0.12.0") {
                AppDivider()
                ItemRow("Läkemedelslista", subtitle = "Läkemedelsverket, tis 3 nov 2026 · CC BY 4.0")
            }
        }
    }

    @Test
    fun `NoticeBanner - med djup`() = captureLightAndDark("NoticeBanner_djup", depth = true) {
        // Kortets klick (#73): med temats djup har det klickbara meddelandet kortets skugga, som förut klipptes bort.
        SampleSheet {
            NoticeBanner("Notiser är avstängda", R.drawable.ic_clock, {}, detail = "Påminnelserna kan inte visas.")
            NoticeBanner("Health Connect kopplad", R.drawable.ic_clock, onClick = null, tone = Tone.Green)
        }
    }

    @Test
    fun `NoticeBanner - lägen`() = captureLightAndDark("NoticeBanner_lagen") {
        Sample {
            NoticeBanner("Notiser är avstängda", R.drawable.ic_clock, {}, detail = "Påminnelserna kan inte visas. Tryck för att slå på notiser för ReseApoteket.")
            NoticeBanner("Health Connect kopplad", R.drawable.ic_clock, onClick = null, tone = Tone.Green, detail = "Steg, puls och sömn visas här.")
            NoticeBanner("Health Connect saknas", R.drawable.ic_download, {}, tone = Tone.Blue, detail = "Installera Health Connect från Play Butik.", action = "Installera")
        }
    }
}
