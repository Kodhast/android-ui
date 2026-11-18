package se.kodhast.ui.components

import android.content.ActivityNotFoundException
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTouchHeightIsEqualTo
import androidx.compose.ui.test.assertTouchWidthIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.unit.dp
import kotlin.test.assertEquals
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.common.DetailUiState
import se.kodhast.ui.common.ListUiState
import se.kodhast.ui.components.testing.TestPalette
import se.kodhast.ui.components.testing.TestTheme
import se.kodhast.ui.components.testing.assertSameColor
import se.kodhast.ui.components.testing.pixelAtEnd
import se.kodhast.ui.components.testing.sampleOffline

/** Rättelserna från granskningen 2026-11-18 (#73), med ett regressionstest per fel. */
@RunWith(RobolectricTestRunner::class)
class FixesBehaviorTest {

    @get:Rule
    val rule = createComposeRule()

    private fun show(content: @Composable () -> Unit) = rule.setContent { TestTheme(content = content) }

    private val empty = EmptyContent(R.drawable.ic_box, "Inga fonder hittades", "Sök på ett annat namn.")

    // --- FAB-menyns Stäng-knapp ---

    private fun fabToolbar(color: ActionColor) = show {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            AppFloatingToolbar(
                listOf(ToolbarItem("Hem", R.drawable.ic_clock)),
                0,
                {},
                action = ToolbarAction("Lägg till", R.drawable.ic_add, listOf(AppMenuItem("Ny resa", {}))),
                actionMenu = ActionMenuStyle.Fab,
                actionColor = color
            )
        }
    }

    @Test
    fun `FAB-menyns kryss har huvudknappens färg i Tertiary`() {
        fabToolbar(ActionColor.Tertiary)
        assertSameColor(TestPalette.light.tertiary, rule.onNodeWithContentDescription("Lägg till").pixelAtEnd(rule.density, 4.dp), "knappen")
        rule.onNodeWithContentDescription("Lägg till").performClick()
        assertSameColor(TestPalette.light.tertiary, rule.onNodeWithContentDescription("Stäng").pixelAtEnd(rule.density, 4.dp), "krysset")
    }

    @Test
    fun `FAB-menyns kryss har huvudknappens färg i Secondary`() {
        fabToolbar(ActionColor.Secondary)
        rule.onNodeWithContentDescription("Lägg till").performClick()
        assertSameColor(TestPalette.light.secondary, rule.onNodeWithContentDescription("Stäng").pixelAtEnd(rule.density, 4.dp))
    }

    // --- Dra för att uppdatera i laddning, tomt och fel ---

    private fun assertRefreshes(content: @Composable (onRefresh: () -> Unit) -> Unit) {
        var refreshes = 0
        show { content { refreshes++ } }
        rule.onNodeWithTag(ENTITY_REFRESH_TAG).performTouchInput { swipeDown() }
        rule.waitForIdle()
        assertEquals(1, refreshes)
    }

    @Test
    fun `listan dras ned för att uppdatera medan den laddar`() = assertRefreshes { refresh ->
        EntityListScreen<String>("Fondsök", ListUiState.Loading, empty, null, key = { it }, onRefresh = refresh) { ItemRow(it) }
    }

    @Test
    fun `listan dras ned för att uppdatera när den är tom`() = assertRefreshes { refresh ->
        EntityListScreen<String>("Fondsök", ListUiState.Empty, empty, null, key = { it }, onRefresh = refresh) { ItemRow(it) }
    }

    @Test
    fun `listan dras ned för att uppdatera efter ett fel`() = assertRefreshes { refresh ->
        EntityListScreen<String>("Fondsök", ListUiState.Error(sampleOffline()), empty, null, key = { it }, onRefresh = refresh) { ItemRow(it) }
    }

    @Test
    fun `detaljen dras ned för att uppdatera medan den laddar`() = assertRefreshes { refresh ->
        EntityDetailScreen<String>(DetailUiState.Loading, header = null, onBack = {}, title = "Global Index", onRefresh = refresh) { ItemRow(it) }
    }

    @Test
    fun `detaljen dras ned för att uppdatera efter ett fel`() = assertRefreshes { refresh ->
        EntityDetailScreen<String>(DetailUiState.Error(sampleOffline()), header = null, onBack = {}, title = "Global Index", onRefresh = refresh) { ItemRow(it) }
    }

    @Test
    fun `det tomma läget ligger kvar med dra-för-att-uppdatera`() {
        show { EntityListScreen<String>("Fondsök", ListUiState.Empty, empty, null, key = { it }, onRefresh = {}) { ItemRow(it) } }
        rule.onNodeWithText("Inga fonder hittades").assertIsDisplayed()
        rule.onNodeWithText("Sök på ett annat namn.").assertIsDisplayed()
    }

    // --- Hopfällt läge per grupp ---

    @Test
    fun `två hopfällbara grupper med samma rubrik fälls ut var för sig`() {
        val rows = listOf("Alvedon" to "1", "Ipren" to "2")
        show {
            EntityListScreen(
                "Packlista",
                ListUiState.Content(persistentListOf(*rows.toTypedArray())),
                empty,
                null,
                key = { it.first },
                group = { ListGroup("Morgon", count = it.second, collapsible = true) }
            ) { ItemRow(it.first) }
        }
        rule.onNodeWithText("Alvedon").assertDoesNotExist()
        rule.onNodeWithText("Ipren").assertDoesNotExist()
        rule.onAllNodesWithText("Morgon")[0].performClick()
        rule.onNodeWithText("Alvedon").assertIsDisplayed()
        rule.onNodeWithText("Ipren").assertDoesNotExist()
        rule.onAllNodesWithText("Morgon")[1].performClick()
        rule.onNodeWithText("Ipren").assertIsDisplayed()
        rule.onAllNodesWithText("Morgon")[0].performClick()
        rule.onNodeWithText("Alvedon").assertDoesNotExist()
        rule.onNodeWithText("Ipren").assertIsDisplayed()
    }

    // --- Uppdatera appen utan webbläsare ---

    private fun updateScreen(failure: RuntimeException) {
        val handler = object : UriHandler {
            override fun openUri(uri: String) = throw failure
        }
        show {
            CompositionLocalProvider(LocalUriHandler provides handler) {
                UpdateRequiredScreen("Hushållet har sparats med en nyare version.", null, "https://example.com/releases")
            }
        }
        // Klockan står still, så att meddelandet (kort snackbar) inte hinner försvinna innan det läses.
        rule.mainClock.autoAdvance = false
        rule.onNode(hasText("Hämta senaste versionen") and hasClickAction()).performClick()
        rule.mainClock.advanceTimeBy(SNACKBAR_SETTLE_MILLIS)
        rule.onNodeWithText("Ingen app kan öppna länken. Installera en webbläsare och försök igen.").assertIsDisplayed()
    }

    @Test
    fun `Hämta senaste versionen utan webbläsare visar ett meddelande i stället för att krascha`() = updateScreen(IllegalArgumentException("Can't open https://example.com/releases."))

    @Test
    fun `Hämta senaste versionen när ingen aktivitet finns visar ett meddelande`() = updateScreen(ActivityNotFoundException())

    // --- Tryckytor och klickbara kort ---

    @Test
    fun `stepperns knappar har 48 dp tryckyta med 44 dp knappar`() {
        var value by mutableIntStateOf(2)
        show { QuantityStepper(value, { value = it }, "doser per dag", buttonSize = QuantityStepperDefaults.ButtonSize) }
        listOf("Minska doser per dag", "Öka doser per dag").forEach {
            rule.onNodeWithContentDescription(it)
                .assertWidthIsEqualTo(QuantityStepperDefaults.ButtonSize)
                .assertTouchWidthIsEqualTo(48.dp)
                .assertTouchHeightIsEqualTo(48.dp)
        }
        // Ett tryck strax utanför den synliga knappen (inom 48 dp) träffar den.
        rule.onNodeWithContentDescription("Minska doser per dag").performTouchInput { click(Offset(-1.5.dp.toPx(), centerY)) }
        assertEquals(1, value)
        rule.onNodeWithContentDescription("Öka doser per dag").performTouchInput { click(Offset(width + 1.5.dp.toPx(), centerY)) }
        assertEquals(2, value)
    }

    @Test
    fun `ett klickbart kort läses som knapp med sin etikett`() {
        var opened = 0
        show { AppCard(onClick = { opened++ }, onClickLabel = "Öppna resan") { ItemRow("Gran Canaria") } }
        val card = rule.onNode(hasClickAction())
        card.assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
        assertEquals("Öppna resan", card.fetchSemanticsNode().config[SemanticsActions.OnClick].label)
        card.performClick()
        assertEquals(1, opened)
    }

    @Test
    fun `ett kort utan onClick är ingen knapp`() {
        show { AppCard { ItemRow("Gran Canaria") } }
        rule.onNode(hasClickAction()).assertDoesNotExist()
        rule.onNodeWithText("Gran Canaria").assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Role))
    }

    @Test
    fun `NoticeBanner är en knapp genom kortets klick`() {
        var opened = 0
        show { NoticeBanner("Notiser är avstängda", R.drawable.ic_box, onClick = { opened++ }, onClickLabel = "Slå på") }
        val banner = rule.onNode(hasClickAction())
        banner.assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
        assertEquals("Slå på", banner.fetchSemanticsNode().config[SemanticsActions.OnClick].label)
        banner.performClick()
        assertEquals(1, opened)
    }
}

/** Tid för snackbaren att komma fram, långt under en kort snackbars visningstid. */
private const val SNACKBAR_SETTLE_MILLIS = 500L
