package model

import model.core.Result
import model.core.StatefulPack
import model.core.loadingResult

typealias WeatherProvider = StatefulPack<WeatherProviderState, WeatherProviderEvent, WeatherData>


data class MainWeatherData(
    val shortName: String,
    val description: String,
    val actualDegrees: Float,
    val feelsLikeDegrees: Float
)

data class WeatherData(
    val mainData: Result<MainWeatherData> = loadingResult(),
    val icon: Result<ByteArray> = loadingResult()
)

data class WeatherProviderState(
    val history: List<MainWeatherData> = listOf()
)


sealed class WeatherProviderEvent {

    class WeatherForCity(val city: String) : WeatherProviderEvent()
}
