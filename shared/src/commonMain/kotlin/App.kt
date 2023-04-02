import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import di.LocalAppState
import di.ProvideAppState
import ui.screen.WeatherScreen

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