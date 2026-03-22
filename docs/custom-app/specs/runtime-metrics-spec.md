# Runtime Metrics Spec

## Purpose

Define how runtime inference metrics should be split, aggregated, and presented without overloading the main custom-app screen.

## Metric Split

Per inference case, the app should distinguish at least:

- `prefill time`
- `generation time`
- `total time`
- `prompt length`
- `generated token count`
- `generation token/s`

Optional if easily available:

- `prefill token/s`

## Definitions

- `prefill time`: time spent processing the prompt before normal autoregressive token generation
- `generation time`: time spent generating output tokens after prompt processing
- `total time`: `prefill time + generation time`
- `prompt length`: prompt token count used by the model
- `generated token count`: number of output tokens produced
- `generation token/s`: `generated token count / generation time`
- `overall token/s`: `(prompt length + generated token count) / total time`

## Frozen UI Policy

- keep the metrics section readable during normal chat and batch runs
- avoid exposing all runtime metrics as always-visible rows
- prefer an inline expandable details pattern over a popup or modal by default
- Toolcalling and RMA should use the same runtime-metrics UI structure and labels

## Frozen Batch Policy

- for a single completed case, per-case metrics are enough
- for 2 or more completed batch cases, the UI should also expose aggregate totals and averages
- aggregates should update incrementally as the batch progresses

## Batch Aggregation

For batch runs with 2 or more evaluated rows, the app should also track:

- total prefill time
- total generation time
- average prefill time
- average generation time
- average generation token/s
- average prefill token/s
- average overall token/s

If the batch has only 1 completed row, the batch summary may simply mirror the per-case metrics.

## UI Direction

Metrics should not all be shown as always-expanded top-level rows.

### Always Visible Core Metrics

Keep these visible in the main runtime card:

- `total time`
- `generation token/s`
- `prompt length`

### Expandable Detailed Metrics

Show the rest only when the user explicitly expands a details section:

- `prefill time`
- `generation time`
- `generated token count`
- optional `prefill token/s`
- optional `overall token/s`
- batch totals and averages

## UI Form Recommendation

Preferred approach:

- inline expandable section or collapsible card

Avoid making detailed metrics permanently visible in the main flow.

Popup or modal UI is allowed, but inline expand or collapse is preferred because this screen is used repeatedly during testing.

## Shared-Component Direction

Runtime metrics should be treated as shared inference instrumentation rather than evaluator-specific UI.

- Toolcalling and RMA should reuse the same runtime-metrics component or helper
- evaluator-specific sections may differ
- metric labels, ordering, and details-toggle behavior should not drift between Toolcalling and RMA
