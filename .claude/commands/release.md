# Release Command

Create a new release for The Day To app.

## Arguments
Version number: $ARGUMENTS (e.g., "1.0.3")

## Instructions

1. **Validate version format** - Must be semver (x.y.z)

2. **Update version numbers**:
   - `app/build.gradle.kts`: Increment `versionCode` by 1, set `versionName` to the new version
   - `ARCHITECTURE.md`: Update "Current version" in the Metrics section

3. **Build release APK**:
   - Run `./gradlew assembleRelease`
   - Verify build succeeds
   - Report APK size

4. **Generate changelog**:
   - Get commits since the last tag using `git log`
   - Summarize changes into categories: Features, Improvements, Bug Fixes

5. **Create GitHub release**:
   - Use `gh release create` with the version tag
   - Attach the APK from `app/build/outputs/apk/release/app-release.apk`
   - Include the generated changelog

6. **Report completion**:
   - Show the release URL
   - Remind user to commit and push the version changes

If no version is provided, show the current version and ask what the new version should be.
