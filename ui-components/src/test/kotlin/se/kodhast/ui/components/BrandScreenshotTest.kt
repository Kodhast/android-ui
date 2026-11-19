package se.kodhast.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import se.kodhast.ui.components.testing.SampleCard
import se.kodhast.ui.components.testing.SampleSheet
import se.kodhast.ui.components.testing.TestTheme
import se.kodhast.ui.components.testing.captureLightAndDark
import se.kodhast.ui.theme.Spacing

/** Skärmdumpar (ljust + mörkt) av Kodhäst-delarna (android-ui #28) med testtemat. */
@RunWith(RobolectricTestRunner::class)
class BrandScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Composable
    private fun Brand(style: AppBrandStyle, content: @Composable () -> Unit) = CompositionLocalProvider(LocalAppBrandStyle provides style, content = content)

    @Test
    fun `BrandMark - storlekar`() = captureLightAndDark("BrandMark_storlekar") {
        SampleSheet {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.l), verticalAlignment = Alignment.Bottom) {
                BrandMark()
                BrandMark(Modifier.size(SMALL))
                BrandMark(Modifier.size(LARGE))
            }
        }
    }

    @Test
    fun `MakerMark - standard`() = captureLightAndDark("MakerMark_standard") {
        SampleSheet { SampleCard { MakerMark() } }
    }

    @Test
    fun `SectionHeader - prickar`() = captureLightAndDark("SectionHeader_prickar") {
        Brand(AppBrandStyle(sectionDots = true)) {
            SampleSheet {
                SectionHeader("Morgon", count = "1 / 2")
                SectionHeader("Kväll")
                SectionHeader("Mediciner", icon = R.drawable.ic_box, count = "4 / 5")
            }
        }
    }

    @Test
    fun `AppLoading - prickar`() {
        // Prickarna hoppar för evigt: klockan står still och går en fast tid före bilden, mitt i första prickens hopp.
        rule.mainClock.autoAdvance = false
        rule.captureLightAndDark("AppLoading_prickar", settle = {
            mainClock.advanceTimeBy(MID_JUMP)
            waitForIdle()
        }) {
            Brand(AppBrandStyle(dotLoading = true)) {
                Box(Modifier.background(MaterialTheme.colorScheme.background).padding(Spacing.xxl)) { AppLoading() }
            }
        }
    }

    @Test
    fun `AppPullToRefresh - prickar`() {
        rule.mainClock.autoAdvance = false
        rule.captureLightAndDark("AppPullToRefresh_prickar", settle = {
            mainClock.advanceTimeBy(MID_JUMP)
            waitForIdle()
        }) {
            Brand(AppBrandStyle(dotLoading = true)) {
                Box(Modifier.background(MaterialTheme.colorScheme.background).size(160.dp, 120.dp).padding(top = Spacing.m)) {
                    PullDots({ 1f }, Modifier.align(Alignment.TopCenter))
                }
            }
        }
    }

    @Test
    fun `AppButton - laddar med prickar`() {
        rule.mainClock.autoAdvance = false
        rule.captureLightAndDark("AppButton_laddar_prickar", settle = {
            mainClock.advanceTimeBy(MID_JUMP)
            waitForIdle()
        }) {
            Brand(AppBrandStyle(dotLoading = true)) {
                SampleSheet { AppButton("Sparar …", {}, loading = true) }
            }
        }
    }

    @Test
    fun `SectionHeader - arbetar med prickar`() {
        rule.mainClock.autoAdvance = false
        rule.captureLightAndDark("SectionHeader_arbetar_prickar", settle = {
            mainClock.advanceTimeBy(MID_JUMP)
            waitForIdle()
        }) {
            Brand(AppBrandStyle(dotLoading = true)) {
                SampleSheet { SectionHeader("Byten", count = "2 väntar på dig", working = true) }
            }
        }
    }

    @Test
    fun `EmptyState - märke`() = captureLightAndDark("EmptyState_marke") {
        Brand(AppBrandStyle(emptyStateMark = true)) {
            SampleSheet { EmptyState(R.drawable.ic_box, "Inga resor än", "Lägg till din första resa, så räknar appen ut vad som ska med.") }
        }
    }

    // Ordmärkets färger är resurser (values/ och values-night/), inte temat: mörkt läge kräver nattkvalificeraren.
    @Test
    fun `kodhast_branding - ordmärket ljust`() = captureRoboImage("kodhast_branding_ordmarke_light.png") { Wordmark(darkTheme = false) }

    @Test
    @Config(qualifiers = "+night")
    fun `kodhast_branding - ordmärket mörkt`() = captureRoboImage("kodhast_branding_ordmarke_dark.png") { Wordmark(darkTheme = true) }

    @Composable
    private fun Wordmark(darkTheme: Boolean) = TestTheme(darkTheme = darkTheme) {
        SampleSheet { Image(painterResource(R.drawable.kodhast_branding), contentDescription = null) }
    }

    private companion object {
        const val MID_JUMP = 225L
        val SMALL = 24.dp
        val LARGE = 64.dp
    }
}
