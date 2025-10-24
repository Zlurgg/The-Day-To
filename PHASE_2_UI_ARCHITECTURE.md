# Phase 2: Architecture Overhaul & UI Modernization

**Target:** Modernize architecture AND UI to industry standards (Material Design 3 + Google MAD)
**Estimated Time:** 12-16 hours (multiple sessions)
**Status:** ✅ Architecture Complete | 🔄 UI In Progress (0/7 UI tasks complete)
**Last Updated:** 2025-10-24

**NOTE:** Part A (Architecture Refactoring) was completed via comprehensive structure overhaul in Session 5. This document now focuses on Part B (UI Modernization) for future sessions.

---

## Table of Contents

1. [Overview](#overview)
2. [Part A: Architecture Refactoring](#part-a-architecture-refactoring)
3. [Part B: UI Modernization](#part-b-ui-modernization)
4. [Material Design 3 Guidelines](#material-design-3-guidelines)
5. [Component Specifications](#component-specifications)
6. [Screen Designs](#screen-designs)
7. [Session Tracking](#session-tracking)

---

## Overview

### Goals

**Architecture:** ✅ COMPLETE
- ✅ Clean Architecture data/domain separation implemented
- ✅ Package structure modernized (`journal`, `auth`, `ui`, `usecases`)
- ✅ Entity/mapper pattern for Room database
- ✅ All architectural violations fixed
- ✅ Proper dependency boundaries established

**UI:** 🔄 IN PROGRESS (Next Phase)
- [ ] Modern Material Design 3 components
- [ ] Professional visual design
- [ ] Excellent UX patterns
- [ ] Loading/empty/error states
- [ ] Portfolio-quality polish

### Success Criteria

- [ ] All screens use Material3 TopAppBar
- [ ] All lists use Card-based layouts
- [ ] Consistent 8dp spacing grid
- [ ] Proper elevation hierarchy
- [ ] Loading/empty/error states everywhere
- [ ] Smooth animations and transitions
- [ ] Accessible (WCAG AA minimum)
- [ ] Passes Material3 design review

---

## Part A: Architecture Refactoring ✅ COMPLETE

**Status:** All architectural goals achieved via comprehensive structure overhaul (Session 5, 2025-10-24)

**What Was Completed:**
- ✅ Implemented Clean Architecture data/domain separation with entities and mappers
- ✅ Renamed all packages: `feature_*` → `journal`/`auth`, `use_case` → `usecases`, `presentation` → `ui`
- ✅ Renamed UI features: `add_edit` → `editor`, `entries` → `overview`
- ✅ Fixed architectural violations: moved implementations to correct layers
- ✅ Domain models are now pure Kotlin (no `@Entity` annotations)
- ✅ Proper package naming (lowercase, no snake_case)

**Note:** The originally planned incremental tasks (2.1-2.9) were superseded by a comprehensive refactoring approach using Android Studio's refactoring tools. The architectural goals were achieved more efficiently through this method.

**What's Remaining for Future (Optional):**
- Type-safe navigation migration (deferred)
- MoodColorPickerDialog as reusable component (current implementation works)
- Standalone MoodColorManagerScreen (nice-to-have feature)

---

### ~~Task 2.1: Convert AddEditMoodColorScreen → MoodColorPickerDialog~~ (Deferred)

**Priority:** CRITICAL
**Estimated Time:** 1.5 hours
**Actual Time:** ~1.5 hours
**Status:** [x] ✅ Completed (2025-10-23)

#### Objective
Convert full-screen mood color creation to Material3 Dialog, solving dual ViewModel anti-pattern.

#### Deliverables
- [x] Create `MoodColorPickerDialog.kt` component
- [x] Material3 AlertDialog wrapper with proper styling
- [x] Color picker (HsvColorPicker)
- [x] Mood name text field (validation in ViewModel)
- [x] Icon buttons (Check/Close) for cleaner UI
- [x] Callback pattern (no ViewModel injection)
- [x] Delete `AddEditMoodColorScreen.kt`
- [x] Update MoodItem component to use dialog
- [x] Business logic moved to ViewModel (proper separation)
- [x] State auto-resets when dialog reopens

#### Implementation Details

```kotlin
@Composable
fun MoodColorPickerDialog(
    showDialog: Boolean,
    initialMood: String = "",
    initialColor: String = "#000000",
    onDismiss: () -> Unit,
    onSave: (mood: String, color: String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (showDialog) {
        var mood by remember { mutableStateOf(initialMood) }
        var color by remember { mutableStateOf(initialColor) }
        var showError by remember { mutableStateOf(false) }

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Create Mood Color",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mood name field
                    OutlinedTextField(
                        value = mood,
                        onValueChange = {
                            mood = it
                            showError = false
                        },
                        label = { Text("Mood Name") },
                        isError = showError,
                        supportingText = if (showError) {
                            { Text("Mood name is required") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Color picker
                    ColorPickerComponent(
                        selectedColor = color,
                        onColorChanged = { color = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Preview
                    MoodPreviewChip(mood = mood, color = color)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (mood.isBlank()) {
                                    showError = true
                                } else {
                                    onSave(mood, color)
                                    onDismiss()
                                }
                            }
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}
```

#### Files to Create
- `feature_mood_color/presentation/components/MoodColorPickerDialog.kt`
- `feature_mood_color/presentation/components/ColorPickerComponent.kt`
- `feature_mood_color/presentation/components/MoodPreviewChip.kt`

#### Files to Delete
- `feature_mood_color/presentation/AddEditMoodColorScreen.kt`

#### Files to Modify
- `feature_daily_entry/presentation/add_edit_daily_entry/AddEditEntryScreen.kt`
- `feature_daily_entry/presentation/add_edit_daily_entry/AddEditEntryViewModel.kt`
- `feature_daily_entry/presentation/add_edit_daily_entry/components/MoodItem.kt`

---

### Task 2.2: Standardize Naming - Event → Action Pattern

**Priority:** High
**Estimated Time:** 45 minutes
**Status:** [ ] Not Started

#### Files to Rename
- [x] `AddEditEntryEvent.kt` → `AddEditEntryAction.kt`
- [x] `EntriesEvent.kt` → `EntriesAction.kt`
- [x] `AddEditMoodColorEvent.kt` → `AddEditMoodColorAction.kt`

#### Pattern
```kotlin
// User interactions → Actions (imperative)
sealed interface AddEditEntryAction {
    data class EnterMood(val mood: String) : AddEditEntryAction
    data class EnterContent(val value: String) : AddEditEntryAction
    data class EnterColor(val color: String) : AddEditEntryAction
    data class EnterDate(val date: Long) : AddEditEntryAction
    data object SaveEntry : AddEditEntryAction
}

// One-time UI events → UiEvent (keep as Event)
sealed interface AddEditEntryUiEvent {
    data class ShowSnackbar(val message: String) : AddEditEntryUiEvent
    data object NavigateBack : AddEditEntryUiEvent
    data object EntrySaved : AddEditEntryUiEvent
}
```

#### Updates Needed
- [ ] Rename all `onEvent()` methods → `onAction()`
- [ ] Update all event dispatch calls in screens
- [ ] Update imports across codebase

---

### Task 2.3: Standardize Naming - State → UiState Pattern

**Priority:** High
**Estimated Time:** 30 minutes
**Status:** [ ] Not Started

#### Files to Rename
- [x] `SignInState.kt` → `state/SignInUiState.kt`
- [x] `EntriesState.kt` → `state/EntriesUiState.kt`

#### Updates
- [ ] Create `state/` directory in each presentation package
- [ ] Move state files into `state/` directories
- [ ] Update all imports
- [ ] Update ViewModel references

---

### Task 2.4: Rename display_daily_entries → entries

**Priority:** Medium
**Estimated Time:** 30 minutes
**Status:** [ ] Not Started

#### Changes
- [ ] Rename directory `display_daily_entries/` → `entries/`
- [ ] Update all package declarations
- [ ] Update all imports across codebase
- [ ] Update navigation routes

---

### Task 2.5: Create state/ Directory Structure

**Priority:** Medium
**Estimated Time:** 15 minutes
**Status:** [ ] Not Started

#### Target Structure
```
presentation/
├── add_edit_entry/
│   ├── AddEditEntryScreen.kt
│   ├── AddEditEntryViewModel.kt
│   └── state/
│       ├── AddEditEntryUiState.kt
│       ├── AddEditEntryAction.kt
│       └── AddEditEntryUiEvent.kt
├── entries/
│   └── state/
│       ├── EntriesUiState.kt
│       └── EntriesAction.kt
└── mood_color_manager/
    └── state/
        ├── MoodColorManagerUiState.kt
        ├── MoodColorManagerAction.kt
        └── MoodColorManagerUiEvent.kt
```

---

### Task 2.6: Remove AddEditMoodColorViewModel

**Priority:** High
**Estimated Time:** 30 minutes
**Status:** [ ] Not Started

#### Rationale
After converting to dialog with callback pattern, ViewModel is no longer needed.

#### Changes
- [ ] Delete `AddEditMoodColorViewModel.kt`
- [ ] Remove from Koin DI modules
- [ ] Remove all references in screens
- [ ] Verify navigation doesn't reference it

---

### Task 2.7: Create MoodColorManagerScreen

**Priority:** High
**Estimated Time:** 2-3 hours (with UI improvements)
**Status:** [ ] Not Started

See [MoodColorManagerScreen Design](#moodcolormanagerscreen-design) in Screen Designs section.

---

### Task 2.8: Update DI Modules

**Priority:** Medium
**Estimated Time:** 30 minutes
**Status:** [ ] Not Started

#### Changes
- [ ] Add MoodColorManagerViewModel to ViewModelModules
- [ ] Remove AddEditMoodColorViewModel
- [ ] Verify all ViewModels properly injected
- [ ] Update module documentation

---

### Task 2.9: Update Navigation

**Priority:** Medium
**Estimated Time:** 30 minutes
**Status:** [ ] Not Started

#### Changes
- [ ] Remove AddEditMoodColorScreen route
- [ ] Add MoodColorManagerScreen route
- [ ] Update routes after package renames
- [ ] Verify all navigation flows work
- [ ] Add navigation from EntriesScreen to MoodColorManagerScreen

---

## Part B: UI Modernization

### Task 2.10: Implement Material3 TopAppBar Everywhere

**Priority:** HIGH
**Estimated Time:** 2 hours
**Status:** [ ] Not Started

#### Screens to Update
1. **SignInScreen** - No TopBar needed (full screen branding)
2. **EntriesScreen** - Main TopBar with actions
3. **AddEditEntryScreen** - TopBar with back + save
4. **MoodColorManagerScreen** - TopBar with back

#### EntriesScreen TopBar Design
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EntriesTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onManageMoods: () -> Unit,
    onSettings: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "The Day To",
                style = MaterialTheme.typography.headlineMedium
            )
        },
        actions = {
            IconButton(onClick = onManageMoods) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Manage Mood Colors"
                )
            }
            IconButton(onClick = onSettings) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Settings"
                )
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}
```

#### AddEditEntryScreen TopBar Design
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditEntryTopBar(
    isEditing: Boolean,
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
    canSave: Boolean
) {
    TopAppBar(
        title = {
            Text(if (isEditing) "Edit Entry" else "New Entry")
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            IconButton(
                onClick = onSave,
                enabled = canSave
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Save"
                )
            }
        }
    )
}
```

#### Implementation Checklist
- [ ] Add `ExperimentalMaterial3Api` opt-in
- [ ] Create TopBar composables for each screen
- [ ] Implement scroll behavior where appropriate
- [ ] Add proper colors using theme
- [ ] Add content descriptions for accessibility
- [ ] Remove old manual TopBar implementations
- [ ] Test on different screen sizes

---

### Task 2.11: Modernize Calendar UI

**Priority:** HIGH
**Estimated Time:** 3 hours
**Status:** [ ] Not Started

See [EntriesScreen Calendar Design](#entriesscreen-calendar-design) for detailed specifications.

#### Key Features
- [ ] Day names header (Mon, Tue, Wed, Thu, Fri, Sat, Sun)
- [ ] Current day indicator with outline
- [ ] Card-based day cells with elevation
- [ ] Empty days show day number
- [ ] Entry days show colored card
- [ ] Ripple effect on clickable days
- [ ] Month/Year selector with dropdown
- [ ] Smooth month transitions
- [ ] Empty state when no entries
- [ ] Loading state while fetching

#### Component Structure
```kotlin
@Composable
fun CalendarGrid(
    currentDate: LocalDate,
    entries: List<DailyEntry>,
    onDayClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Day names header
        DayNamesHeader()

        // Calendar grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Calendar days
            items(daysInMonth) { dayIndex ->
                CalendarDayCard(
                    day = dayIndex + 1,
                    entry = entries.find { it.date == dayDate },
                    isToday = dayDate == today,
                    onClick = { onDayClick(dayDate) }
                )
            }
        }
    }
}
```

---

### Task 2.12: Redesign Entry Creation Screen

**Priority:** HIGH
**Estimated Time:** 2.5 hours
**Status:** [ ] Not Started

See [AddEditEntryScreen Design](#addeditentryscreen-design) for detailed specifications.

#### Card-Based Layout
```kotlin
@Composable
private fun AddEditEntryContent(
    state: AddEditEntryUiState,
    onAction: (AddEditEntryAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date Selection Card
        item {
            DateSelectionCard(
                selectedDate = state.entryDate,
                onDateSelected = { onAction(AddEditEntryAction.EnterDate(it)) }
            )
        }

        // Mood Selection Card
        item {
            MoodSelectionCard(
                selectedMood = state.entryMood,
                selectedColor = state.entryColor,
                onMoodSelected = { mood, color ->
                    onAction(AddEditEntryAction.EnterMood(mood))
                    onAction(AddEditEntryAction.EnterColor(color))
                },
                onCreateNewMood = { /* Show dialog */ }
            )
        }

        // Notes Card
        item {
            NotesCard(
                notes = state.entryContent,
                onNotesChanged = { onAction(AddEditEntryAction.EnterContent(it)) }
            )
        }
    }
}
```

#### Implementation Checklist
- [ ] Create DateSelectionCard with Material3 DatePicker
- [ ] Create MoodSelectionCard with dropdown + preview
- [ ] Create NotesCard with multi-line text field
- [ ] Add validation feedback
- [ ] Add loading state when saving
- [ ] Show success feedback on save
- [ ] Handle error states gracefully

---

### Task 2.13: Create Material3 MoodColorPickerDialog

**Priority:** HIGH
**Estimated Time:** 2 hours
**Status:** [ ] Not Started

*See Task 2.1 for implementation details*

#### Enhanced Features
- [ ] Full-screen dialog on small screens
- [ ] Modal dialog on tablets
- [ ] Live preview with animated color changes
- [ ] Hex color input with validation
- [ ] Recently used colors
- [ ] Preset mood colors
- [ ] Accessibility: color contrast warnings

---

### Task 2.14: Design MoodColorManagerScreen UI

**Priority:** HIGH
**Estimated Time:** 3 hours
**Status:** [ ] Not Started

See [MoodColorManagerScreen Design](#moodcolormanagerscreen-design) for detailed specifications.

#### Grid Layout
```kotlin
@Composable
private fun MoodColorGrid(
    moodColors: List<MoodColor>,
    onEdit: (MoodColor) -> Unit,
    onDelete: (MoodColor) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        items(moodColors, key = { it.id ?: it.mood }) { moodColor ->
            MoodColorCard(
                moodColor = moodColor,
                onEdit = { onEdit(moodColor) },
                onDelete = { onDelete(moodColor) }
            )
        }
    }
}
```

#### Implementation Checklist
- [ ] Grid of mood color cards
- [ ] Each card shows mood name + color
- [ ] Edit/Delete actions on each card
- [ ] FAB to add new mood
- [ ] Empty state with illustration
- [ ] Search functionality in TopBar
- [ ] Swipe to delete with undo
- [ ] Confirmation dialog for delete
- [ ] Loading skeleton while fetching

---

### Task 2.15: Improve Entry List Display

**Priority:** MEDIUM
**Estimated Time:** 2 hours
**Status:** [ ] Not Started

#### Card-Based Entry Items
```kotlin
@Composable
fun EntryListItem(
    entry: DailyEntry,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = entry.mood,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            supportingContent = {
                Text(
                    text = entry.content.takeIf { it.isNotBlank() }
                        ?: "No notes",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingContent = {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = getColor(entry.color),
                            shape = CircleShape
                        )
                )
            },
            trailingContent = {
                Text(
                    text = formatDate(entry.dateStamp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        )
    }
}
```

#### Implementation Checklist
- [ ] Replace basic rows with Material3 Cards
- [ ] Use ListItem for consistent layout
- [ ] Show mood color as leading icon
- [ ] Display date and preview text
- [ ] Add swipe to delete/edit
- [ ] Group by week with section headers
- [ ] Animate list changes
- [ ] Add empty state

---

### Task 2.16: Add Loading & Empty States

**Priority:** MEDIUM
**Estimated Time:** 1.5 hours
**Status:** [ ] Not Started

#### Components Needed
```kotlin
@Composable
fun LoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    icon: ImageVector,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Oops!",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Try Again")
        }
    }
}
```

#### Empty State Messages
- **No Entries**: "No entries yet\nStart tracking your mood today!"
- **No Mood Colors**: "No mood colors yet\nCreate your first mood color to get started"
- **No Entries This Month**: "No entries for {month}\nTap a day to add your mood"
- **Search No Results**: "No results found\nTry a different search term"

#### Implementation Checklist
- [ ] Create common state components
- [ ] Add to all screens
- [ ] Handle loading states in ViewModels
- [ ] Show appropriate empty states
- [ ] Add retry mechanism for errors
- [ ] Test with slow network
- [ ] Test with no data
- [ ] Verify error messages are helpful

---

## Material Design 3 Guidelines

### Color System
```kotlin
// Use MaterialTheme.colorScheme
primary              // Primary brand color
onPrimary            // Text/icons on primary
primaryContainer     // Containers using primary
onPrimaryContainer   // Content on primaryContainer

secondary            // Secondary brand color
tertiary             // Tertiary brand color

surface              // Surface background
onSurface            // Content on surface
surfaceVariant       // Variant surface
onSurfaceVariant     // Content on surfaceVariant

error                // Error color
onError              // Content on error

outline              // Border/divider color
outlineVariant       // Subtle borders
```

### Typography Scale
```kotlin
// Headlines
displayLarge   // 57sp
displayMedium  // 45sp
displaySmall   // 36sp
headlineLarge  // 32sp
headlineMedium // 28sp
headlineSmall  // 24sp

// Titles
titleLarge     // 22sp
titleMedium    // 16sp (medium weight)
titleSmall     // 14sp (medium weight)

// Body
bodyLarge      // 16sp
bodyMedium     // 14sp
bodySmall      // 12sp

// Labels
labelLarge     // 14sp (medium weight)
labelMedium    // 12sp (medium weight)
labelSmall     // 11sp (medium weight)
```

### Spacing System
Follow 4dp base unit:
```kotlin
4.dp   // XXSmall - compact spacing
8.dp   // XSmall - tight spacing
12.dp  // Small - comfortable minimum
16.dp  // Medium - standard spacing (most common)
24.dp  // Large - section spacing
32.dp  // XLarge - major section breaks
48.dp  // XXLarge - screen sections
```

### Elevation
```kotlin
// Card elevation
Level0 = 0.dp     // Flat surface
Level1 = 1.dp     // Slightly raised
Level2 = 3.dp     // Standard raised (cards)
Level3 = 6.dp     // Emphasized
Level4 = 8.dp     // Highly emphasized
Level5 = 12.dp    // Modal/dialog
```

### Touch Targets
- **Minimum**: 48dp × 48dp
- **Preferred**: 48dp × 48dp or larger
- **Icon buttons**: 48dp container with 24dp icon
- **Text buttons**: 48dp height minimum

---

## Component Specifications

### Material3 TopAppBar

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardTopAppBar(
    title: String,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = navigationIcon ?: {},
        actions = actions,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}
```

### Material3 Card

```kotlin
@Composable
fun StandardCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        onClick = onClick ?: {},
        modifier = modifier,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp,
            hoveredElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        content()
    }
}
```

### Material3 ListItem

```kotlin
@Composable
fun StandardListItem(
    headlineText: String,
    supportingText: String? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = headlineText,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = supportingText?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        modifier = Modifier.clickable(onClick = onClick)
    )
}
```

---

## Screen Designs

### EntriesScreen Calendar Design

```
┌─────────────────────────────────────┐
│ The Day To          [🎨] [⋮]       │ ← TopAppBar with actions
├─────────────────────────────────────┤
│                                     │
│  October 2025              [▼]     │ ← Month/Year selector
│                                     │
│  Mon Tue Wed Thu Fri Sat Sun       │ ← Day names (fixed header)
│  ┌───┬───┬───┬───┬───┬───┬───┐   │
│  │   │   │ 1 │ 2 │ 3 │ 4 │ 5 │   │ ← Empty days
│  ├───┼───┼───┼───┼───┼───┼───┤   │
│  │ 6 │ 7 │🟢 │🔵│⊙ │ 11│ 12│   │ ← Entry days
│  │   │   │ 8 │ 9 │10 │   │   │   │   + today (⊙)
│  ├───┼───┼───┼───┼───┼───┼───┤   │
│  │ 13│ 14│ 15│ 16│ 17│ 18│ 19│   │
│  ├───┼───┼───┼───┼───┼───┼───┤   │
│  │ 20│ 21│ 22│ 23│ 24│ 25│ 26│   │
│  ├───┼───┼───┼───┼───┼───┼───┤   │
│  │ 27│ 28│ 29│ 30│ 31│   │   │   │
│  └───┴───┴───┴───┴───┴───┴───┘   │
│                                     │
│  Recent Entries                     │ ← Section header
│  ┌─────────────────────────────┐  │
│  │ 🟢 Happy - Wed, Oct 10      │  │ ← Entry card
│  │    Had a great day!         │  │
│  └─────────────────────────────┘  │
│  ┌─────────────────────────────┐  │
│  │ 🔵 Calm - Tue, Oct 9        │  │
│  │    Relaxing evening         │  │
│  └─────────────────────────────┘  │
│                                     │
│                            [+]      │ ← FAB
└─────────────────────────────────────┘
```

#### Day States
- **Empty Day**: White card with day number, no elevation
- **Entry Day**: Colored card with day number, elevation 2dp
- **Today**: Outlined border (2dp primary color)
- **Selected**: Pressed state with elevation 4dp
- **Past Month**: 40% opacity
- **Future Date**: Disabled, 20% opacity

---

### AddEditEntryScreen Design

```
┌─────────────────────────────────────┐
│ ← New Entry                   [✓]  │ ← TopAppBar
├─────────────────────────────────────┤
│                                     │
│  ┌────────────────────────────────┐│
│  │ 📅 Date                        ││ ← Date card
│  │                                ││
│  │  Wednesday, October 23, 2025   ││
│  │                         [📅]   ││ ← Opens DatePicker
│  └────────────────────────────────┘│
│                                     │
│  ┌────────────────────────────────┐│
│  │ 😊 Mood                        ││ ← Mood card
│  │                                ││
│  │  [Happy ▼]          🟢         ││ ← Dropdown + preview
│  │                                ││
│  │  [+ Create New Mood]           ││ ← Opens dialog
│  └────────────────────────────────┘│
│                                     │
│  ┌────────────────────────────────┐│
│  │ 📝 Notes (Optional)            ││ ← Notes card
│  │                                ││
│  │  ┌──────────────────────────┐ ││
│  │  │ How was your day?        │ ││ ← Multi-line field
│  │  │                          │ ││
│  │  │                          │ ││
│  │  └──────────────────────────┘ ││
│  └────────────────────────────────┘│
│                                     │
└─────────────────────────────────────┘
```

---

### MoodColorManagerScreen Design

```
┌─────────────────────────────────────┐
│ ← Mood Colors              [🔍]    │ ← TopAppBar with search
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────┐  ┌─────────────┐ │
│  │ 🟢          │  │ 🔵          │ │ ← Grid of mood cards
│  │             │  │             │ │
│  │   Happy     │  │   Calm      │ │
│  │             │  │             │ │
│  │    [✏️] [🗑️]│  │    [✏️] [🗑️]│ │
│  └─────────────┘  └─────────────┘ │
│                                     │
│  ┌─────────────┐  ┌─────────────┐ │
│  │ 🔴          │  │ 🟡          │ │
│  │             │  │             │ │
│  │   Angry     │  │  Excited    │ │
│  │             │  │             │ │
│  │    [✏️] [🗑️]│  │    [✏️] [🗑️]│ │
│  └─────────────┘  └─────────────┘ │
│                                     │
│  ┌─────────────┐  ┌─────────────┐ │
│  │ 🟣          │  │ 🟤          │ │
│  │             │  │             │ │
│  │   Sad       │  │   Anxious   │ │
│  │             │  │             │ │
│  │    [✏️] [🗑️]│  │    [✏️] [🗑️]│ │
│  └─────────────┘  └─────────────┘ │
│                                     │
│                            [+]      │ ← FAB
└─────────────────────────────────────┘
```

**Empty State:**
```
┌─────────────────────────────────────┐
│ ← Mood Colors                       │
├─────────────────────────────────────┤
│                                     │
│            🎨                       │ ← Large icon
│                                     │
│      No Mood Colors Yet             │ ← Title
│                                     │
│  Create your first mood color      │ ← Message
│  to start tracking your emotions   │
│                                     │
│      [Create Mood Color]            │ ← Action button
│                                     │
│                            [+]      │ ← FAB
└─────────────────────────────────────┘
```

---

## Session Tracking

### Session 1: [Date]
**Duration:** [hours]
**Status:** Not Started

**Tasks Completed:**
- [ ] Task X.X
- [ ] Task X.X

**Notes:**

**Next Session:**

---

### Session 2: [Date]
**Duration:** [hours]
**Status:** Not Started

**Tasks Completed:**
- [ ] Task X.X
- [ ] Task X.X

**Notes:**

**Next Session:**

---

### Session 3: [Date]
**Duration:** [hours]
**Status:** Not Started

**Tasks Completed:**
- [ ] Task X.X
- [ ] Task X.X

**Notes:**

**Next Session:**

---

## Testing Checklist

### Visual Testing
- [ ] Test on small phone (320dp width)
- [ ] Test on regular phone (360dp width)
- [ ] Test on large phone (420dp width)
- [ ] Test on tablet (600dp+ width)
- [ ] Test light theme
- [ ] Test dark theme
- [ ] Test dynamic color (Android 12+)
- [ ] Test with large text (accessibility)
- [ ] Test RTL layout (if supporting)

### Interaction Testing
- [ ] All buttons have 48dp touch targets
- [ ] Ripple effects work on all clickable items
- [ ] Navigation works correctly
- [ ] Back button behavior correct
- [ ] Dialogs dismiss properly
- [ ] Forms validate correctly
- [ ] Loading states show during async operations
- [ ] Empty states show when no data
- [ ] Error states show on failures
- [ ] Retry mechanism works

### Accessibility Testing
- [ ] All images have content descriptions
- [ ] All buttons have labels
- [ ] Form fields have labels
- [ ] Color contrast meets WCAG AA
- [ ] Focus indicators visible
- [ ] Screen reader announces correctly
- [ ] Semantic ordering correct
- [ ] No text smaller than 12sp

---

## Resources

### Material Design 3
- [Material Design 3](https://m3.material.io/)
- [Material Components](https://m3.material.io/components)
- [Material Theme Builder](https://m3.material.io/theme-builder)
- [Material Motion](https://m3.material.io/styles/motion)

### Jetpack Compose
- [Compose Material3](https://developer.android.com/jetpack/androidx/releases/compose-material3)
- [Compose Layouts](https://developer.android.com/jetpack/compose/layouts)
- [Compose Theming](https://developer.android.com/jetpack/compose/designsystems/material3)
- [Compose Accessibility](https://developer.android.com/jetpack/compose/accessibility)

### Design Inspiration
- [My-Bookshelf](https://github.com/Zlurgg/My-Bookshelf)
- [Now in Android](https://github.com/android/nowinandroid)
- [Material Design Gallery](https://m3.material.io/components)

---

**Last Updated:** 2025-10-24
**Version:** 2.0
**Status:** Part A Complete ✅ | Part B (UI) Ready for Next Session
