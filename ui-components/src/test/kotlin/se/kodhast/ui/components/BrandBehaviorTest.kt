package se.kodhast.ui.components

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import se.kodhast.ui.components.testing.TestTheme

/** Kodhäst-delarna (android-ui #28): märket, signaturen och stilen som slår på prickar och märke. */
@RunWith(RobolectricTestRunner::class)
class BrandBehaviorTest {

    @get:Rule
    val rule = createComposeRule()

    private fun show(style: AppBrandStyle = AppBrandStyle(), content: @Composable () -> Unit) = rule.setContent {
        TestTheme { CompositionLocalProvider(LocalAppBrandStyle provides style, content = content) }
    }

    @Test
    fun `stilen är avstängd som standard`() {
        assertEquals(AppBrandStyle(sectionDots = false, dotLoading = false, emptyStateMark = false), AppBrandStyle())
    }

    @Test
    fun `märket är dekor utan semantik`() {
        show { BrandMark() }
        val root = rule.onRoot(useUnmergedTree = true).fetchSemanticsNode()
        assertEquals(emptyList(), root.children, "märket ska inte ge någon nod")
    }

    @Test
    fun `signaturen är en nod som läses som texten`() {
        show { MakerMark() }
        val node = rule.onNodeWithText("En kodhäst-app").fetchSemanticsNode()
        assertEquals(listOf("En kodhäst-app"), node.config[SemanticsProperties.Text].map { it.text })
        assertNull(node.config.getOrNull(SemanticsProperties.ContentDescription))
        assertEquals(1, rule.onAllNodesWithText("En kodhäst-app").fetchSemanticsNodes().size)
    }

    @Test
    fun `signaturen tar en egen text`() {
        show { MakerMark(text = "En app från Kodhäst") }
        rule.onNodeWithText("En app från Kodhäst").assertExists()
    }

    @Test
    fun `sektionsrubriken har prickar bara med stilen på och utan ikonruta`() {
        show(AppBrandStyle(sectionDots = true)) { SectionHeader("Morgon") }
        rule.onNodeWithTag(SECTION_DOTS_TAG, useUnmergedTree = true).assertExists()
    }

    @Test
    fun `sektionsrubriken med ikonruta får inga prickar`() {
        show(AppBrandStyle(sectionDots = true)) { SectionHeader("Mediciner", icon = R.drawable.ic_box) }
        rule.onNodeWithTag(SECTION_DOTS_TAG, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `sektionsrubriken utan stilen får inga prickar`() {
        show { SectionHeader("Morgon") }
        rule.onNodeWithTag(SECTION_DOTS_TAG, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `prickladdningen läses som Laddar`() {
        show(AppBrandStyle(dotLoading = true)) { AppLoading() }
        rule.onNodeWithContentDescription("Laddar").assertExists()
    }

    @Test
    fun `prickladdningen hoppar med animeringar på`() {
        assertEquals(false, dotsStandStill(), "kontroll: utan avstängda animeringar ska prickarna röra sig")
    }

    @Test
    fun `prickladdningen står still när systemets animeringar är avstängda`() {
        Settings.Global.putFloat(RuntimeEnvironment.getApplication().contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 0f)
        assertEquals(true, dotsStandStill())
        rule.onNodeWithContentDescription("Laddar").assertExists()
    }

    /** Två bilder av prickladdningen med en fast tid emellan – lika bilder betyder att prickarna står still. */
    private fun dotsStandStill(): Boolean {
        rule.mainClock.autoAdvance = false
        show(AppBrandStyle(dotLoading = true)) { AppLoading() }
        rule.waitForIdle()
        val before = rule.onNodeWithContentDescription("Laddar").captureToImage().toPixelMap()
        rule.mainClock.advanceTimeBy(MID_JUMP)
        rule.waitForIdle()
        val after = rule.onNodeWithContentDescription("Laddar").captureToImage().toPixelMap()
        return (0 until before.width).all { x -> (0 until before.height).all { y -> before[x, y] == after[x, y] } }
    }

    private companion object {
        const val MID_JUMP = 225L
    }

    @Test
    fun `sektionsrubrikens prickladdning läses som Uppdaterar titeln`() {
        show(AppBrandStyle(dotLoading = true)) { SectionHeader("Byten", working = true) }
        rule.onNodeWithContentDescription("Uppdaterar Byten").assertExists()
    }

    @Test
    fun `tomt läge visar märket bara i fullstort läge utan fel`() {
        val style = AppBrandStyle(emptyStateMark = true)
        show(style) { EmptyState(R.drawable.ic_box, "Inga resor än", "Lägg till din första resa.") }
        rule.onNodeWithTag(EMPTY_STATE_MARK_TAG, useUnmergedTree = true).assertExists()
    }

    @Test
    fun `tomt läge med fel eller kompakt visar ikonen`() {
        show(AppBrandStyle(emptyStateMark = true)) {
            EmptyState(R.drawable.ic_box, "Kunde inte läsa", "Försök igen.", isError = true)
            EmptyState(R.drawable.ic_box, "För lite data än", "Kom tillbaka om en vecka.", compact = true)
        }
        rule.onNodeWithTag(EMPTY_STATE_MARK_TAG, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `tomt läge utan stilen visar ikonen`() {
        show { EmptyState(R.drawable.ic_box, "Inga resor än", "Lägg till din första resa.") }
        rule.onNodeWithTag(EMPTY_STATE_MARK_TAG, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `ordmärket till startskärmen finns med färger som appen kan ersätta`() {
        val context = RuntimeEnvironment.getApplication()
        val drawable = assertNotNull(context.getDrawable(R.drawable.kodhast_branding))
        val density = context.resources.displayMetrics.density
        assertEquals((200 * density).toInt(), drawable.intrinsicWidth)
        assertEquals((80 * density).toInt(), drawable.intrinsicHeight)
        assertEquals(context.getColor(R.color.kodhast_branding_text), context.getColor(R.color.kodhast_branding_dots))
    }
}
