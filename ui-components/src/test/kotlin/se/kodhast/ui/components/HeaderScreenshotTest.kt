package se.kodhast.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.zIndex
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kodhast.ui.components.testing.FakePhoto
import se.kodhast.ui.components.testing.PHOTO_COLOR
import se.kodhast.ui.components.testing.SampleCard
import se.kodhast.ui.components.testing.SampleRow
import se.kodhast.ui.components.testing.SampleSheet
import se.kodhast.ui.components.testing.captureLightAndDark
import se.kodhast.ui.theme.Spacing

/** Skärmdumpar (ljust + mörkt) av Kodhäst-sidhuvudet (android-ui #42) med testtemat. */
@RunWith(RobolectricTestRunner::class)
class HeaderScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Composable
    private fun Dots(content: @Composable () -> Unit) = CompositionLocalProvider(LocalAppBrandStyle provides AppBrandStyle(headerDots = true), content = content)

    /** Sidhuvudet med en bit innehåll under, så att skuggan syns. */
    @Composable
    private fun WithContent(header: @Composable () -> Unit) = Column(Modifier.fillMaxWidth()) {
        // Ovanpå innehållet, som i Scaffold – annars täcker innehållet skuggan.
        Box(Modifier.zIndex(1f)) { header() }
        SampleSheet { SampleCard { SampleRow("Morgonmedicin", subtitle = "Levaxin 50 µg") } }
    }

    @Test
    fun `AppHeader - vila`() = captureLightAndDark("AppHeader_vila", depth = true) {
        Dots { WithContent { SampleHeader(collapsed = 0f) } }
    }

    @Test
    fun `AppHeader - vila med version`() = captureLightAndDark("AppHeader_vila_version", depth = true) {
        Dots { WithContent { SampleHeader(collapsed = 0f, version = "4.19.1") } }
    }

    @Test
    fun `AppHeader - halvvägs med version`() = captureLightAndDark("AppHeader_halvvags_version", depth = true) {
        Dots { WithContent { SampleHeader(collapsed = 0.3f, version = "4.19.1") } }
    }

    @Test
    fun `AppHeader - halvvägs`() = captureLightAndDark("AppHeader_halvvags", depth = true) {
        Dots { WithContent { SampleHeader(collapsed = 0.5f) } }
    }

    @Test
    fun `AppHeader - ihopfälld`() = captureLightAndDark("AppHeader_ihopfalld", depth = true) {
        Dots { WithContent { SampleHeader(collapsed = 1f, overlapped = true) } }
    }

    @Test
    fun `AppHeader - ihopfälld utan djup`() = captureLightAndDark("AppHeader_utan_djup") {
        Dots { WithContent { SampleHeader(collapsed = 1f, overlapped = true) } }
    }

    @Test
    fun `AppHeader - synkar`() {
        // Prickarna hoppar för evigt: klockan står still och går en fast tid före bilden, mitt i första prickens hopp.
        rule.mainClock.autoAdvance = false
        rule.captureLightAndDark("AppHeader_synkar", settle = {
            mainClock.advanceTimeBy(MID_JUMP_MS)
            waitForIdle()
        }, depth = true) {
            Dots { WithContent { SampleHeader(collapsed = 0f, syncing = true) } }
        }
    }

    @Test
    fun `AppHeader - långt fliknamn`() = captureLightAndDark("AppHeader_langt_namn", depth = true) {
        Column {
            SampleHeader(collapsed = 0f, title = "Läkemedel och kosttillskott hemma")
            SampleHeader(collapsed = 1f, overlapped = true, title = "Läkemedel och kosttillskott hemma")
        }
    }

    @Test
    fun `AccountAvatar - lägen`() = captureLightAndDark("AccountAvatar_lagen", depth = true) {
        SampleSheet {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.l), verticalAlignment = Alignment.CenterVertically) {
                AccountAvatar("Anna Berg", onClick = {}, online = true)
                AccountAvatar("Anna Berg", onClick = {}, online = false)
                AccountAvatar(null, onClick = {})
            }
        }
    }

    @Test
    fun `AccountAvatar - foto`() = captureLightAndDark("AccountAvatar_foto", depth = true) {
        SampleSheet {
            FakePhoto(PHOTO_COLOR) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.l), verticalAlignment = Alignment.CenterVertically) {
                    AccountAvatar("Anna Berg", onClick = {}, online = true, photoUrl = "https://example.com/foto.jpg")
                    AccountAvatar("Anna Berg", onClick = {}, online = false, photoUrl = "https://example.com/foto.jpg")
                }
            }
        }
    }

    private companion object {
        /** Mitt i första prickens hopp (perioden är 900 ms, hoppet första halvan). */
        const val MID_JUMP_MS = 225L
    }
}
