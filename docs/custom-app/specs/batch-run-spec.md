# Batch Run Spec

## Goal

Define the supported batch execution modes and how rows are selected from the uploaded TSV.

## Supported Modes

- `First 1`
- `Random 10`
- `All`

## Row Selection Rules

### First 1

- use the first valid TSV row only

### Random 10

- sample up to 10 unique rows from the loaded TSV
- if the TSV contains fewer than 10 rows, use all rows
- random selection should happen once at the start of the run

### All

- process every valid TSV row in file order

## Batch Inference Rules

- each selected row produces one rendered prompt from the active prompt template
- each rendered prompt is executed independently
- evaluation is performed per row using the existing JSON parser and TSV comparison rule

## Batch Summary Expectations

- total selected rows
- completed rows
- failed rows
- latest processed row id if available
- cumulative Macro Accuracy

## MVP UI Assumption

- use a simple single-choice control for batch mode
- if a checkbox-like UI is used, it must still enforce exactly one active mode
