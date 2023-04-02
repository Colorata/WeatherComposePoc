package viewmodel

import androidx.compose.runtime.*
import di.LocalAppState
import model.MainWeatherData
import model.WeatherData
import model.WeatherProvider
import model.WeatherProviderEvent
import model.core.*

data class WeatherScreenState(
    val city: String,
    val mainWeatherData: Result<MainWeatherData> = loadingResult(),
    val iconWeather: Result<ByteArray> = loadingResult()
)

sealed class WeatherScreenEvent {
    object RefreshWeather : WeatherScreenEvent()
}

@Composable
fun WeatherViewModel(events: EventFlow<WeatherScreenEvent>): WeatherScreenState {
    val eventsCollected by events.collectAsState(null)

    var state by remember { mutableStateOf(WeatherScreenState("Kazan")) }

    val logger = LocalAppState.current.logger
    val weatherProvider = LocalAppState.current.weatherProviderPack

    val updateWeather = remember {
        suspend {
            weatherProvider.weatherForCity("Kazan") { result ->
                state = state.copy(mainWeatherData = result.mainData, iconWeather = result.icon)
            }
        }
    }

    LaunchedEffect(Unit) {
        updateWeather()
    }

    LaunchedEffect(eventsCollected) {
        val event = eventsCollected
        when (event?.value) {
            is WeatherScreenEvent.RefreshWeather -> {
                updateWeather()
            }

            else -> {}
        }
    }

    return state
}

private suspend fun WeatherProvider.weatherForCity(city: String, onLoad: (WeatherData) -> Unit) {
    provideFlowFor(
        WeatherProviderEvent.WeatherForCity(city)
    ).collect {
        onLoad(it)
    }
}