package model.core

import androidx.compose.runtime.*
import di.AppState
import di.provideCoreElements
import kotlinx.coroutines.flow.Flow

sealed interface Provider<IN_STATE, EVENT, OUT_STATE> {
    fun provideFlow(events: EventFlow<EVENT>): Flow<OUT_STATE>
}

fun <IN_STATE, EVENT, OUT_STATE> Provider<IN_STATE, EVENT, OUT_STATE>.provideFlowFor(vararg events: EVENT) =
    provideFlow(eventFlowOf(*events))

class StatelessProvider<EVENT, STATE> internal constructor(
    private val providers: List<ProvidedValue<out Any>>,
    private val provider: @Composable (events: EventFlow<EVENT>) -> STATE
) : Provider<Nothing, EVENT, STATE> {

    override fun provideFlow(events: EventFlow<EVENT>): Flow<STATE> {
        return composableFlow(*providers.toTypedArray()) {
            provider(events)
        }
    }
}

class StatefulProvider<IN_STATE, EVENT, OUT_STATE> internal constructor(
    private val providers: List<ProvidedValue<out Any>>,
    initialState: IN_STATE,
    private val provider: @Composable (state: MutableState<IN_STATE>, events: EventFlow<EVENT>) -> OUT_STATE
) : Provider<IN_STATE, EVENT, OUT_STATE> {

    private val state = mutableStateOf(initialState)

    override fun provideFlow(events: EventFlow<EVENT>): Flow<OUT_STATE> {
        return composableFlow(*providers.toTypedArray()) { provider(state, events) }
    }
}

fun <EVENT, STATE> AppState.statelessProvider(
    vararg providers: ProvidedValue<out Any>,
    provideAppState: Boolean = false,
    provider: @Composable (events: EventFlow<EVENT>) -> STATE
): StatelessProvider<EVENT, STATE> {
    return StatelessProvider(
        providers.toList() + provideCoreElements(provideAppState),
        provider
    )
}

fun <IN_STATE, EVENT, OUT_STATE> AppState.statefulProvider(
    vararg providers: ProvidedValue<out Any>,
    initialState: IN_STATE,
    provideAppState: Boolean = false,
    provider: @Composable (state: MutableState<IN_STATE>, events: EventFlow<EVENT>) -> OUT_STATE
): StatefulProvider<IN_STATE, EVENT, OUT_STATE> {
    return StatefulProvider(
        providers.toList() + provideCoreElements(provideAppState),
        initialState,
        provider
    )
}