package di

import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import model.*
import model.core.*
import model.net.NetClient
import viewmodel.WeatherScreenEvent
import viewmodel.WeatherScreenState
import viewmodel.WeatherViewModel

interface AppState {
    val storageScope: CoroutineScope
    val logger: Logger
    val netClient: NetClient
    val json: Json

    val weatherProviderPack: StatefulPack<WeatherProviderState, WeatherProviderEvent, Result<WeatherData>>
    val weatherScreenProvider: ScreenProvider<WeatherScreenEvent, WeatherScreenState>
}


class AppStateImpl : AppState {
    override val storageScope: CoroutineScope = CoroutineScope(Dispatchers.Main + NoMonotonicFrameClock)
    override val logger: Logger by lazy { LoggerImpl() }
    override val netClient: NetClient by lazy { NetClient }
    override val json: Json by lazy { Json { ignoreUnknownKeys = true } }

    override val weatherProviderPack by lazy {
        StatefulPack(WeatherProviderState(listOf())) { state, events ->
            WeatherProvider(state, events)
        }
    }

    override val weatherScreenProvider: ScreenProvider<WeatherScreenEvent, WeatherScreenState> =
        screenProvider(
            LocalAppState provides this,
            initialState = WeatherScreenState("", Result.Loading())
        ) { events ->
            WeatherViewModel(events)
        }
}

private object NoMonotonicFrameClock : MonotonicFrameClock {
    override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
        return onFrame(0)
    }
}

val LocalAppState = compositionLocalOf<AppState> { error("AppState is not provided") }