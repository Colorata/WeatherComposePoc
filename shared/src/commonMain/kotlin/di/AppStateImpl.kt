package di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import model.OpenWeatherMapProvider
import model.WeatherProviderState
import model.core.*
import model.net.NetProvider
import viewmodel.WeatherScreenEvent
import viewmodel.WeatherScreenState
import viewmodel.WeatherViewModel

private class AppStateImpl : AppState {
    override val storageScope: CoroutineScope = composableCoroutineScope()
    override val logger: Logger by lazy { LoggerImpl() }
    override val netProvider: NetProvider = statelessPack(
        provideAppState = true
    ) { events ->
        NetProvider(events)
    }
    override val json: Json by lazy { Json { ignoreUnknownKeys = true } }

    override val weatherProviderPack =
        statefulPack(
            initialState = WeatherProviderState(),
            provideAppState = true
        ) { state, events ->
            OpenWeatherMapProvider(state, events)
        }

    override val weatherScreenProvider: ScreenProvider<WeatherScreenEvent, WeatherScreenState> =
        screenProvider(
            initialState = WeatherScreenState("")
        ) { events ->
            WeatherViewModel(events)
        }
}

@Composable
fun ProvideAppState(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAppState provides remember { AppStateImpl() }) {
        content()
    }
}