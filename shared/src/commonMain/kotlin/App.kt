import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import di.AppStateImpl
import di.LocalAppState
import model.core.Result
import viewmodel.WeatherScreenEvent

@Composable
internal fun App() {
    val appState = remember { AppStateImpl() }
    CompositionLocalProvider(LocalAppState provides appState) {
        MaterialTheme {
            var showWeather by remember { mutableStateOf(true) }
            Column {
                if (showWeather) {
                    val events = LocalAppState.current.weatherScreenProvider.events
                    val viewModel by LocalAppState.current.weatherScreenProvider.provide()
                    Row {
                        when (val weatherData = viewModel.weatherData) {
                            is Result.Loading -> Text("Loading...")
                            is Result.Success ->
                                Text(
                                    "Current degrees: " +
                                            weatherData.value.actualDegrees.toString()
                                )

                            is Result.Failure ->
                                Text("Cannot load weather")
                        }
                        Button(onClick = { events.emit(WeatherScreenEvent.RefreshWeather) }) {
                            Text("Refresh")
                        }
                    }
                }
                Button({
                    showWeather = !showWeather
                }) {
                    Text(if (showWeather) "Hide weather" else "Show weather")
                }
            }
        }
    }
}

expect fun getPlatformName(): String