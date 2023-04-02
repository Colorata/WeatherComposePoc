# Architecture

This project was made as Proof-of-Concept that **Jetpack Compose** can be applied to entire architecture.
This project follows(mostly) MVI architecture

## Events(aka Interaction)

Events is a **bridge** between layers of architecture.
They are presented as `EventFlow` of event classes and can be called like in example:
```kotlin
sealed class CalculatorEvent {
    class Plus(val other: Int): CalculatorEvent()
    class Minus(val other: Int): CalculatorEvent()
}

// Get result directly
@Composable
fun SomeLayerDirectly() {
    val events = rememberEventFlow<CalculatorEvent>()
    val result = AnotherLayer(events)
    LaunchedEffect(Unit) {
        events.emit(CalculatorEvent.Plus(10))
    }
}

// Or get result via flows
@Composable
fun SomeLayerViaFlow() {
    var result by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(Unit) {
        val event = eventFlowOf(CalculatorEvent.Plus(10))
        composableFlow { AnotherLayer(event) }.collect { res ->
            result = res
        }
    }
}

@Composable
fun AnotherLayer(event: EventFlow<CalculatorEvent>): Int {
    var value by remember { mutableStateOf(0) }
    val eventCollected by event.collectAsState(null)
    
    LaunchedEffect(eventCollected) {
        if (eventCollected != null) {
            when (eventCollected) {
                is CalculatorEvent.Plus -> value += eventCollected.value.other
                is CalculatorEvent.Minus -> value -= eventCollected.value.other
            }
        }
    } 
    return value
}
```
*Source: [Event.kt](../shared/src/commonMain/kotlin/model/core/Event.kt)*

## Providers(aka Models) Layer

From the name, **Providers** provide some data to another layer. For example, they can provide data from Internet, get field from local database, etc.
For example:
```kotlin
sealed class NetProviderEvent {
    class Get(val url: String) : NetProviderEvent()
}
@Composable
fun NetProvider(events: EventFlow<NetProviderEvent>): Result<ByteArray> {
    val ktorClient = remember { HttpClient(CIO) }
    val eventsCollected by events.collectAsState(null)

    var result by remember { mutableStateOf<Result<ByteArray>>(loadingResult()) }
    FlowLauchedEffect(eventsCollected) {
        if (event != null && event.value is NetProviderEvent.Get) {
            val url = event.value.url
            val netResult = client.get(url)
            result = if (netResult.status.isSuccess()) successResult(netResult.body())
            else failureResult(Exception(netResult.status.description))
        }
    }
    return result
}
```
*Source: [NetProvider.kt](../shared/src/commonMain/kotlin/model/net/NetProvider.kt)*

In this case, `NetProvider` provides data from Internet in `ByteArray` format with given url.
Unfortunately, it's hard to pass generic types here(at least for now).
<details>
<summary><strong>Containers for Providers</strong></summary>

**Providers** are not used directly. They are wrapped into `Flow` instead.

Wrapping `Composable` into `Flow` is done with [Molecule](https://github.com/cashapp/molecule):

```kotlin
@OptIn(InternalComposeApi::class)
fun <T> composableFlow(vararg providers: ProvidedValue<out Any>, content: @Composable () -> T): Flow<T> {
    return moleculeFlow(RecompositionClock.Immediate) {
        currentComposer.startProviders(providers)
        val result = content()
        currentComposer.endProviders()
        result
    }
}
```
*Source: [ComposableFlow.kt](../shared/src/commonMain/kotlin/model/core/ComposableFlow.kt)*

Then, flows are wrapped into `Packs`:
```kotlin
sealed interface Pack<IN_STATE, EVENT, OUT_STATE> {
    fun provideFlow(events: EventFlow<EVENT>): Flow<OUT_STATE>
}
```
*Source: [Pack.kt](../shared/src/commonMain/kotlin/model/core/Pack.kt)*

Due to some bugs in using Compose in Flows, it is recommended to use `FlowLaunchedEffect` instead of `LaunchedEffect` in `Provider`:
```kotlin
@Composable
fun FlowLaunchedEffect(vararg keys: Any?, block: suspend () -> Unit) {
    LaunchedEffect(Unit) {
        snapshotFlow { keys }.collect {
            block()
        }
    }
}
```
*Source: [ComposableFlow.kt](../shared/src/commonMain/kotlin/model/core/ComposableFlow.kt)*

It's **recommended** to `typealias` packs into more meaningful names:
```kotlin
typealias WeatherProvider = StatefulPack<WeatherProviderState, WeatherProviderEvent, WeatherData>
```

#### Stateless Pack

**Stateless** Packs provide only flows without internal state.
As soon as parent process dies, flow will be killed.

For example, there is `NetProvider`:

```kotlin
val netProvider: NetProvider = statelessPack(
    // ...
) { events ->
    NetProvider(events)
}

netProvider.provideFlowFor(
    NetProviderEvent.Get("https://myapi.com/get"),
    // Other events
).onSuccess { result: ByteArray ->
    // Process result
}.onFailure { exception: Throwable ->  
    // Process exception
}.collect()
```
*Source: [AppStateImpl.kt](../shared/src/commonMain/kotlin/di/AppStateImpl.kt),
[OpenWeatherMapProvider.kt](../shared/src/commonMain/kotlin/model/OpenWeatherMapProvider.kt)*

#### Stateful Pack

**Stateful** Packs provide flows **with** state that live between flow calls.
So, if flow was killed, state will be alive.

For example, there is `WeatherProvider` with `WeatherProviderState`:

```kotlin
val weatherProvider = statefulPack(
    initialState = WeatherProviderState(),
    // ...
) { state: MutableState<WeatherProviderState>, events ->
    OpenWeatherMapProvider(state, events)
}

weatherProvider.provideFlowFor(
    WeatherProviderEvent.WeatherForCity("NYC")
).onSuccess { result -> 
    // Process result
}.onFailure { exception: Throwable ->  
    // Process exception
}.collect()
```
*Source: [AppStateImpl.kt](../shared/src/commonMain/kotlin/di/AppStateImpl.kt),
[WeatherViewModel.kt](../shared/src/commonMain/kotlin/viewmodel/WeatherViewModel.kt)*
</details>

## ViewModel Layer

To connect Providers and UI, there is **ViewModel** Layer:

```kotlin
@Composable
fun WeatherViewModel(events: EventFlow<WeatherScreenEvent>): WeatherScreenState {
    val eventsCollected by events.collectAsState(null)
    var state by remember { mutableStateOf(WeatherScreenState("NYC")) }

    val weatherProvider = LocalAppState.current.weatherProviderPack
    
    val updateWeather = remember {
        suspend {
            weatherProvider.weatherForCity("Kazan") { result ->
                state = state.copy(mainWeatherData = result.mainData, iconWeather = result.icon)
            }
        }
    }

    LaunchedEffect(Unit) {
        updateWeather()
    }
    
    LaunchedEffect(eventsCollected) {
        // Process events from UI
    }

    return state
}

private suspend fun WeatherProvider.weatherForCity(city: String, onLoad: (WeatherData) -> Unit) {
    provideFlowFor(
        WeatherProviderEvent.WeatherForCity(city)
    ).collect {
        onLoad(it)
    }
}
```
*Source: [WeatherViewModel.kt](../shared/src/commonMain/kotlin/viewmodel/WeatherViewModel.kt)*

<details>
<summary><strong>Containers for ViewModels</strong></summary>

ViewModels are wrapped into **ScreenProviders**, allowing them to survive various situations like screen rotation.
For example, there is `ScreenProvider` for `WeatherViewModel`:
```kotlin
val weatherScreenProvider = screenProvider(     
    initialState = WeatherScreenState("NYC")
) { events ->
    WeatherViewModel(events)
}

@Composable
fun WeatherUI() {
    val viewModel by LocalAppState.current.weatherScreenProvider.provide()
    val events = provider.events
    Button(onClick = {
        events.emit(WeatherScreenEvent.RefreshWeather)
    }) {
        Text("Refresh weather")
    }
}
```
*Source: [WeatherViewModel.kt](../shared/src/commonMain/kotlin/viewmodel/WeatherViewModel.kt),
[WeatherScreen.kt](../shared/src/commonMain/kotlin/ui/screen/WeatherScreen.kt)*
</details>

## UI Layer

This layer is responsible only for **UI**.
Although architecture relies on Compose, UI can be non-Composable(Android Views, Java Swing, etc)

For example, there is `WeatherScreen`:
```kotlin
@Composable
fun WeatherScreen() {
    val viewModel by LocalAppState.current.weatherScreenProvider.provide()
    val events = provider.events
    Button(onClick = {
        events.emit(WeatherScreenEvent.RefreshWeather)
    }) {
        Text("Refresh weather")
    }
}
```
*Source: [WeatherScreen.kt](../shared/src/commonMain/kotlin/ui/screen/WeatherScreen.kt)*

## DI

For now, DI is made just as class holding `Providers` and `ScreenProviders`.

DI in this project:
```kotlin
interface AppState {
    val storageScope: CoroutineScope
    val logger: Logger
    val netProvider: NetProvider
    val json: Json

    val weatherProviderPack: WeatherProvider
    val weatherScreenProvider: ScreenProvider<WeatherScreenEvent, WeatherScreenState>
}

private class AppStateImpl : AppState {
    override val storageScope: CoroutineScope = composableCoroutineScope()
    override val logger: Logger by lazy { LoggerImpl() }
    override val netProvider: NetProvider = statelessPack(
        provideAppState = true
    ) { events ->
        NetProvider(events)
    }
    override val json: Json by lazy { Json { ignoreUnknownKeys = true } }

    override val weatherProviderPack =
        statefulPack(
            initialState = WeatherProviderState(),
            provideAppState = true
        ) { state, events ->
            OpenWeatherMapProvider(state, events)
        }

    override val weatherScreenProvider: ScreenProvider<WeatherScreenEvent, WeatherScreenState> =
        screenProvider(
            initialState = WeatherScreenState("NYC")
        ) { events ->
            WeatherViewModel(events)
        }
}
```
*Source: [AppState.kt](../shared/src/commonMain/kotlin/di/AppState.kt),
[AppStateImpl.kt](../shared/src/commonMain/kotlin/di/AppStateImpl.kt)*