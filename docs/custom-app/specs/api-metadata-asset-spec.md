# API Metadata Asset Spec

## Goal

Define how tool metadata is bundled with the Android app.

## Asset File

- filename: `api_v3.0.1.jsonl`
- location: `app/src/main/assets/`

## File Format

- UTF-8 JSONL
- one JSON object per line
- each object must contain `plan`
- the current source file is expected to match the Python preprocessing logic already used offline

## Loader Expectations

- load once and cache in memory for app use
- expose a `Map<String, Map<String, Any?>>`-like lookup structure or equivalent
- fail clearly if the asset is missing or malformed

## Normalization Rules

- drop `examples`
- drop `returns`
- drop `next_turn_plans`
- keep the remaining keys for prompt injection
- use the `plan` field as the top-level lookup key
- keep `plan` inside the rendered metadata object unless implementation simplicity clearly benefits from removing it

## MVP Assumption

- one bundled asset version is enough
- app updates are the mechanism for refreshing tool metadata
- the asset is shipped inside the APK under `app/src/main/assets/api_v3.0.1.jsonl`
