# Task 02: Compact App Entry Flow

## Goal

Replace the current launcher behavior with the new compact custom app flow.

## Likely Files

- `app/src/main/java/io/shubham0204/smollmandroid/MainActivity.kt`
- `app/src/main/AndroidManifest.xml`
- new compact navigation/screen files

## Scope

- remove redirection to legacy `ChatActivity` and `DownloadModelActivity`
- launch the new compact flow directly
- keep startup stable with the current Koin and Room bootstrapping

## Completion Criteria

- app launches into the new custom flow

