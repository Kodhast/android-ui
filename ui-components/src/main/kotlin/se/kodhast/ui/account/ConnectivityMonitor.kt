package se.kodhast.ui.account

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn

/** Om appen har fungerande internet just nu – pluppen på avataren (`AccountAvatar(online = …)`) och när profilen kontrolleras. */
interface NetworkStatus {
    /** Sant när enhetens nätverk når internet (Android har kontrollerat det); falskt utan nät och bakom en inloggningsportal. */
    val online: StateFlow<Boolean>
}

/**
 * [NetworkStatus] ur systemets standardnätverk: `registerDefaultNetworkCallback`, ansluten = nätverket har
 * `NET_CAPABILITY_VALIDATED` (internet, inte bara wifi). Lyssnar bara medan någon samlar (och fem sekunder efter, så att
 * en skärmrotation inte registrerar om). Behörigheten `ACCESS_NETWORK_STATE` kommer med bibliotekets manifest.
 *
 * Utan DI-ramverk: appen skapar en enda instans med applikationens [context] och ett [scope] som lever lika länge som
 * appen (Dagboken och ReseApoteket: en `@Singleton`-provider i Hilt).
 */
class ConnectivityMonitor(context: Context, scope: CoroutineScope) : NetworkStatus {
    private val manager = context.getSystemService(ConnectivityManager::class.java)

    override val online: StateFlow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                trySend(capabilities.isOnline())
            }

            override fun onLost(network: Network) {
                trySend(false)
            }
        }
        trySend(current())
        manager.registerDefaultNetworkCallback(callback)
        awaitClose { manager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged().stateIn(scope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), current())

    private fun current(): Boolean = manager.activeNetwork?.let(manager::getNetworkCapabilities)?.isOnline() == true

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

private fun NetworkCapabilities.isOnline(): Boolean = hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
