package se.kodhast.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.NativeClipboard
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.components.testing.TestTheme
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.Tone

/** Värderaden, andelsraden, rullgardinsfältet, väntedialogen, toppradens prickar och kopiering med långtryck (android-ui #85). */
@RunWith(RobolectricTestRunner::class)
class ValuePartsBehaviorTest {

    @get:Rule
    val rule = createComposeRule()

    private fun show(style: AppBrandStyle = AppBrandStyle(), content: @Composable () -> Unit) = rule.setContent {
        TestTheme { CompositionLocalProvider(LocalAppBrandStyle provides style, content = content) }
    }

    // ValueRow

    @Test
    fun `värderaden läses som en enhet med etikett, stödrader och värde`() {
        show { ValueRow("Schablonskatt i år", "ca −3 000 kr", supportingLabel = "0,888 % på kapitalunderlaget", supportingValue = "2026") }
        val texts = rule.onNodeWithText("Schablonskatt i år", substring = true).fetchSemanticsNode().config[SemanticsProperties.Text].map { it.text }
        assertEquals(listOf("Schablonskatt i år", "0,888 % på kapitalunderlaget", "ca −3 000 kr", "2026"), texts)
    }

    @Test
    fun `värdets färg - dämpat före egen färg före ton före onSurface`() {
        val expected = mutableMapOf<String, Color>()
        show {
            val scheme = MaterialTheme.colorScheme
            expected += mapOf("vanlig" to scheme.onSurface, "ton" to AppTheme.tone(Tone.Primary).content, "egen" to scheme.error, "dämpad" to scheme.onSurfaceVariant)
            Column {
                ValueRow("vanlig", "1 %")
                ValueRow("ton", "2 %", valueTone = Tone.Primary)
                ValueRow("egen", "3 %", valueColor = scheme.error, valueTone = Tone.Primary)
                ValueRow("dämpad", "4 %", valueColor = scheme.error, valueMuted = true)
            }
        }
        val shown = listOf("vanlig" to "1 %", "ton" to "2 %", "egen" to "3 %", "dämpad" to "4 %").associate { (name, value) -> name to textColor(value) }
        assertEquals(expected.toMap(), shown)
    }

    /** Färgen som texten ritas med, ur textens layout. */
    private fun textColor(text: String): Color = textLayout(text).layoutInput.style.color

    private fun textLayout(text: String): TextLayoutResult {
        val layouts = mutableListOf<TextLayoutResult>()
        rule.onNodeWithText(text, useUnmergedTree = true).fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action?.invoke(layouts)
        return layouts.single()
    }

    @Test
    fun `dämpad rad dämpar också delar av värdet som har egen färg`() {
        var dimmed = Color.Unspecified
        show {
            dimmed = MaterialTheme.colorScheme.onSurfaceVariant
            val gain = MaterialTheme.colorScheme.error
            Column {
                ValueRow(
                    "Resultat",
                    buildAnnotatedString {
                        append("+8,6 % · ")
                        withStyle(SpanStyle(color = gain)) { append("+3 286 kr") }
                    },
                    muted = true
                )
                ValueRow("Värde", buildAnnotatedString { withStyle(SpanStyle(color = gain)) { append("ca 12 000 kr") } }, valueMuted = true)
            }
        }
        listOf("+8,6 % · +3 286 kr", "ca 12 000 kr").forEach { text ->
            val layout = textLayout(text)
            assertEquals(dimmed, layout.layoutInput.style.color)
            assertTrue(layout.layoutInput.text.spanStyles.all { it.item.color == dimmed }, "delarna dämpade i $text")
        }
    }

    @Test
    fun `tabulära siffror med en tom fontFeatureSettings`() {
        assertEquals("tnum", TextStyle(fontFeatureSettings = "").tabular().fontFeatureSettings)
        assertEquals("tnum", TextStyle().tabular().fontFeatureSettings)
        assertEquals("smcp, tnum", TextStyle(fontFeatureSettings = "smcp").tabular().fontFeatureSettings)
    }

    @Test
    fun `en lång etikett lämnar plats åt värdet`() {
        show { ValueRow("Handelsbanken Amerika Småbolag Tema Criteria A1 SEK med ett namn som aldrig tar slut", "1,46 %", Modifier.testTag("rad")) }
        val row = rule.onNodeWithTag("rad").fetchSemanticsNode().boundsInRoot
        val value = rule.onNodeWithText("1,46 %", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        assertTrue(value.right <= row.right && value.width > 0f, "värdet syns inom raden")
    }

    // ShareBar

    @Test
    fun `andelsraden läses med andelen som framsteg`() {
        show { ShareBar("Sverige", 0.4f, "40,0 %") }
        rule.onNodeWithContentDescription("Sverige, 40,0 %")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo(0.4f, 0f..1f)))
        assertEquals(0, rule.onAllNodesWithTag(SHARE_TARGET_TAG, useUnmergedTree = true).fetchSemanticsNodes().size)
    }

    @Test
    fun `med mål visas målet, strecket och en mening för TalkBack`() {
        show { ShareBar("Nivå 4", 1.4f, "52,0 %", target = 0.45f, targetValue = "45,0 %") }
        rule.onNodeWithContentDescription("Nivå 4: 52,0 %, mål 45,0 %")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo(1f, 0f..1f)))
        rule.onNodeWithText("52,0 % · mål 45,0 %", useUnmergedTree = true).assertExists()
        rule.onNodeWithTag(SHARE_TARGET_TAG, useUnmergedTree = true).assertExists()
    }

    @Test
    fun `andelsraden tål en andel som inte är ett tal`() {
        show {
            Column {
                ShareBar("Övrigt", Float.NaN, "0,0 %")
                ShareBar("Okänd region", 0.2f, "20,0 %", target = Float.NaN, targetValue = "–")
            }
        }
        rule.onNodeWithContentDescription("Övrigt, 0,0 %")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo(0f, 0f..1f)))
        rule.onNodeWithTag(SHARE_TARGET_TAG, useUnmergedTree = true).assertExists()
    }

    // SelectField

    @Test
    fun `rullgardinen visar valen och ett val stänger den`() {
        var selected by mutableStateOf<String?>("Global Indexfond A")
        show { SelectField("Fond", FUNDS, selected, { it }, { selected = it }, clearOptionLabel = "Alla fonder") }
        rule.onNodeWithContentDescription("Fond, Global Indexfond A")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Hopfälld"))
            .performClick()
        rule.onNodeWithText("Alla fonder").assertExists()
        rule.onNodeWithContentDescription("Fond, Global Indexfond A").assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Utfälld"))
        rule.onNodeWithText("Räntefond Kort").performClick()
        assertEquals("Räntefond Kort", selected)
        rule.onNodeWithText("Alla fonder").assertDoesNotExist()
        rule.onNodeWithContentDescription("Fond, Räntefond Kort").performClick()
        rule.onNodeWithText("Alla fonder").performClick()
        assertNull(selected)
        rule.onNodeWithContentDescription("Fond, Välj").assertExists()
    }

    @Test
    fun `låst fält visar valet men öppnar ingen meny`() {
        show { SelectField("Byt till", FUNDS, "Sverige Småbolag", { it }, {}, enabled = false) }
        rule.onNodeWithContentDescription("Byt till, Sverige Småbolag").assertIsNotEnabled().performClick()
        rule.onNodeWithText("Räntefond Kort").assertDoesNotExist()
    }

    @Test
    fun `en meny som var öppen när fältet låstes öppnas inte igen av sig själv`() {
        var enabled by mutableStateOf(true)
        show { SelectField("Fond", FUNDS, null, { it }, {}, enabled = enabled) }
        rule.onNodeWithContentDescription("Fond, Välj").performClick()
        rule.onNodeWithText("Räntefond Kort").assertExists()
        enabled = false
        rule.waitForIdle()
        rule.onNodeWithText("Räntefond Kort").assertDoesNotExist()
        enabled = true
        rule.waitForIdle()
        rule.onNodeWithText("Räntefond Kort").assertDoesNotExist()
    }

    @Test
    fun `utan val visas platshållaren`() {
        show { SelectField("Fondbolag", FUNDS, null, { it }, {}, placeholder = "Alla fondbolag") }
        rule.onNodeWithContentDescription("Fondbolag, Alla fondbolag").assertExists()
    }

    // LoadingDialog

    @Test
    fun `väntedialogen är en dialog med texten i en live-region och går inte att stänga`() {
        rule.mainClock.autoAdvance = false
        show { LoadingDialog("Läser avräkningsnotan", "Det tar några sekunder.") }
        rule.mainClock.advanceTimeByFrame()
        rule.onNode(isDialog()).assertExists()
        val dialog = rule.onNodeWithTag(LOADING_DIALOG_TAG).fetchSemanticsNode()
        assertEquals(LiveRegionMode.Polite, dialog.config[SemanticsProperties.LiveRegion])
        val texts = dialog.config[SemanticsProperties.Text].map { it.text }
        assertEquals(listOf("Läser avräkningsnotan", "Det tar några sekunder."), texts)
        // Bakåt stänger inte: dialogen har ingen åtgärd för att stängas.
        assertNull(dialog.config.getOrNull(SemanticsActions.Dismiss))
    }

    // AppTopBar

    @Test
    fun `toppraden har inga prickar utan headerDots`() {
        show(style = AppBrandStyle()) { AppTopBar("Facit", size = TopBarSize.Small, onBack = {}, syncing = true) }
        assertEquals(0, rule.onAllNodesWithTag(HEADER_DOTS_TAG, useUnmergedTree = true).fetchSemanticsNodes().size)
    }

    @Test
    fun `med headerDots står prickarna före rubriken, tysta tills något hämtas`() {
        var syncing by mutableStateOf(false)
        rule.mainClock.autoAdvance = false
        show(style = AppBrandStyle(headerDots = true)) {
            CompositionLocalProvider(LocalTopBarSyncing provides syncing) {
                AppTopBar("Facit", size = TopBarSize.Small, onBack = {})
            }
        }
        rule.mainClock.advanceTimeByFrame()
        val dots = rule.onNodeWithTag(HEADER_DOTS_TAG, useUnmergedTree = true)
        assertNull(dots.fetchSemanticsNode().config.getOrNull(SemanticsProperties.ContentDescription))
        val title = rule.onNodeWithText("Facit").fetchSemanticsNode().boundsInRoot
        assertTrue(dots.fetchSemanticsNode().boundsInRoot.right <= title.left, "prickarna före rubriken")
        syncing = true
        // Ändringen görs utanför kompositionen; med stillastående klocka måste den skickas själv.
        Snapshot.sendApplyNotifications()
        rule.mainClock.advanceTimeByFrame()
        val node = dots.fetchSemanticsNode()
        assertEquals(listOf("Ändringar väntar på servern"), node.config[SemanticsProperties.ContentDescription])
        assertEquals(LiveRegionMode.Polite, node.config[SemanticsProperties.LiveRegion])
    }

    // copyOnLongPress

    @Test
    fun `långtryck kopierar texten med haptik och släpper inte igenom klicket`() {
        val clipboard = RecordingClipboard()
        val haptics = mutableListOf<HapticFeedbackType>()
        var clicks = 0
        show {
            CompositionLocalProvider(LocalClipboard provides clipboard, LocalHapticFeedback provides Recorder(haptics)) {
                Box(Modifier.size(SIZE).testTag(TAG).copyOnLongPress("Global Indexfond A", "Kopiera fondnamn", "Fondnamn").clickable { clicks++ }) { Text("Global Indexfond A") }
            }
        }
        rule.onNodeWithTag(TAG).performClick()
        assertEquals(1, clicks)
        assertEquals(emptyList(), clipboard.texts)
        rule.onNodeWithTag(TAG).performTouchInput { longClick() }
        rule.waitForIdle()
        assertEquals(1, clicks)
        assertEquals(listOf("Fondnamn" to "Global Indexfond A"), clipboard.texts)
        assertEquals(listOf(HapticFeedbackType.LongPress), haptics)
    }

    @Test
    fun `TalkBack får kopieringen som egen åtgärd`() {
        val clipboard = RecordingClipboard()
        show {
            CompositionLocalProvider(LocalClipboard provides clipboard) {
                Text("Sverige Småbolag", Modifier.testTag(TAG).copyOnLongPress("Sverige Småbolag", "Kopiera fondnamn"))
            }
        }
        val actions = rule.onNodeWithTag(TAG).fetchSemanticsNode().config[SemanticsActions.CustomActions]
        assertEquals(listOf("Kopiera fondnamn"), actions.map { it.label })
        rule.runOnIdle { actions.single().action() }
        rule.waitForIdle()
        assertEquals(listOf("Kopiera fondnamn" to "Sverige Småbolag"), clipboard.texts)
    }

    private class Recorder(val into: MutableList<HapticFeedbackType>) : HapticFeedback {
        override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
            into += hapticFeedbackType
        }
    }

    /** Urklipp som sparar etikett och text i stället för att röra systemet. */
    private class RecordingClipboard : Clipboard {
        val texts = mutableListOf<Pair<String, String>>()
        override val nativeClipboard: NativeClipboard get() = error("används inte")
        override suspend fun getClipEntry(): ClipEntry? = null
        override suspend fun setClipEntry(clipEntry: ClipEntry?) {
            val clip = clipEntry?.clipData ?: return
            texts += clip.description.label.toString() to clip.getItemAt(0).text.toString()
        }
    }

    private companion object {
        val FUNDS = listOf("Global Indexfond A", "Sverige Småbolag", "Räntefond Kort")
        const val TAG = "mål"
        val SIZE = 120.dp
    }
}
