package se.kodhast.ui.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Skrivfelen för en skärm utan redigeringsram (lista, detalj, en översikt): en skrivning som misslyckas blir ett [UiError]
 * (via appens [errors]) i [failure] tills skärmen visat det och anropat [clear]. Samma fel två gånger i rad är två
 * händelser (`UiError` jämförs på identitet). [write] kör en skrivning i [scope] (ViewModelns) och rapporterar dess fel.
 */
class WriteFailures(private val scope: CoroutineScope, private val errors: ErrorMapper) {
    private val _failure = MutableStateFlow<UiError?>(null)
    val failure: StateFlow<UiError?> = _failure.asStateFlow()

    /** Visar [error] (om det finns ett). */
    fun report(error: UiError?) {
        if (error != null) _failure.value = error
    }

    /** Visar [result]s fel; `true` när det misslyckades. */
    fun report(result: Result<*>): Boolean {
        val error = result.uiErrorOrNull(errors)
        report(error)
        return error != null
    }

    fun clear() {
        _failure.value = null
    }

    fun write(action: suspend () -> Result<Unit>) {
        scope.launch { report(action()) }
    }
}
