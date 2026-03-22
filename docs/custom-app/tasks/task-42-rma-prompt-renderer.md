# Task 42: RMA Prompt Renderer

## Goal

Render RMA prompts using the exact input construction used by the Python `preprocess_example_it(..., test_type="sft")` path.

## Read First

- [rma-rewrite-spec.md](../specs/rma-rewrite-spec.md)

## Scope

- support `RMA-Qwen3` and `RMA-Phi` prompt presets
- build `{data}` as a JSON string from TSV `conversation_history` and `query`
- show a readable rendered-prompt preview in the RMA flow

## Completion Criteria

- RMA prompts use the correct external templates
- `{data}` is filled with the expected JSON string
- RMA preview matches the Python preprocessing logic closely
