# Task 36: Structural Evaluator

## Goal

Update evaluation so model outputs and TSV `answer` values are parsed and compared structurally rather than as raw strings.

## Read First

- [tool-call-output-spec.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/tool-call-output-spec.md)
- [evaluation-rule.md](/home/hj153lee/SmolChat-Android/docs/custom-app/specs/evaluation-rule.md)

## Scope

- parse TSV `answer` into the same structured tool-call object used for model output
- normalize dictionaries recursively before comparison
- preserve list order
- compare by deep equality or canonical serialized form
- keep Macro Accuracy reporting

## Likely Files

- [CustomAppEvaluator.kt](/home/hj153lee/SmolChat-Android/app/src/main/java/io/shubham0204/smollmandroid/ui/customapp/CustomAppEvaluator.kt)
- [CustomAppTsvLoader.kt](/home/hj153lee/SmolChat-Android/app/src/main/java/io/shubham0204/smollmandroid/ui/customapp/CustomAppTsvLoader.kt)
- [CustomAppJsonParser.kt](/home/hj153lee/SmolChat-Android/app/src/main/java/io/shubham0204/smollmandroid/ui/customapp/CustomAppJsonParser.kt)
- [CustomAppChatViewModel.kt](/home/hj153lee/SmolChat-Android/app/src/main/java/io/shubham0204/smollmandroid/ui/customapp/CustomAppChatViewModel.kt)

## Completion Criteria

- TSV `answer` is no longer treated as a raw gold string
- model output and gold answer are compared structurally
- key order differences do not cause false negatives
- batch summary and latest evaluation result still work
