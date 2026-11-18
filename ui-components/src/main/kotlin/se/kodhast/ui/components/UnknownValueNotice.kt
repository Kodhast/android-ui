package se.kodhast.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import se.kodhast.ui.theme.AppTheme

/**
 * I stället för ett fält vars värde den här appversionen inte känner till – t.ex. en doseringstyp som en nyare
 * version sparat. Värdet lämnas orört; [text] säger att det inte kan visas (standard "Det här kan inte visas i den
 * här versionen. Uppdatera appen för att ändra.").
 */
@Composable
fun UnknownValueNotice(modifier: Modifier = Modifier, text: String = stringResource(R.string.unknown_value)) {
    Text(text, modifier, style = AppTheme.typography.body, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
