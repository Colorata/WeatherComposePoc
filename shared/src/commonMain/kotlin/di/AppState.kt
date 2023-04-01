package di

import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import model.*
import model.core.*
import model.net.NetClient
import model.net.NetClientEvent
import viewmodel.WeatherScreenEvent
import viewmodel.WeatherScreenState
import viewmodel.WeatherViewModel

interface AppState {
    val storageScope: CoroutineScope
    val logger: Logger
    val netClient: StatelessPack<NetClientEvent, Result<ByteArray>>
    val json: Json

    val weatherProviderPack: StatefulPack<WeatherProviderState, WeatherProviderEvent, Result<WeatherData>>
    val weatherScreenProvider: ScreenProvider<WeatherScreenEvent, WeatherScreenState>
}


class AppStateImpl : AppState {
    override val storageScope: CoroutineScope = CoroutineScope(Dispatchers.Main + NoMonotonicFrameClock)
    override val logger: Logger by lazy { LoggerImpl() }
    override val netClient: StatelessPack<NetClientEvent, Result<ByteArray>> = statelessPack(
        provideAppState = true
    ) { events ->
        NetClient(events)
    }
    override val json: Json by lazy { Json { ignoreUnknownKeys = true } }

    override val weatherProviderPack =
        statefulPack(
            initialState = WeatherProviderState(listOf()),
            provideAppState = true
        ) { state, events ->
            OpenWeatherMapProvider(state, events)
        }

    override val weatherScreenProvider: ScreenProvider<WeatherScreenEvent, WeatherScreenState> =
        screenProvider(
            initialState = WeatherScreenState("", Result.Loading())
        ) { events ->
            WeatherViewModel(events)
        }
}

fun AppState.providedCoroutineScope(): ProvidedValue<CoroutineScope> {
    return LocalSharedCoroutineScope provides storageScope
}

fun AppState.provideCoreElements(provideAppState: Boolean = false): List<ProvidedValue<out Any>> {
    val appState = if (provideAppState) listOf(LocalAppState provides this) else listOf()
    return listOf(providedCoroutineScope()) + appState
}

private object NoMonotonicFrameClock : MonotonicFrameClock {
    override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
        return onFrame(0)
    }
}

val LocalAppState = compositionLocalOf<AppState> { error("AppState is not provided") }

val LocalSharedCoroutineScope = compositionLocalOf<CoroutineScope> { error("SharedCoroutineScope is not provided") }