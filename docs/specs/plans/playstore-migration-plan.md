# Play Store Migration Plan

## Overview
Migrate from GitHub Releases distribution to Google Play Store Internal Testing track.

**Current State:** GitHub releases with in-app APK update mechanism
**Target State:** Play Store distribution with automatic updates

---

## Pre-requisites Checklist

| Item | Status | Notes |
|------|--------|-------|
| Google Play Developer Account | Required | $25 one-time fee |
| Privacy Policy URL | Required | Must be publicly accessible |
| App Icon (512x512) | Required | PNG, 32-bit with alpha |
| Feature Graphic (1024x500) | Required | PNG or JPEG |
| Screenshots (phone) | Required | Min 2, max 8 |
| Account Deletion Feature | Done | See `account-deletion-plan.md` |

---

## Phase 1: Remove GitHub Update Feature

### 1.1 Remove Startup Check
**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/ui/overview/OverviewViewModel.kt`

Delete the update check call (will be removed with module deletion):
```kotlin
// DELETE: checkForUpdates(forceCheck = false)
```

### 1.2 Remove "Check for Updates" Menu Option
**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/ui/overview/OverviewScreen.kt`

Remove or hide the menu item that triggers `OverviewAction.CheckForUpdates`.

### 1.3 Delete Update Module
Delete the entire `update/` module. Dead code for "future use" violates YAGNI - if needed later, revert from git history.

**Files to delete:**
- `app/src/main/java/uk/co/zlurgg/thedayto/update/` (entire directory)
- Remove from `app/build.gradle.kts` if module dependency exists
- Remove from Koin DI modules

**Impact:** ~10 files deleted, 2 files modified

### 1.4 Update About Dialog
**File:** `app/src/main/res/values/strings.xml`

Remove GitHub update references from the About dialog description:

```xml
<!-- BEFORE -->
<string name="about_dialog_description_content">...The app checks for updates automatically and lets you download new versions directly from GitHub.</string>

<!-- AFTER -->
<string name="about_dialog_description_content">...Available on Google Play.</string>
```

**Impact:** 1 file modified

---

## Phase 2: Account Deletion Feature

See detailed plan: `docs/specs/plans/account-deletion-plan.md`

**Summary:**
- Add `DeleteAccountUseCase` to orchestrate deletion
- Add `deleteAccount()` to `AuthRepository`
- Add confirmation dialog to Account screen
- Delete Firestore data, Firebase Auth, and local data

**Partial Failure Note:** If Firestore deletion succeeds but Auth deletion fails, data is lost but account remains. User can retry; orphaned account is harmless. This tradeoff is accepted for the free Firebase tier.

**Impact:** 10 files modified, 2 new files, ~300 lines

---

## Phase 3: Privacy Policy

### 3.1 Create Privacy Policy
Host at: `https://zlurgg.github.io/the-day-to/privacy` (GitHub Pages)

**Required Disclosures:**

| Data Type | Collection | Usage | Sharing |
|-----------|------------|-------|---------|
| Email address | Google Sign-In | Account identification | Firebase Auth (Google) |
| Journal entries | User input | Core app functionality | Firebase Firestore (if sync enabled) |
| Mood colors | User input | Personalization | Firebase Firestore (if sync enabled) |
| Notification preferences | User settings | Daily reminders | None |

**Template Sections:**
1. Information We Collect
2. How We Use Your Information
3. Data Storage and Security
4. Third-Party Services (Firebase)
5. Your Rights (access, deletion, export)
6. Contact Information
7. Changes to This Policy

### 3.2 Add String Resource
**File:** `app/src/main/res/values/strings.xml`

```xml
<string name="privacy_policy_url">https://zlurgg.github.io/the-day-to/privacy</string>
<string name="privacy_policy">Privacy Policy</string>
```

### 3.3 Add In-App Privacy Policy Link
**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/ui/overview/OverviewScreen.kt`

Add to About dialog or Settings (using string resource to avoid DRY violation):
```kotlin
val privacyUrl = stringResource(R.string.privacy_policy_url)
TextButton(onClick = { uriHandler.openUri(privacyUrl) }) {
    Text(stringResource(R.string.privacy_policy))
}
```

**Impact:** 2 files modified, privacy policy hosted externally

---

## Phase 4: SHA Key Configuration

### 4.1 Get Current Upload Key SHA-1
```bash
keytool -list -v -keystore <path-from-keystore.properties> -alias <key-alias>
```

### 4.2 Enable Play App Signing (Recommended)
1. Go to Play Console → App → Setup → App signing
2. Choose "Let Google manage and protect your app signing key"
3. Upload your current keystore as the upload key
4. Google generates and manages the app signing key

### 4.3 Get Play Store SHA-1 Fingerprints
After enabling Play App Signing, Play Console provides:
- **App signing key certificate** (SHA-1) - used for installed apps
- **Upload key certificate** (SHA-1) - your keystore

### 4.4 Add SHA-1 to Firebase
1. Firebase Console → Project Settings → Your apps → Android app
2. Add fingerprint: paste the **App signing key SHA-1**
3. Download updated `google-services.json`

### 4.5 Replace google-services.json
**File:** `app/google-services.json`

Replace with downloaded file containing the new OAuth client.

**Note:** See Firebase Console → Project Settings → Your apps for current SHA-1 fingerprints. After Play App Signing is enabled, add the Play Store app signing key fingerprint.

---

## Phase 5: App Assets

### 5.1 App Icon (512x512 PNG)
**Location:** `app/src/main/res/` (launcher icons) + Play Console upload

**AI Generation Prompt (Gemini/DALL-E):**
```
Minimalist app icon for a mood journaling app called "The Day To".
Clean, modern design. Soft gradient background (warm sunset colors -
coral to soft orange, or calming blues). Simple icon element: a
stylized open book/journal with a subtle heart or smile curve.
Material Design 3 aesthetic. No text. Square with slight rounded
corners. High contrast for visibility at small sizes. Flat design,
no shadows or 3D effects.
```

**Sizes needed:**
- 512x512 (Play Store)
- 192x192 (xxxhdpi)
- 144x144 (xxhdpi)
- 96x96 (xhdpi)
- 72x72 (hdpi)
- 48x48 (mdpi)

### 5.2 Feature Graphic (1024x500 PNG/JPEG)
**AI Generation Prompt:**
```
Feature graphic banner for "The Day To" mood journaling app.
Landscape 1024x500 pixels. App name "The Day To" prominently
displayed in modern sans-serif font. Warm, inviting gradient
background (sunset colors or soft pastels). Subtle decorative
elements: calendar page, journal icon, or mood emoticons.
Tagline: "Track your day, understand your mood". Clean,
professional Material Design aesthetic. No phone mockups.
```

### 5.3 Screenshots
Capture from device/emulator:
1. Overview screen with entries
2. Entry editor with mood selection
3. Calendar view
4. Account/sync screen
5. Notification settings

**Tool:** Android Studio screenshot capture or `adb shell screencap`

---

## Phase 6: Build Configuration

### 6.1 Version Bump
**File:** `app/build.gradle.kts`

```kotlin
versionCode = 11  // Increment for each upload
versionName = "1.1.0"  // Semantic version
```

### 6.2 Build Release Bundle
```bash
./gradlew bundleRelease
```

**Output:** `app/build/outputs/bundle/release/app-release.aab`

### 6.3 Test Release Build Locally
Before uploading to Play Console, install and test the release build locally:

```bash
# Build APKs from bundle for local testing
bundletool build-apks --bundle=app/build/outputs/bundle/release/app-release.aab --output=test.apks --local-testing

# Install on connected device
bundletool install-apks --apks=test.apks
```

**Verify:**
- [ ] App launches without crash
- [ ] Google Sign-In works with release signing key
- [ ] Firestore sync works
- [ ] ProGuard hasn't broken Firebase reflection

### 6.4 Verify Signing
```bash
# Check bundle is signed
bundletool validate --bundle=app/build/outputs/bundle/release/app-release.aab
```

---

## Phase 7: Play Console Setup

### 7.1 Create App
1. Play Console → Create app
2. App name: "The Day To"
3. Default language: English (UK)
4. App or game: App
5. Free or paid: Free

### 7.2 Store Listing
| Field | Value |
|-------|-------|
| App name | The Day To |
| Short description | Daily mood journal - track how you feel |
| Full description | (Detailed app description) |
| App icon | 512x512 PNG |
| Feature graphic | 1024x500 |
| Screenshots | Min 2 phone screenshots |
| App category | Lifestyle or Health & Fitness |
| Contact email | (Your email) |
| Privacy policy | https://zlurgg.github.io/the-day-to/privacy |

### 7.3 Content Rating
Complete IARC questionnaire:
- Violence: None
- Sexual content: None
- Language: None
- Controlled substances: None
- User interaction: No (local-first, optional cloud sync)

**Note:** IARC "user interaction" refers to features letting users communicate or share with others. Pure personal journaling with no social features qualifies as "No". Verify exact wording when filling out the questionnaire.

### 7.4 Data Safety Form

| Question | Answer |
|----------|--------|
| Does your app collect or share user data? | Yes |
| **Data types collected:** | |
| - Personal info (email) | Yes - Account creation |
| - App activity (entries) | Yes - App functionality |
| Is data encrypted in transit? | Yes |
| Can users request data deletion? | Yes |
| **Data sharing:** | |
| - Analytics | No |
| - Advertising | No |
| - Account management | Yes (Firebase) |

**Account deletion URL:** (Optional)
If in-app deletion is implemented, select "Users can delete their account in the app" in Data Safety.
A separate web page (`https://zlurgg.github.io/the-day-to/delete-account`) is only needed if you want a support fallback.

### 7.5 Target Audience
- Target age group: 18+ (simplest compliance)
- Not designed for children

---

## Phase 8: Internal Testing

### 8.1 Create Internal Testing Track
1. Play Console → Testing → Internal testing
2. Create new release
3. Upload `app-release.aab`
4. Add release notes

### 8.2 Add Testers
1. Create email list or use Google Group
2. Add tester emails
3. Share opt-in link with testers

### 8.3 Publish to Internal Track
1. Review release
2. Start rollout to internal testing
3. Testers receive email with install link

---

## Phase 9: Closed Testing (Optional but Recommended)

Before production, consider a Closed Testing track for broader validation:

### 9.1 Why Closed Testing?
- Internal testing limited to 100 users
- Closed testing supports larger audience
- Catches issues at broader scale before production
- Can target specific countries or user groups

### 9.2 Create Closed Testing Track
1. Play Console → Testing → Closed testing
2. Create new track (e.g., "Beta")
3. Promote release from Internal Testing
4. Add broader tester list

### 9.3 Promotion to Production
1. Verify no crashes in Play Console vitals
2. Review user feedback
3. Promote to Production track

---

## Implementation Order

| Priority | Phase | Dependencies | Effort | Status |
|----------|-------|--------------|--------|--------|
| 1 | Phase 1: Remove update module | None | Small | Done |
| 2 | Phase 3: Privacy policy | None | Medium | Done |
| 3 | Phase 2: Account deletion | None | Large | Done |
| 4 | Phase 5: App assets | None | Medium | Done |
| 5 | Phase 4: SHA key config | Play Console account | Small | |
| 6 | Phase 6: Build & test AAB | Priority 1, 3 | Small | |
| 7 | Phase 7: Play Console setup | Priority 2, 4, 5 | Medium | Done |
| 8 | Phase 8: Internal testing | Priority 6, 7 | Small | |
| 9 | Phase 9: Closed testing | Priority 8 | Small | |

**Recommended parallel tracks:**
- Track A: Code changes (Phases 1, 3)
- Track B: Assets & Policy (Phases 2, 4)
- Track C: Console setup (Phases 5, 7, 8, 9)

---

## Files Modified Summary

### Code Changes
| File | Phase | Change |
|------|-------|--------|
| `update/` directory | 1 | Delete entire module |
| `OverviewViewModel.kt` | 1 | Remove update check |
| `OverviewScreen.kt` | 1, 3 | Remove update menu, add privacy link |
| `strings.xml` | 3 | Add privacy policy strings |
| `build.gradle.kts` | 6 | Version bump, remove update module if needed |
| `google-services.json` | 4 | Add Play Store SHA |
| + Account deletion files | 2 | See `account-deletion-plan.md` |

### External Deliverables
| Item | Phase | Required? |
|------|-------|-----------|
| Privacy policy page | 3 | Yes |
| Account deletion page | 2 | Optional (if in-app works) |
| App icon (512x512) | 5 | Yes |
| Feature graphic (1024x500) | 5 | Yes |
| Screenshots (min 2) | 5 | Yes |

---

## Verification Checklist

### Before Upload
- [x] Update module deleted (not just disabled)
- [x] Account deletion working
- [x] Privacy policy live and accessible
- [ ] SHA key added to Firebase
- [ ] Version code incremented
- [ ] Release AAB builds successfully
- [ ] Release build tested locally via bundletool
- [ ] Google Sign-In works in release build
- [ ] Firestore sync works in release build
- [x] App icon looks good at all sizes

### After Internal Testing
- [ ] Testers can install from Play Store
- [ ] Google Sign-In works
- [ ] Sync works (Firestore)
- [ ] Account deletion works
- [ ] Notifications work
- [ ] No crashes in Play Console

---

## Rollback Plan

If issues arise after Play Store release:
1. **Code issues:** Fix and upload new AAB with incremented versionCode
2. **Signing issues:** Cannot change signing key - contact Play support
3. **Account issues:** Can unpublish app temporarily

---

## Pre-Implementation Verification

Before starting implementation, verify these assumptions:

### 1. Firestore Security Rules
Verify rules allow users to delete their own data: **Done** - Updated in `firestore.rules`
```javascript
// Users can delete documents where request.auth.uid matches
match /users/{userId}/{collection}/{docId} {
  allow delete: if request.auth != null && request.auth.uid == userId;
}
```

### 2. Firebase SDKs Audit
Check for implicit Firebase SDKs that affect Data Safety form:
```bash
./gradlew :app:dependencies | grep firebase
```

**Expected:** Only `firebase-auth`, `firebase-firestore`
**If present:** Crashlytics, Analytics, Performance - must declare in Data Safety

### 3. ProGuard/R8 Configuration
Verify release build doesn't break Firebase reflection:
- Check `proguard-rules.pro` for Firebase keep rules
- Test Google Sign-In in release build before uploading
