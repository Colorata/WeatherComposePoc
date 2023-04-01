package viewmodel

import androidx.compose.runtime.*
import di.LocalAppState
import model.WeatherData
import model.WeatherProvider
import model.WeatherProviderEvent
import model.core.*
import model.net.NetClient
import model.net.NetClientEvent

data class WeatherScreenState(
    val city: String,
    val weatherData: Result<WeatherData>
)

sealed class WeatherScreenEvent {
    object RefreshWeather : WeatherScreenEvent()
}

@Composable
fun WeatherViewModel(events: EventFlow<WeatherScreenEvent>): WeatherScreenState {
    val eventsCollected by events.collectAsState(null)

    var state by remember { mutableStateOf(WeatherScreenState("Kazan", Result.Loading())) }

    val weatherProvider = LocalAppState.current.weatherProviderPack

    val updateWeather = remember {
        suspend {
            weatherProvider.weatherForCity("Kazan") { result ->
                state = when (result) {
                    is Result.Success -> state.copy(weatherData = Result.Success(result.value))
                    is Result.Failure -> state.copy(weatherData = Result.Failure(result.throwable))
                    is Result.Loading -> state.copy(weatherData = Result.Loading())
                }
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

private suspend fun WeatherProvider.weatherForCity(city: String, onLoad: (Result<WeatherData>) -> Unit) {
    provideFlow(
        eventFlowOf(WeatherProviderEvent.WeatherForCity("Kazan"))
    ).collect {
        onLoad(it)
    }
}