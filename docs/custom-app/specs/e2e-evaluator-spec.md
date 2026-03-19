# E2E Evaluator Spec

## Purpose

Define the end-to-end evaluation flow that chains RMA rewrite inference into tool-calling inference.

## Flow

For each TSV row:

1. run the selected RMA model using TSV `conversation_history` and `query`
2. treat the RMA model output as the effective `rewrited_query`
3. run the matching tool-calling rewrite model with that predicted rewrite
4. compare only the final tool-call output against TSV `answer`

## Model Pairing

The first E2E version supports only same-family pipelines.

- `Qwen3 pipeline` = `Qwen3-RMA` -> `Qwen3-Rewrite`
- `Phi pipeline` = `Phi-RMA` -> `Phi-Rewrite`

## Evaluation Rule

The app should not score the intermediate RMA output.

Only the final tool-calling output should be evaluated against TSV `answer`.

## Display Guidance

The UI should still show the intermediate rewrite for debugging, but it should not affect the score.

Recommended visible outputs:

- intermediate rewrite text
- final tool-call output
- final correctness
- E2E batch summary
