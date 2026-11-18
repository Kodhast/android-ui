package se.kodhast.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.components.testing.Green
import se.kodhast.ui.components.testing.captureLightAndDark
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.Spacing
import se.kodhast.ui.theme.Tone

/** Skärmdumpar (ljust + mörkt) av reglaget, veckoremsan, stegguiden, rullgardinen och sektionskortet, med påhittad data. */
@RunWith(RobolectricTestRunner::class)
class StepsScreenshotTest {

    private val today = LocalDate(2026, 11, 18)

    @Composable
    private fun Sample(content: @Composable () -> Unit) = Column(
        Modifier.width(WIDTH).background(MaterialTheme.colorScheme.background).padding(Spacing.l),
        verticalArrangement = Arrangement.spacedBy(Spacing.m)
    ) { content() }

    @Test
    fun `AppSlider - primärfärg, skala med nivå och nollan mitt på`() = captureLightAndDark("AppSlider_lagen") {
        Sample {
            AppSlider("Längd", 25, {}, valueText = "25 min", valueRange = 0..60, step = 5)
            val accents = AppTheme.extendedColors.accents
            AppSlider(
                "Energi",
                7,
                {},
                valueText = "7",
                trackBrush = Brush.horizontalGradient(accents.take(3)),
                thumbColor = accents[2],
                badge = { InfoPill("Hög", tone = Tone.Green) }
            )
            AppSlider("Aktivitetens energi", -4, {}, valueText = "−4", valueRange = -10..10)
            AppSlider("Stress", 3, {}, valueText = "3", enabled = false)
        }
    }

    @Test
    fun `DateStrip - vald dag, poster, idag klar och framtid nedtonad`() = captureLightAndDark("DateStrip_vecka") {
        Sample {
            DateStrip(today, LocalDate(2026, 11, 17), {}, {}, today = today, datesWithEntries = setOf(16, 17, 18).map { LocalDate(2026, 11, it) }.toSet())
            DateStrip(today, today, {}, {}, today = today, todayDone = true)
        }
    }

    @Test
    fun `DayTab och DayTabWeekday - egen dag i en veckorad`() {
        val days: @Composable () -> Unit = {
            Sample {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    listOf(16, 17, 18).forEach { day ->
                        DayTab(selected = day == 17, onClick = {}, shape = AppTheme.shapes.smallTile, modifier = Modifier.weight(1f), background = AppTheme.extendedColors.card) {
                            Text("🙂")
                            DayTabWeekday(LocalDate(2026, 11, day))
                        }
                    }
                }
            }
        }
        captureLightAndDark("DayTab_veckorad", content = days)
        captureLightAndDark("DayTabWeekday_veckorad", content = days)
    }

    @Test
    fun `SwipeToDelete och ExpandButton - kortet i vila`() {
        val card: @Composable () -> Unit = {
            Sample {
                SwipeToDelete({}) {
                    AppCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ItemRowText("Promenad", "45 min", Modifier.weight(1f))
                            ExpandButton(false, {})
                            ExpandButton(true, {})
                        }
                    }
                }
            }
        }
        captureLightAndDark("SwipeToDelete_vila", content = card)
        captureLightAndDark("ExpandButton_lagen", content = card)
    }

    @Test
    fun `StepIndicator och StepNavigation - andra steget av fyra`() {
        val steps: @Composable () -> Unit = {
            Sample {
                val pager = rememberPagerState(initialPage = 1, pageCount = { 4 })
                StepIndicator(4, pager.currentPage)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { StepNavigation(pager, onSave = {}) }
                val last = rememberPagerState(initialPage = 3, pageCount = { 4 })
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { StepNavigation(last, onSave = {}, done = true) }
            }
        }
        captureLightAndDark("StepIndicator_steg2", content = steps)
        captureLightAndDark("StepNavigation_steg2", content = steps)
    }

    @Test
    fun `DropdownPill - valt värde med pil`() = captureLightAndDark("DropdownPill_standard") {
        Sample { Row { DropdownPill("30 dagar", emptyList()) } }
    }

    @Test
    fun `ChoiceChips - favoriter och fler i meny`() = captureLightAndDark("ChoiceChips_fler") {
        Sample {
            LabeledGroup("Typ") {
                ChoiceChips(listOf("Promenad", "Yoga"), "Simning", {}, { it }, more = listOf("Cykling", "Simning"), moreLabel = "Fler typer")
            }
        }
    }

    @Test
    fun `SectionCard och KeyFigure - vanligt och tonat kort`() {
        val cards: @Composable () -> Unit = {
            Sample {
                SectionCard("Veckan", icon = R.drawable.ic_calendar, count = "4 / 7") { Text("Fyra dagar loggade.") }
                SectionCard("Allt klart för idag", icon = R.drawable.ic_check, tone = Tone.Green) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.l)) {
                        KeyFigure("6,8", "snittenergi", Modifier.weight(1f), tone = Tone.Green)
                        KeyFigure("+0,5", "mot igår", Modifier.weight(1f), tone = Tone.Green)
                        KeyFigure("12", "dagar i rad", Modifier.weight(1f), tone = Tone.Green)
                    }
                }
            }
        }
        captureLightAndDark("SectionCard_lagen", content = cards)
        captureLightAndDark("KeyFigure_lagen", content = cards)
    }

    @Test
    fun `StatPillGrid - två per rad och ett ensamt sist`() = captureLightAndDark("StatPillGrid_tre") {
        Sample {
            StatPillGrid(
                listOf(
                    StatPillItem(R.drawable.ic_clock, "7 h 40 min", "Sömn"),
                    StatPillItem(R.drawable.ic_star, "7 842", "Steg"),
                    StatPillItem(R.drawable.ic_calendar, "62", "Vilopuls")
                ),
                Tone.Primary
            )
        }
    }

    private companion object {
        val WIDTH = 360.dp
    }
}
