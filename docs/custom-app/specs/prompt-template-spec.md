# Prompt Template Spec

## Goal

Define how TSV row values are injected into the active prompt template for batch runs.

## Supported Placeholders

- `{query}`
- `{rewrited_query}`
- `{conversation_history}`

## Input Source

Each placeholder is populated from the currently selected TSV row.

- `{query}` -> TSV `query`
- `{rewrited_query}` -> TSV `rewrited_query`
- `{conversation_history}` -> TSV `conversation_history`

## Rendering Rules

- simple string replacement is sufficient for MVP
- all supported placeholders should be replaced every run
- missing placeholders in the prompt are allowed
- unknown placeholders are left unchanged for MVP
- empty TSV values are allowed and should render as empty strings

## Output

- the rendered prompt text is what gets passed to inference for that row
- the original preset template should remain unchanged in state

## Debugging Expectation

- the UI should make it possible to inspect the rendered prompt or at least the rendered row inputs during batch runs
