import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asComposeImageBitmap
import di.LocalAppState
import di.ProvideAppState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import model.OpenWeatherMapProvider
import model.WeatherProviderEvent
import model.WeatherProviderState
import model.core.*
import model.net.NetClient
import model.net.NetClientEvent
import org.jetbrains.skia.Bitmap
import ui.screen.WeatherScreen
import viewmodel.WeatherScreenEvent

@Composable
internal fun App() {
    ProvideAppState {
        MaterialTheme {
            Column {
                val events = LocalAppState.current.weatherScreenProvider.events
                val viewModel by LocalAppState.current.weatherScreenProvider.provide()
                WeatherScreen(
                    viewModel,
                    onEvent = {
                        events.emit(it)
                    }
                )
            }
        }
    }
}

expect fun getPlatformName(): String