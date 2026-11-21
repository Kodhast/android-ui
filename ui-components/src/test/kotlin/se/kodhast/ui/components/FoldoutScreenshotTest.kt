package se.kodhast.ui.components

import androidx.compose.material3.Text
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.components.testing.SampleCard
import se.kodhast.ui.components.testing.SampleRow
import se.kodhast.ui.components.testing.SampleSheet
import se.kodhast.ui.components.testing.captureLightAndDark

/** Skärmdumpar (ljust + mörkt) av de hopfällbara sektionerna med testtemat. */
@RunWith(RobolectricTestRunner::class)
class FoldoutScreenshotTest {

    @Test
    fun `Foldout - lägen`() = captureLightAndDark("Foldout_lagen") {
        SampleSheet {
            SampleCard { Foldout("Anteckning", expanded = false, onToggle = {}, summary = "Sov dåligt, vaknade två gånger") { Text("–") } }
            SampleCard { Foldout("Anteckning", expanded = true, onToggle = {}) { Text("Sov dåligt, vaknade två gånger och var trött hela förmiddagen.") } }
        }
    }

    /** Tonat öppet läge (Husjakt #518): en lista av utfällbara rader, den mellersta öppen. */
    @Test
    fun `Foldout - tonat öppet läge`() = captureLightAndDark("Foldout_tonad") {
        SampleSheet {
            SampleCard {
                Foldout("Läge", expanded = false, onToggle = {}, summary = "Landsbygd, 6 km till affär", tintedWhenOpen = true) { Text("–") }
                Foldout("Uppgifter", expanded = true, onToggle = {}, tintedWhenOpen = true) {
                    ItemRow("Utgångspris", subtitle = "2 450 000 kr")
                    ItemRow("Boarea", subtitle = "142 kvm")
                }
                Foldout("Visning", expanded = false, onToggle = {}, summary = "sön 30 nov 13:00", tintedWhenOpen = true) { Text("–") }
            }
        }
    }

    @Test
    fun `SectionFoldout - lägen med lägg till-knapp`() = captureLightAndDark("SectionFoldout_lagen") {
        SampleSheet {
            SampleCard {
                SectionFoldout(
                    "Recept",
                    expanded = true,
                    onToggle = {},
                    icon = R.drawable.ic_note,
                    count = "2",
                    trailing = { AppIconButton(R.drawable.ic_add, "Lägg till recept", onClick = {}, variant = IconButtonVariant.Tonal) }
                ) {
                    SampleRow("Levaxin", subtitle = "50 µg · 1 morgon")
                    SampleRow("Omeprazol", subtitle = "20 mg · 1 kväll")
                }
            }
            SampleCard {
                SectionFoldout(
                    "Kosttillskott",
                    expanded = false,
                    onToggle = {},
                    count = "1",
                    trailing = { AppIconButton(R.drawable.ic_add, "Lägg till kosttillskott", onClick = {}, variant = IconButtonVariant.Tonal) }
                ) { SampleRow("D-vitamin") }
            }
        }
    }

    @Test
    fun `ExpandableContent - utfälld`() = captureLightAndDark("ExpandableContent_utfalld") {
        SampleSheet { SampleCard { ExpandableContent(visible = true) { SampleRow("Detaljer", subtitle = "Visas när kortet fälls ut") } } }
    }
}
