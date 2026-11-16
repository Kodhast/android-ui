package se.kodhast.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.components.testing.Green
import se.kodhast.ui.components.testing.SampleSheet
import se.kodhast.ui.components.testing.TestTheme
import se.kodhast.ui.components.testing.captureLightAndDark
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.Spacing
import se.kodhast.ui.theme.Tone

/** Skärmdumpar (ljust + mörkt) av temats djup (`AppTheme.depth`, android-ui #35) i testpalettens ton. */
@RunWith(RobolectricTestRunner::class)
class DepthScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `AppButton - djup i alla varianter`() = captureLightAndDark("AppButton_djup", depth = true) {
        SampleSheet {
            AppButton("Logga in", {}, Modifier.fillMaxWidth(), icon = R.drawable.ic_add)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                AppButton("Lägg till", {}, variant = ButtonVariant.Secondary)
                AppButton("Visa alla", {}, variant = ButtonVariant.Outlined)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                AppButton("Radera", {}, variant = ButtonVariant.Destructive)
                AppButton("Avbryt", {}, variant = ButtonVariant.Text)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                AppButton("Spara", {}, enabled = false)
                AppButton("Sparar …", {}, loading = true)
                AppButton("Spara", {}, compact = true)
            }
            CompositionLocalProvider(LocalAppButtonStyle provides AppButtonStyle(morph = true)) {
                AppButton("Med formmorf", {})
            }
        }
    }

    @Test
    fun `AppButton - nedtryckt med djup, ljust`() = capturePressed(dark = false)

    @Test
    fun `AppButton - nedtryckt med djup, mörkt`() = capturePressed(dark = true)

    /**
     * Klockan står still: trycket hålls kvar och fjädern har landat när bilden tas. Rippeln är avstängd,
     * eftersom den växer på väggklockan och annars gör bilden olika från körning till körning.
     */
    private fun capturePressed(dark: Boolean) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            TestTheme(darkTheme = dark, depth = true) {
                CompositionLocalProvider(LocalRippleConfiguration provides null) {
                    SampleSheet {
                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                            AppButton("Spara", {})
                            AppButton("Spara", {}, Modifier.testTag("tryckt"))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                            AppButton("Lägg till", {}, variant = ButtonVariant.Secondary)
                            AppButton("Lägg till", {}, Modifier.testTag("tryckt2"), variant = ButtonVariant.Secondary)
                        }
                    }
                }
            }
        }
        rule.onNodeWithTag("tryckt").performTouchInput { down(0, center) }
        rule.onNodeWithTag("tryckt2").performTouchInput { down(1, center) }
        rule.mainClock.advanceTimeBy(SETTLE_MILLIS)
        rule.onRoot().captureRoboImage("AppButton_djup_nedtryckt_${if (dark) "dark" else "light"}.png")
    }

    @Test
    fun `AppCard - djup på kort, bitar och tonat`() = captureLightAndDark("AppCard_djup", depth = true) {
        SampleSheet {
            AppCard(onClick = {}) { ItemRow("Resa till Kreta", subtitle = "12–19 juni · 3 personer", navigates = true) }
            Column3Segments()
            AppCard(tone = Tone.Green) { ItemRow("Allt packat", subtitle = "Inget saknas") }
            val outlined = AppCardStyle(border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant))
            AppCard(style = outlined) { Text("Egen kontur går före kanten", style = AppTheme.typography.body) }
        }
    }

    @Composable
    private fun Column3Segments() {
        Column {
            CardSegment(top = true, bottom = false) { ItemRow("Alvedon 500 mg", subtitle = "18 st") }
            CardSegment(top = false, bottom = false) { ItemRow("Loratadin 10 mg", subtitle = "8 st") }
            CardSegment(top = false, bottom = true) { ItemRow("Skavsårsplåster", subtitle = "1 förp.") }
        }
    }

    @Test
    fun `valda element och verktygsraden med djup`() = captureLightAndDark("Valda_djup", depth = true) {
        SampleSheet {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                AppFilterChip("Alla", selected = true, onClick = {})
                AppFilterChip("Mediciner", selected = false, onClick = {})
                AppFilterChip("Kräm", selected = true, onClick = {}, enabled = false)
            }
            AppFilterChip("Ipren 400 mg", selected = false, onClick = {}, pending = ChipPending(R.drawable.ic_clock, "från 15:40", "för tidigt, nästa dos från 15:40"))
            AppSegmentedChoice(listOf("Ljust", "Mörkt", "Auto"), 0, {})
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                AppIconButton(R.drawable.ic_arrow_back, "Tillbaka", {}, variant = IconButtonVariant.Tonal)
                AppIconButton(R.drawable.ic_archive, "Arkivera", {})
            }
            AppFloatingToolbar(TABS, 1, {}, action = ToolbarAction("Ny resa", R.drawable.ic_add) {})
        }
    }

    private companion object {
        const val SETTLE_MILLIS = 1_000L
        val TABS = listOf(ToolbarItem("Resor", R.drawable.ic_box), ToolbarItem("Idag", R.drawable.ic_clock), ToolbarItem("Lägg till", R.drawable.ic_add))
    }
}
