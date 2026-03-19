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
  Add batch-mode selection UI for the supported dataset execution options.

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

- [task-22-simple-api-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-22-simple-api-spec.md)
  Correct the `{tools}` source-of-truth to match the Python SFT path using `simple_api.json`.

- [task-23-simple-api-asset-loader.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-23-simple-api-asset-loader.md)
  Load bundled `simple_api.json` into a cached plan-to-parameter-list map.

- [task-24-simple-tools-renderer-wiring.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-24-simple-tools-renderer-wiring.md)
  Rewire preview and batch prompt rendering so `{tools}` always uses `simple_api.json`.

- [task-25-batch-single-load-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-25-batch-single-load-spec.md)
  Freeze the optimized batch design so the model loads once and rows reset context without reload.

- [task-26-native-reset-api.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-26-native-reset-api.md)
  Add a reset path in native and Kotlin layers to clear conversation state without unloading the model.

- [task-27-batch-runner-single-load.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-27-batch-runner-single-load.md)
  Refactor the batch runner to use one model load and row-level resets.

- [task-28-model-parity-audit.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-28-model-parity-audit.md)
  Freeze the minimum checklist for meaningful Python-vs-APK quality comparison.

- [task-29-align-sampling-defaults.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-29-align-sampling-defaults.md)
  Align Android sampling defaults with the Python Ollama path where practical.

- [task-30-raw-prompt-batch-path.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-30-raw-prompt-batch-path.md)
  Add or define a raw-prompt batch path for closer parity with Python prompt execution.

- [task-31-json-and-length-parity.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-31-json-and-length-parity.md)
  Reduce JSON forcing and generation-length gaps between Python and Android.

- [task-32-tolerant-json-parser.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-32-tolerant-json-parser.md)
  Relax model-output parsing so both JSON quotes and Python-style single-quote dict strings can be accepted.

- [task-33-batch-raw-output-retention.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-33-batch-raw-output-retention.md)
  Keep raw batch outputs visible in conversation until the next batch run starts.

- [task-34-tool-call-schema-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-34-tool-call-schema-spec.md)
  Freeze the real `plan + arguments` output shape used by the current SFT tool-calling task.

- [task-35-nested-tool-call-parser.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-35-nested-tool-call-parser.md)
  Replace the old flat parser with a nested tool-call parser for JSON-style and Python-style outputs.

- [task-36-structural-evaluator.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-36-structural-evaluator.md)
  Compare model output and TSV gold answers structurally after canonical normalization.

- [task-37-runtime-metric-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-37-runtime-metric-spec.md)
  Freeze prefill versus generation metric definitions, batch aggregates, and the default-versus-expanded UI split.

- [task-38-native-prefill-generation-metrics.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-38-native-prefill-generation-metrics.md)
  Expose separate prefill and generation metrics from native inference through Kotlin and manager layers.

- [task-39-expandable-runtime-metrics-ui.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-39-expandable-runtime-metrics-ui.md)
  Show only core metrics by default and move detailed per-case and batch metrics into an expandable section.

- [task-40-rma-mode-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-40-rma-mode-spec.md)
  Freeze the RMA rewrite task as a separate mode with its own activity and evaluator path.

- [task-41-rma-entry-and-activity.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-41-rma-entry-and-activity.md)
  Add a dedicated RMA evaluation activity or route while reusing shared app structure.

- [task-42-rma-prompt-renderer.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-42-rma-prompt-renderer.md)
  Render RMA prompts from TSV `conversation_history` and `query` using the Python preprocessing shape.

- [task-43-rma-evaluator.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-43-rma-evaluator.md)
  Evaluate RMA rewrite outputs against TSV `rewrited_query` in the dedicated RMA flow.

- [task-44-test-type-routing-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-44-test-type-routing-spec.md)
  Freeze the top-level `Toolcalling`, `RMA`, and `E2E` routing model plus conditional model selection.

- [task-45-test-type-setup-ui.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-45-test-type-setup-ui.md)
  Refactor setup so the user picks test type first and then sees only the relevant model or pipeline options.

- [task-46-toolcalling-rma-activity-split.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-46-toolcalling-rma-activity-split.md)
  Split evaluator routing so Toolcalling and RMA use separate activity flows.

- [task-47-e2e-activity-and-runner.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-47-e2e-activity-and-runner.md)
  Add a dedicated E2E evaluator route that runs RMA first and then tool-calling rewrite.

- [task-48-e2e-final-evaluator.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-48-e2e-final-evaluator.md)
  Score only the final tool-call output in the E2E path.

- [task-49-batch-export-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-49-batch-export-spec.md)
  Freeze persistent batch-result export, `Top 1 / Top 50 / All`, and `unique_idx`-based resume assumptions.

- [task-50-batch-result-writer.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-50-batch-result-writer.md)
  Persist per-case batch results and summary output while the run is still in progress.

- [task-51-batch-resume-loader.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-51-batch-resume-loader.md)
  Resume a long batch run by skipping rows already present in a prior result TSV.

- [task-52-batch-export-ui.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-52-batch-export-ui.md)
  Show export path, flush status, resume state, and the new batch-mode labels in the UI.

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

- [batch-session-reset-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/batch-session-reset-spec.md)
  Reset semantics for one-load batch execution with fresh single-turn rows.

- [tools-injection-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/tools-injection-spec.md)
  Rules for building `{tools}` strings from TSV candidates and bundled API metadata.

- [api-metadata-asset-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/api-metadata-asset-spec.md)
  Asset packaging and loading assumptions for `simple_api.json`.

- [inference-parity-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/inference-parity-spec.md)
  Rules for reducing quality differences between Python and APK inference paths.

- [parser-and-batch-retention-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/parser-and-batch-retention-spec.md)
  Rules for tolerant parser fallback and preserving raw batch outputs across parse or evaluation failures.

- [tool-call-output-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/tool-call-output-spec.md)
  The actual `plan` plus nested `arguments` schema used by the current tool-calling model outputs and TSV gold answers.

- [runtime-metrics-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/runtime-metrics-spec.md)
  Definitions for prefill, generation, total-time metrics, batch aggregates, and the preferred expandable UI presentation.

- [batch-result-export-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/batch-result-export-spec.md)
  Persistent TSV export, flush cadence, resume rules, and per-case plus summary result schema for long-running batch evaluation.

- [rma-rewrite-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/rma-rewrite-spec.md)
  The dedicated RMA rewrite mode, prompt input shape, separate activity path, and rewrite-text evaluation target.

- [test-type-routing-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/test-type-routing-spec.md)
  Top-level `Toolcalling`, `RMA`, and `E2E` routing plus conditional model or pipeline selection.

- [e2e-evaluator-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/e2e-evaluator-spec.md)
  The two-stage E2E flow, same-family pipeline rule, and final-only evaluation rule.

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
- tools placeholder substitution from bundled simple API metadata

### Out Of Scope

- ASR
- Hugging Face browsing UI
- RAG
- cloud inference
- folder management
- task marketplace UI
