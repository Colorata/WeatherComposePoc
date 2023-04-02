package model.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map

data class Event<T> internal constructor(
    val value: T,
    private val key: Boolean
)

interface EventFlow<T> : Flow<Event<T>> {
    fun emit(value: T)

    fun reset()
}

@OptIn(ExperimentalCoroutinesApi::class)
private class EventFlowImpl<T>: EventFlow<T> {

    private val _flow =
        MutableSharedFlow<T>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private var key: Boolean = false

    override suspend fun collect(collector: FlowCollector<Event<T>>) {
        _flow.map {
            key = !key
            Event(it, key)
        }.collect(collector)
    }

    override fun emit(value: T) {
        _flow.tryEmit(value)
    }

    override fun reset() {
        _flow.resetReplayCache()
    }
}

fun <T> eventFlow(): EventFlow<T> = EventFlowImpl()

fun <T> eventFlowOf(vararg values: T): EventFlow<T> {
    return eventFlow<T>().apply {
        values.forEach {
            emit(it)
        }
    }
}
@Composable
fun <T> rememberEventFlow() = remember { eventFlow<T>() }
