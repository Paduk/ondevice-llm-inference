# Task 30: Raw Prompt Batch Path

## Goal

Add or define a batch execution path that sends a fully rendered prompt without extra chat-template wrapping so Android can be compared more directly with the Python Ollama path.

## Scope

- inspect whether the current native API can support raw prompt execution directly
- if needed, add a dedicated raw-prompt batch path
- preserve the current chat-template-based path unless intentionally replaced

## Required Reads

- [inference-parity-spec.md](../specs/inference-parity-spec.md)

## Likely Files

- `smollm/src/main/java/io/shubham0204/smollm/SmolLM.kt`
- `smollm/src/main/cpp/LLMInference.cpp`
- `app/src/main/java/io/shubham0204/smollmandroid/llm/SmolLMManager.kt`
- `app/src/main/java/io/shubham0204/smollmandroid/ui/customapp/CustomAppChatViewModel.kt`
- `docs/custom-app/roadmap.md`

## Completion Criteria

- Android has a documented or implemented way to run a fully rendered prompt for parity testing
- extra chat wrapping is either removed for that path or explicitly measured as a remaining gap
