# Task 27: Batch Runner Single-Load Refactor

## Goal

Refactor the batch runner to load once, reset between rows, and restore the interactive session after the batch run.

## Scope

- stop reloading the model for every batch row
- batch start:
  - unload the interactive session once
  - load one batch session once
- for each row:
  - reset batch conversation state
  - re-apply system prompt
  - run one rendered prompt
- batch end:
  - unload the batch session
  - restore the interactive session

## Required Reads

- [batch-run-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/batch-run-spec.md)
- [batch-session-reset-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/batch-session-reset-spec.md)

## Likely Files

- `app/src/main/java/io/shubham0204/smollmandroid/llm/SmolLMManager.kt`
- `app/src/main/java/io/shubham0204/smollmandroid/ui/customapp/CustomAppChatViewModel.kt`
- `docs/custom-app/roadmap.md`

## Completion Criteria

- batch no longer performs reload-per-row
- row prompts remain independent
- interactive chat still works after batch completion or stop
- metrics and evaluation continue to function
