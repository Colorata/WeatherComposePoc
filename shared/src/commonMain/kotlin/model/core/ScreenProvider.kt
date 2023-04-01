package model.core

import androidx.compose.runtime.*
import di.AppState
import di.provideCoreElements
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn

data class ScreenProvider<EVENT, STATE> internal constructor(
    val providers: List<ProvidedValue<out Any>>,
    private val storageScope: CoroutineScope,
    private val initialState: STATE,
    private val viewModel: @Composable (events: EventFlow<EVENT>) -> STATE,
) {
    val events: EventFlow<EVENT> = eventFlow()

    private var currentState by mutableStateOf(initialState)
    private val viewModelFlow by lazy {
        composableFlow(*providers.toTypedArray()) {
            currentState = viewModel(events)
            currentState
        }.shareIn(storageScope, started = SharingStarted.Lazily)
    }

    @Composable
    fun provide(): State<STATE> {
        return viewModelFlow.collectAsState(currentState)
    }

    fun dispose() {
        currentState = initialState
        events.reset()
    }
}

fun <EVENT, STATE> AppState.screenProvider(
    vararg providers: ProvidedValue<out Any>,
    initialState: STATE,
    viewModel: @Composable (events: EventFlow<EVENT>) -> STATE
): ScreenProvider<EVENT, STATE> {
    return ScreenProvider(
        providers.toList() + provideCoreElements(provideAppState = true),
        storageScope,
        initialState,
        viewModel
    )
}