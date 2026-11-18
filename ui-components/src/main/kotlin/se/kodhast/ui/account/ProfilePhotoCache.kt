package se.kodhast.ui.account

import android.content.Context
import coil3.SingletonImageLoader
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Profilfotots cache på enheten: `AccountAvatar(photoUrl = …)` laddar fotot med Coil, som sparar det i appens privata
 * cache (`cacheDir`, som Android inte säkerhetskopierar), så att det syns inloggat utan nät. Ett utbytt foto har en ny
 * adress och blir en ny post.
 */
interface ProfilePhotoCache {
    /** Tar bort de sparade bilderna – vid utloggning. Ett fel här ska inte stoppa utloggningen. */
    suspend fun clear(): Result<Unit>
}

/**
 * [ProfilePhotoCache] i Coils gemensamma bildladdare. Profilfotot är appens enda bild från nätet, så hela Coils cache
 * töms – minne och disk. Har appen andra bilder från nätet ska den ge fotot en egen bildladdare i stället.
 */
class CoilProfilePhotoCache(
    private val context: Context,
    private val io: CoroutineDispatcher = Dispatchers.IO
) : ProfilePhotoCache {
    override suspend fun clear(): Result<Unit> = withContext(io) {
        try {
            val loader = SingletonImageLoader.get(context)
            loader.memoryCache?.clear()
            loader.diskCache?.clear()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
