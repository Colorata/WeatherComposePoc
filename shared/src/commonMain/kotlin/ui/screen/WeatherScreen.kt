package ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import model.WeatherData
import model.core.Result
import model.core.isLoading
import model.core.isSuccess
import ui.core.Image
import viewmodel.WeatherScreenEvent
import viewmodel.WeatherScreenState

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WeatherScreen(state: WeatherScreenState, onEvent: (WeatherScreenEvent) -> Unit, modifier: Modifier = Modifier) {
    val transitionSpec: (AnimatedContentScope<*>.() -> ContentTransform) = remember {
        {
            val duration = 500
            fadeIn(tween(duration)) + slideIntoContainer(
                AnimatedContentScope.SlideDirection.Up,
                animationSpec = tween(duration)
            ) with
                    fadeOut(tween(duration)) + slideOutOfContainer(
                AnimatedContentScope.SlideDirection.Up,
                animationSpec = tween(duration)
            ) using
                    SizeTransform(clip = false)
        }
    }
    Box(modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(Modifier.fillMaxWidth().weight(0.3f), contentAlignment = Alignment.Center) {
                AnimatedContent(
                    targetState = if (state.weatherData.isSuccess()) state.weatherData.value.icon else Result.Loading(),
                    transitionSpec = transitionSpec
                ) { targetState ->
                    if (targetState.isSuccess() && targetState.isSuccess()) {
                        val icon = targetState.value
                        Image(icon, contentDescription = "Icon", Modifier.size(48.dp))
                    } else {
                        Text("No icon")
                    }
                }
            }
            Box(Modifier.fillMaxWidth().weight(0.7f), contentAlignment = Alignment.TopCenter) {
                AnimatedContent(
                    targetState = when (state.weatherData) {
                        is Result.Success -> Result.Success(state.weatherData.value.copy(icon = Result.Loading()))
                        is Result.Loading -> Result.Loading()
                        is Result.Failure -> Result.Failure(state.weatherData.throwable)
                    },
                    transitionSpec = transitionSpec,
                ) { targetState ->
                    val text = if (targetState.isSuccess()) {
                        val weather = targetState.value
                        "${weather.shortName}, ${weather.actualDegrees}°C, feels like ${weather.feelsLikeDegrees}°C"
                    } else if (targetState.isLoading()) {
                        "Loading..."
                    } else {
                        "Cannot load weather"
                    }
                    Text(
                        text,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        LargeFloatingActionButton(onClick = {
            onEvent(WeatherScreenEvent.RefreshWeather)
        }, Modifier.align(Alignment.BottomEnd).padding(12.dp)) {
            Icon(Icons.Default.Refresh, contentDescription = "Refresh weather")
        }
    }
}