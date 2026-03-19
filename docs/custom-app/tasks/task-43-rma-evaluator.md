# Task 43: RMA Evaluator

## Goal

Implement an RMA-specific evaluator that compares model rewrite output with TSV `rewrited_query`.

## Read First

- [rma-rewrite-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/rma-rewrite-spec.md)
- [tsv-schema.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/tsv-schema.md)

## Scope

- treat model output as rewrite text
- compare against TSV `rewrited_query`
- show batch summary for the RMA path separately from tool-calling accuracy

## Completion Criteria

- RMA evaluation no longer depends on tool-call parsing
- batch runs can score rewrite outputs against TSV `rewrited_query`
- RMA results are shown clearly in the dedicated RMA flow
