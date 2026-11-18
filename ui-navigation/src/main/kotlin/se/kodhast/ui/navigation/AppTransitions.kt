package se.kodhast.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset

/**
 * Skärmbyten – samma rörelse överallt, definierad bara här: den nya skärmen glider in en bit och
 * tonas fram med fjädring; tillbaka (och predictive back) är samma rörelse baklänges.
 * [AppNavHost] använder dem; en app med en egen `NavDisplay` skriver `transitionSpec = { AppTransitions.forward() }`.
 */
object AppTransitions {
    private val slide = spring<IntOffset>(dampingRatio = 0.9f, stiffness = 500f)
    private val fade = spring<Float>(stiffness = 700f)

    fun forward(): ContentTransform = (slideInHorizontally(slide) { it / SHIFT } + fadeIn(fade)) togetherWith
        (slideOutHorizontally(slide) { -it / SHIFT } + fadeOut(fade))

    fun back(): ContentTransform = (slideInHorizontally(slide) { -it / SHIFT } + fadeIn(fade)) togetherWith
        (slideOutHorizontally(slide) { it / SHIFT } + fadeOut(fade))

    /** Hur stor del av bredden skärmen glider: en fjärdedel. */
    private const val SHIFT = 4
}
