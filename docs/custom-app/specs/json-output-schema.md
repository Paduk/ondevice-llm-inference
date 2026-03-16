# JSON Output Schema

## Purpose

This file defines the MVP assumption for model output parsing.

## Current Status

- Status: `frozen-for-mvp`

## MVP Schema

Model output must be a single valid JSON object per inference turn.

Required top-level keys:

- `query`: string
- `rewrited_query`: string
- `generated`: string
- `answer`: string

Optional top-level keys:

- `meta`: object
- `rationale`: string
- any additional keys, which are ignored by the MVP evaluator

## Example

```json
{
  "query": "original question",
  "rewrited_query": "rewritten question",
  "generated": "model response",
  "answer": "positive"
}
```

## MVP Parsing Rule

- Model output is expected to be valid JSON.
- The first parser is strict.
- If model output is malformed JSON, the app surfaces a parse failure instead of attempting heuristic repair.
- The MVP parser extracts only:
  - `query`
  - `rewrited_query`
  - `generated`
  - `answer`
- If either required key is missing, the parse fails.
- If either required key is not a string, the parse fails.

## Scope Constraints

- One inference turn produces one prediction object.
- The MVP does not support arrays of predictions.
- The JSON object must map to exactly one TSV row by `unique_idx`.

## Notes For Later Expansion

- Multiple prediction pairs can be added later by introducing an array-based schema version.
- Nested label structures are out of scope for the MVP.
