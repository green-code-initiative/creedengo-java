package org.greencodeinitiative.creedengo.java.integration.tests;

import org.junit.jupiter.api.Test;
import org.sonarqube.ws.Issues;
import org.sonarqube.ws.Measures;

import java.util.List;
import java.util.Map;

import org.greencodeinitiative.creedengo.integration.tests.GCIRulesBase;

import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;

class GCIRulesIT extends GCIRulesBase {

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
    void testGCI27() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI27/ArrayCopyCheck.java";
        String ruleId = "creedengo-java:GCI27";
        String ruleMsg = "Use System.arraycopy to copy arrays";
        int[] startLines = new int[]{
                68, 73, 80, 89, 102, 111,
                122, 133, 156, 162, 170, 180,
                194, 204, 216, 228, 246, 253,
                262, 273, 288, 299, 312, 325,
                351, 358, 367, 378, 393, 406,
                432, 439, 448, 459, 474, 487
        };
        int[] endLines = new int[]{
                70, 77, 86, 99, 108, 119,
                130, 141, 158, 166, 176, 190,
                200, 212, 224, 236, 249, 258,
                269, 284, 295, 308, 321, 334,
                354, 363, 374, 389, 402, 415,
                435, 444, 455, 470, 483, 496
        };

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines, SEVERITY, TYPE, EFFORT_20MIN);
    }

    @Test
    void testGCI74() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI74/AvoidFullSQLRequestCheck.java";
        int[] startLines = new int[]{27, 31, 36, 42};
        int[] endLines = new int[]{27, 31, 36, 42};
        String ruleId = "creedengo-java:GCI74";
        String ruleMsg = "Don't use the query SELECT * FROM";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines, SEVERITY, TYPE, EFFORT_20MIN);
    }

    @Test
    void testGCI3_forEachLoopIgnored() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI3/AvoidGettingSizeCollectionInForEachLoopIgnored.java";
        int[] startLines = new int[]{};
        int[] endLines = new int[]{};
        String ruleId = "creedengo-java:GCI3";
        String ruleMsg = "Avoid getting the size of the collection in the loop";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);
    }

    @Test
    void testGCI3_forLoopBad() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI3/AvoidGettingSizeCollectionInForLoopBad.java";
        int[] startLines = new int[]{13};
        int[] endLines = new int[]{13};
        String ruleId = "creedengo-java:GCI3";
        String ruleMsg = "Avoid getting the size of the collection in the loop";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);
    }

    @Test
    void testGCI3_forEachLoopGood() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI3/AvoidGettingSizeCollectionInForLoopGood.java";
        int[] startLines = new int[]{};
        int[] endLines = new int[]{};
        String ruleId = "creedengo-java:GCI3";
        String ruleMsg = "Avoid getting the size of the collection in the loop";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);
    }

    @Test
    void testGCI3_forLoopIgnored() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI3/AvoidGettingSizeCollectionInForLoopIgnored.java";
        int[] startLines = new int[]{};
        int[] endLines = new int[]{};
        String ruleId = "creedengo-java:GCI3";
        String ruleMsg = "Avoid getting the size of the collection in the loop";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);
    }

    @Test
    void testGCI3_whileLoopBad() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI3/AvoidGettingSizeCollectionInWhileLoopBad.java";
        int[] startLines = new int[]{35};
        int[] endLines = new int[]{35};
        String ruleId = "creedengo-java:GCI3";
        String ruleMsg = "Avoid getting the size of the collection in the loop";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);
    }

    @Test
    void testGCI3_whileLoopGood() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI3/AvoidGettingSizeCollectionInWhileLoopGood.java";
        int[] startLines = new int[]{};
        int[] endLines = new int[]{};
        String ruleId = "creedengo-java:GCI3";
        String ruleMsg = "Avoid getting the size of the collection in the loop";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);
    }

    @Test
    void testGCI3_whileLoopIgnored() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI3/AvoidGettingSizeCollectionInWhileLoopIgnored.java";
        int[] startLines = new int[]{};
        int[] endLines = new int[]{};
        String ruleId = "creedengo-java:GCI3";
        String ruleMsg = "Avoid getting the size of the collection in the loop";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);
    }

    @Test
    void testGCI2() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI2/AvoidMultipleIfElseStatement.java";

        int[] startLines = new int[]{
                41, 60, 62, 88, 105, 127,
                129, 148, 152, 154, 175, 181,
                207, 226, 228, 229, 231, 253,
                274, 276
        };

        int[] endLines = new int[]{
                41, 60, 64, 88, 107, 127,
                131, 150, 152, 156, 177, 183,
                209, 226, 234, 229, 233, 255,
                274, 278
        };

        String ruleId = "creedengo-java:GCI2";
        String ruleMsg = "Use a switch statement instead of multiple if-else if possible";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);
    }

    @Test
    void testGCI2_compareMethodNoIssue() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI2/AvoidMultipleIfElseStatementCompareMethodNoIssue.java";

        int[] startLines = new int[]{};

        int[] endLines = new int[]{};

        String ruleId = "creedengo-java:GCI2";
        String ruleMsg = "Use a switch statement instead of multiple if-else if possible";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);
    }

    @Test
    void testGCI2_interfaceNoIssue() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI2/AvoidMultipleIfElseStatementInterfaceNoIssue.java";

        int[] startLines = new int[]{};

        int[] endLines = new int[]{};

        String ruleId = "creedengo-java:GCI2";
        String ruleMsg = "Use a switch statement instead of multiple if-else if possible";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);
    }

    @Test
    void testGCI2_noBlockNoIssue() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI2/AvoidMultipleIfElseStatementNoBlockNoIssue.java";

        int[] startLines = new int[]{};

        int[] endLines = new int[]{};

        String ruleId = "creedengo-java:GCI2";
        String ruleMsg = "Use a switch statement instead of multiple if-else if possible";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);
    }

    @Test
    void testGCI2_noIssue() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI2/AvoidMultipleIfElseStatementNoIssue.java";

        int[] startLines = new int[]{};

        int[] endLines = new int[]{};

        String ruleId = "creedengo-java:GCI2";
        String ruleMsg = "Use a switch statement instead of multiple if-else if possible";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);
    }

    @Test
    void testGCI77_invalid() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI77/AvoidRegexPatternNotStatic.java";
        int[] startLines = new int[]{25};
        int[] endLines = new int[]{25};
        String ruleId = "creedengo-java:GCI77";
        String ruleMsg = "Avoid using Pattern.compile() in a non-static context.";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines, SEVERITY, TYPE, EFFORT_20MIN);
    }

    @Test
    void testGCI77_valid1() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI77/AvoidRegexPatternNotStaticValid1.java";
        int[] startLines = new int[]{};
        int[] endLines = new int[]{};
        String ruleId = "creedengo-java:GCI77";
        String ruleMsg = "Avoid using Pattern.compile() in a non-static context.";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines, SEVERITY, TYPE, EFFORT_20MIN);
    }

    @Test
    void testGCI77_valid2() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI77/AvoidRegexPatternNotStaticValid2.java";
        int[] startLines = new int[]{};
        int[] endLines = new int[]{};
        String ruleId = "creedengo-java:GCI77";
        String ruleMsg = "Avoid using Pattern.compile() in a non-static context.";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines, SEVERITY, TYPE, EFFORT_20MIN);
    }

    @Test
    void testGCI77_valid3() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI77/AvoidRegexPatternNotStaticValid3.java";
        int[] startLines = new int[]{};
        int[] endLines = new int[]{};
        String ruleId = "creedengo-java:GCI77";
        String ruleMsg = "Avoid using Pattern.compile() in a non-static context.";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines, SEVERITY, TYPE, EFFORT_20MIN);
    }

    @Test
    void testGCI78() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI78/AvoidSetConstantInBatchUpdateCheck.java";
        int[] startLines = new int[]{
                52, 53, 54, 55, 56, 57,
                58, 59, 60, 61, 62, 63,
                79, 81, 82, 83, 84, 87,
                103, 105, 106, 107, 108, 109,
                110, 112, 130, 132, 133, 134,
                135, 136, 137, 139
        };
        int[] endLines = new int[]{
                52, 53, 54, 55, 56, 57,
                58, 59, 60, 61, 62, 63,
                79, 81, 82, 83, 84, 87,
                103, 105, 106, 107, 108, 109,
                110, 112, 130, 132, 133, 134,
                135, 136, 137, 139
        };
        String ruleId = "creedengo-java:GCI78";
        String ruleMsg = "Avoid setting constants in batch update";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines, SEVERITY, TYPE, EFFORT_15MIN);
    }

    @Test
    void testGCI1_loop() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI1/AvoidSpringRepositoryCallInLoopCheck.java";

        int[] startLines = new int[]{32};

        int[] endLines = new int[]{32};

        String ruleId = "creedengo-java:GCI1";
        String ruleMsg = "Avoid Spring repository call in loop or stream";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines, SEVERITY, TYPE, EFFORT_50MIN);
    }

    @Test
    void testGCI1_stream() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI1/AvoidSpringRepositoryCallInStreamCheck.java";

        int[] startLines = new int[]{
                37, 48, 59, 72, 87, 98,
                113, 123
        };

        int[] endLines = new int[]{
                37, 48, 59, 72, 87, 98,
                113, 123
        };

        String ruleId = "creedengo-java:GCI1";
        String ruleMsg = "Avoid Spring repository call in loop or stream";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines, SEVERITY, TYPE, EFFORT_50MIN);
    }

    @Test
    void testGCI72() {
        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI72/AvoidSQLRequestInLoopCheck.java";
        String ruleId = "creedengo-java:GCI72";
        String ruleMsg = "Avoid SQL request in loop";
        int[] startLines = new int[]{74, 105, 136};
        int[] endLines = new int[]{74, 105, 136};

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines, SEVERITY, TYPE, EFFORT_10MIN);
    }

    @Test
    void testGCI5() {
        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI5/AvoidStatementForDMLQueries.java";
        String ruleId = "creedengo-java:GCI5";
        String ruleMsg = "You must not use Statement for a DML query";
        int[] startLines = new int[]{33};
        int[] endLines = new int[]{33};

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines, SEVERITY, TYPE, EFFORT_10MIN);
    }

    @Test
    void testGCI76() {
        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI76/AvoidUsageOfStaticCollections.java";
        String ruleId = "creedengo-java:GCI76";
        String ruleMsg = "Avoid usage of static collections.";
        int[] startLines = new int[]{27, 29, 31};
        int[] endLines = new int[]{27, 29, 31};

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines, SEVERITY, TYPE, EFFORT_20MIN);
    }

    @Test
    void testGCI76_good() {
        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI76/AvoidUsageOfStaticCollectionsGoodWay.java";
        String ruleId = "creedengo-java:GCI76";
        String ruleMsg = "Avoid usage of static collections.";
        int[] startLines = new int[]{};
        int[] endLines = new int[]{};

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines, SEVERITY, TYPE, EFFORT_20MIN);
    }

    @Test
    void testGCI79() {
        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI79/FreeResourcesOfAutoCloseableInterface.java";
        String ruleId = "creedengo-java:GCI79";
        String ruleMsg = "try-with-resources Statement needs to be implemented for any object that implements the AutoCloseable interface.";
        int[] startLines = new int[]{40};
        int[] endLines = new int[]{53};

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines, SEVERITY, TYPE, EFFORT_15MIN);
    }

    @Test
    void testGCI32() {
        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI32/InitializeBufferWithAppropriateSize.java";
        String ruleId = "creedengo-java:GCI32";
        String ruleMsg = "Initialize StringBuilder or StringBuffer with appropriate size";
        int[] startLines = new int[]{38, 46};
        int[] endLines = new int[]{38, 46};

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);
    }

    @Test
    void testGCI67() {
        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI67/IncrementCheck.java";
        String ruleId = "creedengo-java:GCI67";
        String ruleMsg = "Use ++i instead of i++";
        int[] startLines = new int[]{31, 51, 74};
        int[] endLines = new int[]{31, 51, 74};

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);
    }

    @Test
    void testGCI82() {
        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI82/MakeNonReassignedVariablesConstants.java";
        String ruleId = "creedengo-java:GCI82";
        String ruleMsg = "The variable is never reassigned and can be 'final'";
        int[] startLines = new int[]{9, 14, 15, 20, 26, 29, 48, 75, 108, 121};
        int[] endLines = new int[]{9, 14, 15, 20, 26, 29, 48, 75, 108, 121};

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);
    }

    @Test
    void testGCI69() {
        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI69/NoFunctionCallWhenDeclaringForLoop.java";
        String ruleId = "creedengo-java:GCI69";
        String ruleMsg = "Do not call a function when declaring a for-type loop";
        int[] startLines = new int[]{65, 73, 81, 109, 130};
        int[] endLines = new int[]{65, 73, 81, 109, 130};

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);
    }

    @Test
    void testGCI28() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI28/OptimizeReadFileExceptionCheck.java";

        int[] startLines = new int[]{34};

        int[] endLines = new int[]{34};

        String ruleId = "creedengo-java:GCI28";
        String ruleMsg = "Optimize Read File Exceptions";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);
    }

    @Test
    void testGCI28_2() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI28/OptimizeReadFileExceptionCheck2.java";

        int[] startLines = new int[]{32};

        int[] endLines = new int[]{32};

        String ruleId = "creedengo-java:GCI28";
        String ruleMsg = "Optimize Read File Exceptions";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);
    }

    @Test
    void testGCI28_3() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI28/OptimizeReadFileExceptionCheck3.java";

        int[] startLines = new int[]{32};

        int[] endLines = new int[]{32};

        String ruleId = "creedengo-java:GCI28";
        String ruleMsg = "Optimize Read File Exceptions";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);
    }

    @Test
    void testGCI28_4() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI28/OptimizeReadFileExceptionCheck4.java";

        int[] startLines = new int[]{31};

        int[] endLines = new int[]{31};

        String ruleId = "creedengo-java:GCI28";
        String ruleMsg = "Optimize Read File Exceptions";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);
    }

    @Test
    void testGCI28_5() {

        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI28/OptimizeReadFileExceptionCheck5.java";

        int[] startLines = new int[]{31};

        int[] endLines = new int[]{31};

        String ruleId = "creedengo-java:GCI28";
        String ruleMsg = "Optimize Read File Exceptions";

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines);
    }

    @Test
    void testGCI94() {
        String filePath = "src/main/java/org/greencodeinitiative/creedengo/java/checks/GCI94/UseOptionalOrElseGetVsOrElse.java";
        String ruleId = "creedengo-java:GCI94";
        String ruleMsg = "Use optional orElseGet instead of orElse.";
        int[] startLines = new int[]{27};
        int[] endLines = new int[]{27};

        checkIssuesForFile(filePath, ruleId, ruleMsg, startLines, endLines, SEVERITY, TYPE, EFFORT_1MIN);
    }

}
