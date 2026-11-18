package se.kodhast.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Back stacken med en egen stack per flik:
 * - appen startar på första fliken i [tabs]; varje flik minns sin undersida när man byter flik;
 * - tillbaka i roten av en annan flik går till första fliken, tillbaka i dess rot lämnar appen ([pop] ger `false`);
 * - att välja fliken man står på går till flikens rot.
 *
 * [entries] är det som `NavDisplay` visar: första flikens stack och, om en annan flik är vald, dess stack.
 * Nycklarna är appens (`@Serializable` och [NavKey]); [serializer] är deras gemensamma typ, t.ex.
 * `AppKey.serializer()` för ett `sealed interface AppKey : NavKey`. Läget sparas med [saver] – se [rememberTabBackStack].
 */
@Stable
class TabBackStack<K : NavKey> private constructor(
    val tabs: List<K>,
    private val serializer: KSerializer<K>,
    current: K,
    stacks: List<List<K>>
) {
    init {
        require(tabs.isNotEmpty() && tabs.distinct() == tabs) { "flikarna ska vara minst en, utan dubbletter" }
        require(current in tabs) { "$current är ingen flik" }
    }

    private val start = tabs.first()
    private val stacks: Map<K, SnapshotStateList<K>> =
        tabs.withIndex().associate { (i, tab) -> tab to mutableStateListOf<K>().apply { addAll(stacks.getOrNull(i) ?: listOf(tab)) } }

    /** Fliken som är vald. */
    var currentTab: K by mutableStateOf(current)
        private set

    constructor(tabs: List<K>, serializer: KSerializer<K>) : this(tabs, serializer, requireNotNull(tabs.firstOrNull()) { "flikarna ska vara minst en" }, emptyList())

    val entries: List<K>
        get() = if (currentTab == start) stack(start).toList() else stack(start) + stack(currentTab)

    /** Verktygsraden syns bara i en fliks rot; undersidor döljer den. */
    val atTopLevel: Boolean
        get() = entries.last() in tabs

    fun select(tab: K) {
        require(tab in tabs) { "$tab är ingen flik" }
        if (tab == currentTab) stack(tab).removeRange(1, stack(tab).size) else currentTab = tab
    }

    /** Öppnar [key] i fliken man står på; ligger den redan överst (ett dubbeltryck under animationen) händer inget. */
    fun push(key: K) {
        require(key !in tabs) { "Flikar väljs med select, inte push" }
        val stack = stack(currentTab)
        if (stack.last() != key) stack.add(key)
    }

    /**
     * Byter till [tab] och öppnar [key] överst där – det en notis eller djuplänk gör. Det som redan
     * var öppet i fliken (t.ex. ett formulär med osparade ändringar) ligger kvar under och nås med tillbaka.
     */
    fun openInTab(tab: K, key: K) {
        require(tab in tabs) { "$tab är ingen flik" }
        currentTab = tab
        push(key)
    }

    /**
     * Stänger [key] om den är den översta skärmen – det en skärm anropar när den stänger sig själv.
     * Ett andra anrop (dubbeltryck, eller "klar" efter "släng ändringar") stänger då inte skärmen
     * under. `true` om skärmen stängdes.
     */
    fun popIfTop(key: K): Boolean = entries.last() == key && pop()

    /** [popIfTop] för [key] som återanrop – skärmens `onClose`/`onBack`; [then] körs bara om skärmen stängdes. */
    fun closer(key: K, then: () -> Unit = {}): () -> Unit = { if (popIfTop(key)) then() }

    /** Går tillbaka ett steg; `false` om det inte finns något kvar (appen ska stängas). */
    fun pop(): Boolean {
        val stack = stack(currentTab)
        when {
            stack.size > 1 -> stack.removeAt(stack.lastIndex)
            currentTab != start -> currentTab = start
            else -> return false
        }
        return true
    }

    private fun stack(tab: K): SnapshotStateList<K> = stacks.getValue(tab)

    /** Läget som JSON: `{"current": <flik>, "stacks": [[<nycklar>], …]}` i flikarnas ordning. */
    fun save(): String = json.encodeToString(Saved.serializer(serializer), Saved(currentTab, tabs.map { stack(it).toList() }))

    @Serializable
    private class Saved<K>(val current: K, val stacks: List<List<K>>)

    companion object {
        private val json = Json

        /**
         * Läget ur [save]. Ett läge som inte går att läsa eller inte passar flikarna – sparat av en
         * äldre version, med en nyckel som inte finns längre eller med andra flikar – ger startläget
         * i stället för en krasch.
         */
        fun <K : NavKey> restore(saved: String, tabs: List<K>, serializer: KSerializer<K>): TabBackStack<K> = try {
            val state = json.decodeFromString(Saved.serializer(serializer), saved)
            require(state.stacks.size == tabs.size)
            state.stacks.forEachIndexed { i, stack -> require(stack.firstOrNull() == tabs[i] && stack.drop(1).none { it in tabs }) }
            TabBackStack(tabs, serializer, state.current, state.stacks)
        } catch (_: IllegalArgumentException) { // SerializationException ärver av den
            TabBackStack(tabs, serializer)
        }

        /** Sparas som JSON, så att flikarnas stackar överlever rotation och processdöd. */
        fun <K : NavKey> saver(tabs: List<K>, serializer: KSerializer<K>): Saver<TabBackStack<K>, String> = Saver(save = { it.save() }, restore = { restore(it, tabs, serializer) })
    }
}

/** Back stacken för [tabs], sparad över rotation och processdöd. */
@Composable
fun <K : NavKey> rememberTabBackStack(tabs: List<K>, serializer: KSerializer<K>): TabBackStack<K> {
    val saver = remember(tabs, serializer) { TabBackStack.saver(tabs, serializer) }
    return rememberSaveable(saver = saver) { TabBackStack(tabs, serializer) }
}
