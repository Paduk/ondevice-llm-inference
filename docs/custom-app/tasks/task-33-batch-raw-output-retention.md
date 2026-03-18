# Task 33: Batch Raw Output Retention

## Goal

Keep raw model outputs visible in the conversation section during and after a batch run, even when parsing or evaluation fails, and only clear them when a new batch run starts.

## Read First

- [parser-and-batch-retention-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/parser-and-batch-retention-spec.md)
- [batch-run-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/batch-run-spec.md)

## Scope

- preserve batch assistant raw outputs in the visible conversation state
- prevent parse or evaluation failures from removing the raw output
- clear prior batch conversation entries only when a new batch run begins
- keep latest parse and evaluation state separate from raw output visibility

## Likely Files

- [CustomAppChatViewModel.kt](/home/hj153lee/SmolChat-Android/app/src/main/java/io/shubham0204/smollmandroid/ui/customapp/CustomAppChatViewModel.kt)
- [CustomAppRoot.kt](/home/hj153lee/SmolChat-Android/app/src/main/java/io/shubham0204/smollmandroid/ui/customapp/CustomAppRoot.kt)

## Completion Criteria

- raw batch outputs remain visible after parse failure
- raw batch outputs remain visible after evaluation failure
- starting a new batch clears the previous batch conversation outputs
- stopping or completing a batch does not immediately clear the outputs from that batch
