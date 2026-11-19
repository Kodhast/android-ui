package se.kodhast.ui.common

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart

// För en skärm där listan eller det lästa bara är ett fält i skärmens tillstånd (filter, summeringar,
// körstatus …) i stället för hela tillståndet. Laddning, tomt och fel beter sig ändå som i ramarna.

/** Listans rader, eller inga medan den laddas, är tom eller inte gick att läsa. */
fun <T> ListUiState<T>.itemsOrEmpty(): ImmutableList<T> = (this as? ListUiState.Content)?.items ?: persistentListOf()

/** Felet som visas i stället för listan, eller `null`. */
fun ListUiState<*>.errorOrNull(): UiError? = (this as? ListUiState.Error)?.error

/** Det lästa, eller `null` medan det laddas eller inte gick att läsa. */
fun <T> DetailUiState<T>.valueOrNull(): T? = (this as? DetailUiState.Content)?.value

/** Felet som visas i stället för innehållet, eller `null`. */
fun DetailUiState<*>.errorOrNull(): UiError? = (this as? DetailUiState.Error)?.error

/**
 * Ett fel i skärmens källa blir skärmens tillstånd med [ListUiState.Error] – översatt av [errors],
 * den enda felmappningen – i stället för en krasch. [onError] bygger skärmens tillstånd av felet.
 */
fun <S> Flow<S>.catchAsListError(errors: ErrorMapper, onError: (ListUiState.Error) -> S): Flow<S> = catch { emit(onError(ListUiState.Error(errors.map(it)))) }

/** Som [catchAsListError], för en detaljskärm: felet blir [DetailUiState.Error]. */
fun <S> Flow<S>.catchAsDetailError(errors: ErrorMapper, onError: (DetailUiState.Error) -> S): Flow<S> = catch { emit(onError(DetailUiState.Error(errors.map(it)))) }

/**
 * "Försök igen" efter ett läsfel för en skärm där det lästa bara är ett fält i tillståndet: ViewModeln
 * håller en [RetrySignal], felets knapp anropar [retry], och källan – med felet fångat
 * ([catchAsListError] eller [catchAsDetailError]) – läses om från början med [restartOn]. Samma
 * mönster som `asListUiState(errors, retry)`.
 */
class RetrySignal {
    // En buffrad plats: ett tryck medan skärmen visas går aldrig förlorat. Två snabba tryck kan ge två
    // omläsningar, men [restartOn] avbryter den första, så bara den sista syns.
    private val requests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** Läs om källan. */
    fun retry() {
        requests.tryEmit(Unit)
    }

    internal val signals: Flow<Unit> get() = requests.onStart { emit(Unit) }
}

/**
 * Samlar flödet en gång direkt och på nytt efter varje [RetrySignal.retry]. Står **efter** felfångsten,
 * så att ett läsfel blir tillståndet och nästa försök läser om källan i stället för att flödet är slut.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <S> Flow<S>.restartOn(retry: RetrySignal): Flow<S> = retry.signals.flatMapLatest { this }
