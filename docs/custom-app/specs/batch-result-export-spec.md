# Batch Result Export Spec

## Goal

Persist batch evaluation progress to disk so long-running evaluations can be inspected later and resumed after interruption.

## Result Files

The app should write two result artifacts for a batch run:

1. per-case result TSV
2. run-level summary record

The summary record may be TSV or JSON, but it must be written in a stable machine-readable format.

## Batch Modes

The persisted batch flow should assume these modes only:

- `Top 1`
- `Top 50`
- `All`

`Random 10` is no longer part of the supported persistence or resume flow.

## Per-Case TSV Columns

Each evaluated row should persist at least:

- `unique_idx`
- `query`
- `rewrited_query`
- `gold_answer`
- `generated`
- `parse_success`
- `plan_correct`
- `arguments_correct`
- `all_correct`
- `prefill_tokens_per_sec`
- `generation_tokens_per_sec`
- `overall_tokens_per_sec`
- `prefill_time_ms`
- `generation_time_ms`
- `total_time_ms`
- `prompt_tokens`
- `generated_tokens`
- `status`
- `error_message`
- `evaluated_at`

## Metric Definitions

- `prefill_tokens_per_sec`: prompt token throughput during prefill only
- `generation_tokens_per_sec`: generated token throughput after prefill only
- `overall_tokens_per_sec`: `(prompt_tokens + generated_tokens) / total_time`

This keeps `generation` aligned with the post-prefill autoregressive phase only.

## Flush Policy

- keep in-memory row results as the batch runs
- flush the current result TSV and summary after every 10 completed rows
- flush again on:
  - normal batch completion
  - explicit user stop
  - recoverable batch failure before returning control to the UI

The purpose is to bound data loss if the app is killed during a long run.

## Resume Policy

Resume should be based on `unique_idx`.

- the app should be able to read an existing result TSV
- rows whose `unique_idx` already exist in the result TSV should be skipped
- only remaining rows should be evaluated
- summary metrics should be recomputed or incrementally updated from the saved per-case rows

## Source Matching

To reduce accidental mismatch between a gold TSV and an old result TSV, the persisted summary should also store:

- source TSV file name
- source TSV row count if known
- active batch mode
- prompt preset or prompt hash if practical

## UI Direction

The app does not need to expose every persisted column inline.

The UI should expose only:

- current result file path or name
- last flush count or flush status
- whether the current run is fresh or resumed

Detailed analysis should happen from the exported TSV outside the app.

