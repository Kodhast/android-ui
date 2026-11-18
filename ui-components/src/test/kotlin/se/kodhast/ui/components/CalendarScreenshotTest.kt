package se.kodhast.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.datetime.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.components.testing.captureLightAndDark
import se.kodhast.ui.theme.Spacing

/** Skärmdumpar av kalendern och Välj dag (ljust + mörkt), med påhittade dagar. Idag är 18 november 2026. */
@RunWith(RobolectricTestRunner::class)
class CalendarScreenshotTest {

    private val today = LocalDate(2026, 11, 18)
    private val marked = listOf(1, 2, 3, 5, 6, 7, 9, 10, 11, 13, 14, 15, 16, 17, 18).map { LocalDate(2026, 11, it) }.toSet()

    @Composable
    private fun Sample(content: @Composable () -> Unit) = Column(
        Modifier.background(MaterialTheme.colorScheme.background).padding(Spacing.l),
        verticalArrangement = Arrangement.spacedBy(Spacing.m)
    ) { content() }

    @Test
    fun `AppCalendar - vald dag och framtid nedtonad`() = captureLightAndDark("AppCalendar_standard") {
        Sample { AppCard { AppCalendar(today, {}, marked, LocalDate(2026, 11, 7), {}, today = today, dimFuture = true) } }
    }

    @Test
    fun `DayPicker - chipet först i filterraden`() = captureLightAndDark("DayPicker_chip") {
        Sample {
            ChipRow {
                DayPicker(marked, today, {})
                AppFilterChip("Alla", selected = true, onClick = {})
                AppFilterChip("Mående", selected = false, onClick = {})
            }
        }
    }

    @Test
    fun `DayMarkerDot - idag, med poster och vald`() = captureLightAndDark("DayMarkerDot_lagen") {
        Sample {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.l)) {
                DayMarkerDot(isToday = true, marked = true, selected = false)
                DayMarkerDot(isToday = false, marked = true, selected = false)
                DayMarkerDot(isToday = false, marked = false, selected = false)
            }
        }
    }
}
