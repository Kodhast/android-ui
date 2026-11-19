package se.kodhast.ui.components

import android.content.ClipData
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.SemanticsModifierNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.invalidateSemantics
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.customActions
import kotlinx.coroutines.launch

/**
 * Gör [text] kopierbar: ett **långtryck** lägger den i urklipp med haptisk puls, och TalkBack får åtgärden
 * [label] (t.ex. "Kopiera fondnamn"). [clipLabel] beskriver urklippet för systemet (standard [label]).
 * Android visar själv att något kopierats (från Android 13); biblioteket visar ingen egen bekräftelse.
 *
 * Byggd på gesten i [longPress]: långtrycket läses före klickbara barn och en klickbar förälder, och när det
 * väl blivit ett långtryck förbrukas resten av gesten, så att raden eller kortet inte också öppnas. Ett
 * vanligt tryck och en rullning släpps igenom orörda. Använd inte där långtryck redan betyder något annat.
 */
fun Modifier.copyOnLongPress(text: String, label: String, clipLabel: String = label): Modifier = this then CopyOnLongPressElement(text, label, clipLabel)

private data class CopyOnLongPressElement(val text: String, val label: String, val clipLabel: String) : ModifierNodeElement<CopyOnLongPressNode>() {
    override fun create() = CopyOnLongPressNode(text, label, clipLabel)

    override fun update(node: CopyOnLongPressNode) {
        node.text = text
        node.label = label
        node.clipLabel = clipLabel
        node.invalidateSemantics()
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "copyOnLongPress"
        properties["text"] = text
        properties["label"] = label
    }
}

private class CopyOnLongPressNode(var text: String, var label: String, var clipLabel: String) :
    DelegatingNode(),
    CompositionLocalConsumerModifierNode,
    SemanticsModifierNode {

    init {
        delegate(LongPressNode(haptic = true, onLongPress = ::copy))
    }

    override fun SemanticsPropertyReceiver.applySemantics() {
        customActions = listOf(
            CustomAccessibilityAction(label) {
                copy()
                true
            }
        )
    }

    private fun copy() {
        val entry = ClipEntry(ClipData.newPlainText(clipLabel, text))
        val clipboard = currentValueOf(LocalClipboard)
        coroutineScope.launch { clipboard.setClipEntry(entry) }
    }
}
