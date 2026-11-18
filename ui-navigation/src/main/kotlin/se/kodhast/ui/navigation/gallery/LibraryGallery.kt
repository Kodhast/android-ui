package se.kodhast.ui.navigation.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import se.kodhast.ui.common.AuthGate
import se.kodhast.ui.common.DetailUiState
import se.kodhast.ui.common.EditorEvents
import se.kodhast.ui.common.EditorUiState
import se.kodhast.ui.common.ListUiState
import se.kodhast.ui.common.SyncUiState
import se.kodhast.ui.common.UiError
import se.kodhast.ui.components.AboutCard
import se.kodhast.ui.components.AccountAvatar
import se.kodhast.ui.components.AccountRow
import se.kodhast.ui.components.AddAction
import se.kodhast.ui.components.AddSplitButton
import se.kodhast.ui.components.AppBottomSheet
import se.kodhast.ui.components.AppButton
import se.kodhast.ui.components.AppCalendar
import se.kodhast.ui.components.AppCard
import se.kodhast.ui.components.AppDivider
import se.kodhast.ui.components.AppFilterChip
import se.kodhast.ui.components.AppFloatingToolbar
import se.kodhast.ui.components.AppHeader
import se.kodhast.ui.components.AppIconButton
import se.kodhast.ui.components.AppLoading
import se.kodhast.ui.components.AppMenu
import se.kodhast.ui.components.AppMenuItem
import se.kodhast.ui.components.AppMenuPopup
import se.kodhast.ui.components.AppProgress
import se.kodhast.ui.components.AppPullToRefresh
import se.kodhast.ui.components.AppSegmentedChoice
import se.kodhast.ui.components.AppSnackbarHost
import se.kodhast.ui.components.AppTextField
import se.kodhast.ui.components.AppTopBar
import se.kodhast.ui.components.ArchiveEvent
import se.kodhast.ui.components.AuthGateHost
import se.kodhast.ui.components.BrandMark
import se.kodhast.ui.components.ButtonVariant
import se.kodhast.ui.components.CardSegment
import se.kodhast.ui.components.CheckRow
import se.kodhast.ui.components.ChipRow
import se.kodhast.ui.components.ChoiceChips
import se.kodhast.ui.components.ChoiceGrid
import se.kodhast.ui.components.CollapsibleHeader
import se.kodhast.ui.components.ColorSwatchPicker
import se.kodhast.ui.components.Confetti
import se.kodhast.ui.components.ConfirmDialog
import se.kodhast.ui.components.DateField
import se.kodhast.ui.components.DateTimeRow
import se.kodhast.ui.components.DayMarkerDot
import se.kodhast.ui.components.DayPicker
import se.kodhast.ui.components.DeleteAction
import se.kodhast.ui.components.DeleteConfirmDialog
import se.kodhast.ui.components.DetailHeader
import se.kodhast.ui.components.DiscardChangesDialog
import se.kodhast.ui.components.DurationRow
import se.kodhast.ui.components.EmojiPicker
import se.kodhast.ui.components.EmptyContent
import se.kodhast.ui.components.EmptyExample
import se.kodhast.ui.components.EmptyState
import se.kodhast.ui.components.EntityDetailScreen
import se.kodhast.ui.components.EntityEditScreen
import se.kodhast.ui.components.EntityListScreen
import se.kodhast.ui.components.ErrorSnackbar
import se.kodhast.ui.components.ExampleChips
import se.kodhast.ui.components.ExpandableContent
import se.kodhast.ui.components.FavoriteMark
import se.kodhast.ui.components.FavoriteStar
import se.kodhast.ui.components.FieldError
import se.kodhast.ui.components.Foldout
import se.kodhast.ui.components.GroupLabel
import se.kodhast.ui.components.IconButtonVariant
import se.kodhast.ui.components.InfoPill
import se.kodhast.ui.components.ItemRow
import se.kodhast.ui.components.ItemRowText
import se.kodhast.ui.components.LabeledGroup
import se.kodhast.ui.components.LeadingSlot
import se.kodhast.ui.components.ListArchive
import se.kodhast.ui.components.ListGroup
import se.kodhast.ui.components.ListSubgroup
import se.kodhast.ui.components.LoadErrorState
import se.kodhast.ui.components.MakerMark
import se.kodhast.ui.components.MessageSnackbar
import se.kodhast.ui.components.MorphingCheck
import se.kodhast.ui.components.NoteCard
import se.kodhast.ui.components.NoteIndicator
import se.kodhast.ui.components.NoticeBanner
import se.kodhast.ui.components.PausableRow
import se.kodhast.ui.components.PickerField
import se.kodhast.ui.components.PillRow
import se.kodhast.ui.components.QuantityStepper
import se.kodhast.ui.components.R as ComponentsR
import se.kodhast.ui.components.ReminderTimeRow
import se.kodhast.ui.components.SectionFoldout
import se.kodhast.ui.components.SectionHeader
import se.kodhast.ui.components.SettingsCard
import se.kodhast.ui.components.SettingsPage
import se.kodhast.ui.components.SignInScreen
import se.kodhast.ui.components.SignOutRow
import se.kodhast.ui.components.StatPill
import se.kodhast.ui.components.Suggestion
import se.kodhast.ui.components.SuggestionField
import se.kodhast.ui.components.SwatchMarker
import se.kodhast.ui.components.SwipeBackground
import se.kodhast.ui.components.SwipeToHide
import se.kodhast.ui.components.SwitchControl
import se.kodhast.ui.components.SwitchRow
import se.kodhast.ui.components.TimeField
import se.kodhast.ui.components.TimePickerPopup
import se.kodhast.ui.components.ToolbarAction
import se.kodhast.ui.components.ToolbarItem
import se.kodhast.ui.components.TopBarSize
import se.kodhast.ui.components.UndoRequest
import se.kodhast.ui.components.UndoSnackbar
import se.kodhast.ui.components.UnknownValueNotice
import se.kodhast.ui.components.UpdateRequiredScreen
import se.kodhast.ui.components.ValueTile
import se.kodhast.ui.components.asSplitMenu
import se.kodhast.ui.navigation.AppNavHost
import se.kodhast.ui.navigation.R
import se.kodhast.ui.navigation.TabBackStack
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.Spacing
import se.kodhast.ui.theme.Tone

/**
 * Bibliotekets sektioner i [ComponentGallery]. Varje publik komponent i `ui-components` och
 * `ui-navigation` står här med påhittad data (`UiConsistencyTest` läser filen).
 */
internal fun librarySections(): List<GallerySection> = listOf(
    GallerySection("Knappar och meny") { Buttons() },
    GallerySection("Kort, rader och rubriker") { Rows() },
    GallerySection("Fält, val och mängder") { Fields() },
    GallerySection("Inställningar och konto") { Settings() },
    GallerySection("Framsteg, laddning och navigering") { Progress() },
    GallerySection("Dialog, sheet, meddelanden") { Overlays() },
    GallerySection("Inloggning") { SignIn() },
    GallerySection("Ramar") { Frames() }
)

@Composable
private fun Buttons() {
    GroupLabel("AppButton")
    AppButton("Ny post", {}, Modifier.fillMaxWidth(), icon = ComponentsR.drawable.ic_add)
    AppButton("Sparar …", {}, Modifier.fillMaxWidth(), loading = true)
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
        AppButton("Lägg till", {}, variant = ButtonVariant.Secondary)
        AppButton("Avbryt", {}, variant = ButtonVariant.Text)
        AppButton("Spara", {}, enabled = false)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
        AppButton("Visa alla", {}, variant = ButtonVariant.Outlined, compact = true)
        AppButton("Radera", {}, variant = ButtonVariant.Destructive, compact = true)
    }
    GroupLabel("AppIconButton · AppMenu · AppMenuPopup · AddSplitButton")
    var popup by remember { mutableStateOf(false) }
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s), verticalAlignment = Alignment.CenterVertically) {
        AppIconButton(ComponentsR.drawable.ic_arrow_back, "Tillbaka", {}, variant = IconButtonVariant.Tonal)
        AppIconButton(ComponentsR.drawable.ic_edit, "Redigera", {})
        AppMenu(sampleMenu())
        AppMenu(
            listOf(AppMenuItem("Solkräm", {}), AppMenuItem("Plåster", {}, section = "Gemensamt")),
            contentDescription = "Fler, med avdelning"
        )
        Box {
            AppButton("Egen knapp", { popup = true }, variant = ButtonVariant.Text)
            AppMenuPopup(sampleMenu(), expanded = popup, onDismiss = { popup = false })
        }
    }
    AddSplitButton("Lägg till", {}, menu = listOf(AppMenuItem("Räkna om", {}), AppMenuItem("Dölj avprickade", {})).asSplitMenu())
    AddSplitButton("Lägg till person", {})
}

private fun sampleMenu() = listOf(
    AppMenuItem("Arkivera", {}, ComponentsR.drawable.ic_archive),
    AppMenuItem("Radera", {}, ComponentsR.drawable.ic_delete, destructive = true)
)

@Composable
private fun Rows() {
    var checked by remember { mutableStateOf(false) }
    var on by remember { mutableStateOf(true) }
    var favorite by remember { mutableStateOf(true) }
    AppCard {
        SectionHeader("Mediciner", icon = ComponentsR.drawable.ic_box, count = "2 / 3")
        ItemRow("Anna Berg", subtitle = "3 recept · 1 kosttillskott", leading = { AccountAvatar("Anna Berg", onClick = null) }, navigates = true, onClick = {})
        AppDivider()
        ItemRow("Importera från fil", subtitle = "En tidigare export (.json)", icon = ComponentsR.drawable.ic_download, navigates = true, onClick = {})
        AppDivider()
        ItemRow(
            "Gran Canaria",
            subtitle = "fre 2 okt 2026 · 7 dagar",
            trailing = { InfoPill("Planeras") },
            onClick = {},
            navigates = true,
            below = { NoteIndicator("Kom ihåg passen.", "Gran Canaria") }
        )
        AppDivider()
        ItemRow("Alvedon 500 mg", subtitle = "Minst 4 h mellan", navigates = true, onClick = {}, trailing = { FavoriteStar("Alvedon", favorite, { favorite = !favorite }) })
        AppDivider()
        PausableRow("Solkräm SPF 50", "1 tub per 7 dagar", "Per N dagar", active = true, onClick = {}, pillTone = Tone.Primary)
        AppDivider()
        PausableRow("Hydrokortison 1 %", "1 tub", "Fast antal", active = false, onClick = {}, pillTone = Tone.Primary)
        AppDivider()
        CheckRow("Levaxin 50 µg", checked, { checked = it }, subtitle = "08:00 · 1 tablett", trailing = { InfoPill("1/dag") })
        CheckRow("Omega-3", true, {}, subtitle = "08:00 · 2 kapslar")
        CheckRow(
            "D-vitamin 20 µg", false, {}, subtitle = "Förmiddag · 10:00", note = "Tas med mat.", onClick = {},
            accent = AppTheme.extendedColors.accents.first(),
            menu = listOf(AppMenuItem("Hoppa över", {}, ComponentsR.drawable.ic_close)), below = { InfoPill("Försenat") }
        )
        AppDivider()
        SwitchRow("Påminnelser", on, { on = it }, subtitle = "3 tider om dagen")
        SwitchRow("Dags att packa", on, { on = it }, subtitle = "3 dagar före · 18:00", onClick = {})
    }
    GroupLabel("CardSegment · LeadingSlot · ItemRowText · MorphingCheck · SwitchControl · SwipeBackground")
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        CardSegment(top = true, bottom = false) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LeadingSlot { MorphingCheck(checked) }
                ItemRowText("Eget innehåll i ett segment", "Översta biten", Modifier.weight(1f))
                SwitchControl(on, { on = it }, label = "Påminnelse")
            }
        }
        CardSegment(top = false, bottom = true, onClick = {}) { ItemRowText("Nedersta biten", "Hela segmentet är klickbart", done = true) }
    }
    SwipeBackground("Arkivera", ComponentsR.drawable.ic_archive, Modifier.fillMaxWidth().height(ROW_HEIGHT))
    GroupLabel("InfoPill · StatPill · FavoriteMark · tonerna i appens tema")
    ChipRow {
        AppTheme.extendedColors.tones.keys.forEach { InfoPill(it.key, tone = it) }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s), verticalAlignment = Alignment.CenterVertically) {
        InfoPill("Visa mer", icon = ComponentsR.drawable.ic_chevron_right, onClick = {})
        InfoPill("Betapred")
        FavoriteMark()
    }
    GroupLabel("PillRow – etiketterna sist i en rad")
    AppCard {
        ItemRow(
            "Levaxin 50 µg",
            subtitle = "9 tabletter",
            trailing = {
                PillRow {
                    InfoPill("Ändrad")
                    InfoPill("07:30")
                }
            }
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
        StatPill(ComponentsR.drawable.ic_clock, "7 842", "Steg", tone = Tone.Primary)
        StatPill(ComponentsR.drawable.ic_calendar, "–", "Sömn", tone = Tone.Primary, onClick = {}, onClickLabel = "Begär åtkomst")
    }
    StatPill(ComponentsR.drawable.ic_star, "2 saknas", "Träning · Syremättnad", Tone.Primary, Modifier.fillMaxWidth(), onClick = {}, action = "Ge åtkomst")
    GroupLabel("Foldout · SectionFoldout · CollapsibleHeader · ExpandableContent")
    var foldout by remember { mutableStateOf(true) }
    var section by remember { mutableStateOf(false) }
    var header by remember { mutableStateOf(true) }
    AppCard {
        Foldout("Fler val", foldout, { foldout = !foldout }, summary = "2 valda") {
            Text("Innehållet under rubriken.", style = AppTheme.typography.body)
        }
    }
    AppCard {
        SectionFoldout("Recept", section, { section = !section }, icon = ComponentsR.drawable.ic_box, count = "1") {
            ItemRow("Levaxin 50 µg", subtitle = "1 tablett dagligen", navigates = true, onClick = {})
        }
    }
    AppCard {
        SectionFoldout("Kosttillskott", expanded = true, onToggle = {}, count = "0", emptyText = "Inga kosttillskott än") {}
    }
    AppCard {
        CollapsibleHeader(header, { header = !header }) { Text("Egen rubrikrad", Modifier.weight(1f), style = AppTheme.typography.itemTitle) }
        ExpandableContent(header) { Text("Fälls ut och ihop med fjädring.", style = AppTheme.typography.body) }
    }
}

@Composable
private fun Fields() {
    var name by remember { mutableStateOf("Solkräm") }
    AppTextField(name, { name = it }, "Namn", helper = "Visas i listan")
    AppTextField("", {}, "Namn", error = "Ange ett namn")
    var dose by remember { mutableStateOf("25") }
    AppTextField(dose, { dose = it }, "Höjning", suffix = "mg")
    var medicine by remember { mutableStateOf("Alved") }
    SuggestionField(
        medicine,
        { medicine = it },
        "Namn",
        suggestions = listOf(Suggestion("Alvedon 500 mg", "Filmdragerad tablett", 0..4), Suggestion("Alvedon forte 1 g", "Filmdragerad tablett", 0..4))
            .takeIf { medicine == "Alved" }.orEmpty(),
        onPick = { medicine = "Alvedon" },
        footer = "Från Läkemedelsverkets lista"
    )
    var date by remember { mutableStateOf<LocalDate?>(SAMPLE_DATE) }
    DateField("Startdatum", date, { date = it })
    var end by remember { mutableStateOf<LocalDate?>(null) }
    DateField("Slutdatum", end, { end = it }, emptyLabel = "Periodens slut", onClear = { end = null })
    var time by remember { mutableStateOf(LocalTime(18, 0)) }
    TimeField("Tid", time, { time = it })
    val marked = remember { (1..SAMPLE_DATE.day).filter { it % 4 != 0 }.map { LocalDate(SAMPLE_DATE.year, SAMPLE_DATE.month, it) }.toSet() }
    ChipRow { DayPicker(marked, SAMPLE_DATE, {}) }
    var month by remember { mutableStateOf(SAMPLE_DATE) }
    var pickedDay by remember { mutableStateOf<LocalDate?>(null) }
    AppCard { AppCalendar(month, { month = it }, marked, pickedDay, { pickedDay = it }, today = SAMPLE_DATE, dimFuture = true) }
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.l)) {
        DayMarkerDot(isToday = true, marked = true, selected = false)
        DayMarkerDot(isToday = false, marked = true, selected = false)
    }
    PickerField("Ikon", "🦉", "Välj ikon", ComponentsR.drawable.ic_edit, {})
    var whenDate by remember { mutableStateOf(SAMPLE_DATE) }
    DateTimeRow(whenDate, time, { whenDate = it }, { time = it })
    var minutes by remember { mutableIntStateOf(45) }
    DurationRow(minutes, { minutes = it })
    var reminder by remember { mutableStateOf(true) }
    AppCard {
        ReminderTimeRow("Efter frukost", time, { time = it }, enabled = reminder, onEnabledChange = { reminder = it })
        ReminderTimeRow("Periodslut", LocalTime(9, 0), {})
    }
    var note by remember { mutableStateOf<String?>("Tas med mat.") }
    NoteCard(note, { note = it })
    var form by remember { mutableIntStateOf(0) }
    LabeledGroup("Form") {
        ChoiceChips(listOf(0, 1, 2, 3), form, { form = it }, { FORMS[it] })
    }
    var picked by remember { mutableStateOf(setOf(0, 2)) }
    LabeledGroup("Flera val", helper = "Tryck igen för att avmarkera") {
        ChoiceChips(listOf(0, 1, 2), picked, { item: Int -> picked = if (item in picked) picked - item else picked + item }, { item: Int -> FORMS[item] })
    }
    FieldError("Ingen anslutning just nu. Det du sparar skickas när nätet är tillbaka.")
    var filter by remember { mutableIntStateOf(0) }
    ChipRow {
        listOf("Alla", "Mediciner", "Aktiviteter", "Övrigt").forEachIndexed { i, label -> AppFilterChip(label, selected = i == filter, onClick = { filter = i }) }
        AppFilterChip("Arkiverade", selected = false, onClick = {}, enabled = false)
    }
    ExampleChips(listOf("D-vitamin", "Omega-3", "Magnesium"), {}, chosen = "D-vitamin")
    var theme by remember { mutableIntStateOf(2) }
    AppSegmentedChoice(listOf("Ljust", "Mörkt", "System"), theme, { theme = it })
    var count by remember { mutableIntStateOf(7) }
    QuantityStepper(count, { count = it }, "dagar")
    var doses by remember { mutableIntStateOf(2) }
    QuantityStepper(doses, { doses = it }, "doser per dag", range = 1..24, showLabel = true)
    GroupLabel("ValueTile · UnknownValueNotice")
    ValueTile("För en 7-dagarsresa", "18 tabletter · 2 kartor", supporting = "2 × 1 tablett/dag × (7 + 2) dagar")
    UnknownValueNotice()
    var cell by remember { mutableIntStateOf(1) }
    GroupLabel("ChoiceGrid")
    ChoiceGrid(FORMS.indices.toList(), cell, { cell = it }, { FORMS[it] }, selectionRing = true) { item, _ ->
        Text(FORMS[item], style = AppTheme.typography.body)
    }
    var emoji by remember { mutableStateOf("🦉") }
    GroupLabel("EmojiPicker")
    EmojiPicker(emoji, { emoji = it }, listOf("🦊", "🐻", "🐰", "🦉", "🐯", "🐢", "🦄", "🐧"))
    val accents = AppTheme.extendedColors.accents
    val swatches = accents.indices.map { "accent$it" }
    var swatch by remember { mutableStateOf(swatches.first()) }
    GroupLabel("ColorSwatchPicker – appens accentfärger")
    ColorSwatchPicker(
        swatches,
        swatch,
        { swatch = it },
        { accents[swatches.indexOf(it)] },
        { "Färg ${swatches.indexOf(it) + 1}" },
        SwatchMarker.RingAndCheck(MaterialTheme.colorScheme.surface)
    )
}

@Composable
private fun Settings() {
    val appName = appName()
    AppCard {
        AccountRow("Anna Berg", "anna.berg@exempel.se", photoUrl = null)
        SignOutRow(appName, {})
    }
    GroupLabel("SettingsCard – med fel från en åtgärd i kortet")
    SettingsCard(error = "Ingen anslutning just nu. Försök igen.") {
        ItemRow("Exportera", subtitle = "Spara allt som en fil", onClick = {}, navigates = true)
    }
    AboutCard(appName, "1.0 (bygge 1)")
    NoticeBanner("Påminnelser är avstängda", ComponentsR.drawable.ic_clock, {}, detail = "Tillåt aviseringar i Androids inställningar.", action = "Öppna inställningar")
    NoticeBanner("Allt är sparat", ComponentsR.drawable.ic_check, onClick = null)
    GroupLabel("AccountAvatar · ansluten, utan anslutning, utloggad")
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
        AccountAvatar("Anna Berg", {}, online = true)
        AccountAvatar("Anna Berg", {}, online = false)
        AccountAvatar(null, {})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Progress() {
    AppCard { AppProgress(9, 14, label = { done, total -> "$done av $total packat" }, doneLabel = { "Allt packat!" }) }
    AppCard { AppProgress(14, 14, label = { done, total -> "$done av $total packat" }, doneLabel = { "Allt packat!" }) }
    AppProgress(4, 10, label = { done, total -> "$done / $total" }, doneLabel = { "Klart" }, compact = true)
    Box(Modifier.fillMaxWidth().height(LOADING_HEIGHT)) { AppLoading() }
    var refreshing by remember { mutableStateOf(false) }
    AppPullToRefresh(refreshing, { refreshing = !refreshing }, Modifier.fillMaxWidth().height(LOADING_HEIGHT)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Text("Dra nedåt för att uppdatera", style = AppTheme.typography.body)
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m), verticalAlignment = Alignment.CenterVertically) {
        BrandMark()
        MakerMark()
    }
    GroupLabel("AuthGateHost – inloggningen kontrolleras")
    Box(Modifier.fillMaxWidth().height(LOADING_HEIGHT)) { AuthGateHost(AuthGate.Loading, signIn = {}, updateRequired = {}) {} }
    var tab by remember { mutableIntStateOf(0) }
    AppFloatingToolbar(
        items = TAB_ITEMS,
        selectedIndex = tab,
        onSelect = { tab = it },
        action = ToolbarAction("Lägg till", ComponentsR.drawable.ic_add, ADD_MENU) {}
    )
    AppTopBar("Gran Canaria", size = TopBarSize.Small, onBack = {})
    AppHeader("Resor", TopAppBarDefaults.pinnedScrollBehavior(), subtitle = "fredag 2 oktober", syncing = true, account = { AccountAvatar("Anna Berg", onClick = {}, online = true) })
    GroupLabel("AppNavHost – flikarna, sidhuvudet och plusknappen")
    val backStack = remember { TabBackStack(GALLERY_TABS, GalleryKey.serializer()) }
    Box(Modifier.fillMaxWidth().height(FRAME_HEIGHT)) {
        AppNavHost(
            backStack,
            TAB_ITEMS,
            { key -> galleryEntry(key, backStack) },
            sync = SyncUiState(pending = true, online = true),
            action = ToolbarAction("Lägg till", ComponentsR.drawable.ic_add, ADD_MENU) {},
            account = { AccountAvatar("Anna Berg", onClick = {}, online = true) }
        )
    }
}

@Serializable
private sealed interface GalleryKey : NavKey

@Serializable
private data object GalleryTrips : GalleryKey

@Serializable
private data object GalleryPeople : GalleryKey

@Serializable
private data object GalleryShared : GalleryKey

@Serializable
private data class GalleryDetail(val name: String) : GalleryKey

private val GALLERY_TABS: List<GalleryKey> = listOf(GalleryTrips, GalleryPeople, GalleryShared)

@OptIn(ExperimentalMaterial3Api::class)
private fun galleryEntry(key: GalleryKey, backStack: TabBackStack<GalleryKey>): NavEntry<GalleryKey> = NavEntry(key) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when (key) {
            is GalleryDetail -> AppTopBar(key.name, size = TopBarSize.Small, onBack = { backStack.pop() })

            else -> {
                AppHeader(TAB_ITEMS[GALLERY_TABS.indexOf(key)].label, TopAppBarDefaults.pinnedScrollBehavior())
                AppCard { ItemRow("Gran Canaria", subtitle = "Öppna en undersida", navigates = true, onClick = { backStack.push(GalleryDetail("Gran Canaria")) }) }
            }
        }
    }
}

@Composable
private fun Overlays() {
    var dialog by remember { mutableStateOf(false) }
    var fieldDialog by remember { mutableStateOf(false) }
    var deleteDialog by remember { mutableStateOf(false) }
    var discardDialog by remember { mutableStateOf(false) }
    var timePicker by remember { mutableStateOf(false) }
    var sheet by remember { mutableStateOf(false) }
    var celebrate by remember { mutableStateOf(false) }
    var archived by remember { mutableStateOf<UndoRequest?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<UiError?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    ChipRow {
        AppButton("Dialog", { dialog = true }, variant = ButtonVariant.Secondary)
        AppButton("Dialog med fält", { fieldDialog = true }, variant = ButtonVariant.Secondary)
        AppButton("Radera", { deleteDialog = true }, variant = ButtonVariant.Secondary)
        AppButton("Släng ändringar", { discardDialog = true }, variant = ButtonVariant.Secondary)
        AppButton("Klocka", { timePicker = true }, variant = ButtonVariant.Secondary)
        AppButton("Sheet", { sheet = true }, variant = ButtonVariant.Secondary)
    }
    ChipRow {
        AppButton("Snackbar", { scope.launch { snackbar.showSnackbar("Ingen anslutning just nu.") } }, variant = ButtonVariant.Text)
        AppButton("Meddelande", { message = "Sparat" }, variant = ButtonVariant.Text)
        AppButton("Fel", { error = offlineError() }, variant = ButtonVariant.Text)
        AppButton("Arkivera Solkräm", { archived = UndoRequest("solkram", "Solkräm") }, variant = ButtonVariant.Text)
    }
    UndoSnackbar(archived, snackbar, onUndo = { archived = null }, onDismissed = { archived = null })
    MessageSnackbar(message, snackbar) { message = null }
    ErrorSnackbar(error, snackbar) { error = null }
    if (dialog) {
        ConfirmDialog("Arkivera Solkräm?", "Den syns inte i listan, men finns kvar under Visa arkiverade.", "Arkivera", { dialog = false }, { dialog = false })
    }
    if (fieldDialog) {
        var end by remember { mutableStateOf(SAMPLE_DATE) }
        ConfirmDialog("Avsluta perioden?", "Perioden markeras som avslutad. Posterna står kvar.", "Avsluta", { fieldDialog = false }, { fieldDialog = false }) {
            DateField("Slutdatum", end, { end = it })
        }
    }
    if (deleteDialog) {
        DeleteConfirmDialog(DeleteAction("Radera Alvedon?", "Receptet tas bort för gott. Det går inte att ångra.") {}, onDismiss = { deleteDialog = false })
    }
    if (discardDialog) {
        DiscardChangesDialog(onConfirm = { discardDialog = false }, onDismiss = { discardDialog = false })
    }
    if (timePicker) {
        TimePickerPopup(LocalTime(8, 30), "Tid", onPick = { timePicker = false }, onDismiss = { timePicker = false })
    }
    if (sheet) {
        var minutes by remember { mutableIntStateOf(45) }
        AppBottomSheet(
            "Promenad",
            onDismiss = { sheet = false },
            subtitle = "Ändra tiden och spara",
            dirty = minutes != 45,
            footer = { AppButton("Spara", { sheet = false }) }
        ) {
            QuantityStepper(minutes, { minutes = it }, "minuter")
        }
    }
    EmptyState(ComponentsR.drawable.ic_box, "Inget här än", "Det du lägger till med plusknappen hamnar här.") {
        AppButton("Lägg till", {}, Modifier.fillMaxWidth(), icon = ComponentsR.drawable.ic_add)
    }
    LoadErrorState("Kunde inte hämta listan", offlineError(), onRetry = {})
    Box(Modifier.fillMaxWidth().height(CONFETTI_HEIGHT)) {
        AppButton("Allt klart!", { celebrate = !celebrate }, Modifier.align(Alignment.Center), variant = ButtonVariant.Text)
        Confetti(Modifier.fillMaxSize(), play = celebrate)
    }
    AppSnackbarHost(snackbar)
}

@Composable
private fun SignIn() {
    val appName = appName()
    Box(Modifier.fillMaxWidth().height(FRAME_HEIGHT)) {
        SignInScreen(ComponentsR.drawable.ic_box, appName, "Välkommen!", "Första gången skapas ett konto.", busy = false, error = null, onErrorShown = {}, onSignIn = {})
    }
    Box(Modifier.fillMaxWidth().height(FRAME_HEIGHT)) {
        UpdateRequiredScreen("Den här versionen kan inte längre spara.", "Dina uppgifter finns kvar.", "https://github.com")
    }
}

@Composable
private fun Frames() {
    var undo by remember { mutableStateOf<UndoRequest?>(null) }
    var items by remember { mutableStateOf(listOf("Solkräm", "Plåster", "Febertermometer")) }
    GroupLabel("EntityListScreen · SwipeToHide · UndoSnackbar")
    Box(Modifier.fillMaxWidth().height(FRAME_HEIGHT)) {
        EntityListScreen(
            title = "Gemensamt",
            state = if (items.isEmpty()) ListUiState.Empty else ListUiState.Content(items.toImmutableList()),
            empty = EmptyContent(
                ComponentsR.drawable.ic_box,
                "Inget gemensamt än",
                "Solkräm, plåster, febertermometer.",
                examples = listOf(EmptyExample("Solkräm") { items = listOf("Solkräm") })
            ),
            add = AddAction("Lägg till", {}),
            key = { it },
            archive = ListArchive(undo = undo) { event ->
                when (event) {
                    ArchiveEvent.Undo -> {
                        undo?.let { items = items + it.name }
                        undo = null
                    }

                    ArchiveEvent.UndoDismissed -> undo = null

                    else -> Unit
                }
            }
        ) { item ->
            SwipeToHide(onHide = {
                items = items - item
                undo = UndoRequest(item, item)
            }) { swipe ->
                ItemRow(item, swipe, subtitle = "1 per resenär", navigates = true, onClick = {})
            }
        }
    }
    GroupLabel("EntityListScreen – fel")
    Box(Modifier.fillMaxWidth().height(FRAME_HEIGHT)) {
        EntityListScreen<String>(
            title = "Personer",
            state = ListUiState.Error(offlineError()),
            empty = EmptyContent(ComponentsR.drawable.ic_person, "Inga personer än", ""),
            add = AddAction("Lägg till person", {}),
            key = { it }
        ) {}
    }
    GroupLabel("EntityListScreen – utan lägg till, dagar utan räknare och sidfot")
    Box(Modifier.fillMaxWidth().height(FRAME_HEIGHT)) {
        EntityListScreen(
            title = "Dagbok",
            state = ListUiState.Content(persistentListOf("Idag" to "Efter frukost", "Idag" to "Levaxin 50 µg", "Igår" to "Promenad")),
            empty = EmptyContent(ComponentsR.drawable.ic_note, "Inga poster än", "Lägg till med plusknappen."),
            add = null,
            key = { it.second },
            group = { ListGroup(it.first, cards = true, showCount = false) },
            footer = { AppButton("Visa äldre", {}, Modifier.fillMaxWidth(), variant = ButtonVariant.Secondary, icon = ComponentsR.drawable.ic_expand_more) }
        ) { ItemRow(it.second, onClick = {}) }
    }
    GroupLabel("EntityListScreen – rubrik, grupper, undergrupper, kolumner, hopfällbar")
    val grouped = listOf("Anna" to "Levaxin 50 µg", "Erik" to "Ventoline", "" to "Solkräm", "" to "Plåster", "" to "Myggmedel", "-" to "Halstabletter")
    val accents = AppTheme.extendedColors.accents
    Box(Modifier.fillMaxWidth().height(FRAME_HEIGHT)) {
        EntityListScreen(
            title = "Kreta",
            subtitle = "2 okt · 7 dagar",
            state = ListUiState.Content(grouped.toImmutableList()),
            empty = EmptyContent(ComponentsR.drawable.ic_box, "", ""),
            add = AddAction("", {}),
            key = { it.second },
            onBack = {},
            header = { AppCard { AppProgress(2, 5, label = { done, total -> "$done av $total packat" }, doneLabel = { "Allt packat!" }) } },
            group = { (who, _) ->
                when (who) {
                    "" -> ListGroup("Övrigt", count = "1 / 3", columns = 2)
                    "-" -> ListGroup("Dolda", count = "1", collapsible = true)
                    else -> ListGroup("Mediciner", count = "1 / 2")
                }
            },
            subgroup = { (who, _) -> if (who.length > 1) ListSubgroup(who, accents[if (who == "Anna") 0 else 1 % accents.size]) else null }
        ) { (_, name) -> CheckRow(name, name.startsWith("S") || name.startsWith("L"), {}) }
    }
    GroupLabel("EntityDetailScreen – finns inte längre")
    Box(Modifier.fillMaxWidth().height(FRAME_HEIGHT)) {
        EntityDetailScreen<String>(DetailUiState.Error(UiError(GalleryFailure(), R.string.gallery_error_not_found, retryable = false)), header = { DetailHeader(it) }, onBack = {}) {}
    }
    GroupLabel("EntityEditScreen")
    var name by remember { mutableStateOf("") }
    Box(Modifier.fillMaxWidth().height(FRAME_HEIGHT)) {
        EntityEditScreen(
            title = "Ny sak",
            state = EditorUiState(
                value = name,
                errors = if (name.isBlank()) persistentMapOf("name" to R.string.gallery_name_required) else persistentMapOf(),
                isValid = name.isNotBlank(),
                isDirty = name.isNotEmpty()
            ),
            events = EditorEvents.None,
            onSave = {},
            onClose = {},
            onArchive = {},
            delete = DeleteAction("Radera Solkräm?", "Den tas bort för gott. Det går inte att ångra.") {},
            header = { AccountAvatar(name.ifBlank { null }, onClick = null) }
        ) {
            AppTextField(name, { name = it }, "Namn", error = if (name.isBlank()) "Ange ett namn" else null)
        }
    }
    GroupLabel("SettingsPage")
    Box(Modifier.fillMaxWidth().height(FRAME_HEIGHT)) {
        SettingsPage("Inställningar", onBack = {}) {
            GroupLabel("Packlista")
            SettingsCard { SwitchRow("Flytta avprickade sist", checked = true, onCheckedChange = {}) }
            GroupLabel("Om")
            AboutCard(appName(), "1.0 (bygge 1)")
        }
    }
    GroupLabel("EntityDetailScreen")
    var expanded by remember { mutableStateOf(true) }
    Box(Modifier.fillMaxWidth().height(FRAME_HEIGHT)) {
        EntityDetailScreen(
            state = DetailUiState.Content("Anna Berg"),
            header = { DetailHeader(it, "3 recept · 1 kosttillskott") },
            onBack = {},
            onEdit = {},
            menu = { listOf(AppMenuItem("Arkivera", {}, ComponentsR.drawable.ic_archive)) },
            leading = { AccountAvatar(it, onClick = null) }
        ) {
            AppCard {
                SectionFoldout(
                    "Recept",
                    expanded = expanded,
                    onToggle = { expanded = !expanded },
                    icon = ComponentsR.drawable.ic_box,
                    count = "1",
                    trailing = { AppIconButton(ComponentsR.drawable.ic_add, "Lägg till recept", {}, variant = IconButtonVariant.Tonal) }
                ) {
                    ItemRow("Levaxin 50 µg", subtitle = "1 tablett dagligen", navigates = true, onClick = {})
                }
            }
        }
    }
}

/** Appens namn ur manifestet – så att galleriet visar appens egna texter i `SignOutRow`, `AboutCard` och inloggningen. */
@Composable
private fun appName(): String {
    val context = LocalContext.current
    return remember(context) { context.applicationInfo.loadLabel(context.packageManager).toString() }
}

private class GalleryFailure : Exception("galleri")

private fun offlineError() = UiError(GalleryFailure(), R.string.gallery_error_offline)

private val TAB_ITEMS = listOf(
    ToolbarItem("Resor", ComponentsR.drawable.ic_box),
    ToolbarItem("Personer", ComponentsR.drawable.ic_person),
    ToolbarItem("Gemensamt", ComponentsR.drawable.ic_note, badgeCount = 2)
)

private val ADD_MENU = listOf(
    AppMenuItem("Ny resa", {}, icon = ComponentsR.drawable.ic_box),
    AppMenuItem("Ny person", {}, icon = ComponentsR.drawable.ic_person)
)

private val FORMS = listOf("Tablett", "Kapsel", "Flytande", "Inhalator")
private val SAMPLE_DATE = LocalDate(2026, 10, 2)
private val ROW_HEIGHT = 56.dp
private val LOADING_HEIGHT = 96.dp
private val CONFETTI_HEIGHT = 120.dp
private val FRAME_HEIGHT = 480.dp
