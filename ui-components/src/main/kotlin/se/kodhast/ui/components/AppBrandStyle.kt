package se.kodhast.ui.components

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Kodhäst-delarna som en app slår på i sitt tema – sätts en gång runt appens innehåll med
 * [LocalAppBrandStyle]. Allt är avstängt som standard, så ingen app ändrar utseende av att höja
 * beroendet. Delarna ritas i appens egna färger; biblioteket har ingen Kodhäst-färg.
 *
 * - [sectionDots]: två prickar i `primary` före titeln i [SectionHeader] utan ikonruta.
 * - [dotLoading]: [AppLoading] och sektionsrubrikens laddning (`working`) visar två prickar som
 *   hoppar i stället för Material 3:s indikator; stilla när systemets animeringar är avstängda.
 * - [emptyStateMark]: [EmptyState] visar [BrandMark] i ikonrutan i stället för ikonen, bara i
 *   fullstort läge utan fel.
 * - [headerDots]: [AppHeader] visar två prickar i `primary` före rubriken; de hoppar medan ändringar
 *   väntar på servern (`syncing`). Utan dem visar appen synken via `status` som i [AppTopBar].
 * - [toolbarStyle]: bakgrunden i [AppFloatingToolbar]; standard [ToolbarStyle.Toolbar] (`extendedColors.toolbar`).
 * - [actionMenu]: hur huvudknappens meny öppnas; standard [ActionMenuStyle.Popup] (rullgardin).
 * - [actionColor]: huvudknappens färg i [AppFloatingToolbar]; standard [ActionColor.Secondary].
 */
@Immutable
data class AppBrandStyle(
    val sectionDots: Boolean = false,
    val dotLoading: Boolean = false,
    val emptyStateMark: Boolean = false,
    val headerDots: Boolean = false,
    val toolbarStyle: ToolbarStyle = ToolbarStyle.Toolbar,
    val actionMenu: ActionMenuStyle = ActionMenuStyle.Popup,
    val actionColor: ActionColor = ActionColor.Secondary
)

/** Kodhäst-delarna i innehållet; standard [AppBrandStyle] – allt avstängt. */
val LocalAppBrandStyle = staticCompositionLocalOf { AppBrandStyle() }
