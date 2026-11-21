package se.kodhast.ui.components

import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Constraints
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Hur en post i [DepartingItems] lämnar efter pausen. */
enum class DepartingExit {
    /** Fälls ihop; posterna under glider upp. */
    Collapse,

    /** Glider åt höger och tonar ut medan den fälls ihop (Dagbokens avbockade doser, #456 variant B). */
    SlideAway
}

object DepartingDefaults {
    /** Hur länge en post står kvar i sin nya version innan den lämnar – lagom för att se bocken. */
    val Linger: Duration = 700.milliseconds
}

/**
 * Poster i den omgivande layouten (t.ex. ett kort med rader) där en post som lämnar [items] stannar en stund
 * (android-ui, Dagboken #456). En post som försvinner ur [items] ritas kvar på sin plats som [departed] (postens nya
 * version, t.ex. dosen som nu är tagen), väntar [linger] och lämnar sedan med temats rörelse enligt [exit];
 * därefter anropas [onDeparted] en gång (appen nickar t.ex. knappen posten gick till, se [nod]).
 *
 * - [departed] = `null` för en post: den försvinner direkt, som en vanlig lista (t.ex. när "Dölj tagna" trycks).
 * - Kommer posten tillbaka i [items] under pausen (ångra) stannar den. Stannande och lämnande poster har samma
 *   struktur, så att en animation i posten (en bock som fjädrar) fortsätter i stället för att börja om.
 * - Med "Ta bort animationer" på ([rememberReducedMotion]) försvinner posten direkt, utan paus och utan [onDeparted]
 *   (som med `departed` = `null`).
 *
 * Nya poster syns direkt, utan animering.
 */
@Composable
fun <T> DepartingItems(
    items: List<T>,
    key: (T) -> Any,
    departed: (T) -> T? = { null },
    linger: Duration = DepartingDefaults.Linger,
    exit: DepartingExit = DepartingExit.Collapse,
    onDeparted: (T) -> Unit = {},
    content: @Composable (T) -> Unit
) {
    val reduced = rememberReducedMotion()
    val tracker = remember { DepartingTracker<T>() }
    var removals by remember { mutableIntStateOf(0) }
    // Diffen räknas om bara när listan byts; trackern är ingen snapshot-state, så det skriver inget under kompositionen.
    remember(items) { tracker.update(items, key, if (reduced) ({ null }) else departed) }
    val shown = remember(items, removals) { tracker.shown(items) }
    val currentOnDeparted by rememberUpdatedState(onDeparted)
    val motion = MaterialTheme.motionScheme
    val exitTransition = remember(exit, motion) {
        val collapse = shrinkVertically(motion.defaultSpatialSpec()) + fadeOut(motion.defaultEffectsSpec())
        when (exit) {
            DepartingExit.Collapse -> collapse
            DepartingExit.SlideAway -> collapse + slideOutHorizontally(motion.defaultSpatialSpec()) { it }
        }
    }
    val enterTransition = remember(motion) { expandVertically(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()) }
    shown.forEach { entry ->
        key(entry.key) {
            val visible = remember { MutableTransitionState(true) }
            LaunchedEffect(entry.leaving) {
                if (entry.leaving) {
                    delay(linger)
                    visible.targetState = false
                } else {
                    visible.targetState = true
                }
            }
            if (entry.leaving && visible.isIdle && !visible.currentState) {
                LaunchedEffect(Unit) {
                    tracker.remove(entry.key)
                    removals++
                    currentOnDeparted(entry.item)
                }
            }
            AnimatedVisibility(
                visibleState = visible,
                enter = if (reduced) EnterTransition.None else enterTransition,
                exit = if (reduced) ExitTransition.None else exitTransition
            ) { content(entry.item) }
        }
    }
}

/** En rad i [DepartingItems]: [item] är postens aktuella version, [leaving] att den är på väg ut. */
private class DepartingEntry<T>(val key: Any, val item: T, val leaving: Boolean)

/** Håller reda på poster som lämnat listan men står kvar: deras nya version och var de stod. */
private class DepartingTracker<T> {
    private class Leaving<T>(val item: T, val index: Int)

    private var previous: List<T> = emptyList()
    private var keyOf: (T) -> Any = { it as Any }
    private val leaving = LinkedHashMap<Any, Leaving<T>>()

    fun update(items: List<T>, key: (T) -> Any, departed: (T) -> T?) {
        keyOf = key
        val current = items.mapTo(HashSet(), key)
        // Ångrat: posten är tillbaka och stannar.
        leaving.keys.retainAll { it !in current }
        previous.forEachIndexed { index, old ->
            val k = key(old)
            if (k !in current && k !in leaving) departed(old)?.let { leaving[k] = Leaving(it, index) }
        }
        previous = items
    }

    fun remove(key: Any) {
        leaving.remove(key)
    }

    fun shown(items: List<T>): List<DepartingEntry<T>> {
        val result = items.mapTo(ArrayList()) { DepartingEntry(keyOf(it), it, leaving = false) }
        leaving.entries.sortedBy { it.value.index }.forEach { (k, l) ->
            result.add(l.index.coerceAtMost(result.size), DepartingEntry(k, l.item, leaving = true))
        }
        return result
    }
}

/**
 * En kort fjädrande nick (växer lite och fjädrar tillbaka) varje gång [trigger] ändras – inte första gången. För att
 * visa vart något tog vägen, t.ex. "Visa tagna (N)" när en avbockad dos glidit dit ([DepartingItems.onDeparted]).
 * Med "Ta bort animationer" på nickar inget; [enabled] = `false` stänger av nicken.
 */
fun Modifier.nod(trigger: Any?, enabled: Boolean = true): Modifier = if (enabled) this then NodElement(trigger) else this

private data class NodElement(val trigger: Any?) : ModifierNodeElement<NodNode>() {
    override fun create() = NodNode(trigger)

    override fun update(node: NodNode) = node.retrigger(trigger)

    override fun InspectorInfo.inspectableProperties() {
        name = "nod"
        properties["trigger"] = trigger
    }
}

private class NodNode(private var trigger: Any?) :
    Modifier.Node(),
    LayoutModifierNode,
    CompositionLocalConsumerModifierNode {
    private val scale = Animatable(1f)

    fun retrigger(new: Any?) {
        if (new == trigger) return
        trigger = new
        if (!isAttached || animationsOff()) return
        coroutineScope.launch {
            scale.animateTo(NOD_SCALE, spring(stiffness = Spring.StiffnessHigh))
            scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
        }
    }

    private fun animationsOff(): Boolean {
        val resolver = currentValueOf(LocalContext).contentResolver
        return Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) {
            placeable.placeWithLayer(0, 0) {
                scaleX = scale.value
                scaleY = scale.value
            }
        }
    }
}

/** Hur mycket [nod] växer på toppen. */
private const val NOD_SCALE = 1.12f
