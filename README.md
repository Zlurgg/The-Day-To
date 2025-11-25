# The Day To

[![License: MIT](https://img.shields.io/badge/License-MIT-teal.svg)](https://opensource.org/licenses/MIT)

Your personal daily mood journal for Android. Track your emotional wellbeing with colors, visualize your month at a glance, and gain insights into your patterns over time.

## Features

📅 **Daily Mood Tracking**
- Log one mood entry per day with optional notes
- Color-coded calendar view showing your month at a glance
- Swipe-to-delete entries with undo support
- Navigate between months with the date picker

🎨 **Custom Mood Colors**
- Create unlimited mood-color combinations
- Rainbow color wheel picker for precise color selection
- Edit or delete existing mood colors
- Visual color indicators throughout the app

📊 **Statistics & Insights**
- All-time stats: total entries, average per month, first entry date
- Most common moods with frequency counts
- Monthly breakdown showing entries and completion percentage

🔔 **Daily Reminders**
- Configurable notification time
- Quick presets: Morning (8:00), Noon (12:00), Evening (20:00)
- Smart reminder dialog when no entry exists for today
- Auto-dismiss when entry is created

🔒 **Privacy-First**
- All data stored locally on your device (Room database)
- Google Sign-In for authentication only
- No cloud sync, no data collection, no tracking
- Your journal stays completely private

✨ **Modern UX**
- Material Design 3 with dynamic theming
- Light and dark mode support
- Smooth animations and haptic feedback
- Tutorial dialogs for first-time users
- WCAG-compliant touch targets (48dp minimum)

## Screenshots

### Welcome & Sign-In
| Welcome | Sign In |
|---------|---------|
| ![Welcome](docs/screenshots/welcome.png) | ![Sign In](docs/screenshots/signin.png) |

### Calendar Overview
| Calendar with Entries | Month Navigation |
|----------------------|------------------|
| ![Calendar](docs/screenshots/calendar.png) | ![Month Picker](docs/screenshots/month-picker.png) |

### Entry Editor
| Create Entry | Mood Selection | Color Picker |
|--------------|----------------|--------------|
| ![Editor](docs/screenshots/editor.png) | ![Mood Select](docs/screenshots/mood-select.png) | ![Color Picker](docs/screenshots/color-picker.png) |

### Statistics
| Stats Overview |
|----------------|
| ![Stats](docs/screenshots/stats.png) |

### Notifications
| Notification Settings |
|----------------------|
| ![Notifications](docs/screenshots/notifications.png) |

## Tech Stack

| Category | Technology |
|----------|------------|
| **Language** | Kotlin 2.2.21 |
| **UI** | Jetpack Compose + Material 3 |
| **Architecture** | Clean Architecture (MVVM) |
| **Database** | Room 2.8.3 |
| **DI** | Koin 4.1.1 |
| **Background Work** | WorkManager 2.11.0 |
| **Auth** | Credential Manager API (Google Sign-In) |
| **Navigation** | Jetpack Navigation Compose |
| **Logging** | Timber |

## Architecture

The app follows **Clean Architecture** with clear separation of concerns:

```
app/
├── auth/           # Authentication (sign-in/out)
├── journal/        # Core mood journaling feature
│   ├── data/       # Room entities, DAOs, repositories
│   ├── domain/     # Models, use cases, repository interfaces
│   └── ui/         # Screens, ViewModels, components
└── core/           # Shared utilities, theme, DI modules
```

**Key Patterns:**
- Unidirectional Data Flow (StateFlow + SharedFlow)
- Repository pattern with offline-first approach
- Use cases for business logic
- Root/Presenter pattern for Compose screens

## Quality

- **157 Tests** (137 unit + 20 instrumented)
- ViewModels: 100% test coverage
- Repository integration tests with real Room database
- Turbine for Flow testing

## Requirements

- Android 8.1+ (API 27)
- Google account for sign-in

## Building

```bash
# Debug build
./gradlew assembleDebug

# Run tests
./gradlew test

# Full check (lint + tests)
./gradlew check
```

## Download

Get the latest APK from the [Releases](https://github.com/Zlurgg/The-Day-To/releases) page.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author

Created by [Zlurgg](https://github.com/Zlurgg)

---

*Track your moods, understand your patterns, own your data.* 🌈
