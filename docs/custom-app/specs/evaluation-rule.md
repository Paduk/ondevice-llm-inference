# Evaluation Rule

## Purpose

This file defines the evaluation rule for comparing current tool-calling model outputs against TSV gold labels.

## Current Status

- Status: `tool-call-schema-frozen`

## Current Rule Direction

- Parse model output into a structured tool-call object.
- Parse TSV `answer` into the same structured tool-call object shape.
- Compare the two sides structurally after canonical normalization.
- Compute Macro Accuracy across the gold label set represented by normalized tool-call outputs.

## Frozen Comparison Unit

### Unit Of Comparison

- One inference turn produces one tool-call prediction.
- The prediction is matched to one TSV row using the tuple:
  - `query`
  - `rewrited_query`
- The compared values are:
  - parsed model tool call
  - parsed TSV `answer` tool call

### Tool-Call Shape

- The required top-level keys are:
  - `plan`
  - `arguments`
- `plan` must normalize to a string.
- `arguments` must normalize to a dictionary-like object.
- `arguments` may be empty.
- `arguments` may contain nested dictionaries and lists.

### Match Rule

- Comparison target is structural equality after normalization.
- Dictionary key order must not affect correctness.
- List order is preserved and remains significant.
- Raw string equality is no longer the intended comparison rule for this task.

### Missing Or Invalid Cases

- If model output parsing fails, the turn is marked as `parse_error`.
- If no TSV row matches the current `query` and `rewrited_query`, the turn is marked as `missing_gold`.
- If `unique_idx` appears more than once in the TSV, TSV loading fails.
- If the parsed model output is missing `plan` or `arguments`, the turn is invalid.
- If the parsed TSV `answer` is missing `plan` or `arguments`, the gold row is invalid.

### Session Scope

- Evaluation is both:
  - per-turn, for the latest prediction
  - cumulative across the current chat session or batch run

### Macro Accuracy Definition

- Let each normalized gold tool-call output define a class.
- For the evaluated samples in the current session, compute per-class accuracy:
  - `correct predictions for that gold tool-call class / total evaluated samples for that gold tool-call class`
- Macro Accuracy is the arithmetic mean of those per-class accuracies.
- Only classes that appear in the evaluated session samples are included in the mean.

## Compatibility Note

- The older flat schema based on `query`, `rewrited_query`, `generated`, and `answer` is not the active evaluation target for the current SFT tool-calling workflow.
