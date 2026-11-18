package se.kodhast.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * En färgton för pills, sektionsrubriker och kort. Biblioteket känner bara [Primary]; appen
 * lägger till sina egna som tillägg på [Companion], t.ex. `val Tone.Companion.Green get() = Tone("green")`,
 * och ger dem färger i [ExtendedColors.tones].
 */
@Immutable
@JvmInline
value class Tone(val key: String) {
    companion object {
        /** Det markerade, valda och aktiva – appens primärton. */
        val Primary = Tone("primary")
    }
}

/** Behållare och innehåll för en [Tone]; innehållet ska klara 4,5:1 mot behållaren. */
@Immutable
data class ToneColors(val container: Color, val content: Color)

/**
 * Appens färger utöver Material 3-rollerna. Appen fyller i dem; biblioteket har inga egna färger.
 *
 * - [card]: kortens och svepbara raders yta.
 * - [rowTint]: tonad bakgrund på en rad.
 * - [toolbar] / [onToolbar]: den flytande verktygsraden.
 * - [tones]: färgerna per [Tone]; måste innehålla [Tone.Primary].
 * - [accents]: appens dekorativa färgserie (personfärger, valbara färger) – t.ex. konfettin.
 * - [online]: pluppen på kontoavataren när appen har internet; `null` (standard) = `colorScheme.primary`.
 * - [track]: spåret i framstegsraden (`AppProgress`); `null` (standard) = `colorScheme.primaryContainer`.
 * - [progressDone]: framstegsradens fyllnad när allt är klart; `null` (standard) = `colorScheme.primary` utan kontur.
 */
@Immutable
data class ExtendedColors(
    val card: Color,
    val rowTint: Color,
    val toolbar: Color,
    val onToolbar: Color,
    val tones: Map<Tone, ToneColors>,
    val accents: List<Color>,
    val online: Color? = null,
    val track: Color? = null,
    val progressDone: ProgressDoneColors? = null
) {
    init {
        require(Tone.Primary in tones) { "tones saknar Tone.Primary" }
        require(accents.isNotEmpty()) { "accents får inte vara tom" }
    }

    /** Primärtonen ([Tone.Primary]). */
    val primaryTone: ToneColors get() = tones.getValue(Tone.Primary)

    /** Färgerna för [tone]; en ton appen inte gett färger får primärtonen. */
    fun tone(tone: Tone): ToneColors = tones[tone] ?: primaryTone
}

/**
 * Framstegsradens fyllnad när allt är klart: [fill] och en valfri [outline] (1 dp) för en fyllnad som
 * syns för dåligt mot kortet ensam – t.ex. Dagbokens solgula.
 */
@Immutable
data class ProgressDoneColors(val fill: Color, val outline: Color? = null)
