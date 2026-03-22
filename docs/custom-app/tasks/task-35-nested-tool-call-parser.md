# Task 35: Nested Tool-Call Parser

## Goal

Replace the old flat-output parser with a parser that can read nested tool-call objects from either JSON-style or Python-style dict strings.

## Read First

- [tool-call-output-spec.md](../specs/tool-call-output-spec.md)
- [parser-and-batch-retention-spec.md](../specs/parser-and-batch-retention-spec.md)

## Scope

- support both JSON-style and Python-style quoting
- support nested dictionaries
- support lists inside `arguments`
- normalize into a structured tool-call object
- stop assuming `query`, `rewrited_query`, `generated`, and `answer`

## Likely Files

- [CustomAppJsonParser.kt](../../../app/src/main/java/io/shubham0204/smollmandroid/ui/customapp/CustomAppJsonParser.kt)
- [CustomAppChatViewModel.kt](../../../app/src/main/java/io/shubham0204/smollmandroid/ui/customapp/CustomAppChatViewModel.kt)
- [CustomAppRoot.kt](../../../app/src/main/java/io/shubham0204/smollmandroid/ui/customapp/CustomAppRoot.kt)

## Completion Criteria

- nested `plan/arguments` outputs parse successfully
- empty `arguments` also parse successfully
- malformed outputs still fail clearly
- parse result UI reflects the new tool-call structure
