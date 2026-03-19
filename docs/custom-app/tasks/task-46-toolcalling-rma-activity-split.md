# Task 46: Toolcalling And RMA Activity Split

## Goal

Split evaluator entry into separate tool-calling and RMA activities while reusing common runtime and UI pieces where practical.

## Read First

- [test-type-routing-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/test-type-routing-spec.md)
- [rma-rewrite-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/rma-rewrite-spec.md)

## Scope

- keep the current tool-calling evaluator as its own route
- add a dedicated RMA evaluator route
- wire setup entry so the selected test type opens the correct route

## Completion Criteria

- tool-calling and RMA no longer share the same evaluator route
- shared logic is reused where practical
- route selection follows the chosen test type
