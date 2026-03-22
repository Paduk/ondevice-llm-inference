# Task 34: Tool-Call Schema Spec

## Goal

Freeze the actual output schema for the current task so parsing and evaluation can move from the old flat prediction shape to the real `plan + arguments` tool-call shape.

## Read First

- [tool-call-output-spec.md](../specs/tool-call-output-spec.md)
- [evaluation-rule.md](../specs/evaluation-rule.md)

## Scope

- document the real top-level output shape
- freeze the required keys and nested-value expectations
- define structural rather than raw-string comparison as the target

## Completion Criteria

- docs clearly state that current output is `plan` plus `arguments`
- docs clearly state that nested dict parsing is required
- docs clearly state that evaluation should use structural normalization
