# Task 18: Load API Metadata From Assets

## Goal

Load `api_v3.0.1.jsonl` from Android assets and expose plan-based metadata lookup.

## Scope

- place the JSONL file in app assets
- implement an asset loader for the JSONL file
- normalize entries by dropping unneeded keys
- make the plan map reusable by prompt rendering code

## Required Specs

- [api-metadata-asset-spec.md](../specs/api-metadata-asset-spec.md)

## Completion Criteria

- the app can load asset-based API metadata into memory
- plan lookups are available to prompt rendering code
