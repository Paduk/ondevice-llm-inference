# Task 31: JSON And Length Parity

## Goal

Reduce remaining output-shape and generation-length differences between Python and Android.

## Scope

- review JSON output forcing options on Android
- align generation length limits if the engine exposes them
- document any unavoidable runtime differences

## Required Reads

- [inference-parity-spec.md](../specs/inference-parity-spec.md)
- [python-vs-apk-inference-gap-ko.md](../python-vs-apk-inference-gap-ko.md)

## Likely Files

- `smollm/src/main/java/io/shubham0204/smollm/SmolLM.kt`
- `app/src/main/java/io/shubham0204/smollmandroid/ui/customapp/CustomAppJsonParser.kt`
- `app/src/main/java/io/shubham0204/smollmandroid/ui/customapp/CustomAppSetupViewModel.kt`
- `docs/custom-app/roadmap.md`

## Completion Criteria

- remaining output-control gaps are smaller and clearly documented
- parity testing instructions are easier to follow
