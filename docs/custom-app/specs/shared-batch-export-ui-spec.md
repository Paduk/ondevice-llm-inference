# Shared Batch Export UI Spec

## Goal

Keep batch result export and cleanup actions consistent across `Toolcalling` and `RMA` without forcing their evaluator summaries to become identical.

## Shared Scope

The following UI and state should be shared:

- `Share Result`
- `Share Summary`
- `Delete Saved Results`
- current result file name or path
- current summary file name or path
- last flush count or flush status
- whether the current run is fresh or resumed

## Separation Boundary

The following remains task-specific:

- evaluator summary metrics
- correctness labels
- parse-specific or rewrite-specific error messages
- per-task aggregate scoring rules

This means export actions are shared, but evaluator summaries are still different between `Toolcalling` and `RMA`.

## RMA Direction

`RMA` should expose the same batch export controls already available in `Toolcalling`.

That includes:

- reusing the same labels
- reusing the same button order
- reusing the same visibility rules
- reusing the same resumed-run and flush-state messaging

## Data Expectations

The shared export-actions UI assumes each evaluator flow can provide:

- result file path
- summary file path
- last flush count
- resumed or fresh state
- delete availability

The internal summary file contents may differ by evaluator type.

## UX Direction

The export-actions section should stay secondary to:

- runtime metrics
- evaluator summary
- current batch progress

It should appear as a compact shared section rather than a task-specific custom block.
