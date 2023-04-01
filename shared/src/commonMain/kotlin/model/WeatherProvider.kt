package model

import model.core.Result
import model.core.StatefulPack

typealias WeatherProvider = StatefulPack<WeatherProviderState, WeatherProviderEvent, Result<WeatherData>>

data class WeatherData(
    val shortName: String,
    val description: String,
    val icon: Result<ByteArray>,
    val actualDegrees: Float,
    val feelsLikeDegrees: Float
)

data class WeatherProviderState(
    val history: List<WeatherData>
)


sealed class WeatherProviderEvent {

    class WeatherForCity(val city: String) : WeatherProviderEvent()
}
