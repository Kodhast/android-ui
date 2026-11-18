package se.kodhast.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import se.kodhast.ui.components.AppHeader
import se.kodhast.ui.components.AppMenuItem
import se.kodhast.ui.components.R
import se.kodhast.ui.components.ToolbarAction
import se.kodhast.ui.components.ToolbarItem

/** Verktygsradens tre flikar, i samma ordning som [TABS]. */
val TAB_ITEMS = listOf(
    ToolbarItem("Hem", R.drawable.ic_box),
    ToolbarItem("Personer", R.drawable.ic_person),
    ToolbarItem("Gemensamt", R.drawable.ic_note)
)

/** Varje skärm visar sin nyckel. */
val plainScreens: (TestKey) -> NavEntry<TestKey> = { key -> NavEntry(key) { Text("Skärm: $key") } }

/** Varje skärm med appens sidhuvud, som ramarna ger dem: fliken eller undersidans namn. */
@OptIn(ExperimentalMaterial3Api::class)
val headerScreens: (TestKey) -> NavEntry<TestKey> = { key ->
    NavEntry(key) {
        // Skärmen fyller sin yta, som appens Scaffold – ramen själv ritar ingen bakgrund.
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            AppHeader(if (key in TABS) TAB_ITEMS[TABS.indexOf(key)].label else "Skärm: $key", TopAppBarDefaults.pinnedScrollBehavior())
        }
    }
}

/** Plusknappen med en meny, som apparnas "Lägg till". */
fun addAction(onPick: (String) -> Unit): ToolbarAction = ToolbarAction(
    "Lägg till",
    R.drawable.ic_add,
    listOf("Ny resa", "Ny person").map { AppMenuItem(it, { onPick(it) }, icon = R.drawable.ic_add) }
)

@Composable
fun TestNavHost(backStack: TabBackStack<TestKey>, entries: (TestKey) -> NavEntry<TestKey> = plainScreens, action: ToolbarAction? = null) = AppNavHost(backStack, TAB_ITEMS, entries, action = action)
