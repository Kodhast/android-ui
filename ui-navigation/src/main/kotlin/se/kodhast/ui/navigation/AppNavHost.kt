package se.kodhast.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import se.kodhast.ui.common.SyncUiState
import se.kodhast.ui.components.AppFloatingToolbar
import se.kodhast.ui.components.AppSnackbarHost
import se.kodhast.ui.components.ErrorSnackbar
import se.kodhast.ui.components.FloatingToolbarClearance
import se.kodhast.ui.components.LocalBottomClearance
import se.kodhast.ui.components.LocalTopBarAccount
import se.kodhast.ui.components.LocalTopBarSyncing
import se.kodhast.ui.components.NoBottomClearance
import se.kodhast.ui.components.ToolbarAction
import se.kodhast.ui.components.ToolbarItem
import se.kodhast.ui.theme.Spacing

/**
 * Navigationsramen: `NavDisplay` med [AppTransitions] och den flytande verktygsraden, som bara
 * syns i en fliks rot. [tabs] är verktygsradens flikar, i samma ordning som [backStack]s flikar;
 * [action] plusknappen (utan den ingen plusknapp). Skärmarna kommer från [entryProvider]; varje
 * skärm får en egen ViewModel-ägare, och flikarnas rötter behåller sitt sparade tillstånd (t.ex.
 * rullning) när man byter flik.
 *
 * [sync] får prickarna i flikarnas sidhuvud att hoppa medan ändringar väntar och ger meddelandet
 * om en nekad skrivning, i appens gemensamma meddelandeyta ovanför verktygsraden; [onSyncErrorShown]
 * anropas när det visats, så att ViewModeln släpper felet. [account] står längst till höger i
 * flikarnas sidhuvud. Systemets tillbaka stänger översta skärmen och ger den till [onPopped]; i
 * första flikens rot tar ramen inte tillbaka, så systemet stänger appen som vanligt.
 */
@Composable
fun <K : NavKey> AppNavHost(
    backStack: TabBackStack<K>,
    tabs: List<ToolbarItem>,
    entryProvider: (K) -> NavEntry<K>,
    modifier: Modifier = Modifier,
    sync: SyncUiState = SyncUiState(),
    onSyncErrorShown: () -> Unit = {},
    action: ToolbarAction? = null,
    onPopped: (K) -> Unit = {},
    account: @Composable () -> Unit = {}
) {
    require(tabs.size == backStack.tabs.size) { "verktygsraden har ${tabs.size} flikar, back stacken ${backStack.tabs.size}" }
    val clearance = if (backStack.atTopLevel) FloatingToolbarClearance else NoBottomClearance
    val messages = remember { SnackbarHostState() }
    ErrorSnackbar(sync.writeError, messages, R.string.sync_write_rejected, duration = SnackbarDuration.Long, onShown = onSyncErrorShown)
    Box(modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalBottomClearance provides clearance, LocalTopBarSyncing provides sync.pending, LocalTopBarAccount provides account) {
            NavDisplay(
                backStack = backStack.entries,
                onBack = {
                    val top = backStack.entries.last()
                    if (backStack.pop()) onPopped(top)
                },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(rememberTabRootStateHolder()),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                transitionSpec = { AppTransitions.forward() },
                popTransitionSpec = { AppTransitions.back() },
                predictivePopTransitionSpec = { AppTransitions.back() },
                entryProvider = { key -> entryProvider(key).let { if (key in backStack.tabs) it.asTabRoot(key) else it } }
            )
        }
        AnimatedVisibility(
            visible = backStack.atTopLevel,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = Spacing.l),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            AppFloatingToolbar(
                items = tabs,
                selectedIndex = backStack.tabs.indexOf(backStack.currentTab),
                onSelect = { backStack.select(backStack.tabs[it]) },
                action = action
            )
        }
        AppSnackbarHost(messages, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = clearance))
    }
}

/**
 * Sparat tillstånd (t.ex. rullning) för flikarnas rötter behålls när fliken lämnar back stacken
 * vid flikbyte; undersidornas tillstånd släpps som vanligt när de stängs.
 */
@Composable
private fun rememberTabRootStateHolder(): SaveableStateHolder {
    val holder = rememberSaveableStateHolder()
    return remember(holder) { TabRootStateHolder(holder) }
}

private class TabRootStateHolder(private val holder: SaveableStateHolder) : SaveableStateHolder {
    @Composable
    override fun SaveableStateProvider(key: Any, content: @Composable () -> Unit) = holder.SaveableStateProvider(key, content)

    override fun removeState(key: Any) {
        if (!(key is String && key.startsWith(TAB_ROOT))) holder.removeState(key)
    }
}

/** Prefix för flikrötternas innehållsnycklar (en sträng, eftersom nyckeln sparas i en Bundle). */
private const val TAB_ROOT = "flikrot:"

private fun <K : Any> NavEntry<K>.asTabRoot(key: K): NavEntry<K> = NavEntry(key, contentKey = TAB_ROOT + key, metadata = metadata) { Content() }
