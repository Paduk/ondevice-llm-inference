# Task 41: RMA Entry And Activity

## Goal

Add a dedicated RMA evaluation activity or screen flow that reuses the common compact-app structure without sharing the tool-calling evaluator.

## Read First

- [rma-rewrite-spec.md](../specs/rma-rewrite-spec.md)
- [architecture.md](../architecture.md)

## Scope

- add a dedicated RMA activity or route
- keep the current tool-calling activity intact
- wire setup or entry controls so users can open the RMA path explicitly

## Completion Criteria

- app has a separate user-facing path for RMA evaluation
- RMA path does not route through the tool-calling evaluator
- shared setup and runtime pieces are reused where practical
