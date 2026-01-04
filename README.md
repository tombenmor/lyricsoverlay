# LyricsOverlay 🎵📜

![Android](https://img.shields.io/badge/Android-8.0%2B-green)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blue)
![minSdk](https://img.shields.io/badge/minSdk-23-orange)
![targetSdk](https://img.shields.io/badge/targetSdk-34-brightgreen)

LyricsOverlay is an Android app that displays lyrics for the currently playing song in **YouTube Music** using an always-on floating overlay.

A draggable floating bubble appears over all apps; tapping it toggles a lyrics panel at any time.

---

## ✨ Features
- 🎶 Detects the currently playing song from **YouTube Music**
- 📜 Fetches lyrics from a public lyrics API
- 🟢 Always-available **floating bubble (FAB)** overlay
- 📌 Lyrics panel overlay that works over *any app*
- 🖱 Draggable overlays (bubble + lyrics pane)

---

## 📸 Screenshots / Demo

> _Placeholders — replace with real images or GIFs_

```
/screenshots/
 ├─ floating_bubble.png
 ├─ lyrics_overlay.png
 └─ demo.gif
```

Example markdown:
```md
![Floating Bubble](screenshots/floating_bubble.png)
![Lyrics Overlay](screenshots/lyrics_overlay.png)
```

---

## 🚀 Build & Run
```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

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

## 📚 Documentation
- [Architecture](ARCHITECTURE.md)

---

## ⚠️ Disclaimer
Not affiliated with Google or YouTube Music.  
Lyrics may be copyrighted — use responsibly.
