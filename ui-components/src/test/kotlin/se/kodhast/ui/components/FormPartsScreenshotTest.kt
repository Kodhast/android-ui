package se.kodhast.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.common.EditorEvents
import se.kodhast.ui.common.EditorUiState
import se.kodhast.ui.components.testing.Blue
import se.kodhast.ui.components.testing.SampleCard
import se.kodhast.ui.components.testing.SampleRow
import se.kodhast.ui.components.testing.SampleSheet
import se.kodhast.ui.components.testing.captureLightAndDark
import se.kodhast.ui.components.testing.captureScreenLightAndDark
import se.kodhast.ui.theme.Tone

/** Skärmdumpar (ljust + mörkt) av ReseApotekets generella delar (android-ui #75), med påhittade värden. */
@RunWith(RobolectricTestRunner::class)
class FormPartsScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `QuantityStepper - med etikett`() = captureLightAndDark("QuantityStepper_etikett") {
        SampleSheet {
            QuantityStepper(2, {}, "doser per dag", range = 1..24, showLabel = true)
            QuantityStepper(1, {}, "tabletter per dos", range = 1..99, showLabel = true)
        }
    }

    @Test
    fun `ValueTile - med och utan förklaring`() = captureLightAndDark("ValueTile_varianter") {
        SampleSheet {
            ValueTile("För en 7-dagarsresa", "18 tabletter · 2 kartor", supporting = "2 × 1 tablett/dag × (7 + 2) dagar", tone = Tone.Blue)
            ValueTile("Förslag", "1 tub")
        }
    }

    @Test
    fun `UnknownValueNotice - standard`() = captureLightAndDark("UnknownValueNotice_standard") {
        SampleSheet { UnknownValueNotice() }
    }

    @Test
    fun `PillRow - etiketter sist i en rad`() = captureLightAndDark("PillRow_standard") {
        SampleSheet {
            SampleCard {
                ItemRow(
                    "Levaxin 50 µg",
                    subtitle = "9 tabletter",
                    trailing = {
                        PillRow {
                            InfoPill("1 morgon", tone = Tone.Blue)
                            InfoPill("07:30")
                        }
                    }
                )
            }
        }
    }

    @Test
    fun `SettingsPage - med kort och fel`() = captureLightAndDark("SettingsPage_standard") {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            SettingsPage("Inställningar", onBack = {}) {
                GroupLabel("Konto")
                SettingsCard(error = "Ingen anslutning just nu. Försök igen.") {
                    SampleRow("Anna Lind", subtitle = "anna.lind@exempel.se")
                }
                GroupLabel("Packlista")
                SettingsCard { SwitchRow("Flytta avprickade sist", checked = true, onCheckedChange = {}) }
            }
        }
    }

    @Test
    fun `SettingsCard - utan och med fel`() = captureLightAndDark("SettingsCard_lagen") {
        SampleSheet {
            SettingsCard { SampleRow("Exportera", subtitle = "Spara hushållet som en fil") }
            SettingsCard(error = "Kunde inte spara filen.") { SampleRow("Exportera", subtitle = "Spara hushållet som en fil") }
        }
    }

    @Test
    fun `AppBottomSheet - underrubrik`() = rule.captureScreenLightAndDark("AppBottomSheet_underrubrik") {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            AppBottomSheet("Levaxin 50 µg", onDismiss = {}, subtitle = "Anna · 1 tablett dagligen") {
                QuantityStepper(9, {}, "tabletter")
            }
        }
    }

    @Test
    fun `EntityEditScreen - huvud`() = captureLightAndDark("EntityEditScreen_huvud") {
        EntityEditScreen("Ändra person", EditorUiState("Anna"), EditorEvents.None, {}, {}, header = { AccountAvatar("Anna Lind", onClick = null) }) {
            AppTextField("Anna", {}, "Namn")
        }
    }

    @Test
    fun `SectionFoldout - tom`() = captureLightAndDark("SectionFoldout_tom") {
        SampleSheet {
            SampleCard {
                SectionFoldout(
                    "Kosttillskott",
                    expanded = true,
                    onToggle = {},
                    count = "0",
                    emptyText = "Inga kosttillskott än",
                    trailing = { AppIconButton(R.drawable.ic_add, "Lägg till kosttillskott", onClick = {}, variant = IconButtonVariant.Tonal) }
                ) {}
            }
        }
    }
}
