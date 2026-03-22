# Task 17: Freeze Tools Injection Assumptions

## Goal

Freeze how `{tools}` is produced before adding asset loaders and renderer changes.

## Scope

- define where API metadata comes from
- define how TSV `candidates` is parsed
- define the final `{tools}` string format
- define missing-plan and malformed-candidates behavior
- define how rewrite prompts use `{tools}` together with `{rewrited_query}`

## Required Specs

- [tools-injection-spec.md](../specs/tools-injection-spec.md)
- [api-metadata-asset-spec.md](../specs/api-metadata-asset-spec.md)

## Completion Criteria

- `{tools}` rendering assumptions are stable enough for implementation
- asset source, parsing rules, rendering format, and warning behavior are explicit
