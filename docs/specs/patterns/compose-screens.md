# Compose Screen Pattern

Patterns for implementing screens with Jetpack Compose.

## Root/Presenter Pattern

Every screen has two composables:

1. **ScreenRoot** - Handles ViewModel, navigation, side effects
2. **Screen** - Pure UI, receives state and callbacks

```kotlin
// CalendarScreenRoot.kt - Handles ViewModel and navigation
@Composable
fun CalendarScreenRoot(
    viewModel: CalendarViewModel = koinViewModel(),
    onNavigateToEditor: (Int?) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Handle navigation events
    LaunchedEffect(state.navigationEvent) {
        when (val event = state.navigationEvent) {
            is CalendarNavigationEvent.OpenEditor -> {
                onNavigateToEditor(event.entryId)
            }
            null -> { /* no-op */ }
        }
        if (state.navigationEvent != null) {
            viewModel.onAction(CalendarAction.NavigationHandled)
        }
    }

    CalendarScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

// CalendarScreen.kt - Pure UI
@Composable
fun CalendarScreen(
    state: CalendarState,
    onAction: (CalendarAction) -> Unit
) {
    Scaffold(
        topBar = {
            CalendarTopBar(
                currentMonth = state.currentMonth,
                onPreviousMonth = { onAction(CalendarAction.PreviousMonth) },
                onNextMonth = { onAction(CalendarAction.NextMonth) }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingIndicator()
        } else {
            CalendarContent(
                entries = state.entries,
                selectedDate = state.selectedDate,
                onDateClick = { onAction(CalendarAction.SelectDate(it)) },
                modifier = Modifier.padding(padding)
            )
        }
    }
}
```

### Why This Pattern?

- **Testability**: Pure Screen can be tested in isolation with preview
- **Separation**: ViewModel logic stays out of UI composition
- **Reusability**: Screen can be reused with different data sources
- **Navigation**: Root handles navigation callbacks cleanly

## State Hoisting

Pass state down, events up:

```kotlin
@Composable
fun MoodColorPicker(
    selectedColor: MoodColor?,           // State passed down
    availableColors: List<MoodColor>,    // State passed down
    onColorSelected: (MoodColor) -> Unit // Event passed up
) {
    LazyRow {
        items(availableColors) { color ->
            ColorChip(
                color = color,
                isSelected = color == selectedColor,
                onClick = { onColorSelected(color) }
            )
        }
    }
}
```

## Side Effects

Use appropriate effect handlers:

```kotlin
@Composable
fun EditorScreenRoot(...) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // One-time setup
    LaunchedEffect(Unit) {
        viewModel.onAction(EditorAction.Initialize)
    }

    // React to state changes
    LaunchedEffect(state.navigationEvent) {
        state.navigationEvent?.let { event ->
            handleNavigation(event)
            viewModel.onAction(EditorAction.NavigationHandled)
        }
    }

    // Collect one-time events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            // Cleanup if needed
        }
    }
}
```

## Preview Support

Design screens to be previewable:

```kotlin
@Preview(showBackground = true)
@Composable
private fun CalendarScreenPreview() {
    TheDayToTheme {
        CalendarScreen(
            state = CalendarState(
                entries = listOf(
                    Entry(id = 1, date = LocalDate.now(), mood = "Happy"),
                    Entry(id = 2, date = LocalDate.now().minusDays(1), mood = "Calm")
                ),
                selectedDate = LocalDate.now(),
                isLoading = false
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarScreenLoadingPreview() {
    TheDayToTheme {
        CalendarScreen(
            state = CalendarState(isLoading = true),
            onAction = {}
        )
    }
}
```

## Component Extraction

Extract reusable components to `components/` package:

```
journal/ui/calendar/
├── CalendarScreenRoot.kt
├── CalendarScreen.kt
├── CalendarViewModel.kt
├── CalendarState.kt
└── components/
    ├── CalendarGrid.kt
    ├── CalendarTopBar.kt
    ├── DayCell.kt
    └── MonthNavigator.kt
```

### Component Guidelines

```kotlin
// Small, focused components
@Composable
fun DayCell(
    date: LocalDate,
    entry: Entry?,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Single responsibility: render one day cell
}

// Composed from smaller components
@Composable
fun CalendarGrid(
    month: YearMonth,
    entries: Map<LocalDate, Entry>,
    selectedDate: LocalDate,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(...) {
        items(daysInMonth) { date ->
            DayCell(
                date = date,
                entry = entries[date],
                isSelected = date == selectedDate,
                isToday = date == LocalDate.now(),
                onClick = { onDateClick(date) }
            )
        }
    }
}
```

## Navigation Setup

In the navigation graph:

```kotlin
// In AppNavigation.kt
NavHost(navController, startDestination = "calendar") {
    composable("calendar") {
        CalendarScreenRoot(
            onNavigateToEditor = { entryId ->
                navController.navigate("editor/$entryId")
            },
            onNavigateToSettings = {
                navController.navigate("settings")
            }
        )
    }

    composable(
        route = "editor/{entryId}",
        arguments = listOf(navArgument("entryId") { type = NavType.IntType })
    ) { backStackEntry ->
        val entryId = backStackEntry.arguments?.getInt("entryId")
        EditorScreenRoot(
            entryId = entryId,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
```

## Accessibility

Follow Material 3 accessibility guidelines:

```kotlin
@Composable
fun DayCell(
    date: LocalDate,
    entry: Entry?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)  // Minimum touch target
            .clickable(
                onClick = onClick,
                onClickLabel = "Select ${date.format(DateTimeFormatter.ofPattern("MMMM d"))}"
            )
            .semantics {
                contentDescription = buildString {
                    append(date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")))
                    entry?.let { append(", mood: ${it.mood}") }
                }
            }
    ) {
        // Content
    }
}
```
