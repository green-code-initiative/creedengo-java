# Technical debt

Temporary workarounds to reassess once their root cause is fixed.

## TD-1 - Maven 3.10 breaks `maven-git-versioning-extension` (Oct 2026)

### Problem

- GitHub runner images now ship Maven 3.10, which no longer provides `maven-shared-utils`.
- `me.qoomon:maven-git-versioning-extension` 9.12.1, declared in `.mvn/extensions.xml`, still relies on its `MessageUtils` class.
- Calls to the runner's `mvn` fail during project scanning with `NoClassDefFoundError: org/apache/maven/shared/utils/logging/MessageUtils`.
- The wrapper uses Maven 3.9.9 and is not affected.
- Affected entry points include integration tests launching `mvn` on the test project, release builds, and GitHub-managed automatic dependency submission.

### Workarounds in place

| Where | Workaround |
|---|---|
| `.github/workflows/build.yml`, `build-jdk17.yml`, `dependency-submission.yml` | `Use Maven wrapper distribution as "mvn"` puts the wrapper distribution first on `PATH`. Its exact path comes from `Maven home`, avoiding an arbitrary cached distribution. `Check Maven version` prints the selected version. |
| `.github/workflows/tag_release.yml` | Uses `./mvnw` instead of the runner's `mvn`. |
| `src/it/test-projects/creedengo-java-plugin-test-project/.mvn/maven.config` | Contains explanatory comments only, with no Maven options. Makes the test project its own Maven root so it does not load the repository's versioning extension. |
| `.github/workflows/dependency-submission.yml` | Replaces GitHub-managed automatic dependency submission. Keep **Dependency graph enabled** and **Automatic dependency submission disabled** in **Settings > Advanced Security > Dependency graph**. Fork and Dependabot pull requests skip submission because their token is read-only. |

### Related fix (not Maven-3.10 related)

`creedengo-integration-test` 0.7.0 uses `sonar-orchestrator`, which requires a
recent `commons-lang3` (`ObjectUtils.getIfNull(T, T)` requires version 3.18 or
newer). Analyzer plugins can bundle dependencies that shadow the versions
needed by the orchestrator.

The `maven-failsafe-plugin` configuration in `pom.xml` excludes
`org.sonarsource.java:sonar-java-plugin` from the integration-test classpath.
The analyzer is installed in the SonarQube instance via `test-it.plugins` and
is not needed in the Failsafe JVM. Keep this exclusion independently of the
Maven workaround. It does not change the unit-test classpath used by
`java-checks-testkit`.

### Actions when the extension supports Maven 3.10

1. Update `.mvn/extensions.xml` to an upstream release compatible with Maven 3.10.
2. Remove the `Use Maven wrapper distribution as "mvn"` and `Check Maven version` steps from the three workflows after validating integration tests on JDK 17 and 21.
3. Keep `./mvnw` for releases unless there is a reason to use the runner Maven.
4. Remove the test project's `.mvn/maven.config` and confirm that integration tests still pass.
5. Decide whether to retain the explicit dependency submission workflow or delete it and re-enable GitHub-managed automatic dependency submission.
6. If upgrading the wrapper to Maven 3.10, validate the full build and address any compatibility issues.
7. Coordinate equivalent cleanup in `creedengo-php` and `creedengo-python`.
8. Update `CONTRIBUTING.md` and this document, preserving the separate Failsafe exclusion explanation while it remains relevant.

### References

- Original fix: https://github.com/green-code-initiative/creedengo-php/pull/164
- Maven release notes: https://maven.apache.org/docs/3.10.0/release-notes.html
- Runner image update: https://github.com/actions/runner-images/issues/14883
- Versioning extension: https://github.com/qoomon/maven-git-versioning-extension
- Example extension fix: https://github.com/MahiFX/maven-git-versioning-extension/pull/52
