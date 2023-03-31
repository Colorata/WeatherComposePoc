package model.net

import androidx.compose.runtime.compositionLocalOf
import kotlinx.serialization.json.Json

val LocalJson = compositionLocalOf<Json> { error("Encoder is not provided") }
