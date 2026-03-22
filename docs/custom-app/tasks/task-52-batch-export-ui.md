# Task 52: Batch Export UI

## Goal

Expose the minimum export and resume state in the app without overcrowding the evaluator screen.

## Scope

- show current result file name or path
- show last flush status
- show whether the current run is fresh or resumed
- update batch mode labels to `Top 1`, `Top 50`, and `All`

## Read First

- [batch-result-export-spec.md](../specs/batch-result-export-spec.md)
- [runtime-metrics-spec.md](../specs/runtime-metrics-spec.md)

## Completion Criteria

- UI uses the new batch mode labels
- UI shows export and resume state without dumping raw TSV rows
- export state remains secondary to evaluation and metrics content

