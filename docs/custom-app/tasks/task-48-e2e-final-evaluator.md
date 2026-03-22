# Task 48: E2E Final Evaluator

## Goal

Score only the final tool-calling output in the E2E path.

## Read First

- [e2e-evaluator-spec.md](../specs/e2e-evaluator-spec.md)
- [tool-call-output-spec.md](../specs/tool-call-output-spec.md)

## Scope

- do not score intermediate RMA rewrite quality
- compare only the final tool-call output against TSV `answer`
- expose batch summary for the E2E path

## Completion Criteria

- E2E evaluation ignores intermediate rewrite quality for scoring
- final tool-call output is compared structurally against TSV `answer`
- E2E results are clearly separated from tool-calling-only and RMA-only summaries
