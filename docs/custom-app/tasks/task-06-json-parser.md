# Task 06: JSON Output Parser MVP

## Goal

Parse model output assuming valid JSON response format.

## Input Spec

- [json-output-schema.md](../specs/json-output-schema.md)

## Scope

- parse raw model output as JSON
- extract prediction fields needed for evaluation
- show parse success or parse failure

## Notes

- first version should be strict, not heuristic
- malformed JSON should fail explicitly

## Completion Criteria

- valid model output produces a normalized prediction object

