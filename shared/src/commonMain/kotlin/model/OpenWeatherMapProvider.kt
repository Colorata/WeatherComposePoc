package model

import androidx.compose.runtime.*
import di.LocalAppState
import io.ktor.utils.io.charsets.*
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import model.core.*
import model.net.NetClientEvent

@Composable
fun OpenWeatherMapProvider(
    providerState: MutableState<WeatherProviderState>, events: EventFlow<WeatherProviderEvent>
): Result<WeatherData> {
    var state by providerState
    val json = LocalAppState.current.json
    val logger = LocalAppState.current.logger

    val netClient = LocalAppState.current.netClient
    val eventsCollected by events.collectAsState(null)
    var weatherData by remember { mutableStateOf<Result<WeatherData>>(Result.Loading()) }

    LaunchedEffect(eventsCollected) {
        when (val event = eventsCollected?.value) {
            is WeatherProviderEvent.WeatherForCity -> {
                weatherData = Result.Loading()

                weatherForCity(netClient, event.city, json) { result ->
                    if (result.isSuccess()) {
                        state = state.copy(history = state.history + listOf(result.value))
                    } else if (result.isFailure()) {
                        logger.error("Cannot fetch weather", result.throwable.stackTraceToString())
                    }

                    weatherData = result
                }
            }

            else -> {}
        }
    }
    return weatherData
}

private suspend fun weatherForCity(
    netClient: StatelessPack<NetClientEvent, Result<ByteArray>>,
    city: String,
    json: Json,
    onLoad: (Result<WeatherData>) -> Unit
) {
    netClient.provideFlow(
        eventFlowOf(
            NetClientEvent.Get(urlForCity(city, OPENWEATHERMAP_API_KEY))
        )
    ).onSuccess {
        val result = json.decodeFromString<OpenWeatherMapWeatherResponse>(it.decodeToString())
        val convertedResult = result.toWeatherData()
        onLoad(Result.Success(convertedResult))

        val icon = result.icon
        if (icon != null) {
            netClient.provideFlow(
                eventFlowOf(
                    NetClientEvent.Get(urlForIcon(icon))
                )
            ).onSuccess { ic ->
                onLoad(Result.Success(convertedResult.copy(icon = Result.Success(ic))))
            }.onFailure { throwable ->
                onLoad(Result.Success(convertedResult.copy(icon = Result.Failure(throwable))))
            }.collect()
        }
    }.onFailure {
        onLoad(Result.Failure(it))
    }.collect()
}

private fun urlForCity(city: String, apiKey: String) =
    "https://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&appid=$apiKey"

private fun urlForIcon(icon: String) =
    "http://openweathermap.org/img/w/$icon.png"

private const val OPENWEATHERMAP_API_KEY = "201d8e3dd3a424462228eed61610772d"

@Serializable
private data class OpenWeatherMapWeatherResponse(
    val weather: List<WeatherResponseWeatherItem>, val main: WeatherResponseMain
) {
    fun toWeatherData(): WeatherData {
        val lastWeather = weather.last()
        return WeatherData(
            lastWeather.name, lastWeather.description, Result.Loading(), main.actualDegrees, main.feelsLikeDegrees
        )
    }

    val icon: String? = if (weather.isEmpty()) null else weather.last().icon
}

@Serializable
private class WeatherResponseMain(
    @SerialName("temp") val actualDegrees: Float, @SerialName("feels_like") val feelsLikeDegrees: Float
)

@Serializable
private class WeatherResponseWeatherItem(
    @SerialName("main") val name: String,
    @SerialName("description") val description: String,
    @SerialName("icon") val icon: String
)