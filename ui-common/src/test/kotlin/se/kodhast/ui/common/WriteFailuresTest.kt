package se.kodhast.ui.common

import app.cash.turbine.test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import se.kodhast.ui.common.testing.TestError
import se.kodhast.ui.common.testing.TestErrors

class WriteFailuresTest {
    @Test
    fun `en lyckad skrivning visar inget fel`() = runTest(UnconfinedTestDispatcher()) {
        val failures = WriteFailures(backgroundScope, TestErrors)
        failures.write { Result.success(Unit) }
        assertNull(failures.failure.value)
    }

    @Test
    fun `en misslyckad skrivning visas tills den kvitterats, och samma fel två gånger är två händelser`() = runTest(UnconfinedTestDispatcher()) {
        val failures = WriteFailures(backgroundScope, TestErrors)
        failures.failure.test {
            assertNull(awaitItem())
            failures.write { Result.failure(TestError.Offline) }
            assertEquals(TestError.Offline, awaitItem()?.cause)
            failures.clear()
            assertNull(awaitItem())
            failures.write { Result.failure(TestError.Offline) }
            assertEquals(TestError.Offline, awaitItem()?.cause)
        }
    }

    @Test
    fun `report av ett resultat säger om det misslyckades, och ett lyckat rör inte ett fel som visas`() = runTest(UnconfinedTestDispatcher()) {
        val failures = WriteFailures(backgroundScope, TestErrors)
        assertTrue(failures.report(Result.failure<Unit>(TestError.NotFound)))
        assertEquals(TestError.NotFound, failures.failure.value?.cause)
        assertFalse(failures.report(Result.success(Unit)))
        failures.report(null)
        assertEquals(TestError.NotFound, failures.failure.value?.cause)
    }
}
