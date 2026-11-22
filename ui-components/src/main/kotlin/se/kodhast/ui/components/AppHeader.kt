@file:OptIn(ExperimentalMaterial3Api::class)

package se.kodhast.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.lerp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import se.kodhast.ui.theme.AppTheme
import se.kodhast.ui.theme.Spacing

/**
 * Appens ikon och namn överst i [AppHeader]. [icon] är en vektor eller bitmapp (inte en adaptiv
 * startikon). Sätts en gång i appens tema med [LocalAppIdentity].
 *
 * [version] (standard `null` = ingen) är appens versionsnummer, t.ex. `BuildConfig.VERSION_NAME`: det står
 * litet och dämpat efter namnet, tonar bort med det när raden krymper och utelämnas helt om det inte ryms
 * bredvid namnet. Det läses inte av TalkBack (som namnet); i Om står det kvar.
 */
@Immutable
data class AppIdentity(@param:DrawableRes val icon: Int, val name: String, val version: String? = null)

/**
 * Appens identitet för [AppHeader]; standard `null`. Ramarna (`EntityListScreen`, `EntityDetailScreen`)
 * ritar sidhuvudet i flikläget bara när appen satt den – utan den är toppraden som förut.
 */
val LocalAppIdentity = staticCompositionLocalOf<AppIdentity?> { null }

/**
 * Kodhäst-sidhuvudet på en flik (toppnivåskärm). I vila (högst upp, 144 dp) står appens ikon och namn
 * överst med [account] längst till höger, och under dem [title] stort och [subtitle] – i sidans färg,
 * utan skugga. När innehållet rullas krymper raden med fingret till 64 dp: samma rubrik krymper och glider
 * upp bredvid ikonen, appnamnet (första 40 %) och undertiteln (första 33 %) tonar bort. Den smala raden
 * ligger kvar och fälls ut först högst upp ([scrollBehavior] ska vara `exitUntilCollapsedScrollBehavior`).
 * När raden är ihopfälld och innehåll ligger under får den kortens färg och skuggan
 * (`AppDepth.headerShadow`; utan djup en hårfin linje i `outlineVariant`). Rak nederkant.
 *
 * Med `AppBrandStyle.headerDots` står Kodhästs två prickar före rubriken; de hoppar medan [syncing] är
 * sant (stilla när systemets animeringar är avstängda) och TalkBack säger "Ändringar väntar på servern".
 * Till höger står [status], [actions] och [account] i den ordningen. Rubriken är en rubrik för TalkBack
 * och läses en gång; ikonen och appnamnet läses inte. [titleModifier] läggs på rubriken (se [AppTopBar]).
 *
 * @param identity appens ikon och namn; `null` = ingen ikon och inget namn, rubriken står vid kanten.
 */
@Composable
fun AppHeader(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
    titleModifier: Modifier = Modifier,
    subtitle: String? = null,
    syncing: Boolean = LocalTopBarSyncing.current,
    identity: AppIdentity? = LocalAppIdentity.current,
    update: AppUpdateNotice? = LocalAppUpdate.current,
    status: @Composable RowScope.() -> Unit = LocalTopBarStatus.current,
    account: @Composable () -> Unit = LocalTopBarAccount.current,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val state = scrollBehavior.state
    val limit = with(LocalDensity.current) { (COLLAPSED - EXPANDED).toPx() }
    SideEffect { if (state.heightOffsetLimit != limit) state.heightOffsetLimit = limit }

    val raised by remember(state) { derivedStateOf { state.isRaised() } }
    val lift = animateFloatAsState(if (raised) 1f else 0f, MaterialTheme.motionScheme.defaultEffectsSpec(), label = "sidhuvudets skugga")
    val colors = MaterialTheme.colorScheme
    val card = AppTheme.extendedColors.card
    val depth = AppTheme.depth
    val shadow = depth?.let { it.headerShadow ?: it.cardShadow }
    val surface = Modifier
        .then(
            if (shadow == null) {
                Modifier
            } else {
                Modifier.dropShadow(RectangleShape) {
                    radius = SHADOW_RADIUS.toPx()
                    offset = Offset(0f, SHADOW_OFFSET.toPx())
                    color = shadow
                    alpha = lift.value
                }
            }
        )
        .drawBehind { drawRect(lerp(colors.background, card, lift.value)) }
        .then(
            if (shadow != null) {
                Modifier
            } else {
                Modifier.drawWithContent {
                    drawContent()
                    val line = HAIRLINE.toPx()
                    drawRect(colors.outlineVariant, Offset(0f, size.height - line), size.copy(height = line), alpha = lift.value)
                }
            }
        )
        .testTag(APP_HEADER_TAG)

    val icon: @Composable () -> Unit = {
        identity?.let { Image(painterResource(it.icon), contentDescription = null, modifier = Modifier.clearAndSetSemantics { }) }
    }
    val name: @Composable () -> Unit = {
        identity?.let {
            val fade = Modifier.graphicsLayer { alpha = fadeOut(state.collapsedFraction, NAME_FADE) }
            if (update == null) {
                NameWithVersion(it.name, it.version, fade.clearAndSetSemantics { })
            } else {
                // Pricken säger att en ny version finns; namnet och versionen öppnar Om-vyn så länge de syns.
                val shown by remember(state) { derivedStateOf { state.collapsedFraction < NAME_FADE } }
                val label = stringResource(R.string.update_available_format, update.update.version)
                NameWithVersion(
                    it.name,
                    it.version,
                    fade
                        .clickable(enabled = shown, onClick = update.onOpen)
                        .clearAndSetSemantics {
                            contentDescription = label
                            role = Role.Button
                            testTag = APP_HEADER_UPDATE_TAG
                            if (shown) {
                                onClick {
                                    update.onOpen()
                                    true
                                }
                            } else {
                                disabled()
                            }
                        },
                    updateDot = true
                )
            }
        }
    }
    val trailing: @Composable () -> Unit = {
        CompositionLocalProvider(LocalContentColor provides colors.onSurface, LocalAvatarRing provides lerp(colors.background, card, lift.value)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                status()
                actions()
                account()
            }
        }
    }
    val heading: @Composable () -> Unit = {
        HeaderTitle(title, state, syncing, titleModifier)
    }
    val under: @Composable () -> Unit = {
        subtitle?.let {
            Text(
                it,
                style = AppTheme.typography.itemSubtitle,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.graphicsLayer { alpha = fadeOut(state.collapsedFraction, SUBTITLE_FADE) }
            )
        }
    }

    Layout(
        contents = listOf(icon, name, trailing, heading, under),
        modifier = modifier.fillMaxWidth().then(surface).windowInsetsPadding(TopAppBarDefaults.windowInsets).clipToBounds()
    ) { (icons, names, trailings, titles, subtitles), constraints ->
        val f = state.collapsedFraction
        val width = constraints.maxWidth
        val expanded = EXPANDED.roundToPx()
        val row = COLLAPSED.roundToPx()
        val height = (expanded + state.heightOffset).roundToInt().coerceIn(row, expanded)
        val start = Spacing.l.roundToPx()
        val end = Spacing.xs.roundToPx()
        val gap = Spacing.m.roundToPx()

        val loose = Constraints(maxWidth = width)
        val trailing = trailings.firstOrNull()?.measure(loose)
        val trailingWidth = trailing?.width ?: 0
        val iconSize = lerp(APP_ICON.roundToPx(), COLLAPSED_APP_ICON.roundToPx(), f)
        val icon = icons.firstOrNull()?.measure(Constraints.fixed(iconSize, iconSize))
        val beside = if (icon == null) start else start + COLLAPSED_APP_ICON.roundToPx() + gap
        val endClearance = if (trailingWidth > 0) trailingWidth + end + Spacing.s.roundToPx() else start
        val nameX = start + (icon?.width ?: 0) + if (icon == null) 0 else gap
        val name = names.firstOrNull()?.measure(Constraints(maxWidth = (width - nameX - endClearance).coerceAtLeast(0)))
        val restWidth = (width - 2 * start).coerceAtLeast(0)
        // I sidled hinner rubriken fram till ikonen på första halvan, så att den aldrig glider över ikonen.
        val sideways = (f / SIDEWAYS_BY).coerceAtMost(1f)
        val titleWidth = lerp(restWidth, (width - beside - endClearance).coerceAtLeast(0), sideways)
        val title = titles.first().measure(Constraints(maxWidth = titleWidth))
        val subtitle = subtitles.firstOrNull()?.measure(Constraints(maxWidth = restWidth))
        val titleY = lerp(row, (row - title.height) / 2, f)

        layout(width, height) {
            icon?.place(start, (row - icon.height) / 2)
            name?.place(nameX, (row - name.height) / 2)
            trailing?.place(width - trailingWidth - end, (row - trailing.height) / 2)
            title.place(lerp(start, beside, sideways), titleY)
            subtitle?.place(start, titleY + title.height)
        }
    }
}

/**
 * Appnamnet med [version] efter sig, på namnets baslinje i etikettstilen (`caption`) och dämpad färg.
 * Ryms inte versionen bredvid ett helt namn ritas den inte alls; namnet kapas aldrig för den. Med
 * [updateDot] står en liten prick i `primary` sist (ny version finns, #125), om den ryms.
 */
@Composable
private fun NameWithVersion(name: String, version: String?, modifier: Modifier = Modifier, updateDot: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    Layout(
        content = {
            Text(name, style = AppTheme.typography.itemTitle, color = colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (version != null) {
                Text(version, style = AppTheme.typography.caption, color = colors.onSurfaceVariant, maxLines = 1, modifier = Modifier.testTag(APP_HEADER_VERSION_TAG))
            }
            if (updateDot) {
                Box(Modifier.size(UPDATE_DOT).background(colors.primary, AppTheme.shapes.pill).testTag(APP_HEADER_UPDATE_DOT_TAG))
            }
        },
        modifier = modifier
    ) { measurables, constraints ->
        val nameText = measurables[0].measure(Constraints(maxWidth = constraints.maxWidth))
        val versionText = if (version != null) measurables[1].measure(Constraints()) else null
        val dot = if (updateDot) measurables.last().measure(Constraints()) else null
        val gap = Spacing.xs.roundToPx()
        val fits = versionText != null && nameText.width + gap + versionText.width <= constraints.maxWidth
        val textEnd = if (fits) nameText.width + gap + versionText!!.width else nameText.width
        val dotFits = dot != null && textEnd + gap + dot.width <= constraints.maxWidth
        val width = if (dotFits) textEnd + gap + dot!!.width else textEnd
        layout(width, nameText.height) {
            nameText.placeRelative(0, 0)
            if (fits) {
                val y = nameText[FirstBaseline] - versionText!![FirstBaseline]
                versionText.placeRelative(nameText.width + gap, y)
            }
            if (dotFits) dot!!.placeRelative(textEnd + gap, (nameText.height - dot.height) / 2)
        }
    }
}

/** Rubriken: prickarna (med `AppBrandStyle.headerDots`) och fliknamnet, vars stil går från `screenTitle` till `sectionTitle`. */
@Composable
private fun HeaderTitle(title: String, state: TopAppBarState, syncing: Boolean, titleModifier: Modifier = Modifier) {
    val typography = AppTheme.typography
    val style = lerp(typography.screenTitle, typography.sectionTitle, state.collapsedFraction)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
        if (LocalAppBrandStyle.current.headerDots) HeaderDots(syncing)
        Text(
            title,
            style = style,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.semantics { heading() }.then(titleModifier)
        )
    }
}

/**
 * Kodhästs två prickar före rubriken i [AppHeader] och [AppTopBar] (med `AppBrandStyle.headerDots`): de hoppar
 * medan [syncing] är sant, och TalkBack säger då "Ändringar väntar på servern" (en live-region).
 */
@Composable
internal fun HeaderDots(syncing: Boolean) {
    val pending = stringResource(R.string.sync_pending)
    BrandDots(
        MaterialTheme.colorScheme.primary,
        Modifier
            .size(DOTS_WIDTH, DOTS_HEIGHT)
            .testTag(HEADER_DOTS_TAG)
            .clearAndSetSemantics {
                // Alltid en live-region, så att TalkBack säger texten när den dyker upp.
                liveRegion = LiveRegionMode.Polite
                if (syncing) contentDescription = pending
            },
        bouncing = syncing
    )
}

/** Ihopfälld, och innehåll ligger under – då har raden kortfärg och skugga. */
private fun TopAppBarState.isRaised(): Boolean = collapsedFraction >= RAISED_AT && overlappedFraction > 0f

/** 1 i vila, 0 när [fraction] nått [until]. */
private fun fadeOut(fraction: Float, until: Float): Float = (1f - fraction / until).coerceIn(0f, 1f)

private fun lerp(from: Int, to: Int, fraction: Float): Int = (from + (to - from) * fraction).roundToInt()

/** Höjden i vila och ihopfälld. */
internal val EXPANDED = 144.dp
internal val COLLAPSED = 64.dp

/** Appikonen i vila och ihopfälld. */
internal val APP_ICON = 40.dp
internal val COLLAPSED_APP_ICON = 32.dp

private val DOTS_WIDTH = 18.dp

/** Plats för hoppet (70 % av diametern), så att prickarna är lika stora stilla och när de hoppar. */
private val DOTS_HEIGHT = 11.dp
private val SHADOW_RADIUS = 12.dp
private val SHADOW_OFFSET = 4.dp
private val HAIRLINE = 1.dp
private val UPDATE_DOT = 6.dp

private const val NAME_FADE = 0.4f
private const val SUBTITLE_FADE = 0.33f
private const val SIDEWAYS_BY = 0.5f

/** Så nära helt ihopfälld räknas som ihopfälld – flyttal når sällan exakt 1. */
private const val RAISED_AT = 0.99f

/** Sidhuvudets yta, för tester. */
const val APP_HEADER_TAG = "app-header"

/** Versionsnumret efter appnamnet i [AppHeader] (finns bara i det omslagna trädet; namnet och versionen läses inte av TalkBack). */
const val APP_HEADER_VERSION_TAG = "app-header-version"

/** Appnamnet och versionen när en ny version finns: öppnar Om-vyn (#125). */
const val APP_HEADER_UPDATE_TAG = "app-header-update"

/** Pricken efter versionsnumret när en ny version finns (#125). */
const val APP_HEADER_UPDATE_DOT_TAG = "app-header-update-dot"

/** Prickarna före rubriken (`AppBrandStyle.headerDots`), för tester. */
internal const val HEADER_DOTS_TAG = "header-dots"
