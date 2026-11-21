package se.kodhast.ui.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import se.kodhast.ui.common.DetailUiState
import se.kodhast.ui.common.EditorEvents
import se.kodhast.ui.common.EditorUiState
import se.kodhast.ui.common.ListUiState
import se.kodhast.ui.components.testing.Blue
import se.kodhast.ui.components.testing.Green
import se.kodhast.ui.components.testing.SampleSwatches
import se.kodhast.ui.components.testing.captureLightAndDark
import se.kodhast.ui.components.testing.captureScreenLightAndDark
import se.kodhast.ui.components.testing.clickWithoutRipple
import se.kodhast.ui.components.testing.sampleNotFound
import se.kodhast.ui.components.testing.sampleOffline
import se.kodhast.ui.theme.Spacing
import se.kodhast.ui.theme.Tone

/** Skärmdumpar av ramarna (ljust + mörkt): listans, formulärets och detaljens lägen. */
@RunWith(RobolectricTestRunner::class)
class FrameScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    private val empty = EmptyContent(
        R.drawable.ic_box,
        "Inga familjeartiklar än",
        "Solkräm, plåster, febertermometer – saker hela familjen delar.",
        examples = listOf(EmptyExample("Solkräm", {}), EmptyExample("Plåster", {}))
    )
    private val add = AddAction("Lägg till", {})
    private val items = listOf("Solkräm" to "Krämer & salvor", "Hydrokortison 1 %" to "Krämer & salvor", "Plåster" to "Plåster & förband")

    private fun group(item: Pair<String, String>) = ListGroup(item.second, tone = if (item.second.startsWith("Plåster")) Tone.Green else Tone.Primary)

    @Test
    fun `EntityListScreen - lägen`() {
        captureLightAndDark("EntityListScreen_laddar") { EntityListScreen<String>("Familjeartiklar", ListUiState.Loading, empty, add, key = { it }) {} }
        captureLightAndDark("EntityListScreen_tom") { EntityListScreen<String>("Familjeartiklar", ListUiState.Empty, empty, add, key = { it }) {} }
        captureLightAndDark("EntityListScreen_fel") { EntityListScreen<String>("Familjeartiklar", ListUiState.Error(sampleOffline()), empty, add, key = { it }) {} }
        captureLightAndDark("EntityListScreen_innehall") {
            EntityListScreen(
                "Familjeartiklar",
                ListUiState.Content(items.toImmutableList()),
                empty,
                AddAction("Lägg till", {}, listOf(AppMenuItem("Ny från recept", {}))),
                key = { it.first },
                group = ::group,
                archive = ListArchive()
            ) { ItemRow(it.first, subtitle = "1 tub per 7 dagar", navigates = true, onClick = {}) }
        }
    }

    @Test
    fun `EntityListScreen - underskärm med filter, postkort, hopfälld grupp och sidfot, utan lägg till`() {
        val rows = listOf("Alvedon 500 mg" to "Vid behov", "Ipren 200 mg" to "Vid behov", "Levaxin" to "Avslutade")
        captureLightAndDark("EntityListScreen_filter") {
            EntityListScreen(
                "Mediciner",
                ListUiState.Content(rows.toImmutableList()),
                empty,
                add = null,
                key = { it.first },
                group = { if (it.second == "Avslutade") ListGroup(it.second, collapsible = true, tone = Tone.Blue) else ListGroup(it.second, cards = true, showCount = false) },
                subtitle = "sön 4 okt",
                onBack = {},
                topBarSize = TopBarSize.Small,
                filter = { AppSegmentedChoice(listOf("Aktiva", "Alla"), 0, {}) },
                footer = { AppButton("Visa äldre", {}, variant = ButtonVariant.Text) }
            ) { AppCard { ItemRow(it.first, subtitle = "Högst 4 per dygn") } }
        }
    }

    @Test
    @Config(qualifiers = "w360dp-h800dp-mdpi")
    fun `EntityListScreen - grupper med plus, en hopfälld, en tom och rubriken på 360 dp`() {
        val creams = ListGroup("Krämer & salvor", R.drawable.ic_box, count = "1 / 2", action = GroupAction(R.drawable.ic_add, "Lägg till i Krämer & salvor", {}), fold = GroupFold(true, {}))
        val supplements = ListGroup("Kosttillskott", R.drawable.ic_box, Tone.Green, count = "1 / 2", action = GroupAction(R.drawable.ic_add, "Lägg till i Kosttillskott", {}), fold = GroupFold(false, {}))
        val other = ListGroup("Övrigt", R.drawable.ic_box, Tone.Blue, count = "0 / 0", action = GroupAction(R.drawable.ic_add, "Lägg till i Övrigt", {}), fold = GroupFold(true, {}), emptyText = "Inget här än")
        captureLightAndDark("EntityListScreen_gruppknapp") {
            EntityListScreen(
                "Gran Canaria",
                ListUiState.Content(persistentListOf("Solkräm SPF 50", "Hydrokortison 1 %", "Magnesium")),
                empty,
                add = null,
                key = { it },
                group = { if (it == "Magnesium") supplements else creams },
                groups = listOf(creams, supplements, other),
                onBack = {},
                topBarSize = TopBarSize.Small,
                menu = listOf(AppMenuItem("Räkna om", {}))
            ) { CheckRow(it, checked = it.startsWith("S"), onCheckedChange = {}) }
        }
    }

    @Test
    fun `EntityListScreen - tom lista med grupper`() {
        val groups = listOf(
            ListGroup("Mediciner", R.drawable.ic_box, action = GroupAction(R.drawable.ic_add, "Lägg till i Mediciner", {}), fold = GroupFold(true, {}), emptyText = "Inget här än"),
            ListGroup("Övrigt", R.drawable.ic_box, Tone.Blue, action = GroupAction(R.drawable.ic_add, "Lägg till i Övrigt", {}), fold = GroupFold(true, {}), emptyText = "Inget här än")
        )
        captureLightAndDark("EntityListScreen_tom_grupper") {
            EntityListScreen<String>(
                "Gran Canaria",
                ListUiState.Empty,
                EmptyContent(R.drawable.ic_box, "Inget att packa än", "Räkna om för att få förslag från resenärernas mediciner.", actions = listOf(EmptyAction("Räkna om", {}))),
                add = null,
                key = { it },
                group = { groups.first() },
                groups = groups,
                onBack = {},
                topBarSize = TopBarSize.Small
            ) {}
        }
    }

    @Test
    @Config(qualifiers = "w840dp-h600dp-mdpi")
    fun `EntityListScreen - bred skärm ger kolumner`() {
        val rows = listOf("Solkräm SPF 50", "Plåster", "Febertermometer", "Resetabletter", "Myggmedel")
        captureLightAndDark("EntityListScreen_bred") {
            EntityListScreen(
                "Gran Canaria",
                ListUiState.Content(rows.toImmutableList()),
                empty,
                add,
                key = { it },
                group = { ListGroup("Övrigt", columns = 3, count = "2 / 5") },
                subgroup = { ListSubgroup("Anna", SampleSwatches.color(SampleSwatches.hexes[0])) }
            ) { CheckRow(it, checked = it.startsWith("S") || it.startsWith("P"), onCheckedChange = {}) }
        }
    }

    @Test
    fun `EntityEditScreen - ogiltig och giltig`() {
        captureLightAndDark("EntityEditScreen_ogiltig") {
            EntityEditScreen(
                "Ny familjeartikel",
                EditorUiState("", persistentMapOf("name" to R.string.sample_error_unknown), isValid = false, isDirty = true),
                EditorEvents.None,
                {},
                {},
                formError = "Värdet nekades av servern"
            ) {
                AppTextField("", {}, "Namn", error = "Ange ett namn")
                QuantityStepper(1, {}, "Antal tuber")
            }
        }
        captureLightAndDark("EntityEditScreen_giltig") {
            EntityEditScreen("Promenad", EditorUiState("Promenad", isValid = true, isDirty = true), EditorEvents.None, {}, {}, onArchive = {}, horizontalMargin = Spacing.l) {
                AppTextField("Promenad", {}, "Namn")
                SwitchRow("Visa i snabbval", checked = true, onCheckedChange = {})
            }
        }
        captureLightAndDark("EntityEditScreen_laser_fel") {
            EntityEditScreen("Solkräm", EditorUiState("", loadError = sampleOffline()), EditorEvents.None, {}, {}) {}
        }
    }

    @Test
    fun `EntityEditScreen - släng ändringar`() = rule.captureScreenLightAndDark("EntityEditScreen_slang", open = { onNodeWithContentDescription("Tillbaka").clickWithoutRipple() }) {
        EntityEditScreen("Solkräm", EditorUiState("Solkräm 50", isValid = true, isDirty = true), EditorEvents.None, {}, {}) {
            AppTextField("Solkräm 50", {}, "Namn")
        }
    }

    @Test
    fun `EntityEditScreen - radera`() = rule.captureScreenLightAndDark(
        "EntityEditScreen_radera",
        open = {
            onNodeWithContentDescription("Fler val").clickWithoutRipple()
            waitForIdle()
            onNodeWithText("Radera").clickWithoutRipple()
        }
    ) {
        EntityEditScreen("Solkräm", EditorUiState("Solkräm"), EditorEvents.None, {}, {}, delete = DeleteAction("Radera Solkräm?", "Det går inte att ångra.") {}) {
            AppTextField("Solkräm", {}, "Namn")
        }
    }

    @Test
    fun `EntityDetailScreen - innehåll, fel och finns inte`() {
        captureLightAndDark("EntityDetailScreen_innehall") {
            EntityDetailScreen(
                DetailUiState.Content("Anna"),
                header = { DetailHeader(it, "3 recept · 1 kosttillskott") },
                onBack = {},
                onEdit = {},
                menu = { listOf(AppMenuItem("Arkivera", {})) }
            ) {
                AppCard {
                    SectionHeader("Recept", count = "1")
                    ItemRow("Levaxin 50 µg", subtitle = "1 tablett dagligen", navigates = true, onClick = {})
                }
            }
        }
        captureLightAndDark("EntityDetailScreen_fel") {
            EntityDetailScreen<String>(DetailUiState.Error(sampleOffline()), header = { DetailHeader(it) }, onBack = {}) {}
        }
        captureLightAndDark("EntityDetailScreen_finns_inte") {
            EntityDetailScreen<String>(DetailUiState.Error(sampleNotFound()), header = { DetailHeader(it) }, onBack = {}) {}
        }
    }

    /** Listan rullad in under toppraden: överkanten suddas progressivt (#105). Varje bild rullar en bit till. */
    @Test
    fun `EntityListScreen - rullad under toppraden`() = rule.captureLightAndDark(
        "EntityListScreen_rullad",
        settle = {
            onRoot().performTouchInput { swipeUp(startY = bottom * 0.8f, endY = bottom * 0.3f) }
            waitForIdle()
        }
    ) {
        val many = (1..24).map { "Artikel $it" to if (it % 3 == 0) "Plåster & förband" else "Krämer & salvor" }
        EntityListScreen(
            "Familjeartiklar",
            ListUiState.Content(many.toImmutableList()),
            empty,
            add,
            key = { it.first },
            group = ::group
        ) { ItemRow(it.first, subtitle = "1 tub per 7 dagar", navigates = true, onClick = {}) }
    }

    @Test
    fun `EntityDetailScreen - flikläge`() = captureLightAndDark("EntityDetailScreen_flik") {
        EntityDetailScreen(
            DetailUiState.Content(persistentListOf("Levaxin 50 µg", "Alvedon 500 mg")),
            header = null,
            onBack = null,
            title = "Idag",
            subtitle = "sön 4 okt",
            actions = { AppIconButton(R.drawable.ic_calendar, "Välj dag", {}) },
            snackbar = remember { SnackbarHostState() },
            sectionGap = Spacing.l
        ) { meds ->
            AppCard {
                SectionHeader("Morgon", count = "1 / 2")
                meds.forEachIndexed { i, med -> CheckRow(med, checked = i == 0, onCheckedChange = {}) }
            }
        }
    }
}
