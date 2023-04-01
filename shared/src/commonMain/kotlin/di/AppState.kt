package di

import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import model.WeatherData
import model.WeatherProvider
import model.WeatherProviderEvent
import model.WeatherProviderState
import model.core.*
import model.net.NetClient
import model.net.NetClientEvent
import viewmodel.WeatherScreenEvent
import viewmodel.WeatherScreenState

interface AppState {
    val storageScope: CoroutineScope
    val logger: Logger
    val netClient: NetClient
    val json: Json

    val weatherProviderPack: WeatherProvider
    val weatherScreenProvider: ScreenProvider<WeatherScreenEvent, WeatherScreenState>
}

val LocalAppState = compositionLocalOf<AppState> { error("AppState is not provided") }

fun AppState.providedCoroutineScope(): ProvidedValue<CoroutineScope> {
    return LocalSharedCoroutineScope provides storageScope
}

fun AppState.provideCoreElements(provideAppState: Boolean = false): List<ProvidedValue<out Any>> {
    val appState = if (provideAppState) listOf(LocalAppState provides this) else listOf()
    return listOf(providedCoroutineScope()) + appState
}
