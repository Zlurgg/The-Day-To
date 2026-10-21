# Release Command

Prepare and ship a new release of The Day To to the Play Store.

## Arguments
Version number: $ARGUMENTS (e.g., "2.2.4")

## Instructions

Releases are cut from `main`. Pushing a `vX.Y.Z` tag runs `.github/workflows/release.yml`,
which builds the signed AAB and uploads it as a **draft** to the Play Store **internal** track.
Do NOT create GitHub Releases or attach APKs — the app ships only through Google Play.

1. **Validate version format** - Must be semver (x.y.z). If no version is provided, show the
   current `versionName` from `app/build.gradle.kts` and ask what the new version should be.

2. **Write the release notes** - Summarise user-visible changes since the last tag
   (`git log $(git describe --tags --abbrev=0)..HEAD`) into `distribution/whatsnew/whatsnew-en-GB`.
   Plain text, user-facing language, max 500 characters. Leave out internal-only changes.
   Show the notes to the user for approval.

3. **Update version numbers** - In `app/build.gradle.kts`, increment `versionCode` by 1 and set
   `versionName` to the new version. The workflow reads `versionName` for the Play release name.

4. **Verify** - Run `./gradlew testDebugUnitTest assembleRelease` and confirm both succeed.

5. **Commit** - `chore(release): bump version to X.Y.Z` (include the whatsnew file). No
   `Co-Authored-By` trailers.

6. **Ask before shipping** - Confirm with the user, then:
   `git push origin main && git tag -a vX.Y.Z -m "Release X.Y.Z" && git push origin vX.Y.Z`

7. **Watch the workflow** - `gh run list --workflow "Release to Play Store"` / `gh run watch`.
   On success, remind the user to roll out the internal draft and promote to production in the
   Play Console.
