Rule Implementation Plan
---

# Rule Implementation Methodology

Use the TDD (Test-Driven Development) methodology to implement a rule in an efficient and structured way.

Prerequisite: the rule (id, description, metadata) must already exist in the "creedengo-rules-specifications" component (version set by the `creedengo-rules-specifications.version` property in pom.xml).

# Rule Implementation Process

## STEP 1: Define test resources + technical shell of the rule
- Define test resource file(s) for the rule with all possible cases:
  - Compliant code
  - Non-compliant code (line with the issue ends with `// Noncompliant {{message}}`)
  - Code with elements that should not be analyzed
- Location of test resource files (shared by unit and integration tests):
  `src/it/test-projects/creedengo-java-plugin-test-project/src/main/java/org/greencodeinitiative/creedengo/java/checks/GCIxx/`
  (unit tests reach this directory through the system property "testfiles.path")
- Create the rule implementation class in the package "org.greencodeinitiative.creedengo.java.checks"
    - Add the "@Rule" annotation with the correct rule id (ex: `@Rule(key = "GCIxx")`), and the "@DeprecatedRuleKey" annotations if former keys exist
    - Extend the "IssuableSubscriptionVisitor" class
    - Override the "nodesToVisit" and "visitNode" methods (and optionally "leaveNode"), which respectively declare the AST node kinds to analyze and implement the analysis logic, but leave their bodies minimal for now (empty list / empty body)
    - Register the class in "JavaCheckRegistrar" (`checkClasses()`)
    - Add the rule id in "creedengo_way_profile.json" (src/main/resources/org/greencodeinitiative/creedengo/java) if the rule must be active in the "creedengo way" profile
- Create the rule unit test class in the package "org.greencodeinitiative.creedengo.java.checks.GCIxx" (src/test/java), named `<CheckClass>Test`, following the existing template:
  `CheckVerifier.newVerifier().onFile(System.getProperty("testfiles.path") + "/GCIxx/<File>.java").withCheck(new <CheckClass>()).verifyIssues();`
- Verify that unit tests fail with non-compliant test resources

## STEP 2: Implement the rule + unit test validation
- Implement the rule in the implementation class created in step 1
- Implement the "nodesToVisit" method to declare the AST node kinds to analyze in depth, and implement the "visitNode" (and, if needed, "leaveNode") methods to perform the analysis on those nodes
- Implement private methods called from "visitNode"/"leaveNode" to analyze in depth the declared AST nodes and raise issues if needed
- Verify that unit tests pass with both compliant and non-compliant test resources

## STEP 3: Implement integration tests
- Add a test method in the integration class "GCIRulesIT" (src/it/java, package "org.greencodeinitiative.creedengo.java.integration.tests") for the implemented rule, using the same pattern as the existing test methods (`checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines[, SEVERITY, TYPE, EFFORT_xxMIN])`, rule id format "creedengo-java:GCIxx")
- Run `./mvnw verify` and verify that integration tests pass with both compliant and non-compliant test resources
