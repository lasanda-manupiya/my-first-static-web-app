# RouteWake — Release Notes

## v1.1 (versionCode 2) — Closed Testing
**Release date:** 22 July 2026
**Track:** Closed testing
**Min Android:** 7.0 (API 24) · **Target Android:** 16 (API 36)

This release covers two things: **new features requested by the client** and a
**Google Play compliance update**.

---

### ✨ New features (client request)

- **Personalized onboarding** — on first launch RouteWake now asks for your
  **name, gender, and date of birth**. This is stored only on your device.
- **We greet you by name** — the Home screen shows a friendly, time-of-day
  greeting (e.g. *"Good morning, Alex 👋"*).
- **Your own profile avatar** — a simple avatar is generated locally from your
  gender and age, shown on Home and in Settings. No account or internet needed.
- **Birthday wishes** — on your birthday, RouteWake sends you a notification to
  wish you a happy birthday.

### 🔒 Compliance update (Google Play request)

- Updated the app to **target Android 16 (API 36)** to meet Google Play's target
  API level policy (apps must target within one year of the latest Android
  release; the previous target would have been blocked from updates after
  31 August 2026).
- Version bumped to **1.1 (versionCode 2)** for this release.

### 🛠 Under the hood

- Internal data-storage refactor for reliability (shared local storage layer).
- No backend, no database, no cloud — all personal data stays on the device.

---

### 📱 What's new (Play Console — short form)

> **RouteWake 1.1**
> • Personalized experience — we now greet you by name with your own profile avatar
> • Birthday surprise — RouteWake wishes you a happy birthday on your special day
> • Quick first-time setup (name & date of birth), stored only on your device
> • Updated to meet the latest Google Play & Android requirements
>
> Thanks for helping test RouteWake! Please send us your feedback.

---

### 🧪 Notes for testers

- On first launch, please complete the **profile setup** (name + date of birth).
- Grant **Location** and **Notification** permissions when prompted — the tracking
  alarm and birthday wish both need them.
- To try the birthday notification without waiting, temporarily set your date of
  birth to **today** during onboarding; the wish is delivered on the daily check.
- Test the core flow: pick a destination → choose a radius → **Set/Start Alarm**
  → confirm the tracking notification and that the alarm rings within the radius.

### Known limitations (this build)

- Profile details can't be edited after onboarding yet (planned for a later build).
- Map tiles need an internet connection the first time an area is viewed.
