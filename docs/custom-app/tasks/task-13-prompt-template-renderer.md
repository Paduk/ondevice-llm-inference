# Task 13: Implement Prompt Template Renderer

## Goal

Render the active prompt with TSV row values before each batch inference call.

## Scope

- create a small renderer for supported placeholders
- keep the original template intact
- produce a rendered prompt string for one TSV row at a time

## Required Specs

- [prompt-template-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/prompt-template-spec.md)
- [tsv-schema.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/tsv-schema.md)

## Completion Criteria

- the renderer can produce a final prompt from a row using `{query}`, `{rewrited_query}`, and `{conversation_history}`
