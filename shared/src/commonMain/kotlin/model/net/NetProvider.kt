package model.net

import androidx.compose.runtime.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import model.core.*

typealias NetProvider = StatelessPack<NetProviderEvent, Result<ByteArray>>

sealed class NetProviderEvent {
    class Get(val url: String) : NetProviderEvent()
}

@Composable
inline fun NetProvider(events: EventFlow<NetProviderEvent>): Result<ByteArray> {
    // TODO: Move HttpClient to storage
    val client = remember { HttpClient(CIO) }
    val sharedScope = LocalSharedCoroutineScope.current

    val eventsCollected by events.collectAsState(null)

    var result by remember { mutableStateOf<Result<ByteArray>>(loadingResult()) }
    FlowLaunchedEffect(eventsCollected) {
        val event = eventsCollected
        if (event != null && event.value is NetProviderEvent.Get) {
            result = loadingResult()
            sharedScope.launch {
                val netResult = client.get(event.value.url)
                result = if (netResult.status.isSuccess()) successResult(netResult.body())
                else failureResult(Exception(netResult.status.description))
            }
        }
    }
    return result
}

