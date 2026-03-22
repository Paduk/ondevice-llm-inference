# Task 53: Shared Runtime Metrics Spec

## Goal

Freeze the rule that Toolcalling and RMA should present runtime metrics through the same shared UI and metric-shape contract.

## Scope

- confirm that Toolcalling and RMA use the same always-visible core metrics
- confirm that Toolcalling and RMA use the same expandable detailed metrics
- keep evaluator summaries separate from runtime metrics
- define shared component boundaries so future changes do not drift between flows

## Read First

- [runtime-metrics-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/runtime-metrics-spec.md)
- [rma-rewrite-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/rma-rewrite-spec.md)
- [test-type-routing-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/test-type-routing-spec.md)

## Completion Criteria

- docs clearly state that Toolcalling and RMA share the same runtime-metrics UI
- docs clearly separate runtime-metrics reuse from evaluator-specific logic
- roadmap points the next task at extracting or reusing the actual shared component

