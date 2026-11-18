package se.kodhast.ui.common

import androidx.compose.runtime.Immutable

/**
 * Synkläget som navigationsramen (`AppNavHost` i ui-navigation) visar: [pending] får prickarna i
 * flikarnas sidhuvud att hoppa medan ändringar väntar på servern, [writeError] är en skrivning
 * servern nekade (visas en gång som meddelande) och [online] om enheten har nät. Appens
 * ViewModel bygger det ur sitt synkläge och mappar felet med sin [ErrorMapper].
 */
@Immutable
data class SyncUiState(val pending: Boolean = false, val writeError: UiError? = null, val online: Boolean = false)
