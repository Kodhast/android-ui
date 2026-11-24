package se.kodhast.ui.testing

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.components.AppButton
import se.kodhast.ui.components.AppFloatingToolbar
import se.kodhast.ui.components.ButtonVariant
import se.kodhast.ui.components.ConfirmDialog
import se.kodhast.ui.components.EmptyAction
import se.kodhast.ui.components.EmptyContent
import se.kodhast.ui.components.EntityListScreen
import se.kodhast.ui.components.ItemRow
import se.kodhast.ui.components.R
import se.kodhast.ui.components.ToolbarAction
import se.kodhast.ui.components.ToolbarItem

/** D-3: högst en primär handling (fylld knapp eller FAB) synlig per fönster. */
@RunWith(RobolectricTestRunner::class)
class PrimaryActionsTest {

    @get:Rule
    val rule = createComposeRule()

    private fun show(content: @Composable () -> Unit) = rule.setContent { ContractTheme(content) }

    private fun failure(): AssertionError = assertThrows(AssertionError::class.java) { rule.assertSinglePrimaryAction() }

    @Test
    fun `en fylld knapp bland tonade godkänns`() {
        show {
            Column {
                AppButton("Spara", {}, variant = ButtonVariant.Primary)
                AppButton("Lägg till", {})
                AppButton("Avbryt", {}, variant = ButtonVariant.Text)
            }
        }
        rule.assertSinglePrimaryAction()
    }

    @Test
    fun `standardknappen är tonad och räknas inte`() {
        show {
            Column {
                AppButton("Ett", {})
                AppButton("Två", {})
            }
        }
        rule.assertSinglePrimaryAction()
    }

    @Test
    fun `två fyllda knappar fäller provet med båda texterna`() {
        show {
            Column {
                AppButton("Spara", {}, variant = ButtonVariant.Primary)
                AppButton("Importera", {}, variant = ButtonVariant.Primary)
            }
        }
        val message = failure().message.orEmpty()
        assertTrue(message, "Spara" in message && "Importera" in message)
    }

    @Test
    fun `en destruktiv fylld knapp räknas som primär`() {
        show {
            Column {
                AppButton("Spara", {}, variant = ButtonVariant.Primary)
                AppButton("Radera", {}, variant = ButtonVariant.Destructive)
            }
        }
        failure()
    }

    @Test
    fun `FAB-knappen och en fylld knapp fäller provet`() {
        show {
            Column {
                AppButton("Spara", {}, variant = ButtonVariant.Primary)
                AppFloatingToolbar(listOf(ToolbarItem("Idag", R.drawable.ic_box)), 0, {}, action = ToolbarAction("Lägg till", R.drawable.ic_add) {})
            }
        }
        failure()
    }

    @Test
    fun `en dialog räknas för sig, inte ihop med skärmen bakom`() {
        show {
            AppButton("Spara", {}, variant = ButtonVariant.Primary)
            ConfirmDialog("Radera resan?", "Det går inte att ångra.", "Radera", {}, {}, destructive = true)
        }
        rule.assertSinglePrimaryAction()
    }

    @Test
    fun `en fylld knapp utrullad ur bild räknas inte`() {
        show {
            Column(Modifier.size(200.dp).verticalScroll(rememberScrollState())) {
                AppButton("Spara", {}, variant = ButtonVariant.Primary)
                Column(Modifier.height(2000.dp)) {}
                AppButton("Längst ned", {}, variant = ButtonVariant.Primary)
            }
        }
        rule.assertSinglePrimaryAction()
    }

    @Test
    fun `listkontraktet fäller ett tomt tillstånd med två fyllda knappar`() {
        val twoPrimary = EmptyContent(R.drawable.ic_box, "Inga resor än", "Lägg till en resa.", actions = listOf(EmptyAction("Ny resa", {}), EmptyAction("Importera", {})))
        assertThrows(AssertionError::class.java) {
            rule.runListScreenContract(contractEnvironment, "Solkräm", "Solkräm", twoPrimary.title, addLabel = null) { state, _, onRetry ->
                EntityListScreen("Resor", state, twoPrimary, add = null, key = { it }, onRetry = onRetry) { ItemRow(it) }
            }
        }
    }
}
