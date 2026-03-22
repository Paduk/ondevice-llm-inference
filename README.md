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

If you are working on the current RMA Ondevice Inference App, use these docs first:

1. [RMA Ondevice Inference App Docs Map](docs/custom-app/README.md)
2. [Roadmap](docs/custom-app/roadmap.md)

## Current Focus

Current project status is tracked in:

- [Roadmap](docs/custom-app/roadmap.md)

That file contains:

- current phase
- current focus
- checklist
- change log
- execution order

## Key Docs

- [Architecture](docs/custom-app/architecture.md)
- [Batch Result Export Spec](docs/custom-app/specs/batch-result-export-spec.md)
- [Runtime Metrics Spec](docs/custom-app/specs/runtime-metrics-spec.md)
- [RMA Rewrite Spec](docs/custom-app/specs/rma-rewrite-spec.md)

## Repo Notes

- app entry and setup flow live under [ui/customapp](app/src/main/java/io/shubham0204/smollmandroid/ui/customapp)
- on-device inference engine lives under [smollm](smollm)
- default bundled gold TSV is [tc.tsv](app/src/main/assets/tc.tsv)

## Upstream Base

This project is based on the original `SmolChat-Android` codebase, but the active development workflow is centered on the RMA Ondevice Inference App docs above rather than the original upstream README structure.
