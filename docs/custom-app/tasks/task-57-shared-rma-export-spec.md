# Task 57: Shared RMA Export Spec

## Goal

Freeze the rule that `RMA` should reuse the same batch export actions UI already used by `Toolcalling`.

## Scope

- define export-actions sharing between `Toolcalling` and `RMA`
- keep evaluator summaries separate
- define the shared state contract for result path, summary path, flush count, and resumed state

## Read First

- [shared-batch-export-ui-spec.md](../specs/shared-batch-export-ui-spec.md)
- [batch-result-export-spec.md](../specs/batch-result-export-spec.md)
- [rma-rewrite-spec.md](../specs/rma-rewrite-spec.md)

## Completion Criteria

- docs explicitly state that `RMA` reuses the same export-actions UI as `Toolcalling`
- shared versus task-specific boundaries are clear
- roadmap focus can move to implementation of the shared export-actions component or RMA wiring

