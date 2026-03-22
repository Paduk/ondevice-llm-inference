# Task 47: E2E Activity And Runner

## Goal

Add a dedicated E2E evaluator activity that runs RMA first and then tool-calling rewrite with the predicted rewrite.

## Read First

- [test-type-routing-spec.md](../specs/test-type-routing-spec.md)
- [e2e-evaluator-spec.md](../specs/e2e-evaluator-spec.md)

## Scope

- add an E2E route
- support same-family pipelines only
- run RMA then tool-calling rewrite for each TSV row
- preserve intermediate rewrite for debugging

## Completion Criteria

- E2E route exists
- E2E runner executes both models in order
- intermediate rewrite is visible
- final output is produced by the tool-calling model
