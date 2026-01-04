# LyricsOverlay – Architecture

This document describes the internal architecture and design decisions behind **LyricsOverlay**.

---

## 🧩 High-Level Architecture

```
MainActivity
 └─ starts OverlayService

YTNotificationListenerService
 └─ listens to YouTube Music notifications
    └─ extracts title & artist

OverlayService (Foreground Service)
 ├─ Floating FAB overlay (WindowManager)
 ├─ Lyrics panel overlay (WindowManager)
 ├─ CoroutineScope
 └─ Foreground notification

LyricsFetcher
 └─ Retrofit + OkHttp
    └─ lyrics.ovh API
```

---

## 🧠 Core Components

### MainActivity
- Entry point
- Requests permissions
- Starts/stops `OverlayService`

### YTNotificationListenerService
- Subclass of `NotificationListenerService`
- Filters YouTube Music notifications
- Extracts metadata (title / artist)
- Stores latest values in memory

### OverlayService
- Foreground service (API 34 compliant)
- Hosts all overlay UI
- Uses `TYPE_APPLICATION_OVERLAY`
- Manages lifecycle and cleanup

### LyricsFetcher
- Retrofit client
- Executes network calls off main thread
- Gracefully handles failures

---

## 🔐 Permissions & System Constraints

- `SYSTEM_ALERT_WINDOW` → overlays
- `FOREGROUND_SERVICE` → persistent service
- `INTERNET` → lyrics fetching
- Notification access → metadata

Android treats this app as a **high-privilege utility**, so user consent is mandatory.

---

## ⚙️ Design Considerations

- Defensive coding around `WindowManager`
- Explicit foreground service type
- Themed context for Material components outside activities
- Graceful failure for missing lyrics / metadata
- OEM variability (MIUI, ColorOS, etc.)

---

## 🚧 Known Tradeoffs

- Notification-based metadata (not MediaSession)
- No lyrics caching
- Limited API coverage
- Overlay UX depends on OEM behavior

---

## 🔮 Future Improvements

- MediaSession metadata
- Lyrics caching
- Multiple providers
- Snap-to-edge bubble
- Settings screen
- Accessibility-aware behavior

---
