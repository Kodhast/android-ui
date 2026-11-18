@file:OptIn(ExperimentalMaterial3Api::class)

package se.kodhast.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.common.DetailUiState
import se.kodhast.ui.common.ListUiState
import se.kodhast.ui.components.testing.FakePhoto
import se.kodhast.ui.components.testing.PHOTO_COLOR
import se.kodhast.ui.components.testing.TestTheme
import se.kodhast.ui.components.testing.assertSameColor
import se.kodhast.ui.components.testing.pixelAt

/** Kodhäst-sidhuvudet (android-ui #42): vila och ihopfällt, prickarna, avataren, inloggningen och ramarnas flikläge. */
@RunWith(RobolectricTestRunner::class)
class HeaderBehaviorTest {

    @get:Rule
    val rule = createComposeRule()

    private val identity = AppIdentity(R.drawable.ic_box, "Dagboken")

    private fun show(depth: Boolean = false, style: AppBrandStyle = AppBrandStyle(), content: @Composable () -> Unit) = rule.setContent {
        TestTheme(depth = depth) { CompositionLocalProvider(LocalAppBrandStyle provides style, content = content) }
    }

    @Test
    fun `i vila är raden 144 dp med stor rubrik under ikonraden och undertitel i sidans färg`() {
        var background = Color.Unspecified
        show {
            background = MaterialTheme.colorScheme.background
            SampleHeader(collapsed = 0f)
        }
        rule.onNodeWithTag(APP_HEADER_TAG).assertHeightIsEqualTo(144.dp)
        rule.onNodeWithText("God kväll, Anna").assertExists()
        val title = rule.onNodeWithText("Idag").getBoundsInRoot()
        assertTrue(title.top >= 64.dp, "rubriken står under raden med ikonen i vila, men börjar ${title.top}")
        assertSameColor(background, rule.onNodeWithTag(APP_HEADER_TAG).pixelAt(rule.density, 200.dp, 140.dp), "ingen skugga eller kortfärg i vila")
    }

    @Test
    fun `ihopfälld är raden 64 dp med samma rubrik bredvid ikonen`() {
        show { SampleHeader(collapsed = 1f) }
        rule.onNodeWithTag(APP_HEADER_TAG).assertHeightIsEqualTo(64.dp)
        val title = rule.onNodeWithText("Idag").getBoundsInRoot()
        assertTrue(title.bottom <= 64.dp, "rubriken ska stå i raden, men slutar ${title.bottom}")
        assertTrue(title.left >= 16.dp + 32.dp, "rubriken ska stå bredvid ikonen, men börjar ${title.left}")
        assertEquals(1, rule.onAllNodesWithText("Idag").fetchSemanticsNodes().size, "en rubrik som flyttas, inte två")
    }

    @Test
    fun `ihopfälld utan innehåll under behåller sidans färg`() {
        var background = Color.Unspecified
        show {
            background = MaterialTheme.colorScheme.background
            SampleHeader(collapsed = 1f, overlapped = false)
        }
        assertSameColor(background, rule.onNodeWithTag(APP_HEADER_TAG).pixelAt(rule.density, 200.dp, 32.dp))
    }

    @Test
    fun `ihopfälld med innehåll under får kortens färg och utan djup en linje`() {
        var card = Color.Unspecified
        var line = Color.Unspecified
        show {
            card = se.kodhast.ui.theme.AppTheme.extendedColors.card
            line = MaterialTheme.colorScheme.outlineVariant
            SampleHeader(collapsed = 1f, overlapped = true)
        }
        val header = rule.onNodeWithTag(APP_HEADER_TAG)
        assertSameColor(card, header.pixelAt(rule.density, 200.dp, 32.dp), "kortfärg ihopfälld med innehåll under")
        assertSameColor(line, header.pixelAt(rule.density, 200.dp, 63.5.dp), "hårfin linje utan djup")
    }

    @Test
    fun `rubriken är en rubrik för TalkBack och läses en gång, ikonen och appnamnet läses inte`() {
        show { SampleHeader(collapsed = 0f) }
        assertEquals(1, rule.onAllNodes(isHeading() and hasText("Idag")).fetchSemanticsNodes().size)
        assertEquals(0, rule.onAllNodesWithText("Dagboken").fetchSemanticsNodes().size, "appnamnet läses inte")
    }

    @Test
    fun `prickarna står bara med headerDots`() {
        show { SampleHeader(collapsed = 0f) }
        assertEquals(0, rule.onAllNodesWithTag(HEADER_DOTS_TAG, useUnmergedTree = true).fetchSemanticsNodes().size)
    }

    @Test
    fun `prickarna står i vila och ihopfällt med headerDots, stilla utan synk och tysta`() {
        show(style = AppBrandStyle(headerDots = true)) { SampleHeader(collapsed = 1f) }
        val dots = rule.onNodeWithTag(HEADER_DOTS_TAG, useUnmergedTree = true).fetchSemanticsNode()
        assertNull(dots.config.getOrNull(SemanticsProperties.ContentDescription))
    }

    @Test
    fun `synk ger texten i en live-region`() {
        show(style = AppBrandStyle(headerDots = true)) { SampleHeader(collapsed = 0f, syncing = true) }
        val dots = rule.onNodeWithTag(HEADER_DOTS_TAG, useUnmergedTree = true).fetchSemanticsNode()
        assertEquals(listOf("Ändringar väntar på servern"), dots.config[SemanticsProperties.ContentDescription])
        assertEquals(LiveRegionMode.Polite, dots.config[SemanticsProperties.LiveRegion])
    }

    @Test
    fun `avataren är en knapp med Konto och inställningar`() {
        var clicks = 0
        show { AccountAvatar("Anna Berg", onClick = { clicks++ }) }
        val node = rule.onNodeWithContentDescription("Konto och inställningar")
        assertEquals(Role.Button, node.fetchSemanticsNode().config[SemanticsProperties.Role])
        assertNull(node.fetchSemanticsNode().config.getOrNull(SemanticsProperties.StateDescription), "inget anslutningsläge med online = null")
        node.performClick()
        assertEquals(1, clicks)
        assertEquals(0, rule.onAllNodesWithTag(ACCOUNT_ONLINE_TAG, useUnmergedTree = true).fetchSemanticsNodes().size)
    }

    @Test
    fun `fotot täcker cirkeln och initialerna står kvar under`() {
        show { FakePhoto(PHOTO_COLOR) { AccountAvatar("Anna Berg", onClick = {}, photoUrl = "https://example.com/foto.jpg") } }
        val avatar = rule.onNodeWithContentDescription("Konto och inställningar")
        assertSameColor(PHOTO_COLOR, avatar.pixelAt(rule.density, 24.dp, 24.dp), "fotot mitt i cirkeln")
        assertEquals(1, rule.onAllNodesWithText("AB", useUnmergedTree = true).fetchSemanticsNodes().size, "initialerna under fotot")
    }

    @Test
    fun `en egen photo-slot går före photoUrl`() {
        var slotShown = false
        show { FakePhoto(PHOTO_COLOR) { AccountAvatar("Anna Berg", onClick = {}, photoUrl = "https://example.com/foto.jpg", photo = { slotShown = true }) } }
        assertTrue(slotShown)
        val center = rule.onNodeWithContentDescription("Konto och inställningar").pixelAt(rule.density, 24.dp, 24.dp)
        assertTrue(center != PHOTO_COLOR, "photoUrl ritas inte när slotten är satt")
    }

    @Test
    fun `avataren utan onClick är bara en bild`() {
        show { AccountAvatar("Anna Berg", onClick = null) }
        assertEquals(0, rule.onAllNodesWithContentDescription("Konto och inställningar").fetchSemanticsNodes().size)
        assertEquals(0, rule.onAllNodesWithText("AB").fetchSemanticsNodes().size)
    }

    @Test
    fun `ansluten ger pluppen och Ansluten för TalkBack`() {
        show { AccountAvatar("Anna Berg", onClick = {}, online = true) }
        rule.onNodeWithTag(ACCOUNT_ONLINE_TAG, useUnmergedTree = true).assertExists()
        rule.onNodeWithContentDescription("Konto och inställningar").assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Ansluten"))
    }

    @Test
    fun `utan anslutning ingen plupp och Utan anslutning för TalkBack`() {
        show { AccountAvatar("Anna Berg", onClick = {}, online = false) }
        assertEquals(0, rule.onAllNodesWithTag(ACCOUNT_ONLINE_TAG, useUnmergedTree = true).fetchSemanticsNodes().size)
        rule.onNodeWithContentDescription("Konto och inställningar").assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Utan anslutning"))
    }

    @Test
    fun `pluppen har appens färg för online, annars primary`() {
        var primary = Color.Unspecified
        show {
            primary = MaterialTheme.colorScheme.primary
            AccountAvatar("Anna Berg", onClick = {}, online = true)
        }
        // Pluppen är 12 dp nere till höger i 36 dp-cirkeln; mitten ligger 6 dp in från cirkelns hörn.
        assertSameColor(primary, rule.onNodeWithTag(ACCOUNT_ONLINE_TAG, useUnmergedTree = true).pixelAt(rule.density, 6.dp, 6.dp))
    }

    @Test
    fun `initialerna är första och sista ordets första bokstav`() {
        assertEquals("AB", initials("Anna Berg"))
        assertEquals("AB", initials("  anna  maria berg "))
        assertEquals("A", initials("anna"))
        assertNull(initials("   "))
        assertNull(initials("123"))
    }

    @Test
    fun `utan namn visas personikonen och inga initialer`() {
        show { AccountAvatar(null, onClick = {}) }
        rule.onNodeWithContentDescription("Konto och inställningar").assertExists()
        assertEquals(0, rule.onAllNodesWithText("AB", useUnmergedTree = true).fetchSemanticsNodes().size)
    }

    @Test
    fun `listramen utan identitet ritar dagens topprad`() {
        show { EntityListScreen("Dagbok", ListUiState.Content(persistentListOf("Promenad")), EMPTY, add = null, key = { it }) { ItemRow(it) } }
        assertEquals(0, rule.onAllNodesWithTag(APP_HEADER_TAG).fetchSemanticsNodes().size)
        rule.onNode(isHeading() and hasText("Dagbok")).assertExists()
    }

    @Test
    fun `listramen med identitet ritar sidhuvudet i flikläget`() {
        show {
            CompositionLocalProvider(LocalAppIdentity provides identity) {
                EntityListScreen("Dagbok", ListUiState.Content(persistentListOf("Promenad")), EMPTY, add = null, key = { it }) { ItemRow(it) }
            }
        }
        rule.onNodeWithTag(APP_HEADER_TAG).assertExists()
        assertEquals(1, rule.onAllNodes(isHeading() and hasText("Dagbok")).fetchSemanticsNodes().size)
    }

    @Test
    fun `listramen med identitet fälls ihop till 64 dp med kortfärg när listan rullas`() {
        var card = Color.Unspecified
        val rows = (1..40).map { "Rad $it" }.toPersistentList()
        show {
            card = se.kodhast.ui.theme.AppTheme.extendedColors.card
            CompositionLocalProvider(LocalAppIdentity provides identity) {
                EntityListScreen("Dagbok", ListUiState.Content(rows), EMPTY, add = null, key = { it }) { ItemRow(it) }
            }
        }
        rule.onNodeWithTag(APP_HEADER_TAG).assertHeightIsEqualTo(EXPANDED)
        rule.onNode(hasScrollAction()).performTouchInput { swipeUp() }
        rule.onNode(hasScrollAction()).performTouchInput { swipeUp() }
        rule.waitForIdle()
        rule.onNodeWithTag(APP_HEADER_TAG).assertHeightIsEqualTo(COLLAPSED)
        assertSameColor(card, rule.onNodeWithTag(APP_HEADER_TAG).pixelAt(rule.density, 200.dp, 32.dp), "kortfärg när innehåll ligger under")
    }

    @Test
    fun `listramen med identitet men som underskärm har den lilla toppraden`() {
        show {
            CompositionLocalProvider(LocalAppIdentity provides identity) {
                EntityListScreen("Arkiv", ListUiState.Content(persistentListOf("Promenad")), EMPTY, add = null, key = { it }, onBack = {}) { ItemRow(it) }
            }
        }
        assertEquals(0, rule.onAllNodesWithTag(APP_HEADER_TAG).fetchSemanticsNodes().size)
    }

    @Test
    fun `detaljramen ritar sidhuvudet bara i flikläge med identitet`() {
        show {
            CompositionLocalProvider(LocalAppIdentity provides identity) {
                EntityDetailScreen(DetailUiState.Content("Levaxin"), header = null, onBack = null, title = "Idag", subtitle = "God kväll") { ItemRow(it) }
            }
        }
        rule.onNodeWithTag(APP_HEADER_TAG).assertExists()
        rule.onNodeWithText("God kväll").assertExists()
    }

    @Test
    fun `detaljramen utan identitet ritar dagens topprad`() {
        show { EntityDetailScreen(DetailUiState.Content("Levaxin"), header = null, onBack = null, title = "Idag") { ItemRow(it) } }
        assertEquals(0, rule.onAllNodesWithTag(APP_HEADER_TAG).fetchSemanticsNodes().size)
    }

    private companion object {
        val EMPTY = EmptyContent(R.drawable.ic_box, "Inget än", "Lägg till något.")
    }
}

/**
 * Sidhuvudet fällt ihop [collapsed] (0 vila, 1 ihopfällt), med innehåll under ([overlapped]) –
 * som när listan rullats. Delas av beteende- och skärmdumpstesterna.
 */
@Composable
internal fun SampleHeader(
    collapsed: Float,
    overlapped: Boolean = false,
    title: String = "Idag",
    subtitle: String? = "God kväll, Anna",
    syncing: Boolean = false,
    account: @Composable () -> Unit = { AccountAvatar("Anna Berg", onClick = {}, online = true) }
) {
    val limit = with(LocalDensity.current) { (COLLAPSED - EXPANDED).toPx() }
    val state = rememberTopAppBarState(initialHeightOffsetLimit = limit, initialHeightOffset = limit * collapsed, initialContentOffset = if (overlapped) -1f else 0f)
    AppHeader(
        title,
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(state),
        subtitle = subtitle,
        syncing = syncing,
        identity = AppIdentity(R.drawable.ic_box, "Dagboken"),
        account = account
    )
}
