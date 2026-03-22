# Task 11: Freeze Prompt Preset Assumptions

## Goal

Freeze the preset-prompt behavior before modifying the setup UI.

## Scope

- define preset keys for `Model A`, `Model B`, and `Model C`
- define how preset selection updates the system prompt field
- define what gets persisted across app restarts
- define what happens when the user edits a preset-populated prompt manually

## Required Specs

- [prompt-preset-spec.md](../specs/prompt-preset-spec.md)

## Completion Criteria

- prompt preset assumptions are stable enough for UI and persistence work
- preset selection, overwrite behavior, and persistence rules are explicit
