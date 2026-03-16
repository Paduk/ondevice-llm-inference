# Evaluation Rule

## Purpose

This file defines the MVP evaluation rule for comparing JSON predictions against TSV gold labels.

## Current Status

- Status: `frozen-for-mvp`

## MVP Rule

- Parse prediction output from model JSON.
- Load gold labels from TSV.
- Compare prediction against gold using strict equality for the MVP.
- Compute Macro Accuracy across the label set.

## Frozen MVP Rule

### Unit Of Comparison

- One inference turn produces one prediction.
- The prediction is matched to one TSV row using the tuple:
  - `query`
  - `rewrited_query`
- The compared values are:
  - JSON `answer`
  - TSV `answer`

### Match Rule

- Comparison uses exact string equality.
- Matching is case-sensitive in the MVP.
- Whitespace is trimmed at parse/load time.

### Missing Or Invalid Cases

- If JSON parsing fails, the turn is marked as `parse_error`.
- If no TSV row matches the JSON `query` and `rewrited_query`, the turn is marked as `missing_gold`.
- If `unique_idx` appears more than once in the TSV, TSV loading fails.
- If JSON `answer` is empty after trimming, the turn is invalid.

### Session Scope

- Evaluation is both:
  - per-turn, for the latest prediction
  - cumulative across the current chat session

### Macro Accuracy Definition For MVP

- Let each gold label define a class.
- For the evaluated samples in the current session, compute per-class accuracy:
  - `correct predictions for that gold answer / total evaluated samples for that gold answer`
- Macro Accuracy is the arithmetic mean of those per-class accuracies.
- Only classes that appear in the evaluated session samples are included in the mean.

## Notes For Later Expansion

- More tolerant normalization rules can be added later if needed.
- Alternative metrics such as micro accuracy, F1, or confusion matrices are out of scope for MVP.
