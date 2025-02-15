package org.greencodeinitiative.creedengo.java.integration.tests;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.sonarqube.ws.Issues;
import org.sonarqube.ws.Measures;

import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.sonarqube.ws.Common.RuleType.CODE_SMELL;
import static org.sonarqube.ws.Common.Severity.MINOR;

class GCIRulesIT extends BuildProjectEngine {

    @Test
    void testMeasuresAndIssues() {
        String projectKey = analyzedProjects.get(0).getProjectKey();

        Map<String, Measures.Measure> measures = getMeasures(projectKey);

        assertThat(ofNullable(measures.get("code_smells")).map(Measures.Measure::getValue).map(Integer::parseInt).orElse(0))
                .isGreaterThan(1);

        List<Issues.Issue> projectIssues = issuesForComponent(projectKey);
        assertThat(projectIssues).isNotEmpty();

    }

    @Test
    void testGCI3() {
        String projectKey = analyzedProjects.get(0).getProjectKey();

        List<Issues.Issue> issues = issuesForFile(projectKey,
                "src/main/java/org/greencodeinitiative/creedengo/java/checks/AvoidGettingSizeCollectionInForLoopBad.java");

        assertThat(issues)
                .hasSize(2)
                .extracting("rule", "message", "line", "textRange.startLine", "textRange.endLine",
                        "textRange.startOffset", "textRange.endOffset", "severity", "type", "debt", "effort")
                .containsExactlyInAnyOrder(
                        Tuple.tuple("creedengo-java:GCI3", "Avoid getting the size of the collection in the loop",
                                13, 13, 13, 28, 45, MINOR, CODE_SMELL, "5min", "5min"),
                        Tuple.tuple("creedengo-java:GCI69", "Do not call a function when declaring a for-type loop",
                                13, 13, 13, 28, 45, MINOR, CODE_SMELL, "5min", "5min")
                );

    }

    @Test
    void testGCI69() {
        String projectKey = analyzedProjects.get(0).getProjectKey();

        List<Issues.Issue> issues = issuesForFile(projectKey,
                "src/main/java/org/greencodeinitiative/creedengo/java/checks/NoFunctionCallWhenDeclaringForLoop.java");

        assertThat(issues)
            .hasSize(4)
            .extracting("rule", "message", "line", "textRange.startLine", "textRange.endLine",
                    "textRange.startOffset", "textRange.endOffset", "severity", "type", "debt", "effort")
            .containsExactly(
                    Tuple.tuple("creedengo-java:GCI69", "Do not call a function when declaring a for-type loop",
                            58, 58, 58, 28, 40, MINOR, CODE_SMELL, "5min", "5min"),
                    Tuple.tuple("creedengo-java:GCI69", "Do not call a function when declaring a for-type loop",
                            66, 66, 66, 34, 46, MINOR, CODE_SMELL, "5min", "5min"),
                    Tuple.tuple("creedengo-java:GCI69", "Do not call a function when declaring a for-type loop",
                            74, 74, 74, 39, 51, MINOR, CODE_SMELL, "5min", "5min"),
                    Tuple.tuple("creedengo-java:GCI69", "Do not call a function when declaring a for-type loop",
                            101, 101, 101, 108, 132, MINOR, CODE_SMELL, "5min", "5min")
            );

    }

    @Test
    void testGCI82() {
        String projectKey = analyzedProjects.get(0).getProjectKey();

        List<Issues.Issue> issues = issuesForFile(projectKey,
                "src/main/java/org/greencodeinitiative/creedengo/java/checks/MakeNonReassignedVariablesConstants.java");

        assertThat(issues)
                .hasSize(4)
                .extracting("rule", "message", "line", "textRange.startLine", "textRange.endLine",
                        "textRange.startOffset", "textRange.endOffset", "severity", "type", "debt", "effort")
                .contains(
                        Tuple.tuple("creedengo-java:GCI82", "The variable is never reassigned and can be 'final'",
                                7, 7, 7, 4, 67, MINOR, CODE_SMELL, "5min", "5min"),
                        Tuple.tuple("creedengo-java:GCI82", "The variable is never reassigned and can be 'final'",
                                12, 12, 12, 4, 56, MINOR, CODE_SMELL, "5min", "5min"),
                        Tuple.tuple("creedengo-java:GCI82", "The variable is never reassigned and can be 'final'",
                                13, 13, 13, 4, 50, MINOR, CODE_SMELL, "5min", "5min"),
                        Tuple.tuple("creedengo-java:GCI82", "The variable is never reassigned and can be 'final'",
                                45, 45, 45, 8, 25, MINOR, CODE_SMELL, "5min", "5min")
                );

    }

    @Test
    void testGCI94() {
        String projectKey = analyzedProjects.get(0).getProjectKey();

        List<Issues.Issue> issues = issuesForFile(projectKey,
                "src/main/java/org/greencodeinitiative/creedengo/java/checks/UseOptionalOrElseGetVsOrElse.java");

        assertThat(issues)
                .hasSize(1)
                .extracting("rule", "message", "line", "textRange.startLine", "textRange.endLine",
                        "textRange.startOffset", "textRange.endOffset", "severity", "type", "debt", "effort")
                .containsExactly(
                        Tuple.tuple(
                                "creedengo-java:GCI94", "Use optional orElseGet instead of orElse.",
                                25, 25, 25, 38, 69, MINOR, CODE_SMELL, "1min", "1min")
                );

    }

}
