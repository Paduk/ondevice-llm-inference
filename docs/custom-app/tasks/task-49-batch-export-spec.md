# Task 49: Batch Export Spec

## Goal

Freeze the result-export and resume assumptions for long-running batch evaluations.

## Scope

- freeze batch modes as `Top 1`, `Top 50`, and `All`
- freeze per-case result TSV columns
- freeze flush cadence at every 10 completed rows
- freeze `unique_idx`-based resume behavior
- freeze metric naming for persisted throughput fields

## Read First

- [batch-run-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/batch-run-spec.md)
- [batch-result-export-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/batch-result-export-spec.md)
- [runtime-metrics-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/runtime-metrics-spec.md)

## Likely Files

- `docs/custom-app/specs/batch-run-spec.md`
- `docs/custom-app/specs/batch-result-export-spec.md`
- `docs/custom-app/specs/runtime-metrics-spec.md`
- `docs/custom-app/roadmap.md`

## Completion Criteria

- docs clearly replace `Random 10` with `Top 50`
- docs define the persisted result schema
- docs define the flush and resume rules
- roadmap is updated to point the next task at the writer implementation

