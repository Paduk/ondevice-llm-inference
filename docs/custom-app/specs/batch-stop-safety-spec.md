# Batch Stop Safety Spec

## Goal

Define a single stop or cancel contract for long-running batch execution so the app can stop Toolcalling, RMA, and E2E batch runs without crashing or corrupting model state.

## Problem Statement

- pressing `Stop` during batch execution currently reaches cancellation, unload, and reload paths at nearly the same time
- generation may still be active in Kotlin or native code when the model is unloaded
- native resources must not be closed while in-flight generation work is still reading or mutating them

## Required Safety Rules

- `Stop` must be treated as a normal control-flow path, not as a fatal error
- batch cancellation must stop new row execution immediately
- in-flight generation must fully finish cancellation before unload or close is allowed
- model unload and reload must not race with an active native completion loop
- cleanup must be idempotent so repeated stop signals do not produce duplicate teardown

## Ordering Contract

1. mark batch state as stopping so no new row work starts
2. request generation stop or cancellation
3. wait until the active generation job has fully completed its cancellation path
4. only after the generation job is done, unload or reset native model state if needed
5. restore the interactive chat state or idle batch state

## UI State Rules

- UI may show `Stopping...` while waiting for generation shutdown
- final state after successful stop should be a normal stopped state, not an error state
- partial output may remain visible if that helps debugging, but it must not imply successful completion
- result-export progress that was already flushed may remain available after stop

## Scope

- Toolcalling batch flow
- RMA batch flow
- E2E batch flow
- `SmolLMManager` cancellation and unload lifecycle
- Kotlin to JNI to native teardown ordering

## Non-Goals

- redesigning batch export format
- changing evaluation semantics
- adding background execution or OS-level task recovery

## Acceptance Criteria

- repeated `Stop` presses do not crash the app
- stopping during active generation does not close the native model before generation has exited
- stopping during row-to-row transitions also remains safe
- Toolcalling, RMA, and E2E use the same cancellation contract
- the app can start another run after a stopped batch without restart
