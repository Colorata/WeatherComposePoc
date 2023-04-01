package model.core

import androidx.compose.runtime.*
import di.AppState
import di.LocalAppState
import di.provideCoreElements
import di.providedCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

sealed interface Pack<IN_STATE, EVENT, OUT_STATE> {
    @Composable
    fun provide(events: EventFlow<EVENT>): OUT_STATE

    fun provideFlow(events: EventFlow<EVENT>): Flow<OUT_STATE>
}

class StatelessPack<EVENT, STATE>
internal constructor(
    private val providers: List<ProvidedValue<out Any>>,
    private val storageScope: CoroutineScope,
    private val provider: @Composable (events: EventFlow<EVENT>) -> STATE
) :
    Pack<Nothing, EVENT, STATE> {

    @Composable
    override fun provide(events: EventFlow<EVENT>): STATE {
        return provider(events)
    }

    override fun provideFlow(events: EventFlow<EVENT>): Flow<STATE> {
        return composableFlow(*providers.toTypedArray()) { provide(events) }
    }
}

class StatefulPack<IN_STATE, EVENT, OUT_STATE> internal constructor(
    private val providers: List<ProvidedValue<out Any>>,
    initialState: IN_STATE,
    private val storageScope: CoroutineScope,
    private val provider: @Composable (state: MutableState<IN_STATE>, events: EventFlow<EVENT>) -> OUT_STATE
) : Pack<IN_STATE, EVENT, OUT_STATE> {
    private val state = mutableStateOf(initialState)

    @Composable
    override fun provide(events: EventFlow<EVENT>): OUT_STATE {
        return provider(state, events)
    }

    override fun provideFlow(events: EventFlow<EVENT>): Flow<OUT_STATE> {
        return composableFlow(*providers.toTypedArray()) { provide(events) }
    }
}

fun <EVENT, STATE> AppState.statelessPack(
    vararg providers: ProvidedValue<out Any>,
    provideAppState: Boolean = false,
    provider: @Composable (events: EventFlow<EVENT>) -> STATE
): StatelessPack<EVENT, STATE> {
    return StatelessPack(
        providers.toList() + provideCoreElements(provideAppState),
        storageScope,
        provider
    )
}

fun <IN_STATE, EVENT, OUT_STATE> AppState.statefulPack(
    vararg providers: ProvidedValue<out Any>,
    initialState: IN_STATE,
    provideAppState: Boolean = false,
    provider: @Composable (state: MutableState<IN_STATE>, events: EventFlow<EVENT>) -> OUT_STATE
): StatefulPack<IN_STATE, EVENT, OUT_STATE> {
    val appState = if (provideAppState) listOf(LocalAppState provides this) else listOf()
    return StatefulPack(
        providers.toList() + provideCoreElements(provideAppState),
        initialState,
        storageScope,
        provider
    )
}