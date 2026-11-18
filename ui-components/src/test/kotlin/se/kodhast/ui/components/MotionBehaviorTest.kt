package se.kodhast.ui.components

import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlin.test.assertEquals
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import se.kodhast.ui.components.testing.TestTheme

/** Minskad rörelse och låst inmatning. */
@RunWith(RobolectricTestRunner::class)
class MotionBehaviorTest {

    @get:Rule
    val rule = createComposeRule()

    @After
    fun restoreAnimations() = setAnimationScale(1f)

    private fun setAnimationScale(scale: Float) {
        Settings.Global.putFloat(RuntimeEnvironment.getApplication().contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, scale)
    }

    private fun reducedMotion(): Boolean {
        var reduced: Boolean? = null
        rule.setContent { reduced = rememberReducedMotion() }
        rule.waitForIdle()
        return checkNotNull(reduced)
    }

    @Test
    fun `minskad rörelse följer systemets animationsskala`() {
        assertEquals(false, reducedMotion())
    }

    @Test
    fun `minskad rörelse är på när systemets animationer är avstängda`() {
        setAnimationScale(0f)
        assertEquals(true, reducedMotion())
    }

    @Test
    fun `låst inmatning släpper inte igenom tryck, olåst gör det`() {
        var clicks = 0
        rule.setContent {
            TestTheme {
                Column {
                    Box(Modifier.blockInput(true)) { Text("Låst", Modifier.clickable { clicks++ }) }
                    Box(Modifier.blockInput(false)) { Text("Öppen", Modifier.clickable { clicks += 10 }) }
                }
            }
        }
        rule.onNodeWithText("Låst").performClick()
        assertEquals(0, clicks)
        rule.onNodeWithText("Öppen").performClick()
        assertEquals(10, clicks)
    }
}
