# Task 56: Batch Stop Crash Fix

## Goal

Fix the batch `Stop` crash by making cancellation, generation shutdown, native unload, and interactive-state restoration happen in a safe order.

## Scope

- update Toolcalling batch stop flow
- update RMA batch stop flow
- update E2E batch stop flow
- update shared `SmolLMManager` cancellation and unload handling
- add any native or JNI safety needed so in-flight completion does not race with close

## Read First

- [batch-stop-safety-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/batch-stop-safety-spec.md)
- [batch-run-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/batch-run-spec.md)
- [batch-session-reset-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/batch-session-reset-spec.md)

## Completion Criteria

- pressing `Stop` during Toolcalling batch no longer crashes the app
- pressing `Stop` during RMA batch no longer crashes the app
- pressing `Stop` during E2E batch no longer crashes the app
- unload or reload no longer races with active native generation
- the app can start another batch after stop without restart
