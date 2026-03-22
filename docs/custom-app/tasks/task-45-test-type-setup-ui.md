# Task 45: Test Type Setup UI

## Goal

Refactor setup so it selects `Test Type` first, then reveals only the relevant model or pipeline choices.

## Read First

- [test-type-routing-spec.md](../specs/test-type-routing-spec.md)
- [prompt-preset-spec.md](../specs/prompt-preset-spec.md)

## Scope

- add `Toolcalling`, `RMA`, and `E2E` test-type selection
- show only matching downstream choices
- keep the screen cleaner than a single all-options matrix

## Completion Criteria

- setup shows three top-level test types
- tool-calling options appear only for `Toolcalling`
- RMA options appear only for `RMA`
- E2E pipeline options appear only for `E2E`
