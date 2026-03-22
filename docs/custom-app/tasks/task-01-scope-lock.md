# Task 01: Scope Lock

## Goal

Freeze the minimum assumptions required to implement the first usable MVP.

## Inputs

- [json-output-schema.md](../specs/json-output-schema.md)
- [tsv-schema.md](../specs/tsv-schema.md)
- [evaluation-rule.md](../specs/evaluation-rule.md)

## Decisions To Finalize

- JSON output shape
- TSV column structure
- whether gold TSV is bundled, imported, or both
- whether one active chat session is enough for MVP

## Deliverable

- update the relevant files in `specs/`
- update [roadmap.md](../roadmap.md)

## Completion Criteria

- later implementation tasks can proceed without inventing missing schema assumptions

## MVP Decision Summary

- JSON output is a single object with required keys `query`, `rewrited_query`, `generated`, and `answer`
- TSV requires `conversation_history`, `query`, `rewrited_query`, `answer`, `unique_idx`, and `candidates`
- gold TSV is imported by the user from local storage
- one active chat session is sufficient for MVP
