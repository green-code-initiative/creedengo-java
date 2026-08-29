# creedengo-java — Conventions and key guidelines

## Project structure

- Main package: `org.greencodeinitiative.creedengo.java.checks`
- Each rule extends `IssuableSubscriptionVisitor`, annotated with `@Rule(key = "GCI{N}")`
- Test files: `src/it/test-projects/.../checks/GCI{N}/` (never `src/test/files/`)
- Rule specifications: separate repository `creedengo-rules-specifications`

## Checklist for any change

1. **CHANGELOG.md** — Always add an entry under `[Unreleased]` → `### Changed` with format: `- [#PR](url) GCIXXX - description`
2. **Registration** — Any new rule must be added to `JavaCheckRegistrar.ANNOTATED_RULE_CLASSES` AND `creedengo_way_profile.json`
3. **License header** — GPL v3 required on all `.java` files
4. **Unit tests** — `CheckVerifier` with `// Noncompliant {{exact message}}` markers; cover compliant, noncompliant and edge cases
5. **Integration tests** — IT file in `src/it/test-projects/.../GCI{N}/` + entry in `GCIRulesIT.java`
6. **Coverage** — ≥ 80% on added/modified classes (instructions + branches)

## Code style

- Explicit imports (no wildcard `import org.xxx.*`)
- Blank line between each method
- No `var` (explicit typed declarations)
- Lines ≤ 120 characters
- K&R brace style, constants in `CONSTANT_CASE`
- `@Override` on every overridden method
- Error message as `protected static final String`, in correct English

## Runtime safety (blocking)

- **ClassCastException** — Every downcast must be guarded by `instanceof` or `.is(Tree.Kind.XXX)` before the cast
- **NullPointerException** — Guard chained calls on `tree.parent()`, `symbol.declaration()`, `Deque.peek()` etc.
- Never cast `.thenStatement()` / `.elseStatement()` directly to `BlockTree` without a check

## Best practices

- Avoid at all costs patterns that cause false positives.
- `nodesToVisit()` must return the **minimal** list of `Tree.Kind` needed
- Do not duplicate logic from an existing rule
- IT files must compile cleanly (full imports, no unused variables)
- Consistency between the error message in the code and the `// Noncompliant {{...}}` annotations in tests