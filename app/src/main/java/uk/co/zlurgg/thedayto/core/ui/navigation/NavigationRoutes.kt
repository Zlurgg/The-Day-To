package uk.co.zlurgg.thedayto.core.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes using Kotlin Serialization
 * Following Google's 2025 recommendation for Navigation Compose
 */

@Serializable
data object SignInRoute

@Serializable
data object OverviewRoute

@Serializable
data class EditorRoute(
    val entryId: Int? = null,
    val showBackButton: Boolean = false
)

@Serializable
data object MoodColorRoute

@Serializable
data object NotificationTestRoute
