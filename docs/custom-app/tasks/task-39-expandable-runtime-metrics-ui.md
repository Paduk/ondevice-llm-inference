# Task 39: Expandable Runtime Metrics UI

## Goal

Show core metrics by default and hide detailed runtime and batch metrics behind an expandable section so the main screen stays readable.

## Read First

- [runtime-metrics-spec.md](../specs/runtime-metrics-spec.md)

## Scope

- keep core metrics always visible
- add expandable detailed metrics UI
- add batch totals and averages to the detailed section

## Likely Files

- [CustomAppChatViewModel.kt](../../../app/src/main/java/io/shubham0204/smollmandroid/ui/customapp/CustomAppChatViewModel.kt)
- [CustomAppRoot.kt](../../../app/src/main/java/io/shubham0204/smollmandroid/ui/customapp/CustomAppRoot.kt)

## Completion Criteria

- main screen shows only core metrics by default
- detailed metrics are visible only after user expansion
- batch totals and averages are shown in the detailed section
