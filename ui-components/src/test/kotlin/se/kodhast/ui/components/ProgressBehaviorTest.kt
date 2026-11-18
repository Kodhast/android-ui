package se.kodhast.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.components.testing.TestPalette
import se.kodhast.ui.components.testing.TestTheme
import se.kodhast.ui.components.testing.assertSameColor
import se.kodhast.ui.components.testing.pixelAt
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.ProgressDoneColors

/** Framstegsraden (android-ui #55): texterna, klart-läget, kompakt läge, TalkBack och färgerna ur temat. */
@RunWith(RobolectricTestRunner::class)
class ProgressBehaviorTest {

    @get:Rule
    val rule = createComposeRule()

    private fun show(content: @Composable () -> Unit) = rule.setContent { TestTheme(content = content) }

    @Composable
    private fun Progress(done: Int, total: Int, celebrate: Boolean = true, compact: Boolean = false) = AppProgress(
        done,
        total,
        label = { d, t -> "$d av $t klara" },
        doneLabel = { t -> "$t av $t · allt klart" },
        celebrate = celebrate,
        compact = compact
    )

    private fun progress(fraction: Float) = SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo(fraction, 0f..1f))

    @Test
    fun `halvvägs visar x av y och läses med framsteget`() {
        show { Progress(4, 8) }
        rule.onNodeWithText("4 av 8 klara", useUnmergedTree = true).assertExists()
        rule.onNodeWithContentDescription("4 av 8 klara").assert(progress(0.5f))
    }

    @Test
    fun `0 av 0 är inte klart och har inget framsteg`() {
        show { Progress(0, 0) }
        rule.onNodeWithContentDescription("0 av 0 klara").assert(progress(0f))
    }

    @Test
    fun `allt klart visar appens klart-text`() {
        show { Progress(9, 9) }
        rule.onNodeWithText("9 av 9 · allt klart", useUnmergedTree = true).assertExists()
        rule.onNodeWithContentDescription("9 av 9 · allt klart").assert(progress(1f))
    }

    @Test
    fun `klart-läget kan ha en egen talad text`() {
        show {
            AppProgress(9, 9, label = { d, t -> "$d av $t klara" }, doneLabel = { t -> "$t av $t · allt klart" }, doneDescription = { t -> "$t av $t klara, allt klart" })
        }
        rule.onNodeWithText("9 av 9 · allt klart", useUnmergedTree = true).assertExists()
        rule.onNodeWithContentDescription("9 av 9 klara, allt klart").assert(progress(1f))
    }

    @Test
    fun `utan celebrate stannar texten i x av y även när allt är klart`() {
        show { Progress(9, 9, celebrate = false) }
        rule.onNodeWithText("9 av 9 klara", useUnmergedTree = true).assertExists()
        rule.onNodeWithText("9 av 9 · allt klart", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `fler klara än totalt räknas som totalt`() {
        show { Progress(12, 9) }
        rule.onNodeWithContentDescription("9 av 9 · allt klart").assert(progress(1f))
    }

    @Test
    fun `kompakt visar x snedstreck y och läses med appens text`() {
        show { Progress(9, 14, compact = true) }
        rule.onNodeWithContentDescription("9 av 14 klara").assert(progress(9f / 14f))
        rule.onNodeWithText("9 / 14", useUnmergedTree = true).assertExists()
        rule.onNodeWithText("9 av 14 klara", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `fyllnaden är primärfärgen och vid klart temats progressDone`() {
        rule.setContent {
            TestTheme {
                val extended = AppTheme.extendedColors.copy(progressDone = ProgressDoneColors(DONE))
                AppTheme(MaterialTheme.colorScheme, extended, AppTheme.shapes, MaterialTheme.typography) {
                    Column {
                        Progress(9, 9)
                    }
                }
            }
        }
        assertSameColor(DONE, rule.onNodeWithTag(PROGRESS_FILL_TAG, useUnmergedTree = true).pixelAt(rule.density, 20.dp, 4.dp))
    }

    @Test
    fun `fyllnaden är primärfärgen innan allt är klart och utan progressDone`() {
        show { Progress(9, 9, celebrate = false) }
        assertSameColor(TestPalette.light.primary, rule.onNodeWithTag(PROGRESS_FILL_TAG, useUnmergedTree = true).pixelAt(rule.density, 20.dp, 4.dp))
    }

    private companion object {
        val DONE = Color(0xFF5E8A2E)
    }
}
