package di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.mutableStateOf
import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.serialization.json.Json
import model.LocalWeatherProviderState
import model.WeatherProviderState
import model.core.LocalLogger
import model.core.Logger
import model.core.LoggerImpl
import model.net.LocalJson
import model.net.LocalNetClient
import model.net.NetClient

class Storage internal constructor() {
    private val coroutineStorage = CoroutineScope(Dispatchers.Main + NoMonotonicFrameClock)

    fun saveableFlow(content: @Composable () -> Unit) {
        moleculeFlow(RecompositionClock.Immediate, content).shareIn(
            coroutineStorage,
            started = SharingStarted.WhileSubscribed()
        )
    }

    private val logger: Logger = LoggerImpl()
    private val netClient: NetClient = NetClient
    private val json: Json = Json { ignoreUnknownKeys = true }

    private val weatherProviderState = mutableStateOf(WeatherProviderState(listOf(), logger))

    val providers = listOf(
        LocalWeatherProviderState provides weatherProviderState,
        LocalLogger provides logger,
        LocalNetClient provides netClient,
        LocalJson provides json
    )
}

fun storage() = storage

private val storage = Storage()

private object NoMonotonicFrameClock : MonotonicFrameClock {
    override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
        return onFrame(0)
    }
}