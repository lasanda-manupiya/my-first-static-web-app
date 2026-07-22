# RouteWake 🟢🔔

**RouteWake** is a location-based alarm app for Android. Pick a destination,
choose an alert radius, start tracking, and your phone rings + vibrates when you
enter that radius — perfect for napping on the bus or train without missing your
stop.

> **No backend. No database. No Firebase. No paid APIs.**
> Maps use **OpenStreetMap via OSMDroid**, location uses the device GPS, and the
> only persistence is **DataStore** (settings + saved/recent places).

Built with **Kotlin**, **Jetpack Compose**, **MVVM**, **StateFlow**, and a
**foreground location service**.

---

## Features

| Screen | What it does |
| --- | --- |
| **Onboarding** (first launch) | Asks name, gender and date of birth; shows a live local avatar preview |
| **Home** | Personalized greeting + avatar, on-device place search, recent places, radius selector (100 m – 5 km), big green **Set Alarm** button |
| **Map** | OpenStreetMap map, tap to drop a pin, destination marker, transparent green radius circle, bottom details card, **Start Alarm** |
| **Tracking** | Live map, your location, dotted route line, live distance / ETA / speed / radius, red **Stop Alarm** |
| **Alarm** | Big **ALARM!** title, destination name, pulsing bell, distance, **Stop** + **Snooze 5 min** |
| **Saved Places** | Locally stored saved destinations (DataStore — no Room) |
| **Settings** | Profile card (avatar, name, age, birthday), default radius, alarm sound, vibration, speak destination name, high-accuracy mode, keep screen on |

### Personalization & birthday wishes

- **Onboarding** runs once on first launch and stores **name + gender + date of
  birth** in DataStore (never leaves the device).
- The app then **greets you by name** (time-of-day aware) and shows a **local
  avatar** generated from your gender and age group — no avatar service or
  network call; it's a tinted circle with a matching Material face icon.
- On your **birthday**, RouteWake posts a notification wishing you a happy
  birthday. This is driven by `AlarmManager.setInexactRepeating` (a daily
  ~09:00 check) → `BirthdayReceiver`, which is re-armed after reboot by
  `BootReceiver`. No exact-alarm permission, no WorkManager, no Room — all
  local framework APIs. A "last wished year" flag prevents duplicate wishes.

---

## Project structure

```
RouteWake/
├── settings.gradle.kts
├── build.gradle.kts                 # project-level
├── gradle/libs.versions.toml        # version catalog
└── app/
    ├── build.gradle.kts             # app-level
    └── src/main/
        ├── AndroidManifest.xml
        └── java/com/routewake/app/
            ├── RouteWakeApp.kt              # Application (OSMDroid config)
            ├── MainActivity.kt             # Compose host + permissions
            ├── model/                      # Place, AlarmState, AppSettings, UserProfile, Gender
            ├── utils/                      # DistanceUtils, GeocoderHelper, Constants
            ├── storage/                    # DataStore (shared) + Settings/Places/UserProfile repos + ActiveAlarmStore
            ├── location/                   # Foreground service + PermissionHelper
            ├── alarm/                      # AlarmPlayer (sound/vibration/TTS)
            ├── birthday/                   # BirthdayScheduler, BirthdayReceiver, BootReceiver
            ├── viewmodel/                  # MainViewModel, SettingsViewModel
            └── ui/
                ├── theme/                  # Color, Type, Theme (white + green)
                ├── navigation/             # Routes, NavGraph
                ├── components/             # BottomBar, RadiusSelector, PlaceCard, OsmMapView, ProfileAvatar
                └── screens/                # Onboarding/Home/Map/Tracking/Alarm/Saved/Settings
```

---

## How tracking works

1. **Set Alarm** → `MainViewModel.startAlarm()` starts
   `LocationForegroundService` via `startForegroundService`.
2. The service shows a **persistent notification**: *“RouteWake is tracking your
   destination.”*
3. It requests GPS updates every **5 s** from the framework `LocationManager`
   (no Google Play Services).
4. On each fix it computes distance with `Location.distanceBetween()` (Haversine
   is also provided in `DistanceUtils`) and pushes it to the in-memory
   `ActiveAlarmStore` (a `StateFlow`) that the UI observes.
5. When `distance <= radius`, it fires the alarm (sound + vibration + optional
   TTS) and brings the **Alarm** screen forward.
6. **Stop** ends the service; **Snooze** silences it for 5 minutes and re-arms.

Permissions requested: `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`,
`FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_LOCATION`, `POST_NOTIFICATIONS`
(Android 13+), plus `VIBRATE` / `INTERNET` (map tiles).
`ACCESS_BACKGROUND_LOCATION` is intentionally **not** used — the foreground
service with a persistent notification covers the MVP.

---

## Running it

### Option A — Android Studio (recommended)
1. Open **Android Studio** (Koala / Ladybug or newer).
2. **File → Open…** and select the `RouteWake/` folder.
3. Android Studio will sync Gradle and **provision the Gradle wrapper
   automatically** (this is why no `gradle-wrapper.jar` binary is committed).
4. Plug in a device or start an emulator (API 24+), then press **Run ▶**.

### Option B — Command line
If you have a local Gradle (8.11+) installed, first generate the wrapper, then build:
```bash
cd RouteWake
gradle wrapper            # creates gradlew + gradle-wrapper.jar
./gradlew assembleDebug    # build the APK
./gradlew installDebug     # install on a connected device
```

### Notes
- **minSdk 24**, **targetSdk 36** (Android 16 — meets Google Play's target API
  policy), Kotlin 2.0, Compose BOM 2024.12.
- Grant **Location** and **Notification** permissions when prompted, or the
  tracking service can’t start.
- For best results enable **High accuracy mode** (Settings) and test outdoors —
  GPS indoors is unreliable.
- Map tiles need an **internet connection** the first time they’re viewed
  (they’re cached afterwards by OSMDroid).

---

## Tech choices (why)

- **OSMDroid / OpenStreetMap** instead of Google Maps → free, no API key.
- **Framework `LocationManager`** instead of FusedLocationProvider → no Google
  Play Services dependency, pure device feature.
- **DataStore** instead of Room → simple key/value persistence for settings and
  saved places; active-alarm state is held purely in memory.
- **MVVM + StateFlow + Compose Navigation** → clean, testable, reactive UI.
