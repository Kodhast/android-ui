package se.kodhast.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

/** Den inloggade: namn, e-post och Googles profilfoto – vad som finns. Visas överst i [AccountMenu]. */
data class AccountInfo(val name: String?, val email: String?, val photoUrl: String? = null)

/**
 * Menyn under avataren i sidhuvudet (android-ui #102, variant B) – samma första nivå i alla appar, i en
 * fast ordning som appen inte kan ändra:
 *
 * 1. Kontoraden ([AccountRow]) med en linje under – bara inloggad ([account] satt).
 * 2. **Inställningar** → [onOpenSettings], appens egen inställningsvy. Alltid, också utloggad.
 * 3. **Komponentgalleri** → [onOpenGallery] – bara när den är satt (appen sätter den i debug-bygget).
 * 4. **Om [appName]** → [onOpenAbout], näst sist.
 * 5. En linje och **Logga ut** (frågar "Logga ut?" som [SignOutRow] innan [onSignOut]) när inloggad; utloggad
 *    **Logga in med Google** → [onSignIn], med [signInNote] som undertext (Fonder: "För säkerhetskopia på
 *    Drive"). Utan [onSignIn] står ingen inloggningsrad.
 *
 * Menyn förankras i omgivande `Box` ([AppMenuPopup]); tryck utanför och bakåt stänger ([onDismiss]).
 * Oftast används [AccountMenuAvatar], som är avataren och menyn tillsammans.
 */
@Composable
fun AccountMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    account: AccountInfo?,
    appName: String,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onSignIn: (() -> Unit)?,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    signInNote: String? = null,
    onOpenGallery: (() -> Unit)? = null
) {
    var confirmSignOut by rememberSaveable { mutableStateOf(false) }
    val items = buildList {
        add(AppMenuItem(stringResource(R.string.account_menu_settings), onOpenSettings, R.drawable.ic_account_settings, navigates = true))
        onOpenGallery?.let { add(AppMenuItem(stringResource(R.string.account_menu_gallery), it, R.drawable.ic_account_gallery, navigates = true)) }
        add(AppMenuItem(stringResource(R.string.account_menu_about_format, appName), onOpenAbout, R.drawable.ic_account_about, navigates = true))
        if (account != null) {
            add(AppMenuItem(stringResource(R.string.sign_out), { confirmSignOut = true }, R.drawable.ic_logout, divider = true))
        } else if (onSignIn != null) {
            add(AppMenuItem(stringResource(R.string.sign_in_google), onSignIn, R.drawable.ic_login, supporting = signInNote, divider = true))
        }
    }
    AppMenuPopup(
        items,
        expanded,
        onDismiss,
        modifier,
        header = account?.let { { AccountRow(it.name, it.email, it.photoUrl) } }
    )
    if (confirmSignOut) SignOutConfirm(appName, onSignOut, onDismiss = { confirmSignOut = false })
}

/**
 * Avataren i sidhuvudet med [AccountMenu] under: ett tryck fäller ut menyn. Läggs i `AppNavHost(account = …)`
 * eller [AppHeader]s `account`. [online] är pluppen på avataren ([AccountAvatar]); övriga parametrar går
 * till [AccountMenu].
 */
@Composable
fun AccountMenuAvatar(
    account: AccountInfo?,
    appName: String,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onSignIn: (() -> Unit)?,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    online: Boolean? = null,
    signInNote: String? = null,
    onOpenGallery: (() -> Unit)? = null
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Box(modifier) {
        AccountAvatar(account?.name ?: account?.email, onClick = { expanded = true }, online = online, photoUrl = account?.photoUrl)
        AccountMenu(
            expanded = expanded,
            onDismiss = { expanded = false },
            account = account,
            appName = appName,
            onOpenSettings = onOpenSettings,
            onOpenAbout = onOpenAbout,
            onSignIn = onSignIn,
            onSignOut = onSignOut,
            signInNote = signInNote,
            onOpenGallery = onOpenGallery
        )
    }
}
