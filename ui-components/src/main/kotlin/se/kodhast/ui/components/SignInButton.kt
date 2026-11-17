package se.kodhast.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

/**
 * Utloggat läge i stället för [AccountAvatar] i [AppHeader]: Googles "G" i sina egna färger och
 * [text] (standard "Logga in"), som en konturerad [AppButton] i rubrikradens lägre höjd. [loading]
 * visar knappens arbetar-läge medan inloggningen pågår.
 */
@Composable
fun SignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.sign_in),
    loading: Boolean = false
) = ButtonBase(
    text = text,
    onClick = onClick,
    modifier = modifier,
    variant = ButtonVariant.Outlined,
    icon = R.drawable.ic_google,
    enabled = true,
    loading = loading,
    compact = true,
    contentDescription = null,
    style = LocalAppButtonStyle.current,
    tintIcon = false
)
