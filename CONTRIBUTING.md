Please read common [CONTRIBUTING.md](https://github.com/green-code-initiative/creedengo-common/blob/main/doc/CONTRIBUTING.md) in `creedengo-common` repository.

## Maven compatibility in CI

`maven-git-versioning-extension` 9.12.1 relies on `MessageUtils`, which Maven 3.10
no longer provides. The build workflows put the wrapper's Maven 3.9.9 on `PATH`
so integration tests invoking `mvn` use the same Maven as `./mvnw`. The release
workflow also uses `./mvnw`. The distribution path is read from the wrapper's
`Maven home` output rather than selecting an arbitrary cached distribution.

Keep `src/it/test-projects/creedengo-java-plugin-test-project/.mvn/maven.config`
empty (no comments or options). It gives the integration test project its own
Maven root, preventing it from loading the repository's versioning extension.

The `dependency-submission.yml` workflow replaces GitHub's automatic Maven
dependency submission. In **Settings > Advanced Security > Dependency graph**,
keep the dependency graph enabled and **disable Automatic dependency submission**
to prevent the GitHub-managed workflow from continuing to use the runner Maven.
Fork and Dependabot pull requests skip submission because their token is read-only.

Once the versioning extension supports Maven 3.10, update it and reassess the
`PATH` workaround and the test project's empty configuration. Keeping the wrapper
for releases and the explicit dependency submission workflow remains valid.
