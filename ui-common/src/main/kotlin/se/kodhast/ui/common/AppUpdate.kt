package se.kodhast.ui.common

import androidx.compose.runtime.Immutable
import java.net.HttpURLConnection
import java.net.URI

/** En nyare version av appen än den som körs: [version] utan `v` och [downloadUrl] till APK:n (#125). */
@Immutable
data class AppUpdate(val version: String, val downloadUrl: String)

/**
 * Frågar GitHub efter senaste release av [owner]/[repo] och svarar med en [AppUpdate] när den är nyare än
 * [currentVersion], annars `null` – också vid nätfel, ett svar som inte går att läsa eller en release utan APK.
 * Repona är publika, så ingen nyckel behövs (60 anrop i timmen per IP). `releases/latest` tar inte med utkast
 * eller förhandsreleaser. [fetch] hämtar en adress som text (`null` vid fel) och byts i tester.
 */
class UpdateChecker(
    private val owner: String,
    private val repo: String,
    private val currentVersion: String,
    private val fetch: (String) -> String? = ::httpGet
) {
    fun check(): AppUpdate? {
        val json = runCatching { fetch("https://api.github.com/repos/$owner/$repo/releases/latest") }.getOrNull() ?: return null
        val tag = TAG.find(json)?.groupValues?.get(1) ?: return null
        val apk = APK.find(json)?.groupValues?.get(1) ?: return null
        val version = tag.removePrefix("v")
        return if (isNewerVersion(version, currentVersion)) AppUpdate(version, apk) else null
    }

    private companion object {
        // Två fält ur GitHubs svar räcker; ett JSON-bibliotek bara för dem vore ett beroende till.
        val TAG = Regex("\"tag_name\"\\s*:\\s*\"([^\"]+)\"")
        val APK = Regex("\"browser_download_url\"\\s*:\\s*\"(https://[^\"]+\\.apk)\"")
    }
}

/**
 * Sant när [candidate] är en högre version än [current], tal för tal (`4.10.0` > `4.9.0`). Det som följer
 * efter talen (`-debug`, `+20261122`) räknas inte; går en av dem inte att läsa alls blir svaret falskt.
 */
fun isNewerVersion(candidate: String, current: String): Boolean {
    val a = versionParts(candidate) ?: return false
    val b = versionParts(current) ?: return false
    for (i in 0 until maxOf(a.size, b.size)) {
        val x = a.getOrElse(i) { 0 }
        val y = b.getOrElse(i) { 0 }
        if (x != y) return x > y
    }
    return false
}

private fun versionParts(version: String): List<Int>? = Regex("^v?(\\d+(?:\\.\\d+)*)").find(version.trim())?.groupValues?.get(1)?.split('.')?.map { it.toInt() }

private fun httpGet(url: String): String? {
    val connection = URI(url).toURL().openConnection() as HttpURLConnection
    return try {
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = TIMEOUT_MS
        connection.setRequestProperty("Accept", "application/vnd.github+json")
        if (connection.responseCode == HttpURLConnection.HTTP_OK) connection.inputStream.bufferedReader().use { it.readText() } else null
    } finally {
        connection.disconnect()
    }
}

private const val TIMEOUT_MS = 10_000
