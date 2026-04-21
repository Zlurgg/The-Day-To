package uk.co.zlurgg.thedayto.core.data.util

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory
import timber.log.Timber
import uk.co.zlurgg.thedayto.core.ui.util.InAppReviewLauncher

class InAppReviewLauncherImpl : InAppReviewLauncher {

    override fun launchReviewFlow(activity: Activity) {
        val reviewManager = ReviewManagerFactory.create(activity)
        reviewManager.requestReviewFlow().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                reviewManager.launchReviewFlow(activity, task.result)
            } else {
                Timber.tag(TAG).w("Review flow request failed: %s", task.exception?.message)
            }
        }
    }

    companion object {
        private const val TAG = "InAppReviewLauncher"
    }
}
