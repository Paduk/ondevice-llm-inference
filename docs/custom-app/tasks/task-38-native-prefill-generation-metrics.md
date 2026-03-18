# Task 38: Native Prefill And Generation Metrics

## Goal

Extend native and Kotlin runtime metrics so prefill and generation are measured separately and exposed to the app layer.

## Read First

- [runtime-metrics-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/runtime-metrics-spec.md)

## Scope

- separate prefill and generation timing in native inference flow
- expose prompt token count and generated token count
- update Kotlin and manager response structures

## Likely Files

- [LLMInference.h](/home/hj153lee/SmolChat-Android/smollm/src/main/cpp/LLMInference.h)
- [LLMInference.cpp](/home/hj153lee/SmolChat-Android/smollm/src/main/cpp/LLMInference.cpp)
- [smollm.cpp](/home/hj153lee/SmolChat-Android/smollm/src/main/cpp/smollm.cpp)
- [SmolLM.kt](/home/hj153lee/SmolChat-Android/smollm/src/main/java/io/shubham0204/smollm/SmolLM.kt)
- [SmolLMManager.kt](/home/hj153lee/SmolChat-Android/app/src/main/java/io/shubham0204/smollmandroid/llm/SmolLMManager.kt)

## Completion Criteria

- native layer exposes separate prefill and generation timing
- prompt token count is exposed
- generated token count is exposed
- manager response includes the new metrics
