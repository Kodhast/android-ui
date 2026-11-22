package se.kodhast.ui.components

import android.content.ActivityNotFoundException
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import se.kodhast.ui.common.UiError
import se.kodhast.ui.theme.Spacing

/**
 * Inloggningen, i hela skärmen: appens ikon ([appIcon], inte Kodhästs häst – inloggningen ska kännas
 * igen som appen), [appName], [welcome] och [note] (t.ex. vad som skapas första gången), och knappen
 * "Logga in med Google". [busy] visar "Loggar in …" med knappens arbetar-läge och spärrar ett nytt tryck.
 * [error] visas en gång som meddelande ovanför knappen ([ErrorSnackbar]), och [onErrorShown] anropas
 * sedan så att ViewModeln släpper det; ett avbrutet konto-val är inget fel och skickas inte hit.
 *
 * Inloggningen själv (Credential Manager, Firebase) är appens: [onSignIn] startar den.
 */
@Composable
fun SignInScreen(
    @DrawableRes appIcon: Int,
    appName: String,
    welcome: String,
    note: String?,
    busy: Boolean,
    error: UiError?,
    onErrorShown: () -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbar = remember { SnackbarHostState() }
    ErrorSnackbar(error, snackbar, onShown = onErrorShown)
    CompositionLocalProvider(LocalAppBrandStyle provides LocalAppBrandStyle.current.copy(emptyStateMark = false)) {
        EmptyState(
            icon = appIcon,
            title = appName,
            message = welcome,
            modifier = modifier,
            note = note,
            fullScreen = true,
            action = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                    AppSnackbarHost(snackbar)
                    AppButton(
                        text = stringResource(if (busy) R.string.sign_in_busy else R.string.sign_in_google),
                        onClick = onSignIn,
                        modifier = Modifier.fillMaxWidth(),
                        icon = R.drawable.ic_login,
                        loading = busy
                    )
                }
            }
        )
    }
}

/**
 * Datan är sparad med en nyare version av appen: "Uppdatera appen", [message] (vad som sparats med en
 * nyare version) och [note] (att inget ändras förrän appen uppdaterats), och knappen "Hämta senaste
 * versionen", som öppnar [releasesUrl]. Inget av appens innehåll visas förrän appen uppdaterats. Finns ingen
 * app som kan öppna länken visas ett meddelande ovanför knappen i stället för att appen kraschar.
 */
@Composable
fun UpdateRequiredScreen(message: String, note: String?, releasesUrl: String, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val snackbar = remember { SnackbarHostState() }
    // Räknaren gör varje misslyckat tryck till ett eget meddelande.
    var failures by remember { mutableIntStateOf(0) }
    MessageSnackbar(if (failures > 0) stringResource(R.string.update_required_no_browser) else null, snackbar, key = failures) {}
    EmptyState(
        icon = R.drawable.ic_download,
        title = stringResource(R.string.update_required_title),
        message = message,
        modifier = modifier,
        note = note,
        fullScreen = true,
        action = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                // Platsen för meddelandet finns först efter ett misslyckat tryck, så att skärmen annars ligger som förut.
                if (failures > 0) AppSnackbarHost(snackbar)
                AppButton(
                    text = stringResource(R.string.update_required_action),
                    onClick = { if (!uriHandler.tryOpenUri(releasesUrl)) failures++ },
                    modifier = Modifier.fillMaxWidth(),
                    icon = R.drawable.ic_download
                )
            }
        }
    )
}

/**
 * Öppnar [uri] och svarar falskt i stället för att krascha när ingen app tar emot länken
 * (AndroidUriHandler kastar då IllegalArgumentException eller ActivityNotFoundException).
 */
internal fun UriHandler.tryOpenUri(uri: String): Boolean = try {
    openUri(uri)
    true
} catch (_: IllegalArgumentException) {
    false
} catch (_: ActivityNotFoundException) {
    false
}
