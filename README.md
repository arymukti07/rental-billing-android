# Rental Billing — Android (Kotlin + Jetpack Compose)

A two-role billing application for phone and tablet rental shops in Indonesia.
Replaces paper logs and spreadsheets with an offline-first Room database.

## Roles

- **Admin** — register devices, set hourly/package rates, manage members, start/stop rentals, see receipts and reports.
- **Member** — see remaining time, current bill, transaction history, and top-up balance.

## Tech

- Kotlin 1.9
- Jetpack Compose (Material 3)
- Room (SQLite) — offline-first
- Hilt — DI
- Coroutines + Flow
- Min SDK 24, Target SDK 34

## Modules

```
app/
  data/        Room DB, DAOs, entities
  repo/        Repository layer
  billing/     BillingEngine — rate rules, discounts, overstay penalty
  ui/
    auth/      Login screen
    admin/     Admin dashboard, devices, members, sessions, reports
    member/    Member dashboard, balance, history
    theme/     Material 3 theme
```

## Build

Open in Android Studio (Hedgehog or newer) and run.

## Status

Scaffold complete. Build verified locally on Android Studio.
Generated with assistance from Codex (GPT) for rapid UI/data layer scaffolding.

## License

Private — internal use.
