package uk.co.zlurgg.thedayto.core.domain.usecases.notifications

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build

/**
 * Checks if we should show a rationale for notification permission.
 *
 * Returns false if:
 * - Permission is permanently denied ("Don't ask again" selected)
 * - Permission has never been requested
 *
 * Returns true if:
 * - Permission was denied but can be requested again
 */
class ShouldShowPermissionRationaleUseCase(
    private val context: Context
) {
    operator fun invoke(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return false  // No runtime permission needed before Android 13
        }

        val activity = context as? Activity ?: return false
        return activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
    }
}