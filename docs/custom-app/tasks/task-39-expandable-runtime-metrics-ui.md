# Task 39: Expandable Runtime Metrics UI

## Goal

Show core metrics by default and hide detailed runtime and batch metrics behind an expandable section so the main screen stays readable.

## Read First

- [runtime-metrics-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/runtime-metrics-spec.md)

## Scope

- keep core metrics always visible
- add expandable detailed metrics UI
- add batch totals and averages to the detailed section

## Likely Files

- [CustomAppChatViewModel.kt](/home/hj153lee/SmolChat-Android/app/src/main/java/io/shubham0204/smollmandroid/ui/customapp/CustomAppChatViewModel.kt)
- [CustomAppRoot.kt](/home/hj153lee/SmolChat-Android/app/src/main/java/io/shubham0204/smollmandroid/ui/customapp/CustomAppRoot.kt)

## Completion Criteria

- main screen shows only core metrics by default
- detailed metrics are visible only after user expansion
- batch totals and averages are shown in the detailed section
