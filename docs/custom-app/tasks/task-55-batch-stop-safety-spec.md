# Task 55: Batch Stop Safety Spec

## Goal

Freeze the stop or cancel safety contract for long-running batch runs before implementation changes are made.

## Scope

- define the lifecycle for batch stop across Toolcalling, RMA, and E2E
- define when unload or reload is allowed relative to cancellation
- define expected UI states for `Running`, `Stopping`, `Stopped`, and failure
- define whether stop is treated as normal completion or as an error

## Read First

- [batch-stop-safety-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/batch-stop-safety-spec.md)
- [batch-run-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/batch-run-spec.md)
- [batch-session-reset-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/batch-session-reset-spec.md)

## Completion Criteria

- a single stop or cancel contract is documented for all batch flows
- native unload ordering relative to cancellation is explicitly frozen
- UI expectations for stopped state are documented
- the roadmap checklist can mark batch stop safety assumptions as frozen
