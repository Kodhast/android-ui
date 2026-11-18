package se.kodhast.ui.navigation

import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.components.testing.captureLightAndDark

/** Navigationsramen i en fliks rot (sidhuvud, verktygsrad med plusknapp) och på en undersida (ingen verktygsrad). */
@RunWith(RobolectricTestRunner::class)
class NavigationScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    // Back stacken skapas utanför innehållet: en ny per komposition vore en ny navigering per bild.
    private val backStack = testBackStack()
    private val action = addAction {}

    @Test
    fun `flikens rot`() = rule.captureLightAndDark("AppNavHost_rot") {
        TestNavHost(backStack, headerScreens, action = action)
    }

    @Test
    fun undersida() {
        backStack.push(Detail("anna"))
        rule.captureLightAndDark("AppNavHost_undersida") { TestNavHost(backStack, headerScreens, action = action) }
    }
}
