package se.kodhast.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Djup och tryckkänsla: det man kan trycka på och det som är valt får en ljuskant upptill, ett svagt skimmer
 * och en mjuk skugga, och sjunker in när det trycks. Appen ger färgerna per tema (alfa ingår); i mörkt bär
 * ljuskanten djupet och skuggorna kan vara svaga. Utan djup ([AppTheme] `depth = null`) är allt platt.
 *
 * - [highlight]: ljuskanten, 1 dp innanför överkanten på knappar och valda element.
 * - [sheen]: skimret överst på fyllda knappar och valda element; tonar ut ovanför texten.
 * - [shadow]: skuggan under knappar och valda element, tonad i appens färg; också det sänkta spåret i stegaren och växeln.
 * - [cardEdge]: hårfin kant runt kort utan egen kontur – i mörkt den ljusa kant som ger kortet djup. Samma kant
 *   runt meddelandet, menyn och dialogerna.
 * - [cardShadow]: kortens mjuka skugga, och skuggan under det som flyter (meddelandet).
 * - [pressedScale]: hur stor en knapp är nedtryckt (0,97 = krymper 3 %).
 * - [cardSheen]: ett svagt skimmer de översta 64 dp på kort i kortfärgen, som tonar ut nedåt (Dagboken: teal);
 *   `null` = inget. Inte på tonade kort eller kort med egen kontur, och i en lista av bitar bara på den översta.
 */
@Immutable
data class AppDepth(
    val highlight: Color,
    val sheen: Color,
    val shadow: Color,
    val cardEdge: Color,
    val cardShadow: Color,
    val pressedScale: Float = 0.97f,
    val cardSheen: Color? = null
)
