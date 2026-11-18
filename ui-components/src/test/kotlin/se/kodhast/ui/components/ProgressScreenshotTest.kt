package se.kodhast.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.components.testing.captureLightAndDark
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.ProgressDoneColors
import se.kodhast.ui.theme.Spacing

/** Skärmdumpar av framstegsraden (ljust + mörkt), med påhittad packlista. */
@RunWith(RobolectricTestRunner::class)
class ProgressScreenshotTest {

    @Composable
    private fun Sample(content: @Composable () -> Unit) = Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.m)) { content() }

    @Composable
    private fun Packed(done: Int, total: Int, compact: Boolean = false) = AppProgress(done, total, label = { d, t -> "$d av $t packat" }, doneLabel = { t -> "$t av $t · allt packat" }, compact = compact)

    @Test
    fun `AppProgress - standard`() = captureLightAndDark("AppProgress_standard") {
        Sample {
            AppCard { Packed(9, 14) }
            AppCard { Packed(14, 14) }
            AppCard {
                ItemRow("Sicilien 3–10 maj", below = { Packed(9, 14, compact = true) })
            }
        }
    }

    /** Appens klart-färg med kontur (som Dagbokens solgula); i testpalettens ljusa och mörka ton. */
    @Test
    fun `AppProgress - klart med kontur`() = captureLightAndDark("AppProgress_kontur") {
        val dark = MaterialTheme.colorScheme.surface.luminance() < HALF
        val done = if (dark) ProgressDoneColors(Color(0xFFE0A21F), Color(0xFFFCEBC4)) else ProgressDoneColors(Color(0xFFF5B631), Color(0xFF4D3500))
        AppTheme(MaterialTheme.colorScheme, AppTheme.extendedColors.copy(progressDone = done), AppTheme.shapes, MaterialTheme.typography) {
            Sample {
                AppCard { Packed(14, 14) }
                AppCard { Packed(14, 14, compact = true) }
            }
        }
    }

    private companion object {
        const val HALF = 0.5f
    }
}
