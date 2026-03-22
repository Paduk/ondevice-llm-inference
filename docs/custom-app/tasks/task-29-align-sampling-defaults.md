# Task 29: Align Sampling Defaults

## Goal

Bring Android default inference parameters closer to the Python Ollama path.

## Scope

- align default temperature for parity testing
- review min-p handling
- introduce or document generation length alignment if supported

## Required Reads

- [inference-parity-spec.md](../specs/inference-parity-spec.md)
- [python-vs-apk-inference-gap-ko.md](../python-vs-apk-inference-gap-ko.md)

## Likely Files

- `app/src/main/java/io/shubham0204/smollmandroid/ui/customapp/CustomAppSetupViewModel.kt`
- `app/src/main/java/io/shubham0204/smollmandroid/ui/customapp/CustomAppChatViewModel.kt`
- `smollm/src/main/java/io/shubham0204/smollm/SmolLM.kt`
- `docs/custom-app/roadmap.md`

## Completion Criteria

- Android defaults used for parity tests are documented and aligned as closely as practical
- known remaining differences are explicitly noted
