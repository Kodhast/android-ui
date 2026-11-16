package se.kodhast.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.Spacing

/** Den enda platsen där meddelanden visas (skill shared-ui-components). Med temats djup flyter meddelandet. */
@Composable
fun AppSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState, modifier) { data ->
        val shape = AppTheme.shapes.row
        Snackbar(data, modifier = Modifier.padding(Spacing.l).floating(shape, inset = SNACKBAR_INSET), shape = shape)
    }
}

/** Material 3:s `Snackbar(data)` lägger 12 dp luft runt sin yta innanför vår modifierare; djupet ritas på ytan. */
private val SNACKBAR_INSET = 12.dp
