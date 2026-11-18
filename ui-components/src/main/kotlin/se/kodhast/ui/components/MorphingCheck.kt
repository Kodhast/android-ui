@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package se.kodhast.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toPath
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import se.kodhast.ui.theme.AppTheme

/**
 * Det formmorfande krysset – en ring som fjädrar till en fylld "kaka" med bock när [checked].
 * Bara visning, utan semantik: raden som visar det bär rollen ([CheckRow] är en kryssruta, appens
 * egna rader t.ex. ett loggat tillfälle). Läggs i en [LeadingSlot], så att titlarna linjerar. Med temats
 * djup får den ifyllda kakan ljuskant, skimmer och en liten skugga, som tonar in med fyllningen. [interactionSource]
 * är radens tryck: med djup krymper krysset och kakan sjunker in medan raden hålls nedtryckt, som ett chip;
 * `null` (standard) = inget tryck att följa.
 */
@Composable
fun MorphingCheck(checked: Boolean, modifier: Modifier = Modifier, interactionSource: InteractionSource? = null) {
    val progress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "kryss"
    )
    val morph = remember { Morph(MaterialShapes.Circle, MaterialShapes.Cookie9Sided) }
    val color = MaterialTheme.colorScheme.primary
    val depth = AppTheme.depth
    val filled = MaterialShapes.Cookie9Sided.toShape()
    val press = if (depth != null && interactionSource != null) rememberLift(interactionSource) else null
    val pressValue = { press?.value ?: 1f }
    val lift = { progress * pressValue() }
    val sinking = depth?.let { Modifier.pressScale(it, pressValue) } ?: Modifier
    Box(modifier.size(SIZE).then(sinking), contentAlignment = Alignment.Center) {
        // Kakan ritas innanför ringens halva linjebredd; djupet följer dess form.
        if (depth != null) Box(Modifier.size(SIZE - STROKE).outerShadow(filled, depth.shadow, lift))
        Canvas(Modifier.size(SIZE)) {
            val path = morph.toPath(progress)
            val stroke = STROKE.toPx()
            val inner = size.minDimension - stroke
            val offset = Offset(stroke / 2, stroke / 2)
            translate(offset.x, offset.y) {
                scale(inner, pivot = Offset.Zero) {
                    if (progress < 1f) drawPath(path, color, alpha = 1f - progress, style = Stroke(width = stroke / inner))
                    if (progress > 0f) drawPath(path, color, alpha = progress)
                }
            }
        }
        if (depth != null) Box(Modifier.size(SIZE - STROKE).sheen(filled, depth, lift))
        Icon(
            painterResource(R.drawable.ic_check),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(ICON).alpha(progress.coerceIn(0f, 1f))
        )
    }
}

private val SIZE = 40.dp
private val ICON = 22.dp
private val STROKE = 2.5.dp
