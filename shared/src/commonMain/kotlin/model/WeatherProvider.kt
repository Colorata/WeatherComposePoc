package model

import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import model.core.*
import model.net.LocalJson
import model.net.NetClient
import model.net.NetClientEvent
import kotlin.random.Random

data class WeatherData(
    val actualDegrees: Float,
    val feelsLikeDegrees: Float
)

data class WeatherProviderState(
    val history: List<WeatherData>,
    val logger: Logger
)

sealed class WeatherProviderEvent {

    class WeatherForCity(val city: String) : WeatherProviderEvent()
}

val LocalWeatherProviderState =
    localMutableStateOf<WeatherProviderState> { error("WeatherProviderState is not provided") }


@Composable
fun WeatherProvider(events: EventFlow<WeatherProviderEvent>): Result<WeatherData> {
    var state by LocalWeatherProviderState.current
    val logger = LocalLogger.current
    val json = LocalJson.current

    val netEvents = rememberEventFlow<NetClientEvent>()
    val netClient = NetClient(netEvents)

    val eventsCollected by events.collectAsState(null)
    var weatherData by remember { mutableStateOf<Result<WeatherData>>(Result.Loading()) }

    LaunchedEffect(eventsCollected) {
        val event = eventsCollected?.value
        if (event is WeatherProviderEvent.WeatherForCity) {
            weatherData = Result.Loading()
            // TODO: Ktor implementation
            netEvents.emit(NetClientEvent.Get(urlForOpenWeatherMap(event.city, OPENWEATHERMAP_API_KEY)))
        }

    }

    LaunchedEffect(netClient) {
        val result = netClient.asOther { json.decodeFromString<OpenWeatherMapWeatherResponse>(it).toWeatherData() }

        if (result is Result.Success) state = state.copy(history = state.history + listOf(result.value))

        logger.debug("New weather", result.toString())

        weatherData = result
    }
    return weatherData
}

private fun urlForOpenWeatherMap(city: String, apiKey: String) =
    "https://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&appid=$apiKey"

private const val OPENWEATHERMAP_API_KEY = "201d8e3dd3a424462228eed61610772d"

@Serializable
private data class OpenWeatherMapWeatherResponse(
    val main: WeatherResponseMain
) {
    fun toWeatherData(): WeatherData {
        return WeatherData(main.actualDegrees, main.feelsLikeDegrees)
    }
}

@Serializable
private class WeatherResponseMain(
    @SerialName("temp")
    val actualDegrees: Float,
    @SerialName("feels_like")
    val feelsLikeDegrees: Float
)