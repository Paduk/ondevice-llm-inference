# Task 15: Implement Batch Inference Runner

## Goal

Run repeated on-device inference across selected TSV rows using the rendered prompt template.

## Scope

- select the correct row subset for the chosen batch mode
- render one prompt per row
- execute inference repeatedly
- reuse existing JSON parsing and per-row evaluation logic

## Required Specs

- [batch-run-spec.md](../specs/batch-run-spec.md)
- [prompt-template-spec.md](../specs/prompt-template-spec.md)
- [evaluation-rule.md](../specs/evaluation-rule.md)

## Completion Criteria

- batch execution can run first 1, random 10, or all selected rows
- each row triggers inference with a rendered prompt
- batch state tracks progress and failures
