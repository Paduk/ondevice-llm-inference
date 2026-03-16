# Custom App Roadmap

## Status

- Overall status: `in_progress`
- Current phase: `Phase 3`
- Current focus: `Physical-device validation`

## MVP Acceptance Criteria

- user can import a GGUF model from local storage
- user can configure prompt and inference parameters
- user can run on-device inference
- user can continue the conversation across multiple turns
- prior assistant output is automatically part of next-turn inference context
- app can parse model JSON output
- app can compare prediction with TSV gold labels
- app can compute and show Macro Accuracy
- app can show token/s
- app can show prompt/context length used

## Phase 2 Acceptance Criteria

- user can choose one of three predefined prompt presets from setup
- selecting a preset replaces the current system prompt text
- batch test can render prompts using TSV `query`, `rewrited_query`, and `conversation_history`
- batch test can run first 1, random 10, or all TSV rows
- batch test shows progress and cumulative evaluation summary

## Phase 3 Acceptance Criteria

- the app can load `api_v3.0.1.jsonl` from bundled assets
- the prompt renderer supports `{tools}`
- `{tools}` is built from TSV `candidates` and bundled API metadata
- batch prompts can include both TSV placeholders and `{tools}`
- the UI can show tools-rendering warnings when metadata is missing

## Execution Order

1. [task-01-scope-lock.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-01-scope-lock.md)
2. [task-02-app-entry-flow.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-02-app-entry-flow.md)
3. [task-03-setup-screen.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-03-setup-screen.md)
4. [task-04-multiturn-chat.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-04-multiturn-chat.md)
5. [task-05-runtime-metrics.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-05-runtime-metrics.md)
6. [task-06-json-parser.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-06-json-parser.md)
7. [task-07-tsv-loader.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-07-tsv-loader.md)
8. [task-08-macro-accuracy.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-08-macro-accuracy.md)
9. [task-09-legacy-flow-removal.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-09-legacy-flow-removal.md)
10. [task-10-cleanup.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-10-cleanup.md)
11. [task-11-prompt-preset-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-11-prompt-preset-spec.md)
12. [task-12-prompt-preset-ui.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-12-prompt-preset-ui.md)
13. [task-13-prompt-template-renderer.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-13-prompt-template-renderer.md)
14. [task-14-batch-run-mode-ui.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-14-batch-run-mode-ui.md)
15. [task-15-batch-inference-runner.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-15-batch-inference-runner.md)
16. [task-16-batch-evaluation-summary.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-16-batch-evaluation-summary.md)
17. [task-17-tools-injection-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-17-tools-injection-spec.md)
18. [task-18-api-metadata-assets-loader.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-18-api-metadata-assets-loader.md)
19. [task-19-tools-placeholder-renderer.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-19-tools-placeholder-renderer.md)
20. [task-20-tools-batch-wiring.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-20-tools-batch-wiring.md)
21. [task-21-tools-debug-summary.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-21-tools-debug-summary.md)

## Checklist

- [x] Confirm base app direction
- [x] Confirm removal of ASR
- [x] Confirm multi-turn requirement
- [x] Confirm simplified evaluation approach
- [x] Freeze TSV schema
- [x] Freeze JSON output schema assumption
- [x] Define MVP screens
- [x] Implement compact app entry flow
- [x] Implement model import in compact UI
- [x] Implement parameter editor
- [x] Rewire multi-turn inference flow
- [x] Show runtime metrics in UI
- [x] Implement JSON parser
- [x] Implement TSV loader
- [x] Implement Macro Accuracy evaluator
- [x] Remove unused dependencies
- [ ] Validate on physical device
- [x] Freeze prompt preset assumptions
- [x] Add prompt preset selection to setup
- [x] Implement prompt template renderer
- [x] Add batch run mode selection
- [x] Implement batch inference runner
- [x] Show batch evaluation summary
- [x] Freeze tools injection assumptions
- [x] Load API metadata from assets
- [x] Support `{tools}` placeholder rendering
- [x] Use `{tools}` during batch prompt generation
- [x] Show tools rendering diagnostics

## Agent Update Rules

When an agent completes a task, update:

1. `Overall status`
2. `Current phase`
3. `Current focus`
4. checklist items completed
5. the dated change log entry below

## Change Log

- 2026-03-16: Initial custom app plan created.
- 2026-03-16: Scope refined to remove ASR and keep multi-turn chat with prior assistant output reused through conversation history.
- 2026-03-16: Documentation split into `docs/custom-app/` so future agents can read only the map, task, and spec files needed for a focused implementation step.
- 2026-03-16: Task 1 completed. MVP assumptions frozen: JSON requires `sample_id` and `prediction`, TSV requires `sample_id` and `gold_label`, gold TSV is user-imported, and one active chat session is sufficient for MVP.
- 2026-03-16: MVP schema revised. JSON now requires `query`, `rewrited_query`, `generated`, and `answer`. TSV now requires `conversation_history`, `query`, `rewrited_query`, `answer`, `unique_idx`, and `candidates`.
- 2026-03-16: Task 2 completed. `MainActivity` now launches a compact custom app flow directly, with placeholder `Setup` and `Chat And Evaluate` screens replacing the legacy startup redirect to `DownloadModelActivity` or `ChatActivity`.
- 2026-03-16: Task 3 completed. The new compact `Setup` screen now supports local GGUF import, model selection, system prompt editing, inference parameter editing, and TSV gold-file import, with setup state persisted for the next screen.
- 2026-03-16: Task 4 completed. The compact chat screen now loads the selected model, persists user and assistant messages, supports multi-turn inference through chat history replay, and provides a reset action to clear the conversation and reload the model.
- 2026-03-16: Task 5 completed. The compact chat screen now displays generation speed, generation time, and prompt/context length after each response.
- 2026-03-16: Task 6 completed. A strict JSON parser now validates model output as a single object with `query`, `rewrited_query`, `generated`, and `answer`, and the compact chat screen shows parse success or parse failure after each response.
- 2026-03-16: Task 7 completed. A strict TSV loader now validates required columns, unique `unique_idx` values, and non-empty answers, and the compact chat screen reports TSV load status and loaded record count.
- 2026-03-16: Task 8 completed. Parsed model output is now matched against TSV gold data by `query` and `rewrited_query`, with per-turn correctness and cumulative Macro Accuracy shown in the compact chat screen.
- 2026-03-16: Task 9 completed. The manifest now exposes only `MainActivity` as the user-facing app path, and legacy ASR, task-management, model-download, and share-text chat activity registrations have been removed from the main flow.
- 2026-03-16: Task 10 completed. The custom app cleanup pass removed legacy audio permission usage from the manifest, deleted obsolete Android instrumentation tests tied to old flows, and trimmed test-only dependency footprint while keeping the current compact app build stable.
- 2026-03-16: Phase 2 planning added. Prompt presets, placeholder-based prompt rendering, and TSV-driven batch testing are now split into Tasks 11 through 16 with dedicated spec files.
- 2026-03-16: Task 11 completed. Prompt preset assumptions are now frozen with explicit preset keys, `Custom` fallback behavior, overwrite rules for setup, and persistence rules for preset selection plus editable prompt text.
- 2026-03-16: Task 12 completed. The setup screen now exposes `Custom`, `Model A`, `Model B`, and `Model C` prompt preset choices, applies preset templates directly into the editable system prompt field, and persists the selected preset key with the current prompt text.
- 2026-03-16: Task 13 completed. A prompt template renderer now replaces `{query}`, `{rewrited_query}`, and `{conversation_history}` from a TSV row, and the chat screen exposes a rendered-prompt preview using the first loaded TSV record.
- 2026-03-16: Task 14 completed. The chat screen now exposes single-choice batch run modes for `First 1`, `Random 10`, and `All`, persists the selected mode, and shows the active mode before batch execution is implemented.
- 2026-03-16: Task 15 completed. The custom chat screen now starts and stops sequential batch inference runs, selects rows according to the chosen batch mode, renders prompts from TSV rows, evaluates each response, and tracks batch progress, failures, and the latest processed row while restoring the interactive model session afterward.
- 2026-03-16: Task 16 completed. The chat screen now shows a readable batch summary with run state, completion percent, success and failure counts, success rate, evaluated sample count, Macro Accuracy, latest processed row, and the latest batch-level status or evaluation issue.
- 2026-03-16: Phase 3 planning added. Tools injection from TSV `candidates` and bundled `api_v3.0.1.jsonl` assets is now split into Tasks 17 through 21 so `{tools}` support can be implemented incrementally.
- 2026-03-16: Task 17 completed. Tools injection assumptions are now frozen around bundled `api_v3.0.1.jsonl` assets, Python-style `candidates` parsing, line-by-line `{plan}: {api_data}` rendering, partial missing-plan warnings, and rewrite prompts that combine `{tools}` with `{rewrited_query}`.
- 2026-03-16: Task 18 completed. `api_v3.0.1.jsonl` is now bundled under `app/src/main/assets/`, and a cached asset loader provides normalized `plan -> metadata` lookup with `examples`, `returns`, and `next_turn_plans` removed.
- 2026-03-16: Task 19 completed. The prompt renderer now supports `{tools}` by parsing Python-style TSV candidate lists, deduplicating repeated plans, rendering available plan metadata line-by-line, and returning missing-plan warnings alongside the final prompt text.
- 2026-03-16: Task 20 completed. Batch prompt preview and batch inference now both use bundled API metadata to render `{tools}`, and the current state keeps row-level missing-plan warnings so tooling issues can be surfaced in the UI.
- 2026-03-16: Task 21 completed. The UI now shows tools-rendering diagnostics including parsed candidate count, rendered tool count, render status, missing-plan count, and the missing plan names for the latest preview or batch row.
