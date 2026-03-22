# Task 54: Shared Runtime Metrics Component

## Goal

Refactor Toolcalling and RMA so they render runtime metrics through the same shared composable or shared UI helper.

## Scope

- extract the existing runtime-metrics card into a reusable component or helper
- use the same core rows in Toolcalling and RMA
- use the same expandable detailed metrics in Toolcalling and RMA
- keep Toolcalling-specific and RMA-specific evaluation summary sections separate

## Read First

- [runtime-metrics-spec.md](../specs/runtime-metrics-spec.md)
- [rma-rewrite-spec.md](../specs/rma-rewrite-spec.md)

## Completion Criteria

- Toolcalling and RMA show the same runtime metric labels and ordering
- both flows use the same details toggle behavior
- runtime metric code no longer needs to be maintained in two separate UI copies

