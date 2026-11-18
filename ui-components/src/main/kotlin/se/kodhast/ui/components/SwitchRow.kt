package se.kodhast.ui.components

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import se.kodhast.ui.theme.AppTheme

/**
 * Inställningsrad med växel: hela raden är tryckytan, TalkBack läser den som en växel. Med [onClick] är
 * växeln en egen kontroll (läses med titeln) och resten av raden öppnar detaljer – som en påminnelse i
 * Inställningar ([onClickLabel] läses upp), samma mönster som [CheckRow]. [inactive] tonar ned titel och
 * undertext men inte växeln – den ska gå att se och slå på igen. [enabled] = false låser växeln (visar läget
 * men går inte att slå om; TalkBack läser en inaktiv växel) – en rad med [onClick] går ändå att öppna.
 */
@Composable
fun SwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    inactive: Boolean = false,
    enabled: Boolean = true
) {
    // Med djup når radens tryck växeln, så att knoppen sjunker in också när raden trycks. Utan djup inte:
    // Material 3:s knopp växer annars nedtryckt, och utseendet utan djup ska vara som förut.
    val rowPress = remember { MutableInteractionSource() }
    val switchPress = rowPress.takeIf { AppTheme.depth != null }
    val toggle = Modifier.toggleable(value = checked, interactionSource = rowPress, indication = LocalIndication.current, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
    ItemRowLayout(
        title = title,
        modifier = modifier,
        interaction = if (onClick == null) toggle else Modifier.clickable(onClickLabel = onClickLabel, onClick = onClick),
        subtitle = subtitle,
        textInactive = inactive,
        trailing = {
            if (onClick == null) {
                SwitchControl(checked, onCheckedChange = null, enabled = enabled, interactionSource = switchPress)
            } else {
                SwitchControl(checked, onCheckedChange, label = title, enabled = enabled)
            }
        }
    )
}

/**
 * Själva växeln – en gång för [SwitchRow] och appens kort med reglage. Utan [onCheckedChange] är den en
 * ren indikator (raden växlar och läser upp den); med är den en egen kontroll som TalkBack läser som [label].
 * [enabled] = false visar läget i Material 3:s inaktiva färger. Med temats djup är spåret sänkt; påslaget
 * får det knapparnas skimmer och ljuskant, och knoppen lyfts med knapparnas skugga och sjunker in när växeln
 * (eller raden, via [interactionSource]) trycks – som ett valt chip ligger avslaget platt. Tryckytan på 48 dp
 * ligger då runt växeln i layouten.
 */
@Composable
fun SwitchControl(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null
) {
    val described = if (label == null) Modifier else Modifier.semantics { contentDescription = label }
    val depth = AppTheme.depth
    if (depth == null) {
        Switch(checked = checked, onCheckedChange = onCheckedChange, modifier = modifier.then(described), enabled = enabled, interactionSource = interactionSource)
        return
    }
    val pill = AppTheme.shapes.pill
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val raised = checked && enabled
    val lift = rememberLift(source, raised)
    val liftValue = { lift.value }
    // Knoppens skugga ritas i knoppen men bara utanför den, så att den faller på spåret; ljuskanten ovanpå.
    val knob: (@Composable () -> Unit)? = if (raised) {
        { Box(Modifier.fillMaxSize().outerShadow(pill, depth.shadow, liftValue, Lift.Button).sheen(pill, depth, liftValue)) }
    } else {
        null
    }
    val track = Modifier.sunken(pill, depth.shadow, over = true).then(if (raised) Modifier.sheen(pill, depth) else Modifier)
    val switch = @Composable { placed: Modifier ->
        Switch(checked, onCheckedChange, placed.then(described).then(track), thumbContent = knob, enabled = enabled, interactionSource = source)
    }
    if (onCheckedChange == null) switch(modifier) else DepthTouchTarget(modifier) { switch(it) }
}
