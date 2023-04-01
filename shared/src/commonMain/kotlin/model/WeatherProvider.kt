package model

import androidx.compose.runtime.*
import di.LocalAppState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import model.core.*
import model.net.NetClient
import model.net.NetClientEvent

data class WeatherData(
    val actualDegrees: Float,
    val feelsLikeDegrees: Float
)

data class WeatherProviderState(
    val history: List<WeatherData>
)

sealed class WeatherProviderEvent {

    class WeatherForCity(val city: String) : WeatherProviderEvent()

    class IconWeatherForCity(val city: String) : WeatherProviderEvent()
}


@Composable
fun WeatherProvider(
    providerState: MutableState<WeatherProviderState>,
    events: EventFlow<WeatherProviderEvent>
): Result<WeatherData> {
    var state by providerState
    val logger = LocalAppState.current.logger
    val json = LocalAppState.current.json

    val netEvents = rememberEventFlow<NetClientEvent>()
    val netClient = NetClient(netEvents)

    val eventsCollected by events.collectAsState(null)
    var weatherData by remember { mutableStateOf<Result<WeatherData>>(Result.Loading()) }

    LaunchedEffect(eventsCollected) {
        when (val event = eventsCollected?.value) {
            is WeatherProviderEvent.WeatherForCity -> {
                weatherData = Result.Loading()
                netEvents.emit(NetClientEvent.Get(urlForOpenWeatherMap(event.city, OPENWEATHERMAP_API_KEY)))
            }

            is WeatherProviderEvent.IconWeatherForCity -> {
                if (state.history.isNotEmpty()) {
                    // TODO: Implement icon fetching
                }
            }

            else -> {}
        }
    }

    LaunchedEffect(netClient) {
        val result = netClient.asOther { json.decodeFromString<OpenWeatherMapWeatherResponse>(it).toWeatherData() }

        if (result is Result.Success) state = state.copy(history = state.history + listOf(result.value))

        logger.debug("New weather", result.toString())
        logger.info("History", state.history.joinToString(", "))

        weatherData = result
    }
    return weatherData
}

private fun urlForOpenWeatherMap(city: String, apiKey: String) =
    "https://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&appid=$apiKey"

private const val OPENWEATHERMAP_API_KEY = "201d8e3dd3a424462228eed61610772d"

@Serializable
private data class OpenWeatherMapWeatherResponse(
    val weather: List<WeatherResponseWeatherItem>,
    val main: WeatherResponseMain
) {
    fun toWeatherData(): WeatherData {
        return WeatherData(main.actualDegrees, main.feelsLikeDegrees)
    }

    val icon: String? = if (weather.isEmpty()) null else weather.last().icon
}

@Serializable
private class WeatherResponseMain(
    @SerialName("temp")
    val actualDegrees: Float,
    @SerialName("feels_like")
    val feelsLikeDegrees: Float
)

@Serializable
private class WeatherResponseWeatherItem(
    @SerialName("main")
    val name: String,
    @SerialName("description")
    val description: String,
    @SerialName("icon")
    val icon: String
)