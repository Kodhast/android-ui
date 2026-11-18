package se.kodhast.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import se.kodhast.ui.theme.Spacing

/**
 * Ramen för en inställningssida: liten topprad med [title] (och tillbakapil när [onBack] finns), och under den
 * innehållet som rullar med toppraden – [GroupLabel] följt av sitt kort ([SettingsCard], [AboutCard] …) – med samma
 * marginal och avstånd överallt. Sist luft för flikraden ([LocalBottomClearance]).
 */
@Composable
fun SettingsPage(title: String, modifier: Modifier = Modifier, onBack: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        AppTopBar(title, size = TopBarSize.Small, onBack = onBack)
        Column(Modifier.padding(horizontal = Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            content()
            Spacer(Modifier.height(LocalBottomClearance.current))
        }
    }
}

/**
 * Ett kort på en inställningssida. [error] = ett fel från en åtgärd i just det här kortet (t.ex. "Kunde inte logga ut"),
 * som visas sist i kortet, där åtgärden gjordes. Standard: inget fel.
 */
@Composable
fun SettingsCard(modifier: Modifier = Modifier, error: String? = null, content: @Composable ColumnScope.() -> Unit) {
    AppCard(modifier) {
        content()
        error?.let { FieldError(it, Modifier.padding(Spacing.s)) }
    }
}
