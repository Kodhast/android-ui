package se.kodhast.ui.components

import android.view.Window
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.components.testing.TestTheme
import se.kodhast.ui.theme.AppBlur

@RunWith(RobolectricTestRunner::class)
class BlurBehaviorTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `med oskärpa får dialogen temats radie och svagare slöja`() {
        val properties = dialogProperties(AppBlur(), dismissOnClickOutside = false)
        assertEquals(12.dp, properties.blurBehindRadius)
        assertEquals(0.2f, properties.scrimAlpha)
        assertEquals(false, properties.dismissOnClickOutside)
    }

    @Test
    fun `utan oskärpa är dialogen som förut`() {
        val properties = dialogProperties(null)
        assertEquals(Dp.Unspecified, properties.blurBehindRadius)
        assertTrue(properties.scrimAlpha.isNaN())
    }

    @Test
    fun `oskärpan gäller bara när enheten har den påslagen`() {
        var on: AppBlur? = null
        var off: AppBlur? = AppBlur()
        rule.setContent {
            TestTheme {
                CompositionLocalProvider(LocalCrossWindowBlur provides true) { on = activeBlur() }
                CompositionLocalProvider(LocalCrossWindowBlur provides false) { off = activeBlur() }
            }
        }
        assertEquals(AppBlur(), on)
        assertNull(off)
    }

    @Test
    fun `enhetens eget besked används utan att proven tvingar`() {
        // Robolectric har ingen oskärpa mellan fönster: reserven, dagens slöja.
        var blur: AppBlur? = AppBlur()
        rule.setContent { TestTheme { blur = activeBlur() } }
        assertNull(blur)
    }

    @Test
    fun `bekräftelsedialogens fönster suddar bakom och tonar slöjan`() {
        var window: Window? = null
        rule.setContent {
            TestTheme {
                CompositionLocalProvider(LocalCrossWindowBlur provides true) {
                    ConfirmDialog("Radera Solkräm?", "Det går inte att ångra.", "Radera", {}, {}) {
                        window = (LocalView.current.parent as? DialogWindowProvider)?.window
                    }
                }
            }
        }
        rule.waitForIdle()
        val attributes = window!!.attributes
        assertEquals(0.2f, attributes.dimAmount)
        assertTrue(attributes.blurBehindRadius > 0)
    }

    @Test
    fun `arkets fönster suddar bakom`() {
        var view: android.view.View? = null
        rule.setContent {
            TestTheme {
                CompositionLocalProvider(LocalCrossWindowBlur provides true) {
                    AppBottomSheet("Ändra mängd", onDismiss = {}) { view = LocalView.current }
                }
            }
        }
        rule.waitForIdle()
        var parent: Any? = view
        while (parent is android.view.View && parent !is DialogWindowProvider) parent = parent.parent
        val attributes = (parent as DialogWindowProvider).window.attributes
        assertTrue(attributes.flags and android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND != 0)
        assertTrue(attributes.blurBehindRadius > 0)
    }
}
