package ui.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale

@Composable
fun Image(
    image: ByteArray,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
) {
    val bitmap = remember(image) { image.toImageBitmap() }
    androidx.compose.foundation.Image(
        bitmap, contentDescription, modifier, alignment, contentScale, alpha
    )
}

expect fun ByteArray.toImageBitmap(): ImageBitmap