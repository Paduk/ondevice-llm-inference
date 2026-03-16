# TSV Schema

## Purpose

This file defines the MVP assumption for the gold-label TSV format.

## Current Status

- Status: `frozen-for-mvp`

## MVP Schema

TSV is the source of truth for gold labels.

Required columns:

- `conversation_history`
- `query`
- `rewrited_query`
- `answer`
- `unique_idx`
- `candidates`

Optional columns:

- any extra columns are allowed and ignored by the MVP evaluator

## Example

```tsv
conversation_history	query	rewrited_query	answer	unique_idx	candidates
[]	original question	rewritten question	positive	item-0001	positive|negative
```

## MVP Loading Rule

- The TSV loader validates that the header contains all required columns.
- Each `unique_idx` must be unique.
- Empty `unique_idx` values are invalid.
- Empty `answer` values are invalid.
- Multiple labels per row are not supported in the MVP.

## Gold Source Decision

- For the MVP, gold TSV is imported by the user from local storage.
- Bundled TSV support is out of scope for the first implementation.

## Notes For Later Expansion

- Bundled answer sets can be added later.
- Multi-label rows can be added later if evaluation requirements expand.
