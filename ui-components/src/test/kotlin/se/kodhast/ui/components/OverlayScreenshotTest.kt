package se.kodhast.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.components.testing.Blue
import se.kodhast.ui.components.testing.SampleSheet
import se.kodhast.ui.components.testing.captureLightAndDark
import se.kodhast.ui.components.testing.captureScreenLightAndDark
import se.kodhast.ui.components.testing.clickWithoutRipple
import se.kodhast.ui.components.testing.sampleOffline
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.Spacing
import se.kodhast.ui.theme.Tone

/** Skärmdumpar av ark, menyer, dialoger, datumfält, verktygsrad och emojiväljare (ljust + mörkt). */
@RunWith(RobolectricTestRunner::class)
class OverlayScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Composable
    private fun Background(content: @Composable () -> Unit) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(Spacing.l)) { content() }
    }

    @Test
    fun `AppBottomSheet - standard`() = rule.captureScreenLightAndDark("AppBottomSheet_standard") {
        Background {
            AppBottomSheet("Ändra mängd", onDismiss = {}) {
                QuantityStepper(9, {}, "Antal tabletter")
                AppButton("Spara", {})
            }
        }
    }

    @Test
    fun `AppBottomSheet - fast knapprad och fel`() = rule.captureScreenLightAndDark("AppBottomSheet_knapprad") {
        Background {
            AppBottomSheet(
                "Promenad",
                onDismiss = {},
                error = sampleOffline(),
                footer = {
                    AppButton("Avbryt", {}, variant = ButtonVariant.Text)
                    AppButton("Spara", {})
                }
            ) {
                repeat(30) { Text("Rad $it", style = AppTheme.typography.body) }
            }
        }
    }

    @Test
    fun `AppBottomSheet - Släng ändringar`() = rule.captureScreenLightAndDark(
        "AppBottomSheet_slang",
        open = { onNodeWithText("Promenad").performTouchInput { swipeDown(startY = centerY, endY = centerY + SWIPE_PX) } }
    ) {
        Background {
            AppBottomSheet("Promenad", onDismiss = {}, dirty = true) { AppTextField("45", {}, "Minuter") }
        }
    }

    @Test
    fun `AppMenu - öppen med ikoner och radering`() = rule.captureScreenLightAndDark("AppMenu_oppen", open = { onNodeWithContentDescription("Fler val").clickWithoutRipple() }) {
        Background {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopEnd) {
                AppMenu(listOf(AppMenuItem("Arkivera", {}, R.drawable.ic_archive), AppMenuItem("Radera", {}, R.drawable.ic_remove, destructive = true)))
            }
        }
    }

    @Test
    fun `AppMenuPopup - avdelning och kryssrader`() = rule.captureScreenLightAndDark("AppMenuPopup_avdelning_kryss") {
        Background {
            Box {
                AppMenuPopup(
                    listOf(
                        AppMenuItem("Loratadin 10 mg", {}),
                        AppMenuItem("Betapred 0,5 mg", {}, section = "Recept"),
                        AppMenuItem("Levaxin 100 µg", {}, section = "Recept"),
                        AppMenuItem("Mående", {}, section = "Serier", checked = true),
                        AppMenuItem("Sömn", {}, section = "Serier", checked = false),
                        AppMenuItem("Ipren 400 mg", {}, R.drawable.ic_clock, section = "Vid behov", supporting = "För tidigt · från 16:00", supportingTone = Tone.Blue, muted = true)
                    ),
                    expanded = true,
                    onDismiss = {}
                )
            }
        }
    }

    @Test
    fun `AppFloatingToolbar - flikar`() = captureLightAndDark("AppFloatingToolbar_flikar") {
        SampleSheet { AppFloatingToolbar(TABS, 0, {}) }
    }

    @Test
    fun `AppFloatingToolbar - med huvudknapp`() = captureLightAndDark("AppFloatingToolbar_huvudknapp") {
        // Dagboken: plusknappen till höger om flikarna.
        SampleSheet { AppFloatingToolbar(TABS, 1, {}, action = ToolbarAction("Logga", R.drawable.ic_add) {}) }
    }

    @Test
    fun `AppFloatingToolbar - huvudknapp med meny`() = rule.captureScreenLightAndDark(
        "AppFloatingToolbar_huvudknapp_meny",
        open = { onNodeWithContentDescription("Lägg till").clickWithoutRipple() }
    ) {
        // Fonder: plusknappen fäller ut de fyra sätten att lägga till.
        Background {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                AppFloatingToolbar(
                    listOf(ToolbarItem("Översikt", R.drawable.ic_clock), ToolbarItem("Innehav", R.drawable.ic_box), ToolbarItem("Beslut", R.drawable.ic_check)),
                    1,
                    {},
                    action = ToolbarAction(
                        "Lägg till",
                        R.drawable.ic_add,
                        listOf(
                            AppMenuItem("Registrera köp", {}),
                            AppMenuItem("Registrera sälj", {}),
                            AppMenuItem("Importera innehav", {}),
                            AppMenuItem("Importera ordrar", {})
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `AppFloatingToolbar - FAB-meny`() = rule.captureScreenLightAndDark(
        "AppFloatingToolbar_fabmeny",
        open = { onNodeWithContentDescription("Logga").clickWithoutRipple() }
    ) {
        // Dagboken: plusknappen fäller ut loggvalen som kapslar, och blir ett kryss.
        Background {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                Text("Mående · Morgon · 7 av 10", Modifier.align(Alignment.TopStart), color = MaterialTheme.colorScheme.onBackground)
                AppFloatingToolbar(
                    listOf(ToolbarItem("Idag", R.drawable.ic_clock), ToolbarItem("Dagbok", R.drawable.ic_note), ToolbarItem("Mediciner", R.drawable.ic_box)),
                    0,
                    {},
                    action = ToolbarAction(
                        "Logga",
                        R.drawable.ic_add,
                        listOf(
                            AppMenuItem("Mående", {}, icon = R.drawable.ic_person),
                            AppMenuItem("Aktivitet", {}, icon = R.drawable.ic_clock),
                            AppMenuItem("Dos", {}, icon = R.drawable.ic_check),
                            AppMenuItem("Händelse", {}, icon = R.drawable.ic_calendar),
                            AppMenuItem("Sjukdom utan ikon och med ett långt namn som kapas i kanten", {})
                        )
                    ),
                    style = ToolbarStyle.Tonal,
                    actionMenu = ActionMenuStyle.Fab
                )
            }
        }
    }

    @Test
    fun `AppFloatingToolbar - FAB-meny i RTL`() = rule.captureScreenLightAndDark(
        "AppFloatingToolbar_fabmeny_rtl",
        open = { onNodeWithContentDescription("Lägg till").clickWithoutRipple() }
    ) {
        // Speglad: knappen står till vänster och valen linjerar med dess vänsterkant.
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Background {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                    AppFloatingToolbar(
                        listOf(ToolbarItem("Översikt", R.drawable.ic_clock), ToolbarItem("Innehav", R.drawable.ic_box), ToolbarItem("Beslut", R.drawable.ic_check)),
                        0,
                        {},
                        action = ToolbarAction(
                            "Lägg till",
                            R.drawable.ic_add,
                            listOf(
                                AppMenuItem("Ny transaktion", {}, icon = R.drawable.ic_edit),
                                AppMenuItem("Importera avräkningsnota", {}, icon = R.drawable.ic_note),
                                AppMenuItem("Importera innehav", {}, icon = R.drawable.ic_box),
                                AppMenuItem("Bevaka ny fond", {}, icon = R.drawable.ic_check)
                            )
                        ),
                        style = ToolbarStyle.Tonal,
                        actionMenu = ActionMenuStyle.Fab
                    )
                }
            }
        }
    }

    @Test
    fun `ConfirmDialog - bekräftelse`() = rule.captureScreenLightAndDark("ConfirmDialog_standard") {
        Background { ConfirmDialog("Räkna om listan?", "Det du ändrat för hand ersätts.", "Räkna om", {}, {}, dismissLabel = "Behåll listan") }
    }

    @Test
    fun `ConfirmDialog - innehåll och bara en knapp`() = rule.captureScreenLightAndDark("ConfirmDialog_innehall") {
        Background {
            ConfirmDialog("Avsluta förkylningen?", "Välj när den tog slut.", "Avsluta", {}, {}) {
                DateField("Slutdatum", LocalDate(2026, 10, 4), {})
            }
        }
    }

    @Test
    fun `ConfirmDialog - bara att läsa`() = rule.captureScreenLightAndDark("ConfirmDialog_las") {
        Background { ConfirmDialog("Levaxin", "Tas på fastande mage.", "Stäng", {}, {}, dismissLabel = null) }
    }

    @Test
    fun `DeleteConfirmDialog - radera`() = rule.captureScreenLightAndDark("DeleteConfirmDialog_radera") {
        Background { DeleteConfirmDialog(DeleteAction("Radera Alvedon?", "Receptet tas bort för gott. Det går inte att ångra.") {}, onDismiss = {}) }
    }

    @Test
    fun `DiscardChangesDialog - fråga`() = rule.captureScreenLightAndDark("DiscardChangesDialog_fraga") {
        Background { DiscardChangesDialog({}, {}) }
    }

    @Test
    fun `DateField - lägen`() = captureLightAndDark("DateField_lagen") {
        SampleSheet {
            DateField("Avresa", LocalDate(2026, 12, 19), {})
            DateField("Hemresa", null, {}, error = "Välj ett datum")
            // Dagboken: tomt betyder något, och ett satt datum går att rensa.
            DateField("Slutdatum", null, {}, emptyLabel = "Periodens slut", onClear = {})
            DateField("Slutdatum", LocalDate(2026, 10, 4), {}, emptyLabel = "Periodens slut", onClear = {})
        }
    }

    @Test
    fun `EmojiPicker - val`() = captureLightAndDark("EmojiPicker_val") {
        SampleSheet { EmojiPicker("🐻", {}, listOf("🦊", "🐻", "🐰", "🦉", "🐯", "🐢", "🦄", "🐧", "🐼", "🐨")) }
    }

    private companion object {
        val TABS = listOf(ToolbarItem("Resor", R.drawable.ic_box), ToolbarItem("Idag", R.drawable.ic_clock), ToolbarItem("Lägg till", R.drawable.ic_add))
        const val SWIPE_PX = 1500f
    }
}
