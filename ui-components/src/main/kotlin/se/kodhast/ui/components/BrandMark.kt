package se.kodhast.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Kodhästs märke: den täljda hästen med två prickar över ryggen, som två banor som färgas var för
 * sig – [horseColor] (standard `onSurfaceVariant`) och [dotsColor] (standard `primary`). Dekor utan
 * semantik; texten bredvid (t.ex. [MakerMark]) bär det som läses. Storlek med `Modifier.size` i [modifier]
 * (båda axlarna; en axel ensam ger 16 dp på den andra), standard [BrandMarkDefaults.Size]. Märket
 * ritas kvadratiskt och centrerat i ytan.
 *
 * Formen är profilens `kodhast-marke.svg` (viewport 64 × 64) och ritas inte om.
 */
@Composable
fun BrandMark(
    modifier: Modifier = Modifier,
    horseColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    dotsColor: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier.size(BrandMarkDefaults.Size)) {
        val side = size.minDimension
        val unit = side / VIEWPORT
        translate((size.width - side) / 2, (size.height - side) / 2) {
            scale(unit, unit, pivot = Offset.Zero) {
                drawPath(HorsePath, horseColor)
                BrandMarkDots.forEach { drawCircle(dotsColor, DOT_RADIUS, it) }
            }
        }
    }
}

/** Standardvärden för [BrandMark]. */
object BrandMarkDefaults {
    /** Standardstorleken, profilens minsta för märket; anroparens storlek i `modifier` går före. */
    val Size: Dp = 16.dp
}

private const val VIEWPORT = 64f
private const val DOT_RADIUS = 3.6f

/** Hästen, punkterna i `kodhast-marke.svg`. */
private val HorsePoints = floatArrayOf(
    9f, 59f, 9f, 34f, 7f, 28f, 12f, 22f, 29f, 22f, 34f, 11f, 36f, 2f, 41f, 9f, 44f, 9f, 56f, 16f, 59f, 23f,
    53f, 28f, 47f, 24f, 50f, 36f, 50f, 59f, 37f, 59f, 37f, 45f, 34f, 42f, 25f, 42f, 22f, 45f, 22f, 59f
)

private val HorsePath = Path().apply {
    moveTo(HorsePoints[0], HorsePoints[1])
    for (i in 2 until HorsePoints.size step 2) lineTo(HorsePoints[i], HorsePoints[i + 1])
    close()
}

private val BrandMarkDots = listOf(Offset(11.5f, 10f), Offset(22.5f, 10f))
