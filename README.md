# LyricsOverlay 🎵📜

LyricsOverlay is an Android app that displays song lyrics for the **currently playing track in YouTube Music**, using an **always-on floating overlay**.
A draggable floating action button (FAB) appears over all apps; tapping it toggles a lyrics panel that can be shown or hidden at any time.

This project is intended for **personal use, experimentation, and learning** around Android overlays, foreground services, and media metadata access.

---

## ✨ Features

- 🎶 Detects the currently playing song from **YouTube Music**
- 📜 Fetches lyrics from a public lyrics API
- 🟢 Always-available **floating bubble (FAB)** overlay
- 📌 Lyrics panel overlay that works over *any app*
- 🖱 Draggable overlays (bubble + lyrics pane)
- 🔔 Foreground service for reliability on modern Android
- 🛡 Defensive handling of permissions and system restrictions

---

## 🔐 Permissions

| Permission | Purpose |
|----------|---------|
| INTERNET | Fetch lyrics |
| SYSTEM_ALERT_WINDOW | Draw overlays |
| FOREGROUND_SERVICE | Persistent overlay |
| Notification Access | Read song metadata |

> Notification access and overlay permission must be granted manually in system settings.

---

## 🚀 Build & Install

### Build debug APK
```bash
./gradlew clean :app:assembleDebug
```

### Install on device
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## ▶️ Usage

1. Install the app
2. Grant required permissions
3. Play a song in YouTube Music
4. Start the overlay service
5. Tap the floating bubble to toggle lyrics

---

## 🐞 Debugging

```bash
adb logcat -s OverlayService YTNotificationListenerService *:E
```

Common issues:
- Overlay permission not granted
- Notification access disabled
- Lyrics not available for the song

---

## ⚠️ Limitations

- Lyrics availability depends on the external API
- Metadata accuracy varies by Android version/OEM
- Not intended for Play Store distribution without licensing review

---

## 📱 How It Works

1. **NotificationListenerService**
    - Listens for media notifications from YouTube Music
    - Extracts song title and artist

2. **OverlayService**
    - Runs as a foreground service
    - Hosts the floating FAB and lyrics overlay
    - Uses `WindowManager.TYPE_APPLICATION_OVERLAY`

3. **Lyrics Fetching**
    - Lyrics are fetched via Retrofit + OkHttp
    - Network calls are executed with Kotlin coroutines

---

## 🧩 Architecture

```
MainActivity
 └─ starts OverlayService

YTNotificationListenerService
 └─ reads YouTube Music notifications
    └─ stores latest title & artist

OverlayService
 ├─ floating FAB overlay
 ├─ lyrics overlay
 └─ foreground notification

LyricsFetcher
 └─ lyrics.ovh API
```

---

## 📜 Legal Notice

This project is **not affiliated with Google or YouTube Music**.
Lyrics may be copyrighted — use responsibly.

---

## 🧠 Learning Topics

- Foreground services (API 34)
- Android overlays
- NotificationListenerService
- Material Components outside Activities
- Kotlin coroutines
- Retrofit / OkHttp

---

## 📄 License

Provided as-is for educational and personal use.
