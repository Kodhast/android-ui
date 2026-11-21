package se.kodhast.ui.components

import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import se.kodhast.ui.theme.AppBlur
import se.kodhast.ui.theme.AppTheme

/** Om enheten har oskärpa mellan fönster påslagen; `null` = fråga enheten. För proven. */
internal val LocalCrossWindowBlur = staticCompositionLocalOf<Boolean?> { null }

/** Temats oskärpa ([AppTheme.blur]) om enheten kan visa den, annars `null`. */
@Composable
internal fun activeBlur(): AppBlur? {
    val blur = AppTheme.blur ?: return null
    val forced = LocalCrossWindowBlur.current
    val enabled = forced ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) crossWindowBlurEnabled() else false
    return blur.takeIf { enabled }
}

/** Följer enhetens inställning: oskärpan slås av i energisparläget och när den stängs av i utvecklarinställningarna. */
@RequiresApi(Build.VERSION_CODES.S)
@Composable
private fun crossWindowBlurEnabled(): Boolean {
    val context = LocalContext.current
    val windowManager = remember(context) { context.getSystemService(WindowManager::class.java) }
    var enabled by remember(windowManager) { mutableStateOf(windowManager.isCrossWindowBlurEnabled) }
    DisposableEffect(windowManager) {
        val listener = java.util.function.Consumer<Boolean> { enabled = it }
        windowManager.addCrossWindowBlurEnabledListener(listener)
        onDispose { windowManager.removeCrossWindowBlurEnabledListener(listener) }
    }
    return enabled
}

/**
 * Egenskaperna för bibliotekets dialoger: med [activeBlur] suddas innehållet bakom och slöjan blir
 * temats; utan oskärpa samma som `DialogProperties(…)`.
 */
@Composable
internal fun blurDialogProperties(
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    usePlatformDefaultWidth: Boolean = true
): DialogProperties = dialogProperties(activeBlur(), dismissOnBackPress, dismissOnClickOutside, usePlatformDefaultWidth)

internal fun dialogProperties(
    blur: AppBlur?,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    usePlatformDefaultWidth: Boolean = true
): DialogProperties = if (blur == null) {
    DialogProperties(dismissOnBackPress = dismissOnBackPress, dismissOnClickOutside = dismissOnClickOutside, usePlatformDefaultWidth = usePlatformDefaultWidth)
} else {
    DialogProperties(
        dismissOnBackPress = dismissOnBackPress,
        dismissOnClickOutside = dismissOnClickOutside,
        usePlatformDefaultWidth = usePlatformDefaultWidth,
        blurBehindRadius = blur.behindRadius,
        scrimAlpha = blur.scrimAlpha
    )
}

/**
 * Arkets slöja: med [activeBlur] temats svagare slöja, annars [default] (Material 3:s).
 * Anropas i arkets innehåll, som också sätter oskärpan på arkets fönster ([BlurSheetWindow]).
 */
internal fun sheetScrim(blur: AppBlur?, default: Color): Color = blur?.let { default.copy(alpha = it.scrimAlpha) } ?: default

/** Suddar innehållet bakom arket: arket är ett eget fönster, som dialogerna. Gör inget utan [blur] eller före Android 12. */
@Composable
internal fun BlurSheetWindow(blur: AppBlur?) {
    if (blur == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    val view = LocalView.current
    val radius = with(LocalDensity.current) { blur.behindRadius.roundToPx() }
    DisposableEffect(view, radius) {
        view.dialogWindow()?.let { window -> setBlurBehind(window, radius) }
        onDispose {}
    }
}

@RequiresApi(Build.VERSION_CODES.S)
private fun setBlurBehind(window: Window, radius: Int) {
    window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
    window.attributes = window.attributes.also { it.blurBehindRadius = radius }
}

private fun View.dialogWindow(): Window? {
    var current: Any? = this
    while (current is View) {
        if (current is DialogWindowProvider) return current.window
        current = current.parent
    }
    return null
}
