package se.kodhast.ui.navigation.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import kotlinx.datetime.LocalDate
import se.kodhast.ui.components.AppCard
import se.kodhast.ui.components.AppMenuItem
import se.kodhast.ui.components.AppSlider
import se.kodhast.ui.components.ChoiceChips
import se.kodhast.ui.components.DateStrip
import se.kodhast.ui.components.DayTab
import se.kodhast.ui.components.DayTabWeekday
import se.kodhast.ui.components.DropdownPill
import se.kodhast.ui.components.ExpandButton
import se.kodhast.ui.components.GroupLabel
import se.kodhast.ui.components.ItemRowText
import se.kodhast.ui.components.KeyFigure
import se.kodhast.ui.components.LabeledGroup
import se.kodhast.ui.components.R as ComponentsR
import se.kodhast.ui.components.SectionCard
import se.kodhast.ui.components.StatPillGrid
import se.kodhast.ui.components.StatPillItem
import se.kodhast.ui.components.StepIndicator
import se.kodhast.ui.components.StepNavigation
import se.kodhast.ui.components.SwipeToDelete
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.Spacing
import se.kodhast.ui.theme.Tone

/** Reglage, veckoremsa, stegguide, rullgardin och sektionskort i [ComponentGallery], med påhittad data. */
@Composable
internal fun StepsAndSliders() {
    GroupLabel("AppSlider")
    var energy by remember { mutableIntStateOf(7) }
    AppSlider("Energi", energy, { energy = it }, valueText = energy.toString())
    var minutes by remember { mutableIntStateOf(10) }
    AppSlider("Längd", minutes, { minutes = it }, valueText = "$minutes min", valueRange = 0..60, step = 5)
    GroupLabel("DateStrip · DayTab · DayTabWeekday")
    var week by remember { mutableStateOf(STRIP_TODAY) }
    var day by remember { mutableStateOf(STRIP_TODAY) }
    DateStrip(week, day, { day = it }, { week = it }, today = STRIP_TODAY, datesWithEntries = STRIP_ENTRIES, todayDone = true)
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        DayTab(selected = true, onClick = {}, shape = AppTheme.shapes.smallTile, modifier = Modifier.weight(1f)) {
            Text("🙂")
            DayTabWeekday(STRIP_TODAY)
        }
        DayTab(selected = false, onClick = {}, shape = AppTheme.shapes.smallTile, modifier = Modifier.weight(1f)) {
            Text("😐")
            DayTabWeekday(STRIP_YESTERDAY)
        }
    }
    GroupLabel("SwipeToDelete · ExpandButton")
    var expanded by remember { mutableStateOf(false) }
    SwipeToDelete({}) {
        AppCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ItemRowText("Promenad", "45 min · Energi +3", Modifier.weight(1f))
                ExpandButton(expanded, { expanded = !expanded })
            }
        }
    }
    GroupLabel("StepIndicator · StepNavigation")
    val pager = rememberPagerState(pageCount = { STEPS })
    StepIndicator(STEPS, pager.currentPage)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { StepNavigation(pager, onSave = {}) }
    GroupLabel("DropdownPill · ChoiceChips(more)")
    var period by remember { mutableStateOf("30 dagar") }
    Box { DropdownPill(period, PERIODS.map { p -> AppMenuItem(p, { period = p }, if (p == period) ComponentsR.drawable.ic_check else null) }) }
    var type by remember { mutableStateOf("Promenad") }
    LabeledGroup("Typ") {
        ChoiceChips(listOf("Promenad", "Yoga"), type, { type = it }, { it }, more = listOf("Cykling", "Simning", "Övrigt"))
    }
    GroupLabel("SectionCard · KeyFigure · StatPillGrid")
    SectionCard("Allt klart för idag", icon = ComponentsR.drawable.ic_check) {
        Row(Modifier.semantics(mergeDescendants = true) {}, horizontalArrangement = Arrangement.spacedBy(Spacing.l)) {
            KeyFigure("6,8", "snittenergi", Modifier.weight(1f))
            KeyFigure("+0,5", "mot igår", Modifier.weight(1f))
            KeyFigure("12", "dagar i rad", Modifier.weight(1f))
        }
    }
    StatPillGrid(
        listOf(
            StatPillItem(ComponentsR.drawable.ic_clock, "7 h 40 min", "Sömn"),
            StatPillItem(ComponentsR.drawable.ic_star, "7 842", "Steg"),
            StatPillItem(ComponentsR.drawable.ic_calendar, "62", "Vilopuls")
        ),
        Tone.Primary
    )
}

private const val STEPS = 4
private val PERIODS = listOf("7 dagar", "30 dagar", "90 dagar")
private val STRIP_TODAY = LocalDate(2026, 10, 2)
private val STRIP_YESTERDAY = LocalDate(2026, 10, 1)
private val STRIP_ENTRIES = setOf(LocalDate(2026, 9, 28), LocalDate(2026, 9, 30), STRIP_YESTERDAY, STRIP_TODAY)
