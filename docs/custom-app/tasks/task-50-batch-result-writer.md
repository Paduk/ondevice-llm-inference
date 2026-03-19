# Task 50: Batch Result Writer

## Goal

Persist batch results to disk in a stable TSV format while a run is still in progress.

## Scope

- create a per-case result writer
- write or update a run-level summary record
- flush after every 10 completed rows
- flush on normal completion and explicit stop

## Read First

- [batch-result-export-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/batch-result-export-spec.md)
- [runtime-metrics-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/runtime-metrics-spec.md)

## Likely Files

- `app/src/main/java/io/shubham0204/smollmandroid/ui/...`
- `app/src/main/java/io/shubham0204/smollmandroid/data/...`
- `docs/custom-app/roadmap.md`

## Completion Criteria

- a batch run creates a result TSV file
- per-case rows contain generated text, correctness flags, and runtime metrics
- files are flushed every 10 completed rows
- summary output is updated alongside the row file

