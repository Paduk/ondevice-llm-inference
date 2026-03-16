# Task 03: Setup Screen MVP

## Goal

Provide a single screen to prepare inference.

## Scope

- import/select local GGUF model
- show selected model metadata
- edit system prompt
- edit inference parameters:
  - `temperature`
  - `min-p`
  - `context size`
  - `num threads`
  - optional `use mmap`
  - optional `use mlock`
- prepare or select gold TSV source
- navigate to the chat/evaluate screen

## Reuse Targets

- local GGUF import logic
- `GGUFReader`
- model metadata persistence

## Completion Criteria

- user can complete setup without using any legacy screen

