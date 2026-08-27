creedengo-java
===========

_creedengo_ is a collective project aiming to reduce environmental footprint of software at the code level. The goal of
the project is to provide a list of static code analyzers to highlight code structures that may have a negative
ecological impact: energy and resources over-consumption, "fatware", shortening terminals' lifespan, etc.

_creedengo_ is based on evolving catalogs
of [good practices](https://github.com/green-code-initiative/creedengo-rules-specifications/blob/main/docs/rules), for various technologies.
This
SonarQube plugin then implements these catalogs as rules for scanning your Java projects.

> ⚠️ This is still a very early stage project. Any feedback or contribution will be highly appreciated. Please
> refer to the contribution section.

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-2.1-4baaaa.svg)](https://github.com/green-code-initiative/creedengo-common/blob/main/doc/CODE_OF_CONDUCT.md)

🌿 SonarQube Plugins
-------------------

This plugin is part of the creedengo project.\
You can find a list of all our other plugins in
the [creedengo repository](https://github.com/green-code-initiative/creedengo-rules-specifications#-sonarqube-plugins)

🚀 Getting Started
------------------

You can give a try with a one command:

```sh
./mvnw verify -Pkeep-running
```

... then you can use Java test project repository to test the environment : see [Java test project in `./src/it/test-projects/creedengo-java-plugin-test-project`](./src/it/test-projects/creedengo-java-plugin-test-project)

NB: To install other `creedengo` plugins, you can :

- add JAVA System properties `Dtest-it.additional-plugins` with a comma separated list of plugin IDs (`groupId:artifactId:version`), or plugins JAR (`file://....`) to install.

  For example :

  ```sh
  ./mvnw verify -Pkeep-running -Dtest-it.additional-plugins=org.sonarsource.javascript:sonar-plugin:10.1.0.21143
  ```
- install different creedengo plugins with Marketplace (inside admin panel of SonarQube)

You can also directly use a [all-in-one docker-compose](https://github.com/green-code-initiative/creedengo-common/blob/main/doc/INSTALL.md#start-sonarqube-if-first-time)

... and configure local SonarQube (security config and quality profile : see [configuration](https://github.com/green-code-initiative/creedengo-common/blob/main/doc/INSTALL.md#configuration-sonarqube) for more details).

♻️ Reused native SonarQube rules (`eco-design`)
-----------------

Some native SonarQube rules already provide eco-design improvements and are **not** reimplemented by
this plugin. They are declared, next to the plugin rules, in the `reusedNativeRules` sub-structure of
[`creedengo_way_profile.json`](./src/main/resources/org/greencodeinitiative/creedengo/java/creedengo_way_profile.json):

```json
{
  "ruleKeys": [ "GCI1", "GCI2" ],
  "reusedNativeRules": [
    { "key": "java:S6904", "reference": "https://github.com/green-code-initiative/creedengo-rules-specifications/pull/487" }
  ]
}
```

Both mechanisms share a **single source of truth** for the list of reused rule keys —
`JavaCreedengoWayProfile.reusedNativeRuleKeys()`, the very same method used to build the built-in
profile — so there is one place reading `reusedNativeRules` from the JSON, reused by two consumers:

- **Activation in the profile**: `JavaCreedengoWayProfile.define()` reuses the very same built-in
  profile mechanism that activates the plugin's own `GCI*` rules (`BuiltInQualityProfilesDefinition`,
  run by SonarQube internally at server startup). Its
  `NewBuiltInQualityProfile.activateRule(repositoryKey, ruleKey)` accepts **any** repository key, not
  only this plugin's own, so `java:S6904` is activated in the "creedengo way" profile with zero admin
  Web API call and zero credentials — exactly like a built-in rule.
- **Tagging** (`eco-design` label on the rule itself, for search/filtering): unlike activation, a
  rule's tags are metadata SonarQube only lets an administrator change for a rule owned by *another*
  plugin, through the Web API (`api/rules/update`, requires *Administer Quality Profiles*) — there is
  no built-in/declarative equivalent. `NativeRuleTagger` (a server-side component started/stopped
  along with the plugin) reads the same `JavaCreedengoWayProfile.reusedNativeRuleKeys()` list and
  tags those rules when the plugin starts, then removes the tag again when it stops (e.g. on
  uninstall or upgrade) — reusing the same admin call already used by `creedengo-integration-test`'s
  `ReusedRulesConfigurator` during integration tests. The **only** thing to configure is a single
  admin token:

  | Property | Default | Description |
  | --- | --- | --- |
  | `sonar.creedengo.reusedNativeRules.token` | – | User token with *Administer Quality Profiles*, used to tag/untag the reused rules. Leave empty to disable this automatic tagging (activation in the profile still works either way). |

  There is intentionally no other property (no tag name, no base URL, no login/password, no on/off
  switch): the tag is fixed to `eco-design`, the base URL is read from SonarQube itself
  (`Server#getPublicRootUrl()`), and the feature is simply enabled by setting the token.

🛒 Distribution
------------------

Ready to use binaries are available [from GitHub](https://github.com/green-code-initiative/creedengo-java/releases).

🧩 Compatibility
-----------------

| Plugin version | SonarQube version  | Java version |
|----------------|--------------------|--------------|
| 1.6.+          | 9.4.+ to 10.6.+    | 11 / 17      |
| 1.7.+          | 9.9.+ to 10.6.+    | 17           |
| 2.0.+ / 2.1.+  | 9.9.+ to 25.12.+   | 17           |
| 2.2.+          | 24.12.+ to 25.12.+ | 17           |
| 2.2.+          | 25.2.+ to 26.7.+   | 21           |

> Compatibility table of versions lower than 1.4.+ are available from the
> main [creedengo repository](https://github.com/green-code-initiative/creedengo-rules-specifications#-plugins-version-compatibility).

🤝 Contribution
---------------

check [creedengo repository](https://github.com/green-code-initiative/creedengo-rules-specifications#-contribution)

🤓 Main contributors
--------------------

check [creedengo repository](https://github.com/green-code-initiative/creedengo-rules-specifications#-main-contributors)

Links
-----

- https://docs.sonarqube.org/latest/analysis/overview/
