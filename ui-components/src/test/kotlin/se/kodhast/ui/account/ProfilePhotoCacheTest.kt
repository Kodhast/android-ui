package se.kodhast.ui.account

import android.content.Context
import coil3.ColorImage
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toOkioPath
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/** Utloggningen tömmer profilfotots cache – både minnet och disken i Coils gemensamma bildladdare. */
@RunWith(RobolectricTestRunner::class)
class ProfilePhotoCacheTest {

    private val context = RuntimeEnvironment.getApplication() as Context

    @After
    fun reset() = SingletonImageLoader.reset()

    @Test
    fun `clear tömmer minnet och disken`() = runTest {
        val loader = ImageLoader.Builder(context)
            .memoryCache { MemoryCache.Builder().maxSizeBytes(MAX_BYTES).build() }
            .diskCache { DiskCache.Builder().directory(context.cacheDir.resolve("foto").toOkioPath()).maxSizeBytes(MAX_BYTES).build() }
            .build()
        SingletonImageLoader.setUnsafe(loader)
        val key = MemoryCache.Key("https://example.com/foto.jpg")
        loader.memoryCache!![key] = MemoryCache.Value(ColorImage())
        val disk = loader.diskCache!!
        disk.openEditor("foto")!!.apply {
            disk.fileSystem.write(data) { writeUtf8("bild") }
            commit()
        }
        assertTrue(disk.size > 0)

        val result = CoilProfilePhotoCache(context, StandardTestDispatcher(testScheduler)).clear()

        assertTrue(result.isSuccess)
        assertNull(loader.memoryCache!![key], "minnet tömt")
        assertNull(disk.openSnapshot("foto"), "disken tömd")
        assertEquals(0L, disk.size)
    }

    @Test
    fun `en bildladdare utan cache går också bra`() = runTest {
        SingletonImageLoader.setUnsafe(ImageLoader.Builder(context).memoryCache(null).diskCache(null).build())
        assertTrue(CoilProfilePhotoCache(context, StandardTestDispatcher(testScheduler)).clear().isSuccess)
    }

    private companion object {
        const val MAX_BYTES = 1_000_000L
    }
}
