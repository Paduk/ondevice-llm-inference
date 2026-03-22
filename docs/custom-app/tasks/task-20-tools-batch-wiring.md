# Task 20: Wire Tools Rendering Into Batch Runs

## Goal

Use rendered `{tools}` strings during real batch inference and preview generation.

## Scope

- update prompt preview to include tools rendering
- use `{tools}` during batch prompt generation
- keep row-level warnings available for summary or debug UI

## Required Specs

- [tools-injection-spec.md](../specs/tools-injection-spec.md)
- [batch-run-spec.md](../specs/batch-run-spec.md)

## Completion Criteria

- batch runs render prompts with `{tools}` when the placeholder is present
