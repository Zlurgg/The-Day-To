package io.github.zlurgg.update.data.service

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.net.toUri
import timber.log.Timber

/**
 * Service for downloading and installing APK files.
 * Uses Android's DownloadManager for background downloads.
 *
 * @param context Android context for system services
 * @param downloadTitle Notification title shown during download
 */
class ApkDownloadService(
    private val context: Context,
    private val downloadTitle: String = DEFAULT_DOWNLOAD_TITLE
) {

    fun downloadApk(url: String, fileName: String): Long {
        Timber.d("Starting APK download: $fileName from $url")

        val request = DownloadManager.Request(url.toUri())
            .setTitle(downloadTitle)
            .setDescription("Downloading $fileName")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        Timber.i("APK download started with ID: $downloadId")
        return downloadId
    }

    fun installApk(downloadId: Long) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = downloadManager.getUriForDownloadedFile(downloadId)

        if (uri == null) {
            Timber.e("Failed to get URI for download ID: $downloadId")
            return
        }

        Timber.i("Installing APK from: $uri")
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, APK_MIME_TYPE)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    }

    companion object {
        private const val APK_MIME_TYPE = "application/vnd.android.package-archive"
        private const val DEFAULT_DOWNLOAD_TITLE = "Downloading Update"
    }
}
