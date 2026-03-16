# Task 19: Extend Prompt Rendering With {tools}

## Goal

Support `{tools}` in prompt rendering using TSV candidates and loaded API metadata.

## Scope

- parse TSV `candidates`
- map each candidate plan to API metadata
- build the tools string
- replace `{tools}` in the prompt template

## Required Specs

- [tools-injection-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/tools-injection-spec.md)
- [prompt-template-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/prompt-template-spec.md)

## Completion Criteria

- prompt rendering supports `{tools}` in addition to the existing TSV placeholders
