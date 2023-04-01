package model.core

import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.CoroutineScope


val LocalSharedCoroutineScope = compositionLocalOf<CoroutineScope> { error("SharedCoroutineScope is not provided") }