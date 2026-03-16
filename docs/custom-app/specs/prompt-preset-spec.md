# Prompt Preset Spec

## Goal

Define how setup-time prompt presets work before UI implementation.

## Preset Options

- `Model A`
- `Model B`
- `Model C`

## Preset Keys

- `model_a`
- `model_b`
- `model_c`

## MVP Assumptions

- each preset maps to one predefined system prompt template
- selecting a preset replaces the editable system prompt field contents
- the user may still manually edit the populated prompt after selection
- preset selection is independent from the GGUF filename unless a future task adds explicit auto-mapping
- preset selection is optional and the user may leave the field on a custom prompt
- there is no automatic preset change when the GGUF model selection changes
- the initial default state should be `Custom` with no preset selected

## Setup Screen Behavior

- expose four options in setup:
  - `Custom`
  - `Model A`
  - `Model B`
  - `Model C`
- choosing `Model A`, `Model B`, or `Model C` immediately overwrites the current system prompt field with that preset template
- choosing `Custom` does not overwrite the current system prompt text
- once a preset has populated the field, the user may edit the prompt freely without clearing the selected preset immediately
- if the user edits the populated prompt manually, the system should still treat the selected preset as the last applied preset for MVP

## Template Ownership

- each preset owns one raw template string
- the template string may include placeholders that later batch tasks will render dynamically
- the setup screen should store the resolved text currently visible in the editor, not only the preset key

## Persistence

- save the last selected preset key
- save the last resolved prompt text after preset application
- if `Custom` is active, persist it as `custom`
- if no preset has ever been selected, startup should behave like `custom`

## Deferred Work

- domain-specific preset labels can replace `Model A/B/C` later without changing the key structure
- automatic mapping from GGUF filename to preset key is explicitly out of scope for this task
