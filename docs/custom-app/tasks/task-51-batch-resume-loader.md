# Task 51: Batch Resume Loader

## Goal

Allow a batch run to continue from a prior result TSV by skipping rows that were already evaluated.

## Scope

- load an existing result TSV
- index completed rows by `unique_idx`
- skip already-evaluated rows in the next run
- mark the run as resumed in UI state and summary output

## Read First

- [batch-result-export-spec.md](../specs/batch-result-export-spec.md)
- [batch-run-spec.md](../specs/batch-run-spec.md)

## Completion Criteria

- existing result TSV rows are recognized by `unique_idx`
- resumed runs only evaluate remaining rows
- summary output reflects resumed progress
- UI exposes that the run is fresh or resumed

