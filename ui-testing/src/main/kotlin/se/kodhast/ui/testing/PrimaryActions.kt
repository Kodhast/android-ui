package se.kodhast.ui.testing

import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.isRoot
import org.junit.Assert.fail
import se.kodhast.ui.components.PrimaryAction

/**
 * D-3 i skill `android:designregler`: högst en primär handling (fylld knapp eller FAB, märkt med
 * [PrimaryAction]) synlig per fönster. En dialog, ett ark eller en meny är ett eget fönster och räknas för
 * sig; skärmen bakom räknas inte ihop med det. En knapp som är utrullad ur bild (utanför en rullande
 * förälder) räknas inte. Fler än en → felet listar knapparnas texter.
 *
 * Körs i ramarnas kontrakt (`runListScreenContract`, `runEditScreenContract`, `runDetailScreenContract`) i
 * varje läge; en skärm utan ram anropar den i sina egna tester.
 */
fun SemanticsNodeInteractionsProvider.assertSinglePrimaryAction() {
    val roots = onAllNodes(isRoot()).fetchSemanticsNodes(atLeastOneRootRequired = false)
    roots.forEach { root ->
        val shown = root.descendants().filter { it.config.getOrNull(PrimaryAction) == true && it.isShown() }
        if (shown.size > 1) {
            fail("D-3: ${shown.size} primära handlingar syns samtidigt (högst en per vy): ${shown.joinToString { it.label() }}")
        }
    }
}

private fun SemanticsNode.descendants(): List<SemanticsNode> = children.flatMap { listOf(it) + it.descendants() }

/** Placerad och med yta kvar efter föräldrarnas klippning – det användaren kan se. */
private fun SemanticsNode.isShown(): Boolean {
    val coordinates = layoutInfo.coordinates
    if (!layoutInfo.isPlaced || !coordinates.isAttached) return false
    val bounds = coordinates.boundsInWindow()
    return bounds.width > 0f && bounds.height > 0f
}

private fun SemanticsNode.label(): String = config.getOrNull(SemanticsProperties.Text)?.joinToString(" ")
    ?: config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString(" ")
    ?: (children.map { it.label() }.firstOrNull { it != "?" } ?: "?")
