package se.kodhast.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import se.kodhast.ui.theme.AppTheme

/** Standardtaken för [AppTextField] – ReseApotekets `TextLimits`. En app med andra tak (samma som rules) skickar sina. */
object AppTextFieldDefaults {
    /** Namn och andra enradiga fält. */
    const val SHORT_MAX_LENGTH = 200

    /** Anteckningar och förklaringar (flerradiga fält). Dagboken: 5000. */
    const val LONG_MAX_LENGTH = 2000
}

/**
 * Det enda textfältet: etikett, och under fältet antingen [error] (röd, läses av TalkBack som
 * fel) eller [helper]. Fält i formulär får sina fel från `EditorState`. Tangentbordet börjar
 * med stor bokstav; ett fält som anger egna [keyboardOptions], t.ex. siffror, styr själv.
 * Texten växer inte förbi [maxLength] – samma tak som appens rules; en inklistring som skulle gå
 * över ignoreras hellre än kapas mitt i. En lagrad längre text kortas aldrig av, men måste kortas
 * under taket för att gå att spara. Ett flerradigt fält växer med texten, högst [maxLines] rader –
 * sedan scrollar det inuti. [suffix] står efter värdet inne i fältet, nedtonat – t.ex. enheten
 * ("25 mg"). [valueIsPlaceholder] tonar ned värdet som en platshållare – för [PickerField], som visar
 * en text i stället för ett tomt värde. Med temats djup får fältet i fokus en mjuk ring utanför kanten.
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    helper: String? = null,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    @DrawableRes trailingIcon: Int? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
    maxLength: Int = if (singleLine) AppTextFieldDefaults.SHORT_MAX_LENGTH else AppTextFieldDefaults.LONG_MAX_LENGTH,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    suffix: String? = null,
    valueIsPlaceholder: Boolean = false
) {
    val card = AppTheme.extendedColors.card
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = card,
        unfocusedContainerColor = card,
        disabledContainerColor = card,
        errorContainerColor = card,
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
    )
    val isError = error != null
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val style = if (valueIsPlaceholder) AppTheme.typography.body.copy(color = muted) else AppTheme.typography.body
    val shape = AppTheme.shapes.field
    // Samma uppbyggnad som Material 3:s OutlinedTextField, men med ytan (containern) åtkomlig – så att
    // fokusringen kan ritas utanför kanten. Etiketten står på kanten och behöver halva sin höjd ovanför.
    val labelLine = MaterialTheme.typography.bodySmall.lineHeight
    val labelHalfHeight = with(LocalDensity.current) { (if (labelLine.isSp) labelLine else LABEL_LINE_FALLBACK).toDp() / 2 }
    // Felet läses som M3:s standardtext ("Ogiltig inmatning"); själva feltexten läses redan under fältet.
    val invalid = stringResource(androidx.compose.ui.R.string.default_error_message)
    CompositionLocalProvider(LocalTextSelectionColors provides colors.textSelectionColors) {
        BasicTextField(
            value = value,
            onValueChange = { if (it.length <= maxLength || it.length < value.length) onValueChange(it) },
            modifier = modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {}
                .padding(top = labelHalfHeight)
                .then(if (isError) Modifier.semantics { error(invalid) } else Modifier)
                .defaultMinSize(minWidth = OutlinedTextFieldDefaults.MinWidth, minHeight = OutlinedTextFieldDefaults.MinHeight),
            enabled = enabled,
            readOnly = readOnly,
            textStyle = style.copy(color = style.color.takeOrElse { colors.textColor(enabled, isError, focused) }),
            cursorBrush = SolidColor(colors.cursorColor(isError)),
            keyboardOptions = keyboardOptions,
            interactionSource = interactionSource,
            singleLine = singleLine,
            maxLines = maxLines,
            decorationBox = { innerTextField ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = value,
                    innerTextField = innerTextField,
                    enabled = enabled,
                    singleLine = singleLine,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = interactionSource,
                    isError = isError,
                    label = { Text(label) },
                    trailingIcon = trailingIcon?.let { { Icon(painterResource(it), contentDescription = null) } },
                    suffix = suffix?.let { { Text(it, style = AppTheme.typography.body, color = muted) } },
                    supportingText = (error ?: helper)?.let { { Text(it, style = AppTheme.typography.itemSubtitle) } },
                    colors = colors,
                    container = {
                        OutlinedTextFieldDefaults.Container(
                            enabled = enabled,
                            isError = isError,
                            interactionSource = interactionSource,
                            modifier = fieldHalo(shape, focused && enabled && !isError && !readOnly),
                            colors = colors,
                            shape = shape
                        )
                    }
                )
            }
        )
    }
}

/**
 * Med temats djup: fokusringen ([focusHalo]) i primärfärgen utanför kanten på ett fält i fokus. Tonar in och ut
 * med Expressives snabba effekt. Felläget och fält som inte kan skrivas i får ingen ring.
 */
@Composable
private fun fieldHalo(shape: Shape, show: Boolean): Modifier {
    val depth = AppTheme.depth ?: return Modifier
    val visible by animateFloatAsState(if (show) 1f else 0f, MaterialTheme.motionScheme.fastEffectsSpec(), label = "fokusring")
    val color = MaterialTheme.colorScheme.primary.copy(alpha = HALO_ALPHA)
    return Modifier.focusHalo(shape, color) { visible }
}

/** Material 3:s radhöjd för bodySmall, om temats inte är i sp – samma reserv som OutlinedTextField. */
private val LABEL_LINE_FALLBACK = 16.sp

/** Fokusringens styrka: primärfärgen svagt, så att den inte konkurrerar med kanten. */
private const val HALO_ALPHA = 0.16f
