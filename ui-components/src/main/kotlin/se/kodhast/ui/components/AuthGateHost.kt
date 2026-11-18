package se.kodhast.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import se.kodhast.ui.common.AuthGate

/**
 * Appens rot: [gate] avgör vad som visas – [AppLoading] medan inloggningen kontrolleras, [signIn]
 * utloggad eller medan kontot förbereds, [updateRequired] när datan är sparad med en nyare app, och
 * annars [content]. Appens bakgrund sätts här, en gång; skärmarna ritar ovanpå den.
 *
 * [signIn] är normalt [SignInScreen] och [updateRequired] [UpdateRequiredScreen], med appens texter.
 */
@Composable
fun AuthGateHost(
    gate: AuthGate,
    signIn: @Composable () -> Unit,
    updateRequired: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when (gate) {
            AuthGate.SignedOut, AuthGate.NeedsAccount -> signIn()

            AuthGate.UpdateRequired -> updateRequired()

            // Syns bara en kort stund vid start; hänger något ser användaren att appen arbetar.
            AuthGate.Loading -> AppLoading()

            AuthGate.Ready -> content()
        }
    }
}
