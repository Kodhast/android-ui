package se.kodhast.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Minsta tryckyta för allt interaktivt (48 dp) – ett värde för kryss, rensa-knapp, ledande ruta och apparnas egna klickbara ytor. */
val TOUCH_TARGET: Dp = 48.dp

/**
 * Det ledande elementet i en rad (kryss, ikon, avatar) i en ruta på 48 dp, så att titlarna linjerar
 * mellan rader med olika ledande element – [CheckRow], [ItemRow] med `icon` och appens egna rader.
 */
@Composable
fun LeadingSlot(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier.size(TOUCH_TARGET), contentAlignment = Alignment.Center) { content() }
}
