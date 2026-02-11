This file contains the technical description of the project

Global description
---
This project is a SonarQube plugin project and it is named "creedengo-java".
This plugin is for Java project analysis. 
When this plugin is installed inside SonarQube, it adds some new rules for Java language analysis.
The new rules can be added to an existing Java quality profile or to a new Java quality profile.
A Sonarqube quality profile must be associated to only one programming language.
A quality profile can be the default quality profile for one language (here Java language), thus 
all future Java project analysis will be done by default with this quality profile.
If a quality profile is not the default, a specific project (already analysed) can be associated with this
quality profile.

Technical description
---
Important elements of the plugin :
- the plugin contains a list of rule implementations
- each rule has a unique rule id
- each rule is implemented by one class
- there are several tests for each implementation rule class with several ressource files containing compliant code or / and non compliant code

The implementation code is in src/main/java.

Sonarqube analysis are based on the navigation inside the code to detect bad practices and to raise an issue when detected.
The code navigation is done through the AST principle.

Implementation Structure
---
- rule implementations : inside the package org.greencodeinitiative.creedengo.java.checks
- each implementation class has the same code template :
  - "Rule" annotation : to give the rule id
    - the rule id is previously defined in another maven component named "creedengo-rules-specifications"
    - to enable a rule, the rule id must be added in the resources file named "creedengo_way_profile.json"
  - extends a SonarQube API class "IssuableSubscriptionVisitor"
  - "initialize" method (forom super-class) to declare for which AST node type, the analysis will deeply analyse the code.
    - each "registerSyntaxNodeConsumer" method call implies that the node will be deeply analyzed. 
    - For each, a new private method is given to implement the deeply analysis and raise an issue if needed.
- to have a plugin working with activated rules, the Sonarqube plugin development guidelines is followed
  - "JavaPlugin" : enable 2 following extensions
  - "JavaRuleRepository" : extension containing the definition of the plugin and the list of implemented rule classes
  - "JavaCreedengoWayProfile" : extension containing the definition a the quality profile (and rules activated inside) created with the plugin installation

Unit Tests Implementation Structure
---
Inside the "test/java" directory, there are unit tests for each class of the plugin.
The package "org.greencodeinitiative.creedengo.java.checks" contains one unit test class for each rule implementation class

Each unit test class checking rule implementation class has the same template of code :
- at least one test method using the "CheckVerifier" class to check and simulate a SonarQube analysis
  - usage of "CheckVerifier.verify" to check if there is some issues raised or not 
  - usage of "CheckVerifier.verifyNoIssues" to check there is no issues raised
- each call to "CheckVerifier" needs a test resource file : this one is the resource code file for the simulated analysis

each test resource file is in the "test/resources/checks" directory (or sub-directories).
each test resource file contains compliuant code or / and non compliant code.
If there is no compliant code on which Sonarqube analysis should raise an issue, the line with the issue has a comment at the end of the line with the following template : 
"# Noncompliant {{ERROR_MESSAGE_TO_DISPLAY}}"
    - the "ERROR_MESSAGE_TO_DISPLAY" in the previous template is replaced by the real error message.
    - this comment give the information to "CheckVerifier", the simulation tool, that an error is expected at this line

Integration Tests Implementation Structure
---
Inside the "it/java" directory, there are integration tests for each class of the plugin.
The package "org.greencodeinitiative.creedengo.java.integration.tests" contains one unit test class with one method for each rule.

The integration test class extends the common class "GCIRulesBase" to use the system to initialize integration test.

Integration test system process exists to check in a local and real environment that all implemented rules do the raises expected issues.

The "src/it/test-projects" directory contains a real project to analyse.

Here is the process 
- build the plugin project
- download and launch a specific sonarqube version in local machine
- install the built plugin inside sonarqube
- create a specific default quality profile with all rules of the plugin installed
- launch the analysis of the test-project
- send analysis result to local sonarqube
- check the result of the analysis in front of expected results describe inside each test method

Each test method describe different elements to check inside the result analysis :
- the relative path of each resource test file inside de test-project
- the complete rule id containing 2 parts : the plugin id ("creedengo-java") and the rule id (ex : "GCI2")
- the rule error message
- the lines where errors shoudl be raised : there are 2 arrays with the same size representing the start line and the end line of each issue raised

Each test method ends with a call of a common method with all these parameters input.
