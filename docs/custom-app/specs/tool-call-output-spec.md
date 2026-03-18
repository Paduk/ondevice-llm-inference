# Tool Call Output Spec

## Purpose

Define the actual model-output and gold-answer shape for the current SFT tool-calling task.

## Actual Output Shape

The current task does not produce the older normalized object:

- `query`
- `rewrited_query`
- `generated`
- `answer`

Instead, both model output and TSV `answer` are expected to represent a tool call shaped like:

```text
{'plan': 'PLAN_NAME', 'arguments': {...}}
```

## Required Top-Level Keys

- `plan`
- `arguments`

## Value Rules

- `plan` must normalize to a string
- `arguments` must normalize to a dictionary-like object
- `arguments` may be empty
- `arguments` may contain nested dictionaries and lists

## Parsing Rules

- standard JSON parsing may be attempted first
- Python-style single-quote dict strings must also be supported
- nested dictionaries and lists must be supported
- parser output should normalize into a structured tool-call object rather than a flat prediction object

## Evaluation Rules

- TSV `answer` must be parsed structurally, not compared as a raw string
- model output must be parsed structurally, not compared as a raw string
- both sides should be converted into the same canonical normalized structure before comparison
- key order differences must not affect correctness

## Canonical Comparison Direction

Recommended normalization:

1. parse into nested structured objects
2. recursively sort dictionary keys
3. preserve list order
4. compare by deep equality or canonical serialized form

## Compatibility Note

The older `ParsedPrediction` path is no longer the right schema for the current SFT tool-calling evaluation flow.
