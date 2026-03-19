# Test Type Routing Spec

## Purpose

Define the top-level evaluation routing model for the app.

## Test Types

The app should expose exactly three top-level test types.

- `Toolcalling`
- `RMA`
- `E2E`

## Routing

Each test type should route to its own evaluator activity or screen flow.

- `Toolcalling` -> tool-calling evaluator activity
- `RMA` -> RMA rewrite evaluator activity
- `E2E` -> dedicated E2E evaluator activity

These flows may reuse common UI components, model-loading logic, batch-running logic, and runtime metrics, but they should not share the same evaluator logic.

## Model Selection

The setup UI should first ask for `Test Type`, then show only the model options relevant to that type.

### Toolcalling

Show four options:

- `Qwen3-Rewrite`
- `Qwen3-Base`
- `Phi-Rewrite`
- `Phi-Base`

### RMA

Show two options:

- `Qwen3-RMA`
- `Phi-RMA`

### E2E

Show pipeline options instead of exposing two separate model pickers.

Initial supported pipelines:

- `Qwen3 pipeline`
- `Phi pipeline`

This means:

- `Qwen3 pipeline` = `Qwen3-RMA` then `Qwen3-Rewrite`
- `Phi pipeline` = `Phi-RMA` then `Phi-Rewrite`

Cross-family pipelines are out of scope for the first version.

## UI Guidance

The UI should reduce visual clutter by:

- showing `Test Type` first
- rendering only the relevant downstream model or pipeline options
- avoiding a single screen that shows every preset or every evaluator choice at once
