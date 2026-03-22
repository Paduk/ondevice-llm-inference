# Custom App Roadmap

## Status

- Overall status: `in_progress`
- Current phase: `Phase 8`
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

- user can choose one of four predefined prompt presets plus `Custom` from setup
- selecting a preset replaces the current system prompt text
- batch test can render prompts using TSV `query`, `rewrited_query`, and `conversation_history`
- batch test can run `Top 1`, `Top 50`, or `All` TSV rows
- batch test shows progress and cumulative evaluation summary

## Phase 3 Acceptance Criteria

- the app can load bundled tool metadata from app assets
- the prompt renderer supports `{tools}`
- `{tools}` is built from TSV `candidates` and bundled simple API metadata
- batch prompts can include both TSV placeholders and `{tools}`
- the UI can show tools-rendering warnings when metadata is missing

## Phase 3 Correction Acceptance Criteria

- `{tools}` uses `simple_api.json` as the only source of truth
- the simple asset loader exposes `plan -> parameter_list`
- preview and batch rendering use the same simple tools data
- rendered tools lines match the Python SFT path format

## Phase 4 Acceptance Criteria

- batch run loads the model once per run
- each batch row executes with a fresh single-turn context
- row-to-row context leakage is prevented without reload-per-row
- interactive chat is restored after batch completion or stop

## Phase 5 Acceptance Criteria

- Python and APK parity checks are documented
- Android sampling defaults can be aligned with Python for controlled comparison
- Android has a documented or implemented raw-prompt parity path
- remaining JSON and length-control gaps are clearly reduced or documented
- parser can tolerate both JSON-style and Python-style quoted object outputs
- batch raw outputs remain visible until a new batch run starts
- parser and evaluator are aligned with the real `plan + arguments` tool-call schema
- runtime metrics distinguish prefill and generation without overloading the main screen

## Phase 6 Acceptance Criteria

- RMA rewrite evaluation is split from the tool-calling screen
- RMA presets use the Python RMA prompt templates
- RMA prompt rendering matches the Python `preprocess_example_it(..., test_type="sft")` input shape
- RMA evaluation compares model output against TSV `rewrited_query`

## Phase 7 Acceptance Criteria

- setup exposes `Toolcalling`, `RMA`, and `E2E` as the top-level test types
- setup reveals only the relevant model or pipeline options for the selected test type
- Toolcalling, RMA, and E2E route into separate evaluator activities or screen flows
- E2E supports same-family pipelines only in the first version
- E2E runs RMA first, then tool-calling rewrite, and scores only the final tool-call output

## Phase 8 Acceptance Criteria

- batch mode uses `Top 1`, `Top 50`, and `All`
- long batch runs persist per-case results to TSV during execution
- result files include correctness fields and runtime metrics for each row
- the app flushes batch result files every 10 completed rows
- the app can resume from an existing result TSV by skipping already-evaluated `unique_idx` rows
- the app shows export and resume state without overcrowding the evaluator screen
- failed format or evaluation rows are excluded from batch runtime aggregates
- generation is capped to avoid runaway output during malformed runs
- the UI can delete saved batch result files so the next run starts fresh

## Phase 8 Export Reuse Acceptance Criteria

- Toolcalling and RMA share the same batch export-actions UI and state labels
- Toolcalling and RMA keep evaluator summaries separate while reusing export controls

## Phase 8 Follow-up Acceptance Criteria

- Toolcalling and RMA share the same runtime-metrics labels and layout
- Toolcalling and RMA share the same details-toggle behavior
- runtime metrics are reused as a shared inference UI component while evaluator summaries remain separate

## Phase 8 Stability Acceptance Criteria

- pressing `Stop` during any long-running batch test does not crash the app
- batch cancellation is treated as a normal stopped state, not an error path
- native inference is not unloaded or closed before in-flight generation work has fully stopped
- Toolcalling, RMA, and E2E all follow the same stop or cancel safety contract
- interactive state can be restored after a stopped batch without requiring app restart
- the setup flow includes bundled `tc.tsv` as the default gold TSV without requiring manual import

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
22. [task-22-simple-api-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-22-simple-api-spec.md)
23. [task-23-simple-api-asset-loader.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-23-simple-api-asset-loader.md)
24. [task-24-simple-tools-renderer-wiring.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-24-simple-tools-renderer-wiring.md)
25. [task-25-batch-single-load-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-25-batch-single-load-spec.md)
26. [task-26-native-reset-api.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-26-native-reset-api.md)
27. [task-27-batch-runner-single-load.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-27-batch-runner-single-load.md)
28. [task-28-model-parity-audit.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-28-model-parity-audit.md)
29. [task-29-align-sampling-defaults.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-29-align-sampling-defaults.md)
30. [task-30-raw-prompt-batch-path.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-30-raw-prompt-batch-path.md)
31. [task-31-json-and-length-parity.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-31-json-and-length-parity.md)
32. [task-32-tolerant-json-parser.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-32-tolerant-json-parser.md)
33. [task-33-batch-raw-output-retention.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-33-batch-raw-output-retention.md)
34. [task-34-tool-call-schema-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-34-tool-call-schema-spec.md)
35. [task-35-nested-tool-call-parser.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-35-nested-tool-call-parser.md)
36. [task-36-structural-evaluator.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-36-structural-evaluator.md)
37. [task-37-runtime-metric-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-37-runtime-metric-spec.md)
38. [task-38-native-prefill-generation-metrics.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-38-native-prefill-generation-metrics.md)
39. [task-39-expandable-runtime-metrics-ui.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-39-expandable-runtime-metrics-ui.md)
40. [task-40-rma-mode-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-40-rma-mode-spec.md)
41. [task-41-rma-entry-and-activity.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-41-rma-entry-and-activity.md)
42. [task-42-rma-prompt-renderer.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-42-rma-prompt-renderer.md)
43. [task-43-rma-evaluator.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-43-rma-evaluator.md)
44. [task-44-test-type-routing-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-44-test-type-routing-spec.md)
45. [task-45-test-type-setup-ui.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-45-test-type-setup-ui.md)
46. [task-46-toolcalling-rma-activity-split.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-46-toolcalling-rma-activity-split.md)
47. [task-47-e2e-activity-and-runner.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-47-e2e-activity-and-runner.md)
48. [task-48-e2e-final-evaluator.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-48-e2e-final-evaluator.md)
49. [task-49-batch-export-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-49-batch-export-spec.md)
50. [task-50-batch-result-writer.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-50-batch-result-writer.md)
51. [task-51-batch-resume-loader.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-51-batch-resume-loader.md)
52. [task-52-batch-export-ui.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-52-batch-export-ui.md)
53. [task-53-shared-runtime-metrics-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-53-shared-runtime-metrics-spec.md)
54. [task-54-shared-runtime-metrics-component.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-54-shared-runtime-metrics-component.md)
55. [task-55-batch-stop-safety-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-55-batch-stop-safety-spec.md)
56. [task-56-batch-stop-crash-fix.md](/home/hj153lee/SmolChat-Android/docs/custom-app/tasks/task-56-batch-stop-crash-fix.md)

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
- [x] Freeze `simple_api.json` as the only tools source
- [x] Load `simple_api.json` from assets
- [x] Switch preview and batch tools rendering to `simple_api.json`
- [x] Freeze single-load batch reset assumptions
- [x] Add native reset API without model unload
- [x] Refactor batch runner to use single model load
- [x] Freeze Python-vs-APK parity checklist
- [x] Align Android sampling defaults for parity tests
- [x] Add or define raw-prompt parity path
- [ ] Reduce JSON and generation-length parity gaps
- [x] Tolerate Python-style quoted object outputs in parser fallback
- [x] Preserve raw batch outputs until a new batch run starts
- [x] Freeze the real tool-call output schema
- [x] Parse nested tool-call outputs
- [x] Compare tool-call outputs structurally against TSV gold answers
- [x] Freeze prefill and generation metric definitions
- [x] Expose separate prefill and generation metrics from native inference
- [x] Show detailed metrics behind an expandable UI
- [ ] Freeze RMA rewrite mode as a separate activity flow
- [x] Add a dedicated RMA evaluation activity or route
- [x] Render RMA prompts from TSV `conversation_history` and `query`
- [x] Evaluate RMA outputs against TSV `rewrited_query`
- [x] Freeze top-level `Toolcalling / RMA / E2E` routing
- [x] Add conditional setup UI for test type and model or pipeline selection
- [x] Split Toolcalling, RMA, and E2E into separate evaluator routes
- [x] Run same-family E2E pipelines and score only the final tool-call output
- [x] Freeze persistent batch export and resume assumptions
- [x] Persist per-case batch results and summary output
- [x] Resume batch evaluation from an existing result TSV
- [x] Show export and resume status in the UI
- [x] Exclude failed-format rows from batch performance aggregates
- [x] Cap generation length at 200 tokens
- [x] Allow saved batch result files to be deleted from the UI
- [x] Freeze shared Toolcalling/RMA runtime-metrics contract
- [x] Reuse one runtime-metrics component across Toolcalling and RMA
- [x] Freeze batch stop/cancel safety contract across Toolcalling, RMA, and E2E
- [x] Stop batch runs without crash and restore interactive state safely
- [x] Bundle `tc.tsv` as the default gold TSV asset
- [x] Freeze shared Toolcalling/RMA batch export-actions contract

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
- 2026-03-17: Phase 3 correction planning added. `{tools}` will be corrected to use `simple_api.json` consistently so Android prompt construction matches the Python SFT inference path rather than the previous detailed JSONL metadata path.
- 2026-03-17: Prompt preset UI was revised to a 2-column grid with `Rewrite-Qwen3`, `Base-Qwen3`, `Rewrite-Phi`, `Base-Phi`, and `Custom`. `Custom` now clears the system prompt to an empty string when selected, and old stored preset keys fall back safely to `Custom`.
- 2026-03-17: Task 22 completed. The docs now freeze `simple_api.json` as the only `{tools}` source, define rendering as `{plan}: {parameter_list}`, and move the correction flow focus to the simple asset loader.
- 2026-03-17: Task 23 completed. `simple_api.json` is now bundled under app assets, and the asset store now loads and caches a `plan -> parameter_list` map while keeping temporary compatibility methods until Task 24 rewires preview and batch rendering.
- 2026-03-17: Task 24 completed. Prompt preview and batch inference now both use `simple_api.json` through the simple asset loader, and `{tools}` is rendered as `{plan}: {parameter_list}` with missing-plan diagnostics preserved.
- 2026-03-17: Phase 4 planning added. Batch inference will be optimized from reload-per-row to one model load per batch run plus row-level context resets, with native reset support added before the runner is refactored.
- 2026-03-17: Task 25 completed. The batch optimization design is now frozen around one model load per batch run, row-level conversation and KV/cache resets, system-prompt re-priming, and interactive chat restoration after batch completion.
- 2026-03-17: Task 26 completed. A native reset pathway now clears in-memory messages, prompt buffers, partial-response buffers, and llama KV/cache state without unloading model weights, and the Kotlin `SmolLM` wrapper exposes that reset API directly.
- 2026-03-17: Task 27 completed. The batch runner now loads the batch model once, resets loaded state between rows instead of unloading and reloading, and relies on the batch job cleanup path to restore the interactive chat session after completion or cancellation.
- 2026-03-18: Phase 5 planning added. Python-vs-APK quality gaps are now split into parity tasks covering model identity, sampling defaults, raw-prompt execution, and JSON or generation-length alignment.
- 2026-03-18: Task 28 completed. The minimum checklist for meaningful Python-vs-APK quality comparison is now frozen, covering model identity, rendered prompt parity, tools rendering parity, sampling alignment, generation-length alignment, JSON-only expectations, and chat-template handling.
- 2026-03-18: Task 29 completed. The custom app default temperature is now aligned to `0.0` for parity testing, while the remaining min-p and generation-length gaps are explicitly documented for later follow-up.
- 2026-03-18: Task 30 completed. A raw-prompt execution path is now available in the native, Kotlin, and manager layers, and the batch runner uses that path so rendered prompts can be tested without extra chat-template wrapping.
- 2026-03-18: Parser and batch-retention follow-up planning added. The next corrections split tolerant single-quote parsing and raw batch-output retention into Tasks 32 and 33 so usability gaps can be fixed without mixing parser and UI state changes.
- 2026-03-18: Task 32 completed. The model-output parser still prefers strict JSON first, but now falls back to a tolerant quoted-object parser so Python-style single-quote outputs can be accepted when they still satisfy the required normalized schema.
- 2026-03-18: Task 33 completed. Batch raw outputs are now retained in the visible conversation state independently from parse or evaluation success, and the retained batch outputs are cleared only when a new batch run starts or the whole conversation is reset.
- 2026-03-18: Tool-call schema correction planning added. The next follow-up replaces the outdated flat prediction assumption with the actual `plan + arguments` schema from TSV `answer`, then updates parsing and evaluation to work on nested structured tool-call outputs.
- 2026-03-18: Task 34 completed. The docs now freeze the real tool-calling output schema around top-level `plan` plus nested `arguments`, and evaluation is explicitly redirected from legacy flat-string comparison toward structural normalization and comparison.
- 2026-03-18: Task 35 completed. The app parser now reads nested tool-call outputs from both strict JSON and Python-style quoted objects, and the parse-result UI now reflects top-level `plan` plus pretty-printed nested `arguments` instead of the outdated flat prediction fields.
- 2026-03-18: Task 36 completed. The evaluator now parses TSV `answer` values as structured tool calls, canonicalizes model output and gold output recursively before comparison, and scores correctness structurally instead of relying on raw string equality.
- 2026-03-18: Runtime-metrics follow-up planning added. The next work splits prefill and generation metrics in the native path, then exposes only core metrics by default while moving detailed per-case and batch aggregates behind an expandable section.
- 2026-03-18: Task 37 completed. Runtime metrics are now frozen around separate prefill, generation, and total-time definitions, incremental batch totals and averages, and an inline expandable UI that keeps only core metrics always visible.
- 2026-03-18: Task 38 completed. Native inference now exposes separate prefill and generation timing, prompt token count, generated token count, and prefill or generation speeds through JNI, Kotlin, and manager response objects so the UI layer can render split runtime metrics next.
- 2026-03-18: Task 39 completed. The custom app now keeps only core runtime metrics always visible, moves detailed per-case and batch metrics behind an inline expandable section, and updates batch totals and averages incrementally as rows complete.
- 2026-03-19: Phase 6 planning added. RMA rewrite evaluation will be split into a dedicated activity path so it can reuse common UI and runtime pieces without being forced through the current tool-calling parser and evaluator.
- 2026-03-19: Prompt preset defaults were aligned with the external training prompts. New setup state now defaults to `Rewrite-Qwen3`, `Base-Qwen3` and `Base-Phi` now use history-style prompts with `conversation_history` plus `query`, and `Rewrite-Phi` now follows the rewrite-style prompt path with `rewrited_query`, matching the same placeholder direction already used by `Rewrite-Qwen3`.
- 2026-03-19: Phase 7 planning added. The app will move to a top-level `Test Type` model with separate Toolcalling, RMA, and E2E evaluator routes, and the first E2E version will use only same-family pipelines while scoring only the final tool-call output.
- 2026-03-19: Task 44 completed. The top-level routing model is now frozen around `Toolcalling`, `RMA`, and `E2E`, setup will reveal only the relevant model or pipeline choices for the selected type, and the first E2E version is restricted to same-family pipelines with final-only scoring.
- 2026-03-19: Task 45 completed. The setup screen now asks for `Test Type` first, shows only the relevant Toolcalling, RMA, or E2E model or pipeline options, and keeps E2E-specific prompt editing hidden until its dedicated route is implemented in the next task.
- 2026-03-19: Task 46 completed. Toolcalling and RMA now enter different evaluator routes from setup, and RMA has its own placeholder screen so it no longer reuses the tool-calling evaluator route while its task-specific renderer and evaluator remain pending.
- 2026-03-19: Task 42 completed. The RMA route now renders preview prompts using the Python `preprocess_example_it(..., test_type="sft")` input shape by building a JSON `{data}` payload from TSV `conversation_history` and `query`, and the first-row preview is shown directly in the dedicated RMA screen.
- 2026-03-19: Task 43 completed. The dedicated RMA route now runs batch inference against TSV rows, compares raw rewrite outputs directly against TSV `rewrited_query` with exact-match scoring, and shows its own rewrite-focused evaluation summary without depending on the tool-calling parser or evaluator.
- 2026-03-19: Task 47 completed. The app now has a dedicated E2E evaluator route, supports same-family `Qwen3` and `Phi` pipelines with separate RMA and tool-calling model selection inside that flow, and the batch runner executes RMA first then tool-calling rewrite while preserving the intermediate rewrite for debugging.
- 2026-03-19: Task 48 completed. The E2E flow now ignores intermediate rewrite quality for scoring, structurally compares only the final tool-call output against TSV `answer`, and shows a dedicated final-evaluation summary with latest correctness and Macro Accuracy in the E2E route.
- 2026-03-19: Phase 8 planning added. Long-running batch evaluation will move to persistent TSV export with `Top 1 / Top 50 / All` modes, periodic flush every 10 completed rows, per-case runtime metrics, and `unique_idx`-based resume support after interruption.
- 2026-03-19: Task 49 completed. Persistent batch-export assumptions are now frozen around `Top 1 / Top 50 / All`, per-case TSV plus summary output, flush every 10 completed rows, `unique_idx`-based resume, and persisted throughput metrics including prefill, generation, and overall token-per-second fields.
- 2026-03-19: Task 50 completed. Tool-calling batch runs now create a per-case result TSV plus a summary JSON file under app storage, persist generated outputs and correctness flags with runtime metrics, and flush the files every 10 completed rows as well as on batch completion or failure.
- 2026-03-22: Runtime-metrics reuse follow-up added. Toolcalling and RMA will explicitly share the same runtime-metrics contract and UI component so inference instrumentation does not drift while evaluator-specific summary logic stays separate.
- 2026-03-22: Task 53 completed. The docs now explicitly freeze Toolcalling and RMA around the same runtime-metrics labels, layout, and details-toggle behavior while keeping evaluator-specific summary sections separate.
- 2026-03-22: Task 54 completed. Toolcalling and RMA now render runtime metrics through the same shared UI section with identical core labels, identical detailed metrics, and the same details-toggle behavior, while keeping their evaluator summaries separate.
- 2026-03-19: Task 51 completed. Tool-calling batch runs now auto-detect the latest matching result TSV by source TSV name, batch mode, and prompt hash, preload prior rows by `unique_idx`, skip already-evaluated rows, restore aggregate metrics from saved results, and continue writing into the same result files as a resumed run.
- 2026-03-19: Task 52 completed. The tool-calling batch UI now uses `Top 1 / Top 50 / All`, shows whether a run is fresh or resumed, exposes skipped-row count for resumed runs, and displays the current result file name plus the last flush count without dumping export internals into the main screen.
- 2026-03-19: Phase 8 follow-up applied. Tool-calling result files can now be shared directly from the batch UI, and the export naming scheme was simplified to `{source_tsv}_{TestType}_{model_name}_results.tsv` plus `{source_tsv}_{TestType}_{model_name}_summary.json` while keeping the files under app-internal storage.
- 2026-03-19: Phase 8 follow-up applied. Failed parse or evaluation rows are now excluded from batch runtime aggregates, generation is capped at 200 tokens to avoid runaway malformed outputs, and the batch UI now offers a delete action that removes saved result files and resets progress so the next run resumes from a clean state.
- 2026-03-22: Phase 8 stability follow-up added. Batch `Stop` crash investigation is now split into a shared cancellation-safety spec plus an implementation task so Toolcalling, RMA, and E2E can stop long-running runs without racing native unload or reload.
- 2026-03-22: Task 55 completed. Batch stop and cancel handling is now frozen around a single safety contract: `Stop` enters a normal stopping state, no new row work begins, in-flight generation must fully exit before unload or reload, cleanup must be idempotent, and Toolcalling, RMA, and E2E all follow the same ordering rules.
- 2026-03-22: Task 56 completed. Toolcalling, RMA, and E2E now stop batch runs through a shared safe-cancel path that waits for in-flight generation to exit before cleanup, treats cancellation as a normal stopped state, and confines unload or restore work to non-cancellable final cleanup so pressing `Stop` no longer races native unload against active inference.
- 2026-03-22: Bundled `tc.tsv` is now shipped as an app asset and copied into app storage as the default gold TSV when no valid user-selected TSV is configured, so Toolcalling, RMA, and E2E can start from a known baseline dataset without manual import.
- 2026-03-22: Shared export-actions follow-up added. RMA should reuse the same batch export, share, resume-state, and delete-results UI used by Toolcalling, while keeping rewrite-specific evaluator summaries separate from tool-calling summary content.
- 2026-03-22: Task 57 completed. The docs now freeze `RMA` around the same batch export-actions contract as `Toolcalling`, explicitly sharing result or summary sharing, delete-results controls, file-path or flush state, and resumed-run messaging while keeping rewrite-specific evaluator summaries separate.
- 2026-03-22: Phase 8 export reuse applied. RMA batch runs now persist result TSV and summary JSON files, expose the same shared export-actions section used by Toolcalling, and support result sharing plus saved-result deletion without changing the rewrite-specific evaluator summary.
