package model.core

import androidx.compose.runtime.*
import di.AppState
import di.LocalAppState
import di.provideCoreElements
import kotlinx.coroutines.flow.Flow

sealed interface Pack<IN_STATE, EVENT, OUT_STATE> {
    fun provideFlow(events: EventFlow<EVENT>): Flow<OUT_STATE>
}

class StatelessPack<EVENT, STATE> internal constructor(
    private val providers: List<ProvidedValue<out Any>>,
    private val provider: @Composable (events: EventFlow<EVENT>) -> STATE
) :
    Pack<Nothing, EVENT, STATE> {

    override fun provideFlow(events: EventFlow<EVENT>): Flow<STATE> {
        return composableFlow(*providers.toTypedArray()) { provider(events) }
    }
}

class StatefulPack<IN_STATE, EVENT, OUT_STATE> internal constructor(
    private val providers: List<ProvidedValue<out Any>>,
    initialState: IN_STATE,
    private val provider: @Composable (state: MutableState<IN_STATE>, events: EventFlow<EVENT>) -> OUT_STATE
) : Pack<IN_STATE, EVENT, OUT_STATE> {

    private val state = mutableStateOf(initialState)

    override fun provideFlow(events: EventFlow<EVENT>): Flow<OUT_STATE> {
        return composableFlow(*providers.toTypedArray()) { provider(state, events) }
    }
}

fun <EVENT, STATE> AppState.statelessPack(
    vararg providers: ProvidedValue<out Any>,
    provideAppState: Boolean = false,
    provider: @Composable (events: EventFlow<EVENT>) -> STATE
): StatelessPack<EVENT, STATE> {
    return StatelessPack(
        providers.toList() + provideCoreElements(provideAppState),
        provider
    )
}

fun <IN_STATE, EVENT, OUT_STATE> AppState.statefulPack(
    vararg providers: ProvidedValue<out Any>,
    initialState: IN_STATE,
    provideAppState: Boolean = false,
    provider: @Composable (state: MutableState<IN_STATE>, events: EventFlow<EVENT>) -> OUT_STATE
): StatefulPack<IN_STATE, EVENT, OUT_STATE> {
    return StatefulPack(
        providers.toList() + provideCoreElements(provideAppState),
        initialState,
        provider
    )
}