package se.kodhast.ui.navigation.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import kotlin.test.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.components.testing.TestTheme
import se.kodhast.ui.components.testing.captureLightAndDark

/** Galleriet: bibliotekets sektioner följda av appens, i ljust och mörkt, och början som skärmdump. */
@RunWith(RobolectricTestRunner::class)
class ComponentGalleryTest {

    /** Galleriet har oändliga animationer (`AppLoading`): bilden tas i regelns testklocka. */
    @get:Rule
    val rule = createComposeRule()

    private val appSection = GallerySection("Appens egna") { Text("Appens komponent") }

    @Test
    fun `bibliotekets sektioner först, appens efter, i ljust och mörkt`() {
        var dark by mutableStateOf(false)
        var back = 0
        rule.setContent { TestTheme(darkTheme = dark) { ComponentGallery(onBack = { back++ }, appSections = listOf(appSection)) } }
        for (isDark in listOf(false, true)) {
            dark = isDark
            rule.waitForIdle()
            val titles = listOf("Knappar och meny", "Inställningar och konto", "Framsteg, laddning och navigering", "Inloggning", "Ramar", "Appens egna")
            titles.forEach { rule.onNodeWithText(it).performScrollTo() }
            rule.onNodeWithText("Appens komponent").performScrollTo()
            // Positionen i den rullande kolumnen (samma rullning för alla): varje rubrik står under den förra.
            val order = titles.map { title -> rule.onNodeWithText(title).getUnclippedBoundsInRoot().top }
            assertTrue(order.zipWithNext().all { (a, b) -> a < b }, "ordningen: $titles → $order")
            rule.onNodeWithText("Knappar och meny").performScrollTo()
        }
        // Galleriets egen pil står först; ramarna längre ner har sina egna.
        rule.onAllNodesWithContentDescription("Tillbaka")[0].performClick()
        assertTrue(back == 1, "tillbakapilen anropar onBack")
    }

    @Test
    fun `utan appens sektioner`() = rule.captureLightAndDark("ComponentGallery_start") {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) { ComponentGallery(onBack = {}) }
    }
}
