package se.kodhast.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

/**
 * Anteckningskortet i ett formulär: ett [AppCard] med en [Foldout] som i stängt läge visar början av texten (eller
 * "Lägg till en anteckning") och utfälld ett flerradigt [AppTextField]. Ingen anteckning är `null` – ett tömt fält ger
 * `null` via [onNoteChange], så att formuläret aldrig sparar en tom text. [maxLength] är textens tak (appens, samma
 * som dess regler; Dagboken `TextLimits.LONG`). [error] (formulärets validering, t.ex. en lagrad text över taket)
 * står under fältet och syns också i stängt läge.
 */
@Composable
fun NoteCard(
    note: String?,
    onNoteChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
    error: String? = null,
    maxLength: Int = AppTextFieldDefaults.LONG_MAX_LENGTH
) {
    AppCard(modifier) {
        NoteField(note.orEmpty(), { text -> onNoteChange(text.ifEmpty { null }) }, initiallyExpanded, error, maxLength)
    }
}

@Composable
private fun NoteField(note: String, onNoteChange: (String) -> Unit, initiallyExpanded: Boolean, error: String?, maxLength: Int) {
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    val label = stringResource(R.string.note)
    Column {
        Foldout(
            title = if (note.isBlank()) stringResource(R.string.note_placeholder) else label,
            expanded = expanded,
            onToggle = { expanded = !expanded },
            summary = note
        ) {
            AppTextField(note, onNoteChange, label, error = error, singleLine = false, maxLength = maxLength)
        }
        if (!expanded && error != null) FieldError(error)
    }
}
