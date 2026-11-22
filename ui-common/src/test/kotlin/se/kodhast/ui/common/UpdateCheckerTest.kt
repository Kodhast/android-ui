package se.kodhast.ui.common

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.Test

class UpdateCheckerTest {

    private fun release(tag: String, apk: String? = "https://github.com/Kodhast/App/releases/download/$tag/App-${tag.removePrefix("v")}.apk") = """
        {"url":"https://api.github.com/repos/Kodhast/App/releases/1","tag_name":"$tag","name":"App $tag","draft":false,
         "assets":[${apk?.let { """{"name":"App.apk","browser_download_url":"$it"}""" } ?: ""}]}
    """.trimIndent()

    private fun checker(current: String, answer: String?) = UpdateChecker("Kodhast", "App", current) { url ->
        assertEquals("https://api.github.com/repos/Kodhast/App/releases/latest", url)
        answer
    }

    @Test
    fun `en nyare release blir en uppdatering med APK-länken`() {
        val update = checker("0.18.3", release("v0.19.0")).check()
        assertEquals(AppUpdate("0.19.0", "https://github.com/Kodhast/App/releases/download/v0.19.0/App-0.19.0.apk"), update)
    }

    @Test
    fun `samma eller äldre version ger ingen uppdatering`() {
        assertNull(checker("0.19.0", release("v0.19.0")).check())
        assertNull(checker("0.20.0", release("v0.19.0")).check())
    }

    @Test
    fun `nätfel, trasigt svar och release utan APK ger ingen uppdatering`() {
        assertNull(checker("0.18.3", null).check())
        assertNull(checker("0.18.3", "<html>rate limit</html>").check())
        assertNull(checker("0.18.3", release("v0.19.0", apk = null)).check())
        assertNull(UpdateChecker("Kodhast", "App", "0.18.3") { error("offline") }.check())
    }

    @Test
    fun `versioner jämförs tal för tal`() {
        assertTrue(isNewerVersion("4.10.0", "4.9.0"))
        assertTrue(isNewerVersion("1.0.1", "1.0"))
        assertTrue(isNewerVersion("v2.0.0", "1.99.99"))
        assertFalse(isNewerVersion("1.0", "1.0.0"))
        assertFalse(isNewerVersion("4.9.0", "4.10.0"))
        assertFalse(isNewerVersion("1.11.4", "1.11.4-debug"))
        assertFalse(isNewerVersion("trasig", "1.0.0"))
        assertFalse(isNewerVersion("2.0.0", "okänd"))
    }
}
