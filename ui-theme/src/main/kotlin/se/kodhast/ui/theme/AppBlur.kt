package se.kodhast.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Oskärpa bakom det som läggs ovanpå: innehållet bakom en öppen dialog eller ett ark blir suddigt och
 * slöjan svagare. Kräver att enheten har oskärpa mellan fönster påslagen (Android 12+, inte i
 * energisparläget); annars ligger dagens slöja kvar. Samma i alla appar; `null` i [AppTheme] = av.
 *
 * - [behindRadius]: hur suddigt innehållet bakom blir.
 * - [scrimAlpha]: slöjans styrka ovanpå oskärpan (0–1); svagare än utan oskärpa, eftersom suddet redan lyfter dialogen.
 * - [headerRadius]: hur suddigt innehållet är precis under sidhuvudet när det rullats in under det.
 * - [headerFade]: hur långt ned under sidhuvudet suddet tonar ut till skarpt.
 *
 * Oskärpan under sidhuvudet ritas i appens eget fönster och kräver bara Android 12+.
 */
@Immutable
data class AppBlur(
    val behindRadius: Dp = 12.dp,
    val scrimAlpha: Float = 0.2f,
    val headerRadius: Dp = 8.dp,
    val headerFade: Dp = 24.dp
)
