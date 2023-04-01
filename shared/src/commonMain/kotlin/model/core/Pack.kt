package model.core

import androidx.compose.runtime.*
import di.AppState

sealed interface Pack<IN_STATE, EVENT, OUT_STATE> {
    @Composable
    fun provide(events: EventFlow<EVENT>): OUT_STATE
}

class StatelessPack<EVENT, STATE>(val provider: @Composable (events: EventFlow<EVENT>) -> STATE) :
    Pack<Nothing, EVENT, STATE> {

    @Composable
    override fun provide(events: EventFlow<EVENT>): STATE {
        return provider(events)
    }
}

class StatefulPack<IN_STATE, EVENT, OUT_STATE>(
    initialState: IN_STATE,
    private val provider: @Composable (state: MutableState<IN_STATE>, events: EventFlow<EVENT>) -> OUT_STATE
) : Pack<IN_STATE, EVENT, OUT_STATE> {
    private val state = mutableStateOf(initialState)

    @Composable
    override fun provide(events: EventFlow<EVENT>): OUT_STATE {
        return provider(state, events)
    }
}