package com.tbm.lyricsoverlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.tbm.lyricsoverlay.databinding.OverlayLyricsBinding
import kotlinx.coroutines.*

class OverlayService : Service() {
    companion object {
        const val ACTION_START = "START_OVERLAY_SERVICE"
        const val ACTION_STOP = "STOP_OVERLAY_SERVICE"
        private const val TAG = "OverlayService"
        private const val NOTIF_CHANNEL_ID = "ytm_overlay_channel"
        private const val NOTIF_ID = 101
    }

    private var windowManager: WindowManager? = null

    // Views
    private var fabView: View? = null
    private var lyricsView: View? = null
    private var binding: OverlayLyricsBinding? = null

    // params
    private var fabParams: WindowManager.LayoutParams? = null
    private var lyricsParams: WindowManager.LayoutParams? = null

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannelIfNeeded()
        // start foreground with explicit type for API/target requirements
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                startForeground(NOTIF_ID, makeNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            } catch (e: NoSuchMethodError) {
                startForeground(NOTIF_ID, makeNotification())
            }
        } else {
            startForeground(NOTIF_ID, makeNotification())
        }
    }

    // helper toggle function (can be top-level in the service)
    private fun toggleLyrics() {
        if (lyricsView == null) showLyricsOverlay() else removeLyricsOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "Enable 'Display over other apps' for this app.", Toast.LENGTH_LONG).show()
                    val i = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(i)
                    stopSelf()
                    return START_NOT_STICKY
                }
                showFabOverlay()
            }
            ACTION_STOP -> {
                stopSelf()
            }
            else -> {
                // no-op
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        try {
            job.cancelChildren()
            removeFabOverlay()
            removeLyricsOverlay()
        } catch (e: Exception) {
            Log.w(TAG, "onDestroy cleanup", e)
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ---------- FAB overlay ----------
    private fun showFabOverlay() {
        if (fabView != null) return

        // create a themed inflater so Material components can be inflated outside an Activity
        val themeWrapper = android.view.ContextThemeWrapper(this, R.style.Theme_LyricsOverlay)
        // clone the system inflater into the themed context
        val inflater = (getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater).cloneInContext(themeWrapper)
        fabView = inflater.inflate(R.layout.overlay_fab, null)

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        fabParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 20
            y = 200
        }

        try {
            windowManager?.addView(fabView, fabParams)
        } catch (e: Exception) {
            Log.e(TAG, "addView(fab) failed", e)
            fabView = null
            return
        }

        fabView?.isClickable = true
        fabView?.isFocusable = true

        fabView?.setOnTouchListener(object : View.OnTouchListener {
            private var downX = 0
            private var downY = 0
            private var startX = 0
            private var startY = 0
            private val CLICK_THRESHOLD = 12 // pixels; tune if needed

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = event.rawX.toInt()
                        downY = event.rawY.toInt()
                        startX = fabParams?.x ?: 0
                        startY = fabParams?.y ?: 0
                        // return true to indicate we're handling subsequent events
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.rawX.toInt() - downX
                        val dy = event.rawY.toInt() - downY
                        fabParams?.x = startX + dx
                        fabParams?.y = startY + dy
                        try {
                            windowManager?.updateViewLayout(fabView, fabParams)
                        } catch (e: Exception) {
                            Log.w(TAG, "updateViewLayout(fab) failed", e)
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        val upX = event.rawX.toInt()
                        val upY = event.rawY.toInt()
                        val moved = Math.hypot((upX - downX).toDouble(), (upY - downY).toDouble())
                        if (moved <= CLICK_THRESHOLD) {
                            // It's a click/tap
                            Log.d(TAG, "FAB tapped (moved=$moved) -> toggle lyrics")
                            // optional short feedback while debugging:
                            Toast.makeText(this@OverlayService, "FAB tapped", Toast.LENGTH_SHORT).show()
                            toggleLyrics()
                        } else {
                            // drag end; you might want to snap to edge here
                            Log.d(TAG, "FAB drag end (moved=$moved)")
                        }
                        return true
                    }
                    else -> return false
                }
            }
        })

        // long press to remove/hide
        fabView?.setOnLongClickListener {
            removeFabOverlay()
            true
        }
    }

    private fun removeFabOverlay() {
        try {
            fabView?.let { v ->
                if (v.parent != null) windowManager?.removeView(v)
            }
        } catch (e: Exception) {
            Log.w(TAG, "removeFabOverlay failed", e)
        } finally {
            fabView = null
            fabParams = null
        }
    }

    // ---------- Lyrics overlay ----------
    private fun showLyricsOverlay() {
        Log.d(TAG, "showLyricsOverlay() called. current lyricsView=${lyricsView != null}")

        if (lyricsView != null) return
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // inflate binding (ensures view ids available)
        binding = OverlayLyricsBinding.inflate(inflater)
        lyricsView = binding?.root

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        lyricsParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            // prevent stealing input; allow touches on interactive children
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP }

        try {
            windowManager?.addView(lyricsView, lyricsParams)
        } catch (e: Exception) {
            Log.e(TAG, "addView(lyrics) failed", e)
            lyricsView = null
            binding = null
            return
        }

        // wire UI
        binding?.btnClose?.setOnClickListener { removeLyricsOverlay() }
        binding?.btnSearchWeb?.setOnClickListener {
            val title = YTNotificationListenerService.latestTitle ?: ""
            val artist = YTNotificationListenerService.latestArtist ?: ""
            val query = Uri.encode("$title $artist lyrics")
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$query"))
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
        }

        val title = YTNotificationListenerService.latestTitle ?: "Unknown title"
        val artist = YTNotificationListenerService.latestArtist ?: "Unknown artist"
        binding?.tvSong?.text = "$title — $artist"
        binding?.tvLyrics?.text = "Loading lyrics..."

        scope.launch {
            try {
                val lyrics = LyricsFetcher.fetchLyrics(artist, title)
                if (!isActive) return@launch
                binding?.tvLyrics?.text = lyrics ?: "Lyrics not found via API."
            } catch (e: Exception) {
                Log.e(TAG, "fetch lyrics failed", e)
                binding?.tvLyrics?.text = "Error fetching lyrics."
            }
        }

        // make lyric header draggable (so panel can be moved)
        var lastX = 0
        var lastY = 0
        var paramX = lyricsParams?.x ?: 0
        var paramY = lyricsParams?.y ?: 0
        binding?.tvSong?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.rawX.toInt()
                    lastY = event.rawY.toInt()
                    paramX = lyricsParams?.x ?: 0
                    paramY = lyricsParams?.y ?: 0
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX.toInt() - lastX
                    val dy = event.rawY.toInt() - lastY
                    lyricsParams?.x = paramX + dx
                    lyricsParams?.y = paramY + dy
                    try {
                        windowManager?.updateViewLayout(lyricsView, lyricsParams)
                    } catch (e: Exception) {
                        Log.w(TAG, "updateViewLayout(lyrics) failed", e)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun removeLyricsOverlay() {
        try {
            lyricsView?.let { v ->
                if (v.parent != null) windowManager?.removeView(v)
            }
        } catch (e: Exception) {
            Log.w(TAG, "removeLyricsOverlay failed", e)
        } finally {
            binding = null
            lyricsView = null
            lyricsParams = null
        }
    }

    // ---------- Helpers ----------
    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(NOTIF_CHANNEL_ID, "Overlay service", NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(channel)
        }
    }

    private fun makeNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
            .setContentTitle("YTMusic Lyrics Overlay")
            .setContentText("Overlay service running")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
}
