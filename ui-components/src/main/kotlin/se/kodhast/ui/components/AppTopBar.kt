@file:OptIn(ExperimentalMaterial3Api::class)

package se.kodhast.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import se.kodhast.ui.components.R
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.Spacing
import se.kodhast.ui.theme.Tone

enum class TopBarSize {
    /** Toppnivåskärm: stor rundad rubrik som krymper vid rullning. */
    Large,

    /** Underskärm: tillbakaknapp och liten rubrik. */
    Small
}

/**
 * Statusen till höger i toppraden, före [AppTopBar]s actions – t.ex. appens synkindikator.
 * Sätts av appens navigation; utan den visas ingenting (t.ex. i tester).
 */
val LocalTopBarStatus = compositionLocalOf<@Composable RowScope.() -> Unit> { {} }

/**
 * Kontot längst till höger i [AppHeader] – t.ex. appens [AccountAvatar]. Sätts av
 * appens navigation; utan den visas ingenting (t.ex. i tester).
 */
val LocalTopBarAccount = compositionLocalOf<@Composable () -> Unit> { {} }

/**
 * Om något hämtas eller väntar på servern – prickarna före rubriken i [AppHeader] och [AppTopBar] hoppar då
 * (`AppBrandStyle.headerDots`). Appens enda laddningsindikator för bakgrundsarbete. Standard `false`.
 */
val LocalTopBarSyncing = compositionLocalOf { false }

/**
 * Tillbakapilens utseende i [AppTopBar]: knappens [variant] och [tone] (se [AppIconButton]). Standard är
 * den tonade runda plattan i Material-färgerna (ReseApoteket, Dagboken); Fonder har en genomskinlig pil,
 * `TopBarBackStyle(variant = IconButtonVariant.Plain)`.
 */
data class TopBarBackStyle(val variant: IconButtonVariant = IconButtonVariant.Tonal, val tone: Tone? = null)

/** Appens tillbakapil, satt en gång i appens tema – gäller också ramarnas topprad. */
val LocalTopBarBackStyle = staticCompositionLocalOf { TopBarBackStyle() }

/**
 * Skärmrubriken. [onBack] ger en tillbakaknapp; [actions] står till höger (t.ex. `AppMenu`).
 * [scrollBehavior] kopplas av ramarna (`EntityListScreen`, `EntityDetailScreen`).
 * Före [actions] står [status] – standard det appen satt i [LocalTopBarStatus]. Pilens utseende är
 * [backStyle] – standard det appen satt i [LocalTopBarBackStyle]. [titleModifier] läggs på rubrikens text
 * (efter rubriksemantiken), t.ex. för att en app ska kunna göra rubriken kopierbar; standard ingenting.
 * Med `AppBrandStyle.headerDots` står Kodhästs två prickar före rubriken, som i [AppHeader], och hoppar medan
 * [syncing] är sant – standard det appen satt i [LocalTopBarSyncing]. Utan `headerDots` som förut.
 */
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    titleModifier: Modifier = Modifier,
    size: TopBarSize = TopBarSize.Large,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    syncing: Boolean = LocalTopBarSyncing.current,
    status: @Composable RowScope.() -> Unit = LocalTopBarStatus.current,
    backStyle: TopBarBackStyle = LocalTopBarBackStyle.current,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val background = MaterialTheme.colorScheme.background
    val colors = TopAppBarDefaults.topAppBarColors(containerColor = background, scrolledContainerColor = background)
    val navigation: @Composable () -> Unit = {
        onBack?.let { AppIconButton(R.drawable.ic_arrow_back, stringResource(R.string.back), it, variant = backStyle.variant, tone = backStyle.tone) }
    }
    val heading = Modifier.semantics { heading() }.then(titleModifier)
    val dots = LocalAppBrandStyle.current.headerDots
    val titleRow: @Composable (style: TextStyle, maxLines: Int) -> Unit = { style, maxLines ->
        val text = @Composable { Text(title, style = style, maxLines = maxLines, overflow = TextOverflow.Ellipsis, modifier = heading) }
        if (dots) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                HeaderDots(syncing)
                text()
            }
        } else {
            text()
        }
    }
    val allActions: @Composable RowScope.() -> Unit = {
        status()
        actions()
    }
    when (size) {
        TopBarSize.Large -> LargeFlexibleTopAppBar(
            title = { titleRow(AppTheme.typography.screenTitle, 2) },
            modifier = modifier,
            subtitle = subtitle?.let { { Text(it, style = AppTheme.typography.itemSubtitle) } },
            navigationIcon = navigation,
            actions = allActions,
            colors = colors,
            scrollBehavior = scrollBehavior
        )

        TopBarSize.Small -> TopAppBar(
            title = { titleRow(AppTheme.typography.sectionTitle, 1) },
            modifier = modifier,
            navigationIcon = navigation,
            actions = allActions,
            colors = colors,
            scrollBehavior = scrollBehavior
        )
    }
}
