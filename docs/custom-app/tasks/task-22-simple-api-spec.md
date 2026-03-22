# Task 22: Simple API Source Lock

## Goal

Freeze the corrected source of truth for `{tools}` so the Android app matches the Python SFT inference path.

## Scope

- replace `api_v3.0.1.jsonl` assumptions with `simple_api.json`
- confirm that tool rendering uses `plan -> parameter_list`
- keep existing TSV `candidates` parsing and warning behavior
- document that all presets using `{tools}` should use the same simple asset source

## Required Reads

- [tools-injection-spec.md](../specs/tools-injection-spec.md)
- [api-metadata-asset-spec.md](../specs/api-metadata-asset-spec.md)

## Likely Files

- `docs/custom-app/specs/tools-injection-spec.md`
- `docs/custom-app/specs/api-metadata-asset-spec.md`
- `docs/custom-app/roadmap.md`

## Completion Criteria

- docs consistently describe `simple_api.json` as the only `{tools}` source
- rendering format is frozen as `{plan}: {parameter_list}`
- roadmap reflects the correction task and new follow-up tasks
