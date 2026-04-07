# Code Review Plan - Remaining Items

## Overview

Remaining items from the internal testing code review.

**Review Date:** April 2025
**App Version:** v1.0.9

---

## Priority 1: Database Migration Strategy - COMPLETE

Reset database to v1 baseline, removed destructive fallback.

| Status | Priority | Effort |
|--------|----------|--------|
| [x] | 1 | Small |

---

## Priority 2: Enum.valueOf() Crash Risk - COMPLETE

Added `toSyncStatusOrDefault()` extension, applied to both mappers.

| Status | Priority | Effort |
|--------|----------|--------|
| [x] | 2 | Small |

---

## Priority 3: Accessibility Gaps - COMPLETE

**Files reviewed:**
- `StateMessagePanel.kt` - Icon is decorative (title/message convey info), `null` correct
- `LoadErrorBanner.kt` - Error icon decorative, Close icon already has contentDescription ✓
- `SignInButton.kt` - Login icon decorative (button text conveys action), `null` correct

**Result:** No changes needed - all icons properly configured.

| Status | Priority | Effort |
|--------|----------|--------|
| [x] | 3 | N/A |

---

## Priority 4: UseCase Tests (Logic Only)

Only 4 UseCases have actual logic worth testing:

---

### 4.1 SaveNotificationSettingsUseCaseTest

**File:** `app/src/test/java/uk/co/zlurgg/thedayto/core/domain/usecases/notifications/SaveNotificationSettingsUseCaseTest.kt`

**Logic to test:**
- Validation: hour 0-23, minute 0-59
- User ID resolution: signed-in user vs ANONYMOUS_USER_ID
- Conditional scheduling: calls `updateNotificationTime()` when enabled, `cancelNotifications()` when disabled
- Error propagation from repository

**Test cases:**
```
invoke saves settings for signed-in user
invoke saves settings for anonymous user when not signed in
invoke schedules notification when enabled
invoke cancels notification when disabled
invoke returns error when repository fails
invoke throws on invalid hour
invoke throws on invalid minute
```

---

### 4.2 GetNotificationSettingsUseCaseTest

**File:** `app/src/test/java/uk/co/zlurgg/thedayto/core/domain/usecases/notifications/GetNotificationSettingsUseCaseTest.kt`

**Logic to test:**
- User ID resolution: signed-in user vs ANONYMOUS_USER_ID
- Default settings fallback when repository returns null
- Error propagation

**Test cases:**
```
invoke returns settings for signed-in user
invoke returns settings for anonymous user when not signed in
invoke returns default settings when repository returns null
invoke returns error when repository fails
```

---

### 4.3 SeedDefaultMoodColorsUseCaseTest

**File:** `app/src/test/java/uk/co/zlurgg/thedayto/journal/domain/usecases/shared/moodcolor/SeedDefaultMoodColorsUseCaseTest.kt`

**Logic to test:**
- First launch check: only seeds on first launch
- Partial failure handling: continues on individual failures, returns error only if all fail
- reseed() bypasses first launch check

**Test cases:**
```
invoke seeds 7 default mood colors on first launch
invoke returns 0 and does nothing when not first launch
invoke returns success count on partial failure
invoke returns error when all seeds fail
reseed seeds mood colors regardless of first launch state
```

---

### 4.4 GetEntriesForMonthUseCaseTest

**File:** `app/src/test/java/uk/co/zlurgg/thedayto/journal/domain/usecases/shared/entry/GetEntriesForMonthUseCaseTest.kt`

**Logic to test:**
- Validation: month 1-12, year > 0
- Sorting: 4 combinations (Date/Mood × Ascending/Descending)

**Test cases:**
```
invoke throws on month less than 1
invoke throws on month greater than 12
invoke throws on non-positive year
invoke sorts by date ascending
invoke sorts by date descending (default)
invoke sorts by mood ascending
invoke sorts by mood descending
```

| Status | Priority | Effort |
|--------|----------|--------|
| [x] | 4 | Medium |

---

## Priority 5: Adaptive Icon Fix - COMPLETE

**Problem:** Launcher icon shows as full square on older devices (OnePlus 6T).

**Fix:** Created `mipmap-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml` that reference existing foreground/background layers.

| Status | Priority | Effort |
|--------|----------|--------|
| [x] | 5 | Small |

---

## Priority 6: Splash Screen Configuration

**Problem:** App shows white background with square PNG icon during launch on all devices.

**Files to modify:**
- `gradle/libs.versions.toml` - Add splashscreen dependency
- `app/build.gradle.kts` - Add dependency
- `app/src/main/res/values/themes.xml` - Add splash theme
- `app/src/main/res/values/colors.xml` - Add splash background color
- `app/src/main/AndroidManifest.xml` - Apply splash theme to MainActivity
- `app/src/main/java/.../MainActivity.kt` - Call installSplashScreen()

**Fix:**

1. **libs.versions.toml** - Add version and library:
```toml
[versions]
splashscreen = "1.0.1"

[libraries]
androidx-splashscreen = { module = "androidx.core:core-splashscreen", version.ref = "splashscreen" }
```

2. **build.gradle.kts** - Add dependency:
```kotlin
implementation(libs.androidx.splashscreen)
```

3. **colors.xml** - Add splash background color (match your app's primary/background):
```xml
<color name="splash_background">#C5CAE9</color>
```

4. **themes.xml** - Add splash theme:
```xml
<style name="Theme.TheDayTo.Splash" parent="Theme.SplashScreen">
    <item name="windowSplashScreenBackground">@color/splash_background</item>
    <item name="windowSplashScreenAnimatedIcon">@drawable/ic_launcher_foreground</item>
    <item name="postSplashScreenTheme">@style/Theme.TheDayTo</item>
</style>
```

5. **AndroidManifest.xml** - Change MainActivity theme:
```xml
<activity
    android:name=".MainActivity"
    android:theme="@style/Theme.TheDayTo.Splash"
    ...
```

6. **MainActivity.kt** - Install splash screen before super.onCreate():
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)
    // ...
}
```

7. **values-night/themes.xml** (optional but recommended) - Dark mode splash:
```xml
<style name="Theme.TheDayTo.Splash" parent="Theme.SplashScreen">
    <item name="windowSplashScreenBackground">@color/splash_background_dark</item>
    <item name="windowSplashScreenAnimatedIcon">@drawable/ic_launcher_foreground</item>
    <item name="postSplashScreenTheme">@style/Theme.TheDayTo</item>
</style>
```

**Notes:**
- Icon is 108dp (adaptive standard), splash expects up to 288dp - Android scales, may appear slightly smaller
- Test color pairing: #C5CAE9 background with icon's Deep Purple header

| Status | Priority | Effort |
|--------|----------|--------|
| [x] | 6 | Small |

---

## Priority 7: EditMoodColorDialog Color Picker Reset Bug

**Problem:** When editing a mood color, changing one property (mood name OR color) resets the other. User cannot change both mood name and color in a single edit operation.

**Root Cause:** In `EditMoodColorDialog.kt` line 139:
```kotlin
colorPickerController.selectByColor(initialColor, fromUser = false)
```

This line runs on **every recomposition**, not just when the dialog opens. When the user types in the mood name field, it triggers a recomposition, which resets the color picker to `initialColor`, firing `onColorChanged` and overwriting the user's color selection.

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/ui/editor/components/EditMoodColorDialog.kt`

**Fix:**

Replace the direct call (line 139):
```kotlin
colorPickerController.selectByColor(initialColor, fromUser = false)
```

With a `LaunchedEffect` that only runs once when the dialog opens:
```kotlin
LaunchedEffect(moodColor) {
    colorPickerController.selectByColor(initialColor, fromUser = false)
}
```

**Test Plan:**
1. Open editor, expand mood color section
2. Tap edit on any mood color
3. Change the mood name (e.g., "Happy" → "Joyful")
4. Change the color using the picker
5. Tap save
6. Verify BOTH name and color are updated

| Status | Priority | Effort |
|--------|----------|--------|
| [ ] | 7 | Small |

---

## Dropped Items

| Item | Reason |
|------|--------|
| 4.2 Pass-through UseCase tests | Zero value - tested implicitly through integration |
| 4.3 Hardcoded spacing values | 8.dp/12.dp are standard Material spacing - premature abstraction |

---

## Verification Checklist

- [x] Database reset to version 1, migrations cleared, fallback removed
- [x] Schema v1 exported to `app/schemas/` (generates on build)
- [x] Enum.valueOf() uses safe extension in both mappers
- [x] Adaptive icon wrapper created for proper launcher masking
- [x] Splash screen shows adaptive icon with proper background
- [x] All existing tests passing (421 unit + 46 instrumented)
- [ ] EditMoodColorDialog bug fixed
- [ ] Internal testers notified to reinstall app
