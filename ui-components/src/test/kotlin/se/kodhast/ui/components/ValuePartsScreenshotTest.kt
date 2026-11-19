package se.kodhast.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureScreenRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.components.testing.SampleSheet
import se.kodhast.ui.components.testing.TestTheme
import se.kodhast.ui.components.testing.captureLightAndDark
import se.kodhast.ui.components.testing.captureScreenLightAndDark
import se.kodhast.ui.components.testing.clickWithoutRipple
import se.kodhast.ui.theme.Spacing
import se.kodhast.ui.theme.Tone

/** Skärmdumpar av värderaden, andelsraden, rullgardinsfältet, väntedialogen och toppradens prickar (ljust + mörkt), med påhittad portfölj. */
@RunWith(RobolectricTestRunner::class)
class ValuePartsScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Composable
    private fun Brand(style: AppBrandStyle, content: @Composable () -> Unit) = CompositionLocalProvider(LocalAppBrandStyle provides style, content = content)

    @Composable
    private fun Background(content: @Composable () -> Unit) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(Spacing.l)) { content() }
    }

    @Test
    fun `ValueRow - lägen`() = captureLightAndDark("ValueRow_lagen") {
        SampleSheet {
            AppCard {
                ValueRow("Totalresultat", "+8,6 % · +32 860 kr", valueTone = Tone.Primary)
                ValueRow("Varav orealiserat", "+21 400 kr", muted = true)
                ValueRow("Schablonskatt i år", "ca −3 000 kr", supportingLabel = "0,888 % på kapitalunderlaget", valueMuted = true)
                ValueRow("Årsavkastning", "Visas efter ett år", valueMuted = true)
                ValueRow("Träffar", "3 av 5 bättre", supportingValue = "+12 400 kr\nmedian +2,1 %", valueColor = MaterialTheme.colorScheme.error)
                ValueRow("Handelsbanken Amerika Småbolag Tema Criteria A1 SEK", "1,46 %")
            }
        }
    }

    @Test
    fun `ShareBar - andelar och mål`() = captureLightAndDark("ShareBar_andelar") {
        SampleSheet {
            AppCard {
                ShareBar("Nivå 4", 0.52f, "52,0 %", target = 0.45f, targetValue = "45,0 %")
                ShareBar("Nivå 6", 0f, "0,0 %", target = 0.1f, targetValue = "10,0 %")
                ShareBar("Sverige", 0.4f, "40,0 %")
                ShareBar("Övrigt", 0.08f, "8,0 %", color = MaterialTheme.colorScheme.outline)
            }
        }
    }

    @Test
    fun `SelectField - lägen`() = captureLightAndDark("SelectField_lagen") {
        SampleSheet {
            SelectField("Fond", FUNDS, "Global Indexfond A", { it }, {})
            SelectField("Fondbolag", FUNDS, null, { it }, {}, placeholder = "Alla fondbolag", supportingText = "Visar fonder från alla bolag")
            SelectField("Byt till", FUNDS, "Sverige Småbolag", { it }, {}, enabled = false)
            SelectField("Fond", FUNDS, null, { it }, {}, error = "Välj en fond")
        }
    }

    @Test
    fun `SelectField - meny`() = rule.captureScreenLightAndDark("SelectField_meny", open = { onNodeWithContentDescription("Fond, Global Indexfond A").clickWithoutRipple() }) {
        Background { SelectField("Fond", FUNDS, "Global Indexfond A", { it }, {}, clearOptionLabel = "Alla fonder") }
    }

    @OptIn(ExperimentalRoborazziApi::class)
    @Test
    fun `LoadingDialog - standard`() {
        // Indikatorn rör sig för evigt: klockan står still, och varje tema ritas med en bildruta, så att
        // bilden blir densamma varje gång (dialogens fönster ritas om först när klockan går).
        rule.mainClock.autoAdvance = false
        var dark by mutableStateOf(false)
        rule.setContent { TestTheme(darkTheme = dark) { Background { LoadingDialog("Läser avräkningsnotan", "Det tar några sekunder.") } } }
        for ((suffix, isDark) in listOf("light" to false, "dark" to true)) {
            dark = isDark
            Snapshot.sendApplyNotifications()
            rule.mainClock.advanceTimeByFrame()
            captureScreenRoboImage("LoadingDialog_standard_$suffix.png")
        }
    }

    @Test
    fun `AppTopBar - prickar med headerDots`() = captureLightAndDark("AppTopBar_prickar") {
        Brand(AppBrandStyle(headerDots = true)) {
            SampleSheet {
                AppTopBar("Global Indexfond A", size = TopBarSize.Small, onBack = {})
                AppTopBar("Facit", onBack = {})
            }
        }
    }

    @Test
    fun `AppTopBar - prickarna hoppar medan något hämtas`() {
        rule.mainClock.autoAdvance = false
        rule.captureLightAndDark("AppTopBar_prickar_hamtar", settle = {
            mainClock.advanceTimeBy(MID_JUMP)
            waitForIdle()
        }) {
            Brand(AppBrandStyle(headerDots = true)) {
                SampleSheet { AppTopBar("Global Indexfond A", size = TopBarSize.Small, onBack = {}, syncing = true) }
            }
        }
    }

    private companion object {
        val FUNDS = listOf("Global Indexfond A", "Sverige Småbolag", "Räntefond Kort")

        /** Mitt i första prickens hopp (se BrandScreenshotTest). */
        const val MID_JUMP = 225L
    }
}
