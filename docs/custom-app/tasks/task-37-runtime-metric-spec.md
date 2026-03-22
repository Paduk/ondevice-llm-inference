# Task 37: Runtime Metric Spec

## Goal

Freeze how runtime metrics should be split between prefill and generation, how batch aggregates should be computed, and how the UI should expose core versus detailed metrics.

## Read First

- [runtime-metrics-spec.md](../specs/runtime-metrics-spec.md)

## Scope

- define the per-case metric set
- define the batch aggregate metric set
- define the UI exposure policy
- freeze the preferred expandable metrics UI pattern

## Completion Criteria

- docs clearly distinguish prefill vs generation vs total time
- docs define batch totals and averages
- docs define `always visible` vs `expandable details`
