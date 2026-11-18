package se.kodhast.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

/**
 * Kontot överst i inställningarna: foto eller initialer ([AccountAvatar]), [name] och [email] under.
 * Utan namn står e-posten som titel; utan e-post står "Inloggad med Google" under namnet; utan
 * båda bara "Inloggad med Google". Ingen tonad bakgrund (Mats beslut i #54: ReseApotekets form).
 */
@Composable
fun AccountRow(name: String?, email: String?, photoUrl: String?, modifier: Modifier = Modifier) {
    val provider = stringResource(R.string.signed_in_google)
    val title = name ?: email ?: provider
    val subtitle = email?.takeIf { it != title } ?: provider.takeIf { it != title }
    ItemRow(
        title,
        modifier,
        subtitle = subtitle,
        leading = { AccountAvatar(name ?: email, onClick = null, photoUrl = photoUrl) }
    )
}

/**
 * "Logga ut" med utloggningsikonen – likadan i alla appar, bara [appName] i frågan skiljer (Mats 2026-11-18).
 * Frågar med [ConfirmDialog] ("Logga ut?") innan [onSignOut] anropas; Avbryt gör ingenting. Bekräftelseknappen
 * är en vanlig knapp i appens färger: inget raderas, kontots data finns kvar.
 */
@Composable
fun SignOutRow(appName: String, onSignOut: () -> Unit, modifier: Modifier = Modifier) {
    var confirm by rememberSaveable { mutableStateOf(false) }
    ItemRow(stringResource(R.string.sign_out), modifier, icon = R.drawable.ic_logout, onClick = { confirm = true })
    if (confirm) {
        ConfirmDialog(
            title = stringResource(R.string.sign_out_title),
            message = stringResource(R.string.sign_out_message_format, appName),
            confirmLabel = stringResource(R.string.sign_out),
            onConfirm = {
                confirm = false
                onSignOut()
            },
            onDismiss = { confirm = false }
        )
    }
}
