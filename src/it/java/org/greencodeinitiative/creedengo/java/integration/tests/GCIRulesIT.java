package org.greencodeinitiative.creedengo.java.integration.tests;

import org.junit.jupiter.api.Test;
import org.sonarqube.ws.Issues;
import org.sonarqube.ws.Measures;

import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;

class GCIRulesIT extends GCIRulesBase {

    @Test
    void testGCI74() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/AvoidFullSQLRequestCheck.java";
        int[] startLines = new int[]{8, 12, 17, 23};
        int[] endLines = new int[]{8, 12, 17, 23};
        String ruleId = "creedengo-java:GCI74";
        String ruleMsg = "Don't use the query SELECT * FROM";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines, SEVERITY, TYPE, EFFORT_20MIN);

    }

    @Test
    void testMeasuresAndIssues() {
        String projectKey = analyzedProjects.get(0).getProjectKey();

        Map<String, Measures.Measure> measures = getMeasures(projectKey);

        assertThat(ofNullable(measures.get("code_smells")).map(Measures.Measure::getValue).map(Integer::parseInt).orElse(0))
                .isGreaterThan(1);

        List<Issues.Issue> projectIssues = searchIssuesForComponent(projectKey, null).getIssuesList();
        assertThat(projectIssues).isNotEmpty();

    }

    @Test
    void testGCI3_forEachLoopIgnored() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/AvoidGettingSizeCollectionInForEachLoopIgnored.java";
        int[] startLines = new int[]{};
        int[] endLines = new int[]{};
        String ruleId = "creedengo-java:GCI3";
        String ruleMsg = "Avoid getting the size of the collection in the loop";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);

    }

    @Test
    void testGCI3_forLoopBad() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/AvoidGettingSizeCollectionInForLoopBad.java";
        int[] startLines = new int[]{13};
        int[] endLines = new int[]{13};
        String ruleId = "creedengo-java:GCI3";
        String ruleMsg = "Avoid getting the size of the collection in the loop";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);

    }

    @Test
    void testGCI3_forEachLoopGood() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/AvoidGettingSizeCollectionInForLoopGood.java";
        int[] startLines = new int[]{};
        int[] endLines = new int[]{};
        String ruleId = "creedengo-java:GCI3";
        String ruleMsg = "Avoid getting the size of the collection in the loop";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);

    }

    @Test
    void testGCI3_forLoopIgnored() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/AvoidGettingSizeCollectionInForLoopIgnored.java";
        int[] startLines = new int[]{};
        int[] endLines = new int[]{};
        String ruleId = "creedengo-java:GCI3";
        String ruleMsg = "Avoid getting the size of the collection in the loop";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);

    }

    @Test
    void testGCI3_whileLoopBad() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/AvoidGettingSizeCollectionInWhileLoopBad.java";
        int[] startLines = new int[]{17};
        int[] endLines = new int[]{17};
        String ruleId = "creedengo-java:GCI3";
        String ruleMsg = "Avoid getting the size of the collection in the loop";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);

    }

    @Test
    void testGCI3_whileLoopGood() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/AvoidGettingSizeCollectionInWhileLoopGood.java";
        int[] startLines = new int[]{};
        int[] endLines = new int[]{};
        String ruleId = "creedengo-java:GCI3";
        String ruleMsg = "Avoid getting the size of the collection in the loop";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);

    }

    @Test
    void testGCI3_whileLoopIgnored() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/AvoidGettingSizeCollectionInWhileLoopIgnored.java";
        int[] startLines = new int[]{};
        int[] endLines = new int[]{};
        String ruleId = "creedengo-java:GCI3";
        String ruleMsg = "Avoid getting the size of the collection in the loop";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);

    }

    @Test
    void testGCI2() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/AvoidMultipleIfElseStatement.java";

        int[] startLines = new int[]{
                24, 43, 45, 71, 88, 110,
                112, 131, 135, 137, 158, 164,
                190, 209, 212, 214, 211, 236,
                257, 259
        };

        int[] endLines = new int[]{
                24, 43, 47, 71, 90, 110,
                114, 133, 135, 139, 160, 166,
                192, 209, 212, 216, 217, 238,
                257, 261
        };

        String ruleId = "creedengo-java:GCI2";
        String ruleMsg = "Use a switch statement instead of multiple if-else if possible";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);

    }

    @Test
    void testGCI2_compareMethodNoIssue() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/AvoidMultipleIfElseStatementCompareMethodNoIssue.java";

        int[] startLines = new int[]{};

        int[] endLines = new int[]{};

        String ruleId = "creedengo-java:GCI2";
        String ruleMsg = "Use a switch statement instead of multiple if-else if possible";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);

    }

    @Test
    void testGCI2_interfaceNoIssue() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/AvoidMultipleIfElseStatementInterfaceNoIssue.java";

        int[] startLines = new int[]{};

        int[] endLines = new int[]{};

        String ruleId = "creedengo-java:GCI2";
        String ruleMsg = "Use a switch statement instead of multiple if-else if possible";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);

    }

    @Test
    void testGCI2_noBlockNoIssue() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/AvoidMultipleIfElseStatementNoBlockNoIssue.java";

        int[] startLines = new int[]{};

        int[] endLines = new int[]{};

        String ruleId = "creedengo-java:GCI2";
        String ruleMsg = "Use a switch statement instead of multiple if-else if possible";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);

    }

    @Test
    void testGCI2_noIssue() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/AvoidMultipleIfElseStatementNoIssue.java";

        int[] startLines = new int[]{};

        int[] endLines = new int[]{};

        String ruleId = "creedengo-java:GCI2";
        String ruleMsg = "Use a switch statement instead of multiple if-else if possible";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);

    }

    @Test
    void testGCI77_invalid() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/AvoidRegexPatternNotStatic.java";
        int[] startLines = new int[]{8};
        int[] endLines = new int[]{8};
        String ruleId = "creedengo-java:GCI77";
        String ruleMsg = "Avoid using Pattern.compile() in a non-static context.";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines, SEVERITY, TYPE, EFFORT_20MIN);

    }

    @Test
    void testGCI77_valid1() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/AvoidRegexPatternNotStaticValid1.java";
        int[] startLines = new int[]{};
        int[] endLines = new int[]{};
        String ruleId = "creedengo-java:GCI77";
        String ruleMsg = "Avoid using Pattern.compile() in a non-static context.";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines, SEVERITY, TYPE, EFFORT_20MIN);

    }

    @Test
    void testGCI77_valid2() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/AvoidRegexPatternNotStaticValid2.java";
        int[] startLines = new int[]{};
        int[] endLines = new int[]{};
        String ruleId = "creedengo-java:GCI77";
        String ruleMsg = "Avoid using Pattern.compile() in a non-static context.";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines, SEVERITY, TYPE, EFFORT_20MIN);

    }

    @Test
    void testGCI77_valid3() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/AvoidRegexPatternNotStaticValid3.java";
        int[] startLines = new int[]{};
        int[] endLines = new int[]{};
        String ruleId = "creedengo-java:GCI77";
        String ruleMsg = "Avoid using Pattern.compile() in a non-static context.";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines, SEVERITY, TYPE, EFFORT_20MIN);

    }

    @Test
    void testGCI78() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/AvoidSetConstantInBatchUpdateCheck.java";
        int[] startLines = new int[]{
                34, 35, 36, 37, 38, 39,
                40, 41, 42, 43, 44, 45,
                46, 61, 63, 64, 65, 66,
                67, 70, 86, 88, 90, 91,
                92, 93, 94, 96, 114, 116,
                117, 118, 119, 120, 121, 123
        };
        int[] endLines = new int[]{
                34, 35, 36, 37, 38, 39,
                40, 41, 42, 43, 44, 45,
                46, 61, 63, 64, 65, 66,
                67, 70, 86, 88, 90, 91,
                92, 93, 94, 96, 114, 116,
                117, 118, 119, 120, 121, 123
        };
        String ruleId = "creedengo-java:GCI78";
        String ruleMsg = "Avoid setting constants in batch update";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines, SEVERITY, TYPE, EFFORT_15MIN);

    }

    @Test
    void testGCI1_loop() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/AvoidSpringRepositoryCallInLoopCheck.java";

        int[] startLines = new int[]{32};

        int[] endLines = new int[]{32};

        String ruleId = "creedengo-java:GCI1";
        String ruleMsg = "Avoid Spring repository call in loop or stream";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines, SEVERITY, TYPE, EFFORT_50MIN);

    }

    @Test
    void testGCI1_stream() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/AvoidSpringRepositoryCallInStreamCheck.java";

        int[] startLines = new int[]{36, 46, 56, 66, 76, 84, 96, 105};

        int[] endLines = new int[]{36, 46, 56, 66, 76, 84, 96, 105};

        String ruleId = "creedengo-java:GCI1";
        String ruleMsg = "Avoid Spring repository call in loop or stream";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines, SEVERITY, TYPE, EFFORT_50MIN);

    }

    @Test
    void testGCI72() {
        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/AvoidSQLRequestInLoopCheck.java";
        String ruleId = "creedengo-java:GCI72";
        String ruleMsg = "Avoid SQL request in loop";
        int[] startLines = new int[]{57, 88, 119};
        int[] endLines = new int[]{57, 88, 119};

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines, SEVERITY, TYPE, EFFORT_10MIN);
    }

    @Test
    void testGCI5() {
        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/AvoidStatementForDMLQueries.java";
        String ruleId = "creedengo-java:GCI5";
        String ruleMsg = "You must not use Statement for a DML query";
        int[] startLines = new int[]{18};
        int[] endLines = new int[]{18};

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines, SEVERITY, TYPE, EFFORT_10MIN);
    }

    @Test
    void testGCI76() {
        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/AvoidUsageOfStaticCollections.java";
        String ruleId = "creedengo-java:GCI76";
        String ruleMsg = "Avoid usage of static collections.";
        int[] startLines = new int[]{10, 12, 14};
        int[] endLines = new int[]{10, 12, 14};

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines, SEVERITY, TYPE, EFFORT_20MIN);
    }

    @Test
    void testGCI76_good() {
        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/AvoidUsageOfStaticCollectionsGoodWay.java";
        String ruleId = "creedengo-java:GCI76";
        String ruleMsg = "Avoid usage of static collections.";
        int[] startLines = new int[]{};
        int[] endLines = new int[]{};

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines, SEVERITY, TYPE, EFFORT_20MIN);
    }

    @Test
    void testGCI79() {
        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/FreeResourcesOfAutoCloseableInterface.java";
        String ruleId = "creedengo-java:GCI79";
        String ruleMsg = "try-with-resources Statement needs to be implemented for any object that implements the AutoClosable interface.";
        int[] startLines = new int[]{23};
        int[] endLines = new int[]{36};

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines, SEVERITY, TYPE, EFFORT_15MIN);
    }

    @Test
    void testGCI32() {
        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/InitializeBufferWithAppropriateSize.java";
        String ruleId = "creedengo-java:GCI32";
        String ruleMsg = "Initialize StringBuilder or StringBuffer with appropriate size";
        int[] startLines = new int[]{16, 24};
        int[] endLines = new int[]{16, 24};

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);
    }

    @Test
    void testGCI67() {
        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/IncrementCheck.java";
        String ruleId = "creedengo-java:GCI67";
        String ruleMsg = "Use ++i instead of i++";
        int[] startLines = new int[]{9, 24, 47};
        int[] endLines = new int[]{9, 24, 47};

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);
    }

    @Test
    void testGCI82() {
        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/MakeNonReassignedVariablesConstants.java";
        String ruleId = "creedengo-java:GCI82";
        String ruleMsg = "The variable is never reassigned and can be 'final'";
        int[] startLines = new int[]{7, 12, 13, 45};
        int[] endLines = new int[]{7, 12, 13, 45};

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);
    }

    @Test
    void testGCI69() {
        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/NoFunctionCallWhenDeclaringForLoop.java";
        String ruleId = "creedengo-java:GCI69";
        String ruleMsg = "Do not call a function when declaring a for-type loop";
        int[] startLines = new int[]{58, 66, 74, 101};
        int[] endLines = new int[]{58, 66, 74, 101};

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);
    }

    @Test
    void testGCI28() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/OptimizeReadFileExceptionCheck.java";

        int[] startLines = new int[]{23};

        int[] endLines = new int[]{23};

        String ruleId = "creedengo-java:GCI28";
        String ruleMsg = "Optimize Read File Exceptions";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);

    }

    @Test
    void testGCI28_2() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/OptimizeReadFileExceptionCheck2.java";

        int[] startLines = new int[]{20};

        int[] endLines = new int[]{20};

        String ruleId = "creedengo-java:GCI28";
        String ruleMsg = "Optimize Read File Exceptions";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);

    }

    @Test
    void testGCI28_3() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/OptimizeReadFileExceptionCheck3.java";

        int[] startLines = new int[]{19};

        int[] endLines = new int[]{19};

        String ruleId = "creedengo-java:GCI28";
        String ruleMsg = "Optimize Read File Exceptions";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);

    }

    @Test
    void testGCI28_4() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/OptimizeReadFileExceptionCheck4.java";

        int[] startLines = new int[]{18};

        int[] endLines = new int[]{18};

        String ruleId = "creedengo-java:GCI28";
        String ruleMsg = "Optimize Read File Exceptions";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);

    }

    @Test
    void testGCI28_5() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/OptimizeReadFileExceptionCheck5.java";

        int[] startLines = new int[]{18};

        int[] endLines = new int[]{18};

        String ruleId = "creedengo-java:GCI28";
        String ruleMsg = "Optimize Read File Exceptions";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);

    }

    @Test
    void testGCI94() {
        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/UseOptionalOrElseGetVsOrElse.java";
        String ruleId = "creedengo-java:GCI94";
        String ruleMsg = "Use optional orElseGet instead of orElse.";
        int[] startLines = new int[]{25};
        int[] endLines = new int[]{25};

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines, SEVERITY, TYPE, EFFORT_1MIN);
    }

}
