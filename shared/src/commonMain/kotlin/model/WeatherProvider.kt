package model

import androidx.compose.runtime.*
import di.LocalAppState
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import model.core.*
import model.net.NetClientEvent

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