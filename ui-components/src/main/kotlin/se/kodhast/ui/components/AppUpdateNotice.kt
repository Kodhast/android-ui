package se.kodhast.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import se.kodhast.ui.common.AppUpdate
import se.kodhast.ui.common.UpdateChecker

/**
 * "Ny version finns" (#125, variant C): [update] är den nyare versionen och [onOpen] öppnar appens Om-vy,
 * där [AboutCard] har Hämta. Sätts av appen runt `AppNavHost` med [LocalAppUpdate]; då får sidhuvudet
 * ([AppHeader]) en prick efter versionsnumret, Om-raden i [AccountMenu] en undertext och [AboutCard] raden
 * med Hämta. Utan den (standard) ser allt ut som förut.
 */
@Immutable
data class AppUpdateNotice(val update: AppUpdate, val onOpen: () -> Unit)

/** Den nyare versionen, om det finns en; standard `null`. Se [AppUpdateNotice]. */
val LocalAppUpdate = compositionLocalOf<AppUpdateNotice?> { null }

/**
 * Frågar [checker] när appen startar och varje gång den kommer tillbaka i förgrunden, högst en gång per
 * [minIntervalMs] (standard en timme), och svarar med den nyare versionen eller `null`. `checker = null`
 * (debug-bygget) frågar aldrig; håll samma [checker] med `remember`. Frågan går på IO-tråden; ett fel ger `null`.
 */
@Composable
fun rememberAppUpdate(checker: UpdateChecker?, minIntervalMs: Long = HOUR_MS): State<AppUpdate?> {
    val update = remember { mutableStateOf<AppUpdate?>(null) }
    // Inte sparad över en rotation: då är svaret också borta, och en fråga till kostar inget.
    val lastCheck = remember { longArrayOf(Long.MIN_VALUE) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(checker, lifecycle) {
        if (checker == null) return@LaunchedEffect
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            val time = System.currentTimeMillis()
            if (lastCheck[0] == Long.MIN_VALUE || time - lastCheck[0] >= minIntervalMs) {
                lastCheck[0] = time
                update.value = withContext(Dispatchers.IO) { checker.check() }
            }
        }
    }
    return update
}

private const val HOUR_MS = 60 * 60 * 1000L
