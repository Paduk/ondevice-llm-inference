# Custom App Docs Map

## Purpose

This directory is the document hub for the compact custom app built from `SmolChat-Android`.

Use this map first, then open only the task/spec files needed for the current implementation step.

## Read Order

1. `README.md`
2. `roadmap.md`
3. the relevant file in `tasks/`
4. any referenced file in `specs/`
5. `architecture.md` only if code-structure context is needed

## Document Map

### Project-Level Docs

- [roadmap.md](/home/hj153lee/SmolChat-Android/docs/custom-app/roadmap.md)
  Current status, execution order, checklist, and change log.

- [architecture.md](/home/hj153lee/SmolChat-Android/docs/custom-app/architecture.md)
  Reuse strategy, target app structure, data model direction, and codebase impact.

### Task Docs

- [task-01-scope-lock.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-01-scope-lock.md)
  Freeze MVP assumptions for JSON, TSV, and evaluation flow.

- [task-02-app-entry-flow.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-02-app-entry-flow.md)
  Replace launcher behavior with the compact app entry flow.

- [task-03-setup-screen.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-03-setup-screen.md)
  Build the setup screen for model import and parameter editing.

- [task-04-multiturn-chat.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-04-multiturn-chat.md)
  Reuse the multi-turn conversation core in a compact chat screen.

- [task-05-runtime-metrics.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-05-runtime-metrics.md)
  Show generation token/s, context length, and generation time.

- [task-06-json-parser.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-06-json-parser.md)
  Parse model JSON output into a normalized prediction shape.

- [task-07-tsv-loader.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-07-tsv-loader.md)
  Load and validate TSV gold labels.

- [task-08-macro-accuracy.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-08-macro-accuracy.md)
  Compare predictions to gold labels and compute Macro Accuracy.

- [task-09-legacy-flow-removal.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-09-legacy-flow-removal.md)
  Disconnect legacy features from the main user journey.

- [task-10-cleanup.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-10-cleanup.md)
  Remove unused dependencies, permissions, and stale app paths.

### Shared Specs

- [json-output-schema.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/json-output-schema.md)
  JSON prediction shape assumptions for MVP.

- [tsv-schema.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/tsv-schema.md)
  TSV column expectations for gold labels.

- [evaluation-rule.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/evaluation-rule.md)
  MVP evaluation rule and Macro Accuracy definition.

## Recommended Agent Workflow

For a single implementation request:

1. Read [roadmap.md](/home/hj153lee/SmolChat-Android/docs/custom-app/roadmap.md).
2. Read exactly one target file in `tasks/`.
3. Read only the relevant file(s) in `specs/`.
4. Implement only that task.
5. Update `roadmap.md` status, checklist, and change log.

## Scope Summary

### In Scope

- local GGUF import
- system prompt editing
- inference parameter editing
- on-device inference
- multi-turn chat
- automatic carry-over of prior assistant output via conversation history
- JSON parsing
- TSV-based evaluation
- Macro Accuracy
- runtime metrics

### Out Of Scope

- ASR
- Hugging Face browsing UI
- RAG
- cloud inference
- folder management
- task marketplace UI

