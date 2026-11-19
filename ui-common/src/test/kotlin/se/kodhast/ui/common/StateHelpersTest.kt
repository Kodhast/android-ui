package se.kodhast.ui.common

import app.cash.turbine.test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import se.kodhast.ui.common.testing.Messages
import se.kodhast.ui.common.testing.TestError
import se.kodhast.ui.common.testing.TestErrors

/** Hjälparna för en skärm där listan eller det lästa är ett fält i tillståndet. */
class StateHelpersTest {
    private val listError = ListUiState.Error(UiError(TestError.Offline, Messages.OFFLINE))
    private val detailError = DetailUiState.Error(UiError(TestError.Offline, Messages.OFFLINE))

    @Test
    fun `raderna finns bara i innehållsläget`() {
        assertEquals(listOf("a", "b"), listOf("a", "b").toListUiState().itemsOrEmpty())
        assertTrue(ListUiState.Loading.itemsOrEmpty().isEmpty())
        assertTrue(ListUiState.Empty.itemsOrEmpty().isEmpty())
        assertTrue(listError.itemsOrEmpty().isEmpty())
    }

    @Test
    fun `listans fel finns bara i felläget`() {
        assertSame(listError.error, listError.errorOrNull())
        assertNull(ListUiState.Loading.errorOrNull())
        assertNull(ListUiState.Empty.errorOrNull())
        assertNull(listOf(1).toListUiState().errorOrNull())
    }

    @Test
    fun `det lästa finns bara i innehållsläget`() {
        assertEquals("a", DetailUiState.Content("a").valueOrNull())
        assertNull(DetailUiState.Loading.valueOrNull())
        assertNull(detailError.valueOrNull())
    }

    @Test
    fun `detaljens fel finns bara i felläget`() {
        assertSame(detailError.error, detailError.errorOrNull())
        assertNull(DetailUiState.Loading.errorOrNull())
        assertNull(DetailUiState.Content("a").errorOrNull())
    }

    @Test
    fun `ett fel i källan blir skärmens tillstånd med appens mappade fel`() = runTest {
        val source = flow {
            emit("laddad")
            throw TestError.PermissionDenied
        }
        source.catchAsListError(TestErrors) { "lista: ${it.error.cause} ${it.error.message}" }.test {
            assertEquals("laddad", awaitItem())
            assertEquals("lista: ${TestError.PermissionDenied} ${Messages.PERMISSION_DENIED}", awaitItem())
            awaitComplete()
        }
        source.catchAsDetailError(TestErrors) { "detalj: ${it.error.cause}" }.test {
            assertEquals("laddad", awaitItem())
            assertEquals("detalj: ${TestError.PermissionDenied}", awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `ett okänt fel mappas av appen`() = runTest {
        flow<String> { error("x") }.catchAsDetailError(TestErrors) { it.error.cause.toString() }.test {
            assertEquals(TestError.Unknown.toString(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `utan fel passerar flödet orört`() = runTest {
        flowOf(1, 2).catchAsListError(TestErrors) { -1 }.test {
            assertEquals(1, awaitItem())
            assertEquals(2, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `restartOn läser källan direkt och på nytt efter varje retry, och felet blir tillståndet`() = runTest(UnconfinedTestDispatcher()) {
        var attempts = 0
        val retry = RetrySignal()
        val source = flow {
            attempts++
            if (attempts == 1) throw TestError.Offline
            emit("laddad")
        }

        source.catchAsListError(TestErrors) { "fel: ${it.error.message}" }.restartOn(retry).test {
            assertEquals("fel: ${Messages.OFFLINE}", awaitItem())
            retry.retry()
            assertEquals("laddad", awaitItem())
            assertEquals(2, attempts)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `två snabba tryck - bara den sista omläsningen syns`() = runTest {
        var attempts = 0
        val retry = RetrySignal()
        flow {
            val attempt = ++attempts
            delay(100)
            emit(attempt)
        }.restartOn(retry).test {
            assertEquals(1, awaitItem())
            retry.retry()
            retry.retry()
            assertEquals(3, awaitItem())
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `inget tryck går förlorat`() = runTest(UnconfinedTestDispatcher()) {
        var attempts = 0
        val retry = RetrySignal()
        flow {
            emit(++attempts)
        }.restartOn(retry).test {
            assertEquals(1, awaitItem())
            retry.retry()
            assertEquals(2, awaitItem())
            retry.retry()
            assertEquals(3, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
