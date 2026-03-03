# 0.0.14

## Features
- Add scan start/finish logging to the Act goal.
- Include processed file path in Act processing.

## Fixes
- Normalize prompts to use LF line endings.
- Normalize tool output newlines.

## Refactoring
- Simplify Act prompt flow.

## Documentation
- Update Act docs: document `ActProcessor` behavior, template keys, placeholder usage, and add a practical example for custom Acts.
- Update Act docs and commit-act template: add site nav link; clarify auto-commit of grouped changes; refine CLI/config docs for act/default prompt and `scanDir`.
- Update Maven plugin docs (rename `gw.genai` to `gw.model`).
- Update Ghostwriter docs and processing notes.
- Update commit-act instructions.

## Chores
- Update Act directory logging.
- Update Act logging and prompt indentation.
- Update Act processing and Maven act prompt handling.
