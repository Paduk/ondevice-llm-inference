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

- [task-11-prompt-preset-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-11-prompt-preset-spec.md)
  Freeze preset prompt options and model-to-preset mapping assumptions.

- [task-12-prompt-preset-ui.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-12-prompt-preset-ui.md)
  Add preset prompt selection to the setup screen.

- [task-13-prompt-template-renderer.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-13-prompt-template-renderer.md)
  Render system prompts by substituting TSV row data into placeholders.

- [task-14-batch-run-mode-ui.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-14-batch-run-mode-ui.md)
  Add batch-mode selection UI for first 1, random 10, and full-dataset execution.

- [task-15-batch-inference-runner.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-15-batch-inference-runner.md)
  Execute repeated inference against TSV rows using the rendered prompt template.

- [task-16-batch-evaluation-summary.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-16-batch-evaluation-summary.md)
  Show cumulative batch progress and final evaluation summary.

- [task-17-tools-injection-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-17-tools-injection-spec.md)
  Freeze how `{tools}` prompt injection works from TSV candidates and bundled API metadata.

- [task-18-api-metadata-assets-loader.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-18-api-metadata-assets-loader.md)
  Load bundled API metadata from app assets into a plan-indexed map.

- [task-19-tools-placeholder-renderer.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-19-tools-placeholder-renderer.md)
  Extend prompt rendering to support `{tools}` from TSV candidates and API metadata.

- [task-20-tools-batch-wiring.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-20-tools-batch-wiring.md)
  Use rendered tools strings during batch inference and prompt preview.

- [task-21-tools-debug-summary.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-21-tools-debug-summary.md)
  Show tools rendering diagnostics such as selected candidate count and missing-plan warnings.

### Shared Specs

- [json-output-schema.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/json-output-schema.md)
  JSON prediction shape assumptions for MVP.

- [tsv-schema.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/tsv-schema.md)
  TSV column expectations for gold labels.

- [evaluation-rule.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/evaluation-rule.md)
  MVP evaluation rule and Macro Accuracy definition.

- [prompt-preset-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/prompt-preset-spec.md)
  Prompt preset options and how they populate the system prompt field.

- [prompt-template-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/prompt-template-spec.md)
  Placeholder rules for `{query}`, `{rewrited_query}`, and `{conversation_history}`.

- [batch-run-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/batch-run-spec.md)
  Batch execution modes, row sampling rules, and summary expectations.

- [tools-injection-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/tools-injection-spec.md)
  Rules for building `{tools}` strings from TSV candidates and bundled API metadata.

- [api-metadata-asset-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/api-metadata-asset-spec.md)
  Asset packaging and loading assumptions for `api_v3.0.1.jsonl`.

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
- preset prompt selection
- inference parameter editing
- on-device inference
- multi-turn chat
- automatic carry-over of prior assistant output via conversation history
- JSON parsing
- TSV-based evaluation
- Macro Accuracy
- runtime metrics
- dataset-driven batch test
- prompt placeholder substitution from TSV rows
- tools placeholder substitution from bundled API metadata

### Out Of Scope

- ASR
- Hugging Face browsing UI
- RAG
- cloud inference
- folder management
- task marketplace UI
