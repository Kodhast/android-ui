package se.kodhast.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.blur.BlurRadiusSpec
import androidx.compose.ui.graphics.blur.BlurStop
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import se.kodhast.ui.theme.AppBlur
import se.kodhast.ui.theme.AppTheme

/**
 * Progressiv oskärpa på innehållets överkant, där det rullat in under sidhuvudet: suddigast precis under
 * sidhuvudet ([AppBlur.headerRadius]) och skarpt [AppBlur.headerFade] längre ned. [overlap] (0–1) säger hur
 * långt innehållet rullat in under sidhuvudet; 0 = i vila, ingen oskärpa. Läses bara när lagret ritas, så
 * rullningen ger ingen rekomposition. Utan temats oskärpa ([AppTheme.blur] `null`) gör den inget; före
 * Android 12 ritar Compose den inte.
 */
@Composable
internal fun Modifier.underHeaderBlur(overlap: () -> Float): Modifier {
    val theme = AppTheme.blur ?: return this
    return this.blur { blurRadiusSpec = underHeaderSpec(theme, overlap(), size.height) }
}

internal fun underHeaderSpec(blur: AppBlur, overlap: Float, height: Dp): BlurRadiusSpec {
    val fade = blur.headerFade
    if (overlap <= 0f || height <= 0.dp || fade <= 0.dp) return BlurRadiusSpec.uniform(0.dp)
    val radius = blur.headerRadius * overlap.coerceAtMost(1f)
    return BlurRadiusSpec.verticalGradient(listOf(BlurStop(0f, radius), BlurStop((fade / height).coerceAtMost(1f), 0.dp)))
}
