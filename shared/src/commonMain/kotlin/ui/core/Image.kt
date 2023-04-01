package ui.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.skia.Bitmap

@Composable
fun Image(
    image: ByteArray,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
) {
    val bitmap = remember(image) {
        Bitmap.makeFromImage(
            org.jetbrains.skia.Image.makeFromEncoded(
                image
            )
        )
    }.asComposeImageBitmap()
    androidx.compose.foundation.Image(
        bitmap, contentDescription, modifier, alignment, contentScale, alpha
    )
}