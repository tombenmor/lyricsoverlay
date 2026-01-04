YTMusic Lyrics Overlay - Android Studio project skeleton
--------------------------------------------------------
What this contains:
- An Android app skeleton (Kotlin) that shows a FloatingActionButton and starts an overlay service which reads
  YouTube Music notifications (via a NotificationListenerService) and fetches lyrics via lyrics.ovh.

How to use:
1. Open the project in Android Studio (File -> Open -> select the folder).
2. Let Android Studio sync Gradle (it may prompt to upgrade Gradle/AGP; follow the prompts).
3. Build & run on a physical device (recommended).
4. Grant Notification access and Draw over other apps when prompted from the app UI.

Notes:
- This is a development skeleton. You may need to update Gradle/AGP versions to match your Android Studio.
- The lyrics source (lyrics.ovh) is a free API and may be limited; replace LyricsFetcher if you prefer another provider.

