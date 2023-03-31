package model.core

import androidx.compose.runtime.*

fun <T> localMutableStateOf(factory: () -> MutableState<T>) = compositionLocalOf { factory() }
