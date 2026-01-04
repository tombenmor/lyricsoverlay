package com.tbm.lyricsoverlay

import android.content.Context
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class YTNotificationListenerService : NotificationListenerService() {

    companion object {
        @Volatile
        var latestTitle: String? = null

        @Volatile
        var latestArtist: String? = null

        fun isEnabled(context: Context): Boolean {
            val enabled = android.provider.Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            )
            return enabled != null && enabled.contains(context.packageName)
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            val pkg = sbn.packageName ?: return
            if (pkg != "com.google.android.apps.youtube.music") return

            val extras: Bundle? = sbn.notification.extras
            extras?.let {
                val title = it.getString("android.title")
                var artist = it.getString("android.text")
                if (artist == null) artist = it.getString("android.subText")
                if (artist == null) artist = it.getString("android.infoText")

                latestTitle = title
                latestArtist = artist
            }
        } catch (e: Exception) {
        }
    }
}
