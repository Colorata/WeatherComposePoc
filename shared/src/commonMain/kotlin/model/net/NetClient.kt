package model.net

import androidx.compose.runtime.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import model.core.EventFlow
import model.core.LocalSharedCoroutineScope
import model.core.Result

sealed class NetClientEvent {
    class Get(val url: String) : NetClientEvent()
}

@Composable
inline fun NetClient(events: EventFlow<NetClientEvent>): Result<ByteArray> {
    // TODO: Move HttpClient to storage
    val client = remember { HttpClient(CIO) }
    val sharedScope = LocalSharedCoroutineScope.current

    val eventsCollected by events.collectAsState(null)

    var result by remember { mutableStateOf<Result<ByteArray>>(Result.Loading()) }
    LaunchedEffect(eventsCollected) {
        val event = eventsCollected
        if (event != null) {
            if (event.value is NetClientEvent.Get) {
                println(event.value)
                sharedScope.launch {
                    val netResult = client.get(event.value.url)
                    if (netResult.status.isSuccess()) result = Result.Success(netResult.body())
                }
            }
        }
    }
    return result
}

