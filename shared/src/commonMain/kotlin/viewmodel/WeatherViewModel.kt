package viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import di.LocalAppState
import model.WeatherData
import model.WeatherProviderEvent
import model.core.EventFlow
import model.core.Result
import model.core.rememberEventFlow
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

    val weatherEvents = rememberEventFlow<WeatherProviderEvent>()
    val weatherProvider = LocalAppState.current.weatherProviderPack.provide(weatherEvents)
    val netEvents = rememberEventFlow<NetClientEvent>()
    val netResult = NetClient(netEvents)

    LaunchedEffect(Unit) {
        weatherEvents.emit(WeatherProviderEvent.WeatherForCity("Kazan"))
    }

    LaunchedEffect(eventsCollected) {
        val event = eventsCollected
        if (event != null) {
            if (event.value is WeatherScreenEvent.RefreshWeather) {
                weatherEvents.emit(WeatherProviderEvent.WeatherForCity("Kazan"))
            }
        }
    }

    LaunchedEffect(netResult) {
        println(netResult)
    }

    return WeatherScreenState("Kazan", weatherProvider)
}