package se.kodhast.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Ikonstorlekarna – samma i alla appar, som [Spacing]. Komponenterna och apparnas egna delar väljer
 * storlek efter var ikonen sitter, aldrig ett eget dp-värde.
 */
object IconSize {
    /** Markering i en dag (datumremsans bock). */
    val marker = 14.dp

    /** I en pill (`InfoPill`). */
    val pill = 16.dp

    /** I ett chip och en sektionsrubrik. */
    val small = 18.dp

    /** I en knapp (`AppButton`, `AddSplitButton`). */
    val button = 20.dp

    /** I en rad eller ett mätvärde (kryss, `StatPill`, flikarna). */
    val row = 22.dp

    /** Material 3:s standardstorlek – menyer och det kompakta tomma tillståndet. */
    val standard = 24.dp
}
