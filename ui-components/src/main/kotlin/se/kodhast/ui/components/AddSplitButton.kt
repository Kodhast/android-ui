package se.kodhast.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.SplitButtonShapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import se.kodhast.ui.components.R
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.Spacing

/**
 * "Lägg till ▾": huvudåtgärden och, om [menu] finns, en pil med fler val. Utan meny
 * blir den en vanlig sekundär [AppButton] med plus – samma plats och utseende.
 *
 * [menu] ritar menyn (vanligen `items.asSplitMenu()`, som visar [AppMenuPopup]), förankrad under knappen:
 * den får om menyn är öppen och vad som stänger den. Med meny har de två delarna den sekundära [AppButton]:s färger
 * och höjd ur [style], och med temats djup dess skugga, skimmer och ljuskant, som sjunker in vid tryck – i stället
 * för Material 3:s skugga och formmorf.
 */
@Composable
fun AddSplitButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    menu: (@Composable (expanded: Boolean, onDismiss: () -> Unit) -> Unit)? = null,
    menuDescription: String = stringResource(R.string.more_options),
    style: AppButtonStyle = LocalAppButtonStyle.current
) {
    if (menu == null) {
        AppButton(label, onClick, modifier, variant = ButtonVariant.Secondary, icon = R.drawable.ic_add, style = style)
        return
    }
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, MaterialTheme.motionScheme.fastSpatialSpec(), label = "pil")
    val colors = secondaryButtonColors(style)
    val depth = AppTheme.depth
    // Utan djup Material 3:s standardformer (som förut); med djup formerna för knappens höjd, rundade som AppButton.
    val shapeHeight = if (depth == null) SplitButtonDefaults.SmallContainerHeight else style.minHeight
    val leading = SplitButtonDefaults.leadingButtonShapesFor(shapeHeight)
    val trailing = SplitButtonDefaults.trailingButtonShapesFor(shapeHeight)
    val leadingPress = remember { MutableInteractionSource() }
    val trailingPress = remember { MutableInteractionSource() }
    // Med djup ritas formen fast (ingen morf vid tryck), så att skimret och skuggan följer den.
    val trailingShape = trailing.checkedShape?.takeIf { expanded } ?: trailing.shape
    val leadingLift = depth?.let { rememberLift(leadingPress) }
    val trailingLift = depth?.let { rememberLift(trailingPress) }
    val leadingDepth = depth?.let { Modifier.depthSurface(it, leading.shape, { leadingLift?.value ?: 1f }, raised = true, shadow = null) } ?: Modifier
    val trailingDepth = depth?.let { Modifier.depthSurface(it, trailingShape, { trailingLift?.value ?: 1f }, raised = true, shadow = null) } ?: Modifier
    // En skugga under hela paret, som en tonad AppButton, som sjunker när någon av delarna trycks. Delarnas egna
    // former (raka innerhörn) ger annars skuggor som skär av varandra i mellanrummet.
    val shadow = depth?.let {
        Modifier.raisedShadow(AppTheme.shapes.pill, it.shadow, Lift.Button, { minOf(leadingLift?.value ?: 1f, trailingLift?.value ?: 1f) }, SECONDARY_SHADOW)
    } ?: Modifier
    val fixed = { shapes: SplitButtonShapes -> if (depth == null) shapes else SplitButtonShapes(shapes.shape, shapes.shape, shapes.checkedShape) }
    val elevation = if (depth == null) ButtonDefaults.buttonElevation() else null
    Box(modifier.then(shadow)) {
        SplitButtonLayout(
            leadingButton = {
                SplitButtonDefaults.LeadingButton(
                    onClick = onClick,
                    modifier = leadingDepth.heightIn(min = style.minHeight),
                    shapes = fixed(leading),
                    colors = colors,
                    elevation = elevation,
                    interactionSource = leadingPress
                ) {
                    Icon(painterResource(R.drawable.ic_add), contentDescription = null, modifier = Modifier.size(ICON))
                    Spacer(Modifier.width(Spacing.s))
                    Text(label, style = AppTheme.typography.button)
                }
            },
            trailingButton = {
                SplitButtonDefaults.TrailingButton(
                    checked = expanded,
                    onCheckedChange = { expanded = it },
                    modifier = trailingDepth.heightIn(min = style.minHeight),
                    shapes = fixed(trailing),
                    colors = colors,
                    elevation = elevation,
                    interactionSource = trailingPress
                ) {
                    Icon(
                        painterResource(R.drawable.ic_expand_more),
                        contentDescription = menuDescription,
                        modifier = Modifier.size(ICON).rotate(rotation)
                    )
                }
            }
        )
        menu(expanded) { expanded = false }
    }
}

private val ICON = 20.dp
