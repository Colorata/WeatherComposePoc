import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import di.storage
import model.core.Result
import model.core.rememberEventFlow
import viewmodel.WeatherScreenEvent
import viewmodel.WeatherViewModel

@Composable
internal fun App() {
    CompositionLocalProvider(*storage().providers.toTypedArray()) {
        val events = rememberEventFlow<WeatherScreenEvent>()
        val viewModel = WeatherViewModel(events)
        MaterialTheme {
            Row {
                when (viewModel.weatherData) {
                    is Result.Loading -> Text("Loading...")
                    is Result.Success ->
                        Text(
                            "Current degrees: " +
                                    viewModel.weatherData.value.actualDegrees.toString()
                        )

                    is Result.Failure ->
                        Text("Cannot load weather")
                }
                Button(onClick = { events.emit(WeatherScreenEvent.RefreshWeather) }) {
                    Text("Refresh")
                }
            }
        }
    }
}

expect fun getPlatformName(): String