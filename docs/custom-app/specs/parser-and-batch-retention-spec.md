# Parser And Batch Retention Spec

## Purpose

Define two follow-up usability corrections:

- tolerate both JSON-style double quotes and Python-style single quotes in model output parsing
- keep raw batch outputs visible in the conversation view until a new batch run starts

## Parsing Tolerance Rules

- Standard JSON parsing remains the first attempt.
- If standard JSON parsing fails, the app may try a tolerant fallback for Python-like dict strings.
- The tolerant path is only for object-shaped outputs that still map to the same normalized schema.
- Parsing must still require all of these keys:
  - `query`
  - `rewrited_query`
  - `generated`
  - `answer`
- All required values must still normalize to strings.
- If both parsing paths fail, the app reports parse failure.

## Batch Raw Output Retention Rules

- During a batch run, each model raw output should remain visible in the conversation section even if parse or evaluation fails.
- Parse failure must not erase or overwrite the raw assistant output that was already shown.
- Evaluation failure must not erase or overwrite the raw assistant output that was already shown.
- Starting a new batch run clears the previous batch conversation outputs and batch-specific derived results.
- Stopping or completing the current batch does not clear the current batch conversation outputs immediately.

## UI Expectations

- `Conversation` remains the source of truth for raw model outputs.
- `Parse Result` and `Evaluation` continue to describe how the latest output was interpreted.
- A new batch run resets batch-local conversation history so the next run starts cleanly.
