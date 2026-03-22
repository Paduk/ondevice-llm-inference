# RMA Ondevice Inference App Architecture

## Product Direction

The target app is a compact Android APK built from `SmolChat-Android` with these core capabilities:

- local GGUF model import
- editable system prompt and inference parameters
- on-device inference
- multi-turn chat
- JSON output parsing
- TSV-based evaluation with Macro Accuracy
- runtime metrics display

## Reuse Strategy

### Keep As Core Foundation

- `smollm/`
  - `SmolLM.kt`
  - JNI bridge
  - `LLMInference.cpp`
  - `GGUFReader`
- current `llama.cpp` integration
- current model loading APIs
- current generation speed and context-length APIs
- current multi-turn conversation-memory logic
- Koin and Room initially

### Reuse Partially

- local GGUF import logic from `DownloadModelsViewModel`
- model metadata persistence from `ModelsDB`
- chat/message persistence concepts from `ChatsDB` and `MessagesDB`
- `SmolLMManager` as the inference orchestrator

### Remove Or Disable

- ASR-related screens, services, and permissions
- Hugging Face browsing/search/download UI from the main flow
- task-management UI and task DB
- folder-driven navigation
- markdown-heavy rendering if plain JSON display is sufficient

## Target App Structure

### Screen Plan

#### Setup Screen

Responsibilities:

- import/select GGUF model
- edit system prompt
- edit inference parameters
- select or prepare TSV gold data
- start a new session

#### Chat And Evaluate Screen

Responsibilities:

- run multi-turn inference
- display raw JSON output
- show parsed prediction
- show evaluation result
- show Macro Accuracy
- show runtime metrics

## Multi-turn Behavior

The app should preserve multi-turn conversation history.

Flow:

1. User sends a message.
2. Model generates a response.
3. Assistant response is stored in conversation history.
4. Next user message is sent.
5. Inference runs with:
   - system prompt
   - prior user messages
   - prior assistant messages

The app does not need a separate mechanism to inject the previous response manually if the chat history is persisted and replayed correctly.

## Proposed Data Model

### Keep

- model metadata table
- message history table
- chat/session container if useful for managing a single active session

### Simplify

- one active chat/session is sufficient for MVP
- no folders
- no tasks

### Add

- normalized prediction model
- gold TSV row model
- evaluation result model
- optional session summary model

## File-Level Impact

### Likely To Be Modified

- `app/src/main/AndroidManifest.xml`
- `app/build.gradle.kts`
- `app/src/main/java/io/shubham0204/smollmandroid/MainActivity.kt`
- `app/src/main/java/io/shubham0204/smollmandroid/llm/SmolLMManager.kt`
- model/chat/message persistence and repository files

### Likely To Be Added

- new compact setup screen
- new compact chat/evaluate screen
- evaluation package
- TSV parser utility
- JSON parser utility

### Likely To Be Removed Later

- ASR files
- task-management files
- folder-navigation files
- HF browsing/downloading screens if no longer needed
