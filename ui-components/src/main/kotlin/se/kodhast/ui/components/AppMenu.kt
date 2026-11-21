package se.kodhast.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.IconSize
import se.kodhast.ui.theme.Spacing
import se.kodhast.ui.theme.Tone

/**
 * Ett val i [AppMenu], [AppMenuPopup] eller [AddSplitButton] (via [asSplitMenu]). [destructive] färgar
 * det som radering. [section] är en avdelning med rubrik: där den börjar står en avdelare och rubriken
 * (t.ex. "Recept"); `null` = ingen rubrik. [supporting] är en undertext under etiketten, i [supportingTone]
 * (t.ex. en varning) eller dämpad när den är `null`; [muted] tonar ned etiketten (något som inte går just nu
 * men fortfarande kan väljas). [navigates] ger en pil till höger (valet öppnar en vy, som [ItemRow]s `navigates`).
 * [divider] = en linje före posten utan rubrik (t.ex. före "Logga ut" i [AccountMenu]).
 */
data class AppMenuItem(
    val label: String,
    val onClick: () -> Unit,
    @param:DrawableRes val icon: Int? = null,
    val destructive: Boolean = false,
    val section: String? = null,
    /**
     * Satt: posten är en **kryssrad** i menyn – bocken visas när `true`, platsen hålls tom när `false`,
     * TalkBack läser den som kryssruta, och menyn står kvar öppen så att flera kan väljas.
     * `null` (standard): ett vanligt val som stänger menyn.
     */
    val checked: Boolean? = null,
    val supporting: String? = null,
    val supportingTone: Tone? = null,
    val muted: Boolean = false,
    val navigates: Boolean = false,
    val divider: Boolean = false
)

/** Menyn bakom "Fler val" (⋮): arkivera, radera, visa arkiverade m.m. */
@Composable
fun AppMenu(items: List<AppMenuItem>, modifier: Modifier = Modifier, contentDescription: String = stringResource(R.string.more_options)) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        AppIconButton(R.drawable.ic_more_vert, contentDescription, onClick = { expanded = true })
        AppMenuPopup(items, expanded, onDismiss = { expanded = false })
    }
}

/** Menyn bakom pilen i [AddSplitButton] (`menu`); `null` utan val – då visas ingen pil. */
fun List<AppMenuItem>.asSplitMenu(): (@Composable (expanded: Boolean, onDismiss: () -> Unit) -> Unit)? = if (isEmpty()) null else { expanded, onDismiss -> AppMenuPopup(this, expanded, onDismiss) }

/**
 * Själva menyn, förankrad i omgivande `Box` – delas av [AppMenu], [AddSplitButton], [CheckRow] och appens
 * egna knappar som öppnar en meny. Med temats djup är den en egen popup som flyter som alla fönster
 * ([floatingWindow]): kortens kant och tonad skugga, placerad under (eller över) ankaret som Material 3:s meny.
 * Utan djup är den Material 3:s `DropdownMenu`. [header] står överst, före valen, med en linje under
 * (t.ex. kontoraden i [AccountMenu]); `null` = ingen.
 */
@Composable
fun AppMenuPopup(
    items: List<AppMenuItem>,
    expanded: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null
) {
    if (AppTheme.depth == null) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            modifier = modifier,
            shape = AppTheme.shapes.menu,
            containerColor = AppTheme.extendedColors.card
        ) { MenuItems(items, onDismiss, header) }
    } else {
        FloatingMenu(expanded, onDismiss, modifier) { MenuItems(items, onDismiss, header) }
    }
}

/** Menyns poster, sektioner och eventuella [header] – samma i båda formerna av [AppMenuPopup]. */
@Composable
private fun ColumnScope.MenuItems(items: List<AppMenuItem>, onDismiss: () -> Unit, header: (@Composable () -> Unit)?) {
    if (header != null) {
        header()
        AppDivider(Modifier.padding(vertical = Spacing.xs))
    }
    items.forEachIndexed { index, item ->
        val section = item.section
        if (section != null && section != items.getOrNull(index - 1)?.section) {
            if (index > 0) AppDivider(Modifier.padding(vertical = Spacing.xs))
            GroupLabel(section, Modifier.padding(horizontal = Spacing.s, vertical = Spacing.xs))
        } else if (item.divider && index > 0) {
            AppDivider(Modifier.padding(vertical = Spacing.xs))
        }
        val color = when {
            item.destructive -> MaterialTheme.colorScheme.error
            item.muted -> MaterialTheme.colorScheme.onSurfaceVariant
            else -> MaterialTheme.colorScheme.onSurface
        }
        val supportingColor = item.supportingTone?.let { AppTheme.tone(it).content } ?: MaterialTheme.colorScheme.onSurfaceVariant
        val checked = item.checked
        // En kryssrad visar bocken (eller håller platsen tom) och stänger inte menyn – flera kan väljas.
        val icon = if (checked == null) item.icon else R.drawable.ic_check.takeIf { checked }
        val semantics = if (checked == null) {
            Modifier
        } else {
            Modifier.semantics {
                role = Role.Checkbox
                toggleableState = ToggleableState(checked)
            }
        }
        DropdownMenuItem(
            text = {
                val supporting = item.supporting
                if (supporting == null) {
                    Text(item.label, style = AppTheme.typography.body)
                } else {
                    Column {
                        Text(item.label, style = AppTheme.typography.body)
                        Text(supporting, style = AppTheme.typography.caption, color = supportingColor)
                    }
                }
            },
            onClick = {
                if (checked == null) onDismiss()
                item.onClick()
            },
            modifier = semantics,
            leadingIcon = if (icon == null && checked == null) null else ({ icon?.let { Icon(painterResource(it), contentDescription = null) } ?: Spacer(Modifier.size(IconSize.standard)) }),
            trailingIcon = if (item.navigates) ({ Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null) }) else null,
            colors = MenuDefaults.itemColors(textColor = color, leadingIconColor = color, trailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant)
        )
    }
}

/** Menyn med temats djup: ett fönster med luft för skuggan, som växer fram ur ankarets hörn. */
@Composable
private fun FloatingMenu(expanded: Boolean, onDismiss: () -> Unit, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val shown = remember { MutableTransitionState(false) }
    shown.targetState = expanded
    if (!shown.currentState && !shown.targetState) return
    val shape = AppTheme.shapes.menu
    val room = with(LocalDensity.current) { WINDOW_ROOM.roundToPx() }
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val position = remember(room, rtl) { MenuPosition(room, rtl) }
    Popup(popupPositionProvider = position, onDismissRequest = onDismiss, properties = PopupProperties(focusable = true)) {
        AnimatedVisibility(
            visibleState = shown,
            enter = fadeIn(MaterialTheme.motionScheme.fastEffectsSpec()) +
                scaleIn(MaterialTheme.motionScheme.fastSpatialSpec(), initialScale = ENTER_SCALE, transformOrigin = TransformOrigin(if (rtl) 1f else 0f, 0f)),
            exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec())
        ) {
            Column(
                modifier
                    .floatingWindow(shape)
                    .clip(shape)
                    .background(AppTheme.extendedColors.card)
                    .widthIn(min = MENU_MIN_WIDTH, max = MENU_MAX_WIDTH)
                    .width(IntrinsicSize.Max)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = MENU_VERTICAL_PADDING),
                content = content
            )
        }
    }
}

/**
 * Menyns plats: ytans vänsterkant (i RTL högerkant) mot ankarets, under ankaret – över det om det inte ryms under –
 * och innanför skärmen. Fönstret är [room] större än ytan åt alla håll, så det flyttas lika mycket tillbaka.
 */
private class MenuPosition(private val room: Int, private val rtl: Boolean) : PopupPositionProvider {
    override fun calculatePosition(anchorBounds: IntRect, windowSize: IntSize, layoutDirection: LayoutDirection, popupContentSize: IntSize): IntOffset {
        val width = popupContentSize.width - 2 * room
        val height = popupContentSize.height - 2 * room
        val x = (if (rtl) anchorBounds.right - width else anchorBounds.left).coerceIn(0, (windowSize.width - width).coerceAtLeast(0))
        val below = anchorBounds.bottom
        val above = anchorBounds.top - height
        val y = (if (below + height <= windowSize.height || above < 0) below else above).coerceIn(0, (windowSize.height - height).coerceAtLeast(0))
        return IntOffset(x - room, y - room)
    }
}

private const val ENTER_SCALE = 0.9f
private val MENU_MIN_WIDTH = 112.dp
private val MENU_MAX_WIDTH = 280.dp
private val MENU_VERTICAL_PADDING = 8.dp
