package se.kodhast.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.Spacing
import se.kodhast.ui.theme.TRACK_HEIGHT

/**
 * Stegprickarna under ett formulär i steg: en prick per steg ([count]), och den aktuella ([current]) fjädrar ut till
 * ett streck i primärfärg. Läses inte – en text som "Steg 1 av 4" ovanför säger samma sak för TalkBack.
 */
@Composable
fun StepIndicator(count: Int, current: Int, modifier: Modifier = Modifier) {
    val track = trackColor()
    Row(modifier.fillMaxWidth().clearAndSetSemantics {}, horizontalArrangement = Arrangement.spacedBy(Spacing.xs, Alignment.CenterHorizontally)) {
        repeat(count) { index ->
            val active = index == current
            val width by animateDpAsState(if (active) ACTIVE_STEP else TRACK_HEIGHT, MaterialTheme.motionScheme.fastSpatialSpec(), label = "stegprick")
            Box(Modifier.size(width, TRACK_HEIGHT).background(if (active) MaterialTheme.colorScheme.primary else track, AppTheme.shapes.pill))
        }
    }
}

/**
 * Knapparna under ett formulär i steg på en [pager]: Föregående (från andra steget), och till höger Nästa – eller
 * Spara ([onSave], [saveEnabled], [saving]) på sista steget. Stegbytet glider fram med pagern. Står i en rad, t.ex.
 * arkets fasta knapprad (`AppBottomSheet(footer)`), så att knapparna syns också när steget är högt.
 *
 * [modifier] gäller knappen till höger (Nästa, Spara eller klart-läget).
 *
 * [done] = klart-läget: Spara byts mot en fylld knapp med bock och [doneText], Föregående är nedtonad, och
 * [doneAnnouncement] läses upp av skärmläsare (live region). Standard `false` = vanliga knappraden.
 */
@Composable
fun RowScope.StepNavigation(
    pager: PagerState,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    saveEnabled: Boolean = true,
    saving: Boolean = false,
    done: Boolean = false,
    doneText: String = stringResource(R.string.saved),
    doneAnnouncement: String = doneText
) {
    val scope = rememberCoroutineScope()
    val go = { page: Int ->
        scope.launch { pager.animateScrollToPage(page) }
        Unit
    }
    if (pager.currentPage > 0) AppButton(stringResource(R.string.previous), { go(pager.currentPage - 1) }, variant = ButtonVariant.Text, enabled = !done)
    Spacer(Modifier.weight(1f))
    // En och samma live region över lägena: i klart-läget bär den "Sparat" (text) och bekräftelsen (stateDescription),
    // och knappen under är ren status – ingen knapp för skärmläsare.
    Box(
        modifier.semantics {
            liveRegion = LiveRegionMode.Polite
            if (done) {
                text = AnnotatedString(doneText)
                stateDescription = doneAnnouncement
            }
        }
    ) {
        when {
            done -> AppButton(doneText, {}, Modifier.clearAndSetSemantics {}, icon = R.drawable.ic_check, variant = ButtonVariant.Primary)
            pager.currentPage < pager.pageCount - 1 -> AppButton(stringResource(R.string.next), { go(pager.currentPage + 1) }, variant = ButtonVariant.Secondary)
            else -> AppButton(stringResource(R.string.save), onSave, enabled = saveEnabled, loading = saving, variant = ButtonVariant.Primary)
        }
    }
}

private val ACTIVE_STEP = 20.dp
