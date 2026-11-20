package se.kodhast.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.components.testing.Green
import se.kodhast.ui.components.testing.SampleSheet
import se.kodhast.ui.components.testing.SampleSwatches
import se.kodhast.ui.components.testing.TestTheme
import se.kodhast.ui.components.testing.captureLightAndDark
import se.kodhast.ui.components.testing.captureScreenLightAndDark
import se.kodhast.ui.components.testing.clickWithoutRipple
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
    fun `AppButton - nedtryckt med djup, ljust`() = capturePressed("AppButton_djup_nedtryckt", dark = false) { PressedButtons() }

    @Test
    fun `AppButton - nedtryckt med djup, mörkt`() = capturePressed("AppButton_djup_nedtryckt", dark = true) { PressedButtons() }

    @Test
    fun `SwitchControl - nedtryckt med djup, ljust`() = capturePressed("SwitchControl_djup_nedtryckt", dark = false) { PressedSwitches() }

    @Test
    fun `SwitchControl - nedtryckt med djup, mörkt`() = capturePressed("SwitchControl_djup_nedtryckt", dark = true) { PressedSwitches() }

    @Composable
    private fun PressedButtons() {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
            AppButton("Spara", {})
            AppButton("Spara", {}, Modifier.testTag("tryckt"))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
            AppButton("Lägg till", {}, variant = ButtonVariant.Secondary)
            AppButton("Lägg till", {}, Modifier.testTag("tryckt2"), variant = ButtonVariant.Secondary)
        }
    }

    /** Växeln i vila och nedtryckt, och en rad som trycks: knoppen sjunker in också då. */
    @Composable
    private fun PressedSwitches() {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
            SwitchControl(true, onCheckedChange = {}, label = "I vila")
            SwitchControl(true, onCheckedChange = {}, Modifier.testTag("tryckt"), label = "Nedtryckt")
        }
        AppCard { SwitchRow("Aktivt", checked = true, onCheckedChange = {}, Modifier.testTag("tryckt2"), subtitle = "Räknas med i packförslaget") }
    }

    /**
     * Klockan står still: trycket hålls kvar och fjädern har landat när bilden tas. Rippeln är avstängd,
     * eftersom den växer på väggklockan och annars gör bilden olika från körning till körning. [content]
     * märker de två elementen som trycks med `tryckt` och `tryckt2`.
     */
    private fun capturePressed(
        name: String,
        dark: Boolean,
        pressed: () -> List<SemanticsNodeInteraction> = { listOf(rule.onNodeWithTag("tryckt"), rule.onNodeWithTag("tryckt2")) },
        content: @Composable () -> Unit
    ) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            TestTheme(darkTheme = dark, depth = true) {
                CompositionLocalProvider(LocalRippleConfiguration provides null) {
                    SampleSheet { content() }
                }
            }
        }
        pressed().forEachIndexed { pointer, node -> node.performTouchInput { down(pointer, center) } }
        rule.mainClock.advanceTimeBy(SETTLE_MILLIS)
        rule.onRoot().captureRoboImage("${name}_${if (dark) "dark" else "light"}.png")
    }

    @Test
    fun `huvudknappen och kalenderdagen - nedtryckta med djup, ljust`() = capturePressedFabAndDay(dark = false)

    @Test
    fun `huvudknappen och kalenderdagen - nedtryckta med djup, mörkt`() = capturePressedFabAndDay(dark = true)

    /** Huvudknappen och den valda dagen (15) nedtryckta; dagen bredvid (16) i vila. */
    private fun capturePressedFabAndDay(dark: Boolean) = capturePressed(
        "FabOchKalender_djup_nedtryckt",
        dark,
        pressed = { listOf(rule.onNodeWithContentDescription("Ny resa"), rule.onNodeWithContentDescription("15", substring = true)) }
    ) {
        AppCalendar(LocalDate(2026, 6, 1), {}, emptySet(), LocalDate(2026, 6, 15), {})
        AppFloatingToolbar(TABS, 1, {}, action = ToolbarAction("Ny resa", R.drawable.ic_add) {})
    }

    @Test
    fun `fler element med djup`() = captureLightAndDark("Element_djup", depth = true) {
        SampleSheet {
            AppCalendar(LocalDate(2026, 6, 1), {}, setOf(LocalDate(2026, 6, 3)), LocalDate(2026, 6, 15), {}, today = LocalDate(2026, 6, 10))
            AddSplitButton("Lägg till", {}, menu = listOf(AppMenuItem("Från listan", {})).asSplitMenu())
            AppSlider("Energi", 7, {}, "7")
            AppSlider("Humör", -3, {}, "−3", valueRange = -10..10)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                InfoPill("Synkat", onClick = {})
                InfoPill("Etikett")
            }
            StatPill(R.drawable.ic_clock, "7 842", "Steg", Tone.Primary, onClick = {}, action = "Visa")
        }
    }

    @Test
    fun `bottenark med djup`() = rule.captureScreenLightAndDark("AppBottomSheet_djup", depth = true) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            AppBottomSheet("Ändra mängd", onDismiss = {}) {
                QuantityStepper(9, {}, "Antal tabletter")
                AppButton("Spara", {})
            }
        }
    }

    @Test
    fun `datumväljaren med djup`() = rule.captureScreenLightAndDark("DatePicker_djup", open = { onNode(hasClickAction()).clickWithoutRipple() }, depth = true) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(Spacing.l)) {
            DateField("Avresa", LocalDate(2026, 12, 19), {})
        }
    }

    @Test
    fun `FAB-menyn med djup`() = rule.captureScreenLightAndDark("AppFloatingToolbar_fabmeny_djup", open = { onNodeWithContentDescription("Logga").clickWithoutRipple() }, depth = true) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(Spacing.l), contentAlignment = Alignment.BottomCenter) {
            AppFloatingToolbar(
                TABS,
                0,
                {},
                action = ToolbarAction("Logga", R.drawable.ic_add, listOf(AppMenuItem("Mående", {}, icon = R.drawable.ic_person), AppMenuItem("Dos", {}, icon = R.drawable.ic_check))),
                actionMenu = ActionMenuStyle.Fab
            )
        }
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

    @Test
    fun `AppCard - skimmer upptill på kort och översta biten, inte på tonat eller konturerat`() = captureLightAndDark("AppCard_djup_skimmer", depth = true, cardSheen = true) {
        SampleSheet {
            AppCard(onClick = {}) {
                ItemRow("Resa till Kreta", subtitle = "12–19 juni · 3 personer", navigates = true)
                ItemRow("Packlistan", subtitle = "14 av 20 packade")
            }
            Column3Segments()
            AppCard(tone = Tone.Green) { ItemRow("Allt packat", subtitle = "Inget saknas") }
            val outlined = AppCardStyle(border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant))
            AppCard(style = outlined) { Text("Egen kontur: inget skimmer", style = AppTheme.typography.body) }
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

    @Test
    fun `segmentvalet mitt i glidet`() {
        // Klockan står still: valet byts från Ljust till Auto, och bilden tas en fast tid in i glidet.
        rule.mainClock.autoAdvance = false
        rule.captureLightAndDark("AppSegmentedChoice_glid", depth = true, settle = {
            mainClock.advanceTimeBy(GLIDE_MILLIS)
            waitForIdle()
        }) {
            // Om på nytt för varje tema, så att båda bilderna är lika långt in i glidet.
            key(MaterialTheme.colorScheme.background) {
                var selected by remember { mutableIntStateOf(0) }
                LaunchedEffect(Unit) { selected = 2 }
                SampleSheet { AppSegmentedChoice(listOf("Ljust", "Mörkt", "Auto"), selected, { selected = it }) }
            }
        }
    }

    @Test
    fun `kontroller med djup`() = captureLightAndDark("Kontroller_djup", depth = true) {
        SampleSheet {
            AppCard {
                CheckRow("Alvedon 500 mg", checked = false, onCheckedChange = {}, subtitle = "9 tabletter")
                CheckRow("Solkräm", checked = true, onCheckedChange = {}, subtitle = "1 tub")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                QuantityStepper(2, {}, "doser per dag")
                QuantityStepper(0, {}, "tabletter")
            }
            AppCard {
                SwitchRow("Påminn kvällen innan", checked = true, onCheckedChange = {})
                SwitchRow("Visa packade sist", checked = false, onCheckedChange = {})
                SwitchRow("Påminnelse 08:00", checked = true, onCheckedChange = {}, onClick = {})
            }
            ColorSwatchPicker(SampleSwatches.hexes, "#3B7EA1", {}, SampleSwatches::color, { SampleSwatches.label(it) }, SwatchMarker.GridRing)
            EmojiPicker(listOf("🦊", "🐻", "🐰", "🦉"), "🐻", {})
        }
    }

    /** Fältet i fokus: klockan står still, och bilderna tas en hel blinkperiod isär, så att markören syns lika i båda. */
    @Test
    fun `fältet i fokus med djup`() {
        rule.mainClock.autoAdvance = false
        var focused = false
        rule.captureLightAndDark("AppTextField_djup_fokus", depth = true, settle = {
            if (!focused) {
                onNodeWithTag("namn").requestFocus()
                focused = true
                mainClock.advanceTimeBy(FOCUS_MILLIS)
            } else {
                mainClock.advanceTimeBy(BLINK_MILLIS)
            }
        }) {
            SampleSheet {
                AppTextField("Solkräm", {}, "Namn", Modifier.testTag("namn"))
                AppTextField("", {}, "Dos", error = "Fyll i dosen")
                AppTextField("Kreta", {}, "Resmål")
            }
        }
    }

    @Test
    fun `meddelande med djup, ljust`() = captureSnackbar(dark = false)

    @Test
    fun `meddelande med djup, mörkt`() = captureSnackbar(dark = true)

    /** Ett tema per test: ett temabyte i samma komposition slår inte igenom medan meddelandet visas. */
    private fun captureSnackbar(dark: Boolean) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            TestTheme(darkTheme = dark, depth = true) {
                val host = remember { SnackbarHostState() }
                UndoSnackbar(UndoRequest("1", "Solkräm"), host, onUndo = {}, onDismissed = {})
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.BottomCenter) { AppSnackbarHost(host) }
            }
        }
        rule.mainClock.advanceTimeBy(SHOWN_MILLIS)
        rule.onRoot().captureRoboImage("AppSnackbarHost_djup_${if (dark) "dark" else "light"}.png")
    }

    @Test
    fun `meny med djup`() = rule.captureScreenLightAndDark("AppMenu_djup", open = { onNodeWithContentDescription("Fler val").clickWithoutRipple() }, depth = true) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(Spacing.l), contentAlignment = Alignment.TopEnd) {
            AppMenu(listOf(AppMenuItem("Redigera", {}), AppMenuItem("Flytta till resa", {}), AppMenuItem("Radera", {}, R.drawable.ic_remove, destructive = true)))
        }
    }

    @Test
    fun `dialog med djup`() = rule.captureScreenLightAndDark("ConfirmDialog_djup", depth = true) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            DeleteConfirmDialog(DeleteAction("Radera Solkräm?", "Den försvinner från alla resor.") {}, onDismiss = {})
        }
    }

    @Test
    fun `tidsväljaren med djup`() = rule.captureScreenLightAndDark("TimePickerPopup_djup", depth = true) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            TimePickerPopup(LocalTime(8, 0), "Välj tid", {}, {})
        }
    }

    private companion object {
        const val SETTLE_MILLIS = 1_000L
        const val SHOWN_MILLIS = 600L
        const val FOCUS_MILLIS = 250L
        const val BLINK_MILLIS = 1_000L

        /** Segmentvalets glid en bit in (fjädern tar ca 0,4 s). */
        const val GLIDE_MILLIS = 120L
        val TABS = listOf(ToolbarItem("Resor", R.drawable.ic_box), ToolbarItem("Idag", R.drawable.ic_clock), ToolbarItem("Lägg till", R.drawable.ic_add))
    }
}
