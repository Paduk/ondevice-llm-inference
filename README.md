# RMAEvaluator

## Purpose

This repository is an Android on-device LLM evaluator built on top of `SmolChat-Android`.

The current app supports:

- `Toolcalling` evaluation
- `RMA` rewrite evaluation
- `E2E` evaluation
- local GGUF model import
- on-device inference
- TSV-driven batch testing
- runtime metrics, result export, and resume support

## Start Here

If you are working on the current custom app, use these docs first:

1. [Custom App Docs Map](/home/hj153lee/SmolChat-Android/docs/custom-app/README.md)
2. [Roadmap](/home/hj153lee/SmolChat-Android/docs/custom-app/roadmap.md)

## Current Focus

Current project status is tracked in:

- [Roadmap](/home/hj153lee/SmolChat-Android/docs/custom-app/roadmap.md)

That file contains:

- current phase
- current focus
- checklist
- change log
- execution order

## Key Docs

- [Architecture](/home/hj153lee/SmolChat-Android/docs/custom-app/architecture.md)
- [Task 1-8 Summary (KO)](/home/hj153lee/SmolChat-Android/docs/custom-app/task-1-8-summary-ko.md)
- [Batch Result Export Spec](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/batch-result-export-spec.md)
- [Runtime Metrics Spec](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/runtime-metrics-spec.md)
- [RMA Rewrite Spec](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/rma-rewrite-spec.md)

## Repo Notes

- app entry and setup flow live under [ui/customapp](/home/hj153lee/SmolChat-Android/app/src/main/java/io/shubham0204/smollmandroid/ui/customapp)
- on-device inference engine lives under [smollm](/home/hj153lee/SmolChat-Android/smollm)
- default bundled gold TSV is [tc.tsv](/home/hj153lee/SmolChat-Android/app/src/main/assets/tc.tsv)

## Upstream Base

This project is based on the original `SmolChat-Android` codebase, but the active development workflow is centered on the custom app docs above rather than the original upstream README structure.
