package se.kodhast.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import se.kodhast.ui.theme.AppDepth

// Djupet (AppTheme.depth, android-ui #35) ritas bara här, så att knappar, chips, flikar och kort ser likadana ut.

/** Skuggans storlek: en knapp (en nära och en mjuk skugga) eller något litet och valt (bara den nära). */
internal enum class Lift { Button, Selected }

/**
 * Hur upphöjt något är just nu: 1 i vila och 0 nedtryckt, med Expressives snabba fjäder – den kan slå
 * över lite, så att knappen studsar tillbaka. Inaktivt eller arbetande ([active] = false) står kvar i vila.
 */
@Composable
internal fun rememberLift(interactionSource: InteractionSource, active: Boolean = true): State<Float> {
    val pressed by interactionSource.collectIsPressedAsState()
    return animateFloatAsState(
        targetValue = if (active && pressed) 0f else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "lyft"
    )
}

/** Krymper mot [AppDepth.pressedScale] när [lift] sjunker; [amount] förstärker (ikonknappar) eller dämpar (kort). */
internal fun Modifier.pressScale(depth: AppDepth, lift: () -> Float, amount: Float = 1f): Modifier = graphicsLayer {
    val scale = 1f - (1f - depth.pressedScale) * amount * (1f - lift())
    scaleX = scale
    scaleY = scale
}

/** Skuggan under ett upphöjt element i [color]; tonar bort med trycket. [strength] dämpar den (tonade knappar). */
internal fun Modifier.raisedShadow(shape: Shape, color: Color, kind: Lift, lift: () -> Float, strength: Float = 1f): Modifier {
    val near = dropShadow(shape) {
        radius = NEAR_RADIUS.toPx()
        offset = Offset(0f, NEAR_OFFSET.toPx())
        this.color = color
        alpha = lift().coerceIn(0f, 1f) * strength * if (kind == Lift.Selected) SELECTED_ALPHA else 1f
    }
    return if (kind == Lift.Selected) {
        near
    } else {
        near.dropShadow(shape) {
            radius = FAR_RADIUS.toPx()
            spread = FAR_SPREAD.toPx()
            offset = Offset(0f, FAR_OFFSET.toPx())
            this.color = color
            alpha = lift().coerceIn(0f, 1f) * strength
        }
    }
}

/**
 * Ljuskanten (1 dp innanför överkanten, längs formen) och skimret som tonar ut ovanför texten – ovanpå ytan.
 * Båda tonar bort med trycket.
 */
internal fun Modifier.sheen(shape: Shape, depth: AppDepth, lift: () -> Float = { 1f }): Modifier = drawWithCache {
    val outline = shape.createOutline(size, layoutDirection, this)
    val body = Path().apply { addOutline(outline) }
    val shifted = Path().apply {
        addOutline(outline)
        translate(Offset(0f, HIGHLIGHT.toPx()))
    }
    val edge = Path().apply { op(body, shifted, PathOperation.Difference) }
    val brush = Brush.verticalGradient(0f to depth.sheen, SHEEN_END to depth.sheen.copy(alpha = 0f), endY = size.height)
    onDrawWithContent {
        drawContent()
        val alpha = lift().coerceIn(0f, 1f)
        if (alpha > 0f) {
            clipPath(body) { drawRect(brush, alpha = alpha) }
            drawPath(edge, depth.highlight, alpha = alpha)
        }
    }
}

/**
 * Kortets skugga och hårfina kant ([edge], `null` = ingen). En bit av ett kort ([CardSegment]) ritar dem som om
 * kortet fortsatte ovanför ([top] = false) och under ([bottom] = false) och klipper vid sina egna kanter, så att
 * bitarna i en lista ser ut som ett kort: jämn skugga längs sidorna, inga skarvar och ingen linje mellan bitarna.
 */
@Composable
internal fun Modifier.cardDepth(shape: Shape, shadow: Color, edge: Color?, top: Boolean, bottom: Boolean): Modifier {
    val shadows = LocalGraphicsContext.current.shadowContext
    val near = remember(shadows, shape, shadow) {
        shadows.createDropShadowPainter(shape, Shadow(NEAR_RADIUS, shadow, offset = DpOffset(0.dp, NEAR_OFFSET), alpha = CARD_NEAR_ALPHA))
    }
    val far = remember(shadows, shape, shadow) {
        shadows.createDropShadowPainter(shape, Shadow(CARD_RADIUS, shadow, spread = CARD_SPREAD, offset = DpOffset(0.dp, FAR_OFFSET)))
    }
    return drawWithContent {
        val reach = (CARD_RADIUS + FAR_OFFSET).toPx()
        val above = if (top) 0f else reach
        val below = if (bottom) 0f else reach
        val whole = Size(size.width, size.height + above + below)
        clipRect(left = -reach, top = if (top) -reach else 0f, right = size.width + reach, bottom = if (bottom) size.height + reach else size.height) {
            translate(top = -above) {
                with(far) { draw(whole) }
                with(near) { draw(whole) }
            }
        }
        drawContent()
        if (edge != null) {
            val width = EDGE.toPx()
            val outline = shape.createOutline(Size(whole.width - width, whole.height - width), layoutDirection, this)
            clipRect {
                translate(left = width / 2, top = width / 2 - above) { drawOutline(outline, edge, style = Stroke(width)) }
            }
        }
    }
}

/**
 * Tryckytan (48 dp) i layouten runt elementet, medan djupet ritas på själva elementet – annars hamnar
 * ljuskanten och skuggan på tryckytan runt något lägre (`compact`). [modifier] (storlek och plats) sitter
 * på tryckytan; elementet får dess bredd men sin egen höjd, centrerat.
 */
@Composable
internal fun DepthTouchTarget(modifier: Modifier = Modifier, content: @Composable (Modifier) -> Unit) {
    Box(modifier.minimumInteractiveComponentSize(), contentAlignment = Alignment.Center, propagateMinConstraints = true) {
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
            content(
                Modifier.layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints.copy(minHeight = 0))
                    layout(placeable.width, placeable.height) { placeable.place(0, 0) }
                }
            )
        }
    }
}

/** Ljuskantens tjocklek och var skimret har tonat ut (andel av höjden – ovanför en knapps text). */
private val HIGHLIGHT: Dp = 1.dp
private const val SHEEN_END = 0.35f

/** Den nära skuggan: kort och skarp, precis under kanten. */
private val NEAR_RADIUS: Dp = 2.dp
private val NEAR_OFFSET: Dp = 1.dp

/** Den mjuka skuggan: längre ned, krympt så att den inte sticker ut på sidorna. */
private val FAR_RADIUS: Dp = 14.dp
private val FAR_SPREAD: Dp = (-4).dp
private val FAR_OFFSET: Dp = 6.dp

/** Kortets hårfina kant. */
private val EDGE: Dp = 1.dp

/** Kortens mjuka skugga är bredare och svagare upptill än knapparnas. */
private val CARD_RADIUS: Dp = 18.dp
private val CARD_SPREAD: Dp = (-6).dp
private const val CARD_NEAR_ALPHA = 0.35f

/** Ett valt chip eller segment lyfter mindre än en knapp. */
private const val SELECTED_ALPHA = 0.6f
