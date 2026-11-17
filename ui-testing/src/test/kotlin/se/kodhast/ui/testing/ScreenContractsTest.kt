package se.kodhast.ui.testing

import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.common.EditorState
import se.kodhast.ui.common.Validator
import se.kodhast.ui.components.AddAction
import se.kodhast.ui.components.AppIdentity
import se.kodhast.ui.components.AppTextField
import se.kodhast.ui.components.ButtonVariant
import se.kodhast.ui.components.DetailHeader
import se.kodhast.ui.components.EmptyAction
import se.kodhast.ui.components.EmptyContent
import se.kodhast.ui.components.EntityDetailScreen
import se.kodhast.ui.components.EntityEditScreen
import se.kodhast.ui.components.EntityListScreen
import se.kodhast.ui.components.ItemRow
import se.kodhast.ui.components.LocalAppIdentity

/**
 * Kontrakten körs mot bibliotekets egna ramar – det bevisar att de håller för en skärm som använder
 * ramarna rätt, och att de fäller en som inte gör det.
 */
@RunWith(RobolectricTestRunner::class)
class ScreenContractsTest {

    @get:Rule
    val rule = createComposeRule()

    private val empty = EmptyContent(se.kodhast.ui.components.R.drawable.ic_box, "Inga familjeartiklar än", "Solkräm, plåster – saker hela familjen delar.")

    private val identity = AppIdentity(se.kodhast.ui.components.R.drawable.ic_box, "Dagboken")

    private fun string(id: Int) = ApplicationProvider.getApplicationContext<Context>().getString(id)

    @Test
    fun `listramen uppfyller listkontraktet`() = rule.runListScreenContract(contractEnvironment, "Solkräm", "Solkräm", empty.title, "Lägg till") { state, onAdd, onRetry ->
        EntityListScreen("Familjeartiklar", state, empty, AddAction("Lägg till", onAdd), key = { it }, onRetry = onRetry) { ItemRow(it) }
    }

    @Test
    fun `listramen utan lägg till uppfyller listkontraktet`() = rule.runListScreenContract(contractEnvironment, "Promenad", "Promenad", empty.title, addLabel = null) { state, _, onRetry ->
        EntityListScreen("Dagbok", state, empty, add = null, key = { it }, onRetry = onRetry) { ItemRow(it) }
    }

    @Test
    fun `listramen utan lägg till men med egna knappar och egen felrubrik uppfyller listkontraktet`() {
        val withActions = empty.copy(actions = listOf(EmptyAction("Ny transaktion", {}), EmptyAction("Importera avräkningsnota", {}, variant = ButtonVariant.Secondary)))
        rule.runListScreenContract(
            contractEnvironment,
            "Köp",
            "Köp",
            empty.title,
            addLabel = null,
            emptyActions = listOf("Ny transaktion", "Importera avräkningsnota"),
            errorTitle = "Kunde inte läsa händelserna"
        ) { state, _, onRetry ->
            EntityListScreen("Händelser", state, withActions, add = null, key = { it }, onRetry = onRetry, errorTitle = "Kunde inte läsa händelserna") { ItemRow(it) }
        }
    }

    @Test
    fun `listkontraktet fäller en skärm som saknar de tomma knapparna`() {
        assertThrows(AssertionError::class.java) {
            rule.runListScreenContract(contractEnvironment, "Köp", "Köp", empty.title, addLabel = null, emptyActions = listOf("Ny transaktion")) { state, _, onRetry ->
                EntityListScreen("Händelser", state, empty, add = null, key = { it }, onRetry = onRetry) { ItemRow(it) }
            }
        }
    }

    @Test
    fun `listkontraktet fäller en skärm där Försök igen inte läser om`() {
        assertThrows(AssertionError::class.java) {
            rule.runListScreenContract(contractEnvironment, "Solkräm", "Solkräm", empty.title, "Lägg till") { state, onAdd, _ ->
                EntityListScreen("Familjeartiklar", state, empty, AddAction("Lägg till", onAdd), key = { it }) { ItemRow(it) }
            }
        }
    }

    @Test
    fun `redigeringsramen uppfyller redigeringskontraktet`() {
        val editor = EditorState("", Validator { if (it.isBlank()) mapOf("name" to R.string.contract_error_required) else emptyMap() }, ContractErrors)
        rule.runEditScreenContract(
            contractEnvironment,
            editor = editor,
            makeInvalid = {
                update("name") { "x" }
                update("name") { "" }
            },
            makeValid = { update("name") { "Solkräm" } },
            invalidMessage = string(R.string.contract_error_required)
        ) { state, events, onSave, onClose ->
            EntityEditScreen("Ny familjeartikel", state, events, onSave, onClose) {
                AppTextField(state.value, { v -> editor.update("name") { v } }, "Namn", error = state.errorFor("name")?.let { string(it) })
            }
        }
    }

    @Test
    fun `redigeringskontraktet utan ogiltigt värde prövar ändrat-läget`() {
        val editor = EditorState(false, Validator { emptyMap() }, ContractErrors)
        rule.runEditScreenContract(contractEnvironment, editor, makeInvalid = null, makeValid = { update { true } }, invalidMessage = null) { state, events, onSave, onClose ->
            EntityEditScreen("Påminnelser", state, events, onSave, onClose, horizontalMargin = se.kodhast.ui.theme.Spacing.l) {}
        }
    }

    @Test
    fun `redigeringskontraktet fäller en skärm som tappar ändrat-läget`() {
        val editor = EditorState("", Validator { emptyMap() }, ContractErrors)
        assertThrows(AssertionError::class.java) {
            rule.runEditScreenContract(contractEnvironment, editor, makeInvalid = null, makeValid = { update { "Solkräm" } }, invalidMessage = null) { state, events, onSave, onClose ->
                // Fel: skärmen tappar ändrat-läget, så Spara aktiveras aldrig och bakåt frågar inte.
                EntityEditScreen("Ny familjeartikel", state.copy(isDirty = false), events, onSave, onClose) {}
            }
        }
    }

    @Test
    fun `detaljramen uppfyller detaljkontraktet`() = rule.runDetailScreenContract(contractEnvironment, "Anna", "Anna") { state, onRetry, onEdit, onBack ->
        EntityDetailScreen(state, header = { DetailHeader(it, "3 recept") }, onBack = onBack, onEdit = onEdit, onRetry = onRetry) { ItemRow("Levaxin 50 µg") }
    }

    @Test
    fun `listramen med appens identitet (sidhuvudet) uppfyller listkontraktet`() = rule.runListScreenContract(contractEnvironment, "Promenad", "Promenad", empty.title, addLabel = null) { state, _, onRetry ->
        CompositionLocalProvider(LocalAppIdentity provides identity) {
            EntityListScreen("Dagbok", state, empty, add = null, key = { it }, onRetry = onRetry) { ItemRow(it) }
        }
    }

    @Test
    fun `detaljramen i flikläge med appens identitet (sidhuvudet) uppfyller detaljkontraktet`() = rule.runDetailScreenContract(contractEnvironment, "Idag", "Morgonmedicin", editable = false, tab = true) { state, onRetry, _, _ ->
        CompositionLocalProvider(LocalAppIdentity provides identity) {
            EntityDetailScreen(state, header = null, onBack = null, title = "Idag", onRetry = onRetry) { ItemRow("Morgonmedicin") }
        }
    }

    @Test
    fun `detaljramen i flikläge uppfyller detaljkontraktet`() = rule.runDetailScreenContract(contractEnvironment, "Idag", "Morgonmedicin", editable = false, tab = true) { state, onRetry, _, _ ->
        EntityDetailScreen(state, header = null, onBack = null, title = "Idag", onRetry = onRetry) { ItemRow("Morgonmedicin") }
    }
}
