# Git Conventions

> This guide outlines the standards for Git, including branch naming and commit message formats, to ensure a clean, navigable, and automated version history.

## Branch Naming Strategy

All branch names must follow a prefix-based convention to clearly indicate their purpose.

- **`feature/<description>`**: For new features or enhancements.
  - Example: `feature/user-authentication`
- **`fix/<description>`**: For bug fixes.
  - Example: `fix/login-form-validation`
- **`docs/<description>`**: For documentation-only changes.
  - Example: `docs/update-readme`
- **`chore/<description>`**: For routine maintenance, refactoring, or build-related tasks.
  - Example: `chore/upgrade-gradle-wrapper`
- **`refactor/<description>`**: For code changes that neither fix a bug nor add a feature.
  - Example: `refactor/extract-user-service`

## Commit Message Format

We adhere to the [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) specification. This format enables automated changelog generation and semantic versioning.

The commit message should be structured as follows:

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

### Type

Must be one of the following:

- **feat**: A new feature.
- **fix**: A bug fix.
- **docs**: Documentation only changes.
- **style**: Changes that do not affect the meaning of the code (white-space, formatting, etc).
- **refactor**: A code change that neither fixes a bug nor adds a feature.
- **perf**: A code change that improves performance.
- **test**: Adding missing tests or correcting existing ones.
- **build**: Changes that affect the build system or external dependencies.
- **ci**: Changes to our CI configuration files and scripts.
- **chore**: Other changes that don't modify src or test files.

### Scope (Optional)

The scope provides additional contextual information and is contained within parentheses. E.g., `feat(api): ...`, `fix(client): ...`

### Description

The description contains a concise summary of the change.

- Use the imperative, present tense: "change" not "changed" nor "changes".
- Don't capitalize the first letter.
- No dot (.) at the end.

### Example Commits

```
feat(auth): add password reset functionality
```
```
fix(api): correct pagination query parameter

Closes #123
```
