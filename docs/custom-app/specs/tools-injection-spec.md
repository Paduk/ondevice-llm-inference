# Tools Injection Spec

## Goal

Define how `{tools}` is built from TSV `candidates` and bundled simple API metadata.

## Input Sources

- TSV column: `candidates`
- app asset: `simple_api.json`

## Candidate Parsing

- `candidates` is expected to be a Python-style list string
- each parsed value is treated as one tool plan key
- preserve TSV order when building the final tools string
- duplicate plan keys are allowed in the raw TSV string, but the renderer should keep only the first occurrence for MVP
- parsing behavior should match the current dataset style, for example:
  - `['ACTION_OPEN_CONTENT', 'ACTION_SHOW_ALARMS']`
- a lightweight Python-list parser is sufficient for MVP

## API Metadata Lookup

- API metadata is loaded into a `plan -> parameter_list` map
- `simple_api.json` is a single JSON object keyed by plan name
- each value is an ordered list of parameter names
- no additional normalization is required for MVP

## Prompt Rendering Format

For each candidate tool:

- render one line as `{plan}: {parameter_list}`
- join lines with `\n`
- no bullet markers or numbering
- include a trailing newline after each rendered tool line only if convenient for implementation

## Rewrite Prompt Assumption

- for the current rewrite inference path, the active prompt may use:
  - `{tools}`
  - `{rewrited_query}`
- `{query}` and `{conversation_history}` may remain unused in some prompts and that is acceptable

## Error Handling

- missing candidate parse -> row-level rendering error
- missing plan in metadata -> keep running, but track a warning for that plan
- empty candidates list -> render `{tools}` as an empty string for MVP
- if some candidate plans are found and others are missing, render only the found plans and keep warnings for the missing ones
- if all candidate plans are missing, render `{tools}` as an empty string and mark a warning, not a hard failure
- malformed asset JSON is an asset-loading failure, not a row-level warning

## Persistence And Scope

- tools metadata is read-only runtime data from assets
- no user editing UI is required for the metadata in MVP
- no per-row tools string persistence is required in shared preferences or database

## Scope

- bundled asset loading only
- no runtime download or remote refresh
