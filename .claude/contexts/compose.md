# Compose Context — Jetpack Compose UI

## Screen Structure

Каждый экран:
- `*Screen.kt` — Composable с UI
- `*ViewModel.kt` — ViewModel с состоянием
- `*UiState.kt` — sealed class для состояний

```kotlin
// WorkoutUiState.kt
sealed class WorkoutUiState {
    object Loading : WorkoutUiState()
    data class Success(val sets: List<WorkoutSet>) : WorkoutUiState()
    data class Error(val message: String) : WorkoutUiState()
}

// WorkoutViewModel.kt
@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val addSetUseCase: AddSetUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<WorkoutUiState>(WorkoutUiState.Loading)
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()
    
    fun addSet(weight: Float, reps: Int) {
        viewModelScope.launch {
            addSetUseCase(weight, reps)
        }
    }
}

// WorkoutScreen.kt
@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    WorkoutContent(
        uiState = uiState,
        onAddSet = viewModel::addSet
    )
}

@Composable
private fun WorkoutContent(
    uiState: WorkoutUiState,
    onAddSet: (Float, Int) -> Unit
) {
    // UI implementation
}
```

## State Hoisting

- Состояние в ViewModel
- События через callbacks
- Preview-friendly: отдельный `*Content` composable

## Компоненты

Переиспользуемые компоненты в `ui/components/`:

```kotlin
@Composable
fun SetInputCard(
    weight: String,
    reps: String,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
)
```

## Navigation

Используем Navigation Compose:

```kotlin
@Composable
fun GymNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "workout"
    ) {
        composable("workout") { WorkoutScreen() }
        composable("history") { HistoryScreen() }
        composable("progress") { ProgressScreen() }
        composable("records") { RecordsScreen() }
    }
}
```

## Theme

Material 3 с кастомными цветами:

```kotlin
@Composable
fun GymAssistantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

## Preview

Каждый компонент должен иметь Preview:

```kotlin
@Preview(showBackground = true)
@Composable
private fun SetInputCardPreview() {
    GymAssistantTheme {
        SetInputCard(
            weight = "80",
            reps = "10",
            onWeightChange = {},
            onRepsChange = {},
            onSave = {}
        )
    }
}
```
