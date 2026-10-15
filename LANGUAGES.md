# Languages

## Implementations

| Language | Family | Directory | Implementation |
|---|---|---|---|
| C | general-purpose | [`languages/c`](languages/c) | native |
| C++ | general-purpose | [`languages/cpp`](languages/cpp) | native |
| Go | general-purpose | [`languages/go`](languages/go) | native |
| Java | jvm | [`languages/java`](languages/java) | native |
| Kotlin | jvm | [`languages/kotlin`](languages/kotlin) | native |
| JavaScript | scripting | [`languages/javascript`](languages/javascript) | native |
| Perl | scripting | [`languages/perl`](languages/perl) | native |
| PHP | scripting | [`languages/php`](languages/php) | native |
| Python | scripting | [`languages/python`](languages/python) | native |
| Ruby | scripting | [`languages/ruby`](languages/ruby) | native |
| Rust | general-purpose | [`languages/rust`](languages/rust) | native |

Verification status is computed by CI on every run (`./elc test`) and recorded
in `reports/report.json`. Only `verified` implementations count.

### Next up

Goal: 64 languages, built in sessions of ten. See the session plan in
[ROADMAP.md](ROADMAP.md#-64-the-first-goal).

## What counts as a distinct language

These rules decide what is counted. They are meant to be acceptable to an
external reviewer and may be refined through public discussion.

1. **Identifiable language.** It has its own documented syntax and semantics,
   and an interpreter, compiler or runtime that can execute it.
2. **Genuinely written in that language.** The algorithm's logic is in the
   declared language. Wrappers around another language's implementation do
   not count.
3. **Versions are not languages.** C89, C11 and C17 are one language (C).
   Python 2 and Python 3 are one language.
4. **Dialects are classified case by case.** A dialect counts separately only
   when it has a distinct name, specification or standard, and a distinct
   implementation (e.g. Scheme and Common Lisp count separately; GNU and
   MIT-style invocations of the same Scheme do not).
5. **Supersets are evaluated explicitly.** A superset language (e.g.
   TypeScript over JavaScript) counts separately only if the implementation
   relies on features of the superset in a substantive way. Each decision is
   recorded in this file.
6. **Embedded and domain-specific languages** (SQL dialects, shell languages,
   template languages) count if they can execute the full algorithm on their
   own, and are listed with their family.

## Implementation states

| State         | Meaning |
|---------------|---------|
| `proposed`    | Language requested or announced. |
| `in_progress` | Implementation started. |
| `submitted`   | Implementation submitted in a Pull Request. |
| `verified`    | Passes every mandatory check. |
| `rejected`    | Does not conform. |
| `unsupported` | Cannot currently run on the available infrastructure. |
