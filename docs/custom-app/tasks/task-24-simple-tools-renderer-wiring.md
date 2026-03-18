# Task 24: Simple Tools Renderer Wiring

## Goal

Switch prompt rendering and batch wiring so `{tools}` always comes from `simple_api.json`.

## Scope

- update prompt renderer input type from detailed metadata to simple parameter lists
- render each candidate line as `{plan}: {parameter_list}`
- keep `missingPlans`, `parsedCandidateCount`, and `renderedToolCount`
- use the simple asset loader for both preview and batch inference
- keep diagnostics UI aligned with the new source

## Required Reads

- [tools-injection-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/tools-injection-spec.md)
- [api-metadata-asset-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/api-metadata-asset-spec.md)

## Likely Files

- `app/src/main/java/io/shubham0204/smollmandroid/ui/customapp/CustomAppPromptTemplateRenderer.kt`
- `app/src/main/java/io/shubham0204/smollmandroid/ui/customapp/CustomAppChatViewModel.kt`
- `app/src/main/java/io/shubham0204/smollmandroid/ui/customapp/CustomAppRoot.kt`
- `docs/custom-app/roadmap.md`

## Completion Criteria

- preview and batch inference both use simple tools rendering
- rendered tools lines match Python SFT behavior closely enough for prompt parity
- diagnostics continue to show missing plans and rendered counts
