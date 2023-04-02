package ui.core

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import org.jetbrains.skia.Bitmap

actual fun ByteArray.toImageBitmap(): ImageBitmap {
    return Bitmap.makeFromImage(
        org.jetbrains.skia.Image.makeFromEncoded(
            this
        )
    ).asComposeImageBitmap()
}