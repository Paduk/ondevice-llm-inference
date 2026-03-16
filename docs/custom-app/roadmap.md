# Custom App Roadmap

## Status

- Overall status: `in_progress`
- Current phase: `Phase 1`
- Current focus: `Validation and packaging`

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
