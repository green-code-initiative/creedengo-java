Rule Implementation Plan
---

# Rule Implementation Methodology

Use the TDD (Test-Driven Development) methodology to implement a rule in an efficient and structured way.

# Rule Implementation Process

## STEP 1: Define unit test resources + technical shell of the rule
- Define test resources for the rule to be implemented with all possible cases:
  - Compliant code
  - Non-compliant code
  - Code with elements that should not be analyzed
- Location of test resource files:
    - Files for unit tests: in "test/resources/checks" or subdirectories
    - Files for integration tests: in "src/it/test-projects" or subdirectories
- Create the rule implementation class in the package "org.greencodeinitiative.creedengo.java.checks"
    - Add the "Rule" annotation with the correct rule id (the rule is previously defined in another maven component named "creedengo-rules-specifications")
    - Extend the "IssuableSubscriptionVisitor" class
    - Implement the "initialize" method which will be used to declare the AST node types to analyze in depth but leave it empty for now
- Create the rule unit test class in the package "org.greencodeinitiative.creedengo.java.checks"
- Verify that unit tests fail with non-compliant test resources

## STEP 2: Implement the rule + unit test validation
- Implement the rule in the implementation class created in step 1
- Implement the "initialize" method to declare the AST node types to analyze in depth
- Implement private methods to analyze in depth the declared AST nodes and raise issues if needed
- Verify that unit tests pass with both compliant and non-compliant test resources

## STEP 3: Implement integration tests
- Add a test method in the integration class "GCIRulesIT" for the implemented rule using the same pattern as other existing test methods
- Verify that integration tests pass with both compliant and non-compliant test resources
