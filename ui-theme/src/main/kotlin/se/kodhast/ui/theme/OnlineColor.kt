package se.kodhast.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Pluppen på kontoavataren (`AccountAvatar(online = true)`): samma gröna i alla appar, oavsett appens färger
 * (Mats 2026-11-27). Bibliotekets enda egna färg – undantaget står i `ui-allowlist.txt` – och den står i
 * `tokens.json`, så att webben (Kodhast/web-ui) får samma.
 */
object OnlineColor {
    /** På ljus yta; minst 3:1 mot en vit eller nästan vit bakgrund. */
    val light = Color(0xFF1E8E3E)

    /** På mörk yta. */
    val dark = Color(0xFF6DD58C)

    /** Den gröna för en yta med färgen [surface]: mörk yta ger [dark], ljus [light]. */
    fun on(surface: Color): Color = if (surface.luminance() < 0.5f) dark else light
}
