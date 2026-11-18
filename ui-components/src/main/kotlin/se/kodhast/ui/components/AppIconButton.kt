package se.kodhast.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.Tone

enum class IconButtonVariant { Tonal, Plain }

/**
 * Den enda ikonknappen: minst 48 dp och alltid en [contentDescription] för TalkBack. [tone] färgar knappen
 * med temats ton – plattan och ikonen för [IconButtonVariant.Tonal], ikonen för [IconButtonVariant.Plain];
 * standard `null` = Material-standarden (`secondaryContainer` respektive omgivande innehållsfärg). Med temats
 * djup krymper knappen nedtryckt, och den tonade får skimmer, ljuskant och en liten skugga.
 */
@Composable
fun AppIconButton(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: IconButtonVariant = IconButtonVariant.Plain,
    enabled: Boolean = true,
    tone: Tone? = null
) {
    val content: @Composable () -> Unit = { Icon(painterResource(icon), contentDescription) }
    val colors = tone?.let { AppTheme.tone(it) }
    val interactionSource = remember { MutableInteractionSource() }
    val depth = AppTheme.depth
    val sized = if (depth == null) {
        modifier.size(TOUCH_TARGET)
    } else {
        val lift = rememberLift(interactionSource, enabled)
        val liftValue = { lift.value }
        modifier
            .size(TOUCH_TARGET)
            .depthSurface(depth, AppTheme.shapes.pill, liftValue, raised = variant == IconButtonVariant.Tonal && enabled, shadow = Lift.Selected, press = ICON_PRESS)
    }
    when (variant) {
        IconButtonVariant.Tonal -> FilledTonalIconButton(
            onClick,
            sized,
            enabled,
            colors = colors?.let { IconButtonDefaults.filledTonalIconButtonColors(containerColor = it.container, contentColor = it.content) }
                ?: IconButtonDefaults.filledTonalIconButtonColors(),
            interactionSource = interactionSource,
            content = content
        )

        IconButtonVariant.Plain -> IconButton(
            onClick,
            sized,
            enabled,
            colors = colors?.let { IconButtonDefaults.iconButtonColors(contentColor = it.content) } ?: IconButtonDefaults.iconButtonColors(),
            interactionSource = interactionSource,
            content = content
        )
    }
}

/** En liten ikonknapp (och stegarens knoppar) krymper mer än en bred knapp för att trycket ska synas (0,97 → 0,92). */
internal const val ICON_PRESS = 8f / 3f
