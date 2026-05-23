# RentalBilling Android — Setup

## Open in Android Studio

1. Android Studio Hedgehog (2023.1) or newer
2. File → Open → select repo root
3. Sync Gradle (auto)
4. Run on emulator or device (min Android 7.0)

## Project layout

```
app/
  build.gradle.kts
  src/main/
    AndroidManifest.xml
    java/com/cuanz/rentalbilling/
      RentalApp.kt           Hilt application
      MainActivity.kt        single-activity entry
      data/                  Room DB + DAOs + entities
      repo/                  RentalRepository (single source of truth)
      billing/               BillingEngine (pure Kotlin, unit-testable)
      di/                    DataModule (Hilt providers)
      ui/
        AppNavGraph.kt       Compose Navigation
        auth/                LoginScreen
        admin/               Dashboard, Devices, Members, Sessions
        member/              Member dashboard
        theme/               Material 3 theme
    res/values/              strings.xml, themes.xml
```

## Demo flow

1. Launch app → tap "I'm an Admin →"
2. Devices → add a tablet "TAB-01", rate Rp 10,000/hour
3. Members → add a member "Andi", phone "08123"
4. Members → top up Andi Rp 50,000
5. Sessions → start session on TAB-01 with Andi
6. Wait a few minutes → tap Stop → final bill computed,
   balance is auto-deducted, receipt transaction created
7. Back to login → enter "08123" → see member dashboard
   with balance, active rentals, and history

## Tests

`BillingEngine` is pure — add JVM unit tests under `app/src/test/`.
Sample test idea: assert hourly rounding to 15-min blocks, package +
overstay penalty, and discount math.

## Roadmap

- Firebase sync for multi-branch deployment
- Midtrans top-up integration
- PDF receipt export
- Member-facing top-up via Snap
- Daily revenue reports
