# Batch Session Reset Spec

## Goal

Define a faster batch execution model where the GGUF model is loaded once and each TSV row runs as a fresh single-turn inference without reloading the model.

## Target Behavior

- load the model once at batch start
- keep the loaded model resident for the full batch run
- before each row:
  - clear prior user and assistant messages from the in-memory conversation state
  - clear model KV/cache state used for the previous row
  - re-apply the active system prompt
- run one rendered prompt as a fresh single-turn inference
- after batch completion:
  - restore the normal interactive chat session

## Why This Is Needed

- current batch flow reloads the model for every selected row
- reload-per-row is safe but slow on-device
- `storeChats=false` is not enough because the current native implementation still appends user queries into `_messages`

## Reset Semantics

- reset must clear all row-specific conversation state
- reset must not unload model weights
- reset must preserve loaded model configuration:
  - model path
  - temperature
  - min-p
  - context size
  - threads
  - mmap / mlock
  - chat template

## Minimum Required API Surface

- native layer:
  - clear `_messages`
  - clear llama memory / KV cache
  - clear partial response buffers
- Kotlin wrapper:
  - expose a reset method without forcing model reload
- manager layer:
  - expose a batch-safe reset-and-prime method

## Safety Requirements

- each batch row must remain independent
- no row may inherit assistant output or user input from a previous batch row
- stopping a batch run must leave the interactive session recoverable

## Validation Expectations

- batch top-1 and batch all should show lower overhead than reload-per-row
- prompt/context length should reflect only the current row plus system prompt
- repeated runs on the same TSV should not leak row-to-row context
