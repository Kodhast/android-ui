package se.kodhast.ui.components.testing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalInspectionMode
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler

/** Coil hämtar inget i provet: varje bild blir en yta i [color], som i Android Studios förhandsvisning. */
@OptIn(ExperimentalCoilApi::class)
@Composable
fun FakePhoto(color: Color, content: @Composable () -> Unit) = CompositionLocalProvider(
    LocalInspectionMode provides true,
    LocalAsyncImagePreviewHandler provides AsyncImagePreviewHandler { ColorImage(color.toArgb()) },
    content = content
)

/** Fotots färg i proven – ett tydligt grönblått som inte finns i testtemat. */
val PHOTO_COLOR = Color(0xFF2E7D6B)
