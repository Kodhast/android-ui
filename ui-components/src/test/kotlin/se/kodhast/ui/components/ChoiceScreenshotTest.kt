package se.kodhast.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.components.testing.SampleSheet
import se.kodhast.ui.components.testing.SampleSwatches
import se.kodhast.ui.components.testing.captureLightAndDark
import se.kodhast.ui.theme.AppTheme

/** Skärmdumpar av valen (ljust + mörkt); där apparna skiljer sig syns båda varianterna. */
@RunWith(RobolectricTestRunner::class)
class ChoiceScreenshotTest {

    @Test
    fun `AppFilterChip - lägen`() = captureLightAndDark("AppFilterChip_lagen") {
        SampleSheet {
            ChipRow {
                AppFilterChip("Tablett", selected = true, onClick = {})
                AppFilterChip("Kapsel", selected = false, onClick = {})
                AppFilterChip("Plåster", selected = false, onClick = {}, icon = R.drawable.ic_add)
                AppFilterChip("Kräm", selected = false, onClick = {}, enabled = false)
            }
        }
    }

    @Test
    fun `AppFilterChip - värde, framhävt och väntande`() = captureLightAndDark("AppFilterChip_varde_vantande") {
        SampleSheet {
            ChipRow {
                AppFilterChip("Huvudvärk", selected = true, onClick = {}, value = "4", emphasized = true)
                AppFilterChip("Illamående", selected = true, onClick = {}, value = "2")
                AppFilterChip("Yrsel", selected = false, onClick = {})
                AppFilterChip("Ipren 400 mg", selected = false, onClick = {}, pending = ChipPending(R.drawable.ic_clock, "från 15:40", "för tidigt, nästa dos från 15:40"))
            }
        }
    }

    @Test
    fun `AppFilterChip - eget ledande element, valt och inte`() = captureLightAndDark("AppFilterChip_ledande") {
        SampleSheet {
            ChipRow {
                AppFilterChip("Anna", selected = true, onClick = {}, leading = { LeadingDot(MaterialTheme.colorScheme.tertiary) })
                AppFilterChip("Erik", selected = false, onClick = {}, leading = { LeadingDot(MaterialTheme.colorScheme.primary) })
            }
        }
    }

    @Test
    fun `AppFilterChip - långt namn, vanligt och väntande`() = captureLightAndDark("AppFilterChip_langt_namn") {
        val long = "Ryaltris 25 mikrogram/dos + 600 mikrogram/dos 2 st"
        SampleSheet {
            ChipRow { AppFilterChip(long, selected = false, onClick = {}) }
            ChipRow { AppFilterChip(long, selected = true, onClick = {}, pending = ChipPending(R.drawable.ic_check, "tagen 06:37", "tagen 06:37")) }
            ChipRow { AppFilterChip(long, selected = false, onClick = {}, pending = ChipPending(R.drawable.ic_clock, "från 15:40", "för tidigt, nästa dos från 15:40")) }
        }
    }

    @Test
    fun `ChoiceChips - ett val och flera val`() = captureLightAndDark("ChoiceChips_ett_och_flera") {
        SampleSheet {
            ChoiceChips(listOf("Tablett", "Kapsel", "Kräm", "Droppar"), "Kapsel", {}, label = { it })
            ChoiceChips(
                listOf("Spänd käke", "Hjärtklappning", "Klump i magen", "Tung i bröstet", "Rastlösa ben"),
                selected = setOf("Hjärtklappning", "Klump i magen"),
                onToggle = {},
                label = { it }
            )
        }
    }

    @Test
    fun `ExampleChips - exempel`() = captureLightAndDark("ExampleChips_exempel") {
        SampleSheet { ExampleChips(listOf("Solkräm", "Plåster", "Myggmedel", "Resorb"), {}, chosen = "Plåster") }
    }

    @Test
    fun `ChoiceGrid - egen markering och ring`() = captureLightAndDark("ChoiceGrid_markering") {
        val emojis = listOf("🦊", "🐻", "🐰", "🦉", "🐯", "🐢", "🦄", "🐧", "🐼", "🐨")
        SampleSheet {
            // ReseApoteket: cellen ritar sin egen markering (emojiväljaren).
            ChoiceGrid(emojis, "🐻", {}, label = { it }) { emoji, selected ->
                val marker = if (selected) {
                    Modifier.background(MaterialTheme.colorScheme.primaryContainer, AppTheme.shapes.pill).border(2.5.dp, MaterialTheme.colorScheme.primary, AppTheme.shapes.pill)
                } else {
                    Modifier
                }
                Box(marker.fillMaxSize(), contentAlignment = Alignment.Center) { Text(emoji, style = AppTheme.typography.sectionTitle) }
            }
            // Dagboken: rutnätet ritar ringen.
            ChoiceGrid(emojis, "🐻", {}, label = { it }, selectionRing = true) { emoji, _ -> Text(emoji, style = AppTheme.typography.sectionTitle) }
        }
    }

    @Test
    fun `ColorSwatchPicker - ring och bock, och rutnätets ring`() = captureLightAndDark("ColorSwatchPicker_markering") {
        SampleSheet {
            ColorSwatchPicker(SampleSwatches.hexes, "#3B7EA1", {}, SampleSwatches::color, { SampleSwatches.label(it) }, SwatchMarker.RingAndCheck(checkColor = Color.White))
            // Dagboken: rutnätets ring i primärfärgen.
            ColorSwatchPicker(SampleSwatches.hexes, "#3B7EA1", {}, SampleSwatches::color, { SampleSwatches.label(it) }, SwatchMarker.GridRing)
        }
    }
}

/** En liten rund avatar i testerna, i stället för en apps egen. */
@Composable
private fun LeadingDot(color: Color) {
    Box(Modifier.size(24.dp).background(color, AppTheme.shapes.pill))
}
