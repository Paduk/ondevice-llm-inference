# Task 23: Simple API Asset Loader

## Goal

Load `simple_api.json` from app assets and expose it as a cached plan-indexed lookup.

## Scope

- bundle `simple_api.json` under `app/src/main/assets/`
- add or replace the current asset loader
- return `Map<String, List<String>>`
- fail clearly if the asset is missing or malformed

## Required Reads

- [api-metadata-asset-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/api-metadata-asset-spec.md)

## Likely Files

- `app/src/main/assets/simple_api.json`
- `app/src/main/java/io/shubham0204/smollmandroid/data/ApiMetadataAssetStore.kt`
- `docs/custom-app/roadmap.md`

## Completion Criteria

- app can read `simple_api.json` from assets
- the loader returns plan-to-parameter-list data
- old JSONL-specific normalization is removed from the loader path
