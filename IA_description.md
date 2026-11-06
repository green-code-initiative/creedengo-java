This file contains the technical description of the project

Global description
---
This project is a SonarQube plugin named "creedengo-java" (Maven artifact `creedengo-java-plugin`, plugin key `creedengojava`).
This plugin is for Java project analysis.
When this plugin is installed inside SonarQube, it adds new rules for Java language analysis.
The new rules can be added to an existing Java quality profile or to a new Java quality profile.
A SonarQube quality profile must be associated with only one programming language.
A quality profile can be the default quality profile for one language (here Java), thus
all future Java project analyses will be done by default with this quality profile.
If a quality profile is not the default, a specific project (already analysed) can be associated with this
quality profile.

Technical description
---
Important elements of the plugin:
- the plugin contains a list of rule implementations
- each rule has a unique rule id (ex: "GCI67")
- each rule is implemented by one class
- there are one or several unit tests for each rule implementation class, based on resource files containing compliant and/or non-compliant code

The implementation code is in src/main/java.

SonarQube analysis is based on navigating the code (AST) to detect bad practices and to raise an issue when one is detected.

Implementation Structure
---
- rule implementations: flat package `org.greencodeinitiative.creedengo.java.checks` (one class per rule, no sub-package)
- each implementation class has the same code template:
  - "@Rule" annotation: gives the rule id (ex: `@Rule(key = "GCI67")`)
    - the rule id (and its metadata: description, severity, tags...) is previously defined in another maven component named "creedengo-rules-specifications" (dependency with classifier `java`)
    - "@DeprecatedRuleKey" annotations keep the former rule keys (ex: repository "ecocode-java" key "EC67", repository "greencodeinitiative-java" key "S67")
    - to enable a rule in the "creedengo way" quality profile, the rule id must be added in the resource file `src/main/resources/org/greencodeinitiative/creedengo/java/creedengo_way_profile.json`
  - extends the SonarQube API class "IssuableSubscriptionVisitor"
  - overrides "nodesToVisit()" to declare the list of AST node kinds (`Tree.Kind`) to be analysed
  - overrides "visitNode(Tree)" (and optionally "leaveNode(Tree)") to implement the analysis and call "reportIssue" when a bad practice is detected
    - complex analyses are delegated to private methods called from "visitNode"/"leaveNode"
- the plugin follows the SonarQube plugin development guidelines. Package `org.greencodeinitiative.creedengo.java`:
  - "JavaPlugin": entry point, declares the extensions "JavaRulesDefinition" (server side) and "JavaCheckRegistrar" (analysis side)
  - "JavaRulesDefinition": creates the SonarQube rule repository "creedengo-java" (language "java") and loads the rule metadata from the annotated rule classes
  - "JavaCheckRegistrar": registers the list of implemented rule classes (`checkClasses()`) for the Java analyzer. Every new rule class must be added here
  - "JavaCreedengoWayProfile": defines the quality profile "creedengo way" (rules activated come from creedengo_way_profile.json). It is intentionally NOT registered as an extension in "JavaPlugin" (JavaPluginTest expects exactly 2 extensions), so that this profile is not created when the plugin is installed in a real SonarQube. It is only used for tests: the integration tests load the profile on their own through the property `test-it.additional-profile-uris` (see pom.xml)

Unit Tests Implementation Structure
---
Inside the "src/test/java" directory, unit tests are in sub-packages, one per rule:
`org.greencodeinitiative.creedengo.java.checks.GCIxx` (ex: `checks.GCI67.IncrementCheckTest` tests `checks.IncrementCheck`).
Plugin-level tests (JavaPluginTest, JavaRulesDefinitionTest, JavaCheckRegistrarTest, JavaCreedengoWayProfileTest) are in `org.greencodeinitiative.creedengo.java`.

Each rule unit test class has the same code template:
- at least one test method using the "CheckVerifier" class (from `java-checks-testkit`) to simulate a SonarQube analysis:
  `CheckVerifier.newVerifier().onFile(System.getProperty("testfiles.path") + "/GCIxx/<File>.java").withCheck(new <Check>()).verifyIssues();`
  - `verifyIssues()` checks that the issues raised match the "// Noncompliant" comments
  - `verifyNoIssues()` checks that no issue is raised
- each test needs a resource file: the code file used for the simulated analysis

Test resource files are NOT in "src/test/files". They are the files of the integration test project, shared by unit and integration tests:
`src/it/test-projects/creedengo-java-plugin-test-project/src/main/java/org/greencodeinitiative/creedengo/java/checks/GCIxx/`
The system property "testfiles.path" (set in the maven-surefire-plugin configuration of pom.xml) points to the `.../checks` directory of this test project.

Each test resource file contains compliant and/or non-compliant code.
If there is non-compliant code on which SonarQube should raise an issue, the line with the issue has a comment at the end of the line with the following template:
"// Noncompliant {{ERROR_MESSAGE_TO_DISPLAY}}"
    - "ERROR_MESSAGE_TO_DISPLAY" is replaced by the real error message.
    - this comment tells "CheckVerifier" that an issue is expected at this line

Integration Tests Implementation Structure
---
Inside the "src/it/java" directory (added to the test sources by build-helper-maven-plugin), the package
`org.greencodeinitiative.creedengo.java.integration.tests` contains the class "GCIRulesIT".
This class has one or several test methods per rule (ex: testGCI27, testGCI2, testGCI2_noIssue, testGCI82_lombok...), plus a global test on measures and issues.

"GCIRulesIT" extends "GCIRulesBase" (package `org.greencodeinitiative.creedengo.integration.tests`), which comes from the external test dependency "creedengo-integration-test" and provides the integration test initialization.

The integration test process checks, in a local and real environment, that all implemented rules raise the expected issues.

The "src/it/test-projects/creedengo-java-plugin-test-project" directory contains a real Maven project to analyse (it also hosts the unit test resource files).

Here is the process (run by maven-failsafe-plugin with `./mvnw verify`):
- build the plugin project
- download and launch a specific SonarQube version on the local machine (property `test-it.sonarqube.version`)
- install the built plugin and sonar-java-plugin inside SonarQube
- load the quality profile "creedengo way" from creedengo_way_profile.json and associate it with the Java test project
- launch the analysis of the test project
- send the analysis result to the local SonarQube
- compare the result with the expectations described in each test method

Each test method describes the elements to check in the analysis result:
- the relative path of the resource file inside the test project (ex: "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI67/IncrementCheck.java")
- the complete rule id, made of 2 parts: the repository key ("creedengo-java") and the rule id (ex: "creedengo-java:GCI2")
- the rule error message
- the lines where issues should be raised: 2 arrays of the same size with the start line and the end line of each issue (empty arrays when no issue is expected)
- optionally, severity, type and effort (constants SEVERITY, TYPE, EFFORT_xxMIN)

Each test method ends with a call to the common method `checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines[, SEVERITY, TYPE, EFFORT_xxMIN])`.
