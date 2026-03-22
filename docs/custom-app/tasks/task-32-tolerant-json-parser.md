# Task 32: Tolerant JSON Parser

## Goal

Relax model-output parsing so both JSON-style double quotes and Python-style single-quote dict strings can be accepted when they still map to the required normalized schema.

## Read First

- [parser-and-batch-retention-spec.md](../specs/parser-and-batch-retention-spec.md)
- [json-output-schema.md](../specs/json-output-schema.md)

## Scope

- keep the current strict JSON path as the first parsing attempt
- add a fallback parser for Python-like single-quote object strings
- keep required-key and string-value validation
- preserve clear parse errors when both paths fail

## Likely Files

- [CustomAppJsonParser.kt](../../../app/src/main/java/io/shubham0204/smollmandroid/ui/customapp/CustomAppJsonParser.kt)
- [CustomAppChatViewModel.kt](../../../app/src/main/java/io/shubham0204/smollmandroid/ui/customapp/CustomAppChatViewModel.kt)

## Completion Criteria

- outputs with standard JSON quotes still parse
- outputs with Python-style single quotes can parse when structurally valid
- malformed outputs still fail clearly
- normalized result shape remains unchanged
