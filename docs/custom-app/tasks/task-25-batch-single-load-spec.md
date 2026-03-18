# Task 25: Batch Single-Load Spec

## Goal

Freeze the design for batch execution so the model is loaded once and each row uses a fresh single-turn context.

## Scope

- document single-load batch assumptions
- define reset semantics between rows
- define what state must be cleared and what state must be preserved
- confirm that interactive chat restoration still happens after batch completion

## Required Reads

- [batch-run-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/batch-run-spec.md)
- [batch-session-reset-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/batch-session-reset-spec.md)

## Likely Files

- `docs/custom-app/specs/batch-run-spec.md`
- `docs/custom-app/specs/batch-session-reset-spec.md`
- `docs/custom-app/roadmap.md`

## Completion Criteria

- docs clearly describe one-load batch behavior
- row independence rules are frozen
- roadmap reflects the new optimization tasks
