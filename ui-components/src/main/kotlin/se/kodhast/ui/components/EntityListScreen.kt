@file:OptIn(ExperimentalMaterial3Api::class)

package se.kodhast.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import se.kodhast.ui.common.ListUiState
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.Spacing
import se.kodhast.ui.theme.Tone

/**
 * Det tomma tillståndet för en lista: ikon, rubrik och en mening. [examples] är exempel att börja från
 * ("Solkräm") – chips med plus ovanför lägg till-knappen, också när listan saknar en (`add = null`). [actions] är knappar under lägg till-knappen –
 * och de enda knapparna när listan saknar lägg till (`add = null`, t.ex. en flik med plusmeny):
 * "Ny transaktion", "Importera avräkningsnota". Standard inga.
 */
data class EmptyContent(
    @param:DrawableRes val icon: Int,
    val title: String,
    val message: String,
    val examples: List<EmptyExample> = emptyList(),
    val actions: List<EmptyAction> = emptyList()
)

/** Ett exempel i en tom lista: tryck öppnar något nytt att börja från. */
data class EmptyExample(val label: String, val onClick: () -> Unit)

/**
 * Listans lägg till: knappens text och åtgärd hör ihop (ingen knapp utan text). [menu] är valen i
 * split-knappens pil (t.ex. "Ny vid behov-medicin").
 */
data class AddAction(val label: String, val onClick: () -> Unit, val menu: List<AppMenuItem> = emptyList())

/**
 * En grupp i en grupperad lista: rubrik, valfri ikon och ton. [count] ersätter antalet rader i
 * rubriken ("3 / 6" packat); [showCount] = false tar bort räknaren (t.ex. en dag som rubrik).
 * [columns] är det högsta antalet kolumner raderna läggs i – bara så många som får plats med minst
 * 260 dp var, så på en telefon blir det en. [collapsible] gör gruppen hopfälld tills rubriken trycks
 * ("Dolda"). [cards] = true för postkort: varje rad är ett eget kort och rubriken står på bakgrunden
 * ovanför dem, i stället för att gruppen är ett kort med rader. [fold] gör gruppen hopfällbar med läget
 * utifrån (appen håller det, t.ex. under appsessionen) i stället för i listan. [action] är en tonad rund knapp
 * i rubriken mellan räknaren och pilen ("Lägg till i Mediciner"), tryckbar också hopfälld. [emptyText] är
 * texten i en grupp utan rader – bara för grupperna i [EntityListScreen]s `groups`, som alltid visas.
 */
data class ListGroup(
    val title: String,
    @param:DrawableRes val icon: Int? = null,
    val tone: Tone = Tone.Primary,
    val count: String? = null,
    val columns: Int = 1,
    val collapsible: Boolean = false,
    val cards: Boolean = false,
    val showCount: Boolean = true,
    val action: GroupAction? = null,
    val fold: GroupFold? = null,
    val emptyText: String? = null
)

/** Knappen i en grupps rubrik: ikon, etikett (TalkBack) och åtgärd. */
data class GroupAction(@param:DrawableRes val icon: Int, val label: String, val onClick: () -> Unit)

/** Hopfällt läge som appen håller: [expanded] och [onToggle] när rubriken trycks. */
data class GroupFold(val expanded: Boolean, val onToggle: () -> Unit)

/** En undergrupp inom en grupp: namn i en egen färg (t.ex. en person eller ett doseringstillfälle). */
data class ListSubgroup(val title: String, val color: Color)

/**
 * Den enda listskärmen. Samma fyra lägen överallt: laddning → [AppLoading]; tomt → [EmptyState] med
 * knapp; fel → [LoadErrorState] med "Försök igen"; innehåll → rader i kort, valfritt grupperade med
 * [SectionHeader]. "Lägg till" ([add]) står nere till höger ovanför verktygsraden; [add] = `null` för en
 * lista utan lägg till – ingen lägg till-knapp, varken i listan eller i det tomma tillståndet (men
 * [EmptyContent.actions] visas). [errorTitle] ersätter läsfelets rubrik ("Kunde inte hämta listan"). Med [archive] finns
 * "Visa arkiverade" i menyn, svepta rader ([SwipeToHide]) ångras med [UndoSnackbar] och ett fel från
 * arkiveringen visas som meddelande.
 *
 * En undersida har [onBack], [subtitle], egna [actions] i rubrikraden och ett [header] överst i listan.
 * [filter] står fast under rubriken i alla lägen – även tomt och fel – för ett val av vilken lista som
 * visas. [topBarSize] = [TopBarSize.Small] för en underskärm med samma lilla topprad som formulären.
 * [footer] står sist i listan, efter grupperna, och under budskapet i det tomma tillståndet ("Visa äldre").
 * En flik ([onBack] = `null`, [TopBarSize.Large]) har [AppHeader] i stället för den stora toppraden när appen
 * satt [LocalAppIdentity]. Med [onRefresh] dras listan ned för att uppdatera, med indikatorn överst så länge [refreshing] är sant;
 * utan (standard) finns ingen gest. [menu] är valen i ⋮ i rubrikraden (efter [actions]); "Visa arkiverade" läggs
 * sist i samma meny.
 *
 * [groups] är grupper som alltid visas, i den ordningen och före övriga grupper – också utan rader (med
 * [ListGroup.emptyText]) och när listan är tom: då står [empty] som ett kort överst i stället för hela skärmen.
 * En rad hör till en av dem när [group] ger en lika [ListGroup]. Standard inga: som förut.
 *
 * @param group grupp för en rad; grupperna visas i den ordning de först förekommer.
 * @param subgroup undergrupp för en rad inom gruppen, i den ordning de förekommer.
 */
@Composable
fun <T> EntityListScreen(
    title: String,
    state: ListUiState<T>,
    empty: EmptyContent,
    add: AddAction?,
    key: (T) -> Any,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
    group: ((T) -> ListGroup)? = null,
    archive: ListArchive? = null,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    header: (@Composable () -> Unit)? = null,
    subgroup: ((T) -> ListSubgroup?)? = null,
    filter: (@Composable () -> Unit)? = null,
    topBarSize: TopBarSize = TopBarSize.Large,
    footer: (@Composable () -> Unit)? = null,
    refreshing: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    errorTitle: String? = null,
    groups: List<ListGroup> = emptyList(),
    menu: List<AppMenuItem> = emptyList(),
    row: @Composable (T) -> Unit
) {
    val snackbar = remember { SnackbarHostState() }
    ErrorSnackbar(archive?.error, snackbar) { archive?.onEvent(ArchiveEvent.ErrorShown) }
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val clearance = LocalBottomClearance.current
    UndoSnackbar(archive?.undo, snackbar, { archive?.onEvent(ArchiveEvent.Undo) }, { archive?.onEvent(ArchiveEvent.UndoDismissed) })
    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scroll.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            val allActions: @Composable RowScope.() -> Unit = {
                actions()
                val toggle = archive?.takeIf { it.showToggle }?.let {
                    val label = if (it.showing) it.hideLabel ?: stringResource(R.string.hide_archived) else it.showLabel ?: stringResource(R.string.show_archived)
                    AppMenuItem(label, { it.onEvent(ArchiveEvent.ToggleArchived) }, it.toggleIcon ?: R.drawable.ic_unarchive)
                }
                val items = menu + listOfNotNull(toggle)
                if (items.isNotEmpty()) AppMenu(items)
            }
            // Flikläget får Kodhäst-sidhuvudet bara när appen satt sin identitet; annars dagens topprad.
            if (onBack == null && topBarSize == TopBarSize.Large && LocalAppIdentity.current != null) {
                AppHeader(title, scroll, subtitle = subtitle, actions = allActions)
            } else {
                AppTopBar(title, size = topBarSize, subtitle = subtitle, onBack = onBack, scrollBehavior = scroll, actions = allActions)
            }
        },
        snackbarHost = { AppSnackbarHost(snackbar, Modifier.padding(bottom = clearance)) },
        floatingActionButton = {
            if (state is ListUiState.Content && add != null) {
                AddSplitButton(add.label, add.onClick, Modifier.padding(bottom = clearance).testTag(ENTITY_LIST_ADD_TAG), menu = remember(add.menu) { add.menu.asSplitMenu() })
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            filter?.let { Box(Modifier.padding(start = Spacing.l, end = Spacing.l, bottom = Spacing.m)) { it() } }
            AppPullToRefresh(refreshing, onRefresh, Modifier.fillMaxWidth().weight(1f)) {
                when (state) {
                    ListUiState.Loading -> AppLoading()

                    ListUiState.Empty -> if (groups.isNotEmpty()) {
                        val card: @Composable () -> Unit = {
                            header?.let { Box(Modifier.padding(bottom = Spacing.m)) { it() } }
                            AppCard { EmptyState(empty.icon, empty.title, empty.message, compact = true, actions = empty.actions) }
                        }
                        ListContent(emptyList(), key, group, groups, subgroup, card, footer, clearance + Spacing.l, row)
                    } else {
                        EmptyState(
                            icon = empty.icon,
                            title = empty.title,
                            message = empty.message,
                            modifier = Modifier.padding(bottom = clearance),
                            action = if (add == null && footer == null && empty.actions.isEmpty() && empty.examples.isEmpty()) {
                                null
                            } else {
                                { EmptyActions(empty, add, footer) }
                            }
                        )
                    }

                    is ListUiState.Error ->
                        LoadErrorState(errorTitle ?: stringResource(R.string.list_error_title), state.error, onRetry, Modifier.padding(bottom = clearance))

                    is ListUiState.Content -> ListContent(state.items, key, group, groups, subgroup, header, footer, clearance + if (add != null) ADD_BUTTON_CLEARANCE else Spacing.l, row)
                }
            }
        }
    }
}

/**
 * Under budskapet i det tomma tillståndet: exempel, lägg till-knappen (om [add] finns), appens egna
 * knappar ([EmptyContent.actions]), sedan [footer].
 */
@Composable
private fun EmptyActions(empty: EmptyContent, add: AddAction?, footer: (@Composable () -> Unit)?) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.l)) {
        if (empty.examples.isNotEmpty()) {
            ExampleChips(empty.examples, { it.onClick() }, label = { it.label })
        }
        EmptyActionButtons(
            empty.actions,
            add?.let { { AppButton(it.label, it.onClick, Modifier.fillMaxWidth().testTag(ENTITY_LIST_ADD_TAG), icon = R.drawable.ic_add) } }
        )
        footer?.invoke()
    }
}

@Composable
private fun <T> ListContent(
    items: List<T>,
    key: (T) -> Any,
    group: ((T) -> ListGroup)?,
    fixed: List<ListGroup>,
    subgroup: ((T) -> ListSubgroup?)?,
    header: (@Composable () -> Unit)?,
    footer: (@Composable () -> Unit)?,
    bottomPadding: Dp,
    row: @Composable (T) -> Unit
) {
    val groups = remember(items, group, fixed) { grouped(items, group, fixed) }
    // Gruppens nyckel är dess rubrik och ordningsnummer: två grupper med samma rubrik (men t.ex. olika
    // räknare) får inte samma nyckel i listan.
    val ids = remember(groups) {
        val seen = mutableMapOf<String, Int>()
        groups.map { (head, _) -> head?.let { seen.numbered(it.title) } }
    }
    // Utfällda grupper (bara de hopfällbara), efter rubrik; står kvar efter rotation och tillbaka.
    var expanded by rememberSaveable { mutableStateOf(emptyList<String>()) }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        // Kolumner bara där de får plats – annars kapas texten i raderna.
        val fitting = columnsThatFit(maxWidth - Spacing.l * 2)
        // Varje rad är ett eget listobjekt (lat inläsning, animering per rad); korten byggs av bitar.
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = Spacing.l, end = Spacing.l, top = Spacing.xs, bottom = bottomPadding)
        ) {
            header?.let { item(key = "list-header", contentType = "list-header") { Box(Modifier.padding(bottom = Spacing.m)) { it() } } }
            groups.forEachIndexed { index, (head, rows) ->
                if (index > 0) item(key = "gap:$index", contentType = "gap") { Spacer(Modifier.height(Spacing.m)) }
                val open = head?.fold?.expanded ?: (head == null || !head.collapsible || head.title in expanded)
                val emptyText = head?.emptyText?.takeIf { rows.isEmpty() }
                head?.let {
                    item(key = "header:${ids[index]}", contentType = "group-header") {
                        Segment(it.cards, top = true, bottom = !open || (rows.isEmpty() && emptyText == null), Modifier.animateItem(), first = true) {
                            GroupHeader(it, rows.size, open) {
                                it.fold?.onToggle?.invoke() ?: run { expanded = if (open) expanded - it.title else expanded + it.title }
                            }
                        }
                    }
                }
                if (open && head != null && emptyText != null) {
                    item(key = "empty:${ids[index]}", contentType = "group-empty") {
                        Segment(head.cards, top = false, bottom = true, Modifier.animateItem()) { GroupEmpty(emptyText) }
                    }
                }
                if (open) groupRows(head, ids[index], rows, key, subgroup, fitting, row)
            }
            footer?.let { item(key = "list-footer", contentType = "list-footer") { Box(Modifier.padding(top = Spacing.m)) { it() } } }
        }
    }
}

/** Raderna i en grupp: undergrupper med rubrik, och i [ListGroup.columns] kolumner – högst [fitting]. */
private fun <T> LazyListScope.groupRows(
    head: ListGroup?,
    groupId: String?,
    rows: List<T>,
    key: (T) -> Any,
    subgroup: ((T) -> ListSubgroup?)?,
    fitting: Int,
    row: @Composable (T) -> Unit
) {
    val columns = (head?.columns ?: 1).coerceIn(1, fitting)
    val cards = head?.cards == true
    val runs = if (subgroup == null) listOf(null to rows) else rows.runsBy(subgroup)
    // Undergruppens nyckel är dess rubrik (och ordningsnummer om samma namn finns två gånger) –
    // inte första radens, som byts när en rad prickas av och flyttas sist.
    val seen = mutableMapOf<String, Int>()
    val lines = runs.flatMap { (sub, members) ->
        val label = sub?.let { Line.Label<T>(it, seen.numbered(it.title)) }
        listOfNotNull(label) + members.chunked(columns).map { Line.Rows(it) }
    }
    lines.forEachIndexed { position, line ->
        val top = head == null && position == 0
        val bottom = position == lines.lastIndex
        when (line) {
            is Line.Label -> item(key = "sub:$groupId:${line.id}", contentType = "subgroup") {
                Segment(cards, top = top, bottom = bottom, Modifier.animateItem()) { SubgroupLabel(line.subgroup) }
            }

            // Nycklar måste kunna sparas i en Bundle: en rad har sin egen, flera i kolumner en sammansatt sträng.
            // contentType efter antal kolumner och kort: Compose återanvänder bara rader med samma uppbyggnad.
            is Line.Rows -> item(
                key = line.members.singleOrNull()?.let(key) ?: line.members.joinToString("|") { key(it).toString() },
                contentType = "rows:$columns:$cards"
            ) {
                Segment(cards, top = top, bottom = bottom, Modifier.animateItem()) {
                    if (columns == 1) {
                        row(line.members.single())
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                            line.members.forEach { Box(Modifier.weight(1f)) { row(it) } }
                            repeat(columns - line.members.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
    }
}

/**
 * En bit av en grupp: i ett kort ([CardSegment]) eller – för en grupp av postkort ([ListGroup.cards]) –
 * direkt på bakgrunden, med kortavstånd ovanför allt utom rubriken ([first]).
 */
@Composable
private fun Segment(cards: Boolean, top: Boolean, bottom: Boolean, modifier: Modifier = Modifier, first: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    if (cards) {
        Column(if (first) modifier else modifier.padding(top = Spacing.s), content = content)
    } else {
        CardSegment(top = top, bottom = bottom, modifier, content = content)
    }
}

/** "rubrik#n": n:te gången [title] förekommer – två rubriker med samma namn får aldrig samma nyckel. */
private fun MutableMap<String, Int>.numbered(title: String): String = "$title#${merge(title, 1, Int::plus)}"

/** En rad i listan: en undergruppsrubrik eller en eller flera rader (kolumner). */
private sealed interface Line<T> {
    class Label<T>(val subgroup: ListSubgroup, val id: String) : Line<T>
    class Rows<T>(val members: List<T>) : Line<T>
}

/** Följder av rader med samma undergrupp, i ordning. */
private fun <T> List<T>.runsBy(subgroup: (T) -> ListSubgroup?): List<Pair<ListSubgroup?, List<T>>> {
    val runs = mutableListOf<Pair<ListSubgroup?, MutableList<T>>>()
    forEach { item ->
        val sub = subgroup(item)
        if (runs.isNotEmpty() && runs.last().first == sub) runs.last().second += item else runs += sub to mutableListOf(item)
    }
    return runs
}

/**
 * Grupperna i ordning: [fixed] först (också utan rader), sedan övriga i den ordning de först förekommer.
 * Utan [group] en enda grupp utan rubrik.
 */
private fun <T> grouped(items: List<T>, group: ((T) -> ListGroup)?, fixed: List<ListGroup>): List<Pair<ListGroup?, List<T>>> {
    if (group == null) return listOf(null to items)
    val byGroup = items.groupBy(group)
    return fixed.map { it to byGroup[it].orEmpty() } + byGroup.filterKeys { it !in fixed }.toList()
}

@Composable
private fun GroupHeader(group: ListGroup, size: Int, open: Boolean, onToggle: () -> Unit) {
    val count = if (group.showCount) group.count ?: size.toString() else null
    val action: @Composable () -> Unit = {
        group.action?.let { AppIconButton(it.icon, it.label, it.onClick, variant = IconButtonVariant.Tonal) }
    }
    if (!group.collapsible && group.fold == null) {
        if (group.action == null) {
            SectionHeader(group.title, icon = group.icon, count = count, tone = group.tone)
            return
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            SectionHeader(group.title, Modifier.weight(1f), icon = group.icon, count = count, tone = group.tone)
            action()
        }
        return
    }
    CollapsibleHeader(open, onToggle) {
        SectionHeader(group.title, Modifier.weight(1f), icon = group.icon, count = count, tone = group.tone)
        action()
    }
}

/** Raden i en grupp utan rader ("Inget här än"). */
@Composable
private fun GroupEmpty(text: String) {
    Text(
        text,
        style = AppTheme.typography.itemSubtitle,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.m, vertical = Spacing.m)
    )
}

@Composable
private fun SubgroupLabel(subgroup: ListSubgroup) {
    Text(
        subgroup.title,
        style = AppTheme.typography.pill,
        color = subgroup.color,
        modifier = Modifier.padding(start = Spacing.s, top = Spacing.xs).semantics { heading() }
    )
}

/** Lägg till-knappens tagg (i listan och i det tomma tillståndet) – kontraktstestet prövar att den saknas utan [AddAction]. */
const val ENTITY_LIST_ADD_TAG = "entity-list-add"

/** Plats under sista raden så att lägg till-knappen inte täcker den. */
private val ADD_BUTTON_CLEARANCE = 80.dp

/** Hur många kolumner om minst [MIN_COLUMN_WIDTH] som får plats på [width] – alltid minst en. */
internal fun columnsThatFit(width: Dp): Int = ((width + Spacing.xs) / (MIN_COLUMN_WIDTH + Spacing.xs)).toInt().coerceAtLeast(1)

/** En kolumn smalare än så lämnar för lite plats åt texten bredvid krysset. */
private val MIN_COLUMN_WIDTH = 260.dp
