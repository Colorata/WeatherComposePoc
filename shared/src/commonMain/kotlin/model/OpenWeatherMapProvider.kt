package model

import androidx.compose.runtime.*
import di.LocalAppState
import io.ktor.utils.io.charsets.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import model.core.*
import model.net.NetProvider
import model.net.NetProviderEvent

@Composable
fun OpenWeatherMapProvider(
    providerState: MutableState<WeatherProviderState>, events: EventFlow<WeatherProviderEvent>
): WeatherData {
    var state by providerState
    val json = LocalAppState.current.json
    val logger = LocalAppState.current.logger

    val netClient = LocalAppState.current.netProvider
    val eventsCollected by events.collectAsState(null)
    var weatherData by remember { mutableStateOf(WeatherData()) }

    FlowLaunchedEffect(eventsCollected) {
        when (val event = eventsCollected?.value) {
            is WeatherProviderEvent.WeatherForCity -> {
                weatherData = WeatherData()

                weatherForCity(netClient, event.city, json, logger, onLoad = { result ->
                    if (result.isSuccess()) {
                        state = state.copy(history = state.history + listOf(result.value))
                    } else if (result.isFailure()) {
                        logger.error("Cannot fetch weather", result.throwable.stackTraceToString())
                    }

                    weatherData = weatherData.copy(mainData = result)
                }, onLoadIcon = {
                    weatherData = weatherData.copy(icon = it)
                })
            }

            else -> {}
        }
    }
    return weatherData
}

private suspend fun weatherForCity(
    netClient: NetProvider,
    city: String,
    json: Json,
    logger: Logger,
    onLoad: (Result<MainWeatherData>) -> Unit,
    onLoadIcon: (Result<ByteArray>) -> Unit
) {
    netClient.provideFlowFor(
        NetProviderEvent.Get(urlForCity(city, OPENWEATHERMAP_API_KEY))
    ).onSuccess {
        val result = json.decodeFromString<OpenWeatherMapWeatherResponse>(it.decodeToString())
        val convertedResult = result.toMainWeatherData()
        onLoad(successResult(convertedResult))

        val iconName = result.icon
        if (iconName != null) {
            netClient.provideFlowFor(
                NetProviderEvent.Get(urlForIcon(iconName))
            ).collect { icon ->
                delay(1000)
                onLoadIcon(icon)
            }
        }
    }.onFailure {
        onLoad(failureResult(it))
    }.collect()
}

private fun urlForCity(city: String, apiKey: String) =
    "https://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&appid=$apiKey"

private fun urlForIcon(icon: String) =
    "http://openweathermap.org/img/w/$icon.png"

// TODO: Move key to local.properties
private const val OPENWEATHERMAP_API_KEY = "201d8e3dd3a424462228eed61610772d"

@Serializable
private data class OpenWeatherMapWeatherResponse(
    val weather: List<WeatherResponseWeatherItem>, val main: WeatherResponseMain
) {
    fun toMainWeatherData(): MainWeatherData {
        val lastWeather = weather.last()
        return MainWeatherData(
            lastWeather.name, lastWeather.description, main.actualDegrees, main.feelsLikeDegrees
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