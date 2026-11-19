package se.kodhast.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
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
import androidx.compose.ui.unit.isSpecified
import se.kodhast.ui.theme.AppDepth
import se.kodhast.ui.theme.AppTheme

// Djupet (AppTheme.depth, android-ui #35) ritas bara här, så att knappar, chips, flikar och kort ser likadana ut.

/** Skuggans storlek: en knapp (en nära och en mjuk skugga) eller något litet och valt (bara den nära). */
internal enum class Lift { Button, Selected }

/**
 * Hur upphöjt något är just nu: 1 i vila och 0 nedtryckt (eller draget, som skjutreglagets knopp), med
 * Expressives snabba fjäder – den kan slå över lite, så att knappen studsar tillbaka. Inaktivt eller arbetande
 * ([active] = false) står kvar i vila.
 */
@Composable
internal fun rememberLift(interactionSource: InteractionSource, active: Boolean = true, followDrag: Boolean = false): State<Float> {
    val pressed by interactionSource.collectIsPressedAsState()
    // Bara skjutreglaget följer dragningar; annars ingen extra insamling per yta (varje kort och knapp i en lista).
    val dragged = if (followDrag) interactionSource.collectIsDraggedAsState().value else false
    return animateFloatAsState(
        targetValue = if (active && (pressed || dragged)) 0f else 1f,
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
 * En fylld eller vald yta med djup – en gång för knappar, ikonknappar, chips, FAB:en, FAB-menyns val, flikar,
 * segment och kalenderns dag: krymper nedtryckt ([press] förstärker, som ikonknapparnas), och [raised] ger
 * skimmer och ljuskant och skuggan [shadow] (`null` = ingen, för det som sitter i en rad) i [strength] – allt
 * sjunker in med trycket. Ovalt, konturerat och inaktivt ([raised] = false) bara krymper.
 */
internal fun Modifier.depthSurface(
    depth: AppDepth,
    shape: Shape,
    lift: () -> Float,
    raised: Boolean,
    shadow: Lift? = Lift.Button,
    strength: Float = 1f,
    press: Float = 1f
): Modifier {
    val pressed = pressScale(depth, lift, press)
    if (!raised) return pressed
    val shaded = shadow?.let { pressed.raisedShadow(shape, depth.shadow, it, lift, strength) } ?: pressed
    return shaded.sheen(shape, depth, lift)
}

/**
 * Temats djup ([depthSurface]) på en yta som trycks via [press]; utan djup oförändrad. [active] = false (inaktivt)
 * står kvar i vila (fjädrar tillbaka dit om det var nedtryckt). [followDrag]: draget räknas som nedtryckt (skjutreglagets knopp).
 */
@Composable
internal fun Modifier.depth(
    shape: Shape,
    press: InteractionSource,
    raised: Boolean = true,
    shadow: Lift? = Lift.Button,
    active: Boolean = true,
    strength: Float = 1f,
    pressAmount: Float = 1f,
    followDrag: Boolean = false
): Modifier {
    val depth = AppTheme.depth ?: return this
    val lift = rememberLift(press, active, followDrag)
    return depthSurface(depth, shape, { lift.value }, raised, shadow, strength, pressAmount)
}

/**
 * Klipper till [shape] och krymper med temats djup medan [source] trycks – för det som bara krymper, som en
 * klickbar pill. `null` = inget tryck att följa (bara klippt).
 */
@Composable
internal fun Modifier.pressable(shape: Shape, source: InteractionSource?): Modifier = (source?.let { depth(shape, it, raised = false) } ?: this).clip(shape)

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
 * Kortets skimmer ([AppDepth.cardSheen]): tonar från [color] till genomskinligt över de översta
 * [CARD_SHEEN_HEIGHT], klippt till formen. Ritas på ytan men bakom innehållet, så att texten inte tonas.
 */
internal fun Modifier.cardSheen(shape: Shape, color: Color): Modifier = drawWithCache {
    val body = Path().apply { addOutline(shape.createOutline(size, layoutDirection, this@drawWithCache)) }
    val height = minOf(CARD_SHEEN_HEIGHT.toPx(), size.height)
    val brush = Brush.verticalGradient(0f to color, 1f to color.copy(alpha = 0f), endY = CARD_SHEEN_HEIGHT.toPx())
    onDrawBehind { clipPath(body) { drawRect(brush, size = Size(size.width, height)) } }
}

/**
 * Skugga bara utanför [shape] – för det som inte täcker hela sin form: en bock som tonar in, en vald ring med
 * luft innanför, reglagets knopp. En vanlig skugga skulle synas genom det genomskinliga. Tonar med [lift].
 * [kind] = [Lift.Selected]: bara den nära skuggan, svagare (som ett valt chip); [Lift.Button]: knapparnas två lager.
 */
@Composable
internal fun Modifier.outerShadow(shape: Shape, color: Color, lift: () -> Float = { 1f }, kind: Lift = Lift.Selected): Modifier {
    val shadows = LocalGraphicsContext.current.shadowContext
    val near = remember(shadows, shape, color, kind) {
        val alpha = if (kind == Lift.Selected) SELECTED_ALPHA else 1f
        shadows.createDropShadowPainter(shape, Shadow(NEAR_RADIUS, color, offset = DpOffset(0.dp, NEAR_OFFSET), alpha = alpha))
    }
    val far = remember(shadows, shape, color, kind) {
        if (kind == Lift.Button) shadows.createDropShadowPainter(shape, Shadow(FAR_RADIUS, color, spread = FAR_SPREAD, offset = DpOffset(0.dp, FAR_OFFSET))) else null
    }
    return drawWithCache {
        val body = Path().apply { addOutline(shape.createOutline(size, layoutDirection, this@drawWithCache)) }
        onDrawBehind {
            val alpha = lift().coerceIn(0f, 1f)
            if (alpha > 0f) {
                clipPath(body, ClipOp.Difference) {
                    far?.let { with(it) { draw(size, alpha) } }
                    with(near) { draw(size, alpha) }
                }
            }
        }
    }
}

/**
 * Ett sänkt spår ([QuantityStepper], reglaget): en svag inre skugga upptill i [color]. [over] = ritas ovanpå
 * innehållet (reglaget, vars spår är innehållet); annars bakom (stegarens knappar ligger ovanpå).
 */
@Composable
internal fun Modifier.sunken(shape: Shape, color: Color, over: Boolean = false): Modifier {
    val shadows = LocalGraphicsContext.current.shadowContext
    val painter = remember(shadows, shape, color) {
        shadows.createInnerShadowPainter(shape, Shadow(SUNKEN_RADIUS, color, offset = DpOffset(0.dp, SUNKEN_OFFSET), alpha = SUNKEN_ALPHA))
    }
    return drawWithContent {
        if (over) drawContent()
        with(painter) { draw(size) }
        if (!over) drawContent()
    }
}

/**
 * Det som flyter över innehållet (snackbar, dialog): en tonad skugga i två lager i [AppDepth.cardShadow] och
 * kortens hårfina kant ([AppDepth.cardEdge]) ovanpå ytan. [inset] = luften som komponenten själv lägger runt
 * sin yta innanför modifieraren (Material 3:s snackbar). [shadow] = false: bara kanten – för en dialog, vars
 * fönster klipper skuggan och vars bakgrund redan är nedtonad.
 */
@Composable
internal fun Modifier.floatingDepth(shape: Shape, depth: AppDepth, inset: Dp = 0.dp, shadow: Boolean = true): Modifier {
    val shadows = LocalGraphicsContext.current.shadowContext
    val near = remember(shadows, shape, depth.cardShadow) {
        shadows.createDropShadowPainter(shape, Shadow(FLOAT_NEAR_RADIUS, depth.cardShadow, offset = DpOffset(0.dp, FLOAT_NEAR_OFFSET), alpha = CARD_NEAR_ALPHA))
    }
    val far = remember(shadows, shape, depth.cardShadow) {
        shadows.createDropShadowPainter(shape, Shadow(FLOAT_RADIUS, depth.cardShadow, spread = FLOAT_SPREAD, offset = DpOffset(0.dp, FLOAT_OFFSET)))
    }
    return drawWithContent {
        val gap = inset.toPx()
        val surface = Size(size.width - 2 * gap, size.height - 2 * gap)
        if (shadow) {
            translate(gap, gap) {
                with(far) { draw(surface) }
                with(near) { draw(surface) }
            }
        }
        drawContent()
        val width = EDGE.toPx()
        val outline = shape.createOutline(Size(surface.width - width, surface.height - width), layoutDirection, this)
        translate(gap + width / 2, gap + width / 2) { drawOutline(outline, depth.cardEdge, style = Stroke(width)) }
    }
}

/**
 * Bottenarkets kant: kortens hårfina kant ([AppDepth.cardEdge]) längs arkets övre kant och hörn, tonad bort nedåt
 * över [SHEET_EDGE_FADE] – arket går ut i skärmens kanter, så att sidorna och botten inte ska ha någon linje.
 */
internal fun Modifier.sheetEdge(shape: Shape, depth: AppDepth): Modifier = drawWithCache {
    val width = EDGE.toPx()
    val outline = shape.createOutline(Size(size.width - width, size.height - width), layoutDirection, this)
    val brush = Brush.verticalGradient(0f to depth.cardEdge, 1f to depth.cardEdge.copy(alpha = 0f), endY = SHEET_EDGE_FADE.toPx())
    onDrawWithContent {
        drawContent()
        translate(width / 2, width / 2) { drawOutline(outline, brush, style = Stroke(width)) }
    }
}

/** Kortens hårfina kant som ram – för ytor med egen kantparameter (menyn). */
internal fun AppDepth.edgeStroke(): BorderStroke = BorderStroke(EDGE, cardEdge)

/** Ett element som flyter med temats djup ([floatingDepth]), annars oförändrat. */
@Composable
internal fun Modifier.floating(shape: Shape, inset: Dp = 0.dp, shadow: Boolean = true): Modifier = AppTheme.depth?.let { floatingDepth(shape, it, inset, shadow) } ?: this

/**
 * Fältets fokusring: en mjuk ring ([HALO] bred) i [color] utanför fältets kant. [visible] tonar den (0–1).
 * Ritas bakom fältet, utanför dess yta, och tar ingen plats i layouten.
 */
internal fun Modifier.focusHalo(shape: Shape, color: Color, visible: () -> Float): Modifier = drawWithCache {
    val width = HALO.toPx()
    val outline = shape.createOutline(Size(size.width + width, size.height + width), layoutDirection, this)
    onDrawBehind {
        val alpha = visible().coerceIn(0f, 1f)
        if (alpha > 0f) translate(-width / 2, -width / 2) { drawOutline(outline, color, alpha = alpha, style = Stroke(width)) }
    }
}

/**
 * Tryckytan (48 dp) i layouten runt elementet, medan djupet ritas på själva elementet – annars hamnar
 * ljuskanten och skuggan på tryckytan runt något lägre (`compact`). Tryckytan är en layoutmodifierare först i
 * elementets egen kedja, inte en omslutande ruta: [modifier] (storlek, plats, `weight` och anroparens semantik som
 * `stateDescription` eller `testTag`) hamnar då på samma nod som elementets klick och roll. Elementet får tryckytans
 * bredd men sin egen höjd, centrerat; det som följer i kedjan (djupet, klicket) ser elementets eget mått.
 */
@Composable
internal fun DepthTouchTarget(modifier: Modifier = Modifier, content: @Composable (Modifier) -> Unit) {
    val minSize = LocalMinimumInteractiveComponentSize.current
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        content(modifier.touchTarget(minSize))
    }
}

private fun Modifier.touchTarget(minSize: Dp): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(minHeight = 0))
    val min = if (minSize.isSpecified) minSize.roundToPx() else 0
    val width = maxOf(placeable.width, min).coerceIn(constraints.minWidth, constraints.maxWidth)
    val height = maxOf(placeable.height, min).coerceIn(constraints.minHeight, constraints.maxHeight)
    layout(width, height) { placeable.place((width - placeable.width) / 2, (height - placeable.height) / 2) }
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

/** Kortets skimmer upptill ([AppDepth.cardSheen]): hur långt ned det tonar ut. */
private val CARD_SHEEN_HEIGHT: Dp = 64.dp

/** Ett valt chip eller segment lyfter mindre än en knapp. */
private const val SELECTED_ALPHA = 0.6f

/** Det sänkta spåret: en kort inre skugga upptill, svagare än knapparnas. */
private val SUNKEN_RADIUS: Dp = 3.dp
private val SUNKEN_OFFSET: Dp = 1.dp
private const val SUNKEN_ALPHA = 0.5f

/** Det som flyter: en nära skugga och en lång, mjuk – längre än kortens, eftersom ytan ligger högre. */
private val FLOAT_NEAR_RADIUS: Dp = 4.dp
private val FLOAT_NEAR_OFFSET: Dp = 2.dp
private val FLOAT_RADIUS: Dp = 24.dp
private val FLOAT_SPREAD: Dp = (-8).dp
private val FLOAT_OFFSET: Dp = 10.dp

/** Bottenarkets kant tonar ut under hörnen. */
private val SHEET_EDGE_FADE: Dp = 40.dp

/** Fokusringens bredd utanför fältets kant. */
private val HALO: Dp = 4.dp
