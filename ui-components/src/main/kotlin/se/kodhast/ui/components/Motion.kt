package se.kodhast.ui.components

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext

/**
 * Om systemets "Ta bort animationer" är på (animationsskalan 0): då visas rörelse som stillbild eller text i stället för
 * animation (prickarna, appens egna animationer). Läses när komponenten visas.
 */
@Composable
fun rememberReducedMotion(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) { Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f }
}

/**
 * Låser innehållet för beröring och svep medan [block] är sant (t.ex. ett ark i klart-läget): händelserna förbrukas
 * innan barnen ser dem. Bara pekarhändelser: TalkBacks åtgärder går igenom, så det som låses ska inte ha
 * åtgärder som gör skada. `false` = ingen skillnad.
 */
fun Modifier.blockInput(block: Boolean): Modifier = if (!block) {
    this
} else {
    pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
        }
    }
}
