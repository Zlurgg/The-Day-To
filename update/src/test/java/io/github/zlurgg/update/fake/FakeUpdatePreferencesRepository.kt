package io.github.zlurgg.update.fake

import io.github.zlurgg.update.domain.repository.UpdatePreferencesRepository

/**
 * Fake implementation of UpdatePreferencesRepository for testing.
 * Implements just the update-related preferences methods.
 */
class FakeUpdatePreferencesRepository : UpdatePreferencesRepository {

    private var dismissedVersion: String? = null

    override suspend fun getDismissedVersion(): String? {
        return dismissedVersion
    }

    override suspend fun setDismissedVersion(version: String) {
        dismissedVersion = version
    }

    /**
     * Test helper to directly set the dismissed version.
     */
    fun setDismissedVersionForTest(version: String?) {
        dismissedVersion = version
    }

    /**
     * Test helper to reset state.
     */
    fun reset() {
        dismissedVersion = null
    }
}
