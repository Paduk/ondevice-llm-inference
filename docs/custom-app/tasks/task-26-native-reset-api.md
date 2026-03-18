# Task 26: Native Reset API

## Goal

Add a reset pathway that clears conversation and KV/cache state without unloading the model.

## Scope

- extend native inference code with a reset method
- clear message buffers and cached response buffers
- clear llama memory / KV cache for the loaded context
- expose the reset method through JNI and the Kotlin `SmolLM` wrapper

## Required Reads

- [batch-session-reset-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/batch-session-reset-spec.md)

## Likely Files

- `smollm/src/main/cpp/LLMInference.cpp`
- `smollm/src/main/cpp/LLMInference.h`
- `smollm/src/main/cpp/smollm.cpp`
- `smollm/src/main/java/io/shubham0204/smollm/SmolLM.kt`
- `docs/custom-app/roadmap.md`

## Completion Criteria

- loaded model can be reset without full unload
- reset clears conversation state and KV/cache state
- Kotlin layer can call the reset path directly
