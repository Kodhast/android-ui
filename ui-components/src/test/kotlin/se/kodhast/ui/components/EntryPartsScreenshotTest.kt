package se.kodhast.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.components.testing.Blue
import se.kodhast.ui.components.testing.Green
import se.kodhast.ui.components.testing.captureLightAndDark
import se.kodhast.ui.theme.Spacing
import se.kodhast.ui.theme.Tone

/** Skärmdumpar av postens delar från Dagboken (ljust + mörkt), med påhittade värden (android-ui #57). */
@RunWith(RobolectricTestRunner::class)
class EntryPartsScreenshotTest {

    @Composable
    private fun Sample(content: @Composable ColumnScope.() -> Unit) {
        // Innehållsfärgen som appens Scaffold ger, så att ikoner utan egen färg (stjärnan) syns som i appen.
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
            Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.m), content = content)
        }
    }

    @Test
    fun `StatPill - toner`() = captureLightAndDark("StatPill_toner") {
        Sample {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                StatPill(R.drawable.ic_check, "7 842", "Steg", Tone.Blue)
                StatPill(R.drawable.ic_clock, "7 h 45 min", "Sömn", Tone.Primary)
            }
            StatPill(R.drawable.ic_clock, "—", "Puls", Tone.Green, onClick = {}, onClickLabel = "Begär åtkomst")
        }
    }

    @Test
    fun `StatPill - åtgärd`() = captureLightAndDark("StatPill_atgard") {
        Sample { StatPill(R.drawable.ic_clock, "2 saknas", "Träning · Syremättnad", Tone.Primary, Modifier.fillMaxWidth(), onClick = {}, action = "Ge åtkomst") }
    }

    @Test
    fun `FavoriteStar - lägen`() = captureLightAndDark("FavoriteStar_lagen") {
        Sample {
            AppCard {
                ItemRow("Alvedon 500 mg", subtitle = "Minst 4 h mellan · högst 8 per dag", navigates = true, onClick = {}, trailing = { FavoriteStar("Alvedon", true, {}) })
                ItemRow("Loratadin 10 mg", subtitle = "Ingen gräns", navigates = true, onClick = {}, trailing = { FavoriteStar("Loratadin", false, {}) })
            }
        }
    }

    @Test
    fun `FavoriteMark - efter titeln`() = captureLightAndDark("FavoriteMark_standard") {
        Sample { AppCard { ItemRow("Betapred 0,5 mg", subtitle = "Vid behov", trailing = { FavoriteMark() }) } }
    }

    @Test
    fun `DateTimeRow - standard`() = captureLightAndDark("DateTimeRow_standard") {
        Sample { DateTimeRow(LocalDate(2026, 10, 4), LocalTime(8, 30), {}, {}) }
    }

    @Test
    fun `DurationRow - snabbval`() = captureLightAndDark("DurationRow_snabbval") {
        Sample { DurationRow(60, {}) }
    }

    @Test
    fun `ReminderTimeRow - lägen`() = captureLightAndDark("ReminderTimeRow_lagen") {
        Sample {
            AppCard {
                ReminderTimeRow("Efter frukost", LocalTime(8, 0), {}, enabled = true, onEnabledChange = {})
                ReminderTimeRow("Läggdags", LocalTime(22, 0), {}, enabled = false, onEnabledChange = {})
                ReminderTimeRow("Periodslut", LocalTime(9, 0), {})
            }
        }
    }

    @Test
    fun `NoteCard - lägen`() = captureLightAndDark("NoteCard_lagen") {
        Sample {
            NoteCard(null, {})
            NoteCard("Sov dåligt, vaknade vid fyra. Tog en promenad före frukost och mådde bättre efteråt.", {})
            NoteCard("Sov dåligt.", {}, initiallyExpanded = true)
        }
    }

    @Test
    fun `NoteCard - fel`() = captureLightAndDark("NoteCard_fel") {
        Sample { NoteCard("Sov dåligt.", {}, error = "Värdet går inte att spara – korta eller ändra det") }
    }
}
