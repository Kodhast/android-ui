package se.kodhast.ui.navigation

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Test

/** Flikar med egna stackar, start på första fliken, tillbaka via första fliken – som i RA och Dagboken. */
class TabBackStackTest {

    private fun restore(saved: String) = TabBackStack.restore(saved, TABS, TestKey.serializer())

    @Test
    fun `startar på första fliken med verktygsraden synlig`() {
        val stack = testBackStack()
        assertEquals(listOf<TestKey>(Home), stack.entries)
        assertEquals(Home, stack.currentTab)
        assertTrue(stack.atTopLevel)
    }

    @Test
    fun `en undersida döljer verktygsraden och tillbaka visar den igen`() {
        val stack = testBackStack()
        stack.push(Edit())
        assertFalse(stack.atTopLevel)
        assertTrue(stack.pop())
        assertTrue(stack.atTopLevel)
    }

    @Test
    fun `varje flik minns sin undersida när man byter flik`() {
        val stack = testBackStack()
        stack.select(People)
        stack.push(Detail("anna"))
        stack.select(Shared)
        assertEquals(listOf(Home, Shared), stack.entries)
        stack.select(People)
        assertEquals(listOf(Home, People, Detail("anna")), stack.entries)
    }

    @Test
    fun `tillbaka i roten av en annan flik går till första fliken, i dess rot lämnar appen`() {
        val stack = testBackStack()
        stack.select(Shared)
        assertTrue(stack.pop())
        assertEquals(Home, stack.currentTab)
        assertFalse(stack.pop())
    }

    @Test
    fun `att välja fliken man står på går till flikens rot`() {
        val stack = testBackStack()
        stack.select(People)
        stack.push(Detail("anna"))
        stack.push(Edit("anna"))
        stack.select(People)
        assertEquals(listOf(Home, People), stack.entries)
    }

    @Test
    fun `flikar väljs, de läggs inte på stacken, och bara flikar kan väljas`() {
        assertFailsWith<IllegalArgumentException> { testBackStack().push(People) }
        assertFailsWith<IllegalArgumentException> { testBackStack().select(Settings) }
        assertFailsWith<IllegalArgumentException> { testBackStack().openInTab(Settings, Edit()) }
    }

    @Test
    fun `flikarna är minst en och utan dubbletter`() {
        assertFailsWith<IllegalArgumentException> { TabBackStack(emptyList(), TestKey.serializer()) }
        assertFailsWith<IllegalArgumentException> { TabBackStack(listOf(Home, Home), TestKey.serializer()) }
    }

    @Test
    fun `ett dubbeltryck öppnar skärmen en gång`() {
        val stack = testBackStack()
        stack.push(Edit())
        stack.push(Edit())
        assertEquals(listOf(Home, Edit()), stack.entries)
        stack.push(Detail("t1"))
        stack.push(Detail("t2"))
        assertEquals(listOf(Home, Edit(), Detail("t1"), Detail("t2")), stack.entries)
    }

    @Test
    fun `en skärm som stänger sig två gånger stänger inte skärmen under`() {
        val stack = testBackStack()
        stack.select(People)
        stack.push(Detail("p1"))
        stack.push(Edit("p1"))

        assertTrue(stack.popIfTop(Edit("p1")))
        assertFalse(stack.popIfTop(Edit("p1")))

        assertEquals(listOf(Home, People, Detail("p1")), stack.entries)
    }

    @Test
    fun `en stängare kör sitt efteråt bara när skärmen stängdes`() {
        val stack = testBackStack()
        stack.push(Edit("e1"))
        var after = 0
        val close = stack.closer(Edit("e1")) { after++ }
        close()
        close()
        assertEquals(1, after)
        assertEquals(listOf<TestKey>(Home), stack.entries)
    }

    @Test
    fun `en skärm ovanpå en flik stängs tillbaka till fliken`() {
        val stack = testBackStack()
        stack.select(People)
        stack.push(Settings)
        assertEquals(listOf(Home, People, Settings), stack.entries)
        assertFalse(stack.atTopLevel)
        stack.popIfTop(Settings)
        assertEquals(listOf(Home, People), stack.entries)
        assertEquals(People, stack.currentTab)
    }

    @Test
    fun `openInTab öppnar skärmen överst i fliken, det som var öppet ligger kvar under`() {
        val stack = testBackStack()
        stack.push(Edit())
        stack.select(People)
        stack.push(Detail("anna"))
        stack.openInTab(Home, Detail("gc"))
        assertEquals(listOf(Home, Edit(), Detail("gc")), stack.entries)
        assertEquals(Home, stack.currentTab)
        stack.select(People)
        assertEquals(listOf(Home, Edit(), Detail("gc"), People, Detail("anna")), stack.entries)
    }

    @Test
    fun `stackarna överlever att sparas och återställas`() {
        val stack = testBackStack()
        stack.push(Detail("resa-1"))
        stack.select(People)
        stack.push(Edit("anna"))
        val restored = restore(stack.save())
        assertEquals(stack.entries, restored.entries)
        assertEquals(People, restored.currentTab)
        restored.select(Home)
        assertEquals(listOf(Home, Detail("resa-1")), restored.entries)
    }

    @Test
    fun `det sparade formatet är appernas tidigare, så ett läge från före bytet läses`() {
        val pkg = "se.kodhast.ui.navigation"
        val saved = """{"current":{"type":"$pkg.People"},"stacks":[[{"type":"$pkg.Home"},{"type":"$pkg.Detail","id":"resa-1"}],""" +
            """[{"type":"$pkg.People"}],[{"type":"$pkg.Shared"}]]}"""

        val restored = restore(saved)

        assertEquals(listOf(Home, Detail("resa-1"), People), restored.entries)
        assertEquals(People, restored.currentTab)
        restored.select(Home)
        assertEquals(listOf(Home, Detail("resa-1")), restored.entries)
    }

    @Test
    fun `ett oläsligt sparat läge ger startläget`() {
        assertEquals(listOf<TestKey>(Home), restore("inte json").entries)
    }

    @Test
    fun `en nyckel som inte finns längre ger startläget`() {
        val old = "se.annat.paket"
        val saved = """{"current":{"type":"$old.People"},"stacks":[[{"type":"$old.Home"}],[{"type":"$old.People"}],[{"type":"$old.Shared"}]]}"""
        assertEquals(listOf<TestKey>(Home), restore(saved).entries)
    }

    @Test
    fun `ett läge med andra flikar ger startläget`() {
        val pkg = "se.kodhast.ui.navigation"
        val fourTabs = """{"current":{"type":"$pkg.Settings"},"stacks":[[{"type":"$pkg.Home"}],[{"type":"$pkg.People"}],""" +
            """[{"type":"$pkg.Shared"}],[{"type":"$pkg.Settings"}]]}"""
        val notATab = """{"current":{"type":"$pkg.Settings"},"stacks":[[{"type":"$pkg.Home"}],[{"type":"$pkg.People"}],[{"type":"$pkg.Shared"}]]}"""
        val tabInsideStack = """{"current":{"type":"$pkg.Home"},"stacks":[[{"type":"$pkg.Home"},{"type":"$pkg.People"}],[{"type":"$pkg.People"}],[{"type":"$pkg.Shared"}]]}"""

        for (saved in listOf(fourTabs, notATab, tabInsideStack)) {
            val restored = restore(saved)
            assertEquals(listOf<TestKey>(Home), restored.entries, saved)
            assertEquals(Home, restored.currentTab)
        }
    }
}
