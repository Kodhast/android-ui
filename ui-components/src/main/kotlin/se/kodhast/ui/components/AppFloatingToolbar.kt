package se.kodhast.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlin.math.roundToInt
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.Spacing

/**
 * En flik i [AppFloatingToolbar]. [badgeCount] är ett märke med antal i `secondary` bredvid en ovald fliks
 * ikon (Fonders Beslut: "3 nya"); `null` eller 0 ritar inget märke.
 */
data class ToolbarItem(val label: String, @param:DrawableRes val icon: Int, val badgeCount: Int? = null)

/**
 * Huvudknappen bredvid flikarna i [AppFloatingToolbar], t.ex. plusknappen som loggar. Med [menu] öppnar knappen
 * valen i stället för att anropa [onClick] – som rullgardin eller FAB-meny efter [ActionMenuStyle]; ett val eller
 * bakåt stänger menyn. Utan meny (standard) anropar knappen [onClick].
 */
data class ToolbarAction(
    val label: String,
    @param:DrawableRes val icon: Int,
    val menu: List<AppMenuItem>,
    val onClick: () -> Unit
) {
    /** Knappen som anropar [onClick]. */
    constructor(label: String, @DrawableRes icon: Int, onClick: () -> Unit) : this(label, icon, emptyList(), onClick)

    /** Knappen som öppnar [menu]. */
    constructor(label: String, @DrawableRes icon: Int, menu: List<AppMenuItem>) : this(label, icon, menu, {})
}

/**
 * Bakgrunden i [AppFloatingToolbar]; knapparna är desamma i alla stilar (vald flik i `primary`, utom [Filled]).
 *
 * - [Toolbar]: `extendedColors.toolbar` / `onToolbar` – standard, dagens utseende.
 * - [Surface]: kortens yta, ovalda ikoner i `onSurfaceVariant` (Material 3:s standardrad); skuggan avgränsar.
 * - [Tonal]: appens ljusa ton, `primaryContainer` / `onPrimaryContainer` (Material 3 Expressives "vibrant").
 * - [Filled]: appens färg, `primary` / `onPrimary`; vald flik i omvända färger.
 */
enum class ToolbarStyle { Toolbar, Surface, Tonal, Filled }

/**
 * Hur huvudknappens meny ([ToolbarAction.menu]) öppnas.
 *
 * - [Popup]: rullgardin ([AppMenuPopup]) vid knappen – standard.
 * - [Fab]: FAB-meny (Material 3 Expressive): valen växer upp ur knappen som kapslar i `primary` med ikon och
 *   text, knappen blir ett kryss och en ljus slöja läggs över innehållet. Tryck utanför, krysset, bakåt eller
 *   ett val stänger.
 */
enum class ActionMenuStyle { Popup, Fab }

/**
 * Huvudknappens färg i [AppFloatingToolbar] – en accent som skiljer sig från den valda fliken (`primary`).
 *
 * - [Secondary]: `secondary` / `onSecondary` – standard (Dagbokens solgula).
 * - [Tertiary]: `tertiary` / `onTertiary`, för en app vars `secondary` är en neutral ton (ReseApotekets blåa).
 */
enum class ActionColor { Secondary, Tertiary }

/** Plats som verktygsraden tar inklusive marginal. */
val FloatingToolbarClearance: Dp = 96.dp

/** Ingen plats längst ned – verktygsraden syns inte (undersidor). */
val NoBottomClearance: Dp = 0.dp

/**
 * Hur mycket plats den flytande verktygsraden tar längst ned. Navigationen sätter värdet när
 * raden syns; ramarna lägger till det under listan, knappen och snackbaren.
 */
val LocalBottomClearance = compositionLocalOf { NoBottomClearance }

/**
 * Toppnivånavigeringen: en flytande verktygsrad där vald flik visar ikon och namn och övriga bara ikon
 * (med namnet för TalkBack). [action] lägger en gör-något-knapp i [actionColor] (standard `secondary`) till
 * höger om flikarna; utan den (standard) visas bara flikarna. Med temats djup kastar raden en skugga och den valda fliken får
 * skimmer och ljuskant. [style] är radens bakgrund och [actionMenu] hur huvudknappens meny öppnas; de tre
 * kommer som standard ur appens [AppBrandStyle].
 */
@Composable
fun AppFloatingToolbar(
    items: List<ToolbarItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    action: ToolbarAction? = null,
    style: ToolbarStyle = LocalAppBrandStyle.current.toolbarStyle,
    actionMenu: ActionMenuStyle = LocalAppBrandStyle.current.actionMenu,
    actionColor: ActionColor = LocalAppBrandStyle.current.actionColor
) {
    val palette = toolbarPalette(style)
    val colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
        toolbarContainerColor = palette.container,
        toolbarContentColor = palette.content
    )
    val tabs: @Composable RowScope.() -> Unit = {
        items.forEachIndexed { index, item ->
            ToolbarTab(item, palette, selected = index == selectedIndex, onClick = { onSelect(index) })
        }
    }
    // Med temats djup kastar raden en skugga – den flyter över innehållet.
    val raised = AppTheme.depth != null
    if (action == null) {
        HorizontalFloatingToolbar(
            expanded = true,
            modifier = modifier,
            colors = colors,
            expandedShadowElevation = if (raised) TOOLBAR_ELEVATION else FloatingToolbarDefaults.ContainerExpandedElevation,
            content = tabs
        )
    } else {
        HorizontalFloatingToolbar(
            expanded = true,
            floatingActionButton = { ToolbarActionButton(action, actionMenu, actionColor) },
            modifier = modifier,
            colors = colors,
            expandedShadowElevation = if (raised) TOOLBAR_ELEVATION else FloatingToolbarDefaults.ContainerExpandedElevationWithFab,
            content = tabs
        )
    }
}

/** Radens färger i en [ToolbarStyle]: ytan, ovalda flikar och vald flik. */
private data class ToolbarPalette(val container: Color, val content: Color, val selected: Color, val onSelected: Color)

@Composable
private fun toolbarPalette(style: ToolbarStyle): ToolbarPalette {
    val scheme = MaterialTheme.colorScheme
    val extended = AppTheme.extendedColors
    return when (style) {
        ToolbarStyle.Toolbar -> ToolbarPalette(extended.toolbar, extended.onToolbar, scheme.primary, scheme.onPrimary)
        ToolbarStyle.Surface -> ToolbarPalette(extended.card, scheme.onSurfaceVariant, scheme.primary, scheme.onPrimary)
        ToolbarStyle.Tonal -> ToolbarPalette(scheme.primaryContainer, scheme.onPrimaryContainer, scheme.primary, scheme.onPrimary)
        ToolbarStyle.Filled -> ToolbarPalette(scheme.primary, scheme.onPrimary, scheme.onPrimary, scheme.primary)
    }
}

/** Huvudknappen; med meny läses den som utfälld eller hopfälld och menyn förankras vid knappen. */
@Composable
private fun ToolbarActionButton(action: ToolbarAction, menuStyle: ActionMenuStyle, color: ActionColor) {
    var menuOpen by remember { mutableStateOf(false) }
    var bounds by remember { mutableStateOf(Rect.Zero) }
    val hasMenu = action.menu.isNotEmpty()
    val state = expandLabels(menuOpen).state
    val colors = actionColors(color)
    Box(Modifier.onGloballyPositioned { bounds = Rect(it.positionOnScreen(), it.size.toSize()) }) {
        FloatingToolbarDefaults.StandardFloatingActionButton(
            onClick = { if (hasMenu) menuOpen = true else action.onClick() },
            modifier = if (hasMenu) Modifier.semantics { stateDescription = state } else Modifier,
            containerColor = colors.container,
            contentColor = colors.content
        ) { Icon(painterResource(action.icon), contentDescription = action.label) }
        if (hasMenu) {
            when (menuStyle) {
                ActionMenuStyle.Popup -> AppMenuPopup(action.menu, menuOpen, onDismiss = { menuOpen = false })
                ActionMenuStyle.Fab -> if (menuOpen) FabMenu(action, bounds, colors, onDismiss = { menuOpen = false })
            }
        }
    }
}

/** Huvudknappens färger i en [ActionColor]: ytan och ikonen. */
private data class ActionColors(val container: Color, val content: Color)

@Composable
private fun actionColors(color: ActionColor): ActionColors {
    val scheme = MaterialTheme.colorScheme
    return when (color) {
        ActionColor.Secondary -> ActionColors(scheme.secondary, scheme.onSecondary)
        ActionColor.Tertiary -> ActionColors(scheme.tertiary, scheme.onTertiary)
    }
}

/**
 * FAB-menyn: ett fönster över hela skärmen med en ljus slöja, valen staplade ovanför knappen ([anchor], i
 * skärmens koordinater) och krysset ("Stäng") exakt där knappen står, i knappens färger ([colors]). Valen växer fram ur knappens hörn,
 * linjerar med knappens ytterkant (höger, i RTL vänster) och rullar om de inte får plats ovanför knappen.
 */
@Composable
private fun FabMenu(action: ToolbarAction, anchor: Rect, colors: ActionColors, onDismiss: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val closeLabel = stringResource(R.string.close)
    val shown = remember { MutableTransitionState(false) }.apply { targetState = true }
    // Fönstrets eget origo på skärmen: knappen mäts på skärmen, så menyn hamnar rätt även om systemet
    // flyttar fönstret (statusraden, utskurna hörn).
    var origin by remember { mutableStateOf<Offset?>(null) }
    // Riktningen tas från raden och sätts uttryckligen i menyns fönster, som annars inte säkert följer den.
    val direction = LocalLayoutDirection.current
    val rtl = direction == LayoutDirection.Rtl
    Popup(
        popupPositionProvider = WholeWindow,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true, clippingEnabled = false)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides direction) {
            Layout(
                modifier = Modifier.onGloballyPositioned { origin = it.positionOnScreen() },
                content = {
                    // Slöjan stänger vid tryck; TalkBack stänger med krysset eller bakåt i stället.
                    Box(
                        Modifier
                            .background(scheme.background.copy(alpha = SCRIM_ALPHA))
                            .clearAndSetSemantics {}
                            .clickable(interactionSource = null, indication = null, onClick = onDismiss)
                    )
                    Column(
                        Modifier.verticalScroll(rememberScrollState(), reverseScrolling = true),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(Spacing.s)
                    ) {
                        action.menu.forEach { item ->
                            AnimatedVisibility(
                                visibleState = shown,
                                enter = fadeIn(MaterialTheme.motionScheme.fastEffectsSpec()) +
                                    scaleIn(MaterialTheme.motionScheme.fastSpatialSpec(), initialScale = ENTER_SCALE, transformOrigin = TransformOrigin(if (rtl) 0f else 1f, 1f))
                            ) {
                                FabMenuItem(item) {
                                    onDismiss()
                                    item.onClick()
                                }
                            }
                        }
                    }
                    Box(
                        Modifier
                            .clip(FloatingActionButtonDefaults.shape)
                            .background(colors.container)
                            .clickable(onClickLabel = closeLabel, role = Role.Button, onClick = onDismiss)
                            .semantics { contentDescription = closeLabel },
                        contentAlignment = Alignment.Center
                    ) { Icon(painterResource(R.drawable.ic_close), contentDescription = null, tint = colors.content) }
                }
            ) { measurables, constraints ->
                val width = constraints.maxWidth
                val height = constraints.maxHeight
                val at = anchor.translate(-(origin ?: Offset.Zero))
                val left = at.left.roundToInt()
                val right = at.right.roundToInt()
                val top = at.top.roundToInt()
                val gap = Spacing.s.roundToPx()
                val margin = Spacing.l.roundToPx()
                val scrim = measurables[0].measure(Constraints.fixed(width, height))
                val button = measurables[2].measure(Constraints.fixed(at.width.roundToInt().coerceAtLeast(0), at.height.roundToInt().coerceAtLeast(0)))
                // Valen linjerar med knappens ytterkant och får plats fram till motsatta kanten och upp till överkanten.
                val room = (if (rtl) width - left else right) - margin
                val above = top - gap - margin
                val items = measurables[1].measure(Constraints(maxWidth = room.coerceIn(0, width), maxHeight = above.coerceIn(0, height)))
                layout(width, height) {
                    scrim.place(0, 0)
                    // Valen och krysset väntar tills fönstrets läge på skärmen är känt, så att de inte hoppar.
                    if (origin == null) return@layout
                    // place (inte placeRelative): koordinaterna är redan skärmens, även i RTL.
                    items.place(if (rtl) left else right - items.width, top - gap - items.height)
                    button.place(left, top)
                }
            }
        }
    }
}

/** Ett val i FAB-menyn: en kapsel i `primary` med ikon (om valet har en) och text. */
@Composable
private fun FabMenuItem(item: AppMenuItem, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val depth = AppTheme.depth
    Row(
        modifier
            .then(if (depth != null) Modifier.raisedShadow(AppTheme.shapes.pill, depth.shadow, Lift.Button, { 1f }) else Modifier)
            .heightIn(min = FAB_ITEM_HEIGHT)
            .clip(AppTheme.shapes.pill)
            .background(scheme.primary)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = Spacing.l),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s)
    ) {
        item.icon?.let { Icon(painterResource(it), contentDescription = null, tint = scheme.onPrimary, modifier = Modifier.size(ICON)) }
        Text(item.label, style = AppTheme.typography.button, color = scheme.onPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Lägger FAB-menyns fönster över hela fönstret; menyn placerar själv valen vid knappen. */
private object WholeWindow : PopupPositionProvider {
    override fun calculatePosition(anchorBounds: IntRect, windowSize: IntSize, layoutDirection: LayoutDirection, popupContentSize: IntSize): IntOffset = IntOffset.Zero
}

@Composable
private fun ToolbarTab(item: ToolbarItem, palette: ToolbarPalette, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) palette.selected else palette.container
    val content = if (selected) palette.onSelected else palette.content
    // Märket syns bara på en ovald flik; antalet läses med flikens namn, inte en gång till.
    val badge = item.badgeCount?.takeIf { it > 0 && !selected }
    val description = badge?.let { stringResource(R.string.toolbar_badge_format, item.label, it) } ?: item.label
    val depth = AppTheme.depth
    Row(
        Modifier
            .heightIn(min = TAB_HEIGHT)
            .widthIn(min = TAB_HEIGHT)
            .clip(AppTheme.shapes.pill)
            .background(background)
            .then(if (depth != null && selected) Modifier.sheen(AppTheme.shapes.pill, depth) else Modifier)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .semantics { contentDescription = description }
            .animateContentSize(MaterialTheme.motionScheme.defaultSpatialSpec())
            .padding(horizontal = Spacing.m),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs, Alignment.CenterHorizontally)
    ) {
        Icon(painterResource(item.icon), contentDescription = null, tint = content, modifier = Modifier.size(ICON))
        if (selected) Text(item.label, style = AppTheme.typography.button, color = content, maxLines = 1)
        badge?.let { ToolbarBadge(it) }
    }
}

/** Märket med antal på en flik: runt, i `secondary`, och bredare för tvåsiffriga tal. */
@Composable
private fun ToolbarBadge(count: Int) {
    Box(
        Modifier
            .clearAndSetSemantics {}
            .heightIn(min = BADGE)
            .widthIn(min = BADGE)
            .background(MaterialTheme.colorScheme.secondary, AppTheme.shapes.pill)
            .padding(horizontal = BADGE_PADDING),
        contentAlignment = Alignment.Center
    ) {
        Text(count.toString(), style = AppTheme.typography.caption, color = MaterialTheme.colorScheme.onSecondary, maxLines = 1)
    }
}

private val TAB_HEIGHT = 48.dp

/** Verktygsradens höjd med temats djup – en tydlig men mjuk skugga under raden. */
private val TOOLBAR_ELEVATION = 6.dp
private val ICON = 22.dp
private val BADGE = 18.dp
private val BADGE_PADDING = 5.dp

/** FAB-menyns val: samma höjd som knappen. */
private val FAB_ITEM_HEIGHT = 56.dp

/** Slöjan över innehållet medan FAB-menyn är öppen – sidans bakgrund, nästan täckande. */
private const val SCRIM_ALPHA = 0.78f

/** Valen växer fram från lite mindre storlek. */
private const val ENTER_SCALE = 0.8f
