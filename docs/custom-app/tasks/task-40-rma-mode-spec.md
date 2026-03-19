# Task 40: RMA Mode Spec

## Goal

Freeze the RMA rewrite task as a separate evaluation mode instead of extending the existing tool-calling mode.

## Read First

- [rma-rewrite-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/rma-rewrite-spec.md)

## Scope

- define why RMA must be split from tool-calling
- freeze the prompt input shape for RMA presets
- freeze the output and evaluation target for RMA

## Completion Criteria

- docs clearly state that RMA uses a separate activity or screen flow
- docs clearly state that `{data}` is built from TSV `conversation_history` and `query`
- docs clearly state that RMA evaluation targets TSV `rewrited_query`
