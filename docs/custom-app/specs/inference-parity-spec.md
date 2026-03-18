# Inference Parity Spec

## Goal

Reduce quality differences between the Python Ollama inference path and the Android APK inference path by aligning the inputs and runtime assumptions that matter most.

## Primary Alignment Targets

### 1. Model Identity

- Python and APK must use the same underlying model weights when quality is compared
- quantization level, model size, and checkpoint lineage must match

### 2. Sampling Parameters

- temperature should match
- min-p or equivalent sampling knobs should match where possible
- generation length limit should match where possible

### 3. Prompt Construction

- Python currently sends a fully rendered raw prompt
- Android should support a comparable raw-prompt batch path for parity checks
- if chat-template wrapping is retained, it must be treated as a known difference

### 4. Structured Output Forcing

- Python uses Ollama `format=json`
- Android should either:
  - add a comparable hard JSON constraint if available, or
  - strengthen prompt-level output instructions enough to narrow the gap

## Practical Priorities

1. confirm same model weights
2. match temperature
3. verify raw prompt parity
4. align generation length limit
5. narrow JSON-forcing gap

## Minimum Parity Checklist

Before comparing Python and APK quality, confirm all of the following:

1. same model family and checkpoint lineage
2. same parameter size and quantization level
3. same rendered prompt text for the compared sample
4. same tool list source and rendered `{tools}` text
5. same temperature
6. same min-p or closest available equivalent
7. same generation length cap or closest available equivalent
8. same expectation about JSON-only output
9. known treatment of chat template wrapping

## Comparison Labels

- `meaningful parity comparison`
  - use only when the minimum parity checklist is satisfied or the remaining gaps are explicitly logged
- `exploratory comparison`
  - use when some checklist items are still unmatched

## Current Known Gaps

- Android still differs from Python on raw prompt execution unless a dedicated parity path is added
- Android currently does not mirror Ollama `format=json` exactly
- Android default temperature can be aligned for parity testing, but min-p and generation-length control may still differ

## Non-Goals

- perfect parity across different runtimes
- low-level numerical determinism across hardware
- matching performance metrics exactly
