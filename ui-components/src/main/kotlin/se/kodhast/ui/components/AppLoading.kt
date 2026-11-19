@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package se.kodhast.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import se.kodhast.ui.components.R
import se.kodhast.ui.theme.AppTheme

/**
 * Laddning: Expressive laddningsindikator mitt på ytan – eller Kodhästs två prickar som hoppar, i
 * `primary`, när appen slagit på `AppBrandStyle.dotLoading` ([LocalAppBrandStyle]).
 */
@Composable
fun AppLoading(modifier: Modifier = Modifier) {
    val description = stringResource(R.string.loading)
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val indicator = Modifier.semantics { contentDescription = description }
        if (LocalAppBrandStyle.current.dotLoading) {
            BrandDots(MaterialTheme.colorScheme.primary, indicator.progressSemantics().size(DOTS_WIDTH, DOTS_HEIGHT), bouncing = true)
        } else {
            LoadingIndicator(indicator)
        }
    }
}

/**
 * Liten laddningsindikator i en rad (t.ex. [SectionHeader] med `working`), läst som [description];
 * prickarna i [color] med `AppBrandStyle.dotLoading`.
 */
@Composable
internal fun InlineLoading(description: String, color: Color, modifier: Modifier = Modifier) {
    val indicator = modifier.semantics { contentDescription = description }
    if (LocalAppBrandStyle.current.dotLoading) {
        BrandDots(color, indicator.progressSemantics(), bouncing = true)
    } else {
        LoadingIndicator(indicator, color = color)
    }
}

/** Prickladdningen i [AppLoading]: två prickar på 16 dp med plats att hoppa. */
private val DOTS_WIDTH = 48.dp
private val DOTS_HEIGHT = 28.dp

/** Ytan som dras ned för att uppdatera: [AppPullToRefresh], också i `EntityListScreen` och `EntityDetailScreen` (finns bara med `onRefresh`). */
const val ENTITY_REFRESH_TAG = "entity-refresh"

/**
 * Dra för att uppdatera runt [content] när [onRefresh] finns – annars bara en `Box` med innehållet. Ramarna
 * använder den med sina `refreshing`/`onRefresh`; en skärm utan ram (Fonders Översikt, Innehav, Beslut) lägger
 * den själv runt sitt rullbara innehåll. Indikatorn är Expressive-laddningsindikatorn; med
 * `AppBrandStyle.dotLoading` Kodhästs prickar som hoppar i en rund behållare överst ([PullDots]), och då visas
 * den bara medan man drar och under en uppdatering man själv startat (som får starta upp till 5 s efter dragningen) – en körning appen startat själv syns
 * bara av sidhuvudets prickar. Gesten kräver rullbart innehåll ([RefreshableState] för ett läge som inte
 * rullar själv). Ytan har taggen [ENTITY_REFRESH_TAG].
 */
@Composable
fun AppPullToRefresh(refreshing: Boolean, onRefresh: (() -> Unit)?, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    if (onRefresh == null) {
        Box(modifier, content = content)
        return
    }
    val state = rememberPullToRefreshState()
    val dots = LocalAppBrandStyle.current.dotLoading
    // Prickarna visas för en uppdatering man själv dragit fram, också när appen sätter `refreshing` först en
    // stund efter att indikatorn gömts (ett köat jobb). Minnet släpps när den uppdateringen är klar, eller om
    // ingen uppdatering startat inom PULL_WAIT – annars fick nästa körning appen startar själv prickarna.
    var pulled by remember { mutableStateOf(false) }
    var pulls by remember { mutableIntStateOf(0) }
    var wasRefreshing by remember { mutableStateOf(refreshing) }
    if (wasRefreshing != refreshing) {
        if (!refreshing) pulled = false
        wasRefreshing = refreshing
    }
    if (dots && pulled && !refreshing) {
        // En ny dragning börjar väntan om.
        LaunchedEffect(pulls) {
            delay(PULL_WAIT)
            pulled = false
        }
    }
    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = {
            pulled = true
            pulls++
            onRefresh()
        },
        modifier = modifier.testTag(ENTITY_REFRESH_TAG),
        state = state,
        indicator = {
            if (!dots) {
                PullToRefreshDefaults.LoadingIndicator(state = state, isRefreshing = refreshing, modifier = Modifier.align(Alignment.TopCenter))
            } else {
                // Bara övergången till "dras" ritar om indikatorn, inte varje bildruta av dragningen.
                val dragging by remember(state) { derivedStateOf { state.distanceFraction > 0f } }
                val showDots = if (refreshing) pulled else dragging
                if (showDots) {
                    PullDots({ state.distanceFraction }, Modifier.align(Alignment.TopCenter))
                }
            }
        },
        content = content
    )
}

/** Så länge en dragning väntar på att appens uppdatering ska starta. */
private const val PULL_WAIT = 5_000L

/** Dra-ned-indikatorn med prickar: en rund behållare som följer dragningen ned från kanten och stannar där under uppdateringen. */
@Composable
internal fun PullDots(fraction: () -> Float, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.loading)
    val density = LocalDensity.current
    val rest = with(density) { (PullToRefreshDefaults.PositionalThreshold - PULL_SIZE).roundToPx() }
    val size = with(density) { PULL_SIZE.roundToPx() }
    Surface(
        modifier = modifier.offset { IntOffset(0, (fraction().coerceIn(0f, 1f) * (rest + size) - size).toInt()) }.size(PULL_SIZE),
        shape = AppTheme.shapes.pill,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = PULL_ELEVATION
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            InlineLoading(description, MaterialTheme.colorScheme.primary, Modifier.size(DOTS_WIDTH / 2, DOTS_HEIGHT / 2))
        }
    }
}

private val PULL_SIZE = 48.dp
private val PULL_ELEVATION = 2.dp

/**
 * Ett läge som inte rullar själv (laddning, tomt, fel) i [AppPullToRefresh]: med [onRefresh] är ytan rullbar,
 * så att gesten når fram. Läget får alltid minst platsens storlek, så att det ligger där det låg utan rullning.
 */
@Composable
internal fun RefreshableState(onRefresh: (() -> Unit)?, content: @Composable () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val height = maxHeight
        val scroll = if (onRefresh != null) Modifier.verticalScroll(rememberScrollState()) else Modifier
        Box(Modifier.fillMaxSize().then(scroll)) {
            Box(Modifier.fillMaxWidth().heightIn(min = height), propagateMinConstraints = true) { content() }
        }
    }
}
