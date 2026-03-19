# Batch Run Spec

## Goal

Define the supported batch execution modes and how rows are selected from the uploaded TSV.

## Supported Modes

- `Top 1`
- `Top 50`
- `All`

## Row Selection Rules

### Top 1

- use the first valid TSV row only

### Top 50

- use up to the first 50 valid TSV rows in file order
- if the TSV contains fewer than 50 rows, use all rows

### All

- process every valid TSV row in file order

## Batch Inference Rules

- each selected row produces one rendered prompt from the active prompt template
- each rendered prompt is executed independently
- evaluation is performed per row using the existing JSON parser and TSV comparison rule

## Current Implementation Note

- the current batch runner reloads the model for every row to guarantee isolation
- this is correct but slower than needed on-device

## Planned Optimization Direction

- move to one model load per batch run
- reset conversation and KV/cache state between rows
- keep each row as a fresh single-turn inference
- restore the interactive chat session after batch completion

## Batch Summary Expectations

- total selected rows
- completed rows
- failed rows
- latest processed row id if available
- cumulative Macro Accuracy
- periodic persistence progress if result-export is enabled

## MVP UI Assumption

- use a simple single-choice control for batch mode
- if a checkbox-like UI is used, it must still enforce exactly one active mode
