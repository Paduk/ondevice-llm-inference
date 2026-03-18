# API Metadata Asset Spec

## Goal

Define how simple tool metadata is bundled with the Android app.

## Asset File

- filename: `simple_api.json`
- location: `app/src/main/assets/`

## File Format

- UTF-8 JSON
- one top-level object
- each key is a plan name
- each value is an ordered list of parameter names
- the source file is expected to match the Python `read_apis(..., simple=True)` logic already used offline

## Loader Expectations

- load once and cache in memory for app use
- expose a `Map<String, List<String>>`-like lookup structure or equivalent
- fail clearly if the asset is missing or malformed

## MVP Assumption

- one bundled asset version is enough
- app updates are the mechanism for refreshing tool metadata
- the asset is shipped inside the APK under `app/src/main/assets/simple_api.json`
