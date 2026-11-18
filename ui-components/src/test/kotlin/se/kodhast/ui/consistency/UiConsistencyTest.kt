package se.kodhast.ui.consistency

import com.lemonappdev.konsist.api.Konsist
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail
import org.junit.Test

/**
 * Regel 4 i biblioteket: samma element ser likadant ut överallt. Mönstren står bara i
 * `ui-forbidden.txt`, undantagen bara i `ui-allowlist.txt` (båda i repots rot), med samma
 * format som apparnas filer.
 */
class UiConsistencyTest {

    private val repoRoot = File("").absoluteFile.let { if (it.name.startsWith("ui-")) it.parentFile else it }
    private val patterns = parsePatterns(File(repoRoot, "ui-forbidden.txt").readText())
    private val allowlist by lazy { parseAllowlist(File(repoRoot, "ui-allowlist.txt").readText()) }
    private val production = Konsist.scopeFromProduction(moduleName = "ui-components") +
        Konsist.scopeFromProduction(moduleName = "ui-theme") +
        Konsist.scopeFromProduction(moduleName = "ui-common") +
        Konsist.scopeFromProduction(moduleName = "ui-navigation") +
        Konsist.scopeFromProduction(moduleName = "ui-testing")

    @Test
    fun `mönsterfilen har alla scope och en ersättning per mönster`() {
        assertEquals(setOf("components", "theme", "expressive"), patterns.keys)
        patterns.values.flatten().forEach { assertTrue(it.replacement.isNotBlank(), it.regex.pattern) }
    }

    @Test
    fun `ingen kod i bibliotekets main använder förbjudna mönster`() {
        val files = production.files.toList()
        assertTrue(listOf("/ui-theme/", "/ui-components/", "/ui-common/", "/ui-navigation/", "/ui-testing/").all { module -> files.any { it.path.contains(module) } }, "hittade inte alla moduler")
        val hits = files.flatMap { file ->
            val rel = File(file.path).relativeTo(repoRoot).invariantSeparatorsPath
            findViolations(rel, file.text).map { (line, match, replacement) -> "  $rel:$line  `${match.trim()}`  → använd $replacement" }
        }
        if (hits.isNotEmpty()) {
            fail("Regel 4: förbjudna mönster.\n" + hits.joinToString("\n") + "\nUndantag bara via ui-allowlist.txt med motivering och användarens ok.")
        }
    }

    @Test
    fun `varje undantag används fortfarande`() {
        val texts = production.files.associate { File(it.path).relativeTo(repoRoot).invariantSeparatorsPath to it.text }
        val stale = allowlist.filter { allowed -> texts[allowed.file]?.contains(allowed.symbol) != true }
        assertEquals(emptyList(), stale, "ta bort raderna ur ui-allowlist.txt")
    }

    @Test
    fun `scope per sökväg`() {
        assertEquals(listOf("components", "expressive"), scopesFor("ui-components/src/main/kotlin/se/kodhast/ui/components/X.kt"))
        assertEquals(listOf("theme", "expressive"), scopesFor("ui-theme/src/main/kotlin/se/kodhast/ui/theme/X.kt"))
        assertEquals(listOf("expressive"), scopesFor("ui-common/src/main/kotlin/se/kodhast/ui/common/X.kt"))
        assertEquals(listOf("expressive"), scopesFor("ui-testing/src/main/kotlin/se/kodhast/ui/testing/X.kt"))
        assertEquals(listOf("components", "expressive"), scopesFor("ui-navigation/src/main/kotlin/se/kodhast/ui/navigation/X.kt"))
        assertEquals(emptyList(), scopesFor("ui-components/src/test/kotlin/se/kodhast/ui/components/X.kt"))
    }

    @Test
    fun `mönstren fäller hårdkodat utseende men inte temats värden`() {
        val file = "ui-components/src/main/kotlin/se/kodhast/ui/components/X.kt"
        listOf(
            "Box(Modifier.background(Color(0xFFFFFFFF)))",
            "val shape = RoundedCornerShape(12.dp)",
            "Text(text, fontSize = 14.sp)",
            "Modifier.padding(horizontal = 12.dp)",
            "Modifier.size(20.dp)",
            "style = MaterialTheme.typography.bodyLarge"
        ).forEach { assertTrue(findViolations(file, it).isNotEmpty(), it) }
        listOf(
            "Box(Modifier.background(AppTheme.extendedColors.card, AppTheme.shapes.row))",
            "Text(text, style = AppTheme.typography.button)",
            "Modifier.padding(horizontal = Spacing.m).size(ICON_SIZE)",
            "// Color(0xFF000000) i en kommentar"
        ).forEach { assertEquals(emptyList(), findViolations(file, it), it) }
    }

    /** En komponent ritar något: en publik `@Composable` som returnerar `Unit`. En som ger ett värde (t.ex. `chipColors()`) är ingen komponent. */
    private val componentFunctions by lazy {
        production.functions()
            .filter { (it.resideInPackage("..ui.components..") || it.resideInPackage("..ui.navigation..")) && it.hasPublicOrDefaultModifier && it.hasAnnotationWithName("Composable") }
            .filter { it.returnType.let { type -> type == null || type.name == "Unit" } }
            // En override (t.ex. i en privat klass) är ingen egen komponent.
            .filter { !it.hasOverrideModifier }
    }
    private val components by lazy { componentFunctions.map { it.name }.toSet() }

    @Test
    fun `varje publik komponent finns i komponentgalleriet`() {
        assertTrue(components.isNotEmpty(), "hittade inga komponenter")
        val gallery = File(repoRoot, GALLERY_DIR).listFiles { file -> file.extension == "kt" }.orEmpty().joinToString("\n") { it.readText() }
        assertTrue(gallery.contains("fun ComponentGallery("), "ComponentGallery saknas i $GALLERY_DIR")
        // En föråldrad form (`@Deprecated`, t.ex. `SignInButton`) ska inte användas och visas inte i galleriet.
        val current = componentFunctions.filterNot { it.hasAnnotationWithName("Deprecated") }.map { it.name }.toSet()
        val missing = (current - "ComponentGallery").filterNot { Regex("""\b$it\s*[({<]""").containsMatchIn(gallery) }
        assertTrue(missing.isEmpty(), "Saknas i galleriet ($GALLERY_DIR/LibraryGallery.kt): ${missing.sorted()}")
    }

    @Test
    fun `varje publik komponent har skärmdump ljust och mörkt och står i README`() {
        assertTrue(components.isNotEmpty(), "hittade inga komponenter")
        val screenshots = listOf("ui-components", "ui-navigation").flatMap { File(repoRoot, "$it/src/test/screenshots").list().orEmpty().toList() }.toSet()
        val readme = File(repoRoot, "README.md").readText()
        val problems = components.flatMap { name ->
            listOfNotNull(
                "$name saknar skärmdump ${name}_*_light.png".takeIf { screenshots.none { it.startsWith("${name}_") && it.endsWith("_light.png") } },
                "$name saknar skärmdump ${name}_*_dark.png".takeIf { screenshots.none { it.startsWith("${name}_") && it.endsWith("_dark.png") } },
                "$name saknas i README:s komponentlista".takeIf { !readme.contains("| `$name` |") }
            )
        }
        assertTrue(problems.isEmpty(), problems.joinToString("\n"))
    }

    @Test
    fun `varje publik komponent har ett beteendetest`() {
        assertTrue(components.isNotEmpty(), "hittade inga komponenter")
        // Skärmdumpstesterna räknas inte: de visar hur komponenten ser ut, inte att den gör rätt.
        val tests = listOf("ui-components", "ui-navigation").flatMap { module ->
            File(repoRoot, "$module/src/test/kotlin").walkTopDown().filter { it.extension == "kt" && !it.name.contains("Screenshot") }.toList()
        }.joinToString("\n") { it.readText() }
        val missing = components.filterNot { Regex("""\b$it\s*[({<]""").containsMatchIn(tests) }
        assertTrue(missing.isEmpty(), "Saknar beteendetest (regel 3): ${missing.sorted()}")
    }

    @Test
    fun `README har bibliotekets version och alla artefakter`() {
        val version = File(repoRoot, "gradle.properties").readLines().first { it.startsWith("version=") }.substringAfter("=").trim()
        val readme = File(repoRoot, "README.md").readText()
        assertEquals(version, Regex("""\*\*Version:\*\* (\S+)""").find(readme)?.groupValues?.get(1), "versionsraden i README")
        val examples = Regex("""se\.kodhast\.ui:ui-[a-z]+:(\d[^"]*)""").findAll(readme).map { it.groupValues[1] }.toSet()
        assertEquals(setOf(version), examples, "versionen i README:s Gradle-exempel")
        val modules = Regex("""":(ui-[a-z]+)"""").findAll(File(repoRoot, "settings.gradle.kts").readText()).map { it.groupValues[1] }.toList()
        val listed = Regex("""\| `se\.kodhast\.ui:(ui-[a-z]+)` \|""").findAll(readme).map { it.groupValues[1] }.toList()
        assertEquals(modules.sorted(), listed.sorted(), "modultabellen i README")
        val count = NUMBERS.getOrNull(modules.size)
        assertTrue(count != null && readme.contains("publicerar alla $count"), "README:s publicering ska säga \"alla $count\" artefakterna")
    }

    private companion object {
        val NUMBERS = listOf("noll", "en", "två", "tre", "fyra", "fem", "sex", "sju", "åtta", "nio", "tio")
        const val GALLERY_DIR = "ui-navigation/src/main/kotlin/se/kodhast/ui/navigation/gallery"
    }

    private data class Rule(val regex: Regex, val replacement: String)

    private data class Allowed(val file: String, val symbol: String)

    private fun scopesFor(relPath: String): List<String> {
        if (!relPath.endsWith(".kt") || !relPath.contains("/src/main/")) return emptyList()
        return listOfNotNull(
            "components".takeIf { relPath.startsWith("ui-components/") || relPath.startsWith("ui-navigation/") },
            "theme".takeIf { relPath.startsWith("ui-theme/") },
            "expressive"
        )
    }

    private fun findViolations(relPath: String, content: String): List<Triple<Int, String, String>> {
        val active = scopesFor(relPath).filter { it in patterns }
        return content.lines().flatMapIndexed { i, line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("//") || trimmed.startsWith("*") || trimmed.startsWith("import ")) return@flatMapIndexed emptyList()
            active.flatMap { scope ->
                patterns.getValue(scope).mapNotNull { rule ->
                    val match = rule.regex.find(line) ?: return@mapNotNull null
                    val allowed = allowlist.any { it.file == relPath && (match.value.contains(it.symbol) || it.symbol.contains(match.value.trim())) }
                    if (allowed) null else Triple(i + 1, match.value, rule.replacement)
                }
            }
        }
    }

    /** Sektionerad fil: `[namn]` följt av `<regex><TAB><ersättning>`; tomma rader och `#`-rader hoppas över. */
    private fun parsePatterns(text: String): Map<String, List<Rule>> {
        val sections = linkedMapOf<String, MutableList<Rule>>()
        var current: MutableList<Rule>? = null
        text.lines().filter { it.isNotBlank() && !it.startsWith("#") }.forEach { line ->
            val section = Regex("""^\[([\w-]+)]\s*$""").find(line)
            if (section != null) {
                current = mutableListOf<Rule>().also { sections[section.groupValues[1]] = it }
            } else {
                val parts = line.split("\t")
                current?.add(Rule(Regex(parts[0]), parts.getOrElse(1) { "" }))
            }
        }
        return sections
    }

    /** Format per rad: `<sökväg>:<symbol> – <motivering>`. En rad utan symbol eller motivering är ett fel. */
    private fun parseAllowlist(text: String): List<Allowed> = text.lines().mapIndexedNotNull { i, line ->
        if (line.isBlank() || line.startsWith("#")) return@mapIndexedNotNull null
        val parts = line.split(Regex("""\s+[–-]\s+"""))
        val idx = parts[0].lastIndexOf(':')
        val file = if (idx > 0) parts[0].substring(0, idx).trim() else ""
        val symbol = if (idx > 0) parts[0].substring(idx + 1).trim() else ""
        require(file.isNotEmpty() && symbol.isNotEmpty() && parts.drop(1).joinToString("").isNotBlank()) {
            "ui-allowlist.txt rad ${i + 1}: ogiltig rad, formatet är \"<sökväg>:<symbol> – <motivering>\""
        }
        Allowed(file, symbol)
    }
}
