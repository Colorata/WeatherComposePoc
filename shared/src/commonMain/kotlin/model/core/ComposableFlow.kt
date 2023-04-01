package model.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.currentComposer
import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
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