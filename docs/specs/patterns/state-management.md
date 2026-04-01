# State Management Pattern

Patterns for managing UI state in ViewModels using StateFlow and immutable state.

## Core Pattern

ViewModels expose a single immutable state via StateFlow and receive user interactions via an Action sealed interface:

```kotlin
data class CalendarState(
    val entries: List<Entry> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val currentMonth: YearMonth = YearMonth.now(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val userMessage: String? = null
)

sealed interface CalendarAction {
    data class SelectDate(val date: LocalDate) : CalendarAction
    data class ChangeMonth(val month: YearMonth) : CalendarAction
    data object Refresh : CalendarAction
    data object MessageShown : CalendarAction
}
```

## State Field Types

| Field Type | Purpose | UI Handling |
|------------|---------|-------------|
| `error: String?` | Persistent error | Inline error display |
| `userMessage: String?` | Transient message | Snackbar, cleared after shown |
| `isLoading: Boolean` | Loading indicator | Show spinner/shimmer |
| `navigationEvent: Event?` | One-shot navigation | Navigate then clear |

## ViewModel Implementation

```kotlin
class CalendarViewModel(
    private val useCases: JournalUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(CalendarState())
    val state: StateFlow<CalendarState> = _state.asStateFlow()

    init {
        loadEntries()
    }

    fun onAction(action: CalendarAction) {
        when (action) {
            is CalendarAction.SelectDate -> selectDate(action.date)
            is CalendarAction.ChangeMonth -> changeMonth(action.month)
            is CalendarAction.Refresh -> loadEntries()
            is CalendarAction.MessageShown -> clearMessage()
        }
    }

    private fun loadEntries() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            useCases.getEntries()
                .onSuccess { entries ->
                    _state.update {
                        it.copy(isLoading = false, entries = entries)
                    }
                }
                .onError { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = ErrorFormatter.format(error)
                        )
                    }
                }
        }
    }

    private fun selectDate(date: LocalDate) {
        _state.update { it.copy(selectedDate = date) }
    }

    private fun changeMonth(month: YearMonth) {
        _state.update { it.copy(currentMonth = month) }
    }

    private fun clearMessage() {
        _state.update { it.copy(userMessage = null) }
    }

    companion object {
        private const val TAG = "CalendarViewModel"
    }
}
```

## Navigation Events

For one-shot navigation, use a sealed interface in state:

```kotlin
data class EditorState(
    val entry: Entry? = null,
    val isSaving: Boolean = false,
    val navigationEvent: EditorNavigationEvent? = null
)

sealed interface EditorNavigationEvent {
    data object SaveSuccess : EditorNavigationEvent
    data object Back : EditorNavigationEvent
}

sealed interface EditorAction {
    data class Save(val entry: Entry) : EditorAction
    data object NavigateBack : EditorAction
    data object NavigationHandled : EditorAction
}
```

Handle in the ScreenRoot:

```kotlin
@Composable
fun EditorScreenRoot(
    viewModel: EditorViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.navigationEvent) {
        when (state.navigationEvent) {
            is EditorNavigationEvent.SaveSuccess -> onNavigateBack()
            is EditorNavigationEvent.Back -> onNavigateBack()
            null -> { /* no-op */ }
        }
        if (state.navigationEvent != null) {
            viewModel.onAction(EditorAction.NavigationHandled)
        }
    }

    EditorScreen(
        state = state,
        onAction = viewModel::onAction
    )
}
```

## Alternative: SharedFlow for Events

For truly one-time events (snackbars, toasts), SharedFlow can be used:

```kotlin
class CalendarViewModel(...) : ViewModel() {

    private val _state = MutableStateFlow(CalendarState())
    val state: StateFlow<CalendarState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    sealed interface UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent
    }

    private fun handleError(error: DataError) {
        viewModelScope.launch {
            _events.emit(UiEvent.ShowSnackbar(ErrorFormatter.format(error)))
        }
    }
}
```

Collect in ScreenRoot:

```kotlin
LaunchedEffect(Unit) {
    viewModel.events.collect { event ->
        when (event) {
            is UiEvent.ShowSnackbar -> {
                snackbarHostState.showSnackbar(event.message)
            }
        }
    }
}
```

## State Update Patterns

### Simple Update
```kotlin
_state.update { it.copy(isLoading = true) }
```

### Conditional Update
```kotlin
_state.update { current ->
    if (current.entries.isEmpty()) {
        current.copy(error = "No entries found")
    } else {
        current
    }
}
```

### Derived State
```kotlin
data class CalendarState(
    val entries: List<Entry> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now()
) {
    val selectedEntry: Entry?
        get() = entries.find { it.date == selectedDate }

    val hasEntryForSelectedDate: Boolean
        get() = selectedEntry != null
}
```

## Testing State Changes

```kotlin
@Test
fun `onAction Refresh - updates loading state`() = runTest {
    viewModel.state.test {
        // Initial state
        assertThat(awaitItem().isLoading).isFalse()

        // Trigger action
        viewModel.onAction(CalendarAction.Refresh)

        // Loading state
        assertThat(awaitItem().isLoading).isTrue()

        // Success state
        val finalState = awaitItem()
        assertThat(finalState.isLoading).isFalse()
        assertThat(finalState.entries).isNotEmpty()
    }
}
```
