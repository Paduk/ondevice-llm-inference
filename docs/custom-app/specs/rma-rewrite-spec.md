# RMA Rewrite Spec

## Purpose

Define how the app should support the RMA rewrite task separately from the existing tool-calling task.

## Task Boundary

The RMA rewrite task is a different evaluation mode.

- input prompt shape is different
- model output shape is different
- evaluation target is different

It should not reuse the current tool-calling parser and evaluator directly.

## Prompt Behavior

RMA inference uses the prompts from `/home/hj153lee/RMA/train/llama_prompts.py`.

- `RMA-Qwen3` uses `SFT_RMA_INFERENCE_QWEN3`
- `RMA-Phi` uses `SFT_RMA_INFERENCE_PHI4`

The effective `{data}` value follows `/home/hj153lee/RMA/rma_inference.py` `preprocess_example_it(..., test_type="sft")`.

The app should build:

```json
{
  "conversation_history": "<tsv conversation_history>",
  "query": "<tsv query>"
}
```

and inject that JSON string into `{data}`.

## Output Behavior

RMA output is rewrite text, not tool-call JSON.

The target output is the TSV `rewrited_query` value.

## Screen Strategy

The app should add a separate RMA evaluation activity or screen flow under the top-level `Test Type` routing model.

- reuse common model-loading, batch-running, and metrics UI where practical
- reuse the same runtime-metrics presentation used by Toolcalling
- reuse the same batch export-actions UI used by Toolcalling
- do not mix RMA evaluation into the existing tool-calling evaluation screen
- do not mix RMA evaluation into the future E2E evaluator flow either

## Evaluation Direction

The first RMA evaluator can be simple exact-match against TSV `rewrited_query`.

Future improvements may add normalization rules, but the initial path should stay simple and explicit.
