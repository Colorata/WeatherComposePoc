package model.core

import androidx.compose.runtime.*
import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

@OptIn(InternalComposeApi::class)
fun <T> composableFlow(vararg providers: ProvidedValue<out Any>, content: @Composable () -> T): Flow<T> {
    return moleculeFlow(RecompositionClock.Immediate) {
        currentComposer.startProviders(providers)
        val result = content()
        currentComposer.endProviders()
        result
    }
}

fun composableCoroutineScope(): CoroutineScope {
    return CoroutineScope(Dispatchers.Main + NoMonotonicFrameClock)
}

private object NoMonotonicFrameClock : MonotonicFrameClock {
    override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
        return onFrame(0)
    }
}