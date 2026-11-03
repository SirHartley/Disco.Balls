# Changelog guide

How agents write entries in the root `Changelog`. Read it for every implementation task. Version numbers, release steps and how the release tooling reads the file are in [RELEASE.md](RELEASE.md).

## When to write an entry

- Every implementation task adds or amends its entries in the current unreleased section before merge, in the same commit as the change.
- Read-only work and documentation-only changes add nothing. Say why in the PR when a change needs no entry.
- Check whether the feature has been released: look for it under every version heading without `UNRELEASED`.
  - Not released yet: edit its `Added -` entry so it describes the feature as it now stands. No `Fixed -`, `Adjusted -` or `Removed -` line for it.
  - Released: the change gets its own entry.
- Record only completed changes from the actual diff. Never list plans or unfinished work.

## File layout

`Changelog` at the repository root, plain text, no extension. The fixed `Known issues` and `Notes` sections come first (`None` when empty), then the `Changelog` section with versions newest first and a blank line between them:

```text
1.1.d - UNRELEASED
Added - Version Checker support

1.1.c
Added - Mod icon
```

- Version heading: the version alone on its line, optionally followed by ` - UNRELEASED`. No `v` prefix, no date, no Markdown. The release tooling reads these headings, so keep them in this form.
- Entries: one change per line, starting `Added - `, `Adjusted - `, `Fixed - ` or `Removed - `.
- A `Note - ` line directly below an entry explains a limit or something the player must do.
- Optional short category lines (`Colours`, `Disco mode`, `General`) when a version has many entries.
- Use a `Development` category for useful changes to the build or authoring tools. Do not present a refactor or a documentation edit as a gameplay change.
- If no unreleased section exists, add one above the newest version. Ask the user which version comes next if it has not been chosen; do not invent a new version per commit.

## Wording

Entries are player-facing and follow the user's own changelog voice (the `house-style` skill, "Changelog entries", wins where this section is shorter).

- One clause, under 80 characters. Capital letter after the dash, no full stop. In-game names keep their capitals (`Disco Mode`, `Orbital Fusion Lamp`).
- Added: name, colon, what it does: `Added - Disco Mode: cycles the lamp colour to music`.
- Adjusted: numbers as old `->` new with the unit (`Adjusted - Colour step 0.365s -> 0.5s`), otherwise a direction word (Reduced, Increased, Halved).
- Fixed: what the player saw, in the forms `Fixed - X no longer does Y`, `Fixed - X is now properly Y`, `Fixed - Crash when X`, or a short noun phrase.
- Removed: the thing, then the reason or replacement in parentheses.
- Parentheses carry short extras such as `(was 3)` or a credit to a real person. No semicolons, curly quotes, first person, jokes or hedges about whether a fix works.
- No agent or model names, links, validation logs or commit hashes. Untested status goes in the PR.

## Amending versus adding

- Combine entries when several edits fix the same player-visible problem.
- An unreleased feature removed again before release loses its `Added -` entry; it gets no `Removed -` line.
- Never repeat a change once per file, step or PR.

## Maintenance

Update this guide when the `Changelog` layout or entry format changes. Release mechanics belong in [RELEASE.md](RELEASE.md).
