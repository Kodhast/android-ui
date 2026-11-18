package se.kodhast.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

/**
 * Kodhästs två prickar: runda, vågrätt, med en prickbredd emellan, centrerade i ytan som
 * [modifier] ger. Med [bouncing] hoppar de i tur och ordning (period 900 ms, den andra 150 ms
 * efter, höjd 70 % av diametern) – stilla när systemets animeringar är avstängda. Dekor; den som
 * anropar sätter semantiken.
 */
@Composable
internal fun BrandDots(color: Color, modifier: Modifier = Modifier, bouncing: Boolean = false) {
    if (bouncing && !rememberReducedMotion()) {
        val phase = rememberInfiniteTransition(label = "brand-dots").animateFloat(
            initialValue = 0f,
            targetValue = PERIOD_MS,
            animationSpec = infiniteRepeatable(tween(PERIOD_MS.toInt(), easing = LinearEasing)),
            label = "brand-dots-phase"
        )
        DotsCanvas(color, bouncing = true, modifier) { phase.value }
    } else {
        DotsCanvas(color, bouncing, modifier) { null }
    }
}

@Composable
private fun DotsCanvas(color: Color, bouncing: Boolean, modifier: Modifier = Modifier, phase: () -> Float?) {
    Canvas(modifier) {
        val diameter = min(size.width / DOT_SPANS, size.height / (if (bouncing) 1f + JUMP else 1f))
        val radius = diameter / 2
        val left = (size.width - diameter * DOT_SPANS) / 2
        val rest = if (bouncing) (size.height + diameter * (1f + JUMP)) / 2 - radius else size.height / 2
        val now = phase()
        repeat(2) { i ->
            val lift = now?.let { jump(it - i * DELAY_MS) } ?: 0f
            drawCircle(color, radius, Offset(left + radius + i * 2 * diameter, rest - lift * JUMP * diameter))
        }
    }
}

/** 0–1: hur högt en prick är vid [t] ms in i perioden; uppe och nere på första halvan, stilla på andra. */
private fun jump(t: Float): Float {
    val local = ((t % PERIOD_MS) + PERIOD_MS) % PERIOD_MS
    return if (local < AIR_MS) sin(PI.toFloat() * local / AIR_MS) else 0f
}

private const val PERIOD_MS = 900f
private const val DELAY_MS = 150f
private const val AIR_MS = PERIOD_MS / 2
private const val JUMP = 0.7f

/** Två prickar och en prickbredd emellan. */
private const val DOT_SPANS = 3f
