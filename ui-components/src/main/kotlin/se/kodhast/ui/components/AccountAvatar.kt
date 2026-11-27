package se.kodhast.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.OnlineColor

/**
 * Kontots avatar uppe till höger på varje flik ([AppHeader], via [LocalTopBarAccount]): öppnar kontot
 * och inställningarna med [onClick]. Visar profilfotot ovanpå initialerna ur [name] ("Anna Berg" → "AB");
 * utan namn en person-ikon. Fotot är [photoUrl] (Googles profilfoto), laddat med Coil och sparat i appens
 * privata cache så att det syns utan nät – utloggningen tömmer den med `ProfilePhotoCache`
 * (`se.kodhast.ui.account`); adressen loggas aldrig. [photo] är en egen slot som går före [photoUrl].
 * Går fotot inte att visa syns initialerna. Cirkeln är 36 dp i en tryckyta på 48 dp, och TalkBack läser
 * "Konto och inställningar" som en knapp.
 *
 * [online] = `true` ger en grön plupp nere till höger ([OnlineColor], samma i alla appar, med en ring i
 * sidhuvudets aktuella bakgrund) och TalkBack läser "Ansluten"; `false` ingen plupp och "Utan anslutning";
 * `null` (standard) ingen plupp och inget anslutningsläge. Utan [onClick] är avataren bara en bild (t.ex.
 * på ett kontokort där namnet står bredvid) och läses inte upp.
 */
@Composable
fun AccountAvatar(
    name: String?,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    online: Boolean? = null,
    photoUrl: String? = null,
    photo: (@Composable () -> Unit)? = photoUrl?.let { url -> { AccountPhoto(url) } }
) {
    val colors = MaterialTheme.colorScheme
    val circle = AppTheme.shapes.pill
    val description = stringResource(R.string.account_open)
    val connection = online?.let { stringResource(if (it) R.string.online else R.string.offline) }
    val initials = name?.let(::initials)
    val interaction = if (onClick == null) {
        Modifier.clearAndSetSemantics { }
    } else {
        Modifier
            .clickable(role = Role.Button, onClick = onClick)
            .clearAndSetSemantics {
                contentDescription = description
                role = Role.Button
                connection?.let { stateDescription = it }
            }
    }
    Box(modifier.size(TOUCH_TARGET).clip(circle).then(interaction), contentAlignment = Alignment.Center) {
        Box(Modifier.size(AVATAR)) {
            Box(Modifier.size(AVATAR).clip(circle).background(colors.primaryContainer), contentAlignment = Alignment.Center) {
                // Initialerna ligger alltid under fotot: syns tills fotot visas, och om det aldrig gör det.
                if (initials != null) {
                    Text(initials, style = AppTheme.typography.button, color = colors.onPrimaryContainer, maxLines = 1)
                } else {
                    Icon(painterResource(R.drawable.ic_person), contentDescription = null, tint = colors.onPrimaryContainer)
                }
                photo?.invoke()
            }
            if (online == true) {
                val ring = LocalAvatarRing.current ?: colors.background
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .size(DOT)
                        .background(OnlineColor.on(ring), circle)
                        .border(RING, ring, circle)
                        .testTag(ACCOUNT_ONLINE_TAG)
                )
            }
        }
    }
}

/** Profilfotot över hela cirkeln; laddas det inte syns initialerna under. Ingen beskrivning – avataren läses som en knapp. */
@Composable
private fun AccountPhoto(url: String) {
    AsyncImage(url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
}

/** Första bokstaven i det första och det sista ordet, versalt: "Anna Berg" → "AB", "anna" → "A"; inget namn → `null`. */
internal fun initials(name: String): String? {
    val words = name.trim().split(Regex("\\s+")).filter { word -> word.any { it.isLetter() } }
    if (words.isEmpty()) return null
    return listOf(words.first(), words.last()).take(if (words.size > 1) 2 else 1)
        .map { word -> word.first { it.isLetter() }.uppercaseChar() }
        .joinToString("")
}

/** Ringen runt pluppen: ytan avataren står på just nu. [AppHeader] sätter sin (sidans eller kortets färg); `null` = sidans. */
internal val LocalAvatarRing = compositionLocalOf<Color?> { null }

/** Pluppen för [AccountAvatar] med `online = true`, för tester. */
internal const val ACCOUNT_ONLINE_TAG = "account-online"

/** Den synliga cirkeln; tryckytan runt den är [TOUCH_TARGET]. */
private val AVATAR = 36.dp
private val DOT = 12.dp
private val RING = 2.dp
