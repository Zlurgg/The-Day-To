# Constitution

Non-negotiable architectural principles for The Day To. These rules MUST be followed in all code changes.

## Core Principles

- **Local-first**: Room database is the primary data store; works without internet
- **Privacy-focused**: Journal data stored locally by default; cloud sync is OPT-IN only
- **Optional auth**: Firebase Auth for future features, not required for core functionality

## Clean Architecture

```
┌─────────────────────┐
│  Presentation Layer │ ──────┐
└─────────────────────┘       │
                              ▼
┌─────────────────────┐     ┌─────────────────────┐
│     Data Layer      │ ───▶│    Domain Layer     │
└─────────────────────┘     └─────────────────────┘
```

**Dependency Rule**: Presentation and Data depend on Domain. Domain depends on nothing.

- **Domain Layer** (`*/domain/`): Business logic, repository interfaces, UseCases, models
- **Data Layer** (`*/data/`): Repository implementations, Room entities, data sources
- **Presentation Layer** (`*/ui/`): ViewModels, Compose UI, navigation

## Clean Code Principles

### DRY (Don't Repeat Yourself)
- Extract repeated logic into UseCases or utility functions
- Share domain models across features rather than duplicating
- Use composition for common ViewModel behavior
- If you copy-paste code, refactor into a shared function

### SRP (Single Responsibility Principle)
- Each class/function does ONE thing well
- UseCases: One business operation per UseCase
- ViewModels: Manage UI state for ONE screen
- Composables: Render ONE logical UI component
- Repositories: Handle ONE data source type

### Separation of Concerns

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  • UI rendering (Composables)                               │
│  • UI state management (ViewModels)                         │
│  • User input handling                                      │
│  • Navigation                                               │
│  ✗ NO business logic, NO data access                        │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼ depends on
┌─────────────────────────────────────────────────────────────┐
│                      Domain Layer                            │
│  • Business rules (UseCases)                                │
│  • Domain models (entities, value objects)                  │
│  • Repository interfaces (contracts only)                   │
│  ✗ NO Android imports, NO UI, NO frameworks                 │
└─────────────────────────────────────────────────────────────┘
                              ▲
                              │ depends on
┌─────────────────────────────────────────────────────────────┐
│                       Data Layer                             │
│  • Repository implementations                               │
│  • Data sources (Room, preferences)                         │
│  • Data mappers (Entity ↔ Domain)                           │
│  ✗ NO UI, NO business logic beyond data transformation      │
└─────────────────────────────────────────────────────────────┘
```

## Key Rules

- Domain layer has ZERO dependencies on other layers
- Presentation NEVER imports from Data layer directly
- Data layer implements Domain interfaces, never defines them
- Business logic lives in UseCases, NOT in ViewModels or Repositories
- ViewModels orchestrate UseCases, they don't contain business rules

## Layered Dependencies (Enforced)

```
UI (Composables) → ViewModels → UseCases → Repositories → DataSources
                                    ↓
                              Domain Models
```

**Critical Rules:**
- ViewModels depend on UseCases, never repositories directly
- UseCases encapsulate all business logic
- Repositories are implementation details hidden from ViewModels

## Error Handling

All fallible operations return `Result<T, DataError>`, never throw exceptions.

```kotlin
sealed interface DataError {
    sealed interface Local : DataError {
        data object NotFound : Local
        data object DatabaseError : Local
    }
    sealed interface Network : DataError {
        data object NoConnection : Local
        data object ServerError : Network
    }
    data class Unknown(val message: String) : DataError
}
```

## Anti-patterns to Avoid

- ❌ ViewModel calling repository directly (bypasses UseCase)
- ❌ Business logic in Composables (move to ViewModel/UseCase)
- ❌ Domain model with Android imports (keep platform-agnostic)
- ❌ Repository doing business validation (that's UseCase's job)
- ❌ "God class" with multiple responsibilities (split it up)
- ❌ Using `!!` operator (use safe calls or require())
- ❌ Manual dependency instantiation (use Koin injection)
- ❌ Context in ViewModels (inject what you need instead)
